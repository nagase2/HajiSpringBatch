--DROP TABLE people;
DROP TABLE  IF EXISTS people;
DROP SEQUENCE  IF EXISTS people_seq;

CREATE SEQUENCE people_seq START 1;
CREATE TABLE people  (
--this value is needed to be unique and auto sequence value.
    person_id BIGINT  NOT NULL PRIMARY KEY default nextval('people_seq'),
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
