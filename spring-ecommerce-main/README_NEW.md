# 🚀 E-Commerce Pro - Plataforma MercadoLibre Style

## 📋 Estado del Proyecto

### ✅ Completado

#### Backend
- ✅ **JWT Authentication** con Access y Refresh Tokens
- ✅ **APIs REST** para Products, Categories, Brands, Auth
- ✅ **DTOs completos** para todas las entidades principales
- ✅ **MapStruct** configurado para mapeo automático
- ✅ **Redis** configurado para caché
- ✅ **Elasticsearch** agregado a docker-compose
- ✅ **CORS** configurado correctamente
- ✅ **GlobalExceptionHandler** mejorado
- ✅ **Thymeleaf removido** del pom.xml

#### Frontend
- ✅ **Next.js 14** con App Router
- ✅ **TypeScript** configurado
- ✅ **Tailwind CSS** para estilos
- ✅ **Framer Motion** para animaciones
- ✅ **React Three Fiber** para visualización 3D
- ✅ **Diseño Apple-style** con Glassmorphism
- ✅ Páginas: Home, Products, Product Detail

#### Infraestructura
- ✅ **Docker** configurado
- ✅ **docker-compose** con MySQL, Redis, Elasticsearch
- ✅ **Dockerfile** para backend

### 🚧 En Progreso

- 🔄 Conversión completa de controllers a REST APIs
- 🔄 Implementación de OAuth2 (Google, Apple)
- 🔄 Sistema de vendedores (Marketplace)
- 🔄 Funcionalidades avanzadas (Reviews, Q&A)

### 📝 Pendiente

- ⏳ Elasticsearch integration completa
- ⏳ Upload de modelos 3D
- ⏳ Variantes de productos
- ⏳ Sistema de reviews y calificaciones
- ⏳ Preguntas y respuestas
- ⏳ Frontend completo (Login, Cart, Checkout, Seller Dashboard)

---

## 🏗️ Arquitectura

```
Backend (Spring Boot 3.5.7)
├── API REST pura (sin Thymeleaf)
├── JWT + Refresh Tokens
├── Redis Cache
├── Elasticsearch (configurado)
└── MySQL Database

Frontend (Next.js 14)
├── App Router
├── TypeScript
├── Three.js + React Three Fiber
└── Apple-style UI

Infraestructura
├── Docker
├── docker-compose
└── Listo para cloud deployment
```

---

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21
- Maven 3.8+
- Node.js 20+
- Docker y Docker Compose

### Backend

```bash
# Con Docker (recomendado)
docker-compose up -d

# O localmente
mvn clean install
mvn spring-boot:run
```

Backend disponible en: `http://localhost:8081`

### Frontend

```bash
cd frontend-nextjs
npm install
npm run dev
```

Frontend disponible en: `http://localhost:3000`

---

## 📚 APIs Disponibles

### Autenticación
- `POST /api/auth/login` - Login con username/password
- `POST /api/auth/refresh` - Refrescar access token
- `GET /api/auth/validate` - Validar token

### Productos
- `GET /api/products` - Listar productos (con filtros)
- `GET /api/products/{id}` - Detalle de producto

### Categorías
- `GET /api/categories` - Listar categorías activas
- `GET /api/categories/{id}` - Detalle de categoría

### Marcas
- `GET /api/brands` - Listar marcas activas
- `GET /api/brands/{id}` - Detalle de marca

---

## 🔐 Autenticación

### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "usuario",
  "password": "contraseña"
}
```

Respuesta:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "type": "Bearer",
  "username": "usuario",
  "role": "USER",
  "userId": 1,
  "email": "usuario@example.com",
  "name": "Nombre Usuario"
}
```

### Usar Token
```bash
GET /api/products
Authorization: Bearer {accessToken}
```

### Refrescar Token
```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

---

## 🗂️ Estructura del Proyecto

```
.
├── src/main/java/ppi/e_commerce/
│   ├── Config/          # Configuraciones
│   ├── Controller/
│   │   └── Api/         # REST APIs
│   ├── Dto/             # Data Transfer Objects
│   ├── Mapper/           # MapStruct mappers
│   ├── Model/           # Entidades JPA
│   ├── Repository/      # Repositorios JPA
│   ├── Service/          # Lógica de negocio
│   └── Utils/           # Utilidades (JWT, etc.)
├── frontend-nextjs/     # Frontend Next.js
│   ├── app/             # Páginas (App Router)
│   ├── components/      # Componentes React
│   └── types/           # TypeScript types
├── docker-compose.yml   # Orquestación
└── Dockerfile           # Backend container
```

---

## 🎨 Características de Diseño

- **Glassmorphism:** Efectos de vidrio translúcido
- **Animaciones suaves:** Framer Motion
- **Visualización 3D:** Three.js + React Three Fiber
- **Responsive:** Diseño adaptativo
- **Modo claro/oscuro:** Preparado (pendiente implementar)

---

## 🔧 Configuración

### Variables de Entorno Backend

`application.properties`:
```properties
app.jwt.secret=tu-secret-key
app.jwt.expiration=86400000
app.jwt.refresh-expiration=604800000
```

### Variables de Entorno Frontend

`.env.local`:
```env
NEXT_PUBLIC_API_URL=http://localhost:8081
```

---

## 📦 Servicios Docker

- **MySQL:** Puerto 3306
- **Redis:** Puerto 6379
- **Elasticsearch:** Puerto 9200
- **Backend:** Puerto 8081
- **Frontend:** Puerto 3000

---

## 🛠️ Próximos Pasos

1. **Completar APIs REST:**
   - Cart API
   - Order API
   - User API
   - Payment API

2. **Implementar Marketplace:**
   - Modelo Seller
   - Dashboard de vendedor
   - Gestión de productos por vendedor

3. **Funcionalidades Avanzadas:**
   - Reviews y calificaciones
   - Preguntas y respuestas
   - Sistema de reputación

4. **Frontend Completo:**
   - Login/Register
   - Cart y Checkout
   - Profile
   - Seller Dashboard

5. **Integración Elasticsearch:**
   - Indexación de productos
   - Búsqueda avanzada
   - Autocompletado

---

## 📖 Documentación Adicional

- [TRANSFORMACION_TOTAL.md](./TRANSFORMACION_TOTAL.md) - Diagnóstico completo y plan de transformación
- [TRANSFORMACION_GUIA.md](./TRANSFORMACION_GUIA.md) - Guía de transformación anterior

---

## 🤝 Contribuir

Este proyecto está en transformación activa. Ver `TRANSFORMACION_TOTAL.md` para el plan completo.

---

## 📄 Licencia

Este proyecto es privado.

---

**Última actualización:** Transformación en progreso - Fase 1 completada

