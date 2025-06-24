# VerificaMTC - Sistema de Verificación de Identidad para Exámenes de Manejo

## 📌 Descripción del Proyecto

**VerificaMTC** es una aplicación móvil Android desarrollada para optimizar la verificación de identidad en los exámenes de manejo del Ministerio de Transportes y Comunicaciones (MTC) del Perú. Utiliza reconocimiento facial mediante aprendizaje automático y validación de datos personales, ofreciendo un proceso seguro, ágil y moderno.

---

## ✨ Características Clave

- 🔐 **Autenticación** de usuarios y administradores
- 📝 **Registro seguro** con validación de datos
- 👤 **Reconocimiento facial** con ML Kit y TensorFlow Lite
- 📊 **Dashboard administrativo** para gestión de postulantes
- 🚗 **Gestión vehicular** y módulos para trámites
- ☁️ **Sincronización en la nube** con Firebase

---

## 🧭 Estructura del Proyecto

```
com.master.verificamtc
├── admin
│   └── dashboard
│       └── AdminDashboardActivity.java
│
├── auth
│   ├── AuthAdminActivity.java
│   ├── AuthRegisterActivity.java
│   ├── AuthSelectorActivity.java
│   └── AuthUserActivity.java
│
├── helpers
│   ├── database
│   │   ├── AppDatabase.java
│   │   └── FirebaseDatabaseHelper.java
│   │
│   ├── object
│   │   └── FaceRecognitionActivity.java
│   │
│   ├── vision
│   │   ├── obscura
│   │   │   └── ObscureType.java
│   │   │
│   │   ├── recogniser
│   │   │   ├── FaceRecognitionProcessor.java
│   │   │   └── FaceGraphic.java
│   │   │
│   │   ├── GraphicOverlay.java
│   │   └── VisionBaseProcessor.java
│   │
│   ├── MLVideoHelperActivity.java
│   └── SecurityHelper.java
│
└── user
    ├── circuit
    │   └── UserCircuitActivity.java
    │
    ├── dashboard
    │   └── UserDashboardActivity.java
    │
    ├── exam
    │   └── UserExamActivity.java
    │
    ├── payment
    │   └── UserPaymentActivity.java
    │
    └── vehicle
        └── UserVehicleActivity.java
```

---

## 🔧 Tecnologías Utilizadas

| Categoría       | Tecnología                                      |
|-----------------|--------------------------------------------------|
| **Lenguaje**    | Java                                             |
| **Base de datos** | SQLite (local), Firebase Realtime Database     |
| **Autenticación** | Firebase Auth, Reconocimiento facial (ML Kit) |
| **Seguridad**   | BCrypt, validación de entradas                  |
| **Machine Learning** | TensorFlow Lite (`mobile_face_net.tflite`)|
| **UI/UX**       | Material Design, CameraX                        |

---

## ⚙️ Configuración del Proyecto

### Requisitos

- Android Studio Flamingo o superior
- Android SDK 26+
- Dispositivo Android con cámara

### Pasos para Configuración

1. **Clonar repositorio**

   ```bash
   git clone https://github.com/MarcoXMarquez/VerificaMTC.git
   ```

2. **Abrir en Android Studio**

3. **Configurar Firebase**
   - Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descargar y agregar `google-services.json` en `/app`
   - Habilitar Authentication y Realtime Database

4. **Agregar dependencias**

   Asegúrate de tener en `build.gradle.kts`:

   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
   implementation("com.google.firebase:firebase-auth")
   implementation("com.google.firebase:firebase-database")
   ```

5. **Sincronizar Gradle**

---

## 🧠 Modelo de Machine Learning

El modelo de reconocimiento facial se encuentra en:

```
app/src/main/res/ml/mobile_face_net.tflite
```

---

## 👨‍💻 Equipo de Desarrollo

**Universidad Nacional de San Agustín de Arequipa - 2025**  
Curso: *Ingeniería y Procesos de Software*

- Alfonso Huacasi Alejandro Sebastián  
- Arce Mayhua Leonardo Ruben  
- Basurco Casani Jeferson Joao  
- Marquez Herrera Marco Antonio  

---

## 📝 Licencia

Este proyecto está bajo licencia MIT. Consulta el archivo `LICENSE` para más detalles.

---

## 📎 Recursos

- 📂 [Repositorio GitHub](https://github.com/MarcoXMarquez/VerificaMTC)
