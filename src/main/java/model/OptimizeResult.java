package model;

import java.util.ArrayList;

public class OptimizeResult {

    private String method;//修改的方法，目前是修改值和修改约束两种
    private String valueChange;//具体修改的那个约束
    private String newConstraints;//修改后新的约束
    private double value;//修改后最终的优化结果
    private ArrayList<String> param_value = new ArrayList<String>(); //最终优化结果的每个变量以及对应的值

    public OptimizeResult(String method, String valueChange) {
        this.method = method;
        this.valueChange = valueChange;
    }

    public OptimizeResult(String method, String valueChange, String newConstraints) {
        this.method = method;
        this.valueChange = valueChange;
        this.newConstraints = newConstraints;
    }

    public OptimizeResult(String method, String valueChange, double value) {
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

    public void addParamValue(String str){
        param_value.add(str);
    }

    public void cleanParamValue(){
        param_value.clear();
    }

    public ArrayList<String> getParam_value() {
        return param_value;
    }

    @Override
    public String toString() {
        return method +" "+ valueChange +" ";
//        +" value is " +value;

    }
}
