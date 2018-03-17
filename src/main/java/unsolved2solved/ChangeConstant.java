package unsolved2solved;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import model.Inequality;
import model.OptimizeResult;
import model.TreePath;
import model.TreePathPair;
import withcplex.LpModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 修改优化约束的常量（不等号右边）的值
 * 只需要先计算将IIS中的所有大于等于的值的和maxRange，然后把这个和变成iis中小于等于的右边的值即可
 * 比如5<=x-y<=10    12<=x-y<=15    修改为：5<=x-y<=17  12<=x-y<=17  即可。
 * 在具体实现中，为了防止特殊情况的发生，将maxRange+10作为小于等于的值；
 */
public class ChangeConstant {

    public OptimizeResult changeValue(String allConstraints, IloConstraint[] iis, String[] hardConstraints, int type) {
        String[] iisConstraints = IISConstraintResolve.resolveIIS(iis);
        ArrayList<Inequality> inequalityArrayList = new InequationSimplify().simplifyInequalities(iisConstraints);
        //进行分类，分为小于等于和大于等于两类
        ArrayList<Inequality> leInequalityList = new ArrayList<Inequality>();
        ArrayList<Inequality> geInequalityList = new ArrayList<Inequality>();
        classify(inequalityArrayList, leInequalityList, geInequalityList);

        switch (type) {
            case 1:
                return extendRange(allConstraints, geInequalityList, leInequalityList);
            case 2:
                return dwindleRange(allConstraints, geInequalityList, leInequalityList);
            case 3:
                return microTuning(allConstraints, geInequalityList, leInequalityList);
            default:
                return microTuning(allConstraints, geInequalityList, leInequalityList);
        }
    }


    // 如果有硬约束和软约束，需要分别进行处理的
    private OptimizeResult extendRange(String allConstraints, ArrayList<Inequality> geInequalityList, ArrayList<Inequality> leInequalityList) {
        OptimizeResult optimizeResult = new OptimizeResult("增大右值", "");
        // 1. 分别对大于等于和小于等于进行求和
        double leTotal = 0.0;
        double geTotal = 0.0;
        for (Inequality inequality : leInequalityList) {
            leTotal += inequality.getLe();
        }
        for (Inequality inequality : geInequalityList) {
            geTotal += inequality.getGe();
        }
        //必须是le<ge，否则就不是IIS集合了

        // 2. 求二者的差
        double difference = geTotal - leTotal + 5;

        // 3. 然后根据差值修改
        allConstraints = changeAllConstraintsByExtend(allConstraints, leInequalityList, difference, optimizeResult);

        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }


    private OptimizeResult dwindleRange(String allConstraints, ArrayList<Inequality> geInequalityList, ArrayList<Inequality> leInequalityList) {
        OptimizeResult optimizeResult = new OptimizeResult("减小左值", "");
        // 1. 分别对大于等于和小于等于进行求和
        double leTotal = 0.0;
        double geTotal = 0.0;
        for (Inequality inequality : leInequalityList) {
            leTotal += inequality.getLe();
        }
        for (Inequality inequality : geInequalityList) {
            geTotal += inequality.getGe();
        }
        //必须是le<ge，否则就不是IIS集合了

        // 2. 求二者的差
        double difference = geTotal - leTotal;

        // 3. 然后根据差值修改
        allConstraints = changeAllConstraintsByDwindle(allConstraints, geInequalityList, difference, optimizeResult);

        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }

    private OptimizeResult microTuning(String allConstraints, ArrayList<Inequality> geInequalityList, ArrayList<Inequality> leInequalityList) {
        OptimizeResult optimizeResult = new OptimizeResult("均匀微调", "");

        // 1. 分别对大于等于和小于等于进行求和
        double leTotal = 0.0;
        double geTotal = 0.0;
        for (Inequality inequality : leInequalityList) {
            leTotal += inequality.getLe();
        }
        for (Inequality inequality : geInequalityList) {
            geTotal += inequality.getGe();
        }
        //必须是le<ge，否则就不是IIS集合了

        // 2. 求二者的差
        double difference = geTotal - leTotal;

        // 3. 然后根据差值修改
        allConstraints = changeAllConstraintsByMicroTunning(allConstraints, leInequalityList, geInequalityList, difference, optimizeResult);

        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }

