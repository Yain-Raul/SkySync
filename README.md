# ✈️ SkySync – Sistema de análisis y predicción de vuelos en Canarias

SkySync es una herramienta desarrollada en Java que permite **analizar el comportamiento de los vuelos interinsulares de Canarias** y **predecir posibles cancelaciones o retrasos** en base a las condiciones climáticas reales.

## 🌍 ¿Qué hace SkySync?

✔️ Recoge **vuelos reales** desde aeropuertos de Canarias usando la API de [AviationStack](https://aviationstack.com/)  
✔️ Obtiene el **clima actual** de cada ciudad isleña con [OpenWeatherMap](https://openweathermap.org/)  
✔️ Almacena la información en una base de datos local SQLite  
✔️ Genera **informes diarios** con:
- Número de vuelos
- Vuelos cancelados y retrasados
- Temperatura, viento y humedad medias

✔️ Realiza **predicciones de probabilidad de cancelación** basadas en el clima actual

---

## 🔧 Tecnologías usadas

- **Java 21**
- **SQLite** como sistema de almacenamiento
- **AviationStack API** (vuelos reales)
- **OpenWeather API** (clima real)
- **OkHttp + Gson** para manejo de APIs
- Proyecto estructurado en módulos (`core`, `api`, `models`, `database`, `main`)

---

## Ejemplo de usp
🌤️ Bienvenido a SkySync

1️⃣ Recolectar vuelos actuales con AviationStack
2️⃣ Generar informe de un día
3️⃣ Predecir probabilidad de cancelación por clima
4️⃣ Recolección continua de vuelos en segundo plano


Informe diario generado:

📊 Informe del día: 2025-04-08
✈️ Total vuelos: 295
   Retrasados: 0 | Cancelados: 1
🌡️ Temperatura media: 20,7°C | 💨 Viento: 4,1 km/h | 💧 Humedad: 75%

Predicción por clima:

📍 Clima actual en Las Palmas: Clima{ciudad='Las Palmas', temperatura=22.3, humedad=74.0, velocidadViento=3.5}
🔮 Probabilidad estimada de cancelación/retraso: 0.0%
🧠 Futuras mejoras
Visualización con gráficos o interfaz web

Análisis de correlación entre clima y retrasos

Dashboard exportable como PDF o web

Soporte para vuelos de entrada y salida hacia la península

## 👨‍💻 Autor
Proyecto desarrollado por Raul Mendoza y Yain Estrada
Universidad de las Palmas de Gran Canaria | Ciencia e Ingeniería de Datos
Asignatura: Desarrollo de Aplicaciones en Ciencia de Datos (DACD)

