# ChargeHive - EV Charging Station Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-yellow)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-purple)
![JWT](https://img.shields.io/badge/Security-JWT-red)

**A comprehensive microservices-based EV charging station management system**

[Features](#features) • [Architecture](#architecture) • [Quick Start](#quick-start) • [API Documentation](#api-documentation) • [Contributing](#contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## 🚀 Overview

**ChargeHive** is a comprehensive EV charging station management system built using microservices architecture. The system facilitates the entire lifecycle of EV charging operations, from station registration to booking management and administrative oversight.

### 🎯 Key Objectives

- **Digital Transformation**: Create a comprehensive digital platform for EV charging station management
- **Ecosystem Integration**: Establish a unified system connecting station owners, operators, drivers, and administrators
- **Scalability**: Build a microservices-based architecture capable of handling growing EV infrastructure demands
- **Security**: Implement robust authentication and authorization mechanisms for multi-role access control

---

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT-based Authentication**: Secure token-based authentication
- **Role-based Access Control**: ADMIN, OWNER, DRIVER roles with granular permissions
- **Password Encryption**: BCrypt hashing for secure password storage
- **API Gateway Security**: Centralized security with route protection

### 🏢 Station Management
- **Station Registration**: Complete station registration with location data
- **Port Management**: Multiple charging ports per station with specifications
- **Approval Workflow**: Administrative approval system with audit logging
- **Geographic Location**: GPS coordinates and address management

### 📅 Booking System
- **Booking Creation**: Time slot selection and duration specification
- **Status Tracking**: BOOKED, CANCELLED, COMPLETED states
- **Cost Calculation**: Dynamic pricing based on duration and rates
- **User History**: Complete booking history for users

### 👨‍💼 Administrative Functions
- **Station Approval**: Review and approve/reject new station registrations
- **User Management**: View all users, block/unblock accounts
- **Audit Logging**: Comprehensive audit trail for all administrative actions
- **System Monitoring**: Health checks and service monitoring

---

## 🏗️ Architecture

### System Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   Web Browser   │    │   Mobile Apps   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (Port: 8080)  │
                    │  - JWT Auth     │
                    │  - Routing      │
                    │  - Load Balance │
                    └─────────┬───────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼──────┐    ┌────────▼────────┐    ┌──────▼──────┐
│Discovery     │    │   Auth Service  │    │Station      │
│Server        │    │   (Port: 8082)  │    │Service      │
│(Port: 8761)  │    │  - User Mgmt    │    │(Port: 8083) │
│- Service     │    │  - JWT Gen      │    │- Station    │
│  Registry    │    │  - Auth         │    │  Mgmt       │
│- Health      │    │  - Roles        │    │- Port Mgmt  │
│  Monitoring  │    └─────────────────┘    │- Approval   │
└──────────────┘                           └─────────────┘
        │
        │
┌───────▼──────┐    ┌─────────────────┐    ┌──────▼──────┐
│Booking       │    │   Admin Service │    │Test Service │
│Service       │    │   (Port: 8081)  │    │(Dynamic)    │
│(Dynamic)     │    │  - Station      │    │- Testing    │
│- Booking     │    │    Approval     │    │- Dev Utils  │
│  Mgmt        │    │  - User Mgmt    │    └─────────────┘
│- Cost Calc   │    │  - Audit Logs   │
│- Status      │    └─────────────────┘
│  Tracking    │
└──────────────┘
        │
        └─────────────────────────────────────┐
                                              │
                                    ┌────────▼────────┐
                                    │   MySQL DB      │
                                    │   (chargehive)  │
                                    │  - Users        │
                                    │  - Stations     │
                                    │  - Bookings     │
                                    │  - Audit Logs   │
                                    └─────────────────┘
```

### Microservices

| Service | Port | Description | Status |
|---------|------|-------------|--------|
| **Discovery Server** | 8761 | Service registry and discovery | ✅ Active |
| **API Gateway** | 8080 | Centralized routing and security | ✅ Active |
| **Auth Service** | 8082 | User management and JWT generation | ✅ Active |
| **Station Service** | 8083 | Station and port management | ✅ Active |
| **Admin Service** | 8081 | Administrative functions | ✅ Active |
| **Booking Service** | Dynamic | Booking management | 🔄 In Progress |
| **Test Service** | Dynamic | Development utilities | ✅ Active |

---

## 🛠️ Technology Stack

### Backend Technologies
- **Java**: Version 17/21
- **Spring Boot**: Version 3.5.4
- **Spring Cloud**: Version 2025.0.0
- **Spring Security**: Comprehensive security framework
- **JPA/Hibernate**: Object-relational mapping

### Database
- **MySQL**: Version 8.0+
- **Connection Pooling**: HikariCP
- **Migration**: Hibernate auto-DDL

### Security
- **JWT**: HMAC-SHA256 token signing
- **BCrypt**: Password hashing
- **Role-based Authorization**: Granular permissions

### Service Discovery
- **Netflix Eureka**: Service registry and discovery
- **Load Balancing**: Client-side load balancing

### API Gateway
- **Spring Cloud Gateway**: Reactive routing
- **WebFlux**: Non-blocking I/O
- **Route Protection**: JWT validation

### Development Tools
- **Maven**: Dependency management
- **Lombok**: Boilerplate reduction
- **ModelMapper**: Object mapping
- **Swagger/OpenAPI**: API documentation

---

## 📋 Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Maven**: Version 3.6+
- **MySQL**: Version 8.0+
- **Memory**: Minimum 8GB RAM
- **Storage**: Minimum 10GB free space

### Development Environment
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: Version control
- **Postman**: API testing (optional)
- **MySQL Workbench**: Database management (optional)

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd charge-hive/ev-charging-backend
```

### 2. Database Setup

#### Create MySQL Database
```sql
CREATE DATABASE chargehive;
CREATE USER 'chargehive_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON chargehive.* TO 'chargehive_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Configure Database Connection
Update the database configuration in each service's `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chargehive
spring.datasource.username=chargehive_user
spring.datasource.password=your_password
```

### 3. Start Services

#### Option 1: Individual Service Startup

```bash
# 1. Start Discovery Server
cd DiscoveryServer
mvn spring-boot:run

# 2. Start API Gateway
cd ../ApiGateway
mvn spring-boot:run

# 3. Start Auth Service
cd ../auth-service
mvn spring-boot:run

# 4. Start Station Service
cd ../station-service
mvn spring-boot:run

# 5. Start Admin Service
cd ../admin-service
mvn spring-boot:run

# 6. Start Booking Service (optional)
cd ../booking-service
mvn spring-boot:run
```

#### Option 2: Using Maven Wrapper

```bash
# Discovery Server
./DiscoveryServer/mvnw spring-boot:run

# API Gateway
./ApiGateway/mvnw spring-boot:run

# Auth Service
./auth-service/mvnw spring-boot:run

# Station Service
./station-service/mvnw spring-boot:run

# Admin Service
./admin-service/mvnw spring-boot:run
```

### 4. Verify Services

#### Check Service Health
- **Discovery Server**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8082
- **Station Service**: http://localhost:8083
- **Admin Service**: http://localhost:8081

#### Eureka Dashboard
Visit http://localhost:8761 to see all registered services.

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "userRole": "ROLE_DRIVER"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Station Management

#### Create Station
```http
POST /stations
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Downtown Charging Station",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "ownerId": 1,
  "ports": [
    {
      "connectorType": "Type 2",
      "maxPowerKw": 22.0
    }
  ]
}
```

#### Get All Stations
```http
GET /stations
Authorization: Bearer <jwt_token>
```

### Admin Functions

#### Approve Station
```http
POST /admin/stations/process-approval
Authorization: Bearer <jwt_token>
X-Admin-Id: 1
Content-Type: application/json

{
  "stationId": 1,
  "approved": true,
  "reason": "Station meets all requirements"
}
```

#### Get Unapproved Stations
```http
GET /admin/stations/unapproved
Authorization: Bearer <jwt_token>
```

### User Management

#### Get All Users
```http
GET /auth/get-all
Authorization: Bearer <jwt_token>
```

#### Edit User Profile
```http
PUT /auth/edit-user
Authorization: Bearer <jwt_token>
X-User-Id: 1
Content-Type: application/json

{
  "name": "John Updated",
  "email": "john.updated@example.com"
}
```

---

## 🗄️ Database Schema

### Entity Relationships

```
Users (id, name, email, password, role)
    ↓ 1:N
Stations (id, name, address, coordinates, owner_id, is_approved)
    ↓ 1:N
Station_Ports (id, connector_type, max_power, station_id)
    ↓ 1:N
Bookings (id, user_id, station_id, port_id, start_time, end_time, cost, status)
```

### Database Tables

#### Users Table
```sql
CREATE TABLE Users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_role ENUM('ROLE_ADMIN', 'ROLE_OWNER', 'ROLE_DRIVER') NOT NULL
);
```

#### Stations Table
```sql
CREATE TABLE stations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    is_approved BOOLEAN DEFAULT FALSE,
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES Users(id)
);
```

#### Station_Ports Table
```sql
CREATE TABLE station_ports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    connector_type VARCHAR(50) NOT NULL,
    max_power_kw DOUBLE NOT NULL,
    station_id BIGINT NOT NULL,
    FOREIGN KEY (station_id) REFERENCES stations(id)
);
```

#### Bookings Table
```sql
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    port_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT NOT NULL,
    total_cost DOUBLE NOT NULL,
    status ENUM('BOOKED', 'CANCELLED', 'COMPLETED') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (station_id) REFERENCES stations(id),
    FOREIGN KEY (port_id) REFERENCES station_ports(id)
);
```

#### Audit_Logs Table
```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(100) NOT NULL,
    target_id BIGINT NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔐 Security

