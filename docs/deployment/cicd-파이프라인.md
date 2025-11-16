# CI/CD 파이프라인

## GitHub Actions Workflow

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
