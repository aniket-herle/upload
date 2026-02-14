# S3 Upload Service

A microservice for handling secure file uploads to AWS S3 with automatic mirroring to external storage providers. Part of the Multi Mirror Project, this service provides presigned URL-based uploads, file lifecycle management, and event-driven architecture for distributed file operations.

## 🚀 Features

- **Presigned URL-based Uploads**: Generate secure, time-limited presigned URLs for direct S3 uploads
- **Two-Phase Upload Process**: Initialize upload and confirm completion with validation
- **File Lifecycle Management**: Track file upload status (PENDING, UPLOADED, FAILED)
- **Mirror Status Tracking**: Monitor file mirroring across multiple external providers
- **Event-Driven Architecture**: Kafka-based event publishing for distributed systems
- **Data Integrity**: S3 object verification and optional checksum validation
- **RESTful API**: Clean REST API with comprehensive error handling
- **Database Persistence**: MySQL-backed storage for file metadata and mirror status

## 📋 Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Event Model](#event-model)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)

## 🏗️ Architecture

### System Overview

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────┐
│   Client    │────────>│  Upload Service  │────────>│   AWS S3    │
└─────────────┘         └──────────────────┘         └─────────────┘
                               │    │
                               │    │ Kafka Events
                               ▼    ▼
                        ┌──────────────┐
                        │    Kafka     │
                        └──────────────┘
                               │
                               ▼
                        ┌──────────────┐
                        │   Mirror     │
                        │  Services    │
                        └──────────────┘
```

### Upload Flow

```
1. Client Request         2. Generate URL         3. Direct Upload
   ┌────────┐                ┌────────┐              ┌────────┐
   │ Client │──init-upload──>│Service │              │   S3   │
   └────────┘                └────────┘              └────────┘
                                  │                       │
                                  │ Presigned URL         │
                                  ├──────────────────────>│
                                  │                       │
                                  │<──────────────────────┤
   ┌────────┐                ┌────────┐              ┌────────┐
   │ Client │───PUT file────>│   S3   │              │        │
   └────────┘                └────────┘              │        │
       │                                             │        │
       └──complete-upload──>┌────────┐              │        │
                            │Service │──verify─────>│   S3   │
                            └────────┘              └────────┘
                                  │
                                  └──publish event──>Kafka
```

### Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Upload Service                          │
├─────────────────────────────────────────────────────────────┤
│  Controllers (REST API)                                      │
│  ┌──────────────────┐  ┌─────────────────────┐             │
│  │ S3UploadController│  │FileMirrorController │             │
│  └──────────────────┘  └─────────────────────┘             │
├─────────────────────────────────────────────────────────────┤
│  Services (Business Logic)                                   │
│  ┌──────────────────┐  ┌─────────────────────┐             │
│  │S3FileUploadService│  │  FileMirrorService  │             │
│  └──────────────────┘  └─────────────────────┘             │
│  ┌──────────────────┐                                       │
│  │KafkaProducerSvc  │                                       │
│  └──────────────────┘                                       │
├─────────────────────────────────────────────────────────────┤
│  Repositories (Data Access)                                  │
│  ┌──────────────────┐  ┌─────────────────────┐             │
│  │FileRecordRepo    │  │  FileUploadRepo     │             │
│  └──────────────────┘  └─────────────────────┘             │
│  ┌──────────────────┐                                       │
│  │  FileMirrorRepo  │                                       │
│  └──────────────────┘                                       │
├─────────────────────────────────────────────────────────────┤
│  Consumers (Event Handlers)                                  │
│  ┌──────────────────┐                                       │
│  │FileMirroredConsumer│                                     │
│  └──────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Tech Stack

### Core Technologies
- **Java 21** - Latest LTS version with modern language features
- **Spring Boot 4.0.1** - Application framework
- **Spring Data JPA** - Database abstraction and ORM
- **Hibernate** - JPA implementation

### Storage & Messaging
- **AWS SDK for Java v2 (2.40.10)** - S3 operations and presigned URL generation
- **Apache Kafka** - Event streaming platform
- **MySQL** - Primary relational database
- **H2** - In-memory database for testing

### Additional Libraries
- **Lombok** - Code generation for boilerplate reduction
- **Jackson** - JSON serialization/deserialization
- **Spring Validation** - Request validation

### Build & Development
- **Gradle** - Build automation
- **Spring Boot DevTools** - Development productivity tools

## 📡 API Documentation

Base URL: `http://localhost:8081/api`

### Upload Endpoints

#### 1. Initialize Upload

