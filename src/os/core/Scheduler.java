package os.core;

import java.util.*;

/**
 * Planificador de procesos con múltiples algoritmos
 */
public class Scheduler implements FileSystemListener {
    private Queue<Process> readyQueue;
    private List<Process> newProcesses;      // procesos no aún movidos a ready
    private List<Process> completedProcesses;
    private Map<Integer, Process> waitingProcessesByPid; // procesos en WAITING (bloqueados por I/O)
    private Process currentProcess;
    private SchedulingAlgorithm algorithm;
    private int timeQuantum;  // Para Round Robin
    private int currentTime;
    private int currentQuantumUsed;

    public enum SchedulingAlgorithm {
        ROUND_ROBIN, SJF, PRIORITY
    }

    /**
     * Constructor del planificador
     */
    public Scheduler(SchedulingAlgorithm algorithm, int timeQuantum) {
        this.algorithm = algorithm;
        this.timeQuantum = timeQuantum;
        this.readyQueue = new LinkedList<>();
        this.newProcesses = new ArrayList<>();
        this.completedProcesses = new ArrayList<>();
        this.waitingProcessesByPid = new HashMap<>();
        this.currentTime = 0;
        this.currentQuantumUsed = 0;
        this.currentProcess = null;
    }

    /**
     * Añade un proceso al planificador (como NEW)
     */
    public void addProcess(Process process) {
        process.setState(Process.ProcessState.NEW);
        newProcesses.add(process);
    }

