package model;

import java.util.ArrayList;

/**
 * 用来存储遗传算法的每个个体的染色体
 * variables和value一一对应，用hashmap不能保证顺序一致，因此使用这个类来表示
 */
public class Person {
    private ArrayList<String> variables;
    private ArrayList<Double> values;

    public Person() {
        variables = new ArrayList<String>();
        values = new ArrayList<Double>();
    }

    public String getVariable(int i) {
        return variables.get(i);
    }

    public double getValue(int i) {
        return values.get(i);
    }

    public int length() {
        return variables.size();
    }

    public void add(String variable, double value) {
        variables.add(variable);
        values.add(value);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ ");
        for (int i = 0; i < this.length(); i++) {
            if (i < this.length() - 1)
                stringBuilder.append(this.getVariable(i) + ":" + this.getValue(i) + " ,");
            else
                stringBuilder.append(this.getVariable(i) + ":" + this.getValue(i));
        }
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }
}
