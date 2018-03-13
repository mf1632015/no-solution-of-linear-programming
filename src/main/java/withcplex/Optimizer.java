package withcplex;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import model.OptimizeResult;
import unsolved2solved.ChangeConstant;
import unsolved2solved.ReduceConstraints;

/**
 *  结合cplex优化无解的线性规划问题。
 */
public class Optimizer {

    /**
     *
     * @param constraints 约束，不同约束换行符分割
     * @param method 要使用的优化方法
     * @param target 线性规划的目标函数，形如1x+5b-3c(第一个参数的正负号可以没有；必须有系数，系数为1就写1，不能默认为空）
     * @param isMax 目标函数取最大值还是最小值
     * @return 返回优化结果
     */
    public OptimizeResult optimize(String constraints, int method, String target, boolean isMax) {
        if(constraints==null||method<1||method>3||target==null){
            return null;
        }
        switch (method) {
            case 1: return optimizedThroughReduceCons(constraints,target,isMax);
            case 2: return optimizedThroughChangeValue(constraints, target, isMax);
            case 3:
                OptimizeResult optimizeResult1 = optimizedThroughChangeValue(constraints,target,isMax);
                OptimizeResult optimizeResult2 = optimizedThroughReduceCons(constraints,target,isMax);
                if(isMax){
                    if(optimizeResult1.getValue()>optimizeResult2.getValue()){
                        return optimizeResult1;
                    }else{
                        return optimizeResult2;
                    }
                }else{
                    if(optimizeResult1.getValue()>optimizeResult2.getValue()){
                        return optimizeResult2;
                    }else{
                        return optimizeResult1;
                    }
                }
        }
        return null;
    }

    private OptimizeResult optimizedThroughReduceCons(String constraints,String target,boolean isMax){
        OptimizeResult targetOptimizeResult = new OptimizeResult("Constraints can be solved,no need to be optimized", "", Double.MAX_VALUE);
        if (isMax) {
            targetOptimizeResult.setValue(-Double.MAX_VALUE);
        }
        try {
            IloNumVar[][] var = new IloNumVar[1][];
            IloCplex model = new IloCplex();
            LpModel lpModel = new LpModel();
            lpModel.createModel(constraints, model, var, target,isMax);
            if (model.solve()) {// 如果直接可以解，暂时不需要优化
                //ODO 通过放入目标获得最优值
                OptimizeResult optimizeResult = new OptimizeResult("Constraints can be solved,no need to be optimized", "", model.getObjValue());
                //获得取得最优解时每个变量的值
                double[] x = model.getValues(var[0]);
                for(int i=0;i<var[0].length;i++){
                    String name = var[0][i].getName();
                    double value = x[i];
                    optimizeResult.addParamValue(name+":"+value);
                }
                model.end();
                return optimizeResult;
            } else {
                //获得最小不可解约束
                IloConstraint[] iis = new LpModel().getIIS(model);
                for (IloConstraint constraint : iis) {
                    System.out.println(constraint);
                }
                // 通过减少不可解约束条件来优化目标,每次减少一个即可。
                OptimizeResult[] optimizeMethodsArray = ReduceConstraints.reduceConstraints(constraints, iis);
                assert optimizeMethodsArray != null;
                for (OptimizeResult optimizeResult : optimizeMethodsArray) {
                    targetOptimizeResult = obtainOptimizerResult(optimizeResult,isMax, targetOptimizeResult,target);
                }

                //如果上述操作没有改变targetOptimizeMethod的valuechange值，说明优化没有成功.修改下两个值
                if(targetOptimizeResult.getValueChange().equals("")){
                    targetOptimizeResult.setMethod("Optimizer failure");
                    targetOptimizeResult.setValue(-1);
                }

                return targetOptimizeResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private OptimizeResult optimizedThroughChangeValue(String constraints,String target,boolean isMax){
        OptimizeResult targetOptimizeResult = new OptimizeResult("Constraints can be solved,no need to be optimized", "", Double.MAX_VALUE);
        if (isMax) {
            targetOptimizeResult.setValue(-Double.MAX_VALUE);
        }
        try {
            IloCplex model = getCplexModel(constraints,target,isMax);
            if (model.solve()) {// 如果直接可以解，暂时不需要优化
                //ODO 通过放入目标获得最优值
                OptimizeResult optimizeResult = new OptimizeResult("Constraints can be solved,no need to be optimized", "", model.getObjValue());
                model.end();
                return optimizeResult;
            } else {
                //获得最小不可解约束
                IloConstraint[] iis = new LpModel().getIIS(model);
                for (IloConstraint constraint : iis) {
                    System.out.println(constraint);
                }
                OptimizeResult optimizeResult = ChangeConstant.changeValue(constraints, iis);
                assert optimizeResult != null;
                targetOptimizeResult = obtainOptimizerResult(optimizeResult,isMax, targetOptimizeResult,target);

                //如果上述操作没有改变targetOptimizeMethod的valuechange值，说明优化没有成功.修改下两个值
                if(targetOptimizeResult.getValueChange().equals("")){
                    targetOptimizeResult.setMethod("Optimizer failure");
                    targetOptimizeResult.setValue(-1);
                }
                return targetOptimizeResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private OptimizeResult obtainOptimizerResult(OptimizeResult optimizeResult, boolean isMax, OptimizeResult targetOptimizeResult, String target) throws IloException {
        IloNumVar[][] var = new IloNumVar[1][];
        IloCplex model = new IloCplex();
        LpModel lpModel = new LpModel();
        lpModel.createModel(optimizeResult.getNewConstraints(), model, var, target,isMax);
        if(!model.solve()){
            return targetOptimizeResult;
        }
        optimizeResult.setValue(model.getObjValue());
        double[] x = model.getValues(var[0]);
        for(int i=0;i<var[0].length;i++){
            String name = var[0][i].getName();
            double value = x[i];
            optimizeResult.addParamValue(name+":"+value);
        }
        model.end();
        if ((isMax && optimizeResult.getValue() > targetOptimizeResult.getValue()) || ((!isMax) && optimizeResult.getValue() < targetOptimizeResult.getValue())) {
           return optimizeResult;
        }
        return targetOptimizeResult;
    }

    private IloCplex getCplexModel(String constraints,String target,boolean isMax) throws IloException {
        IloNumVar[][] var = new IloNumVar[1][];
        IloCplex model = new IloCplex();
        LpModel lpModel = new LpModel();
        lpModel.createModel(constraints, model, var, target,isMax);
        return model;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n6<=x-y\ny-z<=3";
        Optimizer optimizer = new Optimizer();
        OptimizeResult optimizeResult = optimizer.optimize(constraints, 2, "1x+1y", false);
        System.out.println(optimizeResult);
        System.out.println(optimizeResult.getParam_value());
        System.out.println(" 优化后的结果为："+optimizeResult.getValue());
    }

}