    private void classify(ArrayList<Inequality> inequalityArrayList, ArrayList<Inequality> leInequalityList, ArrayList<Inequality> geInequalityList) {
        for (Inequality inequality : inequalityArrayList) {
            if (inequality.getLe() < 0) {
                geInequalityList.add(inequality);
            } else {
                leInequalityList.add(inequality);
            }
        }
    }

    private String changeAllConstraintsByExtend(String allConstraints, ArrayList<Inequality> leInequalityList, double value, OptimizeResult optimizeResult) {
        int size = leInequalityList.size();
        double averageValue = value / size;
        for (Inequality inequality : leInequalityList) {
            double originLe = inequality.getLe();
            double newLe = originLe + averageValue;
            inequality.setLe(newLe);
            inequality.setValueChanged(averageValue);

            String valueChange = optimizeResult.getValueChange();
            valueChange += "change " + inequality.getSubtractor() + "-" + inequality.getMinuend() + "<=" + originLe + " to " + inequality.getSubtractor() + "-" + inequality.getMinuend() + "<=" + newLe+"; ";
            optimizeResult.setValueChange(valueChange);
        }
        return changeValue(allConstraints, leInequalityList);
    }


    private String changeAllConstraintsByDwindle(String allConstraints, ArrayList<Inequality> geInequalityList, double value, OptimizeResult optimizeResult) {
        //对于要修改的大于等于的IIS，根据value值，根据每个大于0的IIS不等式的值，按比例分配减去的值；
        //比如，要修改的有10个不等式，但是其中只有三个不等式大于0，分别是10<=;20<=;30<=,那么将value值分为6份，10对应1份，20对应2份，30对应3份
        double geTotal = 0.0;
        for (Inequality inequality : geInequalityList) {
            geTotal += inequality.getGe();
        }
        for (Inequality inequality : geInequalityList) {
            double originGe = inequality.getGe();
            double newGe = originGe - originGe / geTotal * value;
            newGe = newGe * 9 / 10;
            inequality.setGe(newGe);
            inequality.setValueChanged(originGe - newGe);

            if ((originGe - newGe) != 0) {
                String valueChange = optimizeResult.getValueChange();
                valueChange += "change " + originGe + "<=" + inequality.getSubtractor() + "-" + inequality.getMinuend() + " to " + newGe + "<=" + inequality.getSubtractor() + "-" + inequality.getMinuend()+"; ";
                optimizeResult.setValueChange(valueChange);
            }
        }
        return changeValue(allConstraints, geInequalityList);
    }

    private String changeAllConstraintsByMicroTunning(String allConstraints, ArrayList<Inequality> leInequalityList, ArrayList<Inequality> geInequalityList, double value, OptimizeResult optimizeResult) {
        double totalLeGe = 0.0;
        for (Inequality inequality : leInequalityList) {
            totalLeGe += inequality.getLe();
        }
        for (Inequality inequality : geInequalityList) {
            totalLeGe += inequality.getGe();
        }

        for (Inequality inequality : leInequalityList) {
            double originLe = inequality.getLe();
            double newLe = originLe + value / totalLeGe * originLe + 0.5;
            inequality.setLe(newLe);
            inequality.setValueChanged(newLe - originLe);
            String valueChange = optimizeResult.getValueChange();
            valueChange += "change " + inequality.getSubtractor() + "-" + inequality.getMinuend() + "<=" + originLe + " to " + inequality.getSubtractor() + "-" + inequality.getMinuend() + "<=" + newLe+"; ";
            optimizeResult.setValueChange(valueChange);

        }

        for (Inequality inequality : geInequalityList) {
            double originGe = inequality.getGe();
            double newGe = originGe - originGe / totalLeGe * value;
            inequality.setGe(newGe);
            inequality.setValueChanged(originGe - newGe);

            if ((originGe - newGe) != 0) {
                String valueChange = optimizeResult.getValueChange();
                valueChange += "change " + originGe + "<=" + inequality.getSubtractor() + "-" + inequality.getMinuend() + " to " + newGe + "<=" + inequality.getSubtractor() + "-" + inequality.getMinuend()+"; ";
                optimizeResult.setValueChange(valueChange);
            }
        }
        geInequalityList.addAll(leInequalityList);
        return changeValue(allConstraints, geInequalityList);
    }

