package com.rmatcher.core.matcher;

import com.rmatcher.core.database.Utils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.SequentialAccessSparseVector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ameen
 * Date: 5/18/13
 * Time: 5:59 PM
 */
public final class MatcherUtils {
    public static int CORRELATION = 0;
    public static int DISTANCE = 1;
    public static int NUM_COMMON = 2;
    public static int TOTAL = 3;

    private MatcherUtils(){}

    public static Map<String, Double[]> getRecommendationsBasic(Map<String, Double> userRatedBusinesses, String user, Connection connect) throws SQLException {

        //get users ratings for all the training biz of the user we are giving recommendation to
        Map<String, Map<String, Double>> commonReviewsByUser = Utils.getReviewsForBusinesses(userRatedBusinesses.keySet(), user, connect);

        //returns user: [pearsonCorrScore, Distance, pearsonCorrScore+NormalisedDistance]
        Map<String, Double[]> correlatedUsers = computeCorrelation(userRatedBusinesses, commonReviewsByUser);

        //List of sorted entries in DESC order
        List<Map.Entry<String, Double[]>> entries = convertToSortedEntries(correlatedUsers, CORRELATION);

        //Sublist of top correlated users
        List<String> users = new ArrayList<>();
        for(Map.Entry<String, Double[]> entry : entries)
        {
            //System.out.println(entry.getKey() + " " + entry.getValue()[CORRELATION]);
            if(entry.getValue()[CORRELATION] < 0.95)
                break;
            users.add(entry.getKey());
        }
        // return businesses for the top correlated users
        return Utils.getAverageStarsBusinessesFromUsers(users, connect);
    }

    public static Map<String, Double[]> getRecommendationsAdvance(Map<String, Double> userRatedBusinesses, String user, Connection connect) throws SQLException {

        //get users ratings for all the training biz of the user we are giving recommendation to
        Map<String, Map<String, Double>> commonReviewsByUser = Utils.getReviewsForBusinesses(userRatedBusinesses.keySet(), user, connect);

        //returns user: [pearsonCorrScore, Distance, pearsonCorrScore+NormalisedDistance]
        Map<String, Double[]> correlatedUsers = computeCorrelation(userRatedBusinesses, commonReviewsByUser);

        //List of sorted entries in DESC order
        List<Map.Entry<String, Double[]>> entries = convertToSortedEntries(correlatedUsers, TOTAL);

        //Sublist of top correlated users
        List<String> users = new ArrayList<>();
        for(Map.Entry<String, Double[]> entry : entries)
        {
            //System.out.println(entry.getKey() + " " + entry.getValue()[TOTAL]);
            if(entry.getValue()[TOTAL] < 2.1)
                break;
            users.add(entry.getKey());
        }
        // return businesses for the top correlated users
        return Utils.getBusinessesFromUsers(users, connect);
    }

    private static List<Map.Entry<String, Double[]>> convertToSortedEntries(Map<String, Double[]> correlatedUsers, final int by) {
        List<Map.Entry<String, Double[]>> entries = new ArrayList<Map.Entry<String, Double[]>>(correlatedUsers.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Double[]>>() {
            public int compare(Map.Entry<String, Double[]> a, Map.Entry<String, Double[]> b) {
                return b.getValue()[by].compareTo(a.getValue()[by]);
            }
        });
        return entries;
    }


    private static Map<String, Double[]> computeCorrelation(Map<String, Double> userRatedBusinesses, Map<String, Map<String, Double>> commonReviewsByUser) {
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        DistanceMeasure euclideanDistanceMeasure = new EuclideanDistanceMeasure();
        Map<String, Double[]> correlatedUsers = new LinkedHashMap<>();
        Double maxDistance = 0.0;
        int maxCommon = 0;

        //For each user
        for (String user_id : commonReviewsByUser.keySet()) {
            //commonReviews with that user
            Map<String, Double> reviews = commonReviewsByUser.get(user_id);

            //Ignore that user if less than 2 biz in common with our user
            if(reviews.keySet().size() < 2){
                continue;
            }

            //two columns of doubles containing UserA and UserB ratings for the PearsonCorr
            double[][] xyRatings = new double[reviews.size()][2];
            //two vectors of values for the distance
            org.apache.mahout.math.Vector x = new SequentialAccessSparseVector(reviews.size());
            org.apache.mahout.math.Vector y = new SequentialAccessSparseVector(reviews.size());

            //load the columns and vectors
            int i = 0;
            for(String biz : reviews.keySet()){
                xyRatings[i][0] = reviews.get(biz);
                x.set(i, reviews.get(biz));
                xyRatings[i][1] = userRatedBusinesses.get(biz);
                y.set(i, userRatedBusinesses.get(biz));
                i++;
            }

            //get scores
            RealMatrix corrMatrix = pearsonsCorrelation.computeCorrelationMatrix(xyRatings);
            Double distance = euclideanDistanceMeasure.distance(x, y);
            Double correlation = corrMatrix.getEntry(0, 1);

            //if correlation is NaN, it means that one user ratings of common biz did not change => sd = 0 => ignore if not equal ratings.
            if(Double.isNaN(correlation)){
                if(distance.intValue() != 0)
                    continue;
                else
                    correlation = 1.0;
            }

            //add the user computed values to a Map
            Double[] values = {correlation, distance, 1.0*reviews.keySet().size(), 0.0};
            correlatedUsers.put(user_id, values);

            //Keep track of MaxDistance for Normalisation later
            if(distance > maxDistance){
                maxDistance = distance;
            }

            //Keep track of MaxCommon for Normalisation later
            if(reviews.keySet().size() > maxCommon){
                maxCommon = reviews.keySet().size();
            }

            //System.out.print(Arrays.deepToString(xyRatings));
            //System.out.println(" Num of biz: " + i + " ,Correlation: " + corrMatrix.getEntry(0, 1) + ", distance: "+ distance);

        }

        //Normalize the Distance and add it with the correlation value in index=2
        for(String u : correlatedUsers.keySet()){
            Double[] values = correlatedUsers.get(u);
            values[NUM_COMMON] = values[NUM_COMMON]/maxCommon;
            values[DISTANCE] = 1 - (values[DISTANCE]/maxDistance);
            values[TOTAL] = (values[CORRELATION] + values[DISTANCE] + values[NUM_COMMON]);
        }
        return correlatedUsers;
    }
}
