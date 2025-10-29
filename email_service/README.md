### Email Service

A standalone Spring Boot service that simulates sending and tracking emails for the Store system. It persists email messages, exposes REST APIs to send and query them, and logs a simulated "send" action instead of sending real emails.

### Folder structure

```
email_service/
  build.gradle
  settings.gradle
  src/
    main/
      java/
        com/
          example/
            emailservice/
              EmailServiceApplication.java
              controller/
                EmailController.java
              model/
                EmailMessage.java
              repository/
                EmailMessageRepository.java
              service/
                EmailSender.java
      resources/
        application.properties
```

### What each file does

- build.gradle: Gradle build script. Defines Spring Boot, JPA, Web, Actuator, Lombok, PostgreSQL driver, Java toolchain (17) and JUnit dependencies.
- settings.gradle: Names the Gradle project `email_service`.
- src/main/resources/application.properties: App and database configuration (datasource URL, username, password, Hibernate dialect and ddl-auto). Defaults point to the Docker Compose Postgres from `delivery-co/docker-compose.yml`.
- src/main/java/com/example/emailservice/EmailServiceApplication.java: Spring Boot entrypoint with `main` method.
- src/main/java/com/example/emailservice/model/EmailMessage.java: JPA entity representing an email to be sent/tracked. Fields include `orderId`, `toAddress`, `subject`, `body`, `status`, `createdAt`, `sentAt`.
- src/main/java/com/example/emailservice/repository/EmailMessageRepository.java: Spring Data JPA repository for `EmailMessage`, with `findByOrderId(UUID)`.
- src/main/java/com/example/emailservice/service/EmailSender.java: Service that simulates sending by logging and updating status to `SENT` + `sentAt` timestamp, then persists changes.
- src/main/java/com/example/emailservice/controller/EmailController.java: REST API for sending and querying emails.

### Configuration

File: `src/main/resources/application.properties`

- spring.application.name: Logical service name.
- spring.datasource.url: PostgreSQL JDBC URL (default `jdbc:postgresql://localhost:15432/deliveryco`). Matches the Compose stack.
- spring.datasource.username / spring.datasource.password: Defaults to `deliveryco` / `changeme` to match Compose.
- spring.jpa.hibernate.ddl-auto: `none` (schema is managed by Flyway migrations `V1__...`, `V2__...`).
- spring.jpa.properties.hibernate.dialect: PostgreSQL dialect.

Environment override example (Windows PowerShell):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:15432/deliveryco"
$env:SPRING_DATASOURCE_USERNAME="deliveryco"
$env:SPRING_DATASOURCE_PASSWORD="changeme"
```

### Build and run

You can run with a local Gradle install or a Gradle wrapper if you add one.

- Using local Gradle (Windows PowerShell):
```powershell
gradle bootRun
```

- Using Gradle wrapper (if generated here):
```powershell
./gradlew.bat bootRun
```

The service starts on port 8081 by default. Change via `server.port` if needed.

### REST APIs

Base path: `/api/emails`

- POST `/api/emails/send`
  - Purpose: Persist and send (simulate) an email; transitions status from `QUEUED` to `SENT` and sets `sentAt`.
  - Request body:
  ```json
  {
    "orderId": "00000000-0000-0000-0000-000000000000",
    "toAddress": "customer@example.com",
    "subject": "Your order update",
    "body": "Package picked up"
  }
  ```
  - Response: `200 OK` with the saved `EmailMessage` (status `SENT`).

- GET `/api/emails/{id}`
  - Purpose: Fetch an email by ID.
  - Response: `200 OK` with `EmailMessage` or `404` if not found.

- GET `/api/emails/order/{orderId}`
  - Purpose: List all emails linked to an order.
  - Response: `200 OK` with `EmailMessage[]`.

- PUT `/api/emails/{id}/status?status=<QUEUED|SENT|FAILED>`
  - Purpose: Manually update status (e.g., simulate failures in demos).
  - Response: `200 OK` with updated `EmailMessage` or `404` if not found.

### Notes for integration with Store

- The service logs a simulated send instead of sending real emails, aligning with assignment requirements.
- Use the POST `/api/emails/send` endpoint from the Store app at delivery status transitions.
- Link emails to orders using the `orderId` UUID field so you can retrieve history per order.

