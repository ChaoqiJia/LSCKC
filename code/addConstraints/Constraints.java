package addConstraints;
import Tools.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
/*
 *  generate the disjoint (intersected) CL&ML cosntraints.
 *
 * pointList: the list of the points
 * ConstrainedPoints: the constrained points
 * pointListGroup: the group of the constrained points
 * mustList: the list of the ML constraints
 * cannotList: the list of the CL constraints
 * d: dimension
 * k: parameter
 * markPosition: marking the label position for different datasets
 * */

public class Constraints {
    ArrayList<Point> CPoints = new ArrayList<>();
    ArrayList<ArrayList<Integer>> pointListGroup = new ArrayList<>(), mustList = new ArrayList<>(), cannotList = new ArrayList<>();

    // rate: ratio of the number of constraint points
    public void constraints(double rate, ArrayList<Point> pointList, int k,  Random random, double rateJoint) {
        initConduct(pointList);

        // Shuffling pointList and reset their IDs
        Collections.shuffle(pointList, random);
        int listSize = pointList.size();
        for (int i = 0; i < listSize; i++) {
            pointList.get(i).setID(i);
        }

        // Select a subset of points based on the rate
        for (int i = 0; i < (int) Math.ceil(listSize * rate); i++) {
//            int random_p = random.nextInt((int) Math.ceil(listSize * rate)); //(intersected)
//            CPoints.add(pointList.get(random_p)); //(intersected)
            CPoints.add(pointList.get(i));  //(disjoint and 5% and 10% intersected)
        }

        // Group the constrained points
        int groupNum = 0;
        for (int i = 0; i < CPoints.size(); ) {

            int groupSize = 0;
            while (pointListGroup.size() <= groupNum) {
                pointListGroup.add(new ArrayList<>());
            }

            //Make sure each point in constraints
            if (CPoints.size() - i == 1) {
                pointListGroup.get(--groupNum).add(CPoints.get(i).getID());
                pointListGroup.remove(groupNum);
                break;
            }

            // Generate a valid group size (at least 2 points)
            do {
                groupSize = random.nextInt(CPoints.size() - i) + 1;
            } while (groupSize < 2);

            for (int j = 0; j < groupSize; j++) {
                pointListGroup.get(groupNum).add(CPoints.get(i++).getID());
            }
            groupNum++;
        }
      /* //(disjoint and 5% and 10% intersected)
        double sizenum = 0;
        ArrayList<Integer> aa = new ArrayList<>();
        for (ArrayList<Integer> integerArrayList : pointListGroup) {
            aa.add(integerArrayList.size());
        }
        for (int i = 0; i < pointListGroup.size(); i++) {
            sizenum += aa.get(i);
            double addsize = 0;
            if (sizenum > 0) {
                addsize = sizenum * rateJoint;
                for (int j = 0; j < addsize; j++) {
                    int p = pointListGroup.get(i).get(j);
                    pointListGroup.get((i + 1) % pointListGroup.size()).add(p);
                }
            }
            if (addsize != 0)
                sizenum -= addsize / rateJoint;
        }
        for (ArrayList<Integer> integerArrayList : pointListGroup) {
            Collections.shuffle(integerArrayList,random);
        }
        */
        // Generate the cannot-link (CL) and must-link (ML) sets
        int CLNum = 0, MLNum = 0;

        for (int i = 0; i < pointListGroup.size() && !pointListGroup.get(i).isEmpty(); i++) {
            List<Integer> cL = new ArrayList<>();
            List<Integer>[] mLID = new List[k];

            for (int j = 0; j < pointListGroup.get(i).size(); j++) {
                int pointID = pointListGroup.get(i).get(j);
                Point point = pointList.get(pointID);
                int labelModK = point.getLabel() % k;

                // cannot-link set generation
                if (!cL.contains(labelModK)) {
                    cL.add(labelModK);

                    if (cannotList.size() <= CLNum) {
                        cannotList.add(new ArrayList<>());
                    }
                    point.setConID(CLNum);
                    cannotList.get(CLNum).add(pointID);
                }

                if (mLID[labelModK] == null) {
                    mLID[labelModK] = new ArrayList<>();
                }
                mLID[labelModK].add(j);
            }

            // Remove single point in cl sets
            if (cannotList.get(CLNum).size() < 2) {
                pointList.get(cannotList.get(CLNum).get(0)).setConID(-1);
                cannotList.get(CLNum--).clear();
            }
            CLNum++;

            // must-link set generation
            for (List<Integer> integers : mLID) {
                if (integers != null && integers.size() > 1) {
                    for (Integer integer : integers) {
                        int index = (int) integer;

                        if (mustList.size() <= MLNum) {
                            mustList.add(new ArrayList<>());
                        }
                        // must-link generation
                        pointList.get(pointListGroup.get(i).get(index)).setMustID(MLNum);
                        mustList.get(MLNum).add(pointListGroup.get(i).get(index));
                    }
                    MLNum++;
                }
            }
        }
    }

    private void initConduct(ArrayList<Point> pointList) {
        pointListGroup.clear();
        CPoints.clear();
        mustList.clear();
        cannotList.clear();

        // Reset the constraint IDs of each point
        for (Point point : pointList) {
            point.setConID(-1);
            point.setMustID(-1);
        }
    }
}
