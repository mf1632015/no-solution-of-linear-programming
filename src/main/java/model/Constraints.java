package model;

public class Constraints {
    //左值
    private double[] lb;
    //右值
    private double[] rb;

    //约束参数
    private String[][] parameters;

    //约束系数
    private double[][] coefficients;

    public Constraints(double[] lb, double[] rb, String[][] parameters, double[][] coefficients) {
        this.lb = lb;
        this.rb = rb;
        this.parameters = parameters;
        this.coefficients = coefficients;
    }

    public double[] getLb() {
        return lb;
    }

    public double[] getRb() {
        return rb;
    }

    public String[][] getParameters() {
        return parameters;
    }

    public double[][] getCoefficients() {
        return coefficients;
    }
}
