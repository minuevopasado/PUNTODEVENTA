# InventarioApp - Sistema de Gestión de Inventario

Una aplicación Android completa desarrollada en Kotlin con Jetpack Compose para la gestión integral de inventario, ventas, compras y reportes.

## 🚀 Características Principales

### 📊 Dashboard Inteligente
- **Resumen en tiempo real**: Ventas del día, total de productos, stock bajo, valor del inventario
- **Gráficos interactivos**: Ventas semanales, distribución por categorías
- **Actividad reciente**: Historial de transacciones más recientes
- **Filtros por período**: Día, semana, mes, año o rango personalizado

### 📦 Gestión de Inventario
- **CRUD completo**: Crear, leer, actualizar y eliminar productos
- **Escáner de códigos de barras**: Integración con cámara del dispositivo
- **Búsqueda avanzada**: Por nombre, código de barras, SKU o categoría
- **Control de stock**: Alertas de stock bajo, actualización en tiempo real
- **Categorización**: Organización por categorías con colores personalizables
- **Imágenes de productos**: Múltiples imágenes por producto con imagen principal
- **Captura de fotos**: Cámara integrada para tomar fotos de productos
- **Galería de imágenes**: Selección desde galería del dispositivo

### 🛒 Sistema de Ventas
- **Carrito de compras**: Interfaz intuitiva para agregar productos
- **Escáner integrado**: Búsqueda rápida por código de barras
- **Cálculo automático**: Subtotal, impuestos y total
- **Historial de ventas**: Registro completo de todas las transacciones
- **Múltiples métodos de pago**: Efectivo, tarjeta, transferencia
- **Generación automática de recibos**: PDF con formato profesional
- **Documentos adjuntos**: Adjuntar comprobantes de pago

### 🛍️ Gestión de Compras
- **Registro de compras**: Proveedores, fechas, cantidades
- **Actualización automática**: Stock se actualiza al registrar compras
- **Historial de compras**: Seguimiento de todas las adquisiciones
- **Gestión de proveedores**: Información de contacto y productos
- **Facturas automáticas**: Generación de facturas de compra
- **Documentos adjuntos**: Adjuntar facturas y comprobantes de proveedores

### 💰 Control de Gastos
- **Categorización de gastos**: Rentas, servicios, marketing, etc.
- **Registro de recibos**: Captura de imágenes de comprobantes
- **Reportes de gastos**: Análisis por categoría y período
- **Gastos recurrentes**: Configuración de gastos automáticos
- **Documentos adjuntos**: Adjuntar recibos, facturas y comprobantes
- **Digitalización**: Captura de documentos con la cámara

### 📈 Reportes Avanzados
- **Múltiples formatos**: CSV, Excel, PDF
- **Filtros personalizables**: Por fecha, categoría, producto
- **Gráficos interactivos**: Ventas, inventario, gastos
- **Exportación**: Compartir reportes por email o almacenamiento
- **Generación automática**: Reportes PDF con formato profesional
- **Almacenamiento local**: Todos los reportes guardados para consulta

### ⚙️ Configuración Completa
- **Modo oscuro**: Tema personalizable
- **Moneda configurable**: Símbolo, decimales, tasa de cambio
- **Información empresarial**: Nombre, dirección, contacto
- **Respaldo automático**: JSON con imágenes y documentos incluidos
- **Umbrales personalizables**: Stock bajo, respaldos automáticos
- **Gestión de archivos**: Organización automática de documentos e imágenes

### 🔐 Gestión de Usuarios y Roles
- **Múltiples roles**: Admin, Manager, Employee, Viewer
- **Permisos granulares**: Acceso controlado por funcionalidad
- **Autenticación segura**: Login con validación
- **Auditoría**: Registro de acciones por usuario

## 🛠️ Tecnologías Utilizadas

### Frontend
- **Kotlin**: Lenguaje principal
- **Jetpack Compose**: UI moderna y declarativa
- **Material Design 3**: Diseño consistente y accesible
- **Navigation Compose**: Navegación fluida entre pantallas

### Backend & Base de Datos
- **Room Database**: Base de datos local robusta
- **Hilt**: Inyección de dependencias
- **Coroutines & Flow**: Programación asíncrona
- **LiveData**: Observación de datos reactiva

### Funcionalidades Avanzadas
- **ZXing**: Escaneo de códigos de barras
- **CameraX**: Captura de imágenes
- **Apache POI**: Exportación a Excel
- **OpenCSV**: Exportación a CSV
- **Gson**: Serialización JSON para respaldos

## 📱 Características Técnicas

### Arquitectura
- **MVVM**: Model-View-ViewModel
- **Clean Architecture**: Separación de responsabilidades
- **Repository Pattern**: Acceso a datos centralizado
- **Dependency Injection**: Hilt para gestión de dependencias

### Base de Datos
- **Entidades principales**: Users, Products, Categories, Transactions, Expenses, Documents, ProductImages
- **Relaciones**: Foreign keys para integridad referencial
- **Índices optimizados**: Búsquedas rápidas por código de barras
- **Migraciones**: Soporte para actualizaciones de esquema
- **SQLite**: Base de datos local robusta y eficiente

### UI/UX
- **Responsive Design**: Adaptable a diferentes tamaños de pantalla
- **Dark/Light Theme**: Soporte completo para ambos temas
- **Accesibilidad**: Compatible con lectores de pantalla
- **Animaciones fluidas**: Transiciones suaves entre pantallas

