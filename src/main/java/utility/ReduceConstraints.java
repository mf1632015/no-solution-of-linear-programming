package utility;

import ilog.concert.IloConstraint;
import model.OptimizeMethod;

/**
 * 减少传入约束量的个数
 */
public class ReduceConstraints {

    //每次减少一个iis中的约束，从而得到一组Constraints
    //最终得到一个OptimizeMethod数组，每个均为减少了一个iis中一个不等式的约束。
    //TODO
    public static OptimizeMethod[] reduceConstraints(String Constraints, IloConstraint[] iis){

        return null;
    }
}
