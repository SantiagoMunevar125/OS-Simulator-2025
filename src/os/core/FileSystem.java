package os.core;

import java.util.*;

/**
 * Sistema de archivos simulado con control de concurrencia.
 * Ahora notifica a un FileSystemListener cuando un proceso es bloqueado
 * o tiene acceso concedido desde la cola.
 */
public class FileSystem {
    private Map<String, SimulatedFile> files;
    private Map<String, Queue<FileRequest>> waitingQueues;
    private List<FileAccessLog> accessLog;
    private int conflictCount;
    private FileSystemListener listener; // listener para notificar bloqueos/desbloqueos

    /**
     * Clase interna que representa un archivo simulado
     */
    public static class SimulatedFile {
        private String name;
        private String content;
        private boolean locked;
        private int lockedByProcess;
        private FileAccessType currentAccessType;
        private int readCount;
        private int writeCount;

        public enum FileAccessType {
            READ, WRITE, NONE
        }

        public SimulatedFile(String name, String content) {
            this.name = name;
            this.content = content;
            this.locked = false;
            this.lockedByProcess = -1;
            this.currentAccessType = FileAccessType.NONE;
            this.readCount = 0;
            this.writeCount = 0;
        }

        // Getters y setters
        public String getName() { return name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
        public int getLockedByProcess() { return lockedByProcess; }
        public void setLockedByProcess(int pid) { this.lockedByProcess = pid; }
        public FileAccessType getCurrentAccessType() { return currentAccessType; }
        public void setCurrentAccessType(FileAccessType type) { this.currentAccessType = type; }
        public int getReadCount() { return readCount; }
        public void incrementReadCount() { this.readCount++; }
        public int getWriteCount() { return writeCount; }
        public void incrementWriteCount() { this.writeCount++; }
    }

    /**
     * Clase que representa una solicitud de acceso a archivo
     */
    public static class FileRequest {
        private int processId;
        private String fileName;
        private SimulatedFile.FileAccessType accessType;
        private long requestTime;
        private long grantedTime;
        private boolean granted;

        public FileRequest(int processId, String fileName,
                          SimulatedFile.FileAccessType accessType, long requestTime) {
            this.processId = processId;
            this.fileName = fileName;
            this.accessType = accessType;
            this.requestTime = requestTime;
            this.granted = false;
        }

        public void grant(long grantedTime) {
            this.granted = true;
            this.grantedTime = grantedTime;
        }

        public long getWaitTime() {
            return granted ? (grantedTime - requestTime) : 0;
        }

        // Getters
        public int getProcessId() { return processId; }
        public String getFileName() { return fileName; }
        public SimulatedFile.FileAccessType getAccessType() { return accessType; }
        public long getRequestTime() { return requestTime; }
        public boolean isGranted() { return granted; }
    }

    /**
     * Clase que registra accesos a archivos
     */
    public static class FileAccessLog {
        private int processId;
        private String fileName;
        private SimulatedFile.FileAccessType accessType;
        private long timestamp;
        private boolean success;
        private String message;

        public FileAccessLog(int processId, String fileName,
                             SimulatedFile.FileAccessType accessType,
                             long timestamp, boolean success, String message) {
            this.processId = processId;
            this.fileName = fileName;
            this.accessType = accessType;
            this.timestamp = timestamp;
            this.success = success;
            this.message = message;
        }

        // Getters
        public int getProcessId() { return processId; }
        public String getFileName() { return fileName; }
        public SimulatedFile.FileAccessType getAccessType() { return accessType; }
        public long getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format("[%d] P%d %s %s - %s (%s)",
                    timestamp, processId, accessType, fileName,
                    success ? "SUCCESS" : "FAILED", message);
        }
    }

    /**
     * Constructor del sistema de archivos
     */
    public FileSystem() {
        this.files = new HashMap<>();
        this.waitingQueues = new HashMap<>();
        this.accessLog = new ArrayList<>();
        this.conflictCount = 0;
        this.listener = null;
    }

    /**
     * Setea el listener (Scheduler) para notificaciones de bloqueo/desbloqueo
     */
    public void setListener(FileSystemListener listener) {
        this.listener = listener;
    }

    /**
     * Crea un archivo en el sistema
     */
    public void createFile(String fileName, String content) {
        if (!files.containsKey(fileName)) {
            files.put(fileName, new SimulatedFile(fileName, content));
            waitingQueues.put(fileName, new LinkedList<>());
        }
    }

    /**
     * Solicita acceso a un archivo
     * @return true si el acceso fue concedido inmediatamente
     */
    public boolean requestAccess(int processId, String fileName,
                                 SimulatedFile.FileAccessType accessType) {
        long currentTime = System.currentTimeMillis();

        if (!files.containsKey(fileName)) {
            logAccess(processId, fileName, accessType, currentTime,
                    false, "File not found");
            return false;
        }

        SimulatedFile file = files.get(fileName);

        // Verificar si el archivo está libre
        if (!file.isLocked()) {
            // Conceder acceso inmediatamente
            file.setLocked(true);
            file.setLockedByProcess(processId);
            file.setCurrentAccessType(accessType);

            if (accessType == SimulatedFile.FileAccessType.READ) {
                file.incrementReadCount();
            } else {
                file.incrementWriteCount();
            }

            logAccess(processId, fileName, accessType, currentTime,
                    true, "Access granted immediately");
            return true;
        } else {
            // Archivo ocupado, añadir a cola de espera
            conflictCount++;
            FileRequest request = new FileRequest(processId, fileName,
                    accessType, currentTime);
            waitingQueues.get(fileName).offer(request);

            logAccess(processId, fileName, accessType, currentTime,
                    false, String.format("Blocked by P%d, added to waiting queue",
                            file.getLockedByProcess()));

            // Notificar al scheduler/listener que el proceso está bloqueado
            if (listener != null) {
                listener.processBlocked(processId, fileName);
            }

            return false;
        }
    }

