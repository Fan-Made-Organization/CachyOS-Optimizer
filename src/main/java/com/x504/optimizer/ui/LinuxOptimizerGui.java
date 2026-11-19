package com.x504.optimizer.ui;

import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.system.AdvancedDiagnostics;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.PlatformHardwareInspector;
import com.x504.optimizer.monitor.LiveMetrics;
import com.x504.optimizer.update.GitHubUpdater;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.ArrayDeque;
import java.util.Deque;

/** Interfaz gráfica Swing: panel de diagnóstico de solo lectura. */
public final class LinuxOptimizerGui {
    private static final Color BG = new Color(13, 16, 25);
    private static final Color PANEL = new Color(23, 28, 41);
    private static final Color PANEL_ALT = new Color(29, 36, 52);
    private static final Color ACCENT = new Color(132, 99, 255);
    private static final Color TEXT = new Color(235, 239, 249);
    private static final Color MUTED = new Color(151, 162, 184);
    private final JFrame frame = new JFrame("Linux Optimizer");
    private final AnimatedPanel content = new AnimatedPanel();
    private final JLabel status = new JLabel("Preparado");
    private final JLabel distro = new JLabel("Detectando sistema…");
    private final JLabel cpu = new JLabel("—");
    private final JLabel ram = new JLabel("—");
    private final JLabel gpu = new JLabel("—");
    private final JTextArea details = new JTextArea();
    private String activePage = "Resumen";
    private final Map<String, JToggleButton> navigation = new LinkedHashMap<>();
    private final ButtonGroup navigationGroup = new ButtonGroup();
    private JButton updateButton;
    private final LiveMetrics liveMetrics = new LiveMetrics();
    private final Deque<Double> cpuHistory = new ArrayDeque<>();
    private final Deque<Double> ramHistory = new ArrayDeque<>();
    private final Deque<Double> gpuHistory = new ArrayDeque<>();
    private final JLabel liveCpu = new JLabel("—");
    private final JLabel liveRam = new JLabel("—");
    private final JLabel liveTemp = new JLabel("No disponible");
    private final JLabel liveGpu = new JLabel("No expuesta por el controlador");
    private final ChartPanel cpuChart = new ChartPanel("CPU", cpuHistory);
    private final ChartPanel ramChart = new ChartPanel("RAM", ramHistory);
    private final ChartPanel gpuChart = new ChartPanel("GPU", gpuHistory);
    private javax.swing.Timer liveTimer;
    private AdvancedDiagnostics.Report lastReport;
    private String portableDetails;
    private PlatformHardwareInspector.Report hardwareReport;