Initialize a new file upload and receive a presigned URL for direct S3 upload.

**Endpoint:** `POST /upload/init-upload`

**Request Body:**
```json
{
  "fileName": "example.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 1048576
}
```

**Request Fields:**
- `fileName` (string, required): Name of the file to upload
- `contentType` (string, required): MIME type of the file
- `sizeBytes` (number, required): File size in bytes (must be positive)

**Response:** `200 OK`
```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "uploadUrl": "https://bucket.s3.region.amazonaws.com/uploads/file?X-Amz-...",
  "s3Key": "uploads/550e8400-e29b-41d4-a716-446655440000_example.pdf"
}
```

**Response Fields:**
- `fileId`: Unique identifier for the file
- `uploadUrl`: Presigned URL for uploading (valid for 10 minutes)
- `s3Key`: S3 object key where the file will be stored

**Process:**
1. Service generates unique file ID
2. Creates file metadata record in database
3. Initializes upload status as `PENDING`
4. Generates presigned S3 URL (10-minute expiration)
5. Returns upload URL to client

---

#### 2. Complete Upload

Confirm upload completion and trigger file verification and mirroring.

**Endpoint:** `POST /upload/{fileId}/complete-upload`

**Path Parameters:**
- `fileId` (string, required): The file ID returned from init-upload

**Request Body (Optional):**
```json
{
  "checksum": "abc123def456..."
}
```

**Request Fields:**
- `checksum` (string, optional): File checksum for integrity verification

**Response:** `204 No Content`

**Process:**
1. Validates upload status is `PENDING`
2. Verifies file exists in S3 using HEAD request
3. Updates metadata (size, checksum, S3 URL)
4. Changes status to `UPLOADED`
5. Publishes `FileUploadEvent` to Kafka
6. Publishes `FileMirrorCheckEvent` for each configured mirror provider

**Error Responses:**
- `400 Bad Request` - Invalid file ID or already completed
- `404 Not Found` - Upload not found or file not in S3
- `409 Conflict` - Upload not in PENDING state

---

### Mirror Endpoints

#### 3. Get File Mirrors

Retrieve mirroring status for a specific file across all providers.

**Endpoint:** `GET /files/{fileId}/mirrors`

**Path Parameters:**
- `fileId` (string, required): The file ID

**Response:** `200 OK`
```json
[
  {
    "providerName": "cloudflare",
    "status": "COMPLETED",
    "externalUrl": "https://cloudflare.example.com/files/abc123",
    "errorMessage": null,
    "mirroredAt": "2026-02-14T04:30:00Z"
  },
  {
    "providerName": "backblaze",
    "status": "PENDING",
    "externalUrl": null,
    "errorMessage": null,
    "mirroredAt": null
  }
]
```

**Response Fields:**
- `providerName`: Name of the mirror provider
- `status`: Mirror status (PENDING, COMPLETED, FAILED)
- `externalUrl`: URL where file is accessible on the provider
- `errorMessage`: Error description if mirroring failed
- `mirroredAt`: Timestamp when mirroring completed

---

#### 4. Report Mirror Failure

Report a mirroring failure for a specific provider (typically called by mirror services).

**Endpoint:** `POST /files/{fileId}/mirrors/{providerName}/report-failure`

**Path Parameters:**
- `fileId` (string, required): The file ID
- `providerName` (string, required): Name of the mirror provider

**Response:** `202 Accepted`

**Process:**
1. Creates/updates mirror record with FAILED status
2. Logs failure for monitoring

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐
│   FileRecord    │
│─────────────────│
│ fileId (PK)     │
│ fileName        │
│ contentType     │
│ sizeBytes       │
│ s3Bucket        │
│ s3Key           │
│ s3Url           │
│ checksum        │
│ createdAt       │
│ updatedAt       │
└─────────────────┘
         │
         │ 1:1
         ▼