    private ArrayList<TreePathPair> findTreePathPair(ArrayList<TreePath> treePaths) {
        ArrayList<TreePathPair> treePathPairs = new ArrayList<TreePathPair>();
        //存储已处理的treePath
        ArrayList<TreePath> treePathResolved = new ArrayList<TreePath>();
        // 1.首先找首尾相同的path对
        for (TreePath treePath : treePaths) {
            if (treePathResolved.contains(treePath)) {
                continue;
            }
            String firstVariable = treePath.getFirst().getSubtractor();
            String lastVariable = treePath.getLast().getMinuend();
            double firstLe = treePath.getFirst().getLe();
            for (TreePath treePathAnother : treePaths) {
                if (treePath == treePathAnother || treePathResolved.contains(treePathAnother)) {
                    continue;
                }
                String firstVariableAnother = treePathAnother.getFirst().getSubtractor();
                String lastVariableAnother = treePathAnother.getLast().getMinuend();
                double firstLeAnother = treePathAnother.getFirst().getLe();
                //如果首尾相同，且符号不同，说明一个是小于等于，一个是大于等于
                if (firstVariable.equals(firstVariableAnother) && lastVariable.equals(lastVariableAnother)
                        && ((firstLe < 0 && firstLeAnother >= 0) || (firstLe >= 0 && firstLeAnother < 0))) {
                    //则这是一个TreePath对，添加
                    TreePathPair treePathPair = new TreePathPair(treePath, treePathAnother, 0, 0);
                    //添加tag
                    //添加allLe和AllGe
                    if (firstLe < 0) {
                        treePathPair.setLeTag(2);
                        ArrayList<Inequality> inequalities = treePath.getPath();
                        double geTotal = 0.0;
                        for (Inequality inequality : inequalities) {
                            geTotal += inequality.getGe();
                        }
                        ArrayList<Inequality> inequalitiesAnother = treePathAnother.getPath();
                        double leTotal = 0.0;
                        for (Inequality inequality : inequalitiesAnother) {
                            leTotal += inequality.getLe();
                        }
                        treePathPair.setLeTotal(leTotal);
                        treePathPair.setGeTotal(geTotal);
                    } else {
                        treePathPair.setLeTag(1);

                    }
                    treePathPairs.add(treePathPair);
                    //已处理
                    treePathResolved.add(treePath);
                    treePathResolved.add(treePathAnother);

                }

            }
        }
        // 2.然后找尾部相同，但是头部不同的对。必然是长一些的treepath包含着某个短的treepath首尾相同的路径
        // 且这些长路径，一定在前面已经处理过的路径当中
        for (TreePath treePath : treePaths) {
            if (treePathResolved.contains(treePath)) {
                continue;
            }
            String firstVariable = treePath.getFirst().getSubtractor();
            String lastVariable = treePath.getLast().getMinuend();
            double firstLe = treePath.getFirst().getLe();
            for (TreePath treePathAnother : treePathResolved) {
                String firstVariableAnother = treePathAnother.getFirst().getSubtractor();
                String lastVariableAnother = treePathAnother.getLast().getMinuend();
                double firstLeAnother = treePathAnother.getFirst().getLe();
                if (lastVariable.equals(lastVariableAnother) && !firstVariable.equals(firstVariable)
                        && ((firstLe < 0 && firstLeAnother >= 0) || (firstLe >= 0 && firstLeAnother < 0))) {

                }
            }
        }

        return treePathPairs;
    }


