# ==============================================================================
# FlexiFeed Studio - Monorepo Makefile
# ==============================================================================

# Default target executed when running `make` with no arguments
.DEFAULT_GOAL := help

# Terminal color codes for styling output
CYAN   := \033[36m
GREEN  := \033[32m
YELLOW := \033[33m
RESET  := \033[0m

.PHONY: help test build install launch server server-install server-build db-up db-down db-push db-seed db-studio clean all

##@ Documentation
help: ## Display this help message with available commands
	@echo ""
	@echo "  $(CYAN)FlexiFeed Studio$(RESET) - Developer Command Runner"
	@echo "  ================================================="
	@echo ""
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(GREEN)%-18s$(RESET) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(RESET)\n", substr($$0, 5) }' $(MAKEFILE_LIST)
	@echo ""

##@ Android Development
test: ## Run Android Unit Tests (28 tests across MVI, DI, & Repository)
	@echo "$(CYAN)Running Android Unit Tests...$(RESET)"
	@cd FlexiFeed && ./gradlew testDevDebugUnitTest

build: ## Assemble Android Dev-Debug APK
	@echo "$(CYAN)Building Dev-Debug APK...$(RESET)"
	@cd FlexiFeed && ./gradlew assembleDevDebug

install: ## Install Dev-Debug APK onto connected emulator or device
	@echo "$(CYAN)Installing Dev-Debug APK on device...$(RESET)"
	@cd FlexiFeed && ./gradlew installDevDebug

launch: ## Launch FlexiFeed application on connected device via ADB
	@echo "$(CYAN)Launching MainActivity on device...$(RESET)"
	@adb shell am start -n com.flexifeed.app.dev/com.flexifeed.app.MainActivity

##@ Node.js Server & Web Studio
server-install: ## Install Node.js server dependencies
	@echo "$(CYAN)Installing server dependencies...$(RESET)"
	@cd server && npm install

server-build: ## Build TypeScript SDUI server to dist/
	@echo "$(CYAN)Building TypeScript server...$(RESET)"
	@cd server && npm run build

server: ## Start the TypeScript SDUI Server and Web Studio (port 8080)
	@echo "$(GREEN)Starting FlexiFeed SDUI Server at http://localhost:8080...$(RESET)"
	@cd server && npm start

##@ Database (PostgreSQL & Drizzle)
db-up: ## Start local PostgreSQL container via Docker Compose
	@echo "$(CYAN)Starting local PostgreSQL container...$(RESET)"
	@docker compose up -d

db-down: ## Stop local PostgreSQL container
	@echo "$(YELLOW)Stopping local PostgreSQL container...$(RESET)"
	@docker compose down

db-push: ## Push Drizzle schema to PostgreSQL (auto-migrates tables)
	@echo "$(CYAN)Pushing schema to database via Drizzle Kit...$(RESET)"
	@cd server && npm run db:push

db-seed: ## Seed SDUI presets, screens, and settings into PostgreSQL
	@echo "$(CYAN)Seeding SDUI data into database...$(RESET)"
	@cd server && npm run db:seed

db-studio: ## Open Drizzle Studio visual database inspector in browser
	@echo "$(GREEN)Launching Drizzle Studio...$(RESET)"
	@cd server && npm run db:studio

##@ Maintenance & Automation
clean: ## Clean Gradle and build caches across monorepo
	@echo "$(YELLOW)Cleaning Gradle build outputs...$(RESET)"
	@cd FlexiFeed && ./gradlew clean
	@rm -rf FlexiFeed/app/build FlexiFeed/build

all: server-install test build ## Install dependencies, run unit tests, and build APK
	@echo "$(GREEN)All builds and tests completed successfully!$(RESET)"
