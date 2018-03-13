package model;

import java.util.ArrayList;

/**
 * 用来存储遗传算法的每个个体的染色体
 * variables和value一一对应，用hashmap不能保证顺序一致，因此使用这个类来表示
 * generation 用来表示代数，从第1代开始
 * fitness 适应度，用来表示当前种群所在种群的适应度。一般为代入目标函数所求的的值
 */
public class Person {
    private ArrayList<String> variables;
    private ArrayList<Double> values;
    private int generation;
    private double fitness;

    public Person() {
        variables = new ArrayList<String>();
        values = new ArrayList<Double>();
        fitness=-1;
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

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
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
