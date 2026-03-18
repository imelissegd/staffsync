# StaffSync: An Employee Management System

A full-stack Employee Management System built with **Spring Boot** and **Gradle (Kotlin DSL)**, featuring a layered architecture with security, REST APIs, and a static frontend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Build Tool | Gradle (Kotlin DSL) |
| Security | Spring Security |
| Frontend | HTML, CSS, JavaScript (served as static files) |
| Database | MySQL |

---

## Project Structure

```
employee-mgmt-system/
├── src/
│   ├── main/
│   │   ├── java/com/mgd/employee_mgmt/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── exception/      # Custom exceptions & global handler
│   │   │   ├── model/          # Entity/domain classes
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── security/       # Security config
│   │   │   ├── service/        # Business logic
│   │   │   └── EmployeeManagementApp.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/        # Stylesheets
│   │       │   ├── js/         # JavaScript files
│   │       │   └── *.html      # Frontend pages
│   │       └── application.properties
│   └── test/                   # Test files
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

---

## Getting Started

### Prerequisites

- Java 17+ installed
- Git installed

> No need to install Gradle separately — the project uses the **Gradle Wrapper**.

### Clone the Repository

```bash
git clone https://github.com/your-username/employee-mgmt-system.git
cd employee-mgmt-system
```

### Run the Application

**Windows (CMD):**
```cmd
gradlew.bat bootRun
```

**Mac/Linux:**
```bash
./gradlew bootRun
```

The app will start at **http://localhost:8080**

### Build the Project

```cmd
gradlew.bat build
```

---

## Security

This project uses **Spring Security** for authentication and authorization. Configuration is located in:
```
src/main/java/com/mgd/employee_mgmt/security/
```

---

## Frontend

Static frontend files are served directly by Spring Boot from:
```
src/main/resources/static/
```
No separate frontend build step required.

---

## Dependencies

All dependencies are managed in:
```
build.gradle.kts
```

Run the following to download all dependencies:
```cmd
gradlew.bat dependencies
```

---

## 📄 License

This project is for educational purposes.