<<<<<<< Updated upstream
# OS-Simulator-2025
🔧 Simulador interactivo de Sistema Operativo en Java Incluye planificador de procesos, administración de memoria y sistema de archivos con bloqueo y colas de espera. Visualización en tiempo real mediante interfaz gráfica.
=======
# 🖥️ Simulador de Sistema Operativo

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Swing](https://img.shields.io/badge/GUI-Swing-green.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)

> **Simulador completo de Sistema Operativo** desarrollado en Java que implementa planificación de procesos, gestión de memoria con paginación por demanda y sistema de archivos con control de concurrencia.

**Proyecto Final - Sistemas Operativos 2025-2**

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Requisitos](#-requisitos)
- [Instalación](#-instalación)
- [Uso](#-uso)
- [Módulos del Sistema](#-módulos-del-sistema)
- [Algoritmos Implementados](#-algoritmos-implementados)
- [Casos de Prueba](#-casos-de-prueba)
- [Documentación Técnica](#-documentación-técnica)
- [Autores](#-autores)

---

## ✨ Características

### 🎯 Funcionalidades Principales

- **Planificación de Procesos**: Implementa 3 algoritmos clásicos (Round Robin, SJF, Prioridad)
- **Gestión de Memoria**: Paginación por demanda con 16 marcos de página
- **Sistema de Archivos**: Control de acceso concurrente con mutex y colas de espera
- **Interfaz Gráfica**: Dashboard interactivo con visualización en tiempo real
- **Métricas Detalladas**: Análisis completo de rendimiento del sistema
- **Detección de Deadlock**: Identificación y resolución automática de estancamientos
- **Simulación Paso a Paso**: Control granular para análisis detallado
- **Exportación de Resultados**: Reportes completos de ejecución

### 🎨 Interfaz de Usuario

- Dashboard con métricas en tiempo real
- Visualización de estado de procesos
- Mapa de memoria con marcos de página
- Monitor de sistema de archivos
- Control de velocidad de simulación
- Log de eventos detallado

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura **modular orientada a objetos** con separación clara de responsabilidades:

```
┌─────────────────────────────────────┐
│      OSSimulatorGUI (Vista)         │
│  ┌───────────┬────────┬──────────┐  │
│  │ Dashboard │ Sched  │  Memory  │  │
│  └───────────┴────────┴──────────┘  │
└──────────────┬──────────────────────┘
               │
    ┌──────────┴──────────┐
    │   Componentes Core  │
    ├─────────────────────┤
    │  • Scheduler        │
    │  • MemoryManager    │
    │  • FileSystem       │
    │  • Process          │
    └─────────────────────┘
```

### 📦 Clases Principales

| Clase | Responsabilidad |
|-------|----------------|
| `Process.java` | Modelo de proceso con estados y métricas | 
| `Scheduler.java` | Planificación con múltiples algoritmos |
| `MemoryManager.java` | Gestión de memoria y paginación | 
| `FileSystem.java` | Control de concurrencia en archivos | 
| `OSSimulatorGUI.java` | Interfaz gráfica y coordinación | 

---

## 💻 Requisitos

### Software Necesario

- **Java JDK**: 11 o superior
- **Sistema Operativo**: Windows, macOS o Linux
- **RAM**: Mínimo 512 MB
- **Espacio en disco**: 50 MB

### Dependencias

El proyecto **NO requiere dependencias externas**. Utiliza únicamente bibliotecas estándar de Java:

- `java.util.*` - Estructuras de datos
- `javax.swing.*` - Interfaz gráfica
- `java.awt.*` - Componentes visuales

---

## 🚀 Instalación

### Opción 1: Clonar desde GitHub

```bash
# Clonar el repositorio
git clone https://github.com/SantiagoMunevar125/OS-Simulator-2025

# Navegar al directorio
cd OS-Simulator-2025

# Compilar
javac *.java

# Ejecutar
java OSSimulatorGUI
```

### Opción 2: Descarga Directa

1. Descarga el archivo ZIP del proyecto
2. Descomprime en tu directorio preferido
3. Abre terminal/CMD en esa carpeta
4. Ejecuta los comandos de compilación

### Opción 3: IDE (Eclipse, IntelliJ, VS Code)

1. Importa el proyecto como "Java Project"
2. Asegúrate de tener JDK 11+ configurado
3. Ejecuta `OSSimulatorGUI.java` como clase principal

---

## 📖 Uso

### Inicio Rápido

1. **Ejecutar el simulador**:
   ```bash
   java OSSimulatorGUI
   ```

2. **Configurar parámetros** (opcional):
   - Selecciona el algoritmo de planificación
   - Ajusta el quantum para Round Robin
   - Elige algoritmo de reemplazo de páginas

3. **Iniciar simulación**:
   - Click en **▶ Iniciar** para modo automático
   - Click en **→ Paso** para ejecución controlada

4. **Analizar resultados**:
   - Revisa métricas en el Dashboard
   - Examina el log de eventos
   - Compara diferentes algoritmos

### Controles Principales

| Botón | Función |
|-------|---------|
| ▶ **Iniciar** | Ejecuta la simulación automáticamente |
| ⏸ **Pausar** | Detiene la simulación temporal |
| → **Paso** | Ejecuta un solo paso de CPU |
| ↻ **Reiniciar** | Resetea el simulador al estado inicial |
| 🎚️ **Velocidad** | Ajusta velocidad de simulación (100-2000ms) |

---

## 🔧 Módulos del Sistema

### 1️⃣ Planificador de Procesos

**Características:**
- Cola de procesos listos (Ready Queue)
- Gestión de estados (NEW → READY → RUNNING → TERMINATED)
- Cálculo automático de métricas
- Cambio de contexto controlado

**Métricas calculadas:**
- Tiempo de espera promedio
- Tiempo de retorno promedio
- Utilización de CPU
- Throughput del sistema

### 2️⃣ Gestor de Memoria

**Características:**
- 16 marcos de página física
- Tabla de páginas por proceso
- Contador de fallos y aciertos
- Visualización gráfica de marcos

**Algoritmos de reemplazo:**
- **FIFO**: First In First Out
- **LRU**: Least Recently Used

### 3️⃣ Sistema de Archivos

**Características:**
- 3 archivos compartidos simulados
- Mutex para exclusión mutua
- Colas de espera FIFO
- Log detallado de accesos

**Control de concurrencia:**
- Bloqueo de archivos (Lock/Unlock)
- Detección de conflictos
- Resolución automática de esperas

---

## 🧮 Algoritmos Implementados

### Planificación de Procesos

#### 🔄 Round Robin
```
- Quantum configurable (1-10 unidades)
- Cola circular FIFO
- Cambio de contexto al expirar quantum
- Equidad entre procesos
```

**Ventajas**: Equitativo, evita inanición  
**Desventajas**: Overhead por cambios de contexto frecuentes

#### ⚡ Shortest Job First (SJF)
```
- Ordena por tiempo de ráfaga restante
- Minimiza tiempo de espera promedio
- No preventivo en esta implementación
```

**Ventajas**: Óptimo para tiempo de espera  
**Desventajas**: Puede causar inanición de procesos largos

#### 🎯 Prioridad
```
- Ordena por valor de prioridad (1-10)
- Menor número = mayor prioridad
- Cola ordenada automáticamente
```

**Ventajas**: Control fino sobre ejecución  
**Desventajas**: Riesgo de inanición sin envejecimiento

### Reemplazo de Páginas

#### 📥 FIFO (First In First Out)
```java
// Pseudocódigo
Queue<Frame> fifoQueue;
victimFrame = fifoQueue.poll(); // Más antiguo
fifoQueue.offer(newFrame);      // Agregar al final
```

**Complejidad**: O(1)  
**Anomalía de Belady**: Puede ocurrir

#### 🕒 LRU (Least Recently Used)
```java
// Pseudocódigo
for (Frame f : frames) {
    if (f.lastAccessTime < oldest) {
        oldest = f.lastAccessTime;
        victimFrame = f;
    }
}
```

**Complejidad**: O(n)  
**Rendimiento**: Mejor que FIFO en localidad temporal

---

## 🧪 Casos de Prueba

### Conjunto de Pruebas Predefinido

El simulador genera automáticamente **8 procesos** con las siguientes características:

| PID | Prioridad | Burst Time | Arrival Time | Páginas | Archivos |
|-----|-----------|------------|--------------|---------|----------|
| P1  | 1-5       | 5-19       | 0-9          | 5       | 1-3      |
| P2  | 1-5       | 5-19       | 0-9          | 5       | 1-3      |
| ... | ...       | ...        | ...          | ...     | ...      |
| P8  | 1-5       | 5-19       | 0-9          | 5       | 1-3      |

### Escenarios de Prueba

#### ✅ Prueba 1: Comparación de Algoritmos
**Objetivo**: Medir rendimiento de Round Robin vs SJF vs Prioridad

**Pasos**:
1. Ejecutar con Round Robin (Q=4)
2. Anotar tiempo de espera promedio
3. Reiniciar y cambiar a SJF
4. Comparar resultados

**Resultado esperado**: SJF debería tener menor tiempo de espera

#### ✅ Prueba 2: Reemplazo de Páginas
**Objetivo**: Validar algoritmos FIFO y LRU

**Pasos**:
1. Ejecutar con FIFO
2. Contar fallos de página
3. Reiniciar con LRU
4. Comparar tasas de fallo

**Resultado esperado**: LRU debería tener menos fallos

#### ✅ Prueba 3: Detección de Deadlock
**Objetivo**: Verificar detección y resolución de estancamientos

**Pasos**:
1. Iniciar simulación
2. Observar ventana de deadlock (si ocurre)
3. Seleccionar "Forzar liberar y reiniciar"
4. Verificar que continúa la simulación

**Resultado esperado**: Sistema detecta y resuelve deadlock

#### ✅ Prueba 4: Acceso Concurrente
**Objetivo**: Validar mutex y colas de espera

**Pasos**:
1. Ir a pestaña "Sistema de Archivos"
2. Observar bloqueos y liberaciones
3. Contar conflictos en métricas

**Resultado esperado**: Conflictos resueltos sin pérdida de datos

### Resultados de Referencia

**Configuración**: 8 procesos, Round Robin Q=4, LRU

| Métrica | Valor Típico |
|---------|--------------|
| Tiempo de espera promedio | 40-50 ms |
| Tiempo de retorno promedio | 55-65 ms |
| Utilización CPU | 95-100% |
| Tasa de fallos de página | 10-15% |
| Conflictos de archivos | 60-90 |

---

## 📚 Documentación Técnica

### Estados del Proceso

```
NEW → Proceso creado, esperando admisión
  ↓
READY → En cola de listos, esperando CPU
  ↓
RUNNING → Ejecutándose en CPU
  ↓
WAITING → Bloqueado esperando recurso
  ↓
TERMINATED → Ejecución completada
```

### Estructura de Datos Principales

#### Process
```java
class Process {
    int pid;                    // Identificador único
    int priority;               // 1-10 (menor = más prioritario)
    int burstTime;              // Tiempo total de CPU
    int remainingTime;          // Tiempo restante
    ProcessState state;         // Estado actual
    List<Integer> pages;        // Páginas requeridas
    List<String> files;         // Archivos a acceder
}
```

#### PageFrame
```java
class PageFrame {
    int pageNumber;             // Número de página
    int processId;              // Proceso dueño
    boolean valid;              // Marco ocupado?
    int lastAccessTime;         // Para LRU
}
```

#### SimulatedFile
```java
class SimulatedFile {
    String name;                // Nombre del archivo
    boolean locked;             // Está bloqueado?
    int lockedByProcess;        // PID del dueño del lock
    Queue<Request> waitQueue;   // Cola de espera
}
```

### Fórmulas de Métricas

**Tiempo de Retorno (Turnaround Time)**:
```
TAT = Tiempo_Finalización - Tiempo_Llegada
```

**Tiempo de Espera (Waiting Time)**:
```
WT = Tiempo_Retorno - Tiempo_Ráfaga
```

**Utilización de CPU**:
```
CPU_Util = (Tiempo_Total_Ráfaga / Tiempo_Total_Simulación) × 100
```

**Tasa de Fallos de Página**:
```
Page_Fault_Rate = (Fallos / Total_Accesos) × 100
```

---

## 🎓 Decisiones de Diseño

### ¿Por qué GUI en una sola clase?

**Decisión**: Mantener `OSSimulatorGUI.java` como clase única para la interfaz.

**Justificación**:
- **Cohesión**: Todos los paneles están relacionados y comparten estado
- **Simplicidad**: Evita complejidad de paso de referencias entre clases
- **Actualización**: Facilita sincronización de vistas
- **Modularidad**: Los métodos `create*Panel()` encapsulan cada módulo

**Alternativa considerada**: Separar en `DashboardPanel`, `SchedulerPanel`, etc.  
**Rechazo**: Aumentaría complejidad sin beneficio significativo para proyecto académico

### Arquitectura de Componentes Core

**Decisión**: Clases independientes para Scheduler, Memory y FileSystem.

**Justificación**:
- **Reutilizabilidad**: Pueden usarse sin GUI
- **Testabilidad**: Fácil crear pruebas unitarias
- **Separación de Responsabilidades**: Cada clase tiene una función clara
- **Mantenibilidad**: Cambios en un módulo no afectan otros

---

## 🐛 Problemas Conocidos

### Deadlock Inicial
**Descripción**: Primera ejecución puede generar deadlock aleatorio.  
**Causa**: Configuración inicial de procesos accediendo simultáneamente.  
**Solución**: Sistema detecta y permite reiniciar automáticamente.  
**Estado**: Comportamiento intencional para demostración.

### Rendimiento en Simulación Rápida
**Descripción**: A máxima velocidad, la GUI puede no actualizarse suavemente.  
**Causa**: Swing no es thread-safe para actualizaciones muy frecuentes.  
**Workaround**: Reducir velocidad o usar modo paso a paso.  
**Estado**: Limitación de Swing, no crítico.


## 👥 Autores

**Santiago Munevar - Jeferson Madera** - Desarrollo completo del simulador  
**Santiago Munevar - Jeferson Madera** - Documentación y pruebas  
**Santiago Munevar** - Casos de prueba y validación  

**Curso**: Sistemas Operativos 2025-2  
**Institución**: Universidad Pedagogica y Tecnologica de Colombia  
**Profesor**: Nini Diaz

---

## 🙏 Agradecimientos

- Documentación oficial de Java Swing
- Libro "Operating System Concepts" de Silberschatz
- Comunidad de Stack Overflow
- Profesora del curso de Sistemas Operativos Nini Diaz

---

<div align="center">

**⭐ Si este proyecto te fue útil, considera darle una estrella ⭐**

Hecho con ❤️ para Sistemas Operativos 2025-2

</div>
>>>>>>> Stashed changes
