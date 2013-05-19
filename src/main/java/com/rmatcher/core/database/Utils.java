package com.rmatcher.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ameen
 * Date: 5/18/13
 * Time: 5:55 PM
 */
public final class Utils {
    private  Utils(){}

    public static String getBusinessName(String biz_id, Connection connect)
            throws SQLException {

        String stmt;
        stmt = "SELECT name FROM rmatcher.business WHERE business_id = ?";

        PreparedStatement statement = connect
                .prepareStatement(stmt);

        ResultSet resultSet = null;
        String biz;
        try{
            statement.setString(1, biz_id);
            statement.execute();
            resultSet = statement.getResultSet();
            resultSet.last();
            biz = resultSet.getString("name");
        }  catch (Exception e) {
            throw e;
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
            statement.close();
        }

        return biz;
    }

    public static Map<String, Collection<String>> groupBusinessesByCategories(Connection connect) throws SQLException{

        Map<String, Collection<String>> categoryList = new HashMap<String, Collection<String>>();
        ResultSet resultSet = null;
        PreparedStatement statement = connect
                .prepareStatement("SELECT business_id, categories FROM rmatcher.business");

        try{
            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {

                String[] categorylist = resultSet.getString("categories").split("\\s*,\\s*");

                for (String k : categorylist)
                {
                    if (categoryList.containsKey(k)){
                        categoryList.get(k).add(resultSet.getString("business_id"));
                    }
                    else
                    {
                        List<String> bizList = new ArrayList<String>();
                        bizList.add(resultSet.getString("business_id"));
                        categoryList.put(k,bizList);
                    }
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
        return categoryList;
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


    public static List<String> getTestUsers(Connection connect)
            throws SQLException{

        List<String> users = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement statement = connect
                .prepareStatement("SELECT user_id FROM rmatcher.viewSelection ORDER BY userTotalReview");

        try{
            statement.execute();
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                String user_id = resultSet.getString("user_id");

                users.add(user_id);
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

        return users;
    }
}
