package withcplex;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.IIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
double[]    lb      = {10.0, 0.0, 0.0};
double[]    ub      = {40.0, Double.MAX_VALUE, Double.MAX_VALUE};
String[]    varname = {"x1", "x2", "x3"};
IloNumVar[] x       = model.numVarArray(3, lb, ub, varname);
var[0] = x;

double[] objvals = {1.0, 2.0, 3.0};
//with out this,solution value = 0;it means we cannot get the maximize or minimize value;
//model.addMaximize(model.scalProd(x, objvals));
//model.addMinimize(model.scalProd(x, objvals));
rng[0] = new IloRange[2];
rng[0][0] = model.addLe(model.sum(model.prod(-1.0, x[0]),
                                  model.prod( 1.0, x[1]),
                                  model.prod( 1.0, x[2])), 20.0, "c1");
rng[0][1] = model.addLe(model.sum(model.prod( 1.0, x[0]),
                                  model.prod(-3.0, x[1]),
                                  model.prod( 1.0, x[2])), 30.0, "c2");
                                  */

public class LpModel {

    public IloNumVar[] createModel(String constraints, IloMPModeler model, IloNumVar[][] var, String target, boolean isMax)
            throws IloException {


        if (constraints == null) {
            return null;
        }
        // get every inequality
        // patterns:0<=x-y<=5 or x-y<=5 or 0<=x-y;
        String[] subConstraints = constraints.split("\n");

        int exprNum = subConstraints.length;// the num of inequality
        // for pattern of 0<=x-y<=5
        ArrayList<String> varList = new ArrayList<String>();// save the varaible
        // without repeat
        double[] lbArr = new double[exprNum];// save the lower bound
        double[] ubArr = new double[exprNum];// save the upper bound
        String[] varArrLeft = new String[exprNum];// it could be null;
        String[] varArrRight = new String[exprNum];
        // for pattern of x-y<=5 or 0<=x-y
        // ignore this
        // also ignore the other pattern such as x<5,x>4,x-5>=4 and so on

        int i = 0;// counts
        for (String s : subConstraints) {
//			System.out.println(s);
            String str = s.trim();
            String[] splitConstraints = str.split("<=");
            if (splitConstraints.length == 3) {
                // save the bounds;
                lbArr[i] = Double.parseDouble(splitConstraints[0]);
                ubArr[i] = Double.parseDouble(splitConstraints[2]);
                // save the variable
                String[] varName = splitConstraints[1].split("-");
                varArrLeft[i] = varName[0];
                varArrRight[i] = varName[1];

                // save the variable without repeat;
                if (!varList.contains(varName[0])) {
                    varList.add(varName[0]);
                }
                if (!varList.contains(varName[1])) {
                    varList.add(varName[1]);
                }

                // do not forget this
                i++;
            } else if (splitConstraints.length == 2) {
                //like 5<= a - b or a-b<=10
                // save the bounds;
                Pattern pattern1 = Pattern.compile("^[-\\+]?[\\d]*$");
                Pattern pattern2 = Pattern.compile("^[-\\+]?[.\\d]*$");
                //5<= a - b
                if(pattern1.matcher(splitConstraints[0]).matches()||pattern1.matcher(splitConstraints[0]).matches()) {

                    lbArr[i] = Double.parseDouble(splitConstraints[0]);
                    ubArr[i] = -1;
                    // save the variable
                    String[] varName = splitConstraints[1].split("-");
                    varArrLeft[i] = varName[0];
                    varArrRight[i] = varName[1];

                    // save the variable without repeat;
                    if (!varList.contains(varName[0])) {
                        varList.add(varName[0]);
                    }
                    if (!varList.contains(varName[1])) {
                        varList.add(varName[1]);
                    }
                }else{//a-b<=10
                    lbArr[i] = -1 ;
                    ubArr[i] = Double.parseDouble(splitConstraints[1]);
                    // save the variable
                    String[] varName = splitConstraints[0].split("-");
                    varArrLeft[i] = varName[0];
                    varArrRight[i] = varName[1];

                    // save the variable without repeat;
                    if (!varList.contains(varName[0])) {
                        varList.add(varName[0]);
                    }
                    if (!varList.contains(varName[1])) {
                        varList.add(varName[1]);
                    }
                }

                // do not forget this
                i++;

            } else if (splitConstraints.length == 1) {
                // other pattern, we do nothing
            }
            splitConstraints = null;
        }

        int length = varList.size();
        double[] lbPara = new double[length];
        double[] ubPara = new double[length];
        String[] varName = new String[length];
        for (int j = 0; j < length; j++) {
            lbPara[j] = 0.0;
            ubPara[j] = Double.MAX_VALUE;
            varName[j] = varList.get(j);
        }

        //创建cplex变量的值域范围
        IloNumVar[] x = model.numVarArray(length, lbPara, ubPara, varName);

        //增加目标
        //1.初始化一个数组为全0
        double[] objvas = new double[x.length];
        Arrays.fill(objvas, 0.0);
        //2. 找到所有系数和变量以及运算符
        String pattern = "[\\+-]?\\d+";
        String params[] = target.split(pattern);
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(target);
        int index=1;
        while (matcher.find()) {
            String coffei = matcher.group();
            if (coffei.equals("-")) {
                coffei = "-1.0";
            }
            if (coffei.equals("+")) {
                coffei = "+1.0";
            }
            //系数
            double c = Double.parseDouble(coffei);
            String param = params[index];
            int paramIndex = varList.indexOf(param);
            objvas[paramIndex]=c;
            index++;
        }
        if (isMax) {
            model.addMaximize(model.scalProd(x, objvas));
        } else {
            model.addMinimize(model.scalProd(x, objvas));
        }

        //增加线性约束
        var[0] = x;
        for (int j = 0; j < varArrLeft.length; j++) {
            if (varArrLeft[j] != null) {
                int m = varList.indexOf(varArrLeft[j]);
                int n = varList.indexOf(varArrRight[j]);
                if (ubArr[j] != -1) {
                    model.addLe(model.sum(model.prod(1.0, x[m]), model.prod(-1.0, x[n])), ubArr[j]);
                }
                if(lbArr[j]!=-1){
                    model.addGe(model.sum(model.prod(1.0, x[m]), model.prod(-1.0, x[n])), lbArr[j]);
                }
            }
        }
        return x;
    }

