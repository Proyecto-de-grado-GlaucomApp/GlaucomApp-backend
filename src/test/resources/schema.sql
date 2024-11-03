CREATE TABLE ophthal_user (
    ophtal_id UUID  PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    role VARCHAR(255)
);
