# syntax=docker/dockerfile:1

# ---- Build stage ------------------------------------------------------
# Builds the full reactor (riot-jooq must compile before riot, per CLAUDE.md).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy POMs first so dependency resolution is cached across source-only changes.
COPY pom.xml ./
COPY riot-jooq/pom.xml riot-jooq/pom.xml
COPY riot/pom.xml riot/pom.xml
RUN mvn -B -q -pl riot-jooq,riot dependency:go-offline || true

# jOOQ codegen reads this DDL at build time (gitignored — must exist in the
# build context; it is NOT a secret, just schema, unlike the rest of conf/).
COPY conf/database/database.sql conf/database/database.sql

COPY riot/src riot/src

RUN mvn -B -q clean package

# ---- Runtime stage ------------------------------------------------------
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

COPY --from=build /build/riot/target/riot.jar ./riot.jar
COPY --from=build /build/riot/target/dependency ./dependency

# conf/ is deliberately NOT baked into this image: conf/jwt/jwt.properties,
# conf/particle/*.properties etc. hold real secrets and are gitignored.
# Utilities.getResourceBundleFromFile reads conf/ relative to the working
# directory at runtime, so mount your local conf/ over /app/conf instead
# (see docker-compose.yml) rather than adding a COPY for it here.

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "riot.jar"]
