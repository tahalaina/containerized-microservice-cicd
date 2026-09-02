# Product API — CI/CD Microservice

A production-style Spring Boot REST API packaged as a Docker container and deployed to Kubernetes. GitHub Actions runs tests, builds and scans the image, publishes it to GitHub Container Registry, creates build provenance, and deploys the staging overlay.

## Stack

- Java 17, Spring Boot 3, Maven
- Spring Web, Validation, Actuator
- PostgreSQL, Spring Data JPA, Flyway migrations
- Prometheus metrics and OpenAPI UI (`/swagger-ui/index.html`)
- Docker (multi-stage build, non-root runtime user)
- GitHub Actions and GitHub Container Registry (GHCR)
- Kubernetes Kustomize overlays, HPA, PDB, NetworkPolicy, hardened pod settings, health probes

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

Run PostgreSQL locally, then start the API:

```bash
docker compose up -d
mvn spring-boot:run
```

Run the API image:

```bash
docker build -t product-api:local .
docker run --rm -p 8080:8080 product-api:local
```

## Deploy to Kubernetes

Create the database secret in each target namespace (do not commit credentials):

```bash
kubectl -n product-api-staging create secret generic product-api-db \\
  --from-literal=url='jdbc:postgresql://YOUR_POSTGRES_HOST:5432/products' \\
  --from-literal=username='products' \\
  --from-literal=password='REPLACE_ME'
```

Render or deploy an environment overlay:

```bash
kubectl kustomize k8s/overlays/staging
kubectl apply -k k8s/overlays/staging
kubectl -n product-api-staging rollout status deployment/product-api
```

For a private GHCR image, create an `imagePullSecret` and add it to the Deployment.

## CI/CD setup

The workflow at `.github/workflows/ci-cd.yml` runs on `main` pushes and pull requests:

1. Compiles, runs unit tests, and runs PostgreSQL Testcontainers integration tests when Docker is available.
2. Builds, scans, attests, and pushes `ghcr.io/<owner>/product-api` on pushes to `main`.
3. Deploys staging only when repository variable `DEPLOY_ENABLED` equals `true`.

To enable deployment, configure GitHub OIDC trust with your chosen cloud provider, then add that provider's login action in the marked workflow step. Keep the identity scoped to the staging namespace. GitHub's OIDC token replaces long-lived cloud credentials.

```bash
For production, add a protected GitHub `production` environment, a separate production deployment job, and required reviewer approval.
```

The deploy job injects the immutable image digest into its Kustomize overlay and waits for rollout completion. For repository pushes, GHCR authentication uses GitHub's built-in `GITHUB_TOKEN`; ensure Actions has **Read and write** workflow permissions.
