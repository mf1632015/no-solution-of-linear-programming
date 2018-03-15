package unsolved2solved;

import model.Inequality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 将下列式子经过转化，化为标准形。
 * 向量c，向量x  maxZ = cx
 * 矩阵A 向量b    Ax=b
 * 所有的x均大于等于0
 *
 IloRange  : -infinity <= (-1.0*b1 + 1.0*c2) <= 4.0
 IloRange  : 5.0 <= (-1.0*b1 + 1.0*d2) <= infinity
 IloRange  : 12.0 <= (-1.0*d3 + 1.0*d4) <= infinity
 IloRange  : 12.0 <= (1.0*f2 - 1.0*f1) <= infinity
 IloRange  : 12.0 <= (-1.0*j5 + 1.0*j6) <= infinity
 IloRange  : 12.0 <= (1.0*j8 - 1.0*j7) <= infinity
 IloRange  : 0.0 <= (-1.0*d2 + 1.0*d3) <= infinity
 IloRange  : 0.0 <= (-1.0*d4 + 1.0*f1) <= infinity
 IloRange  : 0.0 <= (-1.0*f2 + 1.0*j2) <= infinity
 IloRange  : 0.0 <= (-1.0*j2 + 1.0*j3) <= infinity
 IloRange  : 0.0 <= (1.0*j5 - 1.0*j3) <= infinity
 IloRange  : 0.0 <= (-1.0*j6 + 1.0*j7) <= infinity
 IloRange  : 0.0 <= (-1.0*j8 + 1.0*j9) <= infinity
 IloRange  : 0.0 <= (-1.0*j9 + 1.0*j10) <= infinity
 IloRange  : 0.0 <= (-1.0*j10 + 1.0*j12) <= infinity
 IloRange  : 0.0 <= (-1.0*j12 + 1.0*m4) <= infinity
 IloRange  : 0.0 <= (-1.0*m4 + 1.0*b5) <= infinity
 IloRange  : 0.0 <= (-1.0*b5 + 1.0*c1) <= infinity
 IloRange  : 0.0 <= (1.0*c2 - 1.0*c1) <= infinity
 */
public class InequationSimplify {

    /**
     *
     * @param inequalities 传入IIS不等式
     * @return
     * 通过不等式的传递性，将不等式简化到最简形式。
     * 然后计算出所差的值（小于等于和大于等于之间差的值），根据差值来分配
     * 比如注释中的例子，可以简化为：
     * 使用一个单独的结构存储，这个类就叫做Inequality，包含6个变量，小于等于的值，减数，被减数，大于等于的值，上一个不等式list（根据被减数），下一个不等式list（根据减数）
     * 然后使用一个hashmap保存减数对应的Inequality,一个hashmap保存被减数对应的Inequality。
     * 每处理一个不等式，都要为当前不等式找一下（从hashmap中）上一个不等式、下一个不等式，以及判断该不等式是不是已处理不等式（hashmap）中的上一个或者下一个不等式
     */
    public ArrayList<Inequality> simplifyInequalities(String[] inequalities){
        //存储所有的不等式
        ArrayList<Inequality> inequalityList = new ArrayList<Inequality>();
        //存储减数对应的不等式集
        HashMap<String, ArrayList<Inequality>> subtranHashMap = new HashMap<String, ArrayList<Inequality>>();
        //存储被减数对应的不等式集
        HashMap<String, ArrayList<Inequality>> minuHashMap = new HashMap<String, ArrayList<Inequality>>();

        Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");
        Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");

        for(String inequality : inequalities ){
            String[] splitInequality = inequality.split("<=");
            double le=-1;
            double ge=-1;
            String leftVariable="";
            String rightVariable="";
            if(splitInequality.length==3){
                le = Double.parseDouble(splitInequality[2]);
                ge = Double.parseDouble(splitInequality[0]);
                String[] variableSplit = splitInequality[1].split("-");
                leftVariable=variableSplit[0];
                rightVariable=variableSplit[1];
            }else if(splitInequality.length==2&&(regexDouble.matcher(splitInequality[0]).matches()||regexInt.matcher(splitInequality[0]).matches())){
                ge=Double.parseDouble(splitInequality[0]);
                String[] variableSplit = splitInequality[1].split("-");
                leftVariable=variableSplit[0];
                rightVariable=variableSplit[1];

            }else if(splitInequality.length==2&&(regexDouble.matcher(splitInequality[1]).matches()||regexInt.matcher(splitInequality[1]).matches())){
                le=Double.parseDouble(splitInequality[1]);
                String[] variableSplit = splitInequality[0].split("-");
                leftVariable=variableSplit[0];
                rightVariable=variableSplit[1];
            }
            Inequality inequality1 = new Inequality(le,ge,leftVariable,rightVariable);

            //为当前不等式找一下（从hashmap中）上一个不等式、下一个不等式
            ArrayList<Inequality> formerList = findFormerOrNextInequalities(minuHashMap,leftVariable,inequality1);
            ArrayList<Inequality> nextList = findFormerOrNextInequalities(subtranHashMap,rightVariable,inequality1);
            inequality1.addFormer(formerList);
            inequality1.addNext(nextList);

            //判断该不等式是不是已处理不等式（hashmap）中的上一个或者下一个不等式
            //如果是，增加该不等式到目标不等式的下一个或者上一个不等式
            resolveInqualities(inequalityList,inequality1);

            //修改hashmap，存储当前不等式
            changeHashMap(subtranHashMap,leftVariable,inequality1);
            changeHashMap(minuHashMap,rightVariable,inequality1);
            inequalityList.add(inequality1);
        }

        return inequalityList;
    }