    private LinuxOptimizerGui() {
        build();
        refresh();
        checkUpdates(false);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            LinuxOptimizerGui app = new LinuxOptimizerGui();
            app.frame.setVisible(true);
            app.startLiveMonitoring();
        });
    }

    private void build() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setSize(1180, 780);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout());
        frame.add(sidebar(), BorderLayout.WEST);
        JPanel shell = new JPanel(new BorderLayout(0, 18));
        shell.setBackground(BG);
        shell.setBorder(new EmptyBorder(28, 30, 24, 30));
        shell.add(header(), BorderLayout.NORTH);
        content.setOpaque(false);
        shell.add(content, BorderLayout.CENTER);
        shell.add(footer(), BorderLayout.SOUTH);
        frame.add(shell, BorderLayout.CENTER);
        showPage("Resumen");
    }

    private JPanel sidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(226, 0));
        side.setBackground(new Color(17, 21, 32));
        side.setBorder(new EmptyBorder(28, 18, 22, 18));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("<html><span style='color:#9B83FF'>◆</span> LINUX<br>&nbsp;&nbsp;OPTIMIZER</html>");
        brand.setForeground(TEXT);
        brand.setFont(new Font("SansSerif", Font.BOLD, 19));
        brand.setBorder(new EmptyBorder(0, 8, 30, 0));
        side.add(brand);
        JLabel caption = new JLabel("CENTRO DE CONTROL");
        caption.setForeground(MUTED);
        caption.setFont(new Font("SansSerif", Font.BOLD, 10));
        caption.setBorder(new EmptyBorder(0, 8, 12, 0));
        side.add(caption);
        for (String page : List.of("Resumen", "CPU y memoria", "Gráficos", "Almacenamiento", "Red", "Sistema", "Gaming")) {
            JToggleButton button = new JToggleButton(page);
            button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
            button.setPreferredSize(new Dimension(190, 43));
            button.setFont(new Font("SansSerif", Font.PLAIN, 13));
            button.setForeground(MUTED);
            button.setBackground(side.getBackground());
            button.setBorder(new EmptyBorder(0, 13, 0, 10));
            button.setRolloverEnabled(true);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> showPage(page));
            navigation.put(page, button);
            navigationGroup.add(button);
            side.add(button);
            side.add(Box.createVerticalStrut(5));
        }
        side.add(Box.createVerticalGlue());
        JLabel safe = new JLabel("<html><font color='#8D9AB2'>● MODO SEGURO<br>Diagnóstico de solo lectura</font></html>");
        safe.setFont(new Font("SansSerif", Font.PLAIN, 11));
        safe.setBorder(new EmptyBorder(12, 8, 0, 0));
        side.add(safe);
        return side;
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("LINUX / SYSTEM INTELLIGENCE");
        eyebrow.setForeground(ACCENT);
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel title = new JLabel("Panel de control");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        distro.setForeground(MUTED);
        distro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titles.add(eyebrow); titles.add(Box.createVerticalStrut(5)); titles.add(title);
        titles.add(Box.createVerticalStrut(5)); titles.add(distro);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        actions.setOpaque(false);
        updateButton = new JButton("⇧  Buscar actualizaciones");
        styleButton(updateButton);
        updateButton.setBackground(PANEL_ALT);
        updateButton.addActionListener(e -> checkUpdates(true));
        JButton refresh = new JButton("↻  Actualizar");
        styleButton(refresh);
        refresh.addActionListener(e -> refresh());
        actions.add(updateButton);
        actions.add(refresh);
        p.add(titles, BorderLayout.WEST);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    private JPanel footer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        status.setForeground(MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel version = new JLabel("LINUX OPTIMIZER  •  PREVIEW");
        version.setForeground(new Color(103, 115, 139));
        version.setFont(new Font("SansSerif", Font.BOLD, 10));
        p.add(status, BorderLayout.WEST);
        p.add(version, BorderLayout.EAST);
        return p;
    }

    private void showPage(String page) {
        int oldIndex = List.of("Resumen", "CPU y memoria", "Gráficos", "Almacenamiento", "Red", "Sistema", "Gaming").indexOf(activePage);
        int newIndex = List.of("Resumen", "CPU y memoria", "Gráficos", "Almacenamiento", "Red", "Sistema", "Gaming").indexOf(page);
        int travelDirection = newIndex >= oldIndex ? 1 : -1;
        activePage = page;
        navigation.forEach((name, button) -> {
            boolean selected = name.equals(page);
            button.setSelected(selected);
            button.setForeground(selected ? Color.WHITE : MUTED);
            button.setBackground(selected ? new Color(65, 52, 105) : new Color(17, 21, 32));
            button.setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 13));
        });
        content.removeAll();
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (page.equals("Resumen")) {
            JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
            cards.setOpaque(false);
            cards.add(metricCard("PROCESADOR", cpu));
            cards.add(metricCard("MEMORIA RAM", ram));
            cards.add(metricCard("GRÁFICOS", gpu));
            body.add(cards);
            body.add(Box.createVerticalStrut(18));
            body.add(section("Vista general", "Estado detectado del equipo y resumen de los componentes."));
            body.add(section("Diagnóstico", "Selecciona una categoría en el menú lateral para consultar información detallada."));
            body.add(Box.createVerticalStrut(14));
            JPanel liveCards = new JPanel(new GridLayout(1, 4, 12, 0));
            liveCards.setOpaque(false);
            liveCards.add(metricCard("CPU EN VIVO", liveCpu));
            liveCards.add(metricCard("RAM EN VIVO", liveRam));
            liveCards.add(metricCard("TEMPERATURA MÁX.", liveTemp));
            liveCards.add(metricCard("GPU EN VIVO", liveGpu));
            body.add(liveCards);
            body.add(Box.createVerticalStrut(14));
            JPanel charts = new JPanel(new GridLayout(1, 2, 12, 0));
            charts.setOpaque(false);
            charts.add(cpuChart);
            charts.add(ramChart);
            body.add(charts);
            body.add(Box.createVerticalStrut(12));
            body.add(gpuChart);
        } else {
            body.add(section(page, description(page)));
            body.add(Box.createVerticalStrut(14));
            if (page.equals("CPU y memoria")) {
                JPanel charts = new JPanel(new GridLayout(1, 2, 12, 0));
                charts.setOpaque(false);
                charts.add(cpuChart);
                charts.add(ramChart);
                body.add(charts);
                body.add(Box.createVerticalStrut(12));
                body.add(metricCard("TEMPERATURA MÁXIMA DETECTADA", liveTemp));
            } else if (page.equals("Gráficos")) {
                body.add(gpuChart);
                body.add(Box.createVerticalStrut(12));
                body.add(metricCard("USO GPU", liveGpu));
            } else {
                JPanel charts = new JPanel(new GridLayout(1, 2, 12, 0));
                charts.setOpaque(false);
                charts.add(cpuChart);
                charts.add(ramChart);
                body.add(charts);
            }
        }
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        details.setForeground(new Color(211, 220, 237));
        details.setBackground(PANEL);
        details.setBorder(new EmptyBorder(17, 18, 17, 18));
        JScrollPane scroll = new JScrollPane(details);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(PANEL);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        details.setWrapStyleWord(true);
        body.add(scroll);
        JScrollPane pageScroll = new JScrollPane(body);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(20);
        content.add(pageScroll, BorderLayout.CENTER);
        content.revalidate();
        content.startTransition(travelDirection);
        renderDetails();
    }

    private JPanel metricCard(String label, JLabel value) {
        JPanel card = new GlassPanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel heading = new JLabel(label);
        heading.setForeground(MUTED);
        heading.setFont(new Font("SansSerif", Font.BOLD, 10));
        value.setForeground(TEXT);
        value.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel section(String title, String subtitle) {
        JPanel p = new GlassPanel(new BorderLayout(0, 7));
        p.setBorder(new EmptyBorder(18, 19, 18, 19));
        JLabel h = new JLabel(title);
        h.setForeground(TEXT);
        h.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel s = new JLabel("<html>" + subtitle + "</html>");
        s.setForeground(MUTED);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(h, BorderLayout.NORTH);
        p.add(s, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private String description(String page) {
        return switch (page) {
            case "CPU y memoria" -> "Modelo, frecuencia, gobernador, RAM, swap y presión del kernel.";
            case "Gráficos" -> "Adaptadores DRM, controladores y herramientas de aceleración detectables.";
            case "Almacenamiento" -> "Dispositivos de bloque, planificador y sistemas de archivos montados.";
            case "Red" -> "Interfaces, enlace y ruta IPv4 predeterminada.";
            case "Gaming" -> "Herramientas disponibles para juegos en Linux.";
            default -> "Kernel activo, servicios fallidos y detalles del sistema.";
        };
    }

    private void checkUpdates(boolean showDialog) {
        if (updateButton != null) {
            updateButton.setEnabled(false);
            updateButton.setText("Comprobando…");
        }
        new SwingWorker<GitHubUpdater.Result, Void>() {
            protected GitHubUpdater.Result doInBackground() throws Exception {
                return new GitHubUpdater().checkAndStage();
            }
            protected void done() {
                try {
                    GitHubUpdater.Result result = get();
                    status.setText(result.message());
                    if (showDialog) JOptionPane.showMessageDialog(frame, result.message(), "Actualizaciones", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    String message = "No se pudo comprobar GitHub: " + (ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
                    status.setText(message);
                    if (showDialog) JOptionPane.showMessageDialog(frame, message, "Actualizaciones", JOptionPane.WARNING_MESSAGE);
                } finally {
                    if (updateButton != null) {
                        updateButton.setEnabled(true);
                        updateButton.setText("⇧  Buscar actualizaciones");
                    }
                }
            }
        }.execute();
    }

    private void startLiveMonitoring() {
        if (liveTimer != null) liveTimer.stop();
        liveTimer = new javax.swing.Timer(1000, event -> {
            LiveMetrics.Sample sample = liveMetrics.sample();
            addSample(cpuHistory, sample.cpuPercent());
            addSample(ramHistory, sample.memoryPercent());
            if (sample.gpuPercent() >= 0) addSample(gpuHistory, sample.gpuPercent());
            liveCpu.setText(String.format("%.0f%%", sample.cpuPercent()));
            liveRam.setText(String.format("%.0f%%", sample.memoryPercent()));
            liveTemp.setText(sample.temperatureCelsius() < 0 ? "No disponible" : String.format("%.1f °C", sample.temperatureCelsius()));
            liveGpu.setText(sample.gpuPercent() < 0 ? "No expuesta" : String.format("%.0f%%", sample.gpuPercent()));
            cpuChart.repaint();
            ramChart.repaint();
            gpuChart.repaint();
        });
        liveTimer.start();
    }

    private static void addSample(Deque<Double> history, double value) {
        history.addLast(value);
        while (history.size() > 60) history.removeFirst();
    }

    private void refresh() {
        if (!isLinuxHost()) {
            refreshPortableHost();
            return;
        }
        status.setText("Analizando el sistema Linux…");
        new SwingWorker<AdvancedDiagnostics.Report, Void>() {
            protected AdvancedDiagnostics.Report doInBackground() {
                return new AdvancedDiagnostics().inspect();
            }
            protected void done() {
                try {
                    lastReport = get();
                    var info = new LinuxSystemDetector().detect();
                    var hw = new HardwareDetector().detect();
                    distro.setText(info.distro() + "  •  " + info.kernel() + "  •  " + info.desktop());
                    cpu.setText(hw.processor());
                    ram.setText(hw.totalMemoryKiB() > 0 ? String.format("%.1f GiB", hw.totalMemoryKiB()/1024.0/1024.0) : "No disponible");
                    gpu.setText(hw.graphicsAdapters().isEmpty() ? "No detectada" : hw.graphicsAdapters().get(0));
                    status.setText("Actualizado • " + java.time.LocalTime.now().withNano(0));
                    renderDetails();
                    refreshLinuxHardwareInventory();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setText("Actualización interrumpida");
                } catch (ExecutionException ex) {
                    status.setText("Error al consultar el sistema: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void refreshLinuxHardwareInventory() {
        new SwingWorker<PlatformHardwareInspector.Report, Void>() {
            @Override protected PlatformHardwareInspector.Report doInBackground() {
                return new PlatformHardwareInspector().inspect();
            }
            @Override protected void done() {
                try {
                    hardwareReport = get();
                    renderDetails();
                } catch (Exception ex) {
                    status.setText("Inventario adicional no disponible");
                }
            }
        }.execute();
    }
    private static boolean isLinuxHost() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("linux");
    }

    /** Resumen portable para Windows y macOS; los módulos de bajo nivel siguen siendo Linux-only. */
    private void refreshPortableHost() {
        String os = System.getProperty("os.name", "Sistema desconocido");
        String version = System.getProperty("os.version", "");
        String arch = System.getProperty("os.arch", "");
        String javaVersion = System.getProperty("java.version", "");
        var bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        long total = bean instanceof com.sun.management.OperatingSystemMXBean mx ? mx.getTotalMemorySize() : -1;
        cpu.setText(Runtime.getRuntime().availableProcessors() + " procesadores lógicos");
        ram.setText(total > 0 ? String.format("%.1f GiB", total / 1073741824.0) : "No disponible");
        gpu.setText("Consultando adaptadores…");
        distro.setText(os + "  •  " + version + "  •  " + arch);
        portableDetails = "LINUX OPTIMIZER · MODO MULTIPLATAFORMA\n\n"
                + "Sistema operativo: " + os + "\nVersión: " + version
                + "\nArquitectura: " + arch + "\nJava: " + javaVersion
                + "\nProcesadores lógicos: " + Runtime.getRuntime().availableProcessors()
                + "\nMemoria física total: " + (total > 0 ? String.format("%.2f GiB", total / 1073741824.0) : "No expuesta por la JVM")
                + "\n\nLa interfaz, las gráficas de CPU/RAM y el actualizador funcionan en este sistema."
                + "\nEl inventario de GPU y almacenamiento usa las herramientas nativas disponibles."
                + "\nLas métricas de temperatura y uso GPU dependen de los sensores/controladores del fabricante."
                + "\nNo se ejecutan pruebas de estrés ni se modifican dispositivos."; 
        status.setText("Modo multiplataforma • " + java.time.LocalTime.now().withNano(0));
        renderDetails();
        new SwingWorker<PlatformHardwareInspector.Report, Void>() {
            @Override protected PlatformHardwareInspector.Report doInBackground() {
                return new PlatformHardwareInspector().inspect();
            }
            @Override protected void done() {
                try {
                    PlatformHardwareInspector.Report report = get();
                    gpu.setText(report.graphics().isEmpty() ? "No detectada" : report.graphics().get(0));
                    portableDetails += "\n\n━━ GPU / GRÁFICOS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                            + String.join("\n", report.graphics())
                            + "\n\n━━ ALMACENAMIENTO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                            + String.join("\n", report.storage());
                    renderDetails();
                } catch (Exception ex) {
                    portableDetails += "\n\nNo se pudo completar el inventario de hardware: " + ex.getMessage();
                    renderDetails();
                }
            }
        }.execute();
    }
    private void renderDetails() {
        if (!isLinuxHost() && portableDetails != null) {
            details.setText(portableDetails);
            details.setCaretPosition(0);
            return;
        }
        if (lastReport == null) { details.setText("Esperando el primer análisis…"); return; }
        List<String> lines = switch (activePage) {
            case "CPU y memoria" -> combine("CPU", lastReport.cpu(), "MEMORIA", lastReport.memory());
            case "Gráficos" -> combine("GPU Y STACK GRÁFICO", lastReport.graphics());
            case "Almacenamiento" -> combine("ALMACENAMIENTO", lastReport.storage());
            case "Red" -> combine("RED", lastReport.network());
            case "Gaming" -> combine("GAMING", lastReport.gaming());
            case "Sistema" -> combine("SERVICIOS", lastReport.services(), "KERNEL", lastReport.kernel());
            default -> combine("CPU", lastReport.cpu(), "MEMORIA", lastReport.memory(),
                    "ALMACENAMIENTO", lastReport.storage(), "GRÁFICOS", lastReport.graphics(),
                    "RED", lastReport.network(), "SERVICIOS", lastReport.services(),
                    "KERNEL", lastReport.kernel(), "GAMING", lastReport.gaming());
        };
        if (hardwareReport != null && (activePage.equals("Gráficos") || activePage.equals("Almacenamiento") || activePage.equals("Resumen"))) {
            lines = new java.util.ArrayList<>(lines);
            if (activePage.equals("Gráficos") || activePage.equals("Resumen")) {
                lines.add("━━ TELEMETRÍA GRÁFICA ADICIONAL ━━━━━━━━━━━━━━━━━");
                lines.addAll(hardwareReport.graphics());
                lines.add("");
            }
            if (activePage.equals("Almacenamiento") || activePage.equals("Resumen")) {
                lines.add("━━ INVENTARIO DE DISCOS ADICIONAL ━━━━━━━━━━━━━━━");
                lines.addAll(hardwareReport.storage());
                lines.add("");
            }
        }
        details.setText(String.join("\n", lines));
        details.setCaretPosition(0);
    }

    private List<String> combine(Object... sections) {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        for (int i=0;i<sections.length;i+=2) {
            result.add("━━ " + sections[i] + " ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            for (Object line : (List<?>) sections[i+1]) result.add(String.valueOf(line));
            result.add("");
        }
        return result;
    }

    /** Transición vertical direccional: la página entra desde arriba o abajo según la pestaña elegida. */
    private static final class AnimatedPanel extends JPanel {
        private float progress = 1f;
        private int direction = 1;
        private javax.swing.Timer transitionTimer;
        AnimatedPanel() { super(new BorderLayout(0, 18)); setOpaque(false); }
        void startTransition(int direction) {
            if (transitionTimer != null) transitionTimer.stop();
            this.direction = direction;
            progress = 0f;
            long started = System.nanoTime();
            transitionTimer = new javax.swing.Timer(16, event -> {
                double elapsed = (System.nanoTime() - started) / 1_000_000.0;
                float t = (float) Math.min(1.0, elapsed / 260.0);
                progress = 1f - (float) Math.pow(1f - t, 3);
                repaint();
                if (t >= 1f) transitionTimer.stop();
            });
            transitionTimer.start();
        }
        @Override protected void paintChildren(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            int offsetY = Math.round((1f - progress) * 34f * direction);
            g.translate(0, offsetY);
            g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.05f, progress)));
            super.paintChildren(g);
            g.dispose();
        }
    }

    /** Superficie glass oscura con gradiente, borde translúcido y sombra suave. */
    private static final class GlassPanel extends JPanel {
        GlassPanel(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(new Color(0, 0, 0, 48));
            g.fillRoundRect(2, 5, w - 4, h - 5, 22, 22);
            g.setPaint(new GradientPaint(0, 0, new Color(42, 49, 70, 242),
                    w, h, new Color(22, 28, 43, 246)));
            g.fillRoundRect(0, 0, w - 4, h - 7, 22, 22);
            g.setColor(new Color(174, 158, 255, 38));
            g.drawRoundRect(0, 0, w - 5, h - 8, 22, 22);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ChartPanel extends JPanel {
        private final String title;
        private final Deque<Double> values;
        ChartPanel(String title, Deque<Double> values) {
            this.title = title;
            this.values = values;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 15, 12, 15));
            setPreferredSize(new Dimension(300, 175));
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, 42));
            g.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 20, 20);
            g.setPaint(new GradientPaint(0, 0, new Color(35, 42, 60, 248), getWidth(), getHeight(), new Color(19, 25, 39, 248)));
            g.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 7, 20, 20);
            g.setColor(new Color(174, 158, 255, 34));
            g.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 8, 20, 20);
            g.setColor(TEXT);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(title + " · últimos 60 segundos", 15, 20);
            int left=18, top=37, width=getWidth()-36, height=getHeight()-58;
            g.setColor(new Color(48, 56, 73));
            for(int i=0;i<4;i++) {
                int y=top+i*height/3;
                g.drawLine(left,y,left+width,y);
            }
            java.util.List<Double> points = new java.util.ArrayList<>(values);
            if(points.size()>1) {
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(ACCENT);
                int prevX=left, prevY=top+height-(int)(points.get(0)/100.0*height);
                for(int i=1;i<points.size();i++) {
                    int x=left+(i*width/(points.size()-1));
                    int y=top+height-(int)(points.get(i)/100.0*height);
                    g.drawLine(prevX,prevY,x,y);
                    g.fillOval(x-3,y-3,6,6);
                    prevX=x; prevY=y;
                }
            } else {
                g.setColor(MUTED);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString("Recopilando muestras…", left, top+height/2);
            }
            g.setColor(MUTED);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString("100%", left, top+9);
            g.drawString("0%", left, top+height);
            g.dispose();
        }
    }

    private void styleButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
