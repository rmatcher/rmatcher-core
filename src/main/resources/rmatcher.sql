CREATE DATABASE rmatcher;
use rmatcher;

CREATE TABLE business (business_id VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    full_address VARCHAR(500),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    stars DOUBLE NOT NULL,
    review_count INT NOT NULL,
    open INT NOT NULL,
    neighborhoods VARCHAR(500) NOT NULL,
    categories VARCHAR(500) NOT NULL,
    PRIMARY KEY (business_id));

CREATE TABLE user (user_id VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    review_count INT NOT NULL,
    average_stars DOUBLE NOT NULL,
    votes VARCHAR(500) NOT NULL,
    PRIMARY KEY (user_id));

CREATE TABLE review (review_id VARCHAR(30) NOT NULL,
    business_id VARCHAR(30) NOT NULL,
    user_id VARCHAR(30) NOT NULL,
    stars DOUBLE NOT NULL,
    text TEXT NOT NULL,
    date VARCHAR(12) NOT NULL,
    votes VARCHAR(500) NOT NULL,
    vote_count int NOT NULL,
    sentiment_score DOUBLE NOT NULL,
    FOREIGN KEY (business_id) REFERENCES business (business_id));

CREATE TABLE checkin (business_id VARCHAR(30) NOT NULL,
    checkins VARCHAR(2000) NOT NULL,
    FOREIGN KEY (business_id) REFERENCES business (business_id));

--normalize the sentiment_score. Replate 16.633 with the max(abs(score))
UPDATE review SET sentiment_score = sentiment_score/16.633757700453067

--increment vote_count to avoid zeros
UPDATE review SET vote_count = vote_count+1

--DELETE 14028 reviews that we don't have the author user
DELETE FROM `review` WHERE user_id NOT IN (SELECT user_id FROM user)

ALTER TABLE review ADD FOREIGN KEY (user_Id) REFERENCES user(user_Id)