package com.rmatcher.core.matcher;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/18/13
 * Time: 8:19 PM
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import com.rmatcher.core.json.Yelp_Business;
import com.rmatcher.core.json.Yelp_Review;

public class Matcher {

    public static void main(String [] args) throws Exception {

        Connection connect = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?user=root&password=");
            connect.setAutoCommit(false);

            String user = "UrgzxV2ohsEleWOWuAU75w";
            // Creating List of User reviewed Businesses
            List<Yelp_Business> userRatedBusinesses = getListOfBusinessesFromUser(user, connect);
            List<Yelp_Review> reviews = getReviewsForBusinesses(userRatedBusinesses, connect);

            //Print all the users who have also rated same as selected user for each business
            System.out.println("User " + user + " has rated the following businesses:");
            for (Yelp_Business yb : userRatedBusinesses) {
                System.out.println("B: " + yb.get_business_id() + " stars:" + yb.get_stars());
            }

            System.out.println("The following users have made these reviews for the businesses rated by " + user + " :" );
            for (Yelp_Review k : reviews) {
                System.out.println("User: " + k.get_user_id() + " stars:" + k.get_stars());
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    public static List<Yelp_Business> getListOfBusinessesFromUser(String user_id, Connection connect)
            throws SQLException{

        List<Yelp_Business> userRatedBusinesses = new ArrayList<Yelp_Business>();
        ResultSet resultSet = null;
        PreparedStatement statement = connect
                .prepareStatement("SELECT business_id, stars FROM rmatcher.review WHERE user_id = ?");

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
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            statement.close();
        }
        return userRatedBusinesses;
    }

    public static List<Yelp_Review> getReviewsForBusinesses(List<Yelp_Business> yr, Connection connect)
            throws SQLException{

        List<Yelp_Review> businessesReviewed = new ArrayList<Yelp_Review>();
        ResultSet resultSet = null;
        PreparedStatement statement = connect
                .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ?");

        try{
            for (Yelp_Business yb : yr) {
                statement.setString(1, yb.get_business_id());
                statement.execute();
                resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    Yelp_Review yreview = new Yelp_Review(resultSet.getString("user_id"), resultSet.getDouble("stars"));
                    businessesReviewed.add(yreview);
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

        return businessesReviewed;
    }


}