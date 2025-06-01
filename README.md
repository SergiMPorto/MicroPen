# MicroPen 

Una aplicación Android innovadora que combina reconocimiento de escritura digital, inteligencia artificial y traducción de texto para crear una experiencia de escritura inteligente y fluida.

## Descripción

MicroPen es una aplicación móvil avanzada que utiliza tecnologías de Machine Learning para reconocer texto escrito a mano, convertirlo a texto digital, traducirlo a diferentes idiomas y proporcionar funcionalidades inteligentes mediante IA generativa. La aplicación está diseñada para ser una herramienta completa de productividad para estudiantes, profesionales y cualquier persona que busque digitalizar y mejorar su proceso de escritura.

##  Características Principales

###  Reconocimiento de Escritura Digital
- **Escritura a mano libre**: Reconoce texto escrito con el dedo o stylus
- **Conversión a texto digital**: Transforma la escritura manual en texto editable
- **Precisión avanzada**: Utiliza ML Kit de Google para alta precisión

###  Reconocimiento Óptico de Caracteres (OCR)
- **Escaneo de texto**: Captura texto desde imágenes y fotografías
- **Reconocimiento de QR**: Lee y procesa códigos QR automáticamente
- **Extracción inteligente**: Procesamiento avanzado de documentos

###  Traducción Inteligente
- **Traducción en tiempo real**: Traduce texto a múltiples idiomas
- **Múltiples fuentes**: Funciona con texto escrito, escaneado o fotografiado
- **Integración ML Kit**: Traducción offline disponible

###  Inteligencia Artificial Generativa
- **Google Generative AI**: Integración con modelos de IA avanzados
- **Asistencia inteligente**: Mejora y sugiere correcciones de texto
- **Procesamiento contextual**: Comprende y mejora el contenido

###  Autenticación Segura
- **Firebase Authentication**: Sistema de autenticación robusto
- **Login con Google**: Acceso rápido con cuenta de Google
- **Login con Facebook**: Integración con redes sociales
- **Gestión de usuarios**: Perfiles personalizados y seguros

###  Servicios de Localización
- **Geolocalización**: Funcionalidades basadas en ubicación
- **Contexto geográfico**: Adapta la experiencia según la ubicación

##  Tecnologías Utilizadas

### Lenguajes y Frameworks
- **Kotlin** - Lenguaje principal de desarrollo
- **Android SDK** - Framework nativo de Android
- **Material Design** - Interfaz de usuario moderna

### APIs y Servicios de Machine Learning
- **Google ML Kit**
  - Text Recognition API
  - Digital Ink Recognition API
  - Translate API
  - Vision API
- **Google Generative AI** - IA generativa avanzada
- **Firebase ML Vision** - Procesamiento de imágenes

### Backend y Base de Datos
- **Firebase Firestore** - Base de datos NoSQL en tiempo real
- **Firebase Authentication** - Sistema de autenticación
- **Google Services** - Suite completa de servicios de Google

### Autenticación y Redes Sociales
- **Google Auth** - Autenticación con Google
- **Facebook SDK** - Integración con Facebook
- **Play Services Auth** - Servicios de autenticación de Google Play

### Herramientas de Desarrollo
- **Android Studio** - IDE principal
- **Gradle Kotlin DSL** - Sistema de construcción
- **Data Binding** - Vinculación de datos
- **ProGuard** - Ofuscación de código

##  Requisitos del Sistema

- **Android**: API nivel 28 (Android 9.0) o superior
- **Target SDK**: 34 (Android 14)
- **Almacenamiento**: Mínimo 100 MB disponibles
- **Conexión**: Internet requerida para funciones de IA y traducción
- **Permisos**: 
  - Cámara (para OCR)
  - Almacenamiento (para guardar documentos)
  - Ubicación (para servicios contextuales)

##  Instalación y Configuración

### Prerrequisitos
- Android Studio Arctic Fox o superior
- SDK de Android 28+
- Kotlin 1.9.0+
- Cuenta de Firebase activa
- API Keys de Google Cloud (para servicios ML)

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/SergiMPorto/MicroPen.git
   cd MicroPen
   ```

2. **Configurar Firebase**
   - Crear proyecto en [Firebase Console](https://console.firebase.google.com)
   - Descargar `google-services.json`
   - Colocar el archivo en `app/`

3. **Configurar APIs de Google Cloud**
   - Habilitar ML Kit APIs
   - Configurar Generative AI API
   - Obtener claves de API necesarias

4. **Configurar Facebook SDK**
   - Crear app en [Facebook Developers](https://developers.facebook.com)
   - Configurar claves en `strings.xml`

5. **Compilar y ejecutar**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

##  Estructura del Proyecto

```
MicroPen/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sergi/micropen/
│   │   │   │   ├── activities/          # Actividades principales
│   │   │   │   ├── fragments/           # Fragmentos de UI
│   │   │   │   ├── adapters/            # Adaptadores para RecyclerView
│   │   │   │   ├── models/              # Modelos de datos
│   │   │   │   ├── services/            # Servicios de background
│   │   │   │   ├── utils/               # Utilidades y helpers
│   │   │   │   └── ml/                  # Módulos de Machine Learning
│   │   │   ├── res/                     # Recursos (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml      # Manifiesto de la aplicación
│   │   ├── androidTest/                 # Tests instrumentados
│   │   └── test/                        # Tests unitarios
│   ├── build.gradle.kts                 # Configuración de construcción
│   ├── google-services.json             # Configuración de Firebase
│   └── proguard-rules.pro               # Reglas de ofuscación
├── gradle/                              # Wrapper de Gradle
├── build.gradle.kts                     # Configuración del proyecto
├── settings.gradle.kts                  # Configuración de módulos
└── README.md                            # Documentación del proyecto
```

##  Configuración de Desarrollo

### Variables de Entorno
Crear archivo `local.properties`:
```properties
# Google Services
google.api.key=TU_API_KEY_AQUI
generative.ai.key=TU_GENERATIVE_AI_KEY

# Facebook
facebook.app.id=TU_FACEBOOK_APP_ID
facebook.client.token=TU_FACEBOOK_CLIENT_TOKEN
```

### Dependencias Principales
```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:28.3.1"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")

// ML Kit
implementation("com.google.mlkit:digital-ink-recognition:18.1.0")
implementation("com.google.mlkit:text-recognition:16.0.0")
implementation("com.google.mlkit:translate:16.1.2")

// Generative AI
implementation("com.google.ai.client.generativeai:generativeai:0.6.0")

// UI Components
implementation("com.google.android.material:material:1.11.0")
implementation("de.hdodenhof:circleimageview:3.1.0")
```

##  Testing

### Ejecutar Tests Unitarios
```bash
./gradlew test
```

### Ejecutar Tests Instrumentados
```bash
./gradlew connectedAndroidTest
```

### Cobertura de Código
```bash
./gradlew jacocoTestReport
```

##  Uso de la Aplicación

### Funciones Principales

1. **Escritura Digital**
   - Abrir la pantalla de escritura
   - Escribir con el dedo o stylus
   - El texto se convierte automáticamente

2. **Escaneo de Texto**
   - Usar la cámara para capturar texto
   - Procesamiento automático con OCR
   - Edición y mejora del texto reconocido

3. **Traducción**
   - Seleccionar texto a traducir
   - Elegir idioma de destino
   - Traducción instantánea

4. **Asistencia de IA**
   - Solicitar mejoras de texto
   - Corrección automática
   - Sugerencias contextuales



