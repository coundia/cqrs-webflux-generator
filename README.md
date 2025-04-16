# Spring Webflux Code Generator

Ce projet est un **générateur de code** basé sur **Spring Boot**, **Spring WebFlux**, **R2DBC**, Il permet de générer automatiquement des composants applicatifs selon les principes **DDD** et **CQRS**, sans utiliser Axon.

## 🚀 Objectif

Automatiser la création de code backend structuré en couches Domain / Application / Infrastructure / Presentation en exploitant :

- Spring WebFlux pour un modèle non-bloquant
- R2DBC pour les projections réactives
- SSE pour la diffusion en temps réel
- Mustache pour la génération de code

## 💠 Stack Technique

- Spring Boot 3+
- Spring WebFlux
- Spring R2DBC
- PostgreSQL
- Mustache (template engine)
- Swagger/OpenAPI
- Java 17+

## 📦 Architecture Générée

```
src/
├── domain/
│   └── model/
│   └── command/
│   └── event/
├── application/
│   └── service/
│   └── handler/
├── infrastructure/
│   └── repository/
│   └── config/
├── presentation/
│   └── controller/
│   └── sse
```

## 🔄 Fonctionnalités Générées

- CommandController (Create, Update, Delete)
- QueryController (FindById, Paginated, Filtered)
- SSEController (Streaming temps réel)
- DTO, Mapper, Repository, Entity
- Tests unitaires pour chaque use case

## ⚙️ Utilisation

```bash
POST http://127.0.0.1:8070/api/v1/generator/all
Accept: application/x-ndjson
Content-Type: application/json

{
    "outputDir": "/Users/pcoundia/projects/cqrs-rabbitmq-webflux-starter/src/main/java/com/pcoundia/transactions",
    "definition": {
      "name": "Transaction",
      "table": "transactions",
      "fields": [
        { "name": "id", "type": "String" },
        { "name": "reference", "type": "String" },
        { "name": "amount", "type": "Double" }
      ]
    }
  }
```

## 📱 Domaines d'application

- Systèmes temps réel
- Backends orientés microservices
- Dashboards de monitoring
- Applications SaaS back-office


Voir le projet généré : https://github.com/coundia/cqrs-webflux-starter
 
