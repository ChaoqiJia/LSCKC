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

        double[] c = {0.01,0.02,0.04,0.06,0.08,0.1}; //sub fig. a,b
        double[] c1 = {0.0};//set sub fig. a,b
//        String inputFilename = args[0];
        String inputFilename = "cnae";
        // default outputFilename
        String[] outputFilename = {
                "Results/cnae/" + inputFilename + "_LS_output",
        };

        int[] k_vary = { 30};
        for (int i = 0; i < k_vary.length; i++) {
            pointList_init = load_data.readMyFile(inputFilename, i); // Read input file
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
        RMark = getRmax1(pointList_init);
        for (double constraint : c) {
            // Initialize constrained points generator
            Constraints constraints = new Constraints();
            for (double v : c1) {
                pointList = new ArrayList<>(pointList_init);

                // Iterate to run experiments
                for (int j = 0; j < count; j++) {
                    constraints.constraints(constraint, pointList, k, r, v);
                    // Run the algorithms

                    LS_kCenter ourAlg_LS = new LS_kCenter(pointList, constraints.cannotList, constraints.mustList, k, RMark);


                    // Perform random experiments and store results
                    for (int exp = 0; exp < 5; exp++) {
                        System.out.println(j);
                     if (outputFilename.contains("LS")) {
                            float[] our_ls = ourAlg_LS.LS_kcenter(r);
                            output(outputFilename, constraint + ", " + v + ", " + Arrays.toString(our_ls).replaceAll("[\\[\\] ]", "").trim() + "," + k + "\n");
                            System.out.println(constraint + ", " + v + ", " + Arrays.toString(our_ls).replaceAll("[\\[\\] ]", "").trim());
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
