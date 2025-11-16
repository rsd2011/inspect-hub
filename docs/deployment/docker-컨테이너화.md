# Docker 컨테이너화

## Backend Dockerfile

```dockerfile
# backend/Dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle 캐싱 최적화
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY common/build.gradle.kts common/
COPY server/build.gradle.kts server/

# 의존성 다운로드 (캐시 레이어)
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드
COPY common common
COPY server server
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# 보안 설정
RUN addgroup -g 1000 inspecthub && \
    adduser -u 1000 -G inspecthub -s /bin/sh -D inspecthub

WORKDIR /app

# JAR 복사
COPY --from=builder /app/server/build/libs/*.jar app.jar

# 로그 디렉토리
RUN mkdir -p /var/log/inspecthub && \
    chown -R inspecthub:inspecthub /var/log/inspecthub

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# 사용자 전환
USER inspecthub

# JVM 옵션
ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/inspecthub"

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# 의존성 설치 (캐시 레이어)
COPY package*.json ./
RUN npm ci --only=production

# 소스 복사 및 빌드
COPY . .
RUN npm run build

# Stage 2: Nginx
FROM nginx:1.25-alpine

# Nginx 설정 복사
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 빌드된 정적 파일 복사
COPY --from=builder /app/.output/public /usr/share/nginx/html

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

## Nginx 설정

```nginx
# frontend/nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip 압축
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss 
               application/rss+xml font/truetype font/opentype 
               application/vnd.ms-fontobject image/svg+xml;

    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=general:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=api:10m rate=100r/s;

    include /etc/nginx/conf.d/*.conf;
}
```

```nginx
# frontend/default.conf
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://api.inspecthub.com;" always;

    # Health Check Endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # Static Assets (with aggressive caching)
    location /_nuxt/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    location ~* \.(ico|css|js|gif|jpeg|jpg|png|woff|woff2|ttf|svg|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # SPA Routing (모든 요청을 index.html로)
    location / {
        limit_req zone=general burst=20 nodelay;
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    # API Proxy (선택사항 - 백엔드 직접 연결 시 불필요)
    # location /api/ {
    #     limit_req zone=api burst=200 nodelay;
    #     proxy_pass http://backend-service:8090;
    #     proxy_set_header Host $host;
    #     proxy_set_header X-Real-IP $remote_addr;
    #     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    #     proxy_set_header X-Forwarded-Proto $scheme;
    #     proxy_connect_timeout 60s;
    #     proxy_send_timeout 60s;
    #     proxy_read_timeout 60s;
    # }

    # 에러 페이지
    error_page 404 /index.html;
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

## Docker Compose (로컬 개발)

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: inspecthub-postgres
    environment:
      POSTGRES_DB: inspecthub_dev
      POSTGRES_USER: inspecthub
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/database/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - inspecthub-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U inspecthub"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: inspecthub-redis
    command: redis-server --requirepass dev_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - inspecthub-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: inspecthub-kafka
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    networks:
      - inspecthub-net

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: inspecthub-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - inspecthub-net

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: inspecthub-backend
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: inspecthub_dev
      DB_USERNAME: inspecthub
      DB_PASSWORD: dev_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: dev_password
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8090:8090"
    volumes:
      - ./logs:/var/log/inspecthub
    networks:
      - inspecthub-net
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8090/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: inspecthub-frontend
    depends_on:
      - backend
    ports:
      - "3000:80"
    networks:
      - inspecthub-net
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:80/health"]
      interval: 30s
      timeout: 3s
      retries: 3

volumes:
  postgres_data:
  redis_data:

networks:
  inspecthub-net:
    driver: bridge
```

## 이미지 빌드 및 푸시

```bash
# 이미지 빌드
docker build -t inspecthub/backend:1.0.0 ./backend
docker build -t inspecthub/frontend:1.0.0 ./frontend

# 태그 추가
docker tag inspecthub/backend:1.0.0 inspecthub/backend:latest
docker tag inspecthub/frontend:1.0.0 inspecthub/frontend:latest

# Docker Registry 푸시
docker push inspecthub/backend:1.0.0
docker push inspecthub/backend:latest
docker push inspecthub/frontend:1.0.0
docker push inspecthub/frontend:latest
```

---
