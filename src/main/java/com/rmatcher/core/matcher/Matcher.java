package com.rmatcher.core.matcher;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/18/13
 * Time: 8:19 PM
 */

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.*;
import java.util.Arrays;

public class Matcher {

    public static void main(String [] args) throws Exception {

        Connection connect = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?user=root&password=");
            connect.setAutoCommit(false);

            String user = "5mnhUiRWi-q6nX7TkicE2A";

            //return recommendations for a given user. Will return biz_id and rating by other user
            Map<String, Double[]> recommendedBusinesses = getRecommendations(user, connect);

            //System.out.println("////////////Recommended Businesses////////////////");
            //for(String biz : recommendedBusinesses.keySet())
            //{
            //    System.out.println(biz + " " + recommendedBusinesses.get(biz) );
            //}

            System.out.println("////////////Comparison of results////////////////");

            Map<String, Double> userTestRatedBusinesses = getListOfBusinessesFromUser(false, user, connect);
            for(String biz : userTestRatedBusinesses.keySet())
            {
                Double[] values = recommendedBusinesses.get(biz);
                Double stars = values != null ? values[0] : null;
                Double confidence = values != null ? values[1] : null;

                System.out.println(biz + " | " + userTestRatedBusinesses.get(biz) + " | " + stars + "  " + confidence );
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    private static Map<String, Double[]> getRecommendations(String user, Connection connect) throws SQLException {

        //get biz the user rated excluding the testing reviews
        Map<String, Double> userRatedBusinesses = getListOfBusinessesFromUser(true, user, connect);
        //get users ratings for all the training biz of the user we are giving recommendation to
        Map<String, Map<String, Double>> commonReviewsByUser = getReviewsForBusinesses(userRatedBusinesses.keySet(), user, connect);

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
        return getListBusinessesFromUsers(users, connect);
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

    public static Map<String, Map<String, Double>> getReviewsForBusinesses(Set<String> bizSet, String excludedUser, Connection connect)
            throws SQLException{

        Map<String, Map<String, Double>> reviews = new HashMap<>();

        ResultSet resultSet = null;
        PreparedStatement statement = connect
                .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ? AND user_id != ?");

        try{
            for (String biz : bizSet) {
                statement.setString(1, biz);
                statement.setString(2, excludedUser);
                statement.execute();
                resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    String user_id = resultSet.getString("user_id");
                    Double stars = resultSet.getDouble("stars");

                    if(!reviews.containsKey(user_id)){
                        reviews.put(user_id, new HashMap<String, Double>());
                    }

                    reviews.get(user_id).put(biz, stars);
                }
            }
        }  catch (Exception e) {
            throw e;
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            statement.close();
        }

        return reviews;
    }

    public static Map<String, Double> getListOfBusinessesFromUser(Boolean training, String user_id, Connection connect)
            throws SQLException{

        String stmt;
        if(training){
            stmt = "SELECT business_id, stars FROM rmatcher.review WHERE user_id = ? AND review_id NOT IN (SELECT review_id FROM viewTestCase)";
        }else{
            stmt = "SELECT business_id, stars FROM rmatcher.review WHERE user_id = ? AND review_id     IN (SELECT review_id FROM viewTestCase)";
        }
        PreparedStatement statement = connect
                .prepareStatement(stmt);

        return getListOfBusinessesHelper(user_id, statement);
    }

    private static Map<String, Double> getListOfBusinessesHelper(String user_id, PreparedStatement statement) throws SQLException {
        Map<String, Double> userRatedBusinesses = new HashMap<>();
        ResultSet resultSet = null;
        try{
            statement.setString(1, user_id);
            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                userRatedBusinesses.put(resultSet.getString("business_id"), resultSet.getDouble("stars"));
            }
        }  catch (Exception e) {
            throw e;
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            statement.close();
        }
        return userRatedBusinesses;
    }


    public static Map<String, Double[]> getListBusinessesFromUsers(List<String> users, Connection connect)
            throws SQLException{

        Map<String, Double[]> userRatedBusinesses = new HashMap<>();
        ResultSet resultSet = null;

        StringBuilder builder = new StringBuilder();

        for( int i = 0 ; i < users.size(); i++ ) {
            builder.append("?,");
        }

        String stmt = "SELECT business_id, avg(stars) AS stars, count(stars) AS confidence FROM rmatcher.review WHERE user_id IN ("
                + builder.deleteCharAt( builder.length() -1 ).toString() + ") GROUP BY business_id";

        PreparedStatement statement = connect
                .prepareStatement(stmt);

        try{
            int index = 1;
            for( String user : users ) {
                statement.setString(index++, user);
            }

            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                Double[] values =  {resultSet.getDouble("stars"), resultSet.getDouble("confidence")};
                userRatedBusinesses.put(resultSet.getString("business_id"), values);
            }
        }  catch (Exception e) {
            throw e;
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            statement.close();
        }
        return userRatedBusinesses;
    }
}