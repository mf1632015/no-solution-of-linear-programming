package withcplex;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import model.OptimizeResult;
import unsolved2solved.ChangeConstant;
import unsolved2solved.ReduceConstraints;

/**
 * 结合cplex优化无解的线性规划问题。
 */
public class Optimizer {


    /**
     *
     * @param constraints 可解约束
     * @param target 目标
     * @param isMax
     * @return
     * @throws IloException
     */
    public OptimizeResult getCplexModel(String constraints, String target, boolean isMax) throws IloException {
        OptimizeResult optimizeResult = new OptimizeResult();
        IloNumVar[][] var = new IloNumVar[1][];
        IloCplex model = new IloCplex();
        LpModel lpModel = new LpModel();
        lpModel.createModel(constraints, model, var, target, isMax);
        if (model.solve()) {
            optimizeResult.setValue(model.getObjValue());
            double[] x = model.getValues(var[0]);
            for (int i = 0; i < var[0].length; i++) {
                String name = var[0][i].getName();
                double value = x[i];
                optimizeResult.addParamValue(name + ":" + value);
            }
            model.end();
        }
        return optimizeResult;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n3<=x-y\ny-z<=3";
        Optimizer optimizer = new Optimizer();
        OptimizeResult optimizeResult = null;
        try {
            optimizeResult = optimizer.getCplexModel(constraints, "1x+1y-1z", true);
        } catch (IloException e) {
            System.out.println("lalal");
            e.printStackTrace();
        }
        System.out.println(optimizeResult.getParam_value());
        System.out.println(" 优化后的结果为：" + optimizeResult.getValue());
    }

}
