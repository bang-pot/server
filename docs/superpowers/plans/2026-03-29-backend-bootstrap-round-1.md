# Backend Bootstrap Round 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a runnable backend bootstrap for BangPot using Spring Boot, Gradle, and PostgreSQL so later domain builders can start from an already-verified base.

**Architecture:** Start with a single Spring Boot application, a local PostgreSQL connection driven by environment variables, and profile-based JPA schema handling for local versus production usage. Expose only a health endpoint and create empty domain-first package boundaries for `auth`, `crew`, `explore`, and `meeting` without implementing domain behavior.

**Tech Stack:** Java 21, Spring Boot, Gradle, PostgreSQL, JUnit 5, GitHub Actions

---

### Task 1: Bootstrap the Gradle/Spring application

**Files:**
- Create: `build.gradle`
- Create: `settings.gradle`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `src/main/java/com/bangpot/BangpotApplication.java`
- Create: `src/test/java/com/bangpot/BangpotApplicationTests.java`

- [ ] **Step 1: Write the failing application-context test**
- [ ] **Step 2: Run the test to verify the application does not bootstrap yet**
- [ ] **Step 3: Add the minimal Spring Boot application and Gradle build files**
- [ ] **Step 4: Run the targeted test again and make it pass**
- [ ] **Step 5: Commit the bootstrap skeleton**

### Task 2: Add database and runtime configuration

**Files:**
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Create: `.env.example`

- [ ] **Step 1: Write the failing configuration test for the health endpoint or startup path that depends on the datasource config**
- [ ] **Step 2: Run the test to verify it fails for the expected missing configuration reason**
- [ ] **Step 3: Add environment-driven datasource config for local and production profiles**
- [ ] **Step 4: Re-run the focused tests to verify startup behavior is green**
- [ ] **Step 5: Commit the runtime configuration**

### Task 3: Establish backend structure, docs, and CI

**Files:**
- Create: `src/main/java/com/bangpot/common/config/package-info.java`
- Create: `src/main/java/com/bangpot/common/error/package-info.java`
- Create: `src/main/java/com/bangpot/auth/domain/package-info.java`
- Create: `src/main/java/com/bangpot/auth/application/package-info.java`
- Create: `src/main/java/com/bangpot/auth/infrastructure/package-info.java`
- Create: `src/main/java/com/bangpot/auth/presentation/package-info.java`
- Create: `src/main/java/com/bangpot/crew/domain/package-info.java`
- Create: `src/main/java/com/bangpot/crew/application/package-info.java`
- Create: `src/main/java/com/bangpot/crew/infrastructure/package-info.java`
- Create: `src/main/java/com/bangpot/crew/presentation/package-info.java`
- Create: `src/main/java/com/bangpot/explore/domain/package-info.java`
- Create: `src/main/java/com/bangpot/explore/application/package-info.java`
- Create: `src/main/java/com/bangpot/explore/infrastructure/package-info.java`
- Create: `src/main/java/com/bangpot/explore/presentation/package-info.java`
- Create: `src/main/java/com/bangpot/meeting/domain/package-info.java`
- Create: `src/main/java/com/bangpot/meeting/application/package-info.java`
- Create: `src/main/java/com/bangpot/meeting/infrastructure/package-info.java`
- Create: `src/main/java/com/bangpot/meeting/presentation/package-info.java`
- Create: `src/main/java/com/bangpot/health/presentation/HealthController.java`
- Create: `src/test/java/com/bangpot/health/presentation/HealthControllerTest.java`
- Create: `.github/workflows/ci.yml`
- Create: `README.md`

- [ ] **Step 1: Write the failing web test for the health endpoint**
- [ ] **Step 2: Run the test to verify the endpoint does not exist yet**
- [ ] **Step 3: Add the minimal controller plus package skeletons**
- [ ] **Step 4: Add README and GitHub Actions to lock in local and CI verification commands**
- [ ] **Step 5: Run the full test/build flow and commit the round-1 backend bootstrap**