    private ArrayList<Inequality> simplifyEqualities(ArrayList<Inequality> rootList) {
        ArrayList<TreePath> treePaths = new ArrayList<TreePath>();
        //记录所有的路径
        ArrayList<Inequality> simplifiesList = new ArrayList<Inequality>();
        for (Inequality inequality : rootList) {
            double le = 0.0;
            double ge = 0.0;
            String leftVariable = inequality.getSubtractor();
            String rightVariable = inequality.getMinuend();
            Stack<Inequality> preparetoGoThrough = new Stack<Inequality>();
            preparetoGoThrough.push(inequality);
            TreePath treePath = new TreePath();
            while (!preparetoGoThrough.empty()) {
                Inequality currentInequality = preparetoGoThrough.pop();
                if (inequality.getLe() * currentInequality.getLe() >= 0 && inequality.getGe() * currentInequality.getGe() >= 0) {
                    treePath.addNode(currentInequality);
                    rightVariable = currentInequality.getMinuend();
                    //不考虑两边都有数字的情况，假设根节点是le为-1，那么之后的所有不等式le都是-1
                    le += currentInequality.getLe();
                    ge += currentInequality.getGe();
                    ArrayList<Inequality> nextList = currentInequality.getNextList();
                    //如果到了叶子节点，则保存一下
                    if (nextList.isEmpty()) {
                        Inequality newInequality = new Inequality(le, ge, leftVariable, rightVariable);
                        simplifiesList.add(newInequality);
                        treePaths.add(treePath.clone());
                        //还原，直到下一条路径开始
                        le -= currentInequality.getLe();
                        ge -= currentInequality.getGe();
                        treePath.removeLastNode();
                    } else { // 否则则继续放到栈里面去，继续遍历
                        for (Inequality nextInequality : nextList) {
                            preparetoGoThrough.push(nextInequality);
                        }
                    }
                } else {
                    simplifiesList.add(currentInequality);
                }
            }
        }
        //然后遍历simplifiesList,找到其中不成对的组合，将这些组合修改使之成对存在
        return simplifiesList;
    }

    private ArrayList<TreePath> findAllPath(ArrayList<Inequality> rootList) {
        ArrayList<TreePath> treePaths = new ArrayList<TreePath>();

        if (rootList == null || rootList.isEmpty()) {
            return treePaths;
        }

        for (Inequality root : rootList) {
            treePaths.addAll(findAllPath(root));
        }
        return treePaths;
    }

    private Collection<? extends TreePath> findAllPath(Inequality root) {
        ArrayList<TreePath> treePaths = new ArrayList<TreePath>();
        if (root == null) {
            return treePaths;
        } else if (!root.hasNext()) {
            TreePath treePath = new TreePath();
            treePath.addNode(root);
            treePaths.add(treePath);
        } else {
            ArrayList<Inequality> nextList = root.getNextList();
            for (Inequality inequality : nextList) {
                TreePath treePath = new TreePath();
                treePath.addNode(root);
                traverse(inequality, treePath, treePaths);
            }

        }
        return treePaths;

    }

    private void traverse(Inequality inequality, TreePath treePath, ArrayList<TreePath> treePaths) {
        if (!inequality.hasNext()) {
            treePath.addNode(inequality);
            treePaths.add(treePath);
        } else {
            treePath.addNode(inequality);
            ArrayList<Inequality> nextList = inequality.getNextList();
            for (Inequality next : nextList) {
                TreePath newTreePath = treePath.clone();
                traverse(next, newTreePath, treePaths);

            }
        }
    }


    private ArrayList<Inequality> findRoot(ArrayList<Inequality> inequalityArrayList) {
        ArrayList<Inequality> rootList = new ArrayList<Inequality>();
        //先找到没有former的不等式
        for (Inequality inequality : inequalityArrayList) {
            if (inequality.isRoot()) {
                rootList.add(inequality);
            }
        }
        return rootList;
    }

