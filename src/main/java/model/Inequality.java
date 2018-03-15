package model;

import java.util.ArrayList;

/**
 * 包含6个变量，小于等于的值，减数，被减数，大于等于的值，上一个不等式list（根据被减数），下一个不等式list（根据减数）
 */
public class Inequality {
    private double le;
    private double ge;
    private String subtractor;
    private String minuend;
    private ArrayList<Inequality> formerList ;
    private ArrayList<Inequality> nextList  ;

    public Inequality(double le, double ge, String subtractor, String minuend) {
        this.le = le;
        this.ge = ge;
        this.subtractor = subtractor;
        this.minuend = minuend;
        formerList  = new ArrayList<Inequality>();
        nextList  = new ArrayList<Inequality>();
    }

    public void addFormer(ArrayList<Inequality> formerList){
        if(formerList!=null){
            this.formerList.addAll(formerList);
        }

    }

    public void addNext(ArrayList<Inequality> nextList){
        if(nextList!=null){
            this.nextList.addAll(nextList);

        }
    }

    public void addFormer(Inequality inequality){
        formerList.add(inequality);
    }

    public void addNext(Inequality inequality){
        nextList.add(inequality);
    }

    public double getLe() {
        return le;
    }

    public void setLe(double le) {
        this.le = le;
    }

    public double getGe() {
        return ge;
    }

    public void setGe(double ge) {
        this.ge = ge;
    }

    public String getSubtractor() {
        return subtractor;
    }

    public void setSubtractor(String subtractor) {
        this.subtractor = subtractor;
    }

    public String getMinuend() {
        return minuend;
    }

    public void setMinuend(String minuend) {
        this.minuend = minuend;
    }

    public ArrayList<Inequality> getFormerList() {
        return formerList;
    }

    public void setFormerList(ArrayList<Inequality> formerList) {
        this.formerList = formerList;
    }

    public ArrayList<Inequality> getNextList() {
        return nextList;
    }

    public void setNextList(ArrayList<Inequality> nextList) {
        this.nextList = nextList;
    }

    public boolean isRoot(){
        return formerList.isEmpty();
    }

    public boolean isLeap(){
        return nextList.isEmpty();
    }

    public boolean hasNext(){
        return !nextList.isEmpty();
    }

    public String toString(){
        return ge+"<="+subtractor+"-"+minuend+"<="+le;
    }
}
