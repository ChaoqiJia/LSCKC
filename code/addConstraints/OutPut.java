package addConstraints;

import GreedyAlg.greedy_baseline;
import LS_OurAlg.LS_kCenter;
import MatchingAlg.matching_baseline;
import OurAlg_LP.ApproxkCenter;
import OurAlg_impro_matching.Approx_improv_matching_kCenter;
import OurAlg_greedy_matching.Approx_greedy_matching_kCenter;
import Tools.Point;
import Tools.load_data;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static Tools.tools.getRmax1;


public class OutPut {

    // Declare point lists and variables
    public static int k; // k: number of clusters, d: dimension, markPosition: label position
    static ArrayList<Point> pointList_init = new ArrayList<>(), pointList;
    static float[] RMark;

    public static void main(String[] args) {
//        double[] c = {0.001,0.002,0.003,0.004,0.005,0.01,0.015,0.02}; //sub fig. a,b
//        double[] c = {0.001,0.005,0.01,0.015,0.02,0.1}; //sub fig. a,b
//        double[] c = {0.1}; //sub fig. a,b
//        double[] c = {0.005,0.01,0.02,0.04,0.06,0.08,0.1}; //sub fig. a,b
        double[] c = {0.1}; //sub fig. a,b

        // double[] c = {0.05, 0.1}; //the ratio of the number of constraints to the number of points in the dataset sub fig. c,d
//         double[] c1 = {0.0,0.5,1.0};//sub fig. c,d
//         double[] c1 = {0.1,0.2,0.3,0.4,0.5};//set sub fig. a,b
        double[] c1 = {0.0};//set sub fig. a,b
//        String inputFilename = args[0];
        String inputFilename = "skin";
        // default outputFilename
        String[] outputFilename = {
                "Results_vk/skin/" + inputFilename + "_LS_output",
                "Results_vk/skin/" + inputFilename + "_matching_output",
                "Results_vk/skin/" + inputFilename + "_mbl_output",
                "Results_vk/skin/" + inputFilename + "_greedy_baseline_output",
//                "Results/" + inputFilename + "_gm_output",3
//                "Results_heng/" + inputFilename + "_matching_output",
//                "Results/" + inputFilename + "_gm_output",
//                "Results/skin/11" + inputFilename + "_LP_output",
        };
//i0 8 9 171350
        int[] k_vary = {10, 15, 20, 25, 30};
        for (int i = 0; i < k_vary.length; i++) {
//        for (int i = 0; (i == 0 || (7 < i && i  < 10)); i++) {
            pointList_init = load_data.readMyFile(inputFilename, i); // Read input file
//            k = 30;
            k = k_vary[i];
            for (String file_name : outputFilename) {
                runAlg(c, c1, file_name);
            }
        }
        System.out.println("k" + k);
    }

    private static void runAlg(double[] c, double[] c1, String outputFilename) {
        int count = 5; //the number of cycles we set the small dataset is 50 and the large dataset is 20
        // Iterate over different constraints
        Random r = new Random(42);
        outputFilename = outputFilename + ".csv";
        output(outputFilename, "percent,ratio,purity,nmi,ri,cost,whole_runtime,center_runtime,RDS_time\n");
//        System.out.println("aa");
//        RMark = getRmax1(pointList_init);
//        System.out.println("bb");
//        System.out.println(RMark[0]); //For large datasets, it's advisable to record this value as input of RMark to save time during data processing.
//        System.out.println(RMark[1]); //For large datasets, it's advisable to record this value as input of RMark to save time during data processing.
//        System.out.println(RMark[2]); //For large datasets, it's advisable to record this value as input of RMark to save time during data processing.
//        RMark = new float[]{2.550829f, 0.0f, (float)(1.0f/ Math.pow(10, 5))}; wide09
//        RMark = new float[]{8853.366f, 2.828427f, (float) (8853.366f/ Math.pow(10, 3))};
        RMark = new float[]{441.67294f,0.0f,1.0E-4f}; //skin
//        RMark = new float[]{8853.366f, 2.828427f, (float) (8853.366f)};
        for (double constraint : c) {
            // Initialize constrained points generator
            Constraints constraints = new Constraints();
//            outputFilename = outputFilename + (constraint * 100) + ".csv"; //subfig. c and d.
//            output(outputFilename, "percent,ratio,purity,nmi,ri,cost,whole_runtime,center_runtime,RDS_time\n");
            // Compute maximal distance and minimal distance between any pairwise points in the pointList

            // System.out.println(RMark); For large datasets, it's advisable to record this value as input of RMark to save time during data processing.
            for (double v : c1) {
                pointList = new ArrayList<>(pointList_init);

                // Iterate to run experiments
                for (int j = 0; j < count; j++) {
                    constraints.constraints(constraint, pointList, k, r, v);
                    // Run the algorithms

                    Approx_improv_matching_kCenter ourAlg_improv_matching = new Approx_improv_matching_kCenter(pointList, constraints.cannotList, constraints.mustList, k, RMark);
                    ApproxkCenter ourAlg = new ApproxkCenter(pointList, constraints.cannotList, constraints.mustList, k, RMark);
                    Approx_greedy_matching_kCenter ourAlg_matching = new Approx_greedy_matching_kCenter(pointList, constraints.cannotList, constraints.mustList, k, RMark);
                    LS_kCenter ourAlg_LS = new LS_kCenter(pointList, constraints.cannotList, constraints.mustList, k, RMark);
                    greedy_baseline greedy_baseline = new greedy_baseline(pointList, constraints.cannotList, constraints.mustList, k, RMark);
                    matching_baseline matching_baseline = new matching_baseline(pointList, constraints.cannotList, constraints.mustList, k, RMark);

                    // Perform random experiments and store results
                    for (int exp = 0; exp < 5; exp++) {
                        System.out.println(j);
                        System.out.println(exp);
                        if (outputFilename.contains("matching")) {
                            float[] our_imp_matching = ourAlg_improv_matching.vertexCover(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(our_imp_matching).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(our_imp_matching).replaceAll("[\\[\\] ]", "").trim());
                        } else if (outputFilename.contains("LS")) {
                            float[] our_ls = ourAlg_LS.LS_kcenter(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(our_ls).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(our_ls).replaceAll("[\\[\\] ]", "").trim());
                        } else if (outputFilename.contains("gm")) {
                            float[] our_greedy_matching = ourAlg_matching.our_greedy_matching(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(our_greedy_matching).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(our_greedy_matching).replaceAll("[\\[\\] ]", "").trim());
                        } else if (outputFilename.contains("LP")) {
                            float[] our_lP = ourAlg.Our_LP(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(our_lP).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(our_lP).replaceAll("[\\[\\] ]", "").trim());
                        } else if (outputFilename.contains("greedy_baseline")) {
                            float[] greedy_baselines = greedy_baseline.greedy(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(greedy_baselines).replaceAll("[\\[\\] ]", "").trim() + "," + k +"\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(greedy_baselines).replaceAll("[\\[\\] ]", "").trim());
                        } else if (outputFilename.contains("mbl")) {
                            float[] matching_baselines = matching_baseline.matching_bl(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(matching_baselines).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(matching_baselines).replaceAll("[\\[\\] ]", "").trim());
                        }
                    }
                }
            }
        }
    }
//

    // Method to output results to a file
    public static void output(String filename, String data) {
        File file = new File(filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // Create parent directories if they don't exist
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
