package com.rmatcher.core.matcher;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/18/13
 * Time: 8:19 PM
 */

import com.rmatcher.core.json.Yelp_Business;
import com.rmatcher.core.json.Yelp_Review;

import java.sql.*;
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

            String user = "UrgzxV2ohsEleWOWuAU75w";
            // Creating List of User reviewed Businesses
            ArrayList<Yelp_Business> userRatedBusinesses = getListOfBusinessesFromUser(user, statement, resultSet, connect);

            statement = connect
                    .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ?");

            ArrayList<Yelp_Review> uRB = getBusinessReview(userRatedBusinesses, statement, resultSet, connect);

            //Print all the users who have also rated same as selected user for each business
            System.out.println("User " + user + " has rated the following businesses:" );
            for (Yelp_Business yb : userRatedBusinesses) {
                System.out.println("B: " + yb.get_business_id() + " stars:" + yb.get_stars());
            }
            System.out.println("==============================" );
            System.out.println("The following users have made these reviews for the businesses rated by " + user + " :" );
            for (Yelp_Review k : uRB) {
                System.out.println("User: " + k.get_user_id() + " stars:" + k.get_stars());
            }

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

    public static ArrayList<Yelp_Business> getListOfBusinessesFromUser(String user_id,
                                                                       PreparedStatement statement,
                                                                       ResultSet resultSet,
                                                                       Connection connect) throws SQLException{

        ArrayList<Yelp_Business> userRatedBusinesses = new ArrayList<Yelp_Business>();
        try{
            statement.setString(1, user_id);
            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                Yelp_Business yb = new Yelp_Business(resultSet.getString("business_id"), resultSet.getDouble("stars"));
                userRatedBusinesses.add(yb);
            }
        }  catch (Exception e) {
            throw e;
        }

        statement.close();
        return userRatedBusinesses;
    }

    public static ArrayList<Yelp_Review> getBusinessReview(ArrayList<Yelp_Business> yr,
                                                           PreparedStatement statement,
                                                           ResultSet resultSet,
                                                           Connection connect) throws SQLException{

        ArrayList<Yelp_Review> businessesReviewed = new ArrayList<Yelp_Review>();

        for (Yelp_Business yb : yr) {
            statement.setString(1, yb.get_business_id());
            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                Yelp_Review yreview = new Yelp_Review(resultSet.getString("user_id"), resultSet.getDouble("stars"));
                businessesReviewed.add(yreview);
            }
        }

        statement.close();

        return businessesReviewed;
    }


}