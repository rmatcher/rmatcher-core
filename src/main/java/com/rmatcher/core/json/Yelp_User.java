package com.rmatcher.core.json;

/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 4/18/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
import com.google.gson.annotations.SerializedName;

public class Yelp_User {
    @SerializedName("type")
    public String type;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("name")
    public String name;

    @SerializedName("average_stars")
    public float average_stars;

    @SerializedName("votes")
    private Attr votes; // public Attr[] votes;

    // const
    public Yelp_User() {
    }

    // get/set methods
    public String get_type() {
        return type;
    }

    public void set_type(String type) {
        this.type = type;
    }

    public String get_user_id() {
        return user_id;
    }

    public void set_user_id(String user_id) {
        this.user_id = user_id;
    }

    public String get_name() {
        return name;
    }

    public void set_name(String name) {
        this.name = name;
    }

    public float get_average_stars() {
        return average_stars;
    }

    public void set_average_stars(float average_stars) {
        this.average_stars = average_stars;
    }

    public Attr get_votes() {
        return votes;
    }

    public void set_votes(Attr votes) {
        this.votes = votes;
    }

    @Override
    public String toString() {
        return "type: " + type + "\n" + "user_id: " + user_id + "\n"
                + "name: " + name + "\n" + "avg stars: " + average_stars + "\n"
                + "votes: " + votes + "\n";
    }

}