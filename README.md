# Spring Board Practice

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)  
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white) ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

A Spring Boot application that serves a basic bulletin board service, implemented to be deployable as a learning exercise. The original author's code can be found on [this GitHub repository](https://github.com/djkeh/fastcampus-project-board).  

### Application Structure

This application generally follows the traditional Spring MVC design pattern. In addition to this structure, a DTO layer exists between the domain and controller layer, as well as between the controller and view layer to facilitate decoupling between each layer. A separate service layer handles most of the business logic such as moving the appropriate entity data in and out of the persistance layer, pagination of responses, hashtag parsing, and so on.  


### Key Learning Goals
  
- Get familiar with the overall structure and design paradigms of Spring as a framework
- Experience database migration from MySQL to PostgreSQL through the use of the Hibernate ORM
- Application of behavior-driven development through implementation of unit tests following the given-when-then pattern
- Hands-on learning of various software design principles
- Hands-on learning of various Spring Boot modules and related packages such as Spring JPA, Spring Security, Mockito, JUnit, Lombok, Jakarta... etc
