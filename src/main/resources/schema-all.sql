--DROP TABLE people;

CREATE TABLE people  (
--this value is needed to be unique and auto sequence value.
    person_id BIGINT  NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
