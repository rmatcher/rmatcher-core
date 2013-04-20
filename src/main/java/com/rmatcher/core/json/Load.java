package com.rmatcher.core.json;

import com.google.common.base.Joiner;

import java.util.Iterator;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/19/13
 * Time: 8:35 PM
 */

public class Load {

    public static void main(String [] args) throws Exception{

        String pathToFolder = "C:\\Users\\Ameen\\Desktop\\Yelp\\";
        //String pathToFolder = "/Users/santoki/yelp/yelp_phoenix_academic_dataset/";

        String businessFilePath = pathToFolder + "yelp_academic_dataset_business.json";
        String checkinFilePath = pathToFolder + "yelp_academic_dataset_checkin.json";
        String reviewFilePath = pathToFolder + "yelp_academic_dataset_review.json";
        String userFilePath = pathToFolder + "yelp_academic_dataset_user.json";

        Connection connect = null;
        ResultSet resultSet = null;

        JsonParser<Yelp_Business> businessJsonParser = new JsonParser(new Yelp_Business());
        businessJsonParser.createBuffer(businessFilePath);
        JsonParser<Yelp_Checkin> checkinJsonParser = new JsonParser(new Yelp_Checkin());
        checkinJsonParser.createBuffer(checkinFilePath);
        JsonParser<Yelp_Review> reviewJsonParser = new JsonParser(new Yelp_Review());
        reviewJsonParser.createBuffer(reviewFilePath);
        JsonParser<Yelp_User> userJsonParser = new JsonParser(new Yelp_User());
        userJsonParser.createBuffer(userFilePath);


        Iterator<Yelp_Business> businessIterator = businessJsonParser.iterator();
        Iterator<Yelp_Checkin> checkinIterator = checkinJsonParser.iterator();
        Iterator<Yelp_Review> reviewIterator = reviewJsonParser.iterator();
        Iterator<Yelp_User> userIterator = userJsonParser.iterator();


        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/rmatcher?"
                            + "user=root&password=123456");

            loadBusiness(connect, businessIterator);
            loadUser(connect, userIterator);

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

    private static void loadBusiness(Connection connect, Iterator<Yelp_Business> businessIterator) throws SQLException {
        PreparedStatement preparedStatement;
        while(businessIterator.hasNext()){
            Yelp_Business business = businessIterator.next();

            Statement statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("INSERT INTO rmatcher.business values (?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ? , ?)");

            preparedStatement.setString(1, business.get_business_id());
            preparedStatement.setString(2, business.get_name());
            preparedStatement.setString(3, business.get_full_address());
            preparedStatement.setString(4, business.get_city());
            preparedStatement.setString(5, business.get_state());
            preparedStatement.setDouble(6, business.get_latitude());
            preparedStatement.setDouble(7, business.get_longitude());
            preparedStatement.setDouble(8, business.get_stars());
            preparedStatement.setInt(9, business.get_review_count());
            preparedStatement.setInt(10, business.is_open() ? 1 : 0);
            preparedStatement.setString(11, Joiner.on(",").join(business.getNeighborhoods()));
            preparedStatement.setString(12, Joiner.on(",").join(business.get_categories()));

            preparedStatement.executeUpdate();
            statement.close();
        }
    }

    private static void loadUser(Connection connect, Iterator<Yelp_User> userIterator) throws SQLException {
        PreparedStatement preparedStatement;
        while(userIterator.hasNext()){
            Yelp_User user = userIterator.next();

            Statement statement = connect.createStatement();

            preparedStatement = connect
                    .prepareStatement("INSERT INTO rmatcher.user values (?, ?, ?, ?)");

            preparedStatement.setString(1, user.get_user_id());
            preparedStatement.setString(2, user.get_name());
            preparedStatement.setDouble(3, user.get_average_stars());
            preparedStatement.setString(4, user.get_votes().toString());

            preparedStatement.executeUpdate();
            statement.close();
        }
    }
}
