package unsolved2solved;

import ilog.concert.IloConstraint;
import model.OptimizeResult;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 修改优化约束的常量（不等号右边）的值
 * 只需要先计算将IIS中的所有大于等于的值的和maxRange，然后把这个和变成iis中小于等于的右边的值即可
 * 比如5<=x-y<=10    12<=x-y<=15    修改为：5<=x-y<=17  12<=x-y<=17  即可。
 * 在具体实现中，为了防止特殊情况的发生，将maxRange+10作为小于等于的值；
 */
public class ChangeConstant {

    public static OptimizeResult changeValue(String Constraints, IloConstraint[] iis) {
        String[] iisColnstraints = IISConstraintResolve.resolveIIS(iis);
        return changeValue(Constraints, iisColnstraints);
    }

    //TODO 将所有的小于等于的约束值增大为所有其他约束大于等于的和，即可让其变得有解
    private static OptimizeResult changeValue(String constraints, String[] iisColnstraints) {
        OptimizeResult optimizeResult = new OptimizeResult("Change constant","");
        String[] originalConstraints = constraints.split("\n");
        double maxRange = 0;//用来存储小于等于的和
        Pattern regexDouble = Pattern.compile("^[-\\+]?[.\\d]*$");//小数
        Pattern regexInt = Pattern.compile("^[-\\+]?[\\d]*$");//整数
        StringBuilder constraintsToModify = new StringBuilder();//存储待修改的约束

        // 1 找到所有大于等于的和,同时记录所有包含小于等于某个值的约束的位置
        int[] leIndex = new int[iisColnstraints.length];
        Arrays.fill(leIndex, -1);
        int i = 0;
        for (String iisConstrain : iisColnstraints) {
            String[] splitIIS = iisConstrain.split("<=");
            if (regexDouble.matcher(splitIIS[0]).matches() || regexInt.matcher(splitIIS[0]).matches()) {
                maxRange += Double.parseDouble(splitIIS[0]);
            }
            if ((splitIIS.length == 2 && (regexDouble.matcher(splitIIS[1]).matches() || regexInt.matcher(splitIIS[1]).matches())) || splitIIS.length == 3) {
                leIndex[i] = 1;
            }
            i++;
        }
        maxRange += 10;
        // 2 遍历每一个IIS约束，只要包括大于等于的，就需要将原来约束中的对应的该约束小于等于的值改为maxRange；
        for (int j = 0; j < iisColnstraints.length; j++) {
            //所有不包含小于等于的约束，都删掉。
            if (leIndex[j] == -1) {
                continue;
            }
            String[] splitIIS = iisColnstraints[j].split("<=");

            //遍历原始约束，找到包含该约束的项目
            for (int k=0;k<originalConstraints.length;k++) {
                String originConstraint = originalConstraints[k];
                if (splitIIS.length == 2 && originConstraint.contains(splitIIS[0])) {
                    String[] splitOrigin = originConstraint.split("<=");
                    //如果原始约束经过<=分割后长度为2，且下标1的元素为浮点数或者整数
                    if (splitOrigin.length == 2 && (regexDouble.matcher(splitOrigin[1]).matches() || regexInt.matcher(splitOrigin[1]).matches())
                            && judgeEquals(Double.parseDouble(splitIIS[1]), Double.parseDouble(splitOrigin[1]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k]=splitOrigin[0]+"<="+maxRange;
                    }else if(splitOrigin.length==3 && judgeEquals(Double.parseDouble(splitIIS[1]), Double.parseDouble(splitOrigin[2]))){
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k]=splitOrigin[0]+"<="+splitOrigin[1]+"<="+maxRange;
                    }
                } else if (splitIIS.length == 3&&originConstraint.contains(splitIIS[1])) {
                    String[] splitOrigin = originConstraint.split("<=");
                    //如果原始约束经过<=分割后长度为2，且下标1的元素为浮点数或者整数
                    if (splitOrigin.length == 2 && (regexDouble.matcher(splitOrigin[1]).matches() || regexInt.matcher(splitOrigin[1]).matches())
                            && judgeEquals(Double.parseDouble(splitIIS[2]), Double.parseDouble(splitOrigin[1]))) {
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k]=splitOrigin[0]+"<="+maxRange;
                    }else if(splitOrigin.length==3 && judgeEquals(Double.parseDouble(splitIIS[2]), Double.parseDouble(splitOrigin[2]))){
                        constraintsToModify.append(originConstraint).append(";");
                        originalConstraints[k]=splitOrigin[0]+"<="+splitOrigin[1]+"<="+maxRange;
                    }
                }
            }
        }

        //将新的约束组合起来

        StringBuilder newConstraints = new StringBuilder();
        for(String constraint:originalConstraints){
            newConstraints.append(constraint).append("\n");
        }
        optimizeResult.setNewConstraints(newConstraints.toString());
        optimizeResult.setValueChange("The constraints:\""+constraintsToModify.toString()+"\" less than value were change to "+ maxRange);
        return optimizeResult;
    }

    private static boolean judgeEquals(double v, double v1) {
        return v == v1;
    }

    public static void main(String[] args) {
        String constraints = "0<=x-y<=5\n6<=x-y<=20\ny-z<=3";
        String iisConstraints[] = {"x-y<=5.0", "6<=x-y"};
        OptimizeResult optimizeResult = ChangeConstant.changeValue(constraints,iisConstraints);
        System.out.println(optimizeResult.getNewConstraints());
        System.out.println(optimizeResult);
    }
}
