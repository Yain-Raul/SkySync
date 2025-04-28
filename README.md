# ✈️ SkySync – Análisis y Predicción de Vuelos y Clima en Canarias

SkySync es un sistema de análisis orientado a eventos que permite:
- Recolectar información real de vuelos y clima en las Islas Canarias.
- Analizar estados de vuelos (retrasos, cancelaciones) y condiciones climáticas.
- Detectar condiciones meteorológicas extremas y alertas combinadas.
- Trabajar tanto en **tiempo real** como con **eventos históricos** almacenados.

---

## 📚 Tecnologías utilizadas
- **Java 21**
- **ActiveMQ** como broker de eventos
- **SQLite** como base de datos (`datamart.db`)
- **AviationStack API** para información de vuelos
- **OpenWeatherMap API** para datos meteorológicos
- **Gson** y **OkHttp** para consumo de APIs REST
- **Arquitectura orientada a eventos (Publisher/Subscriber)**

---

## 🔥 Funcionalidades principales

| Funcionalidad | Descripción |
|:---|:---|
| 1️⃣ Recolectar vuelos y clima actuales con APIs |
| 2️⃣ Generar informe de un día sobre vuelos y clima |
| 3️⃣ Predecir probabilidad de cancelación por clima actual |
| 4️⃣ Recolección continua de vuelos en segundo plano |
| 5️⃣ Iniciar EventStoreBuilder para almacenar eventos en archivos `.events` |
| 6️⃣ Iniciar BusinessUnit para análisis de clima y vuelos en tiempo real |
| 7️⃣ Ver resumen de clima promedio por ciudad |
| 8️⃣ Detectar condiciones meteorológicas extremas |
| 9️⃣ Ver estado de vuelos (retrasados y cancelados) |
| 🔟 Cargar eventos históricos en el datamart |
| 1️⃣1️⃣ Detectar alerta combinada clima + vuelos |

---

## 📂 Estructura de carpetas

```
SkySync/
├── src/com/Skysync/
│   ├── api/               # Consumo de APIs OpenWeather y AviationStack
│   ├── business/           # BusinessUnit, DatamartManager (análisis de eventos)
│   ├── core/               # Collectors de datos y lógica principal
│   ├── events/             # Modelos de eventos
│   ├── messaging/          # Publisher de eventos a ActiveMQ
│   ├── models/             # Clases Clima y Vuelo
│   ├── store/              # EventStoreBuilder (almacenamiento de eventos)
│   └── main/               # Clase principal SkySync
├── eventstore/             # Archivos históricos `.events`
├── datamart.db             # Base de datos SQLite unificada
└── README.md               # Este documento
```

---

## 🛠️ Cómo ejecutar el proyecto

1. Instalar y arrancar ActiveMQ en localhost (`activemq.bat start` o `./activemq start`).
2. Ejecutar `SkySync.java` desde IntelliJ IDEA o terminal.
3. Utilizar el menú interactivo para realizar las acciones deseadas.

---

## 🧪 Ejemplo de eventos generados

```json
// Evento de clima (WeatherEvent)
{
  "ts": "2025-04-28T15:00:00Z",
  "ss": "feederA",
  "data": {
    "ciudad": "Las Palmas",
    "temperatura": 21.4,
    "humedad": 60.0,
    "velocidadViento": 12.5,
    "condicion": "Rain"
  }
}

// Evento de vuelo (Vuelo)
{
  "numeroVuelo": "IB1234",
  "aerolinea": "Iberia",
  "aeropuertoSalida": "Gran Canaria Airport",
  "aeropuertoSalidaIATA": "LPA",
  "aeropuertoLlegada": "Tenerife North",
  "estado": "delayed"
}
```

---




## 👨‍💻 Autores

Proyecto desarrollado por Raúl Mendoza Peña y Yain Estrada Domínguez  
Universidad de Las Palmas de Gran Canaria – Ciencia e Ingeniería de Datos  
Asignatura: Desarrollo de Aplicaciones en Ciencia de Datos (DACD)

