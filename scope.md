# Dropbox Clone - Project Scope

## Features
| Feature | Description |
|---------|-------------|
| JWT Auth | Register + Login with HttpOnly cookie |
| File Upload | Pre-signed URL (text/pdf, < 50MB), drag & drop |
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
    url TEXT,
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
- Dev: `docker-compose -f docker-compose.dev.yml up --build`
- Prod: `docker-compose up --build`
- Use .env.dev for dev, .env.prod for prod

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
