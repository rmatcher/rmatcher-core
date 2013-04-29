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

public class Matcher {
    public static void main(String [] args) throws Exception {

        Connection connect = null;
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?"
                            + "user=root&password=123456");
            connect.setAutoCommit(false);


            PreparedStatement statement = connect
                    .prepareStatement("SELECT business_id, stars FROM rmatcher.review WHERE user_id = ?");

            statement.setString(1, "__mbhFpCj377OxiJJozXRQ");
            statement.execute();
            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                System.out.println("B "+resultSet.getString("business_id")
                        + " " + resultSet.getDouble("stars"));
            }

            statement.close();


            statement = connect
                    .prepareStatement("SELECT user_id, stars FROM rmatcher.review WHERE business_id = ?");

            statement.setString(1, "k76odRRsXPErPzB0gjn-3g");
            statement.execute();
            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                System.out.println("U " + resultSet.getString("user_id")
                        + " " + resultSet.getDouble("stars"));
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