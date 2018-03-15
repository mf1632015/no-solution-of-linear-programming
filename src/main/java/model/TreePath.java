package model;

import java.util.ArrayList;

public class TreePath {
    private ArrayList<Inequality> path;

    public TreePath() {
        this.path = new ArrayList<Inequality>();
    }

    public void addNode(Inequality inequality){
        path.add(inequality);
    }

    public ArrayList<Inequality> getPath() {
        return path;
    }

    public void setPath(ArrayList<Inequality> path) {
        this.path = path;
    }

    public int getLength(){
        return path.size();
    }

    public void removeLastNode(){
        path.remove(path.size()-1);
    }

    public void clear(){
        path.clear();
    }

    public TreePath clone(){
        TreePath treePath = new TreePath();
        treePath.setPath((ArrayList<Inequality>) this.path.clone());
        return treePath;
    }
}
