#!/bin/bash

export DB_PASSWORD=dropbox123
export RABBITMQ_PASSWORD=dropbox123
export MINIO_USER=dropbox
export MINIO_PASSWORD=dropbox123
export JWT_SECRET=gtlWtwjKFAlzYBDMdPVYxOtcDPMcgaFWBRymOu87luU=

cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
