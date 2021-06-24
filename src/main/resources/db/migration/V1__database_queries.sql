CREATE SCHEMA IF NOT EXISTS template_slick;

CREATE TABLE IF NOT EXISTS template_slick.student (
student_id VARCHAR PRIMARY KEY,
email VARCHAR unique,
name VARCHAR,
date_of_birth TIMESTAMP,
marks json NULL,
address json NULL);
