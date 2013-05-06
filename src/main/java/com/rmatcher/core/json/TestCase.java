package com.rmatcher.core.json;

/**
 * Created with IntelliJ IDEA.
 * User: santoki
 * Date: 5/4/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCase {

    private String user_id;
    private String business_id;
    private String review_id;
    private int totalReviewCount;

    public TestCase( String user_id, String business_id, String review_id, int totalReviewCount) {
        this.user_id = user_id;
        this.business_id = business_id;
        this.review_id = review_id;
        this.totalReviewCount = totalReviewCount;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBusiness_id() {
        return business_id;
    }

    public void setBusiness_id(String business_id) {
        this.business_id = business_id;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    public int getTotalCount() {
        return totalReviewCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalReviewCount = totalCount;
    }
}
