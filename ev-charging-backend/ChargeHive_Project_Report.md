# ChargeHive - EV Charging Station Management System
## Project Report

---

## **1. Goals of the Project**

### **Primary Objectives**
- **Digital Transformation**: Create a comprehensive digital platform for EV charging station management
- **Ecosystem Integration**: Establish a unified system connecting station owners, operators, drivers, and administrators
- **Scalability**: Build a microservices-based architecture capable of handling growing EV infrastructure demands
- **Security**: Implement robust authentication and authorization mechanisms for multi-role access control

### **Business Goals**
- **Station Management**: Enable efficient registration, approval, and management of charging stations
- **Booking System**: Provide seamless booking experience for EV drivers
- **Administrative Control**: Offer comprehensive administrative oversight and audit capabilities
- **Revenue Optimization**: Support cost calculation and billing for charging services

### **Technical Goals**
- **Microservices Architecture**: Implement scalable, maintainable service-oriented design
- **Service Discovery**: Enable dynamic service registration and discovery
- **API Gateway**: Provide centralized routing, security, and load balancing
- **Database Design**: Create efficient, normalized database schema for EV charging operations

---

## **2. Overall Description**

### **Project Overview**
**ChargeHive** is a comprehensive EV charging station management system built using microservices architecture. The system facilitates the entire lifecycle of EV charging operations, from station registration to booking management and administrative oversight.

**System Purpose**: To provide a scalable, secure, and efficient platform for managing EV charging infrastructure, enabling seamless interaction between all stakeholders in the EV ecosystem.

**Target Users**:
- **EV Drivers**: End users who need to book and use charging stations
- **Station Owners**: Individuals or companies who own and operate charging stations
- **Station Operators**: Personnel responsible for station maintenance and operations
- **System Administrators**: Technical personnel managing the platform

### **Proposed Methodology**

#### **Microservices Architecture**
- **Service Independence**: Each service operates independently with its own database and business logic
- **Technology Diversity**: Services can use different technologies while maintaining interoperability
- **Scalability**: Individual services can be scaled based on demand
- **Fault Isolation**: Failure in one service doesn't affect the entire system

#### **Service-Oriented Design**
- **Discovery Server**: Centralized service registry using Netflix Eureka
- **API Gateway**: Single entry point with routing, security, and load balancing
- **Domain Services**: Specialized services for specific business domains
- **Inter-service Communication**: RESTful APIs for service-to-service communication

### **S/W and H/W Requirements**

#### **Software Requirements**
- **Java Development Kit (JDK)**: Version 17 or higher
- **Maven**: Version 3.6+ for dependency management
- **MySQL**: Version 8.0+ for database management
- **Spring Boot**: Version 3.5.4
- **Spring Cloud**: Version 2025.0.0

#### **Hardware Requirements**
- **CPU**: Minimum 4 cores, recommended 8+ cores
- **RAM**: Minimum 8GB, recommended 16GB+
- **Storage**: Minimum 100GB SSD, recommended 500GB+
- **Network**: High-speed internet connection

### **Technology Platform**

#### **Backend Technologies**
- **Spring Boot 3.5.4**: Rapid application development framework
- **Spring Cloud 2025.0.0**: Microservices development framework
- **Spring Security**: Comprehensive security framework
- **JPA/Hibernate**: Object-relational mapping framework

#### **Database Technology**
- **MySQL Database**: ACID compliance, transaction support
- **Database Design**: Third normal form for data integrity
- **Indexing**: Strategic indexing for query optimization

#### **Security Technologies**
- **JWT Authentication**: HMAC-SHA256 for token signing
- **BCrypt Password Encryption**: Industry-standard password hashing
- **Role-based Authorization**: Granular permissions based on user roles

---

## **3. Requirements Specification**

### **Functional Requirements**

#### **User Management**
- **User Registration**: Email, password, name, and role assignment
- **User Authentication**: JWT token generation upon successful authentication
- **Role-based Access Control**: ADMIN, OWNER, DRIVER roles with specific permissions

#### **Station Management**
- **Station Registration**: Location data, multiple charging ports per station
- **Station Approval Workflow**: Administrative approval with audit logging
- **Port Management**: Add/remove charging ports with specifications

#### **Booking Management**
- **Booking Creation**: Time slot selection and duration specification
- **Booking Status Tracking**: BOOKED, CANCELLED, COMPLETED states
- **Cost Calculation**: Dynamic pricing based on duration and rates

#### **Administrative Functions**
- **Station Approval**: Review and approve/reject new station registrations
- **User Management**: View all users, block/unblock accounts
- **Audit Logging**: Track all administrative actions

### **Non-Functional Requirements**
- **Performance**: API responses within 2 seconds, support 1000+ concurrent users
- **Security**: JWT-based authentication, role-based access control
- **Scalability**: Horizontal scaling capability with microservices
- **Availability**: 99.9% uptime with fault tolerance

### **External Interface Requirements**

#### **REST API Endpoints**
```
Authentication: POST /api/auth/register, POST /api/auth/login
Station Management: POST /api/stations, GET /api/stations
Admin Functions: POST /api/admin/stations/process-approval
Booking Management: POST /api/bookings, GET /api/bookings
```

---

## **4. System Diagrams**

