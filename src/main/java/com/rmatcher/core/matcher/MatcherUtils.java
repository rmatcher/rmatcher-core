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
 * To change this template use File | Settings | File Templates.
 */
public final class MatcherUtils {
    private MatcherUtils(){}


    public static Map<String, Double[]> getRecommendations(String user, Connection connect) throws SQLException {

        //get biz the user rated excluding the testing reviews
        Map<String, Double> userRatedBusinesses = Utils.getListOfBusinessesFromUser(true, user, connect);
        //get users ratings for all the training biz of the user we are giving recommendation to
        Map<String, Map<String, Double>> commonReviewsByUser = Utils.getReviewsForBusinesses(userRatedBusinesses.keySet(), user, connect);

        //returns user: [pearsonCorrScore, Distance, pearsonCorrScore+NormalisedDistance]
        Map<String, Double[]> correlatedUsers = computeCorrelation(userRatedBusinesses, commonReviewsByUser);

        //List of sorted entries in DESC order
        List<Map.Entry<String, Double[]>> entries = convertToSortedEntries(correlatedUsers);

        //Sublist of top correlated users
        List<String> users = new ArrayList<>();
        for(Map.Entry<String, Double[]> entry : entries.subList(0, (int)(0.3*entries.size())))
        {
            System.out.println(entry.getKey() + " " + entry.getValue()[2]);
            users.add(entry.getKey());
        }
        // return businesses for the top correlated users
        return Utils.getListBusinessesFromUsers(users, connect);
    }


    private static List<Map.Entry<String, Double[]>> convertToSortedEntries(Map<String, Double[]> correlatedUsers) {
        List<Map.Entry<String, Double[]>> entries = new ArrayList<Map.Entry<String, Double[]>>(correlatedUsers.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Double[]>>() {
            public int compare(Map.Entry<String, Double[]> a, Map.Entry<String, Double[]> b) {
                return b.getValue()[2].compareTo(a.getValue()[2]);
            }
        });
        return entries;
    }


    private static Map<String, Double[]> computeCorrelation(Map<String, Double> userRatedBusinesses, Map<String, Map<String, Double>> commonReviewsByUser) {
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        DistanceMeasure euclideanDistanceMeasure = new EuclideanDistanceMeasure();
        Map<String, Double[]> correlatedUsers = new LinkedHashMap<>();
        Double maxDistance = 0.0;

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

            //if correlation is NaN, it means that one user ratings of common biz did not change => sd = 0 => ignore such cases
            if(Double.isNaN(corrMatrix.getEntry(0, 1))){
                continue;
            }

            //add the user computed values to a Map
            Double[] values = {corrMatrix.getEntry(0, 1), distance, 0.0};
            correlatedUsers.put(user_id, values);

            //Keep track of MaxDistance for Normalisation later
            if(distance > maxDistance){
                maxDistance = distance;
            }

            //System.out.print(Arrays.deepToString(xyRatings));
            //System.out.println(" Num of biz: " + i + " ,Correlation: " + corrMatrix.getEntry(0, 1) + ", distance: "+ distance);

        }

        //Normalize the Distance and add it with the correlation value in index=2
        for(String u : correlatedUsers.keySet()){
            Double[] values = correlatedUsers.get(u);
            values[1] = 1 - (values[1]/maxDistance);
            values[2] = values[0] + values[1];       //This seems better, but still need better normalisation based on users num of common ratings
            //values[2] = values[0];
        }
        return correlatedUsers;
    }

}