### JWT Authentication Flow

1. **User Registration/Login**: User authenticates via Auth Service
2. **Token Generation**: Auth Service generates JWT with user roles
3. **Request Validation**: API Gateway validates JWT on each request
4. **Role-based Access**: Gateway enforces role-based permissions

### Role Hierarchy

| Role | Permissions | Access Level |
|------|-------------|--------------|
| **ADMIN** | Full system access, station approval, user management | System-wide |
| **OWNER** | Station management, booking oversight, revenue tracking | Station-specific |
| **DRIVER** | Booking creation, profile management, payment processing | User-specific |

### Security Features

- **JWT Token Validation**: HMAC-SHA256 signing
- **Password Encryption**: BCrypt hashing with salt
- **Route Protection**: Gateway-level security
- **Role-based Authorization**: Granular permissions
- **Audit Logging**: Comprehensive security audit trail

---

## 💻 Development

### Project Structure

```
ev-charging-backend/
├── DiscoveryServer/          # Service registry
├── ApiGateway/              # API Gateway with security
├── auth-service/            # Authentication & user management
├── station-service/         # Station & port management
├── booking-service/         # Booking operations
├── admin-service/           # Administrative functions
├── test-service/            # Development utilities
└── .gitignore
```

### Development Workflow

1. **Feature Development**: Create feature branches from `main`
2. **Service Development**: Develop individual microservices
3. **Integration Testing**: Test inter-service communication
4. **Code Review**: Peer review before merging
5. **Deployment**: Deploy to development environment

