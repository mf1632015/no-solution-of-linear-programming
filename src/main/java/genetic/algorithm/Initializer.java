package genetic.algorithm;

import java.util.HashMap;

/**
 * TODO 负责初始化种群，重点涉及
 * 基本思路是遍历所有约束，给每个变量赋值
 * 具体怎么做，还需要斟酌
 * 方案一：
 * 1. 将所有的变量存在hashmap中，String double 格式，全部初始化double为-1；
 * 2. 从第一个不等式开始遍历，根据不等式的特性赋值
 * 3. 第二个不等式开始，对每个不等式首先检查不等式的变量前面有没有修改过值，根据修改的情况确定每个变量的值：
 *    3.1 如果都没修改过，则根据不等式的方式直接赋值；
 *    3.2 如果修改过一个，则这个不变，另一个未修改过的（还是初始值-1）根据不等式赋值；
 *    3.3 如果两个都修改过，则先验证是否满足当前不等式，如果不满足，修改这两个数字的值使之满足不等式，同时往前回溯，找到涉及到这两个变量的不等式，修改对应的值，依次执行下去，直到找不到为止（已经改过的不等式不算）
 * 4. 对上述所有变量随机加上一个常数（>0），则可以得到其他好多个点。
 */
public class Initializer {
    public void generteInitialPopulation(String constraints){
        String[] constraintsArray = constraints.split("\n");
        HashMap<String,Double> keyValue = new HashMap<String, Double>();
        for(int i=0;i<constraintsArray.length;i++){
            String currentConstrain = constraintsArray[i];
            String[] consSpli = currentConstrain.split("<=");
        }
    }


}
