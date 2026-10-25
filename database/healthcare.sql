-- create database javaproject_db;
USE javaproject_db;

-- show databases;
-- select* from appointments;
-- SHOW TABLES FROM javaproject_db;


-- CREATE TABLE users (
--     user_id INT AUTO_INCREMENT PRIMARY KEY,
--     username VARCHAR(50) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     role VARCHAR(20) NOT NULL
-- );

-- CREATE TABLE patients (
--     patient_id INT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     age INT,
--     contact VARCHAR(15)
-- );

-- CREATE TABLE doctors (
--     doctor_id INT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     specialization VARCHAR(100) NOT NULL
-- );

-- CREATE TABLE appointments (
--     appointment_id INT AUTO_INCREMENT PRIMARY KEY,
--     patient_id INT NOT NULL,
--     doctor_id INT NOT NULL,
--     appointment_date DATE NOT NULL,
--     appointment_time TIME NOT NULL,
--     status VARCHAR(30) DEFAULT 'Scheduled',

--     FOREIGN KEY (patient_id)
--         REFERENCES patients(patient_id)
--         ON DELETE CASCADE,

--     FOREIGN KEY (doctor_id)
--         REFERENCES doctors(doctor_id)
--         ON DELETE CASCADE
-- );

-- CREATE TABLE consultations (
--     consultation_id INT AUTO_INCREMENT PRIMARY KEY,
--     appointment_id INT NOT NULL,
--     diagnosis TEXT,
--     prescription TEXT,
--     notes TEXT,

--     FOREIGN KEY (appointment_id)
--         REFERENCES appointments(appointment_id)
--         ON DELETE CASCADE
-- );

-- SAMPLE USERS
INSERT INTO users (username, password, role)
VALUES
('admin', 'admin123', 'ADMIN'),
('doctor1', 'doctor123', 'DOCTOR'),
('receptionist1', 'recep123', 'RECEPTIONIST');

-- SAMPLE PATIENTS
INSERT INTO patients (name, age, contact)
VALUES
('shubham', 22, '8434343434'),
('sanjog', 25, '9872243211'),
('aayush', 30, '9982222212');

-- SAMPLE DOCTORS
INSERT INTO doctors (name, specialization)
VALUES
('Dr. Narendra', 'Cardiologist'),
('Dr. Jhatka', 'Dermatologist'),
('Dr. Strange', 'Neurosurgeon');

-- SAMPLE APPOINTMENTS

INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status)
VALUES
(1, 1, '2026-08-25', '10:00:00', 'Scheduled'),
(2, 2, '2026-08-25', '11:00:00', 'Scheduled'),
(3, 3, '2026-08-26', '12:00:00', 'Completed');

-- SAMPLE CONSULTATION

INSERT INTO consultations(appointment_id, diagnosis, prescription, notes)
VALUES
(1, 
 'High Blood Pressure', 
 'Amlodipine 5mg once daily', 
 'Monitor blood pressure regularly and reduce salt intake'),
(3, 'Fever and Viral Infection',
 'Paracetamol twice daily',
 'Take rest and drink plenty of water');
 
 
 