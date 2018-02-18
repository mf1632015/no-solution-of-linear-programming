package model;

public class OptimizeMethod {

    String method;//修改的方法，目前是修改值和修改约束两种
    String valueChange;//具体修改的那个约束
    String newConstraints;//修改后新的约束
    double value;//修改后最终的优化结果

    public OptimizeMethod(String method, String valueChange) {
        this.method = method;
        this.valueChange = valueChange;
    }

    public OptimizeMethod(String method, String valueChange, String newConstraints) {
        this.method = method;
        this.valueChange = valueChange;
        this.newConstraints = newConstraints;
    }

    public OptimizeMethod(String method, String valueChange, double value) {
        this.method = method;
        this.valueChange = valueChange;
        this.value = value;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getValueChange() {
        return valueChange;
    }

    public void setValueChange(String valueChange) {
        this.valueChange = valueChange;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getNewConstraints() {
        return newConstraints;
    }

    public void setNewConstraints(String newConstraints) {
        this.newConstraints = newConstraints;
    }

    @Override
    public String toString() {
        return method +" "+ valueChange +" "+" value is " +value;

    }
}
