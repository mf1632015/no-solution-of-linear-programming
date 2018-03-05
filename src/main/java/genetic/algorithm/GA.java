package genetic.algorithm;

import model.OptimizeResult;

import java.util.Random;

/**
 * 统筹，总的算法
 */
public class GA {

    public OptimizeResult geneticAlgorithm(){
        //1. 选择初始可行解（初始可行解的求解是个难题）

        //2.根据初始可行解，繁衍后代，变异，遗传，选择（这也是一个难题，如何确定后代也是可行的）
        double a= Math.random();
        while(a==3.0){

        }
        //3.直到目标值不再变化（一定精度内）,则停止遗传|当一定种群的值都在目标值附近时（一定精度内），则停止遗传。
        return null;
    }
}