## 🚀 Instalación y Configuración

### Requisitos Previos
- Android Studio Arctic Fox o superior
- Android SDK API 24+
- Kotlin 1.8.20+
- Gradle 8.0+

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/inventarioapp.git
   cd inventarioapp
   ```

2. **Configurar dependencias**
   - Abrir el proyecto en Android Studio
   - Sincronizar Gradle automáticamente
   - Verificar que todas las dependencias se descarguen correctamente

3. **Configurar permisos**
   - La aplicación solicitará permisos de cámara y almacenamiento
   - Aceptar permisos para funcionalidad completa

4. **Ejecutar la aplicación**
   - Conectar dispositivo Android o usar emulador
   - Presionar Run en Android Studio

### Configuración Inicial

1. **Primer inicio**: La app creará automáticamente la base de datos
2. **Usuario administrador**: Crear cuenta con rol Admin
3. **Configuración básica**: Establecer moneda, información empresarial
4. **Categorías**: Crear categorías de productos
5. **Productos**: Agregar productos iniciales

## 📊 Estructura del Proyecto

```
app/src/main/java/com/inventarioapp/
├── data/
│   ├── database/          # Room Database y DAOs
│   ├── entities/          # Entidades de base de datos
│   └── repository/        # Repositorios de datos
├── di/                    # Módulos de Hilt
├── ui/
│   ├── components/        # Componentes reutilizables
│   ├── screens/          # Pantallas principales
│   ├── theme/            # Temas y estilos
│   ├── viewmodels/       # ViewModels
│   └── navigation/       # Navegación
└── utils/                # Utilidades y helpers
```

## 🔧 Configuración Avanzada

### Personalización de Temas
```kotlin
// En Theme.kt
val CustomColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    secondary = Color(0xFF03DAC6),
    // ... más colores
)
```

### Configuración de Base de Datos
```kotlin
// En AppDatabase.kt
@Database(
    entities = [User::class, Product::class, ...],
    version = 1,
    exportSchema = false
)
```

### Permisos Personalizados
```xml
<!-- En AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 📈 Funcionalidades de Reportes

### Tipos de Reportes Disponibles
1. **Reporte de Ventas**
   - Ventas por período
   - Productos más vendidos
   - Análisis de tendencias

2. **Reporte de Inventario**
   - Stock actual
   - Productos con stock bajo
   - Valor total del inventario

3. **Reporte de Gastos**
   - Gastos por categoría
   - Análisis de costos
   - Comparación mensual

### Formatos de Exportación
- **CSV**: Para análisis en Excel
- **Excel**: Reportes formateados
- **JSON**: Respaldo completo con imágenes y documentos
- **PDF**: Reportes y documentos con formato profesional

## 🔒 Seguridad y Privacidad

### Características de Seguridad
- **Encriptación local**: Datos sensibles protegidos
- **Validación de entrada**: Prevención de inyección SQL
- **Permisos granulares**: Acceso controlado por funcionalidad
- **Auditoría completa**: Registro de todas las acciones

### Privacidad
- **Datos locales**: Toda la información se almacena localmente
- **Sin tracking**: No se recopilan datos de uso
- **Respaldo seguro**: Exportación controlada por el usuario

## 🚀 Roadmap y Mejoras Futuras

### Próximas Funcionalidades
- [ ] **Sincronización en la nube**: Backup automático a Google Drive
- [ ] **Múltiples ubicaciones**: Gestión de inventario en múltiples tiendas
- [ ] **Integración con proveedores**: APIs para actualización automática de precios
- [ ] **Análisis predictivo**: IA para predecir demanda
- [ ] **App web**: Versión web complementaria
- [ ] **Notificaciones push**: Alertas de stock bajo
- [ ] **Integración con POS**: Conectividad con sistemas de punto de venta

### Mejoras Técnicas
- [ ] **Offline-first**: Funcionamiento sin conexión
- [ ] **Performance**: Optimización de consultas de base de datos
- [ ] **Testing**: Cobertura completa de pruebas unitarias
- [ ] **CI/CD**: Pipeline de integración continua

## 🤝 Contribución

### Cómo Contribuir
1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

### Guías de Contribución
- Seguir las convenciones de código de Kotlin
- Documentar nuevas funcionalidades
- Agregar pruebas para nuevas features
- Mantener la compatibilidad con versiones anteriores

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

### Contacto
- **Email**: soporte@inventarioapp.com
- **Documentación**: [docs.inventarioapp.com](https://docs.inventarioapp.com)
- **Issues**: [GitHub Issues](https://github.com/tu-usuario/inventarioapp/issues)

### Comunidad
- **Discord**: [Canal de la comunidad](https://discord.gg/inventarioapp)
- **Telegram**: [Grupo de usuarios](https://t.me/inventarioapp)
- **YouTube**: [Tutoriales y demos](https://youtube.com/inventarioapp)

## 🙏 Agradecimientos

- **Jetpack Compose Team**: Por el increíble framework de UI
- **Android Team**: Por las herramientas de desarrollo
- **Comunidad Kotlin**: Por el soporte y recursos
- **Contribuidores**: Por sus valiosas contribuciones

---

**InventarioApp** - Simplificando la gestión de inventario para pequeñas y medianas empresas. 🚀
