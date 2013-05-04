CREATE TABLE viewWithCount
SELECT DISTINCT user_id, COUNT(*) as userTotalReview FROM review
GROUP BY user_id;

CREATE TABLE viewSelection
SELECT user_id, userTotalReview, ceil(`userTotalReview`*.3) as onlyThirtyPercent FROM
viewWithCount
WHERE userTotalReview >= 1 AND userTotalReview <= 200
GROUP BY user_id
ORDER BY RAND()
LIMIT 15;

CREATE TABLE viewFullTestCase
SELECT VS.user_id, review_id, userTotalReview
FROM viewSelection AS VS, review AS R
WHERE VS.user_id = R.user_id
GROUP BY review_id
ORDER BY  VS.user_id ASC

CREATE TABLE viewTestCase(user_id VARCHAR(30) NOT NULL,
    review_count INT NOT NULL);
