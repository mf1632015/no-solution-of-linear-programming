package abandoned.form;

import java.util.ArrayList;

/**
 * 将下列式子经过转化，化为标准形。
 * 向量c，向量x  maxZ = cx
 * 矩阵A 向量b    Ax=b
 * 所有的x均大于等于0
 * IloRange  : -infinity <= (-1.0*b1 + 1.0*c2) <= 4.0
 IloRange  : 5.0 <= (-1.0*b1 + 1.0*d2) <= infinity
 IloRange  : 12.0 <= (-1.0*d3 + 1.0*d4) <= infinity
 IloRange  : 12.0 <= (1.0*f2 - 1.0*f1) <= infinity
 IloRange  : 12.0 <= (-1.0*j5 + 1.0*j6) <= infinity
 IloRange  : 12.0 <= (1.0*j8 - 1.0*j7) <= infinity
 IloRange  : 0.0 <= (-1.0*d2 + 1.0*d3) <= infinity
 IloRange  : 0.0 <= (-1.0*d4 + 1.0*f1) <= infinity
 IloRange  : 0.0 <= (-1.0*f2 + 1.0*j2) <= infinity
 IloRange  : 0.0 <= (-1.0*j2 + 1.0*j3) <= infinity
 IloRange  : 0.0 <= (1.0*j5 - 1.0*j3) <= infinity
 IloRange  : 0.0 <= (-1.0*j6 + 1.0*j7) <= infinity
 IloRange  : 0.0 <= (-1.0*j8 + 1.0*j9) <= infinity
 IloRange  : 0.0 <= (-1.0*j9 + 1.0*j10) <= infinity
 IloRange  : 0.0 <= (-1.0*j10 + 1.0*j12) <= infinity
 IloRange  : 0.0 <= (-1.0*j12 + 1.0*m4) <= infinity
 IloRange  : 0.0 <= (-1.0*m4 + 1.0*b5) <= infinity
 IloRange  : 0.0 <= (-1.0*b5 + 1.0*c1) <= infinity
 IloRange  : 0.0 <= (1.0*c2 - 1.0*c1) <= infinity
 */
public class IISToStandard {


    public StandardForm toStandardForm(String iis){
        String[] inequalities = iis.split("\n");
        String[] simplifiedInequalities = simplifyInequalities(inequalities);
        double[] lb= new double[inequalities.length];
        double[] rb = new double[inequalities.length];
        int i=0;
        int index=0;
        for(String str:inequalities){
            String inequality = str.split(":")[1];

            i++;
        }

        return new StandardForm();
    }

    //通过不等式的传递性，将不等式简化到最简形式。
    //比如注释中的例子，可以简化为：
    private String[] simplifyInequalities(String[] inequalities){
        //存储化简后的不等式
        ArrayList<String> afterSimplifiedInequalities = new ArrayList<String>();

        //
        int length = inequalities.length;
        ArrayList<String> leftVarityies = new ArrayList<String>();
        ArrayList<String> rightVarities = new ArrayList<String>();
        double[] lb = new double[length];
        double[] rb = new double[length];
        double[] leftCoefficient = new double[length];
        double[] rightCoefficient = new double[length];
        for(String inequality : inequalities ){

        }
        return new String[10];
    }

    public static void main(String[] args) {

    }
}
