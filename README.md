# 💰 Loan Management System

## 📌 Overview
The **Loan Management System** is a Spring Boot-based web application that enables users to apply for loans, allows officers to review and approve them, and administrators to manage the entire system. It integrates robust authentication using **JWT** and **Spring Security**, and supports reporting with **JasperReports**. The application maintains high code quality using **JUnit**, **JaCoCo**, and **SonarLint**, and uses **PostgreSQL** as its relational database.

---

## 🔧 Technology


| Technology         | Description                                       |
|--------------------|---------------------------------------------------|
| Java 18            | Core programming language                         |
| Spring Boot        | Backend framework for building REST APIs          |
| Spring Security + JWT | Secure authentication and authorization        |
| Spring Data JPA    | ORM tool to interact with PostgreSQL              |
| PostgreSQL         | Relational database                               |
| JasperReports      | PDF report generation tool                        |
| JUnit 5            | Unit testing framework                            |
| JaCoCo             | Code coverage tool                                |
| Docker             | Containerization of app and database              |
| Docker Compose     | Multi-container orchestration                     |
| HTML, Bootstrap    | Frontend view layer                               |
| JavaScript         | UI interactions and validations                   |
| GitHub Actions     | CI/CD workflows for automated builds              |

---

## 🧩 Modules & Features

### 🧑‍💼 User Module
- User registration and login
- JWT-based authentication
- Role-based access (`user`, `officer`, `admin`)

---

### 💳 Loan Module
- Users can apply for a loan
- Loans are reviewed by officers
- EMIs are calculated automatically

---

### 🧾 Loan Approval Request
Used when officer makes a decision on a loan.

---

### 👮 Officer Module
- Officer can view and approve/reject loan requests

---

### 📄 Report Module
- JasperReports integration to generate:
  - Loan Summary Reports
  - Approval Status Reports
  - Repayment Reports (optional)

---

## 🧬 ER Diagram

```text
+------------------------+
|        User           |
+------------------------+
| id (PK)               |
| name                  |
| username              |
| email                 |
| password              |
| pancard               |
| aadhacard             |
| role                  |
| phoneno               |
+------------------------+
         |
         | 1
         |
         | <——
         |        n
+------------------------+
|        Loan            |
+------------------------+
| id (PK)                |
| user_id (FK)           |
| assigned_officer_id(FK)|
| address                |
| amount                 |
| monthlyIncome          |
| otherExpenses          |
| tenure                 |
| emi                    |
| loanApprovalScore      |
| status                 |
| previousStatus         |
| remarks                |
+------------------------+
         |
         | n
         |
         |——>
         | 1
+------------------------+
|       Officer          |
+------------------------+
| id (PK)                |
| name                   |
| username               |
| email                  |
| password               |
| phoneno                |
| role = 'officer'       |
+------------------------+

         |
         | (used by)
         |
         |——>
         |
+-------------------------------+
|   LoanApprovalRequest (DTO)  |
+-------------------------------+
| status                        |
| emi                           |
| remarks                       |
+-------------------------------+
```
---

## 📂 Project Structure
```text
LoanManagementSystem/
├── src/
│ ├── main/
│ │ ├── java/com/example/loan/
│ │ │ ├── controller/
│ │ │ ├── config/ (JWT config)
│ │ │ ├── model/
│ │ │ ├── repository/
│ │ │ ├── service/
│ │ │ └── util/ (Token utils, report helpers)
│ │ └── resources/
│ │ ├── static/ (CSS, JS)
│ │ ├── templates/ (HTML views)
│ │ ├── reports/ (JasperReports templates)
│ │ └── application.properties
│ └── test/
│ └── java/com/example/loan/
│ └── service/ (JUnit tests)
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```
---

## 🚀 How to Run

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/loan-management-system.git
   cd loan-management-system
    ```
2. **Build the JAR**
   ```bash
   mvn clean install
   ```
3. **Run with Docker Compose**
  ```bash
  docker-compose up --build
  ```
5. **Access**
   
     - App: http://localhost:8083
     - PostgreSQL: localhost:5433

---

## 🧪 How to Test

- **Run unit tests:**
  ```bash
  mvn test
  ```
- **View code coverage report (after test):
  ```bash
  target/site/jacoco/index.html
  ```
  
---

## 🐳 Docker Setup

1. **Dockerfile**

   ```Dockerfile
    FROM openjdk:18
    WORKDIR /app
    COPY target/*.jar loan.jar
    EXPOSE 8083
    ENTRYPOINT ["java", "-jar", "loan.jar"]
   ```

2. **docker-compose.yml**
   ```bash
   services:
    postgres:
      image: postgres:14
      container_name: loan_postgres
      environment:
        POSTGRES_DB: loans
        POSTGRES_USER: postgres
        POSTGRES_PASSWORD: password123
      ports:
        - "5433:5432"
      networks:
        - loan-network
  
    springboot-app:
      build: .
      container_name: loan_springboot
      depends_on:
        - postgres
      ports:
        - "8083:8083"
      environment:
        SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/loans
        SPRING_DATASOURCE_USERNAME: postgres
        SPRING_DATASOURCE_PASSWORD: password123
        SPRING_JPA_HIBERNATE_DDL_AUTO: update
      networks:
        - loan-network
  
    networks:
      loan-network:
   ```
3. **Build and Run Docker:**
   ```bash
   docker-compose up --build
   ```
---

