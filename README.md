# OmniSync - Enterprise Microservices Architecture

### Overview

OmniSync is a hands-on portfolio project I built to master enterprise-grade distributed systems. While it takes the shape of an e-commerce platform (handling inventory, orders, and payments), the application logic is really just the vehicle. The true focus of this project is the infrastructure.

I built this as a deep dive into modern backend engineering, bridging the gap between theoretical microservice concepts and production-ready implementation. It serves as my practical sandbox for exploring event-driven asynchronous messaging, centralized observability (tracing and logging), and robust identity management.

Right now, the stack is configured to run locally via Docker Compose, but it is architected with the ultimate goal of orchestrating the stateless compute layers in Kubernetes while plugging into cloud-managed stateful services.

### Architecture & Tech Stack
* Backend Framework: Java, Spring Boot, Spring Cloud

* API Gateway: Spring Cloud Gateway

* Service Discovery: Netflix Eureka

* Identity & Access Management (IAM): Keycloak (OAuth2 / OpenID Connect)

* Event-Driven Messaging: Apache Kafka (Asynchronous inter-service communication)

* Database: PostgreSQL (Per-service database pattern)

* Distributed Tracing: Jaeger

* Centralized Logging: ELK Stack (Elasticsearch, Logstash, Kibana)

* Containerization & Orchestration: Docker, Docker Compose

* CI/CD: GitHub Actions (Automated image build and push to Docker Hub)

### Local Development Setup
To run this complete architecture locally, you only need Docker and Docker Compose installed on your machine. The local environment relies on containerized versions of the infrastructure (Postgres, Kafka, Keycloak, ELK, Jaeger) and builds the Spring Boot microservices alongside them.

### Prerequisites
* Docker & Docker Desktop (or equivalent)
* Git

### Instructions to Run
1. Clone the repository
```bash
git clone https://github.com/Tijovanth/OmnisyncEcommerce.git
cd OmnisyncEcommerce
```

2. Start the infrastructure and microservices
Run the following command at the root of the project where your __docker-compose.yaml__ is located. This will download the necessary base images, compile the Java services, and boot up the entire network in detached mode.
```bash
docker-compose up -d
```
> [!NOTE]
> The initial boot may take a few minutes as Docker downloads the heavy infrastructure images (Kafka, ELK, Postgres) and compiles the Spring Boot applications.

3. Verify the deployment
Check the status of your containers to ensure all services are healthy and running:
```bash
docker-compose ps
```

### Service Ports & Dashboards

Once the local infrastructure is up and running, you can access the following services and dashboards:

| Service / Dashboard | Port(s) | Purpose |
| :--- | :--- | :--- |
| **OmniSync UI** | `3000` | Frontend client application interface. |
| **API Gateway** | `8080` | The single entry point and reactive router for all frontend/client requests. |
| **Inventory Service** | `8081` | Core business microservice managing product stock and availability. |
| **Order Service** | `8082` | Core business microservice processing customer checkout and order lifecycles. |
| **Payments Service** | `8084` | Core business microservice handling transaction validation. |
| **Keycloak Admin Portal** | `8085` | IAM dashboard to manage security realms, users, and OAuth2 clients. |
| **Eureka Server** | `8761` | Service registry dashboard to verify microservice discovery and health. |
| **Kafka Cluster** | `9092`, `9094`, `9096` | Event brokers for asynchronous messaging between decoupled services. |
| **PostgreSQL DB** | `5433` | Relational database handling persistent state. |
| **Jaeger UI** | `16686` | Distributed tracing dashboard to visualize request lifecycles across services. |
| **Kibana UI** | `5601` | Log visualization platform to search and filter aggregated application logs. |


### CI/CD Pipeline
This repository uses GitHub Actions for Continuous Integration. Upon every push to the main branch, the pipeline automatically:

* Checks out the code.

* Sets up the JDK environment.

* Authenticates with Docker Hub using securely stored repository secrets.

* Builds the container images for all microservices.

* Tags the images (using both latest and the immutable Git commit SHA) and pushes them to the remote Docker registry, ready for deployment to a Kubernetes environment.

### Teardown
To safely shut down the environment and remove the containers, networks, and default volumes:
```bash
docker compose down
```
> [!NOTE]
> (Add -v to the command if you also want to wipe the persistent data in PostgreSQL, Kafka, and Elasticsearch).
```bash
docker compose down -v
```
