![Build Status](https://github.com/martinProg123/dropbox-clone/actions/workflows/build-test.yml/badge.svg)
# React + java spring boot + minio(object storage) + rabbitMQ + postgresql

## Quick start
```
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d postgres-dev rabbitmq minio
cd frontend && npm run dev
./run-backend.sh
or
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d
cd frontend && npm run dev

```
## Features:
- Usual auth flow(login, logout) via jwt cookie
- upload file
- checksum deduplication and tika extract text via message queue
- full text search via postgresql
- file sharing
- crud of file

## Flow:
### Upload
1. user upload file with size and pre cal checksum
2. server verify checksum in db, if exist, point to pre-exist object, no need to upload
3. if not, return presigned url (put) of minio to user, let user directly upload to it
4. if upload complete, send status(UPLOADED) to server.
5. Server receive status, update status(PROCESSING), sent message to rabbitMQ.
6. worker listen for message in queue
7. worker download file, then re-calculate checksum 
8. if checksum exist in db, update status(COMPLETE), return
9. if not, extract text via tika for full-text search, update status(COMPLETE)
10. react polling for upload state = COMPLETE

### Delete
1. only delete db entry
2. schedule job for clean up unreferenced object in minio
3. or On demand clean up: curl -X POST http://localhost:8080/api/admin/cleanup

## Api endpoint
see [controller](/backend/src/main/java/com/example/dropbox/controller/)


## DB schema:
```
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

```
## Todo:
https://www.freecodecamp.org/news/how-to-use-postgresql-as-a-cache-queue-and-search-engine/#heading-benchmark-3-full-text-search-with-tsvector