### Code Standards

- **Java**: Follow Google Java Style Guide
- **Spring Boot**: Use Spring Boot conventions
- **Database**: Use JPA/Hibernate best practices
- **Security**: Implement OWASP security guidelines
- **Testing**: Maintain 80%+ code coverage

---

## 🧪 Testing

### Test Categories

#### Unit Testing
```bash
# Run unit tests for a specific service
cd auth-service
mvn test

# Run all unit tests
mvn test -Dtest=**/*Test.java
```

#### Integration Testing
```bash
# Run integration tests
mvn test -Dtest=**/*IntegrationTest.java
```

#### API Testing
```bash
# Test API endpoints with Postman
# Import the provided Postman collection
```

### Test Coverage

- **Service Layer**: Business logic testing
- **Repository Layer**: Data access testing
- **Controller Layer**: API endpoint testing
- **Security Layer**: Authentication and authorization testing

### Performance Testing

```bash
# Load testing with Apache JMeter
jmeter -n -t performance-test.jmx -l results.jtl
```

---

## 🚀 Deployment

### Production Environment

#### Docker Deployment
```bash
# Build Docker images
docker build -t chargehive-discovery ./DiscoveryServer
docker build -t chargehive-gateway ./ApiGateway
docker build -t chargehive-auth ./auth-service
docker build -t chargehive-station ./station-service
docker build -t chargehive-admin ./admin-service

# Run with Docker Compose
docker-compose up -d
```

#### Kubernetes Deployment
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/discovery-server.yaml
kubectl apply -f k8s/api-gateway.yaml
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/station-service.yaml
kubectl apply -f k8s/admin-service.yaml
```

### Environment Configuration

#### Development
```properties
# application-dev.properties
spring.profiles.active=dev
logging.level.root=DEBUG
```

#### Production
```properties
# application-prod.properties
spring.profiles.active=prod
logging.level.root=WARN
```

### Monitoring

- **Health Checks**: `/actuator/health`
- **Metrics**: Prometheus integration
- **Logging**: Centralized logging with ELK stack
- **Alerting**: Grafana dashboards

---

## 🤝 Contributing

### Getting Started

1. **Fork the Repository**: Create your own fork
2. **Create Feature Branch**: `git checkout -b feature/amazing-feature`
3. **Make Changes**: Implement your feature
4. **Test Thoroughly**: Ensure all tests pass
5. **Submit Pull Request**: Create a detailed PR

### Development Guidelines

- **Code Style**: Follow existing code conventions
- **Documentation**: Update README and API docs
- **Testing**: Add unit and integration tests
- **Security**: Follow security best practices

### Issue Reporting

- **Bug Reports**: Use GitHub Issues with detailed descriptions
- **Feature Requests**: Provide clear use cases and requirements
- **Security Issues**: Report privately to maintainers

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Team

**CDAC Final Project Team**

- **Project Lead**: [Your Name]
- **Backend Development**: [Team Members]
- **Database Design**: [Team Members]
- **Security Implementation**: [Team Members]

---

## 📞 Support

### Getting Help

- **Documentation**: Check this README and project report
- **Issues**: Use GitHub Issues for bug reports
- **Discussions**: Use GitHub Discussions for questions
- **Email**: [vkumarzine@example.com]

### Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [JWT Documentation](https://jwt.io/)

---

<div align="center">

**Made with ❤️ by the CDAC Final Project Team**

[Back to Top](#chargehive---ev-charging-station-management-system)

</div>
