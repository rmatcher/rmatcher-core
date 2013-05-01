package com.rmatcher.core.json;

/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 4/18/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
import com.google.gson.annotations.SerializedName;

public class Yelp_Review {
    @SerializedName("business_id")
    public String business_id;

    @SerializedName("type")
    public String type;

    @SerializedName("date")
    public String date;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("stars")
    public double stars;

    @SerializedName("text")
    private String text;

    @SerializedName("votes")
    private Attr votes; // public Attr[] votes;

    public Yelp_Review() {
    }

    public Yelp_Review(String user_id, Double stars) {
        this.user_id = user_id;
        this.stars = stars;
    }

    // get/set methods
    public String get_business_id() {
        return business_id;
    }

    public void set_business_id(String business_id) {
        this.business_id = business_id;
    }

    public String get_type() {
        return type;
    }

    public void set_type(String type) {
        this.type = type;
    }

    public String get_date() {
        return date;
    }

    public void get_date(String date) {
        this.date = date;
    }

    public String get_user_id() {
        return user_id;
    }

    public void get_user_id(String user_id) {
        this.user_id = user_id;
    }

    public double get_stars() {
        return stars;
    }

    public void set_stars(int stars) {
        this.stars = stars;
    }

    public Attr get_votes() {
        return votes;
    }

    public void set_votes(Attr votes) {
        this.votes = votes;
    }

    public String get_text() {
        return text;
    }

    public void set_text(String text) {
        this.text = text;
    }

    /*
     * public List<Attr> get_votes() { return votes; }
     *
     * public void set_votes(List<Attr> votes) { this.votes = votes; }
     */
    @Override
    public String toString() {
        return "business_id: " + business_id + "\n" + "type_id: " + type + "\n"
                + "date: " + date + "\n"
                + "user_id: " + user_id + "\n" + "stars: " + stars + "\n"
                + "votes: " + votes + "text: " + text +  "\n";
    }

}