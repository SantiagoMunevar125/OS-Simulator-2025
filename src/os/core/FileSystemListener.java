package os.core;

/**
 * Interfaz ligera para notificar al Scheduler cuando un proceso es bloqueado
 * o desbloqueado por el FileSystem.
 */
public interface FileSystemListener {
    /**
     * Notifica que el proceso con pid fue bloqueado (p. ej. añadido a la cola)
     */
    void processBlocked(int pid, String fileName);

    /**
     * Notifica que el proceso con pid fue desbloqueado y ya puede volver a READY
     */
    void processUnblocked(int pid, String fileName);
}