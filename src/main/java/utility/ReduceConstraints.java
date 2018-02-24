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
    public static OptimizeResult[] reduceConstraints(String constraints, IloConstraint[] iis){
        OptimizeResult[] optimizeResults = new OptimizeResult[iis.length];
        String[] originalConstraints = constraints.split("\n");
        String[] iisAfterResolve = IISConstraintResolve.resolveIIS(iis);
        System.out.println(Arrays.toString(iisAfterResolve));

        for(int i=0;i<iis.length;i++){
//            String constrainToReduce =
            String iisConstraint = iisAfterResolve[i];
            String[] iisAfterSplit = iisConstraint.split("<=");
            Pattern regex = Pattern.compile("^[-\\+]?[.\\d]*$");

            double lb=-1;//low bound
            double ub=-1;//up bound
            // lb<=leftparam -rightParam<=up
            String rightParam;
            String leftParam;
            if(iisAfterSplit.length==3){
                //找到下边界和上边界
                lb = Double.parseDouble(iisAfterSplit[0]);
                ub = Double.parseDouble(iisAfterSplit[2]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[1].split("-");
                leftParam = params[0];
                rightParam ="-"+ params[1];
            }
            else if(regex.matcher(iisAfterSplit[0]).matches()){
                //找到下边界
                lb = Double.parseDouble(iisAfterSplit[0]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[1].split("-");
                leftParam = params[0];
                rightParam ="-"+  params[1];

            }else{
                //找到上边界
                ub = Double.parseDouble(iisAfterSplit[1]);
                //找到减数和被减数两个变量
                String[] params = iisAfterSplit[1].split("-");
                leftParam = params[0];
                rightParam ="-"+  params[1];
            }
            //删掉当前这个约束（对应前面求解的东东）
            for(String constrain:originalConstraints){

            }

        }
        return null;
    }

    public static void main(String[] args) {

    }
}
