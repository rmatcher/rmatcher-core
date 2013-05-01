package com.rmatcher.core.json;

/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 4/18/13
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

public class Yelp_Business {
    @SerializedName("type")
    public String type;

    @SerializedName("business_id")
    public String business_id;

    @SerializedName("name")
    public String name;

    @SerializedName("full_address")
    public String full_address;

    @SerializedName("city")
    public String city;

    @SerializedName("state")
    public String state;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("stars")
    public double stars;

    @SerializedName("review_count")
    public int review_count;

    @SerializedName("open")
    public boolean open;

    @SerializedName("neighborhoods")
    public List<String> neighborhoods;

    @SerializedName("categories")
    public List<String> categories;


    // const
    public Yelp_Business() {
    }

    // Add basic
    public Yelp_Business(String business_id, Double stars) {
        this.business_id = Preconditions.checkNotNull(business_id);
        this.stars = Preconditions.checkNotNull(stars);
    }

    // get/set methods
    public String get_type() {
        return type;
    }

    public void set_type(String type) {
        this.type = type;
    }

    public String get_business_id() {
        return business_id;
    }

    public void set_business_id(String business_id) {
        this.business_id = business_id;
    }

    public String get_name() {
        return name;
    }

    public void set_name(String name) {
        this.name = name;
    }

    public String get_full_address() {
        return full_address;
    }

    public void set_full_address(String full_address) {
        this.full_address = full_address;
    }

    public String get_city() {
        return city;
    }

    public void set_city(String city) {
        this.city = city;
    }

    public String get_state() {
        return state;
    }

    public void set_state(String state) {
        this.state = state;
    }

    public double get_latitude() {
        return latitude;
    }

    public void set_latitude(double latitude) {
        this.latitude = latitude;
    }

    public double get_longitude() {
        return longitude;
    }

    public void set_longitude(double longitude) {
        this.longitude = longitude;
    }

    public double get_stars() {
        return stars;
    }

    public void set_stars(double stars) {
        this.stars = stars;
    }

    public int get_review_count() {
        return review_count;
    }

    public void set_review_count(int review_count) {
        this.review_count = review_count;
    }

    public boolean is_open() {
        return open;
    }

    public void set_open(boolean open) {
        this.open = open;
    }

    public List<String> get_categories() {
        return categories;
    }

    public void set_categories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getNeighborhoods() {
        return neighborhoods;
    }

    public void setNeighborhoods(List<String> neighborhoods) {
        this.neighborhoods = neighborhoods;
    }

    @Override
    public String toString() {
        return "type: " + type + "\n" + "business_id: " + business_id + "\n"
                + "name: " + name + "\n" + "neighborhoods: " + neighborhoods + "\n"
                + "full_address: " + full_address + "\n"
                + "city: " + city + "\n" + "state: " + state + "\n"
                + "latitude: " + latitude + "\n" + "longitude: " + longitude + "\n"
                + "stars: " + stars + "\n" + "review_count: " + review_count + "\n"
                + "categories: " + categories + "\n" + "open: " + open + "\n";
    }

}