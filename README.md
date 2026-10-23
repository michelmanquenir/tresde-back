# tienda-tresde.back

API REST de la tienda TresD: Spring Boot 4 · Java 21 · PostgreSQL · JWT.

## Levantar en local

Necesitas Docker Desktop abierto y Java 21.

```bash
docker compose up -d      # base de datos PostgreSQL en el puerto 5432
./gradlew bootRun         # API en http://localhost:8080
```

Al arrancar por primera vez:

- Flyway crea las tablas y carga las 6 categorías y 15 productos de ejemplo.
- Se crea el usuario administrador `admin` / `admin`. Cámbialo con la variable `ADMIN_PASSWORD`.

Luego, en `tienda-tresd-front`, ejecuta `npm run dev`. Vite redirige `/api` a este backend.

Tests (usan H2 en memoria y no necesitan Docker):

```bash
./gradlew test
```

## Endpoints

### Públicos (tienda)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/health` | Estado de la API |
| GET | `/api/categories` | Categorías |
| GET | `/api/products?category=&q=` | Productos visibles, del más nuevo al más antiguo |
| GET | `/api/products/{id}` | Un producto visible |
| GET | `/api/images/{id}` | Foto de un producto |
| POST | `/api/auth/login` | `{username, password}` → `{token, expiresAt, user}` |

### Panel de administración

Requieren el header `Authorization: Bearer <token>` y el rol ADMIN.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/auth/me` | Usuario del token |
| GET | `/api/admin/products` | Todos los productos, incluidos los ocultos |
| GET | `/api/admin/products/{id}` | Un producto |
| POST | `/api/admin/products` | Crear (el id se genera a partir del nombre) |
| PUT | `/api/admin/products/{id}` | Editar |
| PATCH | `/api/admin/products/{id}/visibility` | `{hidden: true/false}` |
| DELETE | `/api/admin/products/{id}` | Eliminar (borra también sus fotos) |
| POST | `/api/admin/products/import` | Reemplaza todo el catálogo con un JSON exportado |
| POST | `/api/admin/images` | Subir foto (multipart, campo `file`, máx. 5 MB) → `{id, url}` |

Los errores siempre responden así: `{"message": "...", "errors": {"campo": "mensaje"}}`.

## Variables de entorno (producción)

| Variable | Por defecto | Para qué |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/tresde` | Conexión a PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `tresde` / `tresde` | Credenciales de la base |
| `JWT_SECRET` | clave de desarrollo | **Obligatorio cambiarla.** Mínimo 32 caracteres |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin` | Admin inicial (solo se crea si no hay usuarios) |
| `CORS_ORIGINS` | `http://localhost:5173,https://tresde-creation.vercel.app` | Dominios del frontend |
| `PORT` | `8080` | Puerto HTTP |

En el frontend publicado, define `VITE_API_URL` con la URL pública de este backend.

## Estructura

```
src/main/java/.../back/
  config/       propiedades (app.*), CORS y creación del admin inicial
  controller/   endpoints REST y manejo global de errores
  dto/          objetos de entrada/salida de la API (records)
  model/        entidades JPA: Product, Category, StoredImage, User
  repository/   repositorios Spring Data JPA
  security/     reglas de acceso, configuración JWT y emisión de tokens
  service/      lógica de negocio: productos, imágenes, login, categorías
  utils/        ApiException, slugs y URLs de imágenes
src/main/resources/db/migration/   esquema y datos iniciales (Flyway)
```
