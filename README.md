# Miel Chende · Backend

API REST de **Miel Chende**, la web de Apícola Chende, una explotación apícola familiar de Cospeito (Lugo), en la Reserva de la Biosfera Terras do Miño.

Proyecto final individual del bootcamp de desarrollo web full stack de Factoría F5 y, a la vez, una aplicación real para el negocio familiar.

El frontend está en [mielchende-frontend](https://github.com/gmp395/mielchende-frontend).

## Índice

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Diagramas](#diagramas)
- [Instalación y puesta en marcha](#instalación-y-puesta-en-marcha)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints de la API](#endpoints-de-la-api)
- [Seguridad](#seguridad)
- [Testing](#testing)
- [Decisiones técnicas](#decisiones-técnicas)
- [Próximos pasos](#próximos-pasos)
- [Autora](#autora)

## Descripción

Apícola Chende produce miel artesanal con **una sola cosecha al año**. Por eso la web no es una tienda online: el cliente envía una **solicitud de pedido** y el apicultor la gestiona a mano, sin carrito ni pasarela de pago.

La API cubre el producto mínimo viable:

- **Catálogo público** de productos, con disponibilidad (disponible o agotado hasta la próxima cosecha).
- **Registro e inicio de sesión** de clientes, con tokens JWT.
- **Solicitudes de pedido** con varios productos, y consulta del historial propio ("Mis solicitudes").
- **Panel de administración**: gestión del catálogo y de las solicitudes, que siguen el ciclo Recibido → Confirmado → Enviado. Las solicitudes nunca se borran, para conservar el historial.

## Tecnologías

| Ámbito | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Seguridad | Spring Security, OAuth2 Resource Server (JWT con HS512), BCrypt |
| Base de datos | MySQL 8.4 en Docker, con Docker Compose |
| Utilidades | Lombok |
| Testing | JUnit 5, Mockito, MockMvc y Testcontainers (MySQL real) |
| Construcción | Maven (incluye Maven Wrapper) |

## Arquitectura

El código está organizado **por funcionalidad** (*feature*), no por capas: cada carpeta agrupa todo lo de un mismo concepto. Dentro de cada una se respeta la separación en capas: **Controller → Service → Repository**, con DTOs y mappers para no exponer nunca las entidades.

```
src/main/java/com/mielchende
├── auth/        Registro e inicio de sesión
├── order/       Solicitudes de pedido (cliente y administración)
├── product/     Catálogo (público y administración)
├── role/        Roles ROLE_USER y ROLE_ADMIN
├── security/    SecurityFilterChain, JWT, UserDetailsService y fachada de BCrypt
├── user/        Usuarios y administrador inicial
└── exception/   GlobalExceptionHandler: errores uniformes { "message": "..." }
```

Cada funcionalidad tiene sus propias excepciones de negocio (por ejemplo, `ProductNotFoundException` o `InvalidStatusTransitionException`), que el `GlobalExceptionHandler` traduce al código HTTP correspondiente.

## Diagramas

<!-- Pendiente (MC-20 y MC-21): diagrama entidad-relación y diagrama de clases -->

## Instalación y puesta en marcha

### Requisitos

- [Java 21](https://adoptium.net/) (JDK)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

No hace falta instalar Maven: el proyecto incluye Maven Wrapper (`mvnw`).

### 1. Clonar el repositorio

```bash
git clone https://github.com/gmp395/mielchende-backend.git
cd mielchende-backend
```

### 2. Crear el archivo `.env`

Las credenciales no están en el código: se leen de un archivo `.env` en la raíz del proyecto, que **no se sube a GitHub**. Parte de la plantilla:

```bash
cp .env.example .env
```

Abre `.env` y sustituye los valores de ejemplo (ver [Variables de entorno](#variables-de-entorno)).

Para generar `JWT_SECRET`:

```bash
openssl rand -base64 64 | tr -d '\n'
```

Copia el resultado completo en `JWT_SECRET=`. Tiene que quedar **en una sola línea**: `tr -d '\n'` quita el salto de línea que `openssl` añade a mitad de la clave.

### 3. Arrancar Docker Desktop

No hace falta levantar MySQL a mano. El proyecto usa `spring-boot-docker-compose`: al arrancar la aplicación, Spring Boot lee `compose.yaml` y crea el contenedor `mielchende-mysql` si no está en marcha. Solo es necesario que **Docker Desktop esté abierto**.

MySQL queda accesible en el puerto **3308** de tu máquina, por si quieres conectarte con un cliente como DBeaver.

### 4. Arrancar la aplicación

Desde la raíz del proyecto:

```bash
./mvnw spring-boot:run
```

O, desde el editor, ejecutando `MielchendeBackendApplication`.

La API queda disponible en **`http://localhost:8081`**. Para comprobarlo, abre en el navegador:

```
http://localhost:8081/api/products
```

### Qué ocurre en el primer arranque

1. Hibernate crea las tablas a partir de las entidades.
2. Se crean los roles `ROLE_USER` y `ROLE_ADMIN`.
3. Se crea la **cuenta de administrador** con los datos de `ADMIN_NAME`, `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
4. Se carga el **catálogo inicial**, solo si la tabla de productos está vacía.

Los datos se guardan en un volumen de Docker, así que se conservan entre arranques.

> **Importante:** el administrador se crea **solo si todavía no existe**. Si después cambias `ADMIN_PASSWORD` en el `.env`, la contraseña del administrador ya creado no cambia. Lo mismo ocurre con el catálogo: si ya hay productos, no se vuelve a cargar, para no pisar los cambios hechos desde el panel.

## Variables de entorno

| Variable | Para qué sirve |
|---|---|
| `MYSQL_DATABASE` | Nombre de la base de datos |
| `MYSQL_USER` | Usuario de MySQL que usa la aplicación |
| `MYSQL_PASSWORD` | Contraseña de ese usuario |
| `MYSQL_ROOT_PASSWORD` | Contraseña del usuario `root` de MySQL |
| `JWT_SECRET` | Clave secreta en Base64 para firmar los tokens (una sola línea) |
| `ADMIN_NAME` | Nombre del administrador inicial |
| `ADMIN_EMAIL` | Email con el que inicia sesión el administrador |
| `ADMIN_PASSWORD` | Contraseña inicial del administrador |

Docker Compose usa las variables de MySQL para crear el contenedor, y Spring Boot las usa para conectarse a él.

## Endpoints de la API

Todas las rutas empiezan por `/api`. Los errores siempre llegan como `{ "message": "..." }`.

### Autenticación (pública)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro. Cuerpo `{ name, email, password }` (contraseña de 8 caracteres o más) → 201. Email ya registrado → 409 |
| POST | `/auth/login` | Credenciales por Basic Auth → `{ token }`. Credenciales incorrectas → 401 |

El token dura **1 hora**. En el resto de peticiones se envía como `Authorization: Bearer <token>`.

### Catálogo (público)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/products` | Lista de productos |
| GET | `/products/{id}` | Un producto. Si no existe → 404 |

### Cliente (`ROLE_USER`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/orders` | Crea una solicitud `{ items: [{ productId, quantity }], phone?, comments? }` → 201. Producto agotado → 409 |
| GET | `/orders` | Solicitudes del cliente con la sesión iniciada, de la más reciente a la más antigua |

### Administración (`ROLE_ADMIN`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/admin/orders` | Todas las solicitudes |
| GET | `/admin/orders/summary` | Resumen: `{ pendingOrders }` (solicitudes recibidas sin revisar) |
| PATCH | `/admin/orders/{id}/status` | Cambia el estado un paso adelante o atrás. Salto no permitido → 409 |
| POST | `/admin/products` | Crea un producto → 201 |
| PUT | `/admin/products/{id}` | Edita un producto |
| PATCH | `/admin/products/{id}/status` | Cambia la disponibilidad (`AVAILABLE` o `SOLD_OUT`) |
| DELETE | `/admin/products/{id}` | Borra un producto → 204. Si tiene solicitudes → 409 |

## Seguridad

- **Autenticación sin estado (*stateless*)** con JWT firmado con HS512. El login usa Basic Auth una sola vez para obtener el token.
- **Contraseñas cifradas con BCrypt**, a través de una fachada (`PasswordEncoderFacade`).
- **Autorización por rol** en la `SecurityFilterChain`: el catálogo y el registro son públicos, las solicitudes son de `ROLE_USER` y todo `/api/admin/**` es de `ROLE_ADMIN`.
- **Cada cliente solo ve sus solicitudes**: el backend obtiene el usuario del token, nunca de un id en la URL.
- **CORS** limitado al origen del frontend (`http://localhost:5173`), permitiendo la cabecera `Authorization`.
- **Credenciales fuera del código**, en el `.env`.

## Testing

```bash
./mvnw test
```

**Requisitos:** Docker Desktop abierto y el archivo `.env` creado (los tests leen `JWT_SECRET` de él).

Los tests de integración usan **Testcontainers**: levantan un contenedor de **MySQL 8.4**, la misma versión que en desarrollo, en lugar de una base de datos en memoria como H2. Así se prueba contra el mismo motor que usa la aplicación.

La batería incluye:

- **Tests unitarios** de los servicios, la regla de cambio de estado, la fachada de BCrypt y los inicializadores, con Mockito.
- **Tests de integración** con MockMvc, que recorren toda la cadena (seguridad → controller → servicio → base de datos): registro y login, catálogo, solicitudes, panel de administración y control de acceso por rol (por ejemplo, un cliente recibe 403 en un endpoint de administración).

## Decisiones técnicas

- **Solicitud de pedido en lugar de compra online**, por el modelo real del negocio (una cosecha al año, gestión manual).
- **Estados en lugar de borrado**: las solicitudes avanzan o retroceden un paso (para corregir errores), pero nunca se borran.
- **Productos con solicitudes no se pueden borrar** (409): se marcan como agotados, que refleja mejor el fin de una cosecha.
- **JWT en lugar de sesión**, para que la API sea *stateless*.
- **La contraseña del registro llega sin codificar en Base64** (viaja por HTTPS en producción) y se cifra solo con BCrypt.
- **Testcontainers en lugar de H2**, para que los tests prueben contra MySQL real.
- **Paquetes por funcionalidad**, siguiendo la convención actual de Spring.

## Próximos pasos

- **Categorías de producto** y filtro en el catálogo, incluida la categoría "Para apicultores" (enjambres y reinas) con consulta de disponibilidad.
- **Notificaciones por email** al cliente y al apicultor en cada cambio de estado.
- **Configuración propia para los tests**, para que no dependan del `.env`.
- **Despliegue** en un servidor con HTTPS.

## Autora

**Gema Miguel** · [GitHub](https://github.com/gmp395)