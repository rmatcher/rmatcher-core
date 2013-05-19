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

import java.util.*;

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
            Map<String, Double[]> recommendedBusinesses = MatcherUtils.getRecommendations(user, connect);

            //System.out.println("////////////Recommended Businesses////////////////");
            //for(String biz : recommendedBusinesses.keySet())
            //{
            //    System.out.println(biz + " " + recommendedBusinesses.get(biz) );
            //}

            System.out.println("////////////Comparison of results////////////////");
            System.out.println("\t\tBusiness\t   | uR  |  stars     confidence ");
            Map<String, Double> userTestRatedBusinesses = Utils.getListOfBusinessesFromUser(false, user, connect);
            for(String biz : userTestRatedBusinesses.keySet())
            {
                Double[] values = recommendedBusinesses.get(biz);
                Double stars = values != null ? values[0] : null;
                Double confidence = values != null ? values[1] : null;
                System.out.println(Utils.getBusinessName(biz, connect) + " | " + userTestRatedBusinesses.get(biz) + " | " + stars + "  " + confidence );
            }

            // Get Businesses' by Categories
            Map<String, Collection<String>> categoryList = Utils.groupBusinessesByCategories(connect);
            System.out.println("************************* GROUP business by Categories *************************");
            printMap(categoryList);


        } catch (Exception e) {
            throw e;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }


    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove();
        }
    }
}