┌─────────────────┐
│   FileUpload    │
│─────────────────│
│ fileId (PK,FK)  │
│ status          │
│ createdAt       │
│ updatedAt       │
└─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│   FileMirror    │
│─────────────────│
│ id (PK)         │
│ fileId (FK)     │
│ providerName    │
│ status          │
│ externalUrl     │
│ errorMessage    │
│ mirroredAt      │
│ createdAt       │
│ updatedAt       │
└─────────────────┘
```

### Table Descriptions

#### `files`
Stores immutable file metadata and S3 location information.

| Column | Type | Description |
|--------|------|-------------|
| file_id | VARCHAR (PK) | Unique file identifier (UUID) |
| file_name | VARCHAR | Original filename |
| content_type | VARCHAR | MIME type of the file |
| size_bytes | BIGINT | File size in bytes |
| s3_bucket | VARCHAR | S3 bucket name |
| s3_key | VARCHAR | S3 object key |
| s3_url | TEXT | Full S3 URL for access |
| checksum | VARCHAR | File checksum (optional) |
| created_at | TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

**Constraints:**
- Primary Key: `file_id`
- Unique: `(s3_bucket, s3_key)`
- Index: `idx_files_created_at` on `created_at`

---

#### `file_uploads`
Tracks the upload lifecycle state for each file.

| Column | Type | Description |
|--------|------|-------------|
| file_id | VARCHAR (PK, FK) | References files.file_id |
| status | VARCHAR (ENUM) | Upload status: PENDING, UPLOADED, FAILED |
| created_at | TIMESTAMP | Upload initiation time |
| updated_at | TIMESTAMP | Last status update |

**Constraints:**
- Primary Key: `file_id`
- Foreign Key: `file_id` → `files.file_id`
- Index: `idx_upload_status` on `status`
- Index: `idx_upload_updated_at` on `updated_at`

---

#### `file_mirrors`
Records mirroring status to external storage providers.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-increment primary key |
| file_id | VARCHAR (FK) | References files.file_id |
| provider_name | VARCHAR | Name of the mirror provider |
| status | VARCHAR | Mirror status (PENDING, COMPLETED, FAILED) |
| external_url | VARCHAR | URL on the external provider |
| error_message | VARCHAR | Error details if failed |
| mirrored_at | TIMESTAMP | Time of successful mirror |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

**Constraints:**
- Primary Key: `id`
- Foreign Key: `file_id` → `files.file_id`

---

### Database Schema Diagram

> **Note:** Placeholder for visual database schema diagram. A detailed ER diagram showing relationships, constraints, and indexes can be added here.

---

## 📨 Event Model

The service uses Apache Kafka for asynchronous, event-driven communication with other microservices.

### Event Types

#### 1. FileUploadEvent

Published when a file upload is successfully completed.

**Topic:** `file_upload` (configurable via `mirror.kafka.file-upload-topic`)

**Event Structure:**
```json
{
  "eventId": "event-uuid-123",
  "fileId": "file-uuid-456",
  "fileName": "example.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 1048576,
  "s3Bucket": "amzn-file-mirror-bucket",
  "s3Key": "uploads/file-uuid-456_example.pdf",
  "s3Url": "https://bucket.s3.region.amazonaws.com/uploads/...",
  "checksum": "abc123...",
  "createdAt": "2026-02-14T04:30:00Z",
  "version": 1
}
```

**Purpose:** Notifies downstream services that a new file is available for processing, mirroring, or analysis.

**Consumers:** Mirror services, analytics services, notification services

---

#### 2. FileMirrorCheckEvent

Published to request mirroring of a file to a specific provider.

**Topic:** `file_mirror_check` (configurable via `mirror.kafka.file-mirror-check-topic`)

**Event Structure:**
```json
{
  "fileId": "file-uuid-456",
  "providerName": "cloudflare",
  "s3Bucket": "amzn-file-mirror-bucket",
  "s3Key": "uploads/file-uuid-456_example.pdf"
}
```

**Purpose:** Triggers mirror services to copy the file from S3 to external providers.

**Consumers:** Provider-specific mirror services

---

#### 3. FileMirroredEvent (Consumed)

Consumed from external mirror services when mirroring completes or fails.

**Topic:** `file_mirrored` (configurable via `mirror.kafka.file-mirrored-topic`)

**Event Structure:**
```json
{
  "fileId": "file-uuid-456",
  "providerName": "cloudflare",
  "status": "COMPLETED",
  "externalUrl": "https://cloudflare.example.com/files/abc123",
  "errorMessage": null,
  "mirroredAt": "2026-02-14T04:32:00Z"
}
```

**Purpose:** Updates the service with mirroring results from external providers.

**Processing:** Updates the `file_mirrors` table with the status and external URL.

---

### Event Flow Diagram

```
Upload Service                  Kafka                   Mirror Services
     │                           │                            │
     │──FileUploadEvent──────────>│                           │
     │                           │                            │
     │──FileMirrorCheckEvent─────>│────────────────────────>  │
     │                           │                            │
     │                           │                            │ (Process)
     │                           │                            │
     │                           │<──FileMirroredEvent───────│
     │<──────────────────────────│                           │
     │ (Update mirror status)    │                            │
