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
    private static Double averageRecall = 0.0;
    private static Double averagePrecision = 0.0;
    private static Double averageRMSD = 0.0;

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
                Double userAverage = (double)Math.ceil(average(userTrainingRatedBusinesses.values()));

                //return recommendations for a given user. Will return biz_id and rating by other user
                Map<String, Double[]> basicRecommendedBusinesses = MatcherUtils.getRecommendationsBasic(userTrainingRatedBusinesses, user, connect);
                stats(connect, userTestRatedBusinesses, userAverage, basicRecommendedBusinesses);

                //return recommendations for a given user. Will return biz_id and rating by other user
                //Map<String, Double[]> advancedRecommendedBusinesses = MatcherUtils.getRecommendationsAdvance(userTrainingRatedBusinesses, user, connect);
                //stats(connect, userTestRatedBusinesses, userAverage, advancedRecommendedBusinesses);
            }
            averageRecall = averageRecall/testUsers.size();
            averagePrecision = averagePrecision/testUsers.size();
            averageRMSD = averageRMSD/testUsers.size();

            System.out.println(averageRecall + " " + averagePrecision + " " + averageRMSD);

            //0.7757942575085289 0.005754022345089785 1.017802138884574
            //0.46454768108817845 0.008880095072428757 0.9825187006647647
            //0.7757942575085289 0.005754022345089785 1.017802138884574

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
        //System.out.println("Business\t   | uR  |  stars  | confidence ");
        int relevantRetrieved = 0;
        int relevant = 0;
        Double sumSquaredError = 0.0;

        for(String biz : userTestRatedBusinesses.keySet())
        {
            Double userRating = userTestRatedBusinesses.get(biz);
            Double[] values = recommendedBusinesses.get(biz);
            Double avgStars = values != null && values[1] > 1 ? values[0] : userAverage;
            Double confidence = values != null ? values[1] : 0;

            if(userRating > 3.9 && confidence > 0){
                relevantRetrieved++;
            }

            if(userRating > 3.9){
                relevant++;
            }

            Double error = userRating - avgStars;
            sumSquaredError += error*error;
            //System.out.println(Utils.getBusinessName(biz, connect) + " | " + userRating + " | " + avgStars + " | " + confidence );
        }

        int retrieved = 0;
        for(Double[] value : recommendedBusinesses.values()){
            if(value[0] > 3.9 && value[1] > 0){
                retrieved++;
            }
        }

        Double recall = (relevant != 0) ? (double)relevantRetrieved/relevant : 1;
        Double precision = (retrieved != 0) ? ((double)relevantRetrieved/retrieved) : 1;
        Double rootMeanSquaredDeviation = Math.sqrt(sumSquaredError / userTestRatedBusinesses.keySet().size());

        averageRecall += recall;
        averagePrecision += precision;
        averageRMSD += rootMeanSquaredDeviation;

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