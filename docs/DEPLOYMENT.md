# Deployment Guide

> **Inspect-Hub 배포 가이드**
> 
> **Version**: 1.0  
> **Last Updated**: 2025-01-13  
> **Target**: Production Deployment (Docker + Kubernetes)

---

## 📚 목차

1. [배포 전략 개요](#배포-전략-개요)
2. [인프라 아키텍처](#인프라-아키텍처)
3. [환경 구성](#환경-구성)
4. [Docker 컨테이너화](#docker-컨테이너화)
5. [Kubernetes 배포](#kubernetes-배포)
6. [데이터베이스 마이그레이션](#데이터베이스-마이그레이션)
7. [CI/CD 파이프라인](#cicd-파이프라인)
8. [모니터링 및 로깅](#모니터링-및-로깅)
9. [백업 및 복구](#백업-및-복구)
10. [보안 설정](#보안-설정)
11. [롤백 절차](#롤백-절차)
12. [트러블슈팅](#트러블슈팅)

---

## 배포 전략 개요

### 배포 환경

| 환경 | 목적 | URL | 업데이트 주기 |
|------|------|-----|---------------|
| **Development** | 개발 및 테스트 | dev.inspecthub.local | 수시 |
| **Staging** | QA 및 검증 | staging.inspecthub.com | 주 1회 |
| **Production** | 운영 환경 | inspecthub.com | 월 2회 |

### 배포 방식

**Blue-Green Deployment**
- 무중단 배포
- 빠른 롤백 가능
- 트래픽 전환 방식

```
┌─────────────────────────────────────────┐
│           Load Balancer                 │
│         (Nginx/ALB)                     │
└─────────────┬───────────────────────────┘
              │
      ┌───────┴────────┐
      │                │
┌─────▼─────┐   ┌──────▼────┐
│   Blue    │   │   Green   │
│ (Current) │   │   (New)   │
│  v1.0.0   │   │  v1.1.0   │
└───────────┘   └───────────┘
```

**배포 단계:**
1. Green 환경에 새 버전 배포
2. Health Check 통과 확인
3. 트래픽 일부를 Green으로 전환 (Canary)
4. 모니터링 및 검증
5. 트래픽 100% Green으로 전환
6. Blue 환경 유지 (롤백 대비)

---

## 인프라 아키텍처

### 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      Internet                               │
└────────────────────────┬────────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │   CloudFlare CDN    │
              │   (SSL/TLS, DDoS)   │
              └──────────┬──────────┘
                         │
         ┌───────────────▼───────────────┐
         │  Load Balancer (AWS ALB)      │
         │  - SSL Termination            │
         │  - Health Check               │
         └───────────┬───────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼─────┐ ┌───▼──────┐
│  Frontend    │ │ Backend│ │ Backend  │
│  (Nginx)     │ │  Pod 1 │ │  Pod 2   │
│  Static SPA  │ │  (K8s) │ │  (K8s)   │
└──────────────┘ └────┬───┘ └───┬──────┘
                      │         │
                ┌─────┴─────────┴─────┐
                │                     │
         ┌──────▼──────┐       ┌─────▼──────┐
         │ PostgreSQL  │       │   Redis    │
         │  (Primary)  │       │  (Cluster) │
         │             │       │            │
         │  (Replica)  │       └────────────┘
         └─────────────┘
                │
         ┌──────▼──────┐
         │    Kafka    │
         │  (Cluster)  │
         └─────────────┘
```

### 컴포넌트 사양

| 컴포넌트 | 인스턴스 수 | CPU | 메모리 | 스토리지 |
|----------|-------------|-----|--------|----------|
| **Frontend (Nginx)** | 2 | 1 core | 512 MB | 10 GB |
| **Backend (Spring Boot)** | 3 | 2 cores | 4 GB | 20 GB |
| **PostgreSQL (Primary)** | 1 | 4 cores | 16 GB | 500 GB SSD |
| **PostgreSQL (Replica)** | 2 | 4 cores | 16 GB | 500 GB SSD |
| **Redis (Cluster)** | 3 | 2 cores | 8 GB | 50 GB |
| **Kafka (Cluster)** | 3 | 4 cores | 16 GB | 200 GB |

---

## 환경 구성

### 환경 변수 관리

**Backend (.env.production)**
```bash
# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8090
APP_NAME=inspect-hub
APP_VERSION=1.0.0

# Database
DB_HOST=postgres-primary.prod.svc.cluster.local
DB_PORT=5432
DB_NAME=inspecthub_prod
DB_USERNAME=inspecthub_app
DB_PASSWORD=${DB_PASSWORD}  # Secret Manager에서 주입
DB_POOL_SIZE=20
DB_POOL_MAX_SIZE=50

# Redis
REDIS_HOST=redis-cluster.prod.svc.cluster.local
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_POOL_SIZE=10

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka-0.kafka.prod.svc.cluster.local:9092,kafka-1.kafka.prod.svc.cluster.local:9092,kafka-2.kafka.prod.svc.cluster.local:9092
KAFKA_CONSUMER_GROUP=inspecthub-group

# Security
JWT_SECRET=${JWT_SECRET}  # 256-bit secret
JWT_ACCESS_TOKEN_EXPIRY=3600000   # 1 hour
JWT_REFRESH_TOKEN_EXPIRY=604800000  # 7 days

# Encryption
ENCRYPTION_KEY=${ENCRYPTION_KEY}  # AES-256 key
ENCRYPTION_ALGORITHM=AES/GCM/NoPadding

# External Services
FIU_API_URL=https://fiu.fss.or.kr/api/v1
FIU_API_KEY=${FIU_API_KEY}

# Monitoring
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INSPECTHUB=DEBUG
LOGGING_FILE_PATH=/var/log/inspecthub
LOGGING_FILE_MAX_SIZE=100MB
LOGGING_FILE_MAX_HISTORY=30
```

**Frontend (.env.production)**
```bash
# API
NUXT_PUBLIC_API_BASE=https://api.inspecthub.com/api/v1

# Application
NUXT_PUBLIC_APP_NAME=Inspect-Hub
NUXT_PUBLIC_APP_VERSION=1.0.0

# SSR (반드시 false)
NUXT_SSR=false

# Build
NODE_ENV=production
```

### Secrets 관리

**Kubernetes Secrets**
```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: inspecthub-secrets
  namespace: production
type: Opaque
stringData:
  db-password: "{{ DB_PASSWORD }}"
  redis-password: "{{ REDIS_PASSWORD }}"
  jwt-secret: "{{ JWT_SECRET }}"
  encryption-key: "{{ ENCRYPTION_KEY }}"
  fiu-api-key: "{{ FIU_API_KEY }}"
```

**생성 및 적용:**
```bash
# Base64 인코딩된 값으로 시크릿 생성
kubectl create secret generic inspecthub-secrets \
  --from-literal=db-password='your-db-password' \
  --from-literal=redis-password='your-redis-password' \
  --from-literal=jwt-secret='your-jwt-secret' \
  --from-literal=encryption-key='your-encryption-key' \
  --from-literal=fiu-api-key='your-fiu-api-key' \
  --namespace=production

# 또는 파일에서 로드
kubectl apply -f secrets.yaml
```

---

## Docker 컨테이너화

### Backend Dockerfile

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

### Frontend Dockerfile

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

### Nginx 설정

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

### Docker Compose (로컬 개발)

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

### 이미지 빌드 및 푸시

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

## Kubernetes 배포

### Namespace 생성

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
  labels:
    name: production
    environment: production
```

### Backend Deployment

```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: production
  labels:
    app: backend
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
        version: v1.0.0
    spec:
      containers:
      - name: backend
        image: inspecthub/backend:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8090
          name: http
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_HOST
          value: "postgres-primary.production.svc.cluster.local"
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "inspecthub_prod"
        - name: DB_USERNAME
          value: "inspecthub_app"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: inspecthub-secrets
              key: db-password
        - name: REDIS_HOST
          value: "redis-cluster.production.svc.cluster.local"
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: inspecthub-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: inspecthub-secrets
              key: jwt-secret
        - name: ENCRYPTION_KEY
          valueFrom:
            secretKeyRef:
              name: inspecthub-secrets
              key: encryption-key
        resources:
          requests:
            cpu: 1000m
            memory: 2Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8090
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8090
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /var/log/inspecthub
      volumes:
      - name: logs
        emptyDir: {}
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - backend
              topologyKey: kubernetes.io/hostname
---
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: production
spec:
  type: ClusterIP
  selector:
    app: backend
  ports:
  - port: 8090
    targetPort: 8090
    protocol: TCP
    name: http
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800  # 3 hours
```

### Frontend Deployment

```yaml
# k8s/frontend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: production
  labels:
    app: frontend
    version: v1.0.0
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
        version: v1.0.0
    spec:
      containers:
      - name: frontend
        image: inspecthub/frontend:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 80
          name: http
          protocol: TCP
        resources:
          requests:
            cpu: 250m
            memory: 256Mi
          limits:
            cpu: 500m
            memory: 512Mi
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 2
          failureThreshold: 3
---
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
  namespace: production
spec:
  type: ClusterIP
  selector:
    app: frontend
  ports:
  - port: 80
    targetPort: 80
    protocol: TCP
    name: http
```

### Ingress 설정

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: inspecthub-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - inspecthub.com
    - www.inspecthub.com
    - api.inspecthub.com
    secretName: inspecthub-tls
  rules:
  - host: inspecthub.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 80
  - host: www.inspecthub.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 80
  - host: api.inspecthub.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: backend-service
            port:
              number: 8090
```

### HorizontalPodAutoscaler (HPA)

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: frontend-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: frontend
  minReplicas: 2
  maxReplicas: 5
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### 배포 스크립트

```bash
#!/bin/bash
# deploy.sh

set -e

NAMESPACE="production"
VERSION=$1

if [ -z "$VERSION" ]; then
  echo "Usage: ./deploy.sh <version>"
  exit 1
fi

echo "🚀 Starting deployment for version $VERSION"

# 1. Namespace 생성 (없으면)
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# 2. Secrets 확인
if ! kubectl get secret inspecthub-secrets -n $NAMESPACE &> /dev/null; then
  echo "❌ Error: Secrets not found. Please create secrets first."
  exit 1
fi

# 3. 이미지 태그 업데이트
sed -i "s|image: inspecthub/backend:.*|image: inspecthub/backend:$VERSION|g" k8s/backend-deployment.yaml
sed -i "s|image: inspecthub/frontend:.*|image: inspecthub/frontend:$VERSION|g" k8s/frontend-deployment.yaml

# 4. 배포
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml

# 5. Rollout 상태 확인
echo "⏳ Waiting for backend rollout..."
kubectl rollout status deployment/backend -n $NAMESPACE --timeout=5m

echo "⏳ Waiting for frontend rollout..."
kubectl rollout status deployment/frontend -n $NAMESPACE --timeout=5m

# 6. Health Check
echo "🔍 Checking health..."
sleep 10

BACKEND_POD=$(kubectl get pod -n $NAMESPACE -l app=backend -o jsonpath="{.items[0].metadata.name}")
kubectl exec -n $NAMESPACE $BACKEND_POD -- wget -qO- http://localhost:8090/actuator/health

FRONTEND_POD=$(kubectl get pod -n $NAMESPACE -l app=frontend -o jsonpath="{.items[0].metadata.name}")
kubectl exec -n $NAMESPACE $FRONTEND_POD -- wget -qO- http://localhost:80/health

echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Deployment Status:"
kubectl get deployments -n $NAMESPACE
echo ""
kubectl get pods -n $NAMESPACE
```

---

## 데이터베이스 마이그레이션

### Flyway 설정

```groovy
// backend/build.gradle.kts
plugins {
    id("org.flywaydb.flyway") version "10.0.0"
}

flyway {
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/inspecthub_prod"
    user = System.getenv("DB_USERNAME") ?: "inspecthub_app"
    password = System.getenv("DB_PASSWORD") ?: ""
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
    validateOnMigrate = true
}
```

### 마이그레이션 스크립트 예시

```sql
-- backend/src/main/resources/db/migration/V1__initial_schema.sql

-- User 테이블
CREATE TABLE "user" (
  id VARCHAR(26) PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  org_id VARCHAR(26),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP,
  deleted_by VARCHAR(26)
);

CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_org_id ON "user"(org_id);
CREATE INDEX idx_user_status ON "user"(status);

-- Organization 테이블
CREATE TABLE organization (
  id VARCHAR(26) PRIMARY KEY,
  code VARCHAR(50) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  parent_id VARCHAR(26),
  org_type VARCHAR(50) NOT NULL,
  level INTEGER NOT NULL,
  path VARCHAR(500),
  org_policy_id VARCHAR(26),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (parent_id) REFERENCES organization(id)
);

CREATE INDEX idx_org_code ON organization(code);
CREATE INDEX idx_org_parent_id ON organization(parent_id);
CREATE INDEX idx_org_path ON organization(path);

-- ... (더 많은 테이블)
```

```sql
-- V2__add_audit_log.sql
CREATE TABLE audit_log (
  id VARCHAR(26) PRIMARY KEY,
  user_id VARCHAR(26),
  username VARCHAR(50),
  action VARCHAR(100) NOT NULL,
  resource VARCHAR(100),
  resource_id VARCHAR(26),
  ip_address VARCHAR(45),
  user_agent TEXT,
  before_json JSONB,
  after_json JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- 월별 파티션 생성 (예시)
CREATE TABLE audit_log_2025_01 PARTITION OF audit_log
  FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
```

### 마이그레이션 실행

```bash
# 로컬 개발
./gradlew flywayMigrate

# 프로덕션 (환경 변수 설정)
DB_URL=jdbc:postgresql://prod-db:5432/inspecthub_prod \
DB_USERNAME=inspecthub_app \
DB_PASSWORD=prod_password \
./gradlew flywayMigrate

# 마이그레이션 정보 확인
./gradlew flywayInfo

# 마이그레이션 검증
./gradlew flywayValidate

# 롤백 (상용 라이선스 필요)
./gradlew flywayUndo
```

### 배포 전 마이그레이션 체크리스트

- [ ] 마이그레이션 스크립트 테스트 완료 (dev/staging)
- [ ] 롤백 시나리오 검증
- [ ] 데이터베이스 백업 완료
- [ ] 다운타임 필요 여부 확인
- [ ] 대용량 테이블 변경 시 온라인 DDL 사용
- [ ] 인덱스 생성 시 CONCURRENTLY 옵션 사용
- [ ] 마이그레이션 실행 권한 확인

---

## CI/CD 파이프라인

### GitHub Actions Workflow

```yaml
# .github/workflows/deploy-production.yml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*.*.*'  # v1.0.0 형태의 태그
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to deploy'
        required: true
        type: string

env:
  DOCKER_REGISTRY: docker.io
  DOCKER_USERNAME: inspecthub
  K8S_NAMESPACE: production

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set version
        id: version
        run: |
          if [ "${{ github.event_name }}" == "workflow_dispatch" ]; then
            echo "VERSION=${{ inputs.version }}" >> $GITHUB_OUTPUT
          else
            echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT
          fi
      
      # Backend Build
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'gradle'
      
      - name: Build Backend
        run: |
          cd backend
          ./gradlew clean bootJar
      
      - name: Run Backend Tests
        run: |
          cd backend
          ./gradlew test
      
      # Frontend Build
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Build Frontend
        run: |
          cd frontend
          npm ci
          npm run build
      
      - name: Run Frontend Tests
        run: |
          cd frontend
          npm run test:unit
      
      # Docker Build & Push
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Login to Docker Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.DOCKER_REGISTRY }}
          username: ${{ env.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and Push Backend Image
        uses: docker/build-push-action@v4
        with:
          context: ./backend
          push: true
          tags: |
            ${{ env.DOCKER_USERNAME }}/backend:${{ steps.version.outputs.VERSION }}
            ${{ env.DOCKER_USERNAME }}/backend:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max
      
      - name: Build and Push Frontend Image
        uses: docker/build-push-action@v4
        with:
          context: ./frontend
          push: true
          tags: |
            ${{ env.DOCKER_USERNAME }}/frontend:${{ steps.version.outputs.VERSION }}
            ${{ env.DOCKER_USERNAME }}/frontend:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set version
        id: version
        run: |
          if [ "${{ github.event_name }}" == "workflow_dispatch" ]; then
            echo "VERSION=${{ inputs.version }}" >> $GITHUB_OUTPUT
          else
            echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT
          fi
      
      - name: Setup kubectl
        uses: azure/setup-kubectl@v3
        with:
          version: 'v1.28.0'
      
      - name: Configure kubectl
        run: |
          echo "${{ secrets.KUBECONFIG }}" | base64 -d > kubeconfig.yaml
          export KUBECONFIG=kubeconfig.yaml
      
      - name: Database Migration
        run: |
          cd backend
          DB_URL=${{ secrets.DB_URL }} \
          DB_USERNAME=${{ secrets.DB_USERNAME }} \
          DB_PASSWORD=${{ secrets.DB_PASSWORD }} \
          ./gradlew flywayMigrate
      
      - name: Deploy to Kubernetes
        run: |
          export KUBECONFIG=kubeconfig.yaml
          
          # 이미지 태그 업데이트
          sed -i "s|image: inspecthub/backend:.*|image: inspecthub/backend:${{ steps.version.outputs.VERSION }}|g" k8s/backend-deployment.yaml
          sed -i "s|image: inspecthub/frontend:.*|image: inspecthub/frontend:${{ steps.version.outputs.VERSION }}|g" k8s/frontend-deployment.yaml
          
          # 배포
          kubectl apply -f k8s/backend-deployment.yaml
          kubectl apply -f k8s/frontend-deployment.yaml
          
          # Rollout 대기
          kubectl rollout status deployment/backend -n ${{ env.K8S_NAMESPACE }} --timeout=5m
          kubectl rollout status deployment/frontend -n ${{ env.K8S_NAMESPACE }} --timeout=5m
      
      - name: Health Check
        run: |
          export KUBECONFIG=kubeconfig.yaml
          
          # Backend Health Check
          BACKEND_POD=$(kubectl get pod -n ${{ env.K8S_NAMESPACE }} -l app=backend -o jsonpath="{.items[0].metadata.name}")
          kubectl exec -n ${{ env.K8S_NAMESPACE }} $BACKEND_POD -- wget -qO- http://localhost:8090/actuator/health
          
          # Frontend Health Check
          FRONTEND_POD=$(kubectl get pod -n ${{ env.K8S_NAMESPACE }} -l app=frontend -o jsonpath="{.items[0].metadata.name}")
          kubectl exec -n ${{ env.K8S_NAMESPACE }} $FRONTEND_POD -- wget -qO- http://localhost:80/health
      
      - name: Notify Success
        if: success()
        uses: 8398a7/action-slack@v3
        with:
          status: custom
          custom_payload: |
            {
              text: "✅ Deployment Successful",
              attachments: [{
                color: 'good',
                text: `Version ${{ steps.version.outputs.VERSION }} deployed to production`
              }]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
      
      - name: Notify Failure
        if: failure()
        uses: 8398a7/action-slack@v3
        with:
          status: custom
          custom_payload: |
            {
              text: "❌ Deployment Failed",
              attachments: [{
                color: 'danger',
                text: `Version ${{ steps.version.outputs.VERSION }} deployment failed`
              }]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## 모니터링 및 로깅

### Prometheus + Grafana

**Prometheus 설정**
```yaml
# k8s/prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s
    
    scrape_configs:
    - job_name: 'kubernetes-pods'
      kubernetes_sd_configs:
      - role: pod
      relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
        action: replace
        regex: ([^:]+)(?::\d+)?;(\d+)
        replacement: $1:$2
        target_label: __address__
    
    - job_name: 'backend'
      static_configs:
      - targets: ['backend-service.production.svc.cluster.local:8090']
      metrics_path: '/actuator/prometheus'
```

**Grafana 대시보드**
- JVM 메트릭 (힙 메모리, GC, 스레드)
- HTTP 요청 메트릭 (RPS, 응답 시간, 에러율)
- 데이터베이스 커넥션 풀
- 비즈니스 메트릭 (탐지 건수, 사례 처리 시간)

### ELK Stack (로그 수집)

**Filebeat 설정**
```yaml
# k8s/filebeat-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: monitoring
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - /var/log/containers/*.log
      processors:
      - add_kubernetes_metadata:
          host: ${NODE_NAME}
          matchers:
          - logs_path:
              logs_path: "/var/log/containers/"
    
    output.elasticsearch:
      hosts: ['elasticsearch.monitoring.svc.cluster.local:9200']
      index: "inspecthub-%{+yyyy.MM.dd}"
```

---

## 백업 및 복구

### 데이터베이스 백업

```bash
#!/bin/bash
# scripts/backup-database.sh

set -e

BACKUP_DIR="/var/backups/postgresql"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_NAME="inspecthub_prod"
DB_USER="inspecthub_app"
DB_HOST="postgres-primary.production.svc.cluster.local"

# Full Backup
pg_dump -h $DB_HOST -U $DB_USER -Fc -f "$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.dump" $DB_NAME

# Upload to S3
aws s3 cp "$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.dump" \
  s3://inspecthub-backups/database/${DB_NAME}_${TIMESTAMP}.dump

# 7일 이상 오래된 백업 삭제
find $BACKUP_DIR -name "*.dump" -mtime +7 -delete

echo "✅ Backup completed: ${DB_NAME}_${TIMESTAMP}.dump"
```

### 복구

```bash
#!/bin/bash
# scripts/restore-database.sh

BACKUP_FILE=$1
DB_NAME="inspecthub_prod"
DB_USER="inspecthub_app"
DB_HOST="postgres-primary.production.svc.cluster.local"

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: ./restore-database.sh <backup_file>"
  exit 1
fi

# S3에서 다운로드
aws s3 cp "s3://inspecthub-backups/database/$BACKUP_FILE" /tmp/

# 복구
pg_restore -h $DB_HOST -U $DB_USER -d $DB_NAME -c /tmp/$BACKUP_FILE

echo "✅ Database restored from $BACKUP_FILE"
```

---

## 보안 설정

### TLS/SSL 인증서

```bash
# Let's Encrypt 인증서 자동 갱신 (cert-manager)
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# ClusterIssuer 생성
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@inspecthub.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### Network Policies

```yaml
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-network-policy
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8090
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
```

---

## 롤백 절차

### Kubernetes Rollback

```bash
# 이전 버전으로 롤백
kubectl rollout undo deployment/backend -n production

# 특정 리비전으로 롤백
kubectl rollout undo deployment/backend --to-revision=2 -n production

# Rollout 히스토리 확인
kubectl rollout history deployment/backend -n production
```

### 데이터베이스 롤백

```bash
# Flyway undo (상용 라이선스 필요)
./gradlew flywayUndo

# 또는 수동 롤백 스크립트 실행
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f rollback/V2__undo_add_audit_log.sql
```

---

## 트러블슈팅

### Pod가 시작되지 않음

```bash
# Pod 상태 확인
kubectl get pods -n production

# 상세 정보
kubectl describe pod <pod-name> -n production

# 로그 확인
kubectl logs <pod-name> -n production

# 이전 컨테이너 로그 (CrashLoopBackOff)
kubectl logs <pod-name> -n production --previous
```

### 데이터베이스 연결 실패

```bash
# 연결 테스트
kubectl exec -it <backend-pod> -n production -- \
  psql -h postgres-primary.production.svc.cluster.local -U inspecthub_app -d inspecthub_prod

# DNS 확인
kubectl exec -it <backend-pod> -n production -- \
  nslookup postgres-primary.production.svc.cluster.local
```

### 메모리 부족 (OOMKilled)

```bash
# 메모리 사용량 확인
kubectl top pod -n production

# 리소스 한도 증가
kubectl set resources deployment/backend \
  --limits=memory=8Gi \
  -n production
```

---

## 참고 자료

### 내부 문서
- [Backend README](./backend/README.md)
- [Frontend README](./frontend/README.md)
- [Architecture Overview](./docs/architecture/OVERVIEW.md)
- [Security Guide](./docs/architecture/SECURITY.md)
- [Database Design](./docs/architecture/DATABASE.md)

### 외부 리소스
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Prometheus Operator](https://prometheus-operator.dev/)
- [Grafana Documentation](https://grafana.com/docs/)

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|-----------|--------|
| 2025-01-13 | 최초 작성 | DevOps 팀 |

---

**문의사항이 있으시면 DevOps 팀으로 연락 주시기 바랍니다.**