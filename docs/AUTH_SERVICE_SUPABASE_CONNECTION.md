# Guia de conexion Auth Service a Supabase

Esta guia explica como conectar un microservicio `auth-service` a la base/API de Supabase de la empresa, siguiendo la misma idea usada en `catalog-service-java`.

La conexion recomendada para nuestros microservicios es:

```text
auth-service Spring Boot
        |
        | HTTP REST
        v
Supabase Kong / PostgREST
        |
        v
Tablas public.users, public.login, public.roles, public.user_has_roles
```

## 1. Variables de entorno

Crear un archivo `.env` local en la raiz del microservicio.

Importante: este archivo NO se sube a GitHub.

```env
SPRING_PROFILES_ACTIVE=supabase

SUPABASE_URL=http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io
SUPABASE_SERVICE_ROLE_KEY=PEGAR_AQUI_LA_SERVICE_ROLE_KEY_REAL
```

En GitHub solo debe existir un `.env.example`:

```env
SPRING_PROFILES_ACTIVE=supabase

SUPABASE_URL=http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io
SUPABASE_SERVICE_ROLE_KEY=tu_service_role_key
```

La `SERVICE_ROLE_KEY` real se entrega por interno, nunca dentro del repositorio.

## 2. Configuracion Spring

Crear este archivo:

```text
src/main/resources/application-supabase.yaml
```

Contenido recomendado si el microservicio se conectara por API REST y no por JDBC:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
```

Esto evita que Spring intente levantar una conexion JDBC obligatoria.

## 3. Cliente HTTP para Supabase

La conexion se hace enviando estos headers en cada request:

```http
apikey: SUPABASE_SERVICE_ROLE_KEY
Authorization: Bearer SUPABASE_SERVICE_ROLE_KEY
Accept: application/json
Content-Type: application/json
```

Ejemplo de URL para leer usuarios:

```text
{SUPABASE_URL}/rest/v1/users?select=*
```

Ejemplo de URL para leer login:

```text
{SUPABASE_URL}/rest/v1/login?select=*
```

Ejemplo de URL para filtrar un login por correo:

```text
{SUPABASE_URL}/rest/v1/login?login=eq.correo@demo.cl&select=*
```

## 4. Ejemplo de adapter en Java

Ejemplo base para un adapter de lectura:

```java
package com.flashdrop.auth.infrastructure.adapter.outbound.persistence.supabase;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

@Repository
public class SupabaseAuthRepositoryAdapter {

    private final RestClient restClient;

    public SupabaseAuthRepositoryAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<LoginRow> findLoginByEmail(String email) {
        LoginRow[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/login")
                        .queryParam("login", "eq." + email)
                        .queryParam("select", "*")
                        .build())
                .retrieve()
                .body(LoginRow[].class);

        return response == null ? List.of() : List.of(response);
    }
}
```

El DTO `LoginRow` debe representar las columnas reales de la tabla `login`.

Ejemplo:

```java
package com.flashdrop.auth.infrastructure.adapter.outbound.persistence.supabase;

public record LoginRow(
        Long id,
        String login,
        String password,
        Long id_users,
        Integer status
) {
}
```

## 5. Configuracion de propiedades

En `application.yaml` agregar:

```yaml
server:
  port: 8081

supabase:
  url: ${SUPABASE_URL}
  service-role-key: ${SUPABASE_SERVICE_ROLE_KEY}
```

Cada microservicio debe usar un puerto distinto. Ejemplo:

```text
auth-service    -> 8081
catalog-service -> 8082
orders-service  -> 8083
delivery-service -> 8084
```

## 6. Probar primero con curl

Antes de culpar al codigo, probar si Supabase responde:

```powershell
curl.exe -i `
  -H "apikey: TU_SERVICE_ROLE_KEY_REAL" `
  -H "Authorization: Bearer TU_SERVICE_ROLE_KEY_REAL" `
  "http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io/rest/v1/login?select=*&limit=1"
```

Si responde `200 OK`, la conexion a Supabase esta bien.

Si responde `401` o `403`, el problema es la key.

Si responde `404`, probablemente estas usando mal la URL base o la tabla no esta expuesta en PostgREST.

## 7. Levantar el microservicio

Desde la carpeta del microservicio:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

Probar health:

```text
http://localhost:8081/health
```

Probar endpoint de auth, segun lo que se implemente:

```text
http://localhost:8081/auth/login
```

## 8. Errores comunes

### Port already in use

Significa que el puerto ya esta ocupado.

Solucion rapida:

```powershell
netstat -ano | findstr :8081
taskkill /PID NUMERO_PID /F
```

O cambia el puerto en `application.yaml`.

### 404 Whitelabel Error Page

El microservicio esta vivo, pero la ruta no existe.

Ejemplo:

```text
/auth
```

puede no existir, pero:

```text
/auth/login
```

si puede existir.

Revisar el `@RequestMapping` del controller.

### 401 o 403 desde Supabase

La key esta mala, incompleta o no corresponde al proyecto.

Revisar:

```env
SUPABASE_SERVICE_ROLE_KEY=...
```

### 404 desde Supabase

Puede pasar por:

- URL equivocada.
- Tabla no existe.
- Tabla no esta expuesta por PostgREST.
- Se uso la URL del Studio en vez de la URL Kong/PostgREST.

La URL correcta para este entorno es:

```text
http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io
```

## 9. Que NO subir a GitHub

No subir:

```text
.env
service_role_key real
password real de Postgres
tokens privados
```

Si alguien necesita conectarse, se sube `.env.example` y la clave real se entrega por interno.