    /**
     * Libera el acceso a un archivo
     */
    public void releaseAccess(int processId, String fileName) {
        long currentTime = System.currentTimeMillis();

        if (!files.containsKey(fileName)) {
            return;
        }

        SimulatedFile file = files.get(fileName);

        // Verificar que el proceso que libera sea el dueño del lock
        if (file.isLocked() && file.getLockedByProcess() == processId) {
            // Guardamos el currentAccessType anterior para el log
            SimulatedFile.FileAccessType prevType = file.getCurrentAccessType();

            file.setLocked(false);
            file.setLockedByProcess(-1);
            file.setCurrentAccessType(SimulatedFile.FileAccessType.NONE);

            logAccess(processId, fileName, prevType,
                    currentTime, true, "File released");

            // Procesar cola de espera
            processWaitingQueue(fileName);
        }
    }

    /**
     * Procesa la cola de espera de un archivo
     */
    private void processWaitingQueue(String fileName) {
        Queue<FileRequest> queue = waitingQueues.get(fileName);

        if (queue != null && !queue.isEmpty()) {
            FileRequest nextRequest = queue.poll();
            long currentTime = System.currentTimeMillis();

            // Conceder acceso al siguiente en la cola
            SimulatedFile file = files.get(fileName);
            file.setLocked(true);
            file.setLockedByProcess(nextRequest.getProcessId());
            file.setCurrentAccessType(nextRequest.getAccessType());

            if (nextRequest.getAccessType() == SimulatedFile.FileAccessType.READ) {
                file.incrementReadCount();
            } else {
                file.incrementWriteCount();
            }

            nextRequest.grant(currentTime);

            logAccess(nextRequest.getProcessId(), fileName,
                    nextRequest.getAccessType(), currentTime,
                    true, String.format("Access granted from queue (waited %dms)",
                            nextRequest.getWaitTime()));

            // Notificar al scheduler/listener que el proceso fue desbloqueado
            if (listener != null) {
                listener.processUnblocked(nextRequest.getProcessId(), fileName);
            }
        }
    }

    /**
     * Lee el contenido de un archivo
     */
    public String readFile(int processId, String fileName) {
        if (!files.containsKey(fileName)) {
            return null;
        }

        SimulatedFile file = files.get(fileName);

        // Verificar que el proceso tiene acceso
        if (file.isLocked() && file.getLockedByProcess() == processId) {
            return file.getContent();
        }

        return null;
    }

    /**
     * Escribe contenido en un archivo
     */
    public boolean writeFile(int processId, String fileName, String content) {
        if (!files.containsKey(fileName)) {
            return false;
        }

        SimulatedFile file = files.get(fileName);

        // Verificar que el proceso tiene acceso y es escritura
        if (file.isLocked() &&
                file.getLockedByProcess() == processId &&
                file.getCurrentAccessType() == SimulatedFile.FileAccessType.WRITE) {
            file.setContent(content);
            return true;
        }

        return false;
    }

    /**
     * Registra un acceso en el log
     */
    private void logAccess(int processId, String fileName,
                           SimulatedFile.FileAccessType accessType,
                           long timestamp, boolean success, String message) {
        FileAccessLog log = new FileAccessLog(processId, fileName,
                accessType, timestamp, success, message);
        accessLog.add(log);
    }

    /**
     * Obtiene las métricas del sistema de archivos
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        int totalAccesses = accessLog.size();
        long successfulAccesses = accessLog.stream()
                .filter(FileAccessLog::isSuccess)
                .count();

        metrics.put("totalFiles", files.size());
        metrics.put("totalAccesses", totalAccesses);
        metrics.put("successfulAccesses", successfulAccesses);
        metrics.put("conflicts", conflictCount);
        metrics.put("successRate", totalAccesses > 0 ?
                (successfulAccesses * 100.0) / totalAccesses : 0.0);

        return metrics;
    }

    /**
     * Obtiene el estado de todos los archivos
     */
    public List<SimulatedFile> getAllFiles() {
        return new ArrayList<>(files.values());
    }

    /**
     * Obtiene el log de accesos
     */
    public List<FileAccessLog> getAccessLog() {
        return new ArrayList<>(accessLog);
    }

    /**
     * Obtiene la cola de espera de un archivo
     */
    public Queue<FileRequest> getWaitingQueue(String fileName) {
        return waitingQueues.getOrDefault(fileName, new LinkedList<>());
    }

    /**
     * Reinicia el sistema de archivos
     */
    public void reset() {
        for (SimulatedFile file : files.values()) {
            file.setLocked(false);
            file.setLockedByProcess(-1);
            file.setCurrentAccessType(SimulatedFile.FileAccessType.NONE);
        }
        for (Queue<FileRequest> queue : waitingQueues.values()) {
            queue.clear();
        }
        accessLog.clear();
        conflictCount = 0;
        listener = null;
    }
}