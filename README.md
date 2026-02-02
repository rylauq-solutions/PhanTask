# 📋 PhanTask - Organizational Management System

<div align="center">

**A full-stack web application for streamlined organizational task management, attendance tracking, and team collaboration.**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-technology-stack) • [Installation](#-installation) • [API Documentation](#-api-documentation)

</div>

---

## 🎯 Overview

PhanTask is a comprehensive organizational management system designed for educational institutions, training centers, and small-to-medium organizations. It digitizes and automates day-to-day administrative workflows through a centralized, secure, and user-friendly platform.

### Key Highlights

- **Role-based Access Control** with multiple user hierarchies (ADMIN, HR, MANAGER, SUPPORT, USER)
- **JWT Authentication** with automatic token refresh and stateless session management
- **QR-based Attendance** tracking with token validation and automated reporting
- **Task Management** with role/individual assignment and deadline tracking
- **Real-time Notice Board** with priority-based announcements
- **Feedback System** with customizable templates and analytics
- **Helpline Ticketing** for organized issue resolution
- **Comprehensive Testing** with 303 automated tests (100% pass rate)

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based stateless authentication with automatic token refresh
- Role-based access control (ADMIN, HR, MANAGER, SUPPORT, USER)
- Secure password management with BCrypt hashing
- Mandatory first-login onboarding with profile completion
- Session management with token expiration handling

### 📝 Task Management
- Create, assign, and track tasks with deadlines
- Role-based or individual task assignment
- Task status tracking (Pending, Submitted, Overdue)
- Google Drive integration for submissions
- Real-time task dashboard with filtering capabilities
- Automatic submission timestamp capture

### 📅 Attendance System
- **QR-based attendance** marking with token validation
- Check-in/Check-out functionality
- Automated attendance percentage calculation
- CSV export for attendance reports and timesheets
- Token expiry management (5-minute validity)
- Duplicate attendance prevention
- Admin/HR attendance oversight

### 📢 Notice Board
- Role-targeted announcements
- Priority-based notices (High, Medium, Low)
- Real-time notice updates
- Admin-controlled publishing and management
- Notice filtering by priority and role
- Dashboard integration for recent notices

### 💬 Feedback System
- Customizable feedback templates with multiple questions
- Star-rating based responses (1-5 scale)
- Aggregated feedback analytics and reports
- Role-specific feedback forms
- Submission tracking and history
- Admin dashboard for feedback insights

### 🆘 Helpline Ticketing
- Priority-based ticket management (High, Medium, Low)
- Role-specific ticket assignment (HR, MANAGER, SUPPORT, ADMIN)
- Ticket status tracking (Pending, Resolved)
- Automatic due date calculation based on priority
- Ticket history and resolution tracking
- Email notifications for ticket creators

### 👥 User Management
- Create and manage user accounts with email notifications
- Activate/deactivate users with timestamp tracking
- Natural sorting for usernames (user1, user2, user10)
- Onboarding status tracking (Completed/Pending)
- Auto-generated temporary passwords
- Separate views for active and inactive users
- Admin profile editing capabilities

---

## 🏗️ Architecture

PhanTask follows a modern **three-tier client-server architecture**:

```
┌─────────────────────────────────────────────────┐
│         Presentation Layer (Frontend)           │
│   React.js -  Material-UI -  Axios -  Vite      │
└───────────────────┬─────────────────────────────┘
                    │ REST API (JSON)
┌───────────────────▼─────────────────────────────┐
│      Business Logic Layer (Backend)             │
│   Spring Boot -  Spring Security -  JWT         │
│   Spring Data JPA -  Spring Web                 │
└───────────────────┬─────────────────────────────┘
                    │ JDBC
┌───────────────────▼─────────────────────────────┐
│          Data Access Layer                      │
│             MySQL Database                      │
└─────────────────────────────────────────────────┘
```

### Design Principles

- **Layered Architecture**: Clear separation between presentation, business logic, and data access
- **RESTful API Design**: Stateless, resource-oriented endpoints
- **Component-Based Frontend**: Reusable React components with Material-UI
- **Database Normalization**: Third Normal Form (3NF) for data integrity
- **Stateless Authentication**: JWT tokens for scalable session management

---

## 🛠️ Technology Stack

### Backend
- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Security:** Spring Security + JWT (JSON Web Tokens)
- **Database:** MySQL 8.0+
- **ORM:** Spring Data JPA (Hibernate)
- **Build Tool:** Maven
- **Email:** Spring JavaMailSender (SMTP)

### Frontend
- **Library:** React 18.x
- **Styling:** TailwindCSS + Material-UI (MUI)
- **HTTP Client:** Axios
- **Build Tool:** Vite
- **Icons:** Font Awesome + React Icons
- **State Management:** React Context API

### Testing
- **Unit Testing:** JUnit 5
- **Mocking:** Mockito
- **Integration Testing:** Spring MockMvc
- **API Testing:** Postman
- **Test Coverage:** 303 automated tests (100% pass rate)

### Development Tools
- **Version Control:** Git & GitHub
- **Backend IDE:** IntelliJ IDEA / Eclipse / STS
- **Frontend Editor:** Visual Studio Code
- **API Client:** Postman
- **Package Manager:** Maven (Backend), npm (Frontend)

---

## 📦 Installation

### Prerequisites

Ensure you have the following installed:
- **Java JDK 17+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 18+** and **npm** - [Download](https://nodejs.org/)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Git** - [Download](https://git-scm.com/)

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
https://github.com/rylauq-solutions/PhanTask.git
cd PhanTask
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE phantask_db;
```

### 3. Backend Configuration

Navigate to backend and create `application-secrets.properties` in `src/main/resources/`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/phantask_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Secret (Change this in production!)
jwt.secret=YourSecureSecretKeyMinimum256BitsForHS256Algorithm

# Email Configuration (Optional - for user account creation emails)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### 📧 Email Configuration Guide

To enable email notifications for user account creation:

1. **For Gmail:**
   - Go to your Google Account settings
   - Enable 2-Step Verification
   - Generate an App Password: [Google App Passwords](https://myaccount.google.com/apppasswords)
   - Use the generated 16-character password in `spring.mail.password`

2. **Example Gmail Configuration:**
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=yourname@gmail.com
   spring.mail.password=abcd efgh ijkl mnop  # 16-character app password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. **For Other Email Providers:**
   - **Outlook/Hotmail:**
     ```properties
     spring.mail.host=smtp-mail.outlook.com
     spring.mail.port=587
     ```
   - **Yahoo:**
     ```properties
     spring.mail.host=smtp.mail.yahoo.com
     spring.mail.port=587
     ```

4. **Skip Email Configuration (Development):**
   - If you don't configure email, the application will still work
   - User creation will succeed, but no email will be sent
   - Check console logs for temporary passwords

### 4. Run Spring Boot Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Backend will run on:** `http://localhost:8080`

On first run, the application automatically creates:
- Database structure based on JPA entities
- Default Admin user with credentials:
  - **Email:** `admin@phantask.in`
  - **Password:** `Temp@123`

⚠️ **Important:** Change the default admin password after first login!

### 5. Run React Frontend

Open a new terminal:

```bash
cd frontend
npm install
npm run dev
```

**Frontend will run on:** `http://localhost:5173`

### 6. Access the Application

Open your browser and navigate to:
- **Frontend:** `http://localhost:5173`
- **Backend API:** `http://localhost:8080/api`

**Login with default admin credentials:**
- Email: `admin@phantask.in`
- Password: `Temp@123`

---

## 📱 Mobile Access (Local Network)

To access PhanTask from mobile devices on your local network:

### 1. Update CORS Configuration

Edit `backend/src/main/java/com/phantask/authentication/security/CorsConfig.java`:

```java
.allowedOriginPatterns(
    "http://localhost:*",
    "http://192.168.*.*:*",  // Allow local network access
    "https://phantask.vercel.app"
)
```

### 2. Restart Spring Boot Backend

```bash
mvn spring-boot:run
```

### 3. Run Frontend with Host Flag

```bash
npm run dev -- --host
```

This will display output like:

```
  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.1.xxx:5173/
```

### 4. Access from Mobile

On your mobile device (connected to the same WiFi):
- Open browser and navigate to the **Network URL**: `http://192.168.1.xxx:5173/`
- Login with admin credentials

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/auth/login` | User login | ❌ |
| `POST` | `/auth/refresh-token` | Refresh access token | ❌ |
| `GET` | `/auth/current-profile` | Get current user profile | ✅ |

### User Management (Admin Only)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/users/create-account` | Create new user | ✅ ADMIN |
| `GET` | `/users/active` | Get all active users | ✅ ADMIN |
| `GET` | `/users/inactive` | Get all inactive users | ✅ ADMIN |
| `PUT` | `/users/{userId}/edit` | Edit user profile | ✅ ADMIN |
| `PUT` | `/users/{userId}/deactivate` | Deactivate user | ✅ ADMIN |
| `PUT` | `/users/{userId}/reactivate` | Reactivate user | ✅ ADMIN |
| `GET` | `/users/profile` | Get user profile | ✅ |
| `POST` | `/users/update-profile` | Update user profile | ✅ |
| `POST` | `/users/change-password` | Change password | ✅ |

### Task Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/tasks/my` | Get user's assigned tasks | ✅ |
| `POST` | `/tasks/admin/create` | Create new task | ✅ ADMIN |
| `PUT` | `/tasks/admin/{id}` | Update task | ✅ ADMIN |
| `DELETE` | `/tasks/admin/{id}` | Delete task | ✅ ADMIN |
| `PUT` | `/tasks/my/submit/{id}` | Submit task | ✅ |
| `GET` | `/tasks/admin/all` | Get all tasks | ✅ ADMIN |

### Attendance Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/attendance/token/register` | Register attendance QR token | ✅ |
| `POST` | `/attendance/mark` | Mark attendance with token | ✅ ADMIN/HR |
| `GET` | `/attendance/my` | Get user's attendance records | ✅ |
| `GET` | `/attendance/percentage/my` | Get attendance percentage | ✅ |
| `GET` | `/attendance/download` | Download attendance CSV | ✅ ADMIN/HR |

### Notice Board

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/notices/my` | Get role-based notices | ✅ |
| `POST` | `/notices/create` | Create notice | ✅ ADMIN |
| `PUT` | `/notices/{id}` | Update notice | ✅ ADMIN |
| `DELETE` | `/notices/{id}` | Delete notice | ✅ ADMIN |
| `GET` | `/notices/all` | Get all notices | ✅ ADMIN |

### Feedback System

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/feedback/user/available` | Get available feedback forms | ✅ |
| `POST` | `/feedback/submit` | Submit feedback | ✅ |
| `POST` | `/feedback/create` | Create feedback template | ✅ ADMIN |
| `GET` | `/feedback/reports` | View feedback reports | ✅ ADMIN |
| `GET` | `/feedback/templates` | Get all templates | ✅ ADMIN |
| `DELETE` | `/feedback/{id}` | Delete template | ✅ ADMIN |

### Helpline Ticketing

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/helpline/raise` | Raise new ticket | ✅ |
| `GET` | `/helpline/my` | Get user's raised tickets | ✅ |
| `GET` | `/helpline/assigned` | Get assigned tickets | ✅ HR/MANAGER/SUPPORT |
| `PUT` | `/helpline/{id}/resolve` | Resolve ticket | ✅ HR/MANAGER/SUPPORT |

### Role Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/roles/add` | Add new role | ✅ ADMIN |
| `GET` | `/roles/all` | Get all roles | ✅ |

---

## 🔐 Security Features

- **JWT Authentication**: Stateless, secure token-based authentication
- **Password Hashing**: BCrypt algorithm for password storage (never stored in plain text)
- **Role-Based Authorization**: Fine-grained access control with `@PreAuthorize` annotations
- **CORS Configuration**: Controlled cross-origin resource sharing
- **SQL Injection Prevention**: Parameterized queries via JPA/Hibernate
- **Automatic Token Refresh**: Seamless session continuity within 2 minutes of expiry
- **Input Validation**: Frontend and backend validation for all user inputs
- **Account Lifecycle Management**: User activation/deactivation with timestamp tracking
- **Session Expiration Handling**: Automatic logout on token expiration

---

## 🧪 Testing

### Test Coverage

**Total Tests:** 303 ✅ | **Passed:** 303 | **Failed:** 0 | **Success Rate:** 100%

| Module | Test Cases | Status |
|--------|------------|--------|
| Authentication & Authorization | 28 | ✅ All Pass |
| User Management | 37 | ✅ All Pass |
| Role Management | 25 | ✅ All Pass |
| Task Management | 26 | ✅ All Pass |
| Attendance Management | 40 | ✅ All Pass |
| Notice Management | 47 | ✅ All Pass |
| Feedback Management | 30 | ✅ All Pass |
| Helpline Ticketing | 18 | ✅ All Pass |
| Email Service | 20 | ✅ All Pass |
| **Total** | **303** | **✅ 100% Pass** |

### Run Tests

```bash
cd backend
mvn test
```

### Test Categories

- **Unit Tests**: Service layer business logic validation
- **Integration Tests**: Controller and API endpoint testing with MockMvc
- **Security Tests**: Authentication and authorization verification
- **Edge Case Tests**: Boundary conditions and error handling

---

## 👥 User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access • User management • All CRUD operations • System configuration |
| **HR** | Attendance management • User oversight • Download reports • Resolve HR tickets |
| **MANAGER** | Team management • Task oversight • Resolve manager tickets • Team notices |
| **SUPPORT** | Helpline ticket management • Resolve support tickets |
| **USER** | View assigned tasks • Mark attendance • Submit feedback • Raise tickets • View notices |

---

## 📂 Project Structure

```
phantask/
├── backend/                          # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/phantask/
│   │   │   │   └── authentication/  # Authentication module
│   │   │   │       ├── controller/  # REST API controllers
│   │   │   │       ├── service/     # Business logic layer
│   │   │   │       │   ├── api/     # Service interfaces
│   │   │   │       │   └── impl/    # Service implementations
│   │   │   │       ├── repository/  # Data access layer (JPA)
│   │   │   │       ├── entity/      # JPA entities
│   │   │   │       ├── dto/         # Data transfer objects
│   │   │   │       └── security/    # Security configuration
│   │   │   │           ├── JwtUtil.java
│   │   │   │           ├── JwtFilter.java
│   │   │   │           ├── SecurityConfig.java
│   │   │   │           └── CorsConfig.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-secrets.properties  # Create this
│   │   └── test/                    # Unit & integration tests
│   │       └── java/com/phantask/
│   │           └── authentication/
│   │               ├── controller/   # Controller tests
│   │               ├── service/      # Service tests
│   │               └── repository/   # Repository tests
│   └── pom.xml                      # Maven dependencies
│
├── frontend/                        # React frontend
│   ├── public/                      # Static assets
│   │   └── assets/                  # Images, icons
│   ├── src/
│   │   ├── components/              # Reusable components
│   │   │   ├── dashboard/           # Dashboard components
│   │   │   │   └── cards/           # Dashboard cards
│   │   │   ├── sidebar/             # Navigation sidebar
│   │   │   ├── login/               # Auth components
│   │   │   └── LoadingSkeleton.jsx  # Loading states
│   │   ├── pages/                   # Page components
│   │   │   ├── dashboard/           # Dashboard pages
│   │   │   ├── tasks/               # Task pages
│   │   │   ├── attendance/          # Attendance pages
│   │   │   ├── notices/             # Notice pages
│   │   │   ├── feedback/            # Feedback pages
│   │   │   ├── helpline/            # Helpline pages
│   │   │   └── users/               # User management
│   │   ├── services/                # API service layer
│   │   │   └── api.js               # Axios configuration
│   │   ├── context/                 # React context
│   │   │   └── ApiContext.jsx       # API context provider
│   │   ├── constants/               # App constants
│   │   │   └── roles.js             # Role definitions
│   │   ├── App.jsx                  # Root component
│   │   └── main.jsx                 # Entry point
│   ├── package.json                 # npm dependencies
│   ├── vite.config.js               # Vite configuration
│   └── tailwind.config.js           # TailwindCSS config
│
├── docs/                            # Documentation
│   ├── screenshots/                 # Application screenshots
│   └── project-report.pdf           # Complete project report
│
├── .gitignore
└── README.md                        # This file
```

---

## 🗺️ Roadmap & Future Enhancements

### Phase 1: Mobile & Real-time Features
- [ ] **Mobile Application** (Android & iOS with React Native)
- [ ] **Real-time Notifications** (WebSocket integration)
- [ ] **Push Notifications** for mobile devices

### Phase 2: Advanced Features
- [ ] **Advanced Analytics Dashboard** with charts and graphs
- [ ] **Calendar Integration** (Google Calendar, Outlook)
- [ ] **Document Management System** with version control
- [ ] **Chat & Collaboration Features** for team communication

### Phase 3: Enhanced Capabilities
- [ ] **Geolocation-based Attendance** verification
- [ ] **Leave Management System** with approval workflow
- [ ] **Biometric Integration** for attendance
- [ ] **Workflow Automation** with rule-based triggers

### Phase 4: Enterprise Features
- [ ] **Multi-organization Support** (SaaS deployment)
- [ ] **AI-powered Insights** and predictive analytics
- [ ] **Two-Factor Authentication (2FA)**
- [ ] **Single Sign-On (SSO)** integration
- [ ] **Audit Logs** for compliance tracking
- [ ] **Localization** and internationalization (i18n)

---

## 🐛 Known Issues & Limitations

- **External API Dependency**: Motivational quotes feature relies on DummyJSON API
- **Offline Mode**: Application requires continuous internet connectivity
- **Email Configuration**: SMTP server required for user creation emails (optional in development)

## 🔒 Security Design

- **Daily Re-authentication**: 12-hour session expiry ensures users must log in fresh each day for enhanced security
- **Mobile View**: Admin functionalities and helpline module are optimized for desktop/tablet (≥990px); mobile-responsive design in development

---

## 📊 Database Schema

### Core Entities

- **users**: User accounts and authentication
- **user_profiles**: Extended user information
- **roles**: System roles and permissions
- **user_roles**: User-role mapping (many-to-many)
- **tasks**: Task assignments and tracking
- **attendance**: Attendance records
- **attendance_tokens**: QR token management
- **notices**: Organizational announcements
- **notice_target_roles**: Notice-role mapping
- **feedback**: Feedback templates
- **rating**: User feedback responses
- **submission**: Feedback submissions
- **helpline_tickets**: Support ticket system

**Database Normalization:** Third Normal Form (3NF)

---

## 🔧 Troubleshooting

### Backend Issues

**Port Already in Use (8080)**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Database Connection Error**
- Verify MySQL is running
- Check credentials in `application-secrets.properties`
- Ensure database `phantask_db` exists

**JWT Secret Error**
- Ensure `jwt.secret` is at least 256 bits (32 characters) for HS256 algorithm

**Email Not Sending**
- Verify SMTP configuration in `application-secrets.properties`
- For Gmail, use App Password (not regular password)
- Check firewall/antivirus blocking port 587
- Application will still work without email configuration

### Frontend Issues

**CORS Error**
- Verify backend `CorsConfig.java` includes your frontend URL
- Check if backend is running on port 8080

**API Connection Failed**
- Ensure backend is running
- Check `src/services/api.js` has correct backend URL

**Module Not Found**
```bash
rm -rf node_modules package-lock.json
npm install
```

### Mobile Access Issues

**Cannot Access from Mobile**
- Ensure both devices are on the same WiFi network
- Verify CORS configuration includes `http://192.168.*.*:*`
- Check firewall settings allow incoming connections on port 5173
- Run frontend with `npm run dev -- --host` flag

---

## 📞 Support

For issues, questions, or contributions:
- **GitHub Issues**: [Report a bug](https://github.com/rylauq-solutions/PhanTask/issues)

---

<div align="center">

**Made with ❤️ using Spring Boot & React by PhanTask Team**

⭐ **Star this repository if you find it helpful!** ⭐

</div>
