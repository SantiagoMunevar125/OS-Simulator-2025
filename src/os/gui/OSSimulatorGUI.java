package os.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;

import os.core.Process;
import os.core.Scheduler;
import os.core.MemoryManager;
import os.core.FileSystem;

/**
 * Interfaz gráfica principal del Simulador de Sistema Operativo
 */
public class OSSimulatorGUI extends JFrame {
    // Componentes del simulador
    private Scheduler scheduler;
    private MemoryManager memoryManager;
    private FileSystem fileSystem;

    // Componentes de UI
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private JPanel schedulerPanel;
    private JPanel memoryPanel;
    private JPanel fileSystemPanel;

    // Tablas
    private JTable processTable;
    private DefaultTableModel processTableModel;
    private JTable memoryTable;
    private DefaultTableModel memoryTableModel;
    private JTable fileTable;
    private DefaultTableModel fileTableModel;
    private JTextArea logArea;
    private JTextArea fileLogArea;

    // Métricas
    private JLabel lblAvgWaitTime;
    private JLabel lblAvgTurnaroundTime;
    private JLabel lblCPUUtilization;
    private JLabel lblPageFaults;
    private JLabel lblPageHits;
    private JLabel lblFileConflicts;

    // Control de simulación
    private javax.swing.Timer simulationTimer;
    private boolean isRunning;
    private int simulationSpeed = 500; // ms

    // Visual frames for memory
    private JLabel[] frameContentLabels;

    public OSSimulatorGUI() {
        initializeComponents();
        setupUI();
        createSampleData();
    }

    /**
     * Inicializa los componentes del simulador
     */
    private void initializeComponents() {
        // Crear scheduler primero (para pasarlo como listener si se necesita)
        scheduler = new Scheduler(Scheduler.SchedulingAlgorithm.ROUND_ROBIN, 4);
        memoryManager = new MemoryManager(16, MemoryManager.PageReplacementAlgorithm.LRU);
        fileSystem = new FileSystem();
        // Registrar scheduler como listener del fileSystem para bloqueos/desbloqueos
        fileSystem.setListener(scheduler);
        isRunning = false;
    }

