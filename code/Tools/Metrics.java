package Tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import static Tools.tools.distance;

public class Metrics {
    static float[][] assess = null;
    public static String[] desc = { cost: ", ", whole time: "};


    public static float[] metrics(int N, ArrayList<Point> pointlist, ArrayList<Integer> centers, int k, String times) {
        assess = new float[k][k];
        float maxDist = 0;
        float maxDist_mark = 0;
        int mark = -1;

        for (int i = 0; i < N; i++) {
            maxDist = Math.max(maxDist, distance(pointlist.get(i), pointlist.get(centers.get(pointlist.get(i).getClusterID()))));
            if(maxDist_mark != maxDist){
                mark = i;
            }
            //label and assignment
            int Pmark = pointlist.get(i).getLabel() % k;
            int Pcluster = pointlist.get(i).getClusterID();
            assess[Pcluster][Pmark]++;
        }
        return new float[]{maxDist,mark};
    }


    public static float[] evalu_cost(int N, ArrayList<Point> pointlist, ArrayList<Integer> centers, int k, ArrayList<Integer> must) {
        assess = new float[k][k];
        float maxDist = 0;
        float maxDist_mark = 0;
        int mark = -1;

        for (int i = 0; i < N; i++) {
            if (!centers.contains(i) && !must.contains(pointlist.get(i).getMustID())) {
                maxDist = Math.max(maxDist, distance(pointlist.get(i), pointlist.get(centers.get(pointlist.get(i).getClusterID()))));
                if (maxDist_mark != maxDist) {
                    mark = i;
                }
                maxDist_mark = maxDist;
                //label and assignment
                int Pmark = pointlist.get(i).getLabel() % k;
                int Pcluster = pointlist.get(i).getClusterID();
                assess[Pcluster][Pmark]++;
            }
        }

        return new float[]{maxDist,mark};
    }

    public static String toString(float[] metric, String[] desc) {
        StringBuilder accuracy = new StringBuilder();

        for (int i = 0; i < metric.length; i++) {
            accuracy.append(desc[i]).append(metric[i]);
        }
        return accuracy + ",\n";
    }

    private static float costFunction(float maxDist) {
        return maxDist;
    }

}
