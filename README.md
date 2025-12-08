# Backend dev technical test — Updated

## Description
This application exposes a REST API that returns **product details for similar products**, according to the agreed contract.
It is represented by yourApp in the current schema

![Diagram](./assets/diagram.jpg "Diagram")


- Main API: `GET /{productId}/similar`
- Response: `200 OK` with a list of `ProductDetail` or `404` if the product does not exist.

It consumes two existing external APIs (see `existingApis.yaml`):
- `/product/{id}/similarids`
- `/product/{id}`

The application has been redesigned to use **hexagonal architecture**, **reactive WebClient**, **timeouts**, **retries**, and optimizations validated with **k6 performance tests**.

The architecture follows **hexagonal principles**:
- **Domain**: business logic
- **Ports**: interfaces
- **Adapters**: concrete implementations (controllers, WebClient configuration)
- **Tests**: unit, integration, and resilience (timeout, retry, concurrency)
---

## **Setup and Running**

### Prerequisites
- Java `17+`
- Spring Boot `3.5.8`
- Docker (for performance testing)

### Running the Application
You can run the same test we will put through your application. You just need to have docker installed.

First of all, you may need to enable file sharing for the `shared` folder on your docker dashboard -> settings -> resources -> file sharing.

Then you can start the mocks and other needed infrastructure with the following command.
```
docker-compose up -d simulado influxdb grafana
```
Check that mocks are working with a sample request to [http://localhost:3001/product/1/similarids](http://localhost:3001/product/1/similarids).

Then you need to install dependencies, build and execute the application (required Maven installed).

```bash
mvn clean install
```

Build the aplicación.

```bash
mvn package
```
Execute the la applicación.

```bash
java -jar target/similarproducts-1.0.0.jar
```

To execute the test run:
```
docker-compose run --rm k6 run scripts/test.js
```
Browse [http://localhost:3000/d/Le2Ku9NMk/k6-performance-test](http://localhost:3000/d/Le2Ku9NMk/k6-performance-test) to view the results.