    private void changeHashMap(HashMap<String, ArrayList<Inequality>> hashMap, String key,Inequality inequality){
        if(hashMap.get(key)==null){
            ArrayList<Inequality> inequalities = new ArrayList<Inequality>();
            inequalities.add(inequality);
            hashMap.put(key,inequalities);
        }else{
            ArrayList<Inequality> inequalities1 = hashMap.get(key);
            inequalities1.add(inequality);
            hashMap.put(key,inequalities1);
        }
    }

    private ArrayList<Inequality> findFormerOrNextInequalities(HashMap<String, ArrayList<Inequality>> hashMap, String key, Inequality inequality1){
        ArrayList<Inequality> inequalities = hashMap.get(key);
        if(inequalities==null){
            return null;
        }
        ArrayList<Inequality> formerOrNext = new ArrayList<Inequality>();
        double le1 = inequality1.getLe();
        double ge1 = inequality1.getGe();
        for(Inequality inequality:inequalities){
            double le2 = inequality.getLe();
            double ge2 = inequality.getGe();
            if(le1*le2>=0&&ge1*ge2>=0){
                formerOrNext.add(inequality);
            }
        }
        return formerOrNext;
    }

    /**
     * 判断该不等式是不是已处理不等式（hashmap）中的上一个或者下一个不等式,如果是，增加该不等式到目标不等式的下一个或者上一个不等式
     * @param inequalities 已处理不等式
     * @param inequality
     */
    private void resolveInqualities(ArrayList<Inequality> inequalities,Inequality inequality){
        double le = inequality.getLe();
        double ge = inequality.getGe();
        String leftVariable = inequality.getSubtractor();
        String rightVariable = inequality.getMinuend();
        for(Inequality inequalition:inequalities){
            String leftPartation  = inequalition.getSubtractor();
            String rightPartation = inequalition.getMinuend();
            double le1 = inequalition.getLe();
            double ge1 = inequalition.getGe();
            if(leftPartation.equals(rightVariable)&&le*le1>=0&&ge*ge1>=0){
                inequalition.addFormer(inequality);
            }
            if(rightPartation.equals(leftVariable)&&le*le1>=0&&ge*ge1>=0){
                inequalition.addNext(inequality);
            }
        }
    }


    public static void main(String[] args) {
        String inequalities = "IloRange  : -infinity <= (-1.0*b1 + 1.0*c2) <= 4.0\n" +
                " IloRange  : 5.0 <= (-1.0*b1 + 1.0*d2) <= infinity\n" +
                " IloRange  : 12.0 <= (-1.0*d3 + 1.0*d4) <= infinity\n" +
                " IloRange  : 12.0 <= (1.0*f2 - 1.0*f1) <= infinity\n" +
                " IloRange  : 12.0 <= (-1.0*j5 + 1.0*j6) <= infinity\n" +
                " IloRange  : 12.0 <= (1.0*j8 - 1.0*j7) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*d2 + 1.0*d3) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*d4 + 1.0*f1) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*f2 + 1.0*j2) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j2 + 1.0*j3) <= infinity\n" +
                " IloRange  : 0.0 <= (1.0*j5 - 1.0*j3) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j6 + 1.0*j7) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j8 + 1.0*j9) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j9 + 1.0*j10) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j10 + 1.0*j12) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*j12 + 1.0*m4) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*m4 + 1.0*b5) <= infinity\n" +
                " IloRange  : 0.0 <= (-1.0*b5 + 1.0*c1) <= infinity\n" +
                " IloRange  : 0.0 <= (1.0*c2 - 1.0*c1) <= infinity\n"+
                "IloRange  : 5.0 <= (1.0*a - 1.0*b) <= infinity\n"+
                "IloRange  : 7.0 <= (1.0*b - 1.0*c) <= infinity\n"+
                "IloRange  : 7.0 <= (1.0*b - 1.0*d) <= infinity\n"+
                "IloRange  : -infinity <= (1.0*a - 1.0*c) <= 6.0\n" +
                "IloRange  : -infinity <= (1.0*b - 1.0*d) <= 6.0";
        String[] inequalitiesAfterResolve = IISConstraintResolve.resolveIIS(inequalities.split("\n"));
        for(String str:inequalitiesAfterResolve){
            System.out.println(str);
        }

        ArrayList<Inequality> inequalityList = new InequationSimplify().simplifyInequalities(inequalitiesAfterResolve);
        for(Inequality inequality :inequalityList){
            System.out.println(inequality+" former:"+inequality.getFormerList()+" next:" + inequality.getNextList());
            System.out.println();
        }

    }


}
