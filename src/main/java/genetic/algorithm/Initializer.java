package genetic.algorithm;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import model.Person;
import withcplex.LpModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * TODO 负责初始化种群，重点涉及
 * 基本思路是遍历所有约束，给每个变量赋值
 * 具体怎么做，还需要斟酌
 * 方案一：
 * 1. 将所有的变量存在hashmap中，如果该变量不在hashmap中，说明未被赋值，反之说明已被赋值
 * 2. 从第一个不等式开始遍历，根据不等式的特性赋值,让减数赋值为0，被减数赋值为所能赋值的最大值（原因是防止出现3.2的情况）
 * 3. 第二个不等式开始，对每个不等式首先检查不等式的变量前面有没有修改过值，根据修改的情况确定每个变量的值：
 * 3.1 如果都没修改过，则根据不等式的方式直接赋值；让减数赋值为0，被减数取能取得的最大值
 * 3.2 如果修改过一个，则这个不变，另一个未修改过的（还是初始值-1）根据不等式赋值。但是这里要注意，可能会出现下面的问题（不过经过上面的操作，已经不会出现下面的问题了）：
 *
 * 3.3 如果两个都修改过，则先验证是否满足当前不等式，如果不满足，修改这两个数字的值使之满足不等式，同时往前回溯，找到涉及到这两个变量的不等式，修改对应的值，依次执行下去，直到找不到为止（已经改过的不等式不算）
 * 4. 对上述所有变量随机加上一个常数（>0），则可以得到其他好多个点。
 *
 * 方案二：
 * 使用cplex直接获得一组可行的初始解，这个方法切实有效。不用费脑子
 */
public class Initializer {

    public ArrayList<Person> generateInitialPopulation(String constraints,int populations) throws IloException {
        Person person = new Person();
        IloNumVar[][] var = new IloNumVar[1][];
        IloCplex model = new IloCplex();
        LpModel lpModel = new LpModel();
        lpModel.createModel(constraints, model, var, null,false);
        if(model.solve()){
            double[] x = model.getValues(var[0]);
            for(int i=0;i<var[0].length;i++){
                String name = var[0][i].getName();
                double value = x[i];
                person.add(name,value);
            }
        }



        ArrayList<Person> firstGeneration = new ArrayList<Person>();
        firstGeneration.add(person);
        for(int i=1;i<populations;i++){
            Person newPerson = new Person();
            for(int j=0;j<person.length();j++){
                newPerson.add(person.getVariable(j),person.getValue(j)+i);
            }
            firstGeneration.add(newPerson);
        }
        return firstGeneration;
    }

    public HashMap<String, Double> generateInitialPopulation(String constraints) {
        String[] constraintsArray = constraints.split("\n");
        HashMap<String, Double> keyValue = new HashMap<String, Double>();
        Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");//小数
        Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");//整数
        //遍历每一个不等式
        for (int i = 0; i < constraintsArray.length; i++) {
            //对当前不等式进行分解
            String currentConstrain = constraintsArray[i];
            String[] consSpli = currentConstrain.split("<=");
            String lb = "0.0";// left bound/ lowerbound
            String ub = "0.0";// right bound/ up bound
            String lv = "";// left variable
            String rv = "";//right variable
            if (consSpli.length == 3) {
                lb = consSpli[0];
                ub = consSpli[2];
                String[] variables = consSpli[1].split("-");
                lv = variables[0];
                rv = variables[1];
            } else if (consSpli.length == 2 &&
                    (regexDouble.matcher(consSpli[0]).matches() || regexInt.matcher(consSpli[0]).matches())) {
                //形如 5<= x-y
                lb=consSpli[0];
                String[] variables = consSpli[1].split("-");
                lv = variables[0];
                rv = variables[1];
            }else if(consSpli.length==2&&
                    (regexDouble.matcher(consSpli[1]).matches() || regexInt.matcher(consSpli[1]).matches())){
                //形如 x-y<=5
                ub=consSpli[1];
                String[] variables = consSpli[0].split("-");
                lv = variables[0];
                rv = variables[1];
            }
            if(keyValue.containsKey(lv)&&keyValue.containsKey(rv)){

            }else if(keyValue.containsKey(lv)&&!keyValue.containsKey(rv)){
                //如果keyValue包含lv而不包含rv，说明被减数值已存在，

            }else if(keyValue.containsKey(rv)&&!keyValue.containsKey(lv)){

            }else{
                //如果两个都不包含，那么让rv赋值为0，lv取值为最大值ub，避免后面出现问题
                keyValue.put(rv,0.0);
                double leftBound = Double.parseDouble(lb);
                double rightBound = Double.parseDouble(ub);
                keyValue.put(lv,Math.random()*(rightBound-leftBound)+leftBound);
            }
        }
        return keyValue;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n3<=x-y\ny-z<=3";
        try {
            ArrayList<Person> population = new Initializer().generateInitialPopulation(constraints,4);
            System.out.println(population);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

}
