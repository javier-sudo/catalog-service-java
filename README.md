# Catalog Service

Microservicio de catalogo de Flash Drop Delivery construido con Java 21 y Spring Boot 3.

## Endpoints

```text
GET  /health
GET  /catalog/products
POST /catalog/products/validate
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

Para usar PostgreSQL directo en Docker:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
docker compose up --build
```

Nota: `POSTGRES_HOST=db` funciona solo si este servicio corre dentro de la misma red Docker/VPS
donde existe el contenedor `db`. Desde el equipo local normalmente no resuelve. Si se usa pooler,
hay que confirmar que el usuario/tenant del pooler sea valido para ese Supabase.
