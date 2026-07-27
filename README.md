# riot — REST IoT service with JWT, jOOQ and MariaDB

This project implements a JAX-RS based RESTful web service (Jersey, embedded Jetty, HK2) that
manages IoT devices via the [Particle Cloud API](https://docs.particle.io/reference/device-cloud/api/).
Endpoints are secured with token-based authentication: a client authenticates with a
username/password, receives a signed JWT, and presents it as a bearer token on subsequent calls
for authentication and authorization.

## Prerequisites

- Java 17
- Maven
- Docker & Docker Compose (recommended for local development, see below)

## Modules

The parent module `riot-root` contains two child modules:

- `riot-jooq` — jOOQ code generation settings, produces the generated DB access classes `riot` depends on
- `riot` — the application itself (REST resources, services, Jetty bootstrap)

`riot-jooq` must build before `riot` in the reactor, since `riot` depends on its generated sources.

## Configuration

A handful of files under `conf/` are gitignored because they hold local secrets/certificates,
and are never committed. `riot` won't start without them — `Utilities.getResourceBundleFromFile`
reads `conf/` relative to the process's working directory, so keep it alongside the jar/working
directory regardless of how you run the app. Create each of the following before your first run.

### 1. HTTPS keystore — `conf/jetty/keystore.p12` + `conf/jetty/jetty.properties`

`JettyServer` serves HTTPS only, so it needs a keystore. A self-signed one is fine for local dev:

```
mkdir -p conf/jetty
keytool -genkeypair -alias riot -keyalg RSA -keysize 2048 -validity 365 \
  -storetype PKCS12 -keystore conf/jetty/keystore.p12 -storepass <choose-a-password> \
  -dname "CN=localhost, OU=riot, O=riot, L=, ST=, C="
```

Then create `conf/jetty/jetty.properties`:

```
keystore.path=conf/jetty/keystore.p12
keystore.password=<the password you chose above>
```

### 2. JWT signing secret — `conf/jwt/jwt.properties`

`JWTProvider` signs tokens with HS256 using this secret. Any sufficiently random string works:

```
mkdir -p conf/jwt
echo "secret=$(openssl rand -base64 48)" > conf/jwt/jwt.properties
```

### 3. Particle Cloud credentials — `conf/particle/api_base.properties` + `conf/particle/client_secret.properties`

`ParticleClient`/`ParticleLoginServiceImpl` authenticate to the Particle Device Cloud as a single,
application-wide Particle account (an OAuth2 "password" grant) and proxy every `/riot/devices/*`
call through it — there's no per-`riot`-user Particle credential, just one shared account behind
the whole app.

`conf/particle/api_base.properties` — Particle's public endpoints, the same for everyone, so you
can copy this as-is:

```
mkdir -p conf/particle
cat > conf/particle/api_base.properties <<'EOF'
particle_api_base=https://api.particle.io
particle_login_base=https://api.particle.io/oauth/token
particle_devices_base=/v1/devices
EOF
```

`conf/particle/client_secret.properties` — your own Particle account plus an OAuth client
registered against it:

```
client_id=<your Particle OAuth client id>
client_secret=<your Particle OAuth client secret>
username=<the Particle account email that owns the devices>
password=<that account's password>
grant_type=password
```

To get `client_id`/`client_secret`: sign in at [console.particle.io](https://console.particle.io)
(create a free account if you don't have one — you can develop against Particle's simulated/test
devices without owning physical hardware), then register an OAuth client that supports the
`password` grant type — either through the Particle CLI (`particle login`, then the client-creation
command under `particle client`) or the console's API/OAuth settings. Particle's own device cloud
API docs (docs.particle.io) have the current step-by-step for this, since the exact console/CLI
flow has changed over time and isn't part of this codebase. `username`/`password` above are just
that Particle account's normal login, not anything OAuth-specific.

> `.gitignore` also lists `conf/particle/particle.properties`. That's a leftover from before this
> refactor — nothing in the current code reads it, so it doesn't need to exist.

### 4. jOOQ codegen DDL — `conf/database/database.sql`

Only read at build time, by `riot-jooq`'s jOOQ codegen (`DDLDatabase`, configured in
`riot-jooq/pom.xml`) to generate the DB access classes `riot` depends on. It's not shipped in the
jar or read at runtime, and it must describe the same schema the app runs against — the simplest
approach is to reuse the tracked schema DDL directly:

```
mkdir -p conf/database
cp riot/src/main/resources/sql/schema.sql conf/database/database.sql
```

### Already tracked

`conf/database.properties`, `conf/hikari.properties` and `conf/swagger.properties` are committed
and already configured for the Docker Compose setup below; adjust them if you're pointing at a
different database or environment.

## Build and run

### Docker Compose (recommended)

```
docker compose up --build
```

This builds the full reactor, starts a MariaDB container seeded from
`riot/src/main/resources/sql/{schema,data}.sql`, and starts `riot` on
`https://localhost:8443`. Your local `conf/` directory is mounted read-only into the container,
so the gitignored files above must already exist on the host first.

- API base: `https://localhost:8443/riot`
- Swagger UI: `https://localhost:8443/riot/docs`
- Login UI: `https://localhost:8443/riot`

### Local Maven

1. From the project root: `mvn clean package`
2. Run `com.project.iotrest.application.JettyServer#main`, or `java -jar riot/target/riot.jar`
   from a working directory that has `conf/` alongside it and a reachable database matching
   `conf/database.properties`.

## Seed users

The Docker Compose MariaDB seed (`riot/src/main/resources/sql/data.sql`) provisions demo users,
all with password `secret`, each isolating a different access right:

| username | email | rights |
| --- | --- | --- |
| john | admin@example.org | CREATE, READ, UPDATE, DELETE |
| jwick | admin@admin.org | READ |
| acreator | alice.creator@example.org | CREATE |
| bupdater | bob.updater@example.org | UPDATE |
| cdeleter | carol.deleter@example.org | DELETE |
| dnoaccess | dave.noaccess@example.org | none |

## REST APIs

All endpoints are Swagger-annotated; documentation is served at `/riot/docs`.

- `POST /riot/auth/login` — exchange credentials for a JWT
- `POST /riot/users` / `GET /riot/users/{id}` / `DELETE /riot/users/{id}`
- `GET /riot/devices` / `GET /riot/devices/{id}` / `GET /riot/devices/{id}/variables/{name}` / `POST /riot/devices/{id}/functions/{name}`

Device endpoints proxy through to the Particle Cloud API and require a bearer token with the
matching access right (`READ` for GETs, `UPDATE` to call a function, etc.).

## Testing

```
mvn test
```

Covers unit tests for services/validators/filters plus HTTP-level integration tests that exercise
a real embedded Jetty + Jersey deployment (see `JerseyIntegrationTestSupport`).

## Acknowledgments

* Inspired by https://github.com/cassiomolin/jersey-jwt
