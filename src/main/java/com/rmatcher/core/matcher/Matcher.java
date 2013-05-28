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

            // Get Businesses' by Categories
            Map<String, Collection<String>> categoryBizMap = new HashMap<>();
            Map<String, Collection<String>> bizCategoryMap = new HashMap<>();
            Utils.groupBusinessesByCategories(connect, categoryBizMap, bizCategoryMap);

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
                Set<String> userVisitedCategories = new HashSet<>();
                for(String biz : userTrainingRatedBusinesses.keySet()){
                    userVisitedCategories.addAll(bizCategoryMap.get(biz));
                }
                Set<String> categoryFilteredBiz = new HashSet<>();
                for(String category : userVisitedCategories){
                    categoryFilteredBiz.addAll(categoryBizMap.get(category));
                }

                //Map<String, Double[]> advancedRecommendedBusinesses = MatcherUtils.getRecommendationsAdvance(userTrainingRatedBusinesses, user, categoryFilteredBiz, connect);
                //stats(connect, userTestRatedBusinesses, userAverage, advancedRecommendedBusinesses);
            }
            averageRecall = averageRecall/testUsers.size();
            averagePrecision = averagePrecision/testUsers.size();
            averageRMSD = averageRMSD/testUsers.size();

            System.out.println(averageRecall + " " + averagePrecision + " " + averageRMSD);

            //0.6508191755376845 0.004319272658871976 1.1829490508155156
            //0.8361187391558982 0.0031479623235842412 1.1290800901769424

            //0.7848945154759107 0.0053443942569722505 1.0778074942882392
            //0.8309262615076567 0.006240419585548509 1.0484008163799625


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

            if(userRating > 3.9 && confidence > 1){
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
            if(value[0] > 3.9 && value[1] > 1){
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