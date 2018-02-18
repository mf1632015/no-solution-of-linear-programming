package withcplex;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import model.OptimizeMethod;
import utility.ChangeConstant;
import utility.ReduceConstraints;

public class Optimizer {

    public OptimizeMethod optimize(String constraints, String method, String target, boolean isMax) {

        OptimizeMethod targetOptimizeMethod = new OptimizeMethod("Constraints can be solved,no need to be optimized", "", Double.MAX_VALUE);
        if (isMax) {
            targetOptimizeMethod.setValue(-Double.MAX_VALUE);
        }
        try {
            IloCplex model = getCplexModel(constraints);
            if (model.solve()) {// 如果直接可以解，暂时不需要优化
                OptimizeMethod optimizeMethod = new OptimizeMethod("Constraints can be solved,no need to be optimized", "", model.getObjValue());
                model.end();
                return optimizeMethod;
            } else {
                //获得最小不可解约束
                IloConstraint[] iis = new LpModel().getIIS(model);
                for (IloConstraint constraint : iis) {
                    System.out.println(constraint);
                }
                // 通过减少不可解约束条件来优化目标,每次减少一个即可。
                OptimizeMethod[] optimizeMethodsArray = ReduceConstraints.reduceConstraints(constraints, iis);
                assert optimizeMethodsArray != null;
                for (OptimizeMethod optimizeMethod : optimizeMethodsArray) {
                    targetOptimizeMethod = obtainOptimizerResult(optimizeMethod,isMax,targetOptimizeMethod);
                }

                // 通过优化约束变量来优化目标，变量应该取满足所求结果的最小值
                OptimizeMethod optimizeMethod = ChangeConstant.changeValue(constraints, iis);
                assert optimizeMethod != null;
                targetOptimizeMethod = obtainOptimizerResult(optimizeMethod,isMax,targetOptimizeMethod);

                //如果上述操作没有改变targetOptimizeMethod的valuechange值，说明优化没有成功.修改下两个值
                if(targetOptimizeMethod.getValueChange().equals("")){
                    targetOptimizeMethod.setMethod("Optimizer failure");
                    targetOptimizeMethod.setValue(-1);
                }
                return targetOptimizeMethod;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private OptimizeMethod obtainOptimizerResult(OptimizeMethod optimizeMethod,boolean isMax,OptimizeMethod targetOptimizeMethod) throws IloException {
        IloCplex newModel = getCplexModel(optimizeMethod.getNewConstraints());
        if(!newModel.solve()){
            return targetOptimizeMethod;
        }
        optimizeMethod.setValue(newModel.getObjValue());
        newModel.end();
        if ((isMax && optimizeMethod.getValue() > targetOptimizeMethod.getValue()) || ((!isMax) && optimizeMethod.getValue() < targetOptimizeMethod.getValue())) {
           return optimizeMethod;
        }
        return targetOptimizeMethod;
    }

    private IloCplex getCplexModel(String constraints) throws IloException {
        IloNumVar[][] var = new IloNumVar[1][];
        IloCplex model = new IloCplex();
        LpModel lpModel = new LpModel();
        lpModel.createModel(constraints, model, var, "");
        return model;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n3<=x-y";
        Optimizer optimizer = new Optimizer();
        OptimizeMethod optimizeMethod = optimizer.optimize(constraints, "1", "x+y", true);
        System.out.println(optimizeMethod);
    }

}