    public IloConstraint[] getIIS(IloCplex model) {

        try {
            IIS iis = model.getIIS();
            return iis.getConstraints();
        } catch (IloException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // strT:所有的约束；s1减数；s2被减数，n表示值；
	/*
	public ArrayList<BDAnalysis> boundedDelayAnalysis(IloCplex model, IloNumVar[][] var, String strT, String s1, String s2, int n,
			ArrayList<Element> pathList,boolean isMax) throws IOException {
		ArrayList<BDAnalysis> aList = new ArrayList<BDAnalysis>();

		for (int k = 0; k < pathList.size(); k++) {
			// 创建model
			try {
				BDAnalysis a = new BDAnalysis();
				// 因为是通过pathlist找的，所以一定会找到相邻的两个，这就不需要担心了。
				// 找到被减数s2
				boolean b = false;
				int i = k;
				String minuend="";
				loop1: for (; i < pathList.size(); i++) {
					Element e = pathList.get(i);
					if(e.getName().equals("InitialNode1")||e.getName().equals("ActivityFinalNode1")){
						continue loop1;
					}
					ArrayList<String> observerList = e.getSubTimeObserv();
					for (String s : observerList) {
						if (s.startsWith(s2)) {
							b = true;
							minuend = s;
							System.out.println("minuend " + minuend);
							break loop1;
						}
					}
					e=null;
					observerList=null;
				}
				//System.out.println("s1:"+minuend);
				// 找到减数s1
				String minu="";
				boolean c = false;
				if (b) {
					int j = i + 1;
					loop2: for (; j < pathList.size(); j++) {
						Element e1 = pathList.get(j);
						ArrayList<String> observerList1 = e1.getSubTimeObserv();
						for (String s : observerList1) {
							if (s.startsWith(s1)) {
								c = true;
								minu = s;
								System.out.println("minu " + minu);
								k = j + 1;
								break loop2;
							}
						}
						e1=null;
						observerList1=null;
					}
				}
				
				if (b & c) {
					// s2的位置
					int l = 0;
					for (; l < var[0].length; l++) {
						String tmp = var[0][l].getName();
						if (tmp.equals(minu)) {
							break;
						}
					}
//					System.out.println("s1的位置:"+l);
					// s1的位置
					int m = 0;
					for (; m < var[0].length; m++) {
						String tmp = var[0][m].getName();
						if (tmp.equals(minuend))
							break;
					}
//					System.out.println("s2的位置:"+m);
					// 初始化數組o
					double[] o = new double[var[0].length];
					for (int n1 = 0; n1 < o.length; n1++) {
						if (n1 == l) {
							o[n1] = -1;
						} else if (n1 == m) {
							o[n1] = 1;
						} else {
							o[n1] = 0;
						}
					}
					IloNumVar[][] var1 = new IloNumVar[1][];
					IloCplex model1= new IloCplex();
					createModel(strT, model1, var1, null,null);
					if(isMax){
						model1.addMaximize(model.scalProd(var1[0], o));
					}else{
						model1.addMinimize(model.scalProd(var1[0], o));
					}
					if(model1.solve()){
						double[] x = model1.getValues(var1[0]);
						double s1Value = x[l];
						double s2Value = x[m];
						a.setBoundDelay(s1Value - s2Value);
						if (s1Value - s2Value > n) {
							a.setB(true);
						}
						aList.add(a);
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				FileOutputStream fileOut = new FileOutputStream(new File("C:/2.txt/"),true);
				fileOut.write(e2.toString().getBytes());
			}
		}
		return aList;
	}*/
}
