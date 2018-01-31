package to.standard.form;

public class StandardForm {
    //决策系数
    double[] c;
    //资源限制（不等式或者等式右边的常数，由于是标准形，因此这里是等式，且均大于等于0）
    double[] b;
    //约束条件系数构成的矩阵
    double[][] A;

    public StandardForm() {
    }

    public StandardForm(double[] c, double[] b, double[][] a) {
        this.c = c;
        this.b = b;
        A = a;
    }

    public double[] getC() {
        return c;
    }

    public double[] getB() {
        return b;
    }

    public double[][] getA() {
        return A;
    }
}
