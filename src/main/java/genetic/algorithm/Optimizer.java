package genetic.algorithm;

public class Optimizer {

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n3<=x-y\ny-z<=3";
        GA ga = new GA(constraints,"1x+1y",100,0.5,1000,false);

    }


}
