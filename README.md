# Product API — CI/CD Microservice

A production-style Spring Boot REST API packaged as a Docker container and deployed to Kubernetes. GitHub Actions runs tests, builds the image, publishes it to GitHub Container Registry, and applies the Kubernetes manifests.

## Stack

- Java 17, Spring Boot 3, Maven
- Spring Web, Validation, Actuator
- Docker (multi-stage build, non-root runtime user)
- GitHub Actions and GitHub Container Registry (GHCR)
- Kubernetes Deployment, Service, ConfigMap, health/readiness probes

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/products` | List products |
| `GET` | `/api/v1/products/{id}` | Find a product |
| `POST` | `/api/v1/products` | Create a product |
| `GET` | `/actuator/health` | Liveness health |
| `GET` | `/actuator/health/readiness` | Readiness health |

Create an item:

```bash
curl -X POST http://localhost:8080/api/v1/products \\
  -H 'Content-Type: application/json' \\
  -d '{"name":"Wireless Mouse","price":29.99}'
```

## Local development

Prerequisites: Java 17 and Maven 3.9+.

```bash
mvn clean verify
mvn spring-boot:run
```

Run in Docker:

```bash
docker build -t product-api:local .
docker run --rm -p 8080:8080 product-api:local
```

## Deploy to Kubernetes

First replace `ghcr.io/YOUR_GITHUB_USERNAME/product-api:latest` in `k8s/deployment.yaml`, then:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl -n product-api rollout status deployment/product-api
```

For a private GHCR image, create an `imagePullSecret` and add it to the Deployment.

## CI/CD setup

The workflow at `.github/workflows/ci-cd.yml` runs on `main` pushes and pull requests:

1. Compiles and runs tests.
2. Builds and pushes `ghcr.io/<owner>/product-api` on pushes to `main`.
3. Deploys when a cluster kubeconfig secret is configured.

To enable deployment, create a GitHub Actions secret named `KUBE_CONFIG_DATA` containing a base64-encoded kubeconfig:

```bash
base64 -i ~/.kube/config | tr -d '\\n'
```

The deploy job substitutes the built image tag into the deployment manifest and waits for rollout completion. For repository pushes, GHCR authentication uses GitHub's built-in `GITHUB_TOKEN`; ensure Actions has **Read and write** workflow permissions.
