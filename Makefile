SHELL        := /bin/bash
PROJETO      ?= oficina-mecanica
TAG          ?= $(shell git rev-parse --short HEAD 2>/dev/null || echo local)
COMPOSE      := docker compose
TODOS_PERFIS := --profile app --profile observabilidade

-include .env
export

.DEFAULT_GOAL := ajuda
.PHONY: ajuda env run up down logs smoke test verify cobertura image observabilidade

ajuda: ## Lista os alvos disponíveis
	@grep -hE '^[a-zA-Z_-]+:.*?## ' $(firstword $(MAKEFILE_LIST)) | awk 'BEGIN{FS=":.*?## "}{printf "  \033[36m%-16s\033[0m %s\n",$$1,$$2}'

env: ## Cria o .env a partir do .env.example
	@test -f .env || (cp .env.example .env && echo ".env criado: troque as senhas")

run: env ## Roda a aplicação local com o profile dev (o Postgres sobe pelo docker compose)
	./mvnw spring-boot:run

up: env ## Sobe banco e aplicação em containers
	$(COMPOSE) --profile app up --build -d

observabilidade: env ## Sobe aplicação, Prometheus (9090), Loki (3100) e Grafana (3000)
	APP_PROFILES=dev,loki $(COMPOSE) --profile observabilidade up --build -d

down: ## Para todos os containers do projeto
	$(COMPOSE) $(TODOS_PERFIS) down

logs: ## Acompanha os logs da aplicação
	$(COMPOSE) logs -f app

smoke: ## Smoke test da aplicação no ar (BASE_URL=http://localhost:8080 por padrão)
	BASE_URL=$${BASE_URL:-http://localhost:8080} bash scripts/smoke-test.sh

test: ## Testes unitários e de integração
	./mvnw -B test

verify: ## Testes e cobertura mínima de 80% em domain e application
	./mvnw -B verify

cobertura: verify ## Gera o relatório de cobertura
	@echo "Relatório: target/site/jacoco/index.html"

image: ## Constrói a imagem Docker
	docker build -t $(PROJETO):$(TAG) .