    /**
     * Configura la interfaz de usuario
     */
    private void setupUI() {
        setTitle("Simulador de Sistema Operativo - Proyecto SO 2025-2");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 245));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Crear paneles
        dashboardPanel = createDashboardPanel();
        schedulerPanel = createSchedulerPanel();
        memoryPanel = createMemoryPanel();
        fileSystemPanel = createFileSystemPanel();

        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        tabbedPane.addTab("⚙️ Planificador", schedulerPanel);
        tabbedPane.addTab("💾 Memoria", memoryPanel);
        tabbedPane.addTab("📁 Sistema de Archivos", fileSystemPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Crea el panel de encabezado
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Simulador de Sistema Operativo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Planificación | Memoria | Sistema de Archivos");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    /**
     * Crea el panel de dashboard
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Métricas principales
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsPanel.setOpaque(false);

        metricsPanel.add(createMetricCard("Tiempo de Espera Promedio",
                lblAvgWaitTime = new JLabel("0.0 ms")));
        metricsPanel.add(createMetricCard("Tiempo de Retorno Promedio",
                lblAvgTurnaroundTime = new JLabel("0.0 ms")));
        metricsPanel.add(createMetricCard("Utilización de CPU",
                lblCPUUtilization = new JLabel("0.0%")));
        metricsPanel.add(createMetricCard("Fallos de Página",
                lblPageFaults = new JLabel("0")));
        metricsPanel.add(createMetricCard("Aciertos de Página",
                lblPageHits = new JLabel("0")));
        metricsPanel.add(createMetricCard("Conflictos de Archivos",
                lblFileConflicts = new JLabel("0")));

        panel.add(metricsPanel, BorderLayout.NORTH);

        // Log de eventos
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Registro de Eventos",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 152, 219)));
        logPanel.setBackground(Color.WHITE);

        logArea = new JTextArea(15, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        panel.add(logPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea una tarjeta de métrica
     */
    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(236, 240, 241));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(127, 140, 141));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(52, 73, 94));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Crea el panel del planificador
     */
    private JPanel createSchedulerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Configuración
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración"));

        configPanel.add(new JLabel("Algoritmo:"));
        JComboBox<String> algoCombo = new JComboBox<>(
                new String[]{"Round Robin", "SJF", "Prioridad"});
        algoCombo.addActionListener(e -> {
            String selected = (String) algoCombo.getSelectedItem();
            if (selected.equals("Round Robin")) {
                scheduler.setAlgorithm(Scheduler.SchedulingAlgorithm.ROUND_ROBIN);
            } else if (selected.equals("SJF")) {
                scheduler.setAlgorithm(Scheduler.SchedulingAlgorithm.SJF);
            } else {
                scheduler.setAlgorithm(Scheduler.SchedulingAlgorithm.PRIORITY);
            }
            log("Algoritmo cambiado a: " + selected);
        });
        configPanel.add(algoCombo);

        configPanel.add(new JLabel("Quantum:"));
        JSpinner quantumSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        quantumSpinner.addChangeListener(e -> {
            scheduler.setTimeQuantum((Integer) quantumSpinner.getValue());
        });
        configPanel.add(quantumSpinner);

        panel.add(configPanel, BorderLayout.NORTH);

        // Tabla de procesos
        String[] columns = {"PID", "Nombre", "Prioridad", "T. Ráfaga",
                "T. Restante", "T. Espera", "T. Retorno", "Estado"};
        processTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        processTable = new JTable(processTableModel);
        processTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        processTable.setRowHeight(25);
        processTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        processTable.getTableHeader().setBackground(new Color(52, 152, 219));
        processTable.getTableHeader().setForeground(Color.WHITE);
        fixTableHeader(processTable, new Color(52, 152, 219), Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(processTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Procesos"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de memoria
     */
    private JPanel createMemoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Configuración
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración"));

        configPanel.add(new JLabel("Algoritmo de Reemplazo:"));
        JComboBox<String> algoCombo = new JComboBox<>(new String[]{"FIFO", "LRU"});
        algoCombo.addActionListener(e -> {
            String selected = (String) algoCombo.getSelectedItem();
            if (selected.equals("FIFO")) {
                memoryManager = new MemoryManager(16, MemoryManager.PageReplacementAlgorithm.FIFO);
            } else {
                memoryManager = new MemoryManager(16, MemoryManager.PageReplacementAlgorithm.LRU);
            }
            log("Algoritmo de memoria cambiado a: " + selected);
            updateMemoryView();
            updateMetrics();
        });
        configPanel.add(algoCombo);

        panel.add(configPanel, BorderLayout.NORTH);

        // Visualización de marcos
        JPanel framesPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        framesPanel.setBorder(BorderFactory.createTitledBorder("Marcos de Página (16 total)"));
        framesPanel.setBackground(Color.WHITE);

        frameContentLabels = new JLabel[16];
        for (int i = 0; i < 16; i++) {
            JPanel framePanel = new JPanel(new BorderLayout());
            framePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            framePanel.setBackground(new Color(236, 240, 241));

            JLabel frameLabel = new JLabel("Marco " + i, SwingConstants.CENTER);
            frameLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            JLabel contentLabel = new JLabel("Libre", SwingConstants.CENTER);
            contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));

            framePanel.add(frameLabel, BorderLayout.NORTH);
            framePanel.add(contentLabel, BorderLayout.CENTER);

            frameContentLabels[i] = contentLabel; // guardar referencia para actualizar luego
            framesPanel.add(framePanel);
        }

        panel.add(framesPanel, BorderLayout.CENTER);

        // Tabla de estadísticas de memoria
        String[] columns = {"Marco", "Página", "Proceso", "Última Acceso"};
        memoryTableModel = new DefaultTableModel(columns, 0);
        memoryTable = new JTable(memoryTableModel);
        memoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fixTableHeader(memoryTable, new Color(189, 195, 199), Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(memoryTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detalles de Memoria"));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel del sistema de archivos
     */
    private JPanel createFileSystemPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tabla de archivos
        String[] columns = {"Archivo", "Estado", "Bloqueado por", "Lecturas", "Escrituras"};
        fileTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable = new JTable(fileTableModel);
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileTable.setRowHeight(25);
        fileTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        fileTable.getTableHeader().setBackground(new Color(46, 204, 113));
        fileTable.getTableHeader().setForeground(Color.WHITE);
        fixTableHeader(fileTable, new Color(46, 204, 113), Color.WHITE);


        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Estado de Archivos"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Log de accesos
        fileLogArea = new JTextArea(10, 40);
        fileLogArea.setEditable(false);
        fileLogArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(fileLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Registro de Accesos"));
        panel.add(logScroll, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de control
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnStart = createStyledButton("▶ Iniciar", new Color(46, 204, 113));
        JButton btnPause = createStyledButton("⏸ Pausar", new Color(241, 196, 15));
        JButton btnReset = createStyledButton("↻ Reiniciar", new Color(231, 76, 60));
        JButton btnStep = createStyledButton("→ Paso", new Color(52, 152, 219));

        btnStart.addActionListener(e -> startSimulation());
        btnPause.addActionListener(e -> pauseSimulation());
        btnReset.addActionListener(e -> resetSimulation());
        btnStep.addActionListener(e -> stepSimulation());

        panel.add(btnStart);
        panel.add(btnPause);
        panel.add(btnStep);
        panel.add(btnReset);

        // Control de velocidad
        panel.add(new JLabel("Velocidad:"));
        JSlider speedSlider = new JSlider(100, 2000, 500);
        speedSlider.setInverted(true);
        speedSlider.addChangeListener(e -> {
            simulationSpeed = speedSlider.getValue();
            if (simulationTimer != null && simulationTimer.isRunning()) {
                simulationTimer.setDelay(simulationSpeed);
            }
        });
        panel.add(speedSlider);

        return panel;
    }

    /**
     * Crea un botón estilizado
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Crea datos de ejemplo para la simulación
     */
    private void createSampleData() {
        // Crear procesos de ejemplo
        Random rand = new Random();
        for (int i = 1; i <= 8; i++) {
            List<Integer> pages = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                pages.add(rand.nextInt(30));
            }

            List<String> files = new ArrayList<>();
            files.add("file" + (rand.nextInt(3) + 1) + ".txt");

            Process p = new Process(
                    i,
                    "P" + i,
                    rand.nextInt(5) + 1,
                    rand.nextInt(15) + 5,
                    rand.nextInt(10),
                    pages,
                    files
            );
            scheduler.addProcess(p);
        }

        // Crear archivos de ejemplo
        fileSystem.createFile("file1.txt", "Contenido inicial 1");
        fileSystem.createFile("file2.txt", "Contenido inicial 2");
        fileSystem.createFile("file3.txt", "Contenido inicial 3");

        log("Sistema inicializado con 8 procesos y 3 archivos");
        updateAllViews();
    }

    /**
     * Inicia la simulación
     */
    private void startSimulation() {
        if (!isRunning) {
            isRunning = true;
            simulationTimer = new javax.swing.Timer(simulationSpeed, e -> stepSimulation());
            simulationTimer.start();
            log("Simulación iniciada");
        }
    }

    /**
     * Pausa la simulación
     */
    private void pauseSimulation() {
        if (isRunning && simulationTimer != null) {
            simulationTimer.stop();
            isRunning = false;
            log("Simulación pausada");
        }
    }

    /**
     * Reinicia la simulación
     */
    private void resetSimulation() {
        pauseSimulation();
        scheduler.reset();
        memoryManager.reset();
        fileSystem.reset();
        logArea.setText("");
        createSampleData();
        log("Simulación reiniciada");
    }

    /**
     * Ejecuta un paso de la simulación
     */
    private void stepSimulation() {
        boolean hasWork = scheduler.executeStep();

        if (hasWork) {
            Process current = scheduler.getCurrentProcess();
            if (current != null) {
                // Simular acceso a memoria
                for (int page : current.getRequiredPages()) {
                    boolean pageFault = memoryManager.accessPage(current.getPid(), page);
                    if (pageFault) {
                        log(String.format("P%d: Fallo de página %d",
                                current.getPid(), page));
                    }
                }

                // Simular acceso a archivos (READ por defecto)
                for (String file : current.getRequiredFiles()) {
                    boolean granted = fileSystem.requestAccess(current.getPid(), file,
                            FileSystem.SimulatedFile.FileAccessType.READ);
                    if (!granted) {
                        // requestAccess ya notificó al scheduler (listener), así que aquí sólo logeamos.
                        log(String.format("P%d: Bloqueado por archivo %s", current.getPid(), file));
                    }
                }
            }

            updateAllViews();

            // --- DEADLOCK DETECTION ---
            // Si se detecta que no hay CURRENT ni READY pero sí WAITING -> posible deadlock
            if (isRunning && detectDeadlock()) {
                handleDeadlock();
            }
            // --------------------------

        } else {
            pauseSimulation();
            log("Simulación completada - Todos los procesos han terminado");
            showFinalReport();
        }
    }

    /**
     * Detecta deadlock simple: no hay proceso current, la cola READY está vacía
     * y existen procesos en WAITING.
     */
    private boolean detectDeadlock() {
        boolean noCurrent = scheduler.getCurrentProcess() == null;
        boolean readyEmpty = scheduler.getReadyQueue().isEmpty();
        // scheduler.getWaitingProcesses() debe existir en tu Scheduler (lo usas ya en updateProcessTable)
        boolean waitingExists = !scheduler.getWaitingProcesses().isEmpty();

        return noCurrent && readyEmpty && waitingExists;
    }

    /**
     * Maneja un deadlock detectado: muestra diálogo con opciones.
     * Opciones:
     *  - Pausar (mantener estado tal cual)
     *  - Forzar liberar + reiniciar simulación (resuelve deadlock)
     */
    private void handleDeadlock() {
        // Pausar la simulación antes de mostrar diálogo
        if (simulationTimer != null && simulationTimer.isRunning()) {
            simulationTimer.stop();
        }
        isRunning = false;

        String message = "Se detectó que TODOS los procesos están bloqueados esperando archivos.\n\n"
                + "Esto es un deadlock/estancamiento en la simulación.\n\n"
                + "¿Qué desea hacer?\n\n"
                + "Pausar: mantener el estado actual (recomendado si quieres inspeccionar logs).\n\n"
                + "Forzar liberar y reiniciar: liberará los locks y reiniciará la simulación (perderás el estado actual).";

        String[] options = {"Pausar (mantener)", "Forzar liberar y reiniciar"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Deadlock detectado",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0 || choice == JOptionPane.CLOSED_OPTION) {
            // Mantener pausado
            log("Deadlock detectado: simulación en pausa (usuario eligió mantener estado).");
            // isRunning ya es false y timer detenido.
        } else {
            // Forzar liberar y reiniciar para continuar
            log("Deadlock detectado: forzando liberación y reiniciando simulación (usuario eligió continuar).");
            // Reset completo: esto liberará locks, limpiará colas y reiniciará datos de ejemplo
            resetSimulation();
            // Nota: resetSimulation pausa la simulación y recrea datos
        }
    }

    /**
     * Actualiza todas las vistas
     */
    private void updateAllViews() {
        updateProcessTable();
        updateMemoryView();
        updateFileSystemView();
        updateMetrics();
    }

    /**
     * Actualiza la tabla de procesos
     */
    private void updateProcessTable() {
        processTableModel.setRowCount(0);

        List<Process> display = new ArrayList<>();
        Process current = scheduler.getCurrentProcess();
        if (current != null) display.add(current);

        display.addAll(scheduler.getReadyQueue());
        display.addAll(scheduler.getWaitingProcesses()); // mostrar WAITING también
        display.addAll(scheduler.getCompletedProcesses());

        for (Process p : display) {
            processTableModel.addRow(new Object[]{
                    p.getPid(),
                    p.getName(),
                    p.getPriority(),
                    p.getBurstTime(),
                    p.getRemainingTime(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime(),
                    p.getState()
            });
        }
    }

    /**
     * Actualiza la vista de memoria
     */
    private void updateMemoryView() {
        memoryTableModel.setRowCount(0);

        List<MemoryManager.PageFrame> frames = memoryManager.getMemoryState();
        int frameNum = 0;
        for (MemoryManager.PageFrame frame : frames) {
            if (frame.isValid()) {
                memoryTableModel.addRow(new Object[]{
                        frameNum,
                        frame.getPageNumber(),
                        "P" + frame.getProcessId(),
                        frame.getLastAccessTime()
                });
                frameContentLabels[frameNum].setText(String.format("P%d - Pg %d", frame.getProcessId(), frame.getPageNumber()));
            } else {
                // libre
                frameContentLabels[frameNum].setText("Libre");
            }
            frameNum++;
        }
    }

    /**
     * Actualiza la vista del sistema de archivos
     */
    private void updateFileSystemView() {
        fileTableModel.setRowCount(0);

        for (FileSystem.SimulatedFile file : fileSystem.getAllFiles()) {
            fileTableModel.addRow(new Object[]{
                    file.getName(),
                    file.isLocked() ? "Bloqueado" : "Libre",
                    file.isLocked() ? "P" + file.getLockedByProcess() : "-",
                    file.getReadCount(),
                    file.getWriteCount()
            });
        }

        // actualizar log de archivos
        fileLogArea.setText("");
        for (FileSystem.FileAccessLog entry : fileSystem.getAccessLog()) {
            fileLogArea.append(entry.toString() + "\n");
        }
    }

    /**
     * Actualiza las métricas
     */
    private void updateMetrics() {
        Map<String, Double> schedMetrics = scheduler.getMetrics();
        Map<String, Object> memMetrics = memoryManager.getMetrics();
        Map<String, Object> fileMetrics = fileSystem.getMetrics();

        // === ¿Hay datos de planificación? ===
        boolean hasSchedData = !schedMetrics.isEmpty();

        if (!hasSchedData) {
            // Antes de que algún proceso termine solo mostramos "Esperando..."
            lblAvgWaitTime.setText("⏳ Esperando datos…");
            lblAvgTurnaroundTime.setText("⏳ Esperando datos…");
            lblCPUUtilization.setText("⏳ Esperando datos…");

            Color softGray = new Color(120, 120, 120);
            lblAvgWaitTime.setForeground(softGray);
            lblAvgTurnaroundTime.setForeground(softGray);
            lblCPUUtilization.setForeground(softGray);

            // Tooltips explicativos
            lblAvgWaitTime.setToolTipText("Se mostrará cuando al menos un proceso termine.");
            lblAvgTurnaroundTime.setToolTipText("Debe completarse algún proceso para calcular esta métrica.");
            lblCPUUtilization.setToolTipText("La CPU requiere datos completos de ejecución para calcular su uso.");

        } else {
            lblAvgWaitTime.setText(String.format("%.2f ms",
                    schedMetrics.getOrDefault("avgWaitingTime", 0.0)));
            lblAvgTurnaroundTime.setText(String.format("%.2f ms",
                    schedMetrics.getOrDefault("avgTurnaroundTime", 0.0)));
            lblCPUUtilization.setText(String.format("%.1f%%",
                    schedMetrics.getOrDefault("cpuUtilization", 0.0)));

            // Volver a color normal
            Color normal = new Color(52, 73, 94);
            lblAvgWaitTime.setForeground(normal);
            lblAvgTurnaroundTime.setForeground(normal);
            lblCPUUtilization.setForeground(normal);

            // Tooltips con explicación
            lblAvgWaitTime.setToolTipText("Promedio de tiempo que los procesos esperaron en la cola de listos.");
            lblAvgTurnaroundTime.setToolTipText("Tiempo total desde la llegada del proceso hasta su finalización.");
            lblCPUUtilization.setToolTipText("Porcentaje del tiempo en que la CPU estuvo ocupada.");
        }

        // === MEMORIA ===
        lblPageFaults.setText(memMetrics.get("pageFaults").toString());
        lblPageHits.setText(memMetrics.get("pageHits").toString());
        lblPageFaults.setToolTipText("Número total de fallos de página ocurridos.");
        lblPageHits.setToolTipText("Accesos que no requirieron traer la página desde memoria secundaria.");

        // === ARCHIVOS ===
        lblFileConflicts.setText(fileMetrics.get("conflicts").toString());
        lblFileConflicts.setToolTipText("Cantidad de procesos que intentaron acceder al mismo archivo simultáneamente.");
    }


    /**
     * Muestra el reporte final
     */
    private void showFinalReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== REPORTE FINAL DE SIMULACIÓN ===\n\n");

        Map<String, Double> schedMetrics = scheduler.getMetrics();
        report.append("PLANIFICACIÓN:\n");
        report.append(String.format("- Tiempo de espera promedio: %.2f ms\n",
                schedMetrics.getOrDefault("avgWaitingTime", 0.0)));
        report.append(String.format("- Tiempo de retorno promedio: %.2f ms\n",
                schedMetrics.getOrDefault("avgTurnaroundTime", 0.0)));
        report.append(String.format("- Utilización de CPU: %.1f%%\n\n",
                schedMetrics.getOrDefault("cpuUtilization", 0.0)));

        Map<String, Object> memMetrics = memoryManager.getMetrics();
        report.append("MEMORIA:\n");
        report.append(String.format("- Fallos de página: %s\n",
                memMetrics.get("pageFaults")));
        report.append(String.format("- Tasa de fallos: %.2f%%\n\n",
                memMetrics.get("pageFaultRate")));

        Map<String, Object> fileMetrics = fileSystem.getMetrics();
        report.append("SISTEMA DE ARCHIVOS:\n");
        report.append(String.format("- Total de accesos: %s\n",
                fileMetrics.get("totalAccesses")));
        report.append(String.format("- Conflictos: %s\n",
                fileMetrics.get("conflicts")));

        log(report.toString());
    }

    /**
     * Registra un mensaje en el log
     */
    private void log(String message) {
        logArea.append(String.format("[%s] %s\n",
                java.time.LocalTime.now().toString(), message));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * Método principal
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            OSSimulatorGUI gui = new OSSimulatorGUI();
            gui.setVisible(true);
        });
    }

    private void fixTableHeader(JTable table, Color bg, Color fg) {
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                label.setBackground(bg);
                label.setForeground(fg);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
    }

}