    /**
     * Añade procesos a la cola de listos según su tiempo de llegada
     */
    private void updateReadyQueue() {
        Iterator<Process> iterator = newProcesses.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.getArrivalTime() <= currentTime) {
                p.setState(Process.ProcessState.READY);
                addToReadyQueue(p);
                iterator.remove();
            }
        }
    }

    /**
     * Añade un proceso a la cola de listos según el algoritmo
     */
    private void addToReadyQueue(Process process) {
        if (algorithm == SchedulingAlgorithm.PRIORITY) {
            // Insertar ordenado por prioridad (menor = más prioritario)
            List<Process> temp = new ArrayList<>(readyQueue);
            temp.add(process);
            Collections.sort(temp); // utiliza compareTo de Process (prioridad)
            readyQueue = new LinkedList<>(temp);
        } else if (algorithm == SchedulingAlgorithm.SJF) {
            // Insertar ordenado por tiempo de ráfaga restante
            List<Process> temp = new ArrayList<>(readyQueue);
            temp.add(process);
            temp.sort(Comparator.comparingInt(Process::getRemainingTime));
            readyQueue = new LinkedList<>(temp);
        } else {
            // Round Robin: simplemente añadir al final
            readyQueue.add(process);
        }
    }

    /**
     * Selecciona el siguiente proceso a ejecutar
     */
    private Process selectNextProcess() {
        // Si el algoritmo es no-preemptive SJF/PRIORITY, aún tomamos el primer de la cola
        return readyQueue.poll();
    }

    /**
     * Ejecuta un paso del algoritmo de planificación
     * @return true si hay procesos ejecutándose o por llegar
     */
    public boolean executeStep() {
        // mover NEW -> READY según arrivalTime
        updateReadyQueue();

        // Si no hay proceso actual, seleccionar uno nuevo
        if (currentProcess == null || currentProcess.getState() == Process.ProcessState.TERMINATED) {
            currentProcess = selectNextProcess();
            currentQuantumUsed = 0;

            if (currentProcess != null) {
                currentProcess.setState(Process.ProcessState.RUNNING);
            }
        }

        // Si hay proceso actual, ejecutarlo
        if (currentProcess != null) {
            // Ejecutamos 1 unidad de tiempo
            currentProcess.execute(1);
            currentQuantumUsed++;
            currentTime++;

            // Verificar si el proceso terminó
            if (currentProcess.isCompleted()) {
                currentProcess.setState(Process.ProcessState.TERMINATED);
                currentProcess.calculateMetrics(currentTime);
                completedProcesses.add(currentProcess);
                currentProcess = null;
            }
            // Round Robin: verificar quantum
            else if (algorithm == SchedulingAlgorithm.ROUND_ROBIN &&
                     currentQuantumUsed >= timeQuantum) {
                currentProcess.setState(Process.ProcessState.READY);
                addToReadyQueue(currentProcess);
                currentProcess = null;
            }

            return true;
        }

        // Si no hay procesos listos pero quedan NEW por llegar o hay waiting processes
        if (!newProcesses.isEmpty() || !waitingProcessesByPid.isEmpty()) {
            currentTime++;
            // Podemos decidir avanzar tiempo aun si no hay proceso listo.
            return true;
        }

        // Todo terminó
        return false;
    }

    /**
     * Ejecuta la simulación completa
     */
    public void runComplete() {
        while (executeStep()) {
            // Continuar hasta que todos los procesos terminen
        }
    }

    /**
     * Obtiene las métricas de todos los procesos completados
     */
    public Map<String, Double> getMetrics() {
        List<Process> completed = getCompletedProcesses();

        if (completed.isEmpty()) {
            return new HashMap<>();
        }

        double avgWaitingTime = completed.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);

        double avgTurnaroundTime = completed.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);

        double cpuUtilization = calculateCPUUtilization(completed);

        Map<String, Double> metrics = new HashMap<>();
        metrics.put("avgWaitingTime", avgWaitingTime);
        metrics.put("avgTurnaroundTime", avgTurnaroundTime);
        metrics.put("cpuUtilization", cpuUtilization);
        metrics.put("throughput", (double) completed.size() / Math.max(1, currentTime));

        return metrics;
    }

    /**
     * Calcula la utilización de CPU
     */
    private double calculateCPUUtilization(List<Process> completed) {
        int totalBurstTime = completed.stream()
                .mapToInt(Process::getBurstTime)
                .sum();
        return currentTime > 0 ? (totalBurstTime * 100.0) / currentTime : 0.0;
    }

    /**
     * Obtiene todos los procesos completados
     */
    public List<Process> getCompletedProcesses() {
        return new ArrayList<>(completedProcesses);
    }

    /**
     * Obtener los procesos listos actuales (copia)
     */
    public List<Process> getReadyQueue() {
        return new ArrayList<>(readyQueue);
    }

    /**
     * Obtener procesos en estado WAITING (bloqueados por I/O)
     */
    public List<Process> getWaitingProcesses() {
        return new ArrayList<>(waitingProcessesByPid.values());
    }

    // Getters
    public Process getCurrentProcess() { return currentProcess; }
    public int getCurrentTime() { return currentTime; }
    public SchedulingAlgorithm getAlgorithm() { return algorithm; }
    public int getTimeQuantum() { return timeQuantum; }

    // Setters
    public void setAlgorithm(SchedulingAlgorithm algorithm) { this.algorithm = algorithm; }
    public void setTimeQuantum(int timeQuantum) { this.timeQuantum = timeQuantum; }

    /**
     * Reinicia el planificador
     */
    public void reset() {
        readyQueue.clear();
        newProcesses.clear();
        completedProcesses.clear();
        waitingProcessesByPid.clear();
        currentTime = 0;
        currentProcess = null;
        currentQuantumUsed = 0;
    }

    /**
     * FileSystemListener callbacks
     * Cuando el filesystem bloquea un proceso, el scheduler debe moverlo a WAITING
     * y sacarlo de la cola de listos / del CPU si aplica.
     */
    @Override
    public void processBlocked(int pid, String fileName) {
        // Buscar entre readyQueue y currentProcess
        // Si está en readyQueue lo removemos
        Iterator<Process> it = readyQueue.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p.getPid() == pid) {
                p.setState(Process.ProcessState.WAITING);
                waitingProcessesByPid.put(pid, p);
                it.remove();
                return;
            }
        }

        // Si es el proceso actual, moverlo a WAITING y liberar CPU
        if (currentProcess != null && currentProcess.getPid() == pid) {
            currentProcess.setState(Process.ProcessState.WAITING);
            waitingProcessesByPid.put(pid, currentProcess);
            currentProcess = null;
        }

        // También puede estar aún en newProcesses (no llegado a READY) - marcar waiting
        Iterator<Process> it2 = newProcesses.iterator();
        while (it2.hasNext()) {
            Process p = it2.next();
            if (p.getPid() == pid) {
                p.setState(Process.ProcessState.WAITING);
                waitingProcessesByPid.put(pid, p);
                it2.remove();
                return;
            }
        }
    }

    /**
     * Cuando FileSystem concede acceso desde su cola, el scheduler debe mover
     * el proceso de WAITING a READY (volver a la readyQueue).
     */
    @Override
    public void processUnblocked(int pid, String fileName) {
        Process p = waitingProcessesByPid.remove(pid);
        if (p != null) {
            p.setState(Process.ProcessState.READY);
            addToReadyQueue(p);
        }
    }
}