# SkySync ✈️🌦️

**SkySync** es una aplicación desarrollada en Java que cruza información meteorológica con datos de vuelos para analizar la posible correlación entre las condiciones climáticas y los retrasos. Utiliza APIs públicas para obtener datos en tiempo real y los almacena localmente para su posterior análisis.

---

## 🚀 Funcionalidades

- 🌦️ Consulta del clima actual por ciudad (temperatura, humedad, viento)
- ✈️ Consulta del estado de vuelos mediante código IATA (salida, llegada, estado)
- 💾 Persistencia de datos en base de datos SQLite
- 🔄 Captura de datos de forma periódica (modo automático)
- 📊 Pensado para análisis estadístico de correlación entre clima y retrasos

---

## 🛠️ Tecnologías Usadas

| Herramienta      | Rol                                  |
|------------------|---------------------------------------|
| Java 21          | Lenguaje principal                    |
| Maven            | Gestor de dependencias y compilación  |
| SQLite JDBC      | Motor de base de datos local          |
| OkHttp           | Cliente HTTP para consumo de APIs     |
| Gson             | Parseo de respuestas JSON             |
| IntelliJ IDEA    | Entorno de desarrollo recomendado     |

---

## 📦 Estructura del Proyecto

```bash
skysync/
├── README.md
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── Skysync/
                    ├── api/
                    │   ├── OpenWeatherAPI.java
                    │   └── AviationStackAPI.java
                    ├── database/
                    │   └── DatabaseManager.java
                    ├── models/
                    │   ├── Clima.java
                    │   └── Vuelo.java
                    ├── utils/
                    │   └── Config.java
                    └── Main.java
```

## 📄 Licencia
Este proyecto está desarrollado con fines educativos. Puedes modificarlo, adaptarlo o integrarlo a tus propios desarrollos libremente.

## 🙌 Autores
Raúl Mendoza Peña
Yain Estrada
Estudiantes del Grado en Ciencia e Ingeniería de Datos – ULPGC