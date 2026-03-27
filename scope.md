# Dropbox Clone - Project Scope

## Features
| Feature | Description |
|---------|-------------|
| JWT Auth | Register + Login with HttpOnly cookie |
| File Upload | Pre-signed URL (text/pdf, < 50MB), drag & drop |
| Object Storage | MinIO with object_key (not URL), format: users/{userId}/{uuid}_{filename} |
| Tika Worker | Extract text + mimetype |
| Checksum Worker | SHA-256 for deduplication |
| Full-Text Search | PostgreSQL tsvector + GIN index |
| Real-time Updates | SSE for processing status |
| File Sharing | Generate public share link |
| Download | Via share link or owner |
| Delete | Owner can delete own files |
| Logout | Clear HttpOnly cookie |

## API Endpoints
```
POST   /api/auth/register         # Register
POST   /api/auth/login            # Login (returns HttpOnly cookie)
POST   /api/auth/logout           # Clear cookie

POST   /api/upload/init           # Get fileId + presigned URL
PUT    /api/upload/{fileId}       # Upload to MinIO
POST   /api/upload/complete       # Enqueue to RabbitMQ

GET    /api/files                 # User's files + details
GET    /api/files/{id}/events    # SSE status updates
DELETE /api/files/{id}           # Delete own file

POST   /api/files/{id}/share     # Enable sharing → returns share URL
DELETE /api/files/{id}/share     # Disable sharing

GET    /api/share/{token}        # Public download via share link

GET    /api/search?q=...         # Full-text search (owner only)
```

## Controllers & Services
| Controller | Service | Purpose |
|------------|---------|---------|
| **AuthController** | AuthService | Login, Register, Logout |
| **UploadController** | UploadService | Init, Complete |
| **FileController** | FileService | List, Delete, Share, Download, Events, Search |
| **ShareController** | ShareService | Public download via token |

## Database Schema
```sql
CREATE TYPE upload_status AS ENUM ('UPLOADING', 'UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED');

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE file_metadata (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    filename TEXT NOT NULL,
    file_type TEXT,
    file_size BIGINT,
    object_key TEXT,
    extracted_text TEXT,
    checksum TEXT,
    status upload_status DEFAULT 'UPLOADING',
    is_shared BOOLEAN DEFAULT false,
    share_token UUID UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_extracted_text_fts ON file_metadata 
    USING GIN(to_tsvector('english', extracted_text));
```

## Docker Services
```
postgres    # Database
rabbitmq    # Message queue + DLQ
minio       # Object storage (S3)
backend     # Spring Boot
frontend    # React + Vite + Tailwind + shadcn/ui
```

## Docker Workflow
- Dev: `docker compose -f docker-compose.dev.yml --env-file .env.dev up -d`
- Prod: `docker compose -f docker-compose.yml --env-file .env.prod up -d`
- Use .env.dev for dev, .env.prod for prod

## Upload Flow
```
1. Init: POST /api/upload/init
   - Client sends: fileName, fileSize
   - Backend validates size (≤50MB)
   - Generate objectKey: users/{userId}/{uuid}_{filename}
   - Create FileMetadata (status=UPLOADING)
   - Generate presigned PUT URL from MinIO
   - Return: { fileId, presignedUrl }

2. Upload: Client → MinIO (direct via presigned URL)
   - PUT request with file data

3. Complete: POST /api/upload/complete
   - Verify upload (optional: check actual size)
   - Send message to RabbitMQ queue
   - Update status: PROCESSING
```

## Worker Flow
```
Upload → MinIO → RabbitMQ → Worker
                           → Tika extract text → Update DB
                           → Generate checksum → Update DB
                           → SSE emit COMPLETED
```

## Worker Retry Logic
```
- Retry 3 times with exponential backoff
- After max retries → Dead Letter Queue
- Log failures for monitoring
```

## Time Estimate
~30-40 hours (1 week @ 4-6h/day)
