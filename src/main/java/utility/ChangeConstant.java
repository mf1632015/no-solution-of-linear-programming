package utility;

import ilog.concert.IloConstraint;
import model.OptimizeResult;

/**
 * 修改优化约束的常量（不等号右边）的值
 */
public class ChangeConstant{

//TODO
    public static OptimizeResult changeValue(String constraints, IloConstraint[] iis){
        String[] iisConstraints = new String[iis.length];
        int i=0;
        for(IloConstraint iloConstraint:iis){
            iisConstraints[i]=iis[i].toString();
            i++;
        }


        return null;
    }
}
