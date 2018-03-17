package model;

import java.util.ArrayList;

public class TreePath {
    private ArrayList<Inequality> path;

    public TreePath() {
        this.path = new ArrayList<Inequality>();
    }

    public boolean containsVariable(String variable){
        boolean b=false;
        for(Inequality inequality:path){
            if(inequality.getSubtractor().equals(variable)||inequality.getMinuend().equals(variable)){
                b=true;
                break;
            }
        }
        return b;
    }

    public Inequality getFirst(){
        return path.get(0);
    }

    public Inequality getLast(){
        return path.get(path.size()-1);
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