    //TODO 将所有的小于等于的约束值增大为所有其他约束大于等于的和，即可让其变得有解
    private static String changeValue(String constraints, ArrayList<Inequality> inequalityArrayList) {

        String[] originalConstraints = constraints.split("\n");

        Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");
        Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");

        // 1 遍历每一个IIS约束，只要包括大于等于的，就需要将原来约束中的对应的该约束小于等于的值改为maxRange；
        for (Inequality inequality : inequalityArrayList) {
            String leftVariable = inequality.getSubtractor();
            String rightVariable = inequality.getMinuend();
            double le = inequality.getLe();
            double ge = inequality.getGe();
            double valueChange = inequality.getValueChanged();
            // 1.1遍历原始约束，找到包含该约束的项目
            for (int k = 0; k < originalConstraints.length; k++) {
                String originConstraint = originalConstraints[k];
                String[] originConsSplit = originConstraint.split("<=");
                // 1.2 判断原始约束是否和iis约束一致
                double originLe = -1;
                double originGe = -1;

                if (originConsSplit.length == 3) {
                    originGe = Double.parseDouble(originConsSplit[0]);
                    originLe = Double.parseDouble(originConsSplit[2]);
                    String[] variables = originConsSplit[1].split("-");
                    String originLeftVariable = variables[0];
                    String originRightVariable = variables[1];
                    //如果对应的不等式的数字相等，且减数和被减数也对应相等
                    if (((le >= 0 && originLe == (le - valueChange) || (ge >= 0 && originGe == (ge + valueChange)))
                            && originLeftVariable.contains(leftVariable)
                            && originRightVariable.contains(rightVariable))
                            ) {
                        // 1.3 直接修改originalConstraints[k]
                        if (le > 0) {
                            originalConstraints[k] = originConsSplit[0] + "<=" + originConsSplit[1] + "<=" + le;
                        }else if(ge>0){
                            originalConstraints[k] = ge + "<=" + originConsSplit[1] + "<=" + originConsSplit[2];
                        }
                    }
                } else if (originConsSplit.length == 2) {
                    // 如果原始不等式类似于a-b<=5，则要求le>=0,且le-valueChange == originLe
                    if((regexDouble.matcher(originConsSplit[1]).matches() || regexInt.matcher(originConsSplit[1]).matches())
                            &&le>=0
                            && (le-valueChange)==Double.parseDouble(originConsSplit[1])){
                        String[] variables = originConsSplit[0].split("-");
                        String originLeftVariable = variables[0];
                        String originRightVariable = variables[1];
                        if(originLeftVariable.contains(leftVariable)
                                && originRightVariable.contains(rightVariable)){
                            originalConstraints[k] = originConsSplit[0] + "<=" + le;
                        }
                    }

                    // 如果原始不等式类似于 5<= a-b ，则要求ge>=0;
                    if((regexDouble.matcher(originConsSplit[0]).matches() || regexInt.matcher(originConsSplit[0]).matches())
                            && ge>=0
                            && (ge+valueChange)==Double.parseDouble(originConsSplit[0])){
                        String[] variables = originConsSplit[1].split("-");
                        String originLeftVariable = variables[0];
                        String originRightVariable = variables[1];
                        if(originLeftVariable.contains(leftVariable)
                                && originRightVariable.contains(rightVariable)){
                            originalConstraints[k] = ge +"<=" + originConsSplit[1] ;
                        }
                    }
                }

            }
        }
        // 2. 将新的约束组合起来
        StringBuilder newConstraints = new StringBuilder();
        for (String constraint : originalConstraints) {
            newConstraints.append(constraint).append("\n");
        }
        return newConstraints.toString();
    }

    private static boolean judgeEquals(double v, double v1) {
        return v == v1;
    }

