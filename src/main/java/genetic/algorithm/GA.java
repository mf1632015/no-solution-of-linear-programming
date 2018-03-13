package genetic.algorithm;

import ilog.concert.IloException;
import model.OptimizeResult;
import model.Person;

import java.util.ArrayList;

/**
 * 统筹，总的算法
 *
 * 1. 首先生成初代人口
 * 2. 评价初代人口，用作以后选择算子
 * 3. 进行一个循环，直到达到指定的繁衍代数或者达到某个适应度目标
 *      3.1 当前代数+1
 *      3.2 选择上一代人口，进行杂交（crossover)，选择最优个体，同时保持多样化
 *      3.3 选择当前人口，进行变异。防止局部收敛
 *      3.4 评价当前人口
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

        //3.根据初始可行解，繁衍后代，变异，遗传，选择
        //当人口满足某种情况，或者达到某一代时，停止
       do{
            //3.1 杂交,通过选择算子来选择父母，产生子代

            //3.2 对子代进行变异

            //3.3 选择,根据isMax选择最大的种群或者最小的种群。更新populations以及每个个体的适应度（已经有的不需要计算了）

            generationNumber--;
        } while(!isStopped(populations)&&generationNumber<=0);
        return null;
    }

    private boolean isStopped(ArrayList<Person> population){

        return false;
    }
}
