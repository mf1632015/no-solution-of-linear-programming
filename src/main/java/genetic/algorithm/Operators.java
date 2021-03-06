package genetic.algorithm;

/**
 * TODO 负责操作种群的基因，
 * 杂交——遍布空间内所有点；
 *     杂交也有公式，因为不能胡乱杂交。不是简单的交换基因即可。为了防止杂交的点在凸集外面，需要如下操作：
 *     对于父亲<v1,v2,....vm>和<w1,w2....wm>，杂交的后代为
 *     <v1,v2,....vk',....vm'>和<w1,w2....wk',....wm'>
 *      其中，vk'=wk·a+(1-a)·vk   ;    wk' = vk·a+(1-a)·vk
 *      a的取值可以随机去（0-1），为了优化，可以按照论文上的公式来取。
 *
 * 变异——朝着空间边界移动
 *     变异有三种：
 *     1. 均匀变异，将某个基因变异，变异的范围是<low,high>。low和high可以根据不等式和其他值计算得出
 *     2. 边界变异，取low或者high，前面已经计算过了
 *     3. 非均匀变异，这里有一个公式。见论文（P8)或者笔记
 */
public class Operators {


    public void crossFertilize(){

    }

    public void mutationByUniform(){

    }
    public void mutationByBounded(){

    }
    public void mutationByUnUniform(){

    }

}
