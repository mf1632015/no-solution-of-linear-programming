package unsolved2solved;

import ilog.concert.IloConstraint;

import java.util.ArrayList;
import java.util.Arrays;

public class IISConstraintResolve {

    public static String[] resolveIIS(IloConstraint[] iis) {
        String iisConstraints = "";
        for(IloConstraint iloConstraint:iis){
            iisConstraints+=iloConstraint.toString()+"\n";
        }
        return resolveIIS(iisConstraints.split("\n"));
    }

    private static String[] resolveIIS(String[] iis){
        ArrayList<String> iisList = new ArrayList<String>();
        for(String str:iis){
            if(iis.equals("")){
                continue;
            }
            String constrain = str.split(":")[1].trim();
            constrain = constrain.replace(") <= infinity","");
            constrain = constrain.replace("-infinity <= (","");
            constrain = constrain.replace(" <= (","<=");
            constrain = constrain.replace(") <= ","<=");
            constrain = constrain.replace("1.0*","");
            constrain = constrain.replace(".0*","");
            constrain = constrain.replace(" ","");

            if(constrain.contains("+")){
                String newConstrain="";
                String[] splitConstrain = constrain.split("<=");
                for(int i=0;i<splitConstrain.length;i++){
                    if(i==0&&splitConstrain[i].contains("+")){
                        String[] params = splitConstrain[i].split("\\+");
                        newConstrain+=params[1]+params[0]+"<=";
                    }else if(i==1&&splitConstrain[i].contains("+")){
                        String[] params = splitConstrain[i].split("\\+");
                        newConstrain+="<="+params[1]+params[0];
                    }else if(i==2){
                        newConstrain+="<="+splitConstrain[i];
                    }else{
                        newConstrain+=splitConstrain[i];
                    }
                }
                constrain=newConstrain;
            }
            iisList.add(constrain);
        }
        String[] result = new String[iisList.size()];
        for(int i=0;i<iisList.size();i++){
            result[i]=iisList.get(i);
        }
        return result;
    }

    public static void main(String[] args) {
        String iis="IloRange  : 0.0 <= (1.0*x - 1.0*y) <= infinity\n" +
                "IloRange  : 6.0 <= (-1.0*x + 1.0*y) <= infinity\n"+
                "IloRange : -infinity <= (-1.0*x + 1.0*y) <= 8.0";
        System.out.println(Arrays.toString(IISConstraintResolve.resolveIIS(iis.split("\n"))));
    }
}
