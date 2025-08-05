package LS_OurAlg;

import OurAlg_LP.RDS;
import Tools.Metrics;
import Tools.Point;
import Tools.matching2;
import Tools.tools;

import java.sql.SQLOutput;
import java.util.*;

import static Tools.tools.distance;

public class LS_kCenter {
    static int k = 0, N;
    //    static double R = 0, limit = 0, Rmax, Rmin = 0, N = 0;
    static float R, errorBar, Rmax, Rmin;
    static float[] RMark;
    //    static Graph graph = null;
    static ArrayList<ArrayList<Integer>> cannotLinkSet, mustLinkSet;
    static ArrayList<Integer> must = new ArrayList<>(), centers;
    public static ArrayList<Point> pointList;

    public LS_kCenter(ArrayList<Point> pointList, ArrayList<ArrayList<Integer>> CannotList, ArrayList<ArrayList<Integer>> MustList, int k, float[] RMark) {
        this.pointList = pointList;
        this.cannotLinkSet = CannotList;
        this.mustLinkSet = MustList;
        this.k = k;
        this.RMark = RMark;
    }

    public float[] LS_kcenter(Random r) {
        System.out.println("Our_LS:___________________________________________");
        N = pointList.size();
        float[] accuracy;
        Rmax = RMark[0];
        Rmin = RMark[1];
        errorBar = RMark[2];
        centers = new ArrayList<>();
        must.clear();

        for (int i = 0; i < N; i++) {
            pointList.get(i).setClusterID(-1);
        }
        String times = algorithm(r);

        accuracy = Metrics.metrics(N, pointList, centers, k, times);
        return accuracy;

    }

    private List<List<Integer>> findCandidateCenterSet() {
        List<List<Integer>> candidateCenterSet = new ArrayList<>();
        int maximumSize = 0;
        for (ArrayList<Integer> integerArrayList : cannotLinkSet) {
            maximumSize = Math.max(maximumSize, integerArrayList.size());
        }
        for (ArrayList<Integer> integers : cannotLinkSet) {
            if (integers.size() == maximumSize) {
                candidateCenterSet.add(integers);
            }
        }
        return candidateCenterSet;
    }

    // Method to initialize center
    private List<Integer> initializeCenter(List<List<Integer>> candidateCenterSet, Random r) {
        List<Integer> initCenter = new ArrayList<>();
        if (candidateCenterSet.size() == 0) {
            initCenter.add(r.nextInt(N));
        } else {
            initCenter.addAll(candidateCenterSet.get(r.nextInt(candidateCenterSet.size())));
        }
        return initCenter;
    }


