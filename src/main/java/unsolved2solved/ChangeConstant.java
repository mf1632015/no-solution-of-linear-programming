package unsolved2solved;

import ilog.concert.IloConstraint;
import model.Inequality;
import model.OptimizeResult;
import model.TreePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
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
        switch (type) {
            case 1:
                return extendRange(allConstraints, iisConstraints);
            case 2:
                return dwindleRange(allConstraints, iisConstraints);
            case 3:
                return microTuning(allConstraints, iisConstraints);
            default:
                return microTuning(allConstraints, iisConstraints);
        }

    }

    public static OptimizeResult changeValue(String allConstraints, IloConstraint[] iis) {
        return null;
    }

    // 如果有硬约束和软约束，需要分别进行处理的
    private OptimizeResult extendRange(String allConstraints, String[] iisConstraints) {
        OptimizeResult optimizeResult = new OptimizeResult("增大右值", "");
        ArrayList<Inequality> inequalityArrayList = new InequationSimplify().simplifyInequalities(iisConstraints);
        //先找到没有former的不等式
        ArrayList<Inequality> rootList = findRoot(inequalityArrayList);

        //找到所有path
        ArrayList<TreePath> treePaths = findAllPath(rootList);

        //处理path，找到所有矛盾的变量

        //从没有former的不等式开始（这是root），依次next，求不等式le和ge所有的和,并将所有的都化简为最简形式
//        ArrayList<Inequality> simplifiesList = simplifyEqualities(rootList);
        //可能是好几组不可行，分别进行处理,对小于等于那一边，也就是le那一边的数字，进行扩大
//        ArrayList<Inequality> resolvedInequality = new ArrayList<Inequality>();
//        for (int i = 0; i < simplifiesList.size(); i++) {
//            Inequality inequality1 = simplifiesList.get(i);
//            if (resolvedInequality.contains(inequality1)) {
//                continue;
//            }
//            for (int j = 0; j < simplifiesList.size(); j++) {
//                Inequality inequality2 = simplifiesList.get(j);
//                if (j == i || resolvedInequality.contains(inequality2)) {
//                    continue;
//                }
//                if (inequality1.getSubtractor().equals(inequality2.getSubtractor()) && inequality1.getMinuend().equals(inequality2.getMinuend())) {
//                    double le1 = inequality1.getLe();
//                    double ge1 = inequality1.getGe();
//                    double le2 = inequality2.getLe();
//                    double ge2 = inequality2.getGe();
//                    String valueChange = optimizeResult.getValueChange();
//                    if (le1 < 0) {
//                        //修改allConstraints
//                        allConstraints = changeAllConstraintsByExtend(allConstraints, inequality2, ge1 - le2, rootList, optimizeResult);
//                    } else {
//                        allConstraints = changeAllConstraintsByExtend(allConstraints, inequality1, ge2 - le1, rootList, optimizeResult);
//                    }
//                    resolvedInequality.add(inequality1);
//                    resolvedInequality.add(inequality2);
//                    optimizeResult.setValueChange(valueChange);
//                }
//            }
//        }
        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }

    private OptimizeResult dwindleRange(String allConstraints, String[] iisConstraints) {
        OptimizeResult optimizeResult = new OptimizeResult("减小左值", "");
        ArrayList<Inequality> inequalityArrayList = new InequationSimplify().simplifyInequalities(iisConstraints);
        //先找到没有former的不等式
        ArrayList<Inequality> rootList = findRoot(inequalityArrayList);
        //找到所有path
        ArrayList<TreePath> treePaths = findAllPath(rootList);
        /*
        //从没有former的不等式开始（这是root），依次next，求不等式le和ge所有的和,并将所有的都化简为最简形式
        ArrayList<Inequality> simplifiesList = simplifyEqualities(rootList);
        ArrayList<Inequality> resolvedInequality = new ArrayList<Inequality>();
        for (int i = 0; i < simplifiesList.size(); i++) {
            Inequality inequality1 = simplifiesList.get(i);
            if (resolvedInequality.contains(inequality1)) {
                continue;
            }
            for (int j = 0; j < simplifiesList.size(); j++) {
                Inequality inequality2 = simplifiesList.get(j);
                if (j == i || resolvedInequality.contains(inequality2)) {
                    continue;
                }
                if (inequality1.getSubtractor().equals(inequality2.getSubtractor()) && inequality1.getMinuend().equals(inequality2.getMinuend())) {
                    double le1 = inequality1.getLe();
                    double ge1 = inequality1.getGe();
                    double le2 = inequality2.getLe();
                    double ge2 = inequality2.getGe();
                    String valueChange = optimizeResult.getValueChange();
                    if (le1 < 0) {
                        //修改allConstraints
                        allConstraints = changeAllConstraintsByDwindle(allConstraints, inequality1, ge1 - le2, rootList, optimizeResult);
                    } else {
                        allConstraints = changeAllConstraintsByDwindle(allConstraints, inequality2, ge2 - le1, rootList, optimizeResult);
                    }
                    resolvedInequality.add(inequality1);
                    resolvedInequality.add(inequality2);
                    optimizeResult.setValueChange(valueChange);
                }
            }
        }*/
        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }

    private OptimizeResult microTuning(String allConstraints, String[] iisConstraints) {
        OptimizeResult optimizeResult = new OptimizeResult("均匀微调", "");
        ArrayList<Inequality> inequalityArrayList = new InequationSimplify().simplifyInequalities(iisConstraints);
        //先找到没有former的不等式
        ArrayList<Inequality> rootList = findRoot(inequalityArrayList);
        //找到所有path
        ArrayList<TreePath> treePaths = findAllPath(rootList);
        /*
        //从没有former的不等式开始（这是root），依次next，求不等式le和ge所有的和,并将所有的都化简为最简形式
        ArrayList<Inequality> simplifiesList = simplifyEqualities(rootList);
        ArrayList<Inequality> resolvedInequality = new ArrayList<Inequality>();
        for (int i = 0; i < simplifiesList.size(); i++) {
            Inequality inequality1 = simplifiesList.get(i);
            if (resolvedInequality.contains(inequality1)) {
                continue;
            }
            for (int j = 0; j < simplifiesList.size(); j++) {
                Inequality inequality2 = simplifiesList.get(j);
                if (j == i || resolvedInequality.contains(inequality2)) {
                    continue;
                }
                if (inequality1.getSubtractor().equals(inequality2.getSubtractor()) && inequality1.getMinuend().equals(inequality2.getMinuend())) {
                    double le1 = inequality1.getLe();
                    double ge1 = inequality1.getGe();
                    double le2 = inequality2.getLe();
                    double ge2 = inequality2.getGe();
                    String valueChange = optimizeResult.getValueChange();
                    if (le1 < 0) {
                        //修改allConstraints
                        allConstraints = changeAllConstraintsByMicroTunning(allConstraints, inequality1, inequality2, ge1 - le2, rootList, optimizeResult);
                    } else {
                        allConstraints = changeAllConstraintsByMicroTunning(allConstraints, inequality2, inequality2, ge2 - le1, rootList, optimizeResult);
                    }
                    resolvedInequality.add(inequality1);
                    resolvedInequality.add(inequality2);
                    optimizeResult.setValueChange(valueChange);
                }
            }
        }
        */
        optimizeResult.setNewConstraints(allConstraints);
        return optimizeResult;
    }

    private String changeAllConstraintsByExtend(String allConstraints, Inequality inequality, double value, ArrayList<Inequality> rootList, OptimizeResult optimizeResult) {
        // 0.对于要修改的小于等于的IIS，根据value值，除以修改的个数，然后每个小于等于都加上这个数字
        double le = inequality.getLe();
        double ge = inequality.getGe();
        String leftVariable = inequality.getSubtractor();
        String rightVariable = inequality.getMinuend();
        double absGe = Math.abs(ge);
        double addValue = value / absGe + 1;
        // 1.找到要修改的root不等式
        for (Inequality inequality1 : rootList) {
            if (inequality1.getSubtractor().equals(leftVariable) && inequality1.getGe() < 0) {

            }
        }

        // 2.遍历该根root不等式，找到要修改的路径，对路径中每个不等式

        // 3.
        return null;
    }

    private String changeAllConstraintsByDwindle(String allConstraints, Inequality inequality, double value, ArrayList<Inequality> rootList, OptimizeResult optimizeResult) {
        //对于要修改的大于等于的IIS，根据value值，根据每个大于0的IIS不等式的值，按比例分配减去的值；
        //比如，要修改的有10个不等式，但是其中只有三个不等式大于0，分别是10<=;20<=;30<=,那么将value值分为6份，10对应1份，20对应2份，30对应3份

        return null;
    }

    private String changeAllConstraintsByMicroTunning(String allConstraints, Inequality inequality2, Inequality inequality, double value, ArrayList<Inequality> rootList, OptimizeResult optimizeResult) {


        return null;
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
        if(root == null){
            return treePaths;
        }else if(!root.hasNext()){
            TreePath treePath = new TreePath();
            treePath.addNode(root);
            treePaths.add(treePath);
        }else{

            ArrayList<Inequality> nextList = root.getNextList();
            for(Inequality inequality:nextList){
                TreePath treePath = new TreePath();
                treePath.addNode(root);
                traverse(inequality,treePath);
                treePaths.add(treePath);
            }

        }
        return treePaths;

    }

    private void traverse(Inequality inequality, TreePath treePath) {
        if(!inequality.hasNext()){
            treePath.addNode(inequality);
        }else{
            treePath.addNode(inequality);
            ArrayList<Inequality> nextList = inequality.getNextList();
            for(Inequality next:nextList){
                traverse(next,treePath);
                treePath.clear();
            }
        }
    }


    private ArrayList<Inequality> findRoot(ArrayList<Inequality> inequalityArrayList) {

        //TODO 还有一种情况需要考虑，如下所示，并不能简单的进行只找root来解决，所以不能说具体实现，应该只考虑每一步的算法，也就是步骤。管他具体实现怎么搞呢。
        /*
                "IloRange  : 5.0 <= (1.0*a - 1.0*b) <= infinity\n"+
                "IloRange  : 7.0 <= (1.0*b - 1.0*c) <= infinity\n"+
                "IloRange  : 7.0 <= (1.0*b - 1.0*d) <= infinity\n"+
                "IloRange  : -infinity <= (1.0*a - 1.0*c) <= 6.0\n" +
                "IloRange  : -infinity <= (1.0*b - 1.0*d) <= 6.0";
         */
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
    private static OptimizeResult changeValue(String constraints, String[] iisColnstraints) {
        OptimizeResult optimizeResult = new OptimizeResult("Change constant", "");
        String[] originalConstraints = constraints.split("\n");
        double maxRange = 0;//用来存储小于等于的和
        Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");//小数
        Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");//整数
        StringBuilder constraintsToModify = new StringBuilder();//存储待修改的约束

        // 1 找到所有大于等于的和,同时记录所有包含小于等于某个值的约束的位置
        int[] leIndex = new int[iisColnstraints.length];
        Arrays.fill(leIndex, -1);
        int i = 0;
        for (String iisConstrain : iisColnstraints) {
            String[] splitIIS = iisConstrain.split("<=");
            if (regexDouble.matcher(splitIIS[0]).matches() || regexInt.matcher(splitIIS[0]).matches()) {
                maxRange += Double.parseDouble(splitIIS[0]);
            }
            if ((splitIIS.length == 2 && (regexDouble.matcher(splitIIS[1]).matches() || regexInt.matcher(splitIIS[1]).matches())) || splitIIS.length == 3) {
                leIndex[i] = 1;
            }
            i++;
        }
        maxRange += 10;
        // 2 遍历每一个IIS约束，只要包括大于等于的，就需要将原来约束中的对应的该约束小于等于的值改为maxRange；
        for (int j = 0; j < iisColnstraints.length; j++) {
            //所有不包含小于等于的约束，都删掉。
            if (leIndex[j] == -1) {
                continue;
            }
            String[] splitIIS = iisColnstraints[j].split("<=");

            //遍历原始约束，找到包含该约束的项目
            for (int k = 0; k < originalConstraints.length; k++) {
                String originConstraint = originalConstraints[k];
                if (splitIIS.length == 2 && originConstraint.contains(splitIIS[0])) {
                    String[] splitOrigin = originConstraint.split("<=");
                    //如果原始约束经过<=分割后长度为2，且下标1的元素为浮点数或者整数
                    if (splitOrigin.length == 2 && (regexDouble.matcher(splitOrigin[1]).matches() || regexInt.matcher(splitOrigin[1]).matches())
                            && judgeEquals(Double.parseDouble(splitIIS[1]), Double.parseDouble(splitOrigin[1]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k] = splitOrigin[0] + "<=" + maxRange;
                    } else if (splitOrigin.length == 3 && judgeEquals(Double.parseDouble(splitIIS[1]), Double.parseDouble(splitOrigin[2]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k] = splitOrigin[0] + "<=" + splitOrigin[1] + "<=" + maxRange;
                    }
                } else if (splitIIS.length == 3 && originConstraint.contains(splitIIS[1])) {
                    String[] splitOrigin = originConstraint.split("<=");
                    //如果原始约束经过<=分割后长度为2，且下标1的元素为浮点数或者整数
                    if (splitOrigin.length == 2 && (regexDouble.matcher(splitOrigin[1]).matches() || regexInt.matcher(splitOrigin[1]).matches())
                            && judgeEquals(Double.parseDouble(splitIIS[2]), Double.parseDouble(splitOrigin[1]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k] = splitOrigin[0] + "<=" + maxRange;
                    } else if (splitOrigin.length == 3 && judgeEquals(Double.parseDouble(splitIIS[2]), Double.parseDouble(splitOrigin[2]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k] = splitOrigin[0] + "<=" + splitOrigin[1] + "<=" + maxRange;
                    }
                }
            }
        }

        //将新的约束组合起来
        StringBuilder newConstraints = new StringBuilder();
        for (String constraint : originalConstraints) {
            newConstraints.append(constraint).append("\n");
        }
        optimizeResult.setNewConstraints(newConstraints.toString());
        optimizeResult.setValueChange("The constraints:\"" + constraintsToModify.toString() + "\" less than value were change to " + maxRange);
        return optimizeResult;
    }

    private static boolean judgeEquals(double v, double v1) {
        return v == v1;
    }

    public static void main(String[] args) {
        //测试方法 changeValue
//        String constraints = "0<=x-y<=5\n6<=x-y<=20\ny-z<=3";
//        String iisConstraints[] = {"x-y<=5.0", "6<=x-y"};
//        OptimizeResult optimizeResult = ChangeConstant.changeValue(constraints,iisConstraints);
//        System.out.println(optimizeResult.getNewConstraints());
//        System.out.println(optimizeResult);

        //测试extendRange

        String inequalities =
//                "IloRange  : -infinity <= (-1.0*b1 + 1.0*c2) <= 4.0\n" +
//                " IloRange  : 5.0 <= (-1.0*b1 + 1.0*d2) <= infinity\n" +
//                " IloRange  : 12.0 <= (-1.0*d3 + 1.0*d4) <= infinity\n" +
//                " IloRange  : 12.0 <= (1.0*f2 - 1.0*f1) <= infinity\n" +
//                " IloRange  : 12.0 <= (-1.0*j5 + 1.0*j6) <= infinity\n" +
//                " IloRange  : 12.0 <= (1.0*j8 - 1.0*j7) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*d2 + 1.0*d3) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*d4 + 1.0*f1) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*f2 + 1.0*j2) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j2 + 1.0*j3) <= infinity\n" +
//                " IloRange  : 0.0 <= (1.0*j5 - 1.0*j3) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j6 + 1.0*j7) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j8 + 1.0*j9) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j9 + 1.0*j10) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j10 + 1.0*j12) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*j12 + 1.0*m4) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*m4 + 1.0*b5) <= infinity\n" +
//                " IloRange  : 0.0 <= (-1.0*b5 + 1.0*c1) <= infinity\n" +
//                " IloRange  : 0.0 <= (1.0*c2 - 1.0*c1) <= infinity\n" +
                " IloRange  : 5.0 <= (1.0*a - 1.0*b) <= infinity\n" +
                        " IloRange  : 7.0 <= (1.0*b - 1.0*d) <= infinity\n" +
                        " IloRange  : 6.0 <= (1.0*b - 1.0*c) <= infinity\n" +
                        " IloRange  : 3.0 <= (1.0*b - 1.0*e) <= infinity\n" +
                        " IloRange  : 4.0 <= (1.0*a - 1.0*e) <= infinity\n" +
                        " IloRange  : 7.0 <= (1.0*b - 1.0*c) <= infinity\n" +
                        " IloRange  : -infinity <= (1.0*a - 1.0*c) <= 6.0\n" +
                        " IloRange  : -infinity <= (1.0*b - 1.0*d) <= 6.0\n"+"" +
                        " IloRange  : -infinity <= (1.0*d - 1.0*e) <= 6.0";
//                " IloRange  : 5.0 <= (1.0*a - 1.0*b) <= infinity\n"+
//                " IloRange  : 7.0 <= (1.0*b - 1.0*c) <= infinity\n"+
//                " IloRange  : -infinity <= (1.0*a - 1.0*c) <= 6.0";
        String[] inequalitiesAfterResolve = IISConstraintResolve.resolveIIS(inequalities.split("\n"));
        new ChangeConstant().extendRange("", inequalitiesAfterResolve);
    }
}
