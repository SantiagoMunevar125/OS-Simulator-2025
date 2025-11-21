package os.core;

import java.util.*;

/**
 * Clase que representa un proceso en el simulador de Sistema Operativo
 */
public class Process implements Comparable<Process> {
    private int pid;                    // ID único del proceso
    private String name;                // Nombre del proceso
    private int priority;               // Prioridad (1-10, menor = más prioritario)
    private int burstTime;              // Tiempo de CPU requerido
    private int remainingTime;          // Tiempo restante de ejecución
    private int arrivalTime;            // Tiempo de llegada
    private int waitingTime;            // Tiempo de espera
    private int turnaroundTime;         // Tiempo de retorno
    private int completionTime;         // Tiempo de finalización
    private ProcessState state;         // Estado actual del proceso
    private List<Integer> requiredPages; // Páginas de memoria requeridas
    private List<String> requiredFiles; // Archivos que necesita acceder
    private int quantum;                // Quantum usado (para Round Robin)
    
    public enum ProcessState {
        NEW, READY, RUNNING, WAITING, TERMINATED
    }
    
    /**
     * Constructor completo para crear un proceso
     */
    public Process(int pid, String name, int priority, int burstTime, 
                   int arrivalTime, List<Integer> requiredPages, List<String> requiredFiles) {
        this.pid = pid;
        this.name = name;
        this.priority = priority;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.state = ProcessState.NEW;
        this.requiredPages = requiredPages != null ? new ArrayList<>(requiredPages) : new ArrayList<>();
        this.requiredFiles = requiredFiles != null ? new ArrayList<>(requiredFiles) : new ArrayList<>();
        this.quantum = 0;
    }
    
    /**
     * Ejecuta el proceso por un tiempo determinado
     * @param time Tiempo de ejecución
     * @return Tiempo realmente ejecutado
     */
    public int execute(int time) {
        int executedTime = Math.min(time, remainingTime);
        remainingTime -= executedTime;
        quantum += executedTime;
        
        if (remainingTime == 0) {
            state = ProcessState.TERMINATED;
        }
        
        return executedTime;
    }
    
    /**
     * Verifica si el proceso ha terminado
     */
    public boolean isCompleted() {
        return remainingTime == 0;
    }
    
    /**
     * Calcula las métricas del proceso
     */
    public void calculateMetrics(int currentTime) {
        if (state == ProcessState.TERMINATED) {
            this.completionTime = currentTime;
            this.turnaroundTime = completionTime - arrivalTime;
            this.waitingTime = turnaroundTime - burstTime;
        }
    }
    
    /**
     * Comparación para ordenamiento por prioridad
     */
    @Override
    public int compareTo(Process other) {
        return Integer.compare(this.priority, other.priority);
    }
    
    // Getters y Setters
    public int getPid() { return pid; }
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getArrivalTime() { return arrivalTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getCompletionTime() { return completionTime; }
    public ProcessState getState() { return state; }
    public List<Integer> getRequiredPages() { return requiredPages; }
    public List<String> getRequiredFiles() { return requiredFiles; }
    public int getQuantum() { return quantum; }
    
    public void setState(ProcessState state) { this.state = state; }
    public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }
    public void setCompletionTime(int completionTime) { this.completionTime = completionTime; }
    
    @Override
    public String toString() {
        return String.format("P%d(%s) - Priority:%d, Burst:%d, Remaining:%d, State:%s", 
                             pid, name, priority, burstTime, remainingTime, state);
    }
}