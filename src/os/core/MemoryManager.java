package os.core;

import java.util.*;

/**
 * Gestor de memoria con paginación por demanda y algoritmos de reemplazo
 */
public class MemoryManager {
    private int frameCount;              // Número total de marcos de página
    private Map<Integer, PageFrame> frames; // Marcos de página
    public enum PageReplacementAlgorithm { FIFO, LRU }
    private PageReplacementAlgorithm algorithm;
    private Queue<Integer> fifoQueue;    // Cola para FIFO
    private int accessCounter;           // Contador de accesos para LRU
    private int pageFaults;              // Contador de fallos de página
    private int pageHits;                // Contador de aciertos

    /**
     * Clase interna que representa un marco de página
     */
    public static class PageFrame {
        private int pageNumber;
        private int processId;
        private boolean valid;
        private int loadTime;
        private int lastAccessTime;

        public PageFrame() {
            this.valid = false;
            this.pageNumber = -1;
            this.processId = -1;
            this.loadTime = 0;
            this.lastAccessTime = 0;
        }

        public void load(int pageNumber, int processId, int time) {
            this.pageNumber = pageNumber;
            this.processId = processId;
            this.valid = true;
            this.loadTime = time;
            this.lastAccessTime = time;
        }

        public void access(int time) {
            this.lastAccessTime = time;
        }

        public void clear() {
            this.valid = false;
            this.pageNumber = -1;
            this.processId = -1;
        }

        // Getters
        public int getPageNumber() { return pageNumber; }
        public int getProcessId() { return processId; }
        public boolean isValid() { return valid; }
        public int getLoadTime() { return loadTime; }
        public int getLastAccessTime() { return lastAccessTime; }
    }

    /**
     * Constructor del gestor de memoria
     */
    public MemoryManager(int frameCount, PageReplacementAlgorithm algorithm) {
        this.frameCount = frameCount;
        this.algorithm = algorithm;
        this.frames = new HashMap<>();
        this.fifoQueue = new LinkedList<>();
        this.accessCounter = 0;
        this.pageFaults = 0;
        this.pageHits = 0;

        // Inicializar marcos
        for (int i = 0; i < frameCount; i++) {
            frames.put(i, new PageFrame());
        }
    }

    /**
     * Accede a una página de memoria
     * @return true si hay fallo de página
     */
    public boolean accessPage(int processId, int pageNumber) {
        accessCounter++;

        // Verificar si la página ya está en memoria
        for (PageFrame frame : frames.values()) {
            if (frame.isValid() &&
                frame.getProcessId() == processId &&
                frame.getPageNumber() == pageNumber) {
                // Page hit
                frame.access(accessCounter);
                pageHits++;
                return false;
            }
        }

        // Page fault - necesita cargar la página
        pageFaults++;
        loadPage(processId, pageNumber);
        return true;
    }

    /**
     * Carga una página en memoria
     */
    private void loadPage(int processId, int pageNumber) {
        // Buscar marco libre
        Integer freeFrame = findFreeFrame();

        if (freeFrame != null) {
            // Hay marco libre, cargar directamente
            frames.get(freeFrame).load(pageNumber, processId, accessCounter);
            updateAlgorithmStructures(freeFrame);
        } else {
            // No hay marcos libres, aplicar algoritmo de reemplazo
            int victimFrame = selectVictimFrame();
            PageFrame victim = frames.get(victimFrame);

            // Reemplazar
            victim.clear();
            victim.load(pageNumber, processId, accessCounter);
            updateAlgorithmStructures(victimFrame);
        }
    }