    public static void main(String[] args) {


        String inequalities =
                "IloRange  : -infinity <= (-1.0*b1 + 1.0*c2) <= 4.0\n" +
                        " IloRange  : 5.0 <= (-1.0*b1 + 1.0*d2) <= infinity\n" +
                        " IloRange  : 12.0 <= (-1.0*d3 + 1.0*d4) <= infinity\n" +
                        " IloRange  : 12.0 <= (1.0*f2 - 1.0*f1) <= infinity\n" +
                        " IloRange  : 12.0 <= (-1.0*j5 + 1.0*j6) <= infinity\n" +
                        " IloRange  : 12.0 <= (1.0*j8 - 1.0*j7) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*d2 + 1.0*d3) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*d4 + 1.0*f1) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*f2 + 1.0*j2) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j2 + 1.0*j3) <= infinity\n" +
                        " IloRange  : 0.0 <= (1.0*j5 - 1.0*j3) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j6 + 1.0*j7) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j8 + 1.0*j9) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j9 + 1.0*j10) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j10 + 1.0*j12) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*j12 + 1.0*m4) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*m4 + 1.0*b5) <= infinity\n" +
                        " IloRange  : 0.0 <= (-1.0*b5 + 1.0*c1) <= infinity\n" +
                        " IloRange  : 0.0 <= (1.0*c2 - 1.0*c1) <= infinity\n"+

                        " IloRange  : 5.0 <= (1.0*a - 1.0*b) <= infinity\n" +
                        " IloRange  : 6.0 <= (1.0*b - 1.0*c) <= infinity\n" +
                        " IloRange  : 6.0 <= (1.0*c - 1.0*d) <= infinity\n" +
                        " IloRange  : 6.0 <= (1.0*d - 1.0*e) <= infinity\n" +
                        " IloRange  : 6.0 <= (1.0*e - 1.0*f) <= infinity\n" +
                        " IloRange  : -infinity <= (1.0*a - 1.0*f) <= 16.0\n" +

                        //情况 2
                        " IloRange  : -infinity <= (1.0*b - 1.0*f) <= 16.0\n" +

                        // 情况3
                        " IloRange  : -infinity <= (1.0*b - 1.0*c) <= 2.0\n" +

                        //情况4
                        " IloRange  : 25.0 <= (1.0*g - 1.0*h) <= infinity\n" +
                        " IloRange  : -infinity <= (1.0*g - 1.0*d) <= 6.0\n" +
                        " IloRange  : -infinity <= (1.0*d - 1.0*e) <= 4.0\n" +
                        " IloRange  : -infinity <= (1.0*e - 1.0*h) <= 6.0"
;


        String[] inequalitiesAfterResolve = IISConstraintResolve.resolveIIS(inequalities.split("\n"));

        for (String str : inequalitiesAfterResolve) {
            System.out.println(str);
        }

        //结合使用cplex来做
        try {
            IloNumVar[][] var = new IloNumVar[1][];
            IloCplex model = new IloCplex();
            LpModel lpModel = new LpModel();
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : inequalitiesAfterResolve) {
                stringBuilder.append(str).append("\n");
            }
            lpModel.createModel(stringBuilder.toString(), model, var, null, false);
            ArrayList<OptimizeResult> optimizeResults = new ArrayList<OptimizeResult>();
            String allConstraints = stringBuilder.toString();
            while (!model.solve()) {
                IloConstraint[] cons = lpModel.getIIS(model);
                for (IloConstraint iloConstraint : cons) {
                    System.out.println(iloConstraint);
                }
                OptimizeResult optimizeResult = new ChangeConstant().changeValue(allConstraints, cons, null, 3);
                allConstraints = optimizeResult.getNewConstraints();
                optimizeResults.add(optimizeResult);
                model = new IloCplex();
                var = new IloNumVar[1][];
                lpModel.createModel(optimizeResult.getNewConstraints(),model,var,null,false);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for(OptimizeResult optimizeResult:optimizeResults){
                System.out.println(optimizeResult.getValueChange());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }

    }
}
