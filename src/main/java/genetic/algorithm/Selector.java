package genetic.algorithm;

import model.Person;

import java.util.ArrayList;

/**
 * 选择算子有很多。选择一个何时的算子，能够有效的避免收敛过早或者无法收敛
 * 1. 锦标赛选择算子：随机抽取2-n个，然后从中选取最优的1-m个，m<n
 * 2. 轮盘对赌选择算子：
 * 3. 。。。。
 * 这里实现锦标赛选择算子Tournament Selection
 */
public class Selector {

    public void generateFitness(ArrayList<Person> population){
        for(Person person:population){
            if(person.getFitness()==-1){
                continue;
            }
            //TODO 计算适应度
//            person.setFitness();
        }

    }

}
