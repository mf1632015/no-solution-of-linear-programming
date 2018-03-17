package model;

public class TreePathPair {
    private TreePath treePath1;
    private TreePath treePath2;
    // treePath1从index1开始，treePath2从index2开始，二者首字母相同
    //二者的末尾字母一定是相同的。
    private int index1;
    private int index2;

    //表示谁是LE为非负数的标志,1表示treePath1的le为非负，2表示treePath2的le非负
    private int leTag;
    private double leTotal;
    private double geTotal;

    public TreePathPair(TreePath treePath1, TreePath treePath2, int index1, int index2) {
        this.treePath1 = treePath1;
        this.treePath2 = treePath2;
        this.index1 = index1;
        this.index2 = index2;
    }

    public int getLeTag() {
        return leTag;
    }

    public void setLeTag(int leTag) {
        this.leTag = leTag;
    }

    public double getLeTotal() {
        return leTotal;
    }

    public void setLeTotal(double leTotal) {
        this.leTotal = leTotal;
    }

    public double getGeTotal() {
        return geTotal;
    }

    public void setGeTotal(double geTotal) {
        this.geTotal = geTotal;
    }

    public TreePath getTreePath1() {
        return treePath1;
    }

    public void setTreePath1(TreePath treePath1) {
        this.treePath1 = treePath1;
    }

    public TreePath getTreePath2() {
        return treePath2;
    }

    public void setTreePath2(TreePath treePath2) {
        this.treePath2 = treePath2;
    }

    public int getIndex1() {
        return index1;
    }

    public void setIndex1(int index1) {
        this.index1 = index1;
    }

    public int getIndex2() {
        return index2;
    }

    public void setIndex2(int index2) {
        this.index2 = index2;
    }
}
