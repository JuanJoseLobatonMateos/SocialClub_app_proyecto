# 📱 Social Club App - Aplicación Móvil para Socios

<div align="center">
  <img src="app/src/main/res/drawable/logo_club_social.png" alt="Logo del Club Social" width="200">
  <br>
  <p><em>Gestiona tu experiencia como socio desde tu dispositivo móvil</em></p>
</div>

## 📋 Descripción

Social Club App es la aplicación móvil complementaria del sistema integral de gestión para clubes sociales. Diseñada específicamente para los socios, esta app te permite gestionar reservas, consultar tu perfil, recibir notificaciones y llevar siempre contigo tu carnet digital.

## ✨ Características Principales

### 🆔 Carnet Digital
- Visualización del carnet de socio en formato digital
- Código QR integrado para control de acceso
- Credenciales personales siempre disponibles
- Validación en tiempo real

### 📅 Reserva de Instalaciones
- Consulta de disponibilidad en tiempo real
- Reserva de pistas deportivas y espacios
- Visualización del calendario de disponibilidad
- Cancelación de reservas desde la app

### 👤 Gestión de Perfil
- Visualización y edición de datos personales
- Historial de reservas y actividades
- Cambio de contraseña
- Actualización de información de contacto

### 🔔 Notificaciones
- Avisos sobre eventos del club
- Recordatorios de reservas
- Notificaciones de cambios de horario
- Comunicados importantes del club

## 🛠️ Tecnologías Utilizadas

- **Java** - Lenguaje principal
- **Android Studio Meerkat (2024.3.1)** - Entorno de desarrollo
- **ViewBinding / DataBinding** - Enlace de datos con UI
- **JDBC con DriverManager** - Conexión a base de datos MySQL
- **jBCrypt** - Seguridad de contraseñas
- **AndroidX** - Bibliotecas de compatibilidad
- **Navigation Component** - Navegación entre pantallas
- **ConstraintLayout** - Diseño de interfaces responsivas

## 📱 Requisitos del Sistema

- Android 7.0 (Nougat) o superior
- Conexión a Internet
- Aproximadamente 50MB de espacio en almacenamiento

## 🚀 Instalación

### Opción 1: Google Play Store
Descarga la app desde [Google Play Store](https://play.google.com/store/apps/details?id=com.jlobatonm.socialclub)

### Opción 2: APK directo
1. Habilita la instalación de aplicaciones de orígenes desconocidos en la configuración de tu dispositivo
2. Descarga el archivo APK desde [Google Drive](https://drive.google.com/file/d/1O8eYR2HjK1HJ6BuutddeIuD2IJwepNFp/view?usp=drive_link)
3. Abre el archivo APK y sigue las instrucciones de instalación

### Opción 3: Compilar desde el código fuente
1. Clona el repositorio:
   ```bash
   git clone https://github.com/JuanJoseLobatonMateos/SocialClub_app_proyecto.git
   ```
2. Abre el proyecto en Android Studio
3. Compila y ejecuta en tu dispositivo o emulador

## 📁 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│       ├── java/com/jlobatonm/socialclub/
│       │   ├── activities/      # Actividades principales
│       │   ├── adapters/        # Adaptadores para RecyclerView
│       │   ├── dao/             # Acceso a datos
│       │   ├── model/           # Clases de modelo
│       │   ├── utils/           # Utilidades y helpers
│       │   └── fragments/       # Fragmentos de UI
│       └── res/
│           ├── layout/          # Layouts XML
│           ├── drawable/        # Imágenes y recursos gráficos
│           ├── values/          # Strings, colores, estilos
│           └── navigation/      # Gráficos de navegación
```

## 🧪 Ejecución de Pruebas

Para ejecutar las pruebas unitarias:
```bash
./gradlew test
```

## 🔒 Seguridad

- Credenciales enviadas por email al registrarse como socio
- Contraseñas encriptadas con BCrypt
- Conexión segura con la base de datos
- Validación de sesión y tokens de autenticación

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor, sigue estos pasos:

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Haz commit de tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 🔮 Próximas Funcionalidades

- 💬 Chat interno con empleados del club
- 💳 Pago de cuotas y reservas desde la app
- 🏆 Sistema de logros y fidelización
- 📊 Estadísticas personales de uso de instalaciones

## 🤝 Integración con el Sistema

Esta aplicación móvil forma parte del ecosistema Social Club, integrado por:

1. **Aplicación de Escritorio (JavaFX)** - Para administradores y empleados
2. **Aplicación Móvil (Android)** - Para socios
3. **Base de Datos Centralizada** - Alojada en Google Cloud

Todas las aplicaciones comparten la misma base de datos, garantizando información actualizada en tiempo real.

## 📝 Licencia

Este proyecto está protegido por una licencia propietaria. Ver LICENSE.txt para más información.

## 📞 Contacto

Juan José Lobatón Mateos - [jlobatonm@gmail.com](mailto:jlobatonm@gmail.com)

Link del proyecto: [https://github.com/JuanJoseLobatonMateos/SocialClub_app_proyecto](https://github.com/JuanJoseLobatonMateos/SocialClub_app_proyecto)