### **System Architecture**
```
Client Apps → API Gateway → Discovery Server
                ↓
        ┌─────────────────┐
        │   Microservices │
        │  - Auth Service │
        │  - Station Svc  │
        │  - Booking Svc  │
        │  - Admin Service│
        └─────────────────┘
                ↓
            MySQL Database
```

### **Service Communication Flow**
1. **Client Request** → API Gateway
2. **JWT Validation** → Authentication check
3. **Route to Service** → Based on endpoint
4. **Service Processing** → Business logic execution
5. **Database Operations** → Data persistence
6. **Response** → Client receives result

### **Database Schema**
```
Users (id, name, email, password, role)
    ↓ 1:N
Stations (id, name, address, coordinates, owner_id, is_approved)
    ↓ 1:N
Station_Ports (id, connector_type, max_power, station_id)
    ↓ 1:N
Bookings (id, user_id, station_id, port_id, start_time, end_time, cost, status)
```

---

## **5. Implementation Details**

### **Core Services**

#### **Discovery Server (Port: 8761)**
- **Technology**: Spring Cloud Netflix Eureka Server
- **Purpose**: Service registry and discovery
- **Features**: Health monitoring, load balancing

#### **API Gateway (Port: 8080)**
- **Technology**: Spring Cloud Gateway with WebFlux
- **Features**: JWT authentication, role-based authorization, route management
- **Security**: Custom JWT filter with role validation

#### **Auth Service (Port: 8082)**
- **Technology**: Spring Boot with Spring Security
- **Features**: User management, JWT generation, password encryption
- **Security**: BCrypt password hashing, JWT token management

#### **Station Service (Port: 8083)**
- **Technology**: Spring Boot with JPA/Hibernate
- **Features**: Station management, port management, approval workflow
- **Database**: MySQL with optimized queries

#### **Booking Service (Dynamic Port)**
- **Technology**: Spring Boot with JPA
- **Features**: Booking management, cost calculation, status tracking
- **Status**: Partially implemented

#### **Admin Service (Port: 8081)**
- **Technology**: Spring Boot with RestTemplate
- **Features**: Administrative functions, audit logging
- **Integration**: REST client for inter-service communication

### **Security Implementation**

#### **JWT Authentication Flow**
1. User registers/logs in via Auth Service
2. Auth Service generates JWT token with user roles
3. API Gateway validates JWT tokens on each request
4. Role-based access control enforced at gateway level

#### **Role Hierarchy**
- **ADMIN**: Full system access, station approval, user management
- **OWNER**: Station management, booking oversight
- **DRIVER**: Booking creation, profile management

### **Database Implementation**

#### **Entity Relationships**
- **User-Station**: One-to-many (owner relationship)
- **Station-Port**: One-to-many (port management)
- **User-Booking**: One-to-many (booking history)
- **Station-Booking**: One-to-many (station usage)

---

## **6. Testing and Deployment**

### **Testing Strategy**
- **Unit Testing**: Service layer, repository layer, controller layer
- **Integration Testing**: Inter-service communication, database integration
- **API Testing**: Postman collections, Swagger documentation
- **Security Testing**: JWT flow testing, penetration testing

### **Deployment**
- **Service Deployment**: Docker containerization with Kubernetes orchestration
- **Database Deployment**: MySQL server with backup and replication
- **Configuration Management**: Environment variables and secret management

---

## **7. Conclusion and Future Enhancements**

### **Project Summary**
**ChargeHive** successfully implements a comprehensive EV charging station management system using modern microservices architecture. The system provides scalable architecture, robust security, comprehensive functionality, and administrative oversight.

### **Achievements**
✅ **Successfully Implemented**:
- Microservices architecture with service discovery
- JWT-based authentication and authorization
- Station registration and approval workflow
- User management with role-based access
- API Gateway with security and routing
- Database design with proper relationships
- Inter-service communication
- Audit logging system

### **Future Enhancements**

#### **Technical Enhancements**
- Complete booking service implementation
- Payment gateway integration
- Real-time notifications with WebSocket
- Redis caching for performance optimization
- Comprehensive monitoring and alerting
- Complete API documentation

#### **Feature Enhancements**
- Native mobile applications
- Business intelligence and reporting
- Advanced location-based features
- Multiple payment method support
- Email and SMS notifications
- Comprehensive reporting capabilities

#### **Operational Enhancements**
- Automated deployment pipeline
- Kubernetes deployment
- Prometheus and Grafana integration
- Centralized logging system
- Advanced security measures
- Database and query optimization

### **Recommendations**

#### **Immediate Priorities**
1. Complete booking service endpoints
2. Add comprehensive testing coverage
3. Implement robust error handling
4. Complete Swagger documentation
5. Conduct security audit

#### **Medium-term Goals**
1. Implement payment processing
2. Add WebSocket for real-time features
3. Optimize performance with caching
4. Implement application monitoring
5. Develop mobile applications

#### **Long-term Vision**
1. Implement horizontal scaling
2. Add multi-language support
3. Integrate business intelligence
4. Add IoT integration for smart stations
5. Implement AI/ML for predictive analytics

---

**Project Status**: Development Phase  
**Last Updated**: December 2024  
**Version**: 1.0.0  
**Team**: CDAC Final Project Team
