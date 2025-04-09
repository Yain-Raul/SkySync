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

1️⃣ Recolectar vuelos y clima actuales con AviationStack
2️⃣ Generar informe de un día
3️⃣ Predecir probabilidad de cancelación por clima. Opciones: LPA, TFN, TFS, ACE, FUE, SPC, GMZ, VDE
4️⃣ Recolección continua de vuelos en segundo plano

Elige una opción:


📊 Informe del día: 2025-04-09
✈️ Total vuelos en Canarias: 104
Retrasados en Canarias: 0 | Cancelados: 0
🌡️ Temperatura media en Canarias: 20,7°C | 💨 Viento: 5,5 km/h | 💧 Humedad: 67%

Predicción por clima:

📍 Clima actual en Las Palmas: Clima{ciudad='Las Palmas', temperatura=22.3, humedad=74.0, velocidadViento=3.5}
🔮 Probabilidad estimada de cancelación/retraso: 0.0%
🧠 Futuras mejoras
Visualización con gráficos o interfaz web


3️⃣ Predecir probabilidad de cancelación por clima. Opciones: LPA, TFN, TFS, ACE, FUE, SPC, GMZ, VDE
Introduce el código de tu aeropuerto: LPA
📡 Llamando a OpenWeather: https://api.openweathermap.org/data/2.5/weather?q=Las Palmas&appid=e3ab094c405a366e4715800f0ac15040&units=metric

📍 Aeropuerto LPA (Las Palmas)
🌤️ Clima actual: Clima{ciudad='Las Palmas', temperatura=20,4, humedad=73,0, viento=4,6, condición='Clouds'}
🔮 Riesgo estimado: BAJO (0,0%)

## 👨‍💻 Autor
Proyecto desarrollado por Raul Mendoza y Yain Estrada
Universidad de las Palmas de Gran Canaria | Ciencia e Ingeniería de Datos
Asignatura: Desarrollo de Aplicaciones en Ciencia de Datos (DACD)

