# Catalog Service

Microservicio de catalogo de Flash Drop Delivery construido con Java 21 y Spring Boot 3.

## Endpoints

```text
GET  /health
GET  /catalog/products
POST /catalog/products
POST /catalog/products/validate
GET  /catalog/categories
GET  /catalog/restaurants
```

Ejemplo de validacion:

```json
{
  "productIds": [1, 2, 999]
}
```

## Perfiles

```text
local     Usa datos en memoria
supabase  Usa Supabase REST API con SUPABASE_URL y SUPABASE_SERVICE_ROLE_KEY
postgres  Usa JPA/PostgreSQL directo con POSTGRES_HOST, POSTGRES_PORT, POSTGRES_DB, POSTGRES_USER y POSTGRES_PASSWORD
```

## Levantar con Supabase

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

Este es el modo recomendado para probar desde el equipo local, porque usa `SUPABASE_URL` y
`SUPABASE_SERVICE_ROLE_KEY`.

## Levantar local sin Supabase

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## Docker

```powershell
docker compose up --build
```

Por defecto Docker levanta el servicio con el perfil `postgres`, pensado para correr dentro
del VPS/Docker de la empresa usando `POSTGRES_HOST=db`.

El archivo `.env` real debe quedar en el servidor, no en GitHub:

```text
SPRING_PROFILES_ACTIVE=postgres
POSTGRES_HOST=db
POSTGRES_PORT=5432
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=********
POSTGRES_SSLMODE=disable
```

Nota: `POSTGRES_HOST=db` funciona solo si este servicio corre dentro de la misma red Docker/VPS
donde existe el contenedor `db`. Desde el equipo local normalmente no resuelve. Si se usa pooler,
hay que confirmar que el usuario/tenant del pooler sea valido para ese Supabase.

Para probar sin base real:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```