## 10. Resumen corto

Para conectar `auth-service`:

1. Crear `.env`.
2. Configurar `application.yaml`.
3. Crear `application-supabase.yaml`.
4. Crear adapter REST hacia Supabase.
5. Leer tablas por `/rest/v1`.
6. Probar con `curl`.
7. Levantar con perfil `supabase`.

La idea es que el codigo quede reutilizable, pero las claves reales queden fuera del repositorio.

## 11. Como se conecto el microservicio catalog-service-java

El microservicio de catalogo quedo conectado a Supabase usando la misma estrategia que se recomienda para `auth-service`: conexion HTTP REST hacia Supabase Kong/PostgREST, no conexion directa JDBC.

La carpeta del microservicio es:

```text
C:\Users\javie\Desktop\geosoft\aplicacion delivery github repositorios\respaldo\catalog-service-java
```

### 11.1 Variables locales

En `catalog-service-java` se creo un archivo `.env` local con las variables reales:

```env
SPRING_PROFILES_ACTIVE=supabase
SUPABASE_URL=http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io
SUPABASE_SERVICE_ROLE_KEY=LA_KEY_REAL
```

Ese archivo `.env` no se sube al repositorio.

En GitHub solo queda `.env.example`:

```env
SPRING_PROFILES_ACTIVE=supabase
SUPABASE_URL=http://supabasekong-wymwq8rktid7ov678oe4va90.76.13.169.150.sslip.io
SUPABASE_SERVICE_ROLE_KEY=tu_service_role_key
```

### 11.2 application.yaml

En catalogo se dejo el puerto del servicio en `8082`:

```yaml
server:
  port: 8082

supabase:
  url: ${SUPABASE_URL}
  service-role-key: ${SUPABASE_SERVICE_ROLE_KEY}
```

Archivo:

```text
src/main/resources/application.yaml
```

### 11.3 Perfil supabase

Tambien se creo:

```text
src/main/resources/application-supabase.yaml
```

Con esto:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
```

Esto significa:

- Spring Boot no intenta conectarse por JDBC.
- No pide `datasource`.
- No levanta JPA.
- No ejecuta Flyway.
- El microservicio consume Supabase como API REST.

### 11.4 Donde esta la conexion real

La conexion real esta en los adapters de infraestructura:

```text
src/main/java/com/flashdrop/catalog/infrastructure/adapter/outbound/persistence/supabase
```

Ahi existen adapters como:

```text
SupabaseRestProductRepositoryAdapter.java
SupabaseRestCategoryRepositoryAdapter.java
SupabaseRestRestaurantRepositoryAdapter.java
```

Esos adapters implementan los ports:

```text
ProductRepositoryPort
CategoryRepositoryPort
RestaurantRepositoryPort
```

La idea es:

```text
Controller
   -> UseCase
      -> Port
         -> Adapter Supabase REST
            -> Supabase /rest/v1
```

### 11.5 Ejemplo real del flujo

Cuando abres:

```text
http://localhost:8082/catalog/products
```

pasa esto:

```text
ProductController
   llama a ListProductsUseCase
      llama a ProductRepositoryPort.findAll()
         lo implementa SupabaseRestProductRepositoryAdapter
            hace GET a:
            {SUPABASE_URL}/rest/v1/products?select=*
```

La respuesta vuelve como JSON al navegador.

### 11.6 Headers usados

Cada adapter manda estos headers a Supabase:

```http
apikey: SUPABASE_SERVICE_ROLE_KEY
Authorization: Bearer SUPABASE_SERVICE_ROLE_KEY
Accept: application/json
Content-Type: application/json
```

Esto permite que el backend consulte las tablas reales de Supabase.

### 11.7 Endpoints probados

En catalogo se probaron estos endpoints:

```text
GET http://localhost:8082/health
GET http://localhost:8082/catalog/products
GET http://localhost:8082/catalog/categories
GET http://localhost:8082/catalog/restaurants
POST http://localhost:8082/catalog/products/validate
```

Y respondieron datos reales desde Supabase.

### 11.8 Como levantarlo

Desde la carpeta:

```powershell
cd "C:\Users\javie\Desktop\geosoft\aplicacion delivery github repositorios\respaldo\catalog-service-java"
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

Luego abrir:

```text
http://localhost:8082/catalog/products
```

### 11.9 Como copiar esta idea para auth-service

Para `auth-service`, hacer lo mismo pero cambiando:

```text
catalog-service-java -> auth-service
catalog -> auth
products/categories/restaurants -> users/login/roles/user_has_roles
puerto 8082 -> puerto 8081
```

Ejemplo de endpoint esperado:

```text
POST http://localhost:8081/auth/login
POST http://localhost:8081/auth/register
GET  http://localhost:8081/auth/users
```

Y el adapter de auth deberia consultar:

```text
{SUPABASE_URL}/rest/v1/users
{SUPABASE_URL}/rest/v1/login
{SUPABASE_URL}/rest/v1/roles
{SUPABASE_URL}/rest/v1/user_has_roles
```

### 11.10 Frase para explicar la conexion

Si preguntan como esta conectado, se puede explicar asi:

```text
El microservicio no se conecta directamente por JDBC a Postgres. Se conecta al Supabase de la empresa usando la API REST expuesta por Kong/PostgREST. La URL y la service role key se leen desde variables de entorno, y los adapters de infraestructura hacen las peticiones HTTP a /rest/v1. Las claves reales no estan en GitHub; solo existe un .env.example con placeholders.
```
