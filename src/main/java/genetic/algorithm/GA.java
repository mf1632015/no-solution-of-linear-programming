package genetic.algorithm;

import ilog.concert.IloException;
import model.OptimizeResult;
import model.Person;

import java.util.ArrayList;

/**
 * 统筹，总的算法
 */
public class GA {
    private String constraints;//约束
    private String target;//目标
    private int personNumber;//人口数量
    private double mutationRate;//变异概率
    private int generationNumber;//繁殖总代数
    private boolean isMax; //求目标的最大值还是最小值

    public GA(String constraints, String target, int personNumber, double mutationRate, int generationNumber, boolean isMax) {
        this.constraints = constraints;
        this.target = target;
        this.personNumber = personNumber;
        this.mutationRate = mutationRate;
        this.generationNumber = generationNumber;
        this.isMax = isMax;
    }

    public OptimizeResult geneticAlgorithm() throws IloException {

        //1. 选择初始可行解
        Initializer initializer = new Initializer();
        ArrayList<Person> populations = initializer.generateInitialPopulation(constraints,personNumber);

        //2.根据初始可行解，繁衍后代，变异，遗传，选择
        //当人口满足某种情况，或者达到某一代时，停止
       do{
            //2.1 杂交,通过选择算子来选择父母，产生子代

            //2.2 对子代进行变异

            //2.3 选择,根据isMax选择最大的种群或者最小的种群。更新populations

            generationNumber--;
        } while(!isStopped(populations)&&generationNumber<=0);
        return null;
    }

    private boolean isStopped(ArrayList<Person> population){

        return false;
    }
}
