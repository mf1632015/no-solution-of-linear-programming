package utility;

import ilog.concert.IloConstraint;
import model.OptimizeResult;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 减少传入约束量的个数
 */
public class ReduceConstraints {

    //每次减少一个iis中的约束，从而得到一组Constraints
    //最终得到一个OptimizeMethod数组，每个均为减少了一个iis中一个不等式的约束。
    public static OptimizeResult[] reduceConstraints(String constraints, IloConstraint[] iis) {
        String[] iisAfterResolve = IISConstraintResolve.resolveIIS(iis);
        return reduceConstraints(constraints, iisAfterResolve);
    }

    public static OptimizeResult[] reduceConstraints(String constraints, String[] iisConstraints) {
        OptimizeResult[] optimizeResults = new OptimizeResult[iisConstraints.length];
        String[] originalConstraints = constraints.split("\n");

        for (int i = 0; i < iisConstraints.length; i++) {
//            String constrainToReduce =
            String iisConstraint = iisConstraints[i];
            String[] iisAfterSplit = iisConstraint.split("<=");
            Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");
            Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");
            double lb = -1;//low bound
            double ub = -1;//up bound
            // lb<=leftparam -rightParam<=up
            String rightParam;
            String leftParam;
            if (iisAfterSplit.length == 3) {
                //找到下边界和上边界
                lb = Double.parseDouble(iisAfterSplit[0]);
                ub = Double.parseDouble(iisAfterSplit[2]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[1].split("-");
                leftParam = params[0];
                rightParam = "-" + params[1];
            } else if (regexDouble.matcher(iisAfterSplit[0]).matches()) {
                //找到下边界
                lb = Double.parseDouble(iisAfterSplit[0]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[1].split("-");
                leftParam = params[0];
                rightParam = "-" + params[1];

            } else {
                //找到上边界
                ub = Double.parseDouble(iisAfterSplit[1]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[0].split("-");
                leftParam = params[0];
                rightParam = "-" + params[1];
            }
            StringBuilder stringBuilder = new StringBuilder();
            //删掉当前这个约束（对应前面求解的东东）
            String deleteConstrain = "";
            for (String constrain : originalConstraints) {
//                System.out.println("aaa");
                if (constrain.contains(leftParam) && constrain.contains(rightParam)) {
                    //TODO 对lb和ub进行处理
                    if (lb == -1 && ub != -1) {
                        String[] split = constrain.split("<=");
                        if (split.length == 3) {
                            if (judgeEquals(ub, split[2])) {// up相等才删除，不相等不删除
                                String newConstrain = split[0] + "<=" + split[1];
                                stringBuilder.append(newConstrain).append("\n");
                            }else{
                                stringBuilder.append(constrain).append("\n");
                            }
                        } else {
                            if(!((regexDouble.matcher(split[1]).matches()||regexInt.matcher(split[1]).matches())&&judgeEquals(ub,split[1]))){//相等不做处理，等效于删除
                                stringBuilder.append(constrain).append("\n");//除了相等才删除，其他的都添加，因此加了个！,取非，下同。
                            }
                        }
                    } else if (lb != -1 && ub == -1) {
                        String[] split = constrain.split("<=");
                        if (split.length == 3) {
                            if(judgeEquals(lb,split[0])) {
                                String newConstrain = split[1] + "<=" + split[2];
                                stringBuilder.append(newConstrain).append("\n");
                            }else{
                                stringBuilder.append(constrain).append("\n");
                            }
                        }else{//如果split.length==2 ，
                            if(!((regexDouble.matcher(split[0]).matches()||regexInt.matcher(split[0]).matches())&&judgeEquals(lb,split[0]))){
                                stringBuilder.append(constrain).append("\n");
                            }
                        }
                    }
                    else{
                        String[] split = constrain.split("<=");
                        if(split.length==3){
                            if(judgeEquals(lb,split[0])&&judgeEquals(ub,split[2])){
                                //do nothing 删除
                            }else if(judgeEquals(lb,split[0])&&!judgeEquals(ub,split[2])){
                                //删除左边的
                                String newConstrain = split[1] + "<=" + split[2];
                                stringBuilder.append(newConstrain).append("\n");
                            }else if(!judgeEquals(lb,split[0])&&judgeEquals(ub,split[2])){
                                //删除右边的
                                String newConstrain = split[0] + "<=" + split[1];
                                stringBuilder.append(newConstrain).append("\n");
                            }
                        }else{
                            if(!((regexDouble.matcher(split[0]).matches()||regexInt.matcher(split[0]).matches())&&judgeEquals(lb,split[0]))
                                    || !((regexDouble.matcher(split[1]).matches()||regexInt.matcher(split[1]).matches())&&judgeEquals(ub,split[1]))){
                                stringBuilder.append(constrain).append("\n");
                            }
                        }
                    }
                    deleteConstrain = iisConstraint;
                } else {
                    stringBuilder.append(constrain).append("\n");
                }
            }
            optimizeResults[i] = new OptimizeResult("Reduce Constrain.", "Delete this constrain: " + deleteConstrain, stringBuilder.toString());
        }
        return optimizeResults;
    }

    private static boolean judgeEquals(double bound, String val) {

        Double value = Double.parseDouble(val);
        return bound == value;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n6<=x-y\ny-z<=3";
        String iisConstraints[] = {"x-y<=5.0", "0<=x-y"};
        OptimizeResult[] optimizeResults = ReduceConstraints.reduceConstraints(constraints, iisConstraints);
        for (OptimizeResult optimizeResult : optimizeResults) {
            System.out.println(optimizeResult.getNewConstraints());
            System.out.println(optimizeResult);
        }
    }
}
