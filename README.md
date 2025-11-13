# Email Registration Microservice

A public Spring Boot microservice for email registration with MindsDB integration for fraud detection.

## 🌐 Public API

**All API endpoints are publicly accessible without authentication.**

This service provides a RESTful API for:
- Email registration management
- MindsDB query execution for fraud detection
- Database and model management through MindsDB

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- (Optional) MindsDB instance for fraud detection features

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The service will start on `http://localhost:8080/api`

## 📡 API Endpoints

### Email Registration

- **POST** `/api/emails/register` - Register a new email
  ```json
  {
    "email": "user@example.com",
    "username": "username",
    "password": "password"
  }
  ```

- **GET** `/api/emails` - Get all registered emails
- **GET** `/api/emails/{email}` - Get specific email registration

### MindsDB Integration

- **POST** `/api/mindsdb/query` - Execute MindsDB SQL query
  ```json
  {
    "query": "SELECT * FROM mindsdb.models"
  }
  ```

- **GET** `/api/mindsdb/models` - List all MindsDB models
- **GET** `/api/mindsdb/databases` - List all connected databases
- **GET** `/api/mindsdb/health` - Health check for MindsDB connection
- **POST** `/api/mindsdb/check-fraud` - Check if registration is suspicious
  ```json
  {
    "email": "test@example.com",
    "username": "testuser"
  }
  ```

## 🔧 Configuration

### Environment Variables

For production deployment, configure the following environment variables:

#### Database Configuration
```bash
export DB_URL="jdbc:mysql://your-db-host:3306/email_reg_db?createDatabaseIfNotExist=true"
export DB_USERNAME="your-db-username"
export DB_PASSWORD="your-db-password"
```

#### MindsDB Configuration
```bash
export MINDSDB_API_URL="http://your-mindsdb-host:47334"
export MINDSDB_API_TIMEOUT="30000"
export MINDSDB_API_KEY="your-api-key"  # For MindsDB Cloud
```

#### Security Configuration (Optional)
```bash
export SPRING_SECURITY_USER="admin"
export SPRING_SECURITY_PASSWORD="secure-password"
```

### Default Values

If environment variables are not set, the application uses these defaults (suitable for local development):

- **Database**: `localhost:3306/email_reg_db` (username: `emailapp`, password: `emailapp123`)
- **MindsDB**: `http://localhost:47334`
- **Server Port**: `8080`
- **Context Path**: `/api`

## 🔒 Security

### Public Access

All endpoints are publicly accessible. The API does not require authentication for any operations.

**Important Security Notes:**

1. **For Production Use**: Consider implementing:
   - API rate limiting to prevent abuse
   - API keys or OAuth2 for sensitive operations
   - IP whitelisting if needed
   - Proper firewall rules

2. **Credentials**: Never commit sensitive credentials to version control. Always use environment variables in production.

3. **CORS**: The API allows requests from any origin. Restrict this in production by modifying `SecurityConfig.java`:
   ```java
   configuration.setAllowedOriginPatterns(Arrays.asList("https://yourdomain.com"));
   ```

## 🗄️ Database Setup

Create a MySQL database and user:

```sql
CREATE DATABASE email_reg_db;
CREATE USER 'emailapp'@'localhost' IDENTIFIED BY 'emailapp123';
GRANT ALL PRIVILEGES ON email_reg_db.* TO 'emailapp'@'localhost';
FLUSH PRIVILEGES;
```

The application will automatically create the required tables using JPA/Hibernate.

## 🧪 Testing

Run tests with:

```bash
./gradlew test
```

**Note**: Tests require a running MySQL database. Configure test database connection in your test configuration.

## 📦 Building for Production

Create a production-ready JAR:

```bash
./gradlew bootJar
```

The JAR file will be created in `build/libs/email-reg-ms-0.0.1-SNAPSHOT.jar`

Run the JAR:

```bash
java -jar build/libs/email-reg-ms-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Deployment (Optional)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t email-reg-ms .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/email_reg_db" \
  -e DB_USERNAME="emailapp" \
  -e DB_PASSWORD="emailapp123" \
  email-reg-ms
```

## 🤖 MindsDB Integration

This service integrates with MindsDB for:
- Fraud detection on email registrations
- ML-based pattern recognition
- Database connectivity through MindsDB

### Setting up MindsDB

**Option 1: Local MindsDB**
```bash
pip install mindsdb
python -m mindsdb
```

**Option 2: MindsDB Cloud**
1. Sign up at https://cloud.mindsdb.com
2. Get your API key
3. Set `MINDSDB_API_URL=https://cloud.mindsdb.com`
4. Set `MINDSDB_API_KEY=your-key`

**Option 3: Docker**
```bash
docker run -p 47334:47334 mindsdb/mindsdb
```

## 📝 License

This project is open source and available for public use.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## 📧 Support

For issues and questions, please use the GitHub issue tracker.