    private String algorithm(Random r) {
        // Select the center processing 2258 2449
        long startTime1 = System.nanoTime();

        List<List<Integer>> candidateCenterSet = findCandidateCenterSet();
        List<Integer> initCenter = initializeCenter(candidateCenterSet, r);
        boolean flag = false;

        //ensure R
        while (Rmax - Rmin > errorBar || !flag) {
            //change R
            R = (Rmin + Rmax) / 2.0f;
            if (R == Rmin || R == Rmax) {
                Rmin = R = Rmax;
            }

            //initialization
            flag = true;
            centers = new ArrayList<>(initCenter);
            must.clear();
            updateMLinCenter(centers);

            //Stage 1(a): Selection of centers for points without CL constraints.
            flag = farthestPoint();

            //Stage 1(b): Selection of centers for points with CL constraints.
            if (flag && centers.size() < k) {
                ArrayList<Integer> center_2 = addCenter();
                if (centers.size() > k) {
                    //Stage 2: Removal of redundant CL centers and elimination of single swaps
                    for (int i = 0; i < center_2.size() - 1; i++) {
                        for (int j = i + 1; j < center_2.size(); j++) {
                            for (int l = 0; l < pointList.size(); l++) {
                                Point p = pointList.get(l);
                                Point u = pointList.get(center_2.get(i));
                                Point v = pointList.get(center_2.get(j));
                                if (Math.max(distance(u, p), distance(p, v)) <= 4 * R) {
                                    centers.add(Integer.valueOf(l));
                                    centers.remove(Integer.valueOf(center_2.get(i)));
                                    centers.remove(Integer.valueOf(center_2.get(j)));
                                    must.remove(Integer.valueOf(u.getMustID()));
                                    must.remove(Integer.valueOf(v.getMustID()));
                                    if (p.getMustID() != -1) {
                                        must.add(Integer.valueOf(p.getMustID()));
                                    }
                                    if (!judgeCannot(R)) {
                                        if (centers.contains(l)) centers.remove(Integer.valueOf(l));
                                        if (p.getMustID() != -1) must.remove(Integer.valueOf(p.getMustID()));
                                        centers.add(Integer.valueOf(center_2.get(i)));
                                        centers.add(Integer.valueOf(center_2.get(j)));
                                        if (u.getMustID() != -1) must.add(Integer.valueOf(u.getMustID()));
                                        if (v.getMustID() != -1) must.add(Integer.valueOf(v.getMustID()));
                                    } else {
                                        center_2.remove(Integer.valueOf(center_2.get(j)));
                                        center_2.remove(Integer.valueOf(center_2.get(i)));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                flag = centers.size() <= k;
            } else if (centers.size() == k) {
                flag = judgeCannot(R);
            }
            System.out.println(Rmax);
            System.out.println(Rmin);
            System.out.println(R);

            if (flag) {
                Rmax = R;
            } else {
                Rmin = R;
            }
        }

        if (centers.size() < k) {
            Float[] Dist_Points = new Float[N];
            Arrays.fill(Dist_Points, 0.0f);
            getKPoint(centers, Dist_Points);
        }

//        for (int i = 0; i < centers.size(); i++) {
//            System.out.println(centers.get(i));
//        }
        setClusterIDs(centers);
        updateMLinCenter(centers);
        assignMust(centers);
        assignCannot(centers);
        assignGene(centers);
        return String.valueOf(System.nanoTime() - startTime1);
    }

    private void setClusterIDs(List<Integer> centers) {
        for (int i = 0; i < centers.size(); i++) {
            pointList.get(centers.get(i)).setClusterID(i);
        }
    }

    private void getKPoint(List<Integer> input_center, Float[] Dist_Points) {
        int farPID = -1;
        float farPdistance = -1;

        for (int i = 0; i < pointList.size(); i++) {
            Point currentPoint = pointList.get(i);
            int mustID = currentPoint.getMustID();

            if (mustID==-1) {
                List<Integer> linkedPoints = mustID != -1 ? mustLinkSet.get(mustID) : Collections.singletonList(i);
                int mark = i;
                float minDist = Float.MAX_VALUE;

                for (int center : input_center) {
                    float maxDist = 0;
                    for (int linkedPointID : linkedPoints) {
                        float dist = distance(pointList.get(center), pointList.get(linkedPointID));
                        if (maxDist < dist) {
                            maxDist = dist;
                            mark = linkedPointID;
                        }
                    }
                    minDist = Math.min(minDist, maxDist);
                }
                for (int linkedPointID : linkedPoints) {
                    Dist_Points[linkedPointID] = Math.min(minDist, Dist_Points[linkedPointID]);
                }
                if (Dist_Points[mark] > farPdistance) {
                    farPdistance = Dist_Points[mark];
                    farPID = mark;
                }
            }
        }
        centers.add(farPID);
        if (pointList.get(farPID).getMustID() != -1) must.add(pointList.get(farPID).getMustID());
        if (centers.size() < k) getKPoint(Collections.singletonList(farPID), Dist_Points);
    }

    // Method to update must list
    private void updateMLinCenter(ArrayList<Integer> currentcenters) {
        must.clear();
        for (int i = 0; i < mustLinkSet.size(); i++) {
            ArrayList<Integer> commonElements = new ArrayList<>(mustLinkSet.get(i));
            commonElements.retainAll(currentcenters);
            if (!commonElements.isEmpty()) {
                must.add(i);
            }
        }
    }


    // Method to update centers
    private boolean farthestPoint() {
//    if the distance is large than 2r* in a ml set return false.
        for (int c_integers : centers) {
            Point currentPoint = pointList.get(c_integers);
            int mustID = currentPoint.getMustID();
            if (mustID != -1)
                for (int j = 0; j < mustLinkSet.get(mustID).size(); j++) {
//                for (int l = j + 1; l < mustLinkSet.get(ml_integers).size(); l++) {
                    if (distance(currentPoint, pointList.get(mustLinkSet.get(mustID).get(j))) > R) {
                        return false;
                    }
                }
        }


// add center to the set and check the size of the center set
        for (int i = 0; i < pointList.size(); i++) {
            Point currentPoint = pointList.get(i);
            int mustID = currentPoint.getMustID();
            boolean Flag_f = true;

            if (!must.contains(mustID)) {
                List<Integer> linkedPoints = mustID != -1 ? mustLinkSet.get(mustID) : Collections.singletonList(i);
                int mark = i;
                for (int center : centers) {
                    float maxDist = 0;
                    for (int linkedPointID : linkedPoints) {
                        float dist = distance(pointList.get(center), pointList.get(linkedPointID));
                        if (maxDist < dist) {
                            maxDist = dist;
                            mark = linkedPointID;
                        }
                    }
                    if (maxDist <= R) {
                        Flag_f = false;
                        break;
                    }
                }
                if (Flag_f) {
                    centers.add(mark);
                    if (mustID != -1) must.add(mustID);
                }
            }
            if (centers.size() > k) {
                return false;
            }
        }
        return true;
    }
    // Method to update centers

    //assign the points of must-link set and ensure the points

    private void assignMust(ArrayList<Integer> currentcenters) {

        for (int mustID : must) {
            for (Integer centerID : currentcenters) {
                if (pointList.get(centerID).getMustID() == mustID) {
                    for (int linkedPointID : mustLinkSet.get(mustID)) {
                        pointList.get(linkedPointID).setClusterID(pointList.get(centerID).getClusterID());
                    }
                    break;
                }
            }
        }


        for (int j = 0; j < mustLinkSet.size(); j++) {
            int mark = -1;
            if (!must.contains(j)) {
                float minDist = Float.MAX_VALUE;

                for (int l = 0; l < currentcenters.size(); l++) {
                    float maxDist = 0;
                    Point centerPoint = pointList.get(currentcenters.get(l));

                    for (int linkedPointID : mustLinkSet.get(j)) {
                        float tempDist = Math.max(maxDist, distance(centerPoint, pointList.get(linkedPointID)));
                        if (centerPoint.getMustID() != -1) {
                            for (int mustID : mustLinkSet.get(centerPoint.getMustID())) {
                                tempDist = Math.max(tempDist, distance(pointList.get(linkedPointID), pointList.get(mustID)));
                            }
                        }
                        maxDist = Math.max(maxDist, tempDist);
                    }
                    if (minDist > maxDist) {
                        minDist = maxDist;
                        mark = l;
                    }
                    for (int p_i : mustLinkSet.get(j)) {
                        Point p = pointList.get(p_i);
                        p.setClusterID(mark);
                    }
                }
            }
        }
    }


    private void assignCannot(ArrayList<Integer> currentcenters) {

        //for cannot-link:add center
        for (List<Integer> cannotLink : cannotLinkSet) {
            ArrayList<Integer> pointsLeft = new ArrayList<>(currentcenters);
            ArrayList<Integer> pointsRight = new ArrayList<>(cannotLink);
            List<Integer> repeatLeft = new ArrayList<>();
            List<Integer> repeatRight = new ArrayList<>();
            processCannotLinks(cannotLink, repeatLeft, repeatRight);
            pointsLeft.removeAll(repeatLeft);
            pointsRight.removeAll(repeatRight);

            // float inRmax = RMark[0]; //(interested)
            float inRmax = R;
            while (pointsRight.size() > 1) {
                float inRmin = 0;
                float inR = (inRmin + inRmax) / 2.0f;
                boolean conditionMet = false;
                while (inRmax - inR > errorBar || !conditionMet) {
                    inR = (inRmin + inRmax) / 2.0f;
                    if (inR == inRmax || inR == inRmin) {
                        inR = inRmax;
                        inRmin = inRmax;
                    }
                    System.out.println("int");
                    System.out.println(inR);
                    System.out.println(inRmax);
                    System.out.println(inRmin);
                    if (judgeCannotOnce(inR, pointsLeft, pointsRight)) {
                        inRmax = inR;
                        conditionMet = true;
                    } else {
                        inRmin = inR;
                        conditionMet = false;
                    }
                }
                matching2 MAlg = new matching2(pointList, pointsLeft, pointsRight, inR, mustLinkSet);
                MAlg.assignCannot(pointList, mustLinkSet);

                int markFar = 0;
                float farPointDist = 0;
                for (int j = 0; j < pointsRight.size(); j++) {
                    float dist = distance(pointList.get(pointsRight.get(j)), pointList.get(pointsLeft.get(MAlg.match[j])));
                    if (dist > farPointDist) {
                        markFar = j;
                        farPointDist = dist;
                    }
                }
                pointsLeft.remove(MAlg.match[markFar]);
                pointsRight.remove(markFar);
            }

            if (pointsRight.size() == 1) {
                Point rightPoint = pointList.get(pointsRight.get(0));
                int mustID = rightPoint.getMustID();

                float minDist = Float.MAX_VALUE;
                int mark = 0;

                for (int j = 0; j < pointsLeft.size(); j++) {
                    Point leftPoint = pointList.get(pointsLeft.get(j));
                    float dmax = distance(rightPoint, leftPoint);

                    if (mustID != -1) {
                        for (int linkedPointID : mustLinkSet.get(mustID)) {
                            Point linkedPoint = pointList.get(linkedPointID);
                            dmax = Math.max(dmax, distance(linkedPoint, leftPoint));
                        }
                    }
                    if (dmax < minDist) {
                        mark = j;
                        minDist = dmax;
                    }
                }
                int clusterID = pointList.get(pointsLeft.get(mark)).getClusterID();
                rightPoint.setClusterID(clusterID);
                if (mustID != -1) {
                    for (int linkedPointID : mustLinkSet.get(mustID)) {
                        pointList.get(linkedPointID).setClusterID(clusterID);
                    }
                }
            }
        }
    }

    private static void assignGene(ArrayList<Integer> currentcenters) {
        for (Point point : pointList) {
            if (point.getMustID() == -1 && point.getConID() == -1) { //(disjointed)
                float mindistance = Float.MAX_VALUE;
                for (int l = 0; l < currentcenters.size(); l++) {
                    float dist = tools.distance(pointList.get(currentcenters.get(l)), point);
                    if (dist < mindistance) {
                        point.setClusterID(l);
                        mindistance = dist;
                    }
                }
            }
        }
    }




    private void processCannotLinks(List<Integer> cannotLink, List<Integer> repeatLeft, List<Integer> repeatRight) {

        for (int e : cannotLink) {
            Point point = pointList.get(e);
            int mustID = point.getMustID();
            if (centers.contains(e)) {
                repeatLeft.add(e);
                repeatRight.add(e);
            } else if (must.contains(mustID)) {
                repeatRight.add(e);
                for (Integer center : centers) {
                    if (pointList.get(center).getMustID() == mustID) {
                        repeatLeft.add(center);
                    }
                }
            }
        }
    }

    ArrayList<Integer> addCenter() {
        ArrayList<Integer> center_2 = new ArrayList<>();

        //for cannot-link:add center
        for (List<Integer> cannotLink : cannotLinkSet) {
            List<Integer> pointLeft = new ArrayList<>(centers);
            List<Integer> pointRight = new ArrayList<>(cannotLink);
            List<Integer> repeatLeft = new ArrayList<>();
            List<Integer> repeatRight = new ArrayList<>();
            processCannotLinks(cannotLink, repeatLeft, repeatRight);
            pointLeft.removeAll(repeatLeft);
            pointRight.removeAll(repeatRight);

            matching2 minMaxMatchingNew1 = new matching2(pointList, pointLeft, pointRight, R, mustLinkSet);
            int matchsize = minMaxMatchingNew1.searchcount();
            if (matchsize < pointRight.size()) {
                int[] matching = minMaxMatchingNew1.match;

                for (int j = 0; j < matching.length; j++) {
                    if (matching[j] == -1) {
                        centers.add(pointRight.get(j));
                        center_2.add(pointRight.get(j));
                        if (pointList.get(pointRight.get(j)).getMustID() != -1) {
                            must.add(pointList.get(pointRight.get(j)).getMustID());
                        }
                    }
                }
            }
        }
        return center_2;
    }

    private boolean judgeCannot(float inputR) {
        boolean flag = true;
        for (List<Integer> cannotLink : cannotLinkSet) {
            List<Integer> pointLeft = new ArrayList<>(centers);
            List<Integer> pointRight = new ArrayList<>(cannotLink);
            List<Integer> repeatLeft = new ArrayList<>();
            List<Integer> repeatRight = new ArrayList<>();
            processCannotLinks(cannotLink, repeatLeft, repeatRight);
            pointLeft.removeAll(repeatLeft);
            pointRight.removeAll(repeatRight);

            flag = judgeCannotOnce(inputR, pointLeft, pointRight);
            if (!flag) {
                return false;
            }
        }
        return flag;
    }


    private boolean judgeCannotOnce(float inputR, List<Integer> pointsLeft, List<Integer> pointsRight) {

        //for cannot-link:add center
        matching2 minMaxMatchingNew = new matching2(pointList, pointsLeft, pointsRight, inputR, mustLinkSet);
        return minMaxMatchingNew.searchcount() >= pointsRight.size();
    }

}


