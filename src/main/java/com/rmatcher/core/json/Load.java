package com.rmatcher.core.json;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Ameen
 * Date: 4/19/13
 * Time: 8:35 PM
 */

public class Load {

    public static void main(String [] args){

        String pathToFolder = "C:\\Users\\Ameen\\Desktop\\Yelp\\";
        //String pathToFolder = "/Users/santoki/yelp/yelp_phoenix_academic_dataset/";

        String businessFilePath = pathToFolder + "yelp_academic_dataset_business.json";
        String checkinFilePath = pathToFolder + "yelp_academic_dataset_checkin.json";
        String reviewFilePath = pathToFolder + "yelp_academic_dataset_review.json";
        String userFilePath = pathToFolder + "yelp_academic_dataset_user.json";

        JsonParser<Yelp_Business> businessJsonParser = new JsonParser(new Yelp_Business());
        businessJsonParser.createBuffer(businessFilePath);
        JsonParser<Yelp_Business> checkinJsonParser = new JsonParser(new Yelp_Checkin());
        checkinJsonParser.createBuffer(checkinFilePath);
        JsonParser<Yelp_Business> reviewJsonParser = new JsonParser(new Yelp_Review());
        reviewJsonParser.createBuffer(reviewFilePath);
        JsonParser<Yelp_Business> userJsonParser = new JsonParser(new Yelp_User());
        userJsonParser.createBuffer(userFilePath);


        Iterator businessIterator = businessJsonParser.iterator();
        Iterator checkinIterator =checkinJsonParser.iterator();
        Iterator reviewIterator = reviewJsonParser.iterator();
        Iterator userIterator = userJsonParser.iterator();

        while(businessIterator.hasNext()){
            System.out.println(businessIterator.next());
        }

        while(checkinIterator.hasNext()){
            System.out.println(checkinIterator.next());
        }

        while(reviewIterator.hasNext()){
            System.out.println(reviewIterator.next());
        }

        while(userIterator.hasNext()){
            System.out.println(userIterator.next());
        }
    }
}