```

---

### Kafka Configuration

**Producer Configuration:**
- Serializer: JSON (Spring Kafka JsonSerializer)
- Acknowledgment: `all` (ensures durability)
- Retries: 3 attempts

**Consumer Configuration:**
- Group ID: `file-mirror-status-group`
- Deserializer: JSON with trusted packages
- Auto-offset reset: `earliest`
- Error handling: Error-handling deserializer wrapper

---

## 🚦 Getting Started

### Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Apache Kafka 3.x
- AWS Account with S3 access
- Gradle (wrapper included)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/aniket-herle/upload.git
   cd upload
   ```

2. **Set up the database**
   ```sql
   CREATE DATABASE `file-mirror-upload`;
   ```

3. **Configure AWS credentials**
   
   Ensure AWS credentials are configured via environment variables or AWS credentials file:
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_REGION=ap-south-1
   ```

4. **Start Kafka**
   
   Ensure Kafka is running locally or update the configuration to point to your Kafka cluster.

5. **Build the project**
   ```bash
   ./gradlew build
   ```

6. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The service will start on `http://localhost:8081/api`

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/file-mirror-upload` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `S3_BUCKET` | AWS S3 bucket name | `amzn-file-mirror-bucket` |
| `AWS_REGION` | AWS region | `ap-south-1` |
| `SERVER_PORT` | Application server port | `8081` |

### Application Configuration

Key configuration properties in `application.yaml`:

```yaml
spring:
  application:
    name: S3 Upload Service
  
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  kafka:
    producer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      acks: all
      retries: 3
    consumer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      group-id: file-mirror-status-group

aws:
  s3:
    bucket: ${S3_BUCKET}
  region: ${AWS_REGION}

mirror:
  kafka:
    file-upload-topic: file_upload
    file-mirrored-topic: file_mirrored
    file-mirror-check-topic: file_mirror_check
```

---

## 📖 Usage Examples

### Complete Upload Flow

#### Step 1: Initialize Upload

```bash
curl -X POST http://localhost:8081/api/upload/init-upload \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "document.pdf",
    "contentType": "application/pdf",
    "sizeBytes": 2048576
  }'
```

**Response:**
```json
{
  "fileId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "uploadUrl": "https://amzn-file-mirror-bucket.s3.ap-south-1.amazonaws.com/uploads/...",
  "s3Key": "uploads/a1b2c3d4-e5f6-7890-abcd-ef1234567890_document.pdf"
}
```

#### Step 2: Upload File to S3

Use the presigned URL to upload the file directly to S3:

```bash
curl -X PUT "https://amzn-file-mirror-bucket.s3.ap-south-1.amazonaws.com/uploads/..." \
  -H "Content-Type: application/pdf" \
  --data-binary @document.pdf
```

#### Step 3: Complete Upload

```bash
curl -X POST http://localhost:8081/api/upload/a1b2c3d4-e5f6-7890-abcd-ef1234567890/complete-upload \
  -H "Content-Type: application/json" \
  -d '{
    "checksum": "sha256:abc123..."
  }'
```

**Response:** `204 No Content`

---

### Check Mirror Status

```bash
curl http://localhost:8081/api/files/a1b2c3d4-e5f6-7890-abcd-ef1234567890/mirrors
```

**Response:**
```json
[
  {
    "providerName": "cloudflare",
    "status": "COMPLETED",
    "externalUrl": "https://cloudflare.example.com/files/xyz789",
    "errorMessage": null,
    "mirroredAt": "2026-02-14T04:32:15Z"
  },
  {
    "providerName": "backblaze",
    "status": "FAILED",
    "externalUrl": null,
    "errorMessage": "Connection timeout",
    "mirroredAt": null
  }
]
```

---

## 🔒 Security Considerations

- **Presigned URLs**: Time-limited (10 minutes) to prevent unauthorized access
- **HTTPS Only**: All S3 operations use HTTPS
- **Input Validation**: Request validation using Jakarta Validation
- **Error Handling**: Global exception handler prevents information leakage
- **Database Constraints**: Unique constraints prevent duplicate uploads
- **Kafka Security**: Configure SASL/SSL for production Kafka clusters

---



## 🎯 Future Enhancements

- [ ] Support for multipart uploads for large files
- [ ] Resume capability for interrupted uploads
- [ ] Additional storage provider integrations
- [ ] File versioning support
- [ ] Advanced analytics and monitoring
- [ ] CDN integration for faster access
- [ ] Webhook notifications for upload events
