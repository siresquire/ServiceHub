# =============================================================================
# ServiceHub — Makefile
# =============================================================================
# Common commands for local development. Run "make help" to see all targets.
#
# Prerequisites:
#   - Docker and Docker Compose installed
#   - (Optional) JDK 17 for running Maven commands outside Docker
#
# Quick Start:
#   make setup   # First-time setup (copies .env.example → .env)
#   make up      # Start all services
#   make logs    # Tail the backend logs
# =============================================================================

.PHONY: help setup up down build rebuild logs test clean db-shell status etl

# Default target: show help
help: ## Show this help message
	@echo.
	@echo  ServiceHub — Available Commands
	@echo  ================================
	@echo.
	@echo  SETUP
	@echo    make setup       Copy .env.example to .env (first-time only)
	@echo.
	@echo  DOCKER
	@echo    make up          Start all services (detached)
	@echo    make down        Stop all services
	@echo    make build       Build without cache
	@echo    make rebuild     Full clean rebuild (removes volumes too)
	@echo    make status      Show running containers and health
	@echo.
	@echo  DEVELOPMENT
	@echo    make logs        Tail backend logs
	@echo    make test        Run backend tests via Maven
	@echo    make db-shell    Open PostgreSQL interactive shell
	@echo    make etl         Run the data engineering ETL pipeline
	@echo.
	@echo  CLEANUP
	@echo    make clean       Stop containers and remove volumes
	@echo.

# ---------------------------------------------------------------------------
# Setup
# ---------------------------------------------------------------------------

setup: ## First-time setup — creates .env from template
	@if not exist .env ( \
		copy .env.example .env && \
		echo [OK] Created .env from .env.example. Edit it if needed. \
	) else ( \
		echo [SKIP] .env already exists. Delete it first to regenerate. \
	)

# ---------------------------------------------------------------------------
# Docker Compose Commands
# ---------------------------------------------------------------------------

up: ## Start all services in detached mode
	docker-compose up --build -d
	@echo.
	@echo [OK] ServiceHub is starting up...
	@echo   Application:  http://localhost:8080
	@echo   Swagger UI:   http://localhost:8080/swagger-ui.html
	@echo   Health Check: http://localhost:8080/actuator/health
	@echo.

down: ## Stop all services
	docker-compose down
	@echo [OK] All services stopped.

build: ## Build all Docker images without cache
	docker-compose build --no-cache

rebuild: ## Full clean rebuild — stops containers, removes volumes, rebuilds
	docker-compose down -v --remove-orphans
	docker-compose build --no-cache
	docker-compose up -d
	@echo [OK] Full rebuild complete.

status: ## Show container status and health
	docker-compose ps

# ---------------------------------------------------------------------------
# Development Helpers
# ---------------------------------------------------------------------------

logs: ## Tail backend application logs
	docker-compose logs -f backend

test: ## Run backend unit tests (requires JDK 17 locally)
	cd backend && mvn test

db-shell: ## Open an interactive PostgreSQL shell
	docker exec -it servicehub-db psql -U servicehub -d servicehub

etl: ## Run the data engineering ETL pipeline
	docker-compose run --rm data-engineering

# ---------------------------------------------------------------------------
# Cleanup
# ---------------------------------------------------------------------------

clean: ## Full cleanup — stop containers, remove volumes and orphans
	docker-compose down -v --remove-orphans
	@echo [OK] All containers, volumes, and orphans removed.
