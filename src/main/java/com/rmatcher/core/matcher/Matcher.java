package com.rmatcher.core.matcher;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/18/13
 * Time: 8:19 PM
 */

import com.rmatcher.core.database.Utils;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;
import java.util.*;

public class Matcher {

    public static void main(String [] args) throws Exception {

        Connection connect = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?user=root&password=");
            connect.setAutoCommit(false);

            List<String> testUsers = Utils.getTestUsers(connect);

            for(String user : testUsers){
                //get biz the user rated excluding the testing reviews
                Map<String, Double> userTrainingRatedBusinesses = Utils.getListOfBusinessesFromUser(true, user, connect);
                Map<String, Double> userTestRatedBusinesses = Utils.getListOfBusinessesFromUser(false, user, connect);
                Double userAverage = average(userTrainingRatedBusinesses.values());

                //return recommendations for a given user. Will return biz_id and rating by other user
                Map<String, Double[]> basicRecommendedBusinesses = MatcherUtils.getRecommendationsBasic(userTrainingRatedBusinesses, user, connect);
                stats(connect, userTestRatedBusinesses, userAverage, basicRecommendedBusinesses);

                //return recommendations for a given user. Will return biz_id and rating by other user
                Map<String, Double[]> advancedRecommendedBusinesses = MatcherUtils.getRecommendationsAdvance(userTrainingRatedBusinesses, user, connect);
                stats(connect, userTestRatedBusinesses, userAverage, advancedRecommendedBusinesses);
            }

            // Get Businesses' by Categories
            //Map<String, Collection<String>> categoryList = Utils.groupBusinessesByCategories(connect);
            //System.out.println("************************* GROUP business by Categories *************************");
            //printMap(categoryList);


        } catch (Exception e) {
            throw e;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    private static void stats(Connection connect, Map<String, Double> userTestRatedBusinesses, Double userAverage, Map<String, Double[]> recommendedBusinesses) throws SQLException {
        System.out.println("Business\t   | uR  |  stars  | confidence ");
        Double relevantRetrieved = 0.0;
        Double sumSquaredError = 0.0;

        for(String biz : userTestRatedBusinesses.keySet())
        {
            Double userRating = userTestRatedBusinesses.get(biz);
            Double[] values = recommendedBusinesses.get(biz);
            Double avgStars = values != null ? values[0] : userAverage;
            Double confidence = values != null ? values[1] : 0;

            if(confidence > 1){
                relevantRetrieved++;
            }

            Double error = userRating - avgStars;
            sumSquaredError = error*error;
            System.out.println(Utils.getBusinessName(biz, connect) + " | " + userRating + " | " + avgStars + " | " + confidence );
        }

        int retrieved = 0;
        for(Double[] value : recommendedBusinesses.values()){
            if(value[0] > 3.9 && value[1] > 1){
                retrieved++;
            }
        }

        Double recall = (relevantRetrieved/userTestRatedBusinesses.keySet().size());
        Double precision = (relevantRetrieved/retrieved);
        Double rootMeanSquaredDeviation = Math.sqrt(sumSquaredError / userTestRatedBusinesses.keySet().size());
        System.out.println("TotalTestedReviews: " + userTestRatedBusinesses.keySet().size() +  " Recall: " + recall + " Precision: " + precision + " RMSD: " + rootMeanSquaredDeviation);
    }


    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove();
        }
    }

    public static Double average(Collection<Double> values){
        Double sum = 0.0;
        int size = 0;

        for(Double value : values){
            sum += value;
            size++;
        }
        return sum/size;
    }

}