    /**
     * Encuentra un marco libre
     */
    private Integer findFreeFrame() {
        for (Map.Entry<Integer, PageFrame> entry : frames.entrySet()) {
            if (!entry.getValue().isValid()) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Selecciona un marco víctima según el algoritmo de reemplazo
     */
    private int selectVictimFrame() {
        if (algorithm == PageReplacementAlgorithm.FIFO) {
            return selectVictimFIFO();
        } else {
            return selectVictimLRU();
        }
    }

    /**
     * Selecciona víctima usando FIFO
     */
    private int selectVictimFIFO() {
        // Poll hasta encontrar un frame válido (evita entradas obsoletas)
        while (!fifoQueue.isEmpty()) {
            Integer candidate = fifoQueue.poll();
            PageFrame f = frames.get(candidate);
            if (f != null && f.isValid()) {
                return candidate;
            }
            // si no es válido, seguir buscando
        }
        // fallback
        return 0;
    }

    /**
     * Selecciona víctima usando LRU
     */
    private int selectVictimLRU() {
        int lruFrame = 0;
        int oldestAccess = Integer.MAX_VALUE;

        for (Map.Entry<Integer, PageFrame> entry : frames.entrySet()) {
            if (entry.getValue().isValid()) {
                int lastAccess = entry.getValue().getLastAccessTime();
                if (lastAccess < oldestAccess) {
                    oldestAccess = lastAccess;
                    lruFrame = entry.getKey();
                }
            }
        }

        return lruFrame;
    }

    /**
     * Actualiza las estructuras de datos del algoritmo
     */
    private void updateAlgorithmStructures(int frameNumber) {
        if (algorithm == PageReplacementAlgorithm.FIFO) {
            fifoQueue.offer(frameNumber);
        }
        // Para LRU no necesitamos estructuras adicionales: usamos lastAccessTime de PageFrame
    }

    /**
     * Libera todas las páginas de un proceso
     */
    public void freeProcessPages(int processId) {
        for (Map.Entry<Integer, PageFrame> entry : frames.entrySet()) {
            PageFrame frame = entry.getValue();
            if (frame.isValid() && frame.getProcessId() == processId) {
                frame.clear();
                // Remover de estructuras de algoritmo (fifo)
                fifoQueue.remove(entry.getKey());
            }
        }
    }

    /**
     * Obtiene el estado actual de la memoria
     */
    public List<PageFrame> getMemoryState() {
        // retornar en orden de frame index
        List<PageFrame> list = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            list.add(frames.get(i));
        }
        return list;
    }

    /**
     * Calcula la tasa de fallos de página
     */
    public double getPageFaultRate() {
        int totalAccesses = pageFaults + pageHits;
        return totalAccesses > 0 ? (pageFaults * 100.0) / totalAccesses : 0.0;
    }

    /**
     * Calcula la tasa de aciertos
     */
    public double getPageHitRate() {
        int totalAccesses = pageFaults + pageHits;
        return totalAccesses > 0 ? (pageHits * 100.0) / totalAccesses : 0.0;
    }

    /**
     * Obtiene métricas de memoria
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pageFaults", pageFaults);
        metrics.put("pageHits", pageHits);
        metrics.put("pageFaultRate", getPageFaultRate());
        metrics.put("pageHitRate", getPageHitRate());
        metrics.put("framesUsed", countUsedFrames());
        metrics.put("framesFree", frameCount - countUsedFrames());
        return metrics;
    }

    /**
     * Cuenta los marcos en uso
     */
    private int countUsedFrames() {
        int count = 0;
        for (PageFrame frame : frames.values()) {
            if (frame.isValid()) {
                count++;
            }
        }
        return count;
    }

    // Getters
    public int getFrameCount() { return frameCount; }
    public int getPageFaults() { return pageFaults; }
    public int getPageHits() { return pageHits; }
    public PageReplacementAlgorithm getAlgorithm() { return algorithm; }

    /**
     * Reinicia el gestor de memoria
     */
    public void reset() {
        for (PageFrame frame : frames.values()) {
            frame.clear();
        }
        fifoQueue.clear();
        accessCounter = 0;
        pageFaults = 0;
        pageHits = 0;
    }
}