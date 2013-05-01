package com.rmatcher.core.matcher;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/18/13
 * Time: 8:19 PM
 */

import com.rmatcher.core.json.Yelp_Business;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;

public class Matcher {

    public static void main(String [] args) throws Exception {

        Connection connect = null;
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?"
                            //+ "user=root&password=123456");
                            + "user=root&password=");   // problem with xampp
            connect.setAutoCommit(false);

            PreparedStatement statement = connect
                    .prepareStatement("SELECT business_id, stars FROM rmatcher.review WHERE user_id = ?");

            statement.setString(1, "UrgzxV2ohsEleWOWuAU75w");
            statement.execute();
            resultSet = statement.getResultSet();
            System.out.println("User: " + "UrgzxV2ohsEleWOWuAU75w");

            // Creating List of User reviewed Businesses
            ArrayList<Yelp_Business> userRatedBusinesses = new ArrayList<Yelp_Business>();

            while (resultSet.next()) {
                Yelp_Business yb = new Yelp_Business(resultSet.getString("business_id"), resultSet.getDouble("stars"));
                userRatedBusinesses.add(yb);
            }

            statement.close();


            statement = connect
                    .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ?");

            for (Yelp_Business rs : userRatedBusinesses) {
                statement.setString(1, rs.get_business_id());
                statement.execute();
                resultSet = statement.getResultSet();
                System.out.println("\nFor Business: " + rs.get_business_id());
                System.out.println("============================");
                while (resultSet.next()) {
                    System.out.println("\tU " + resultSet.getString("user_id")
                            + " " + resultSet.getDouble("stars"));
                }
            }


            statement.close();

        } catch (Exception e) {
            throw e;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

            if (connect != null) {
                connect.close();
            }
        }



    }


}