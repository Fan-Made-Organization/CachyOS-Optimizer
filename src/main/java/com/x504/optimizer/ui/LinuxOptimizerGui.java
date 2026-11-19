package com.x504.optimizer.ui;

import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.system.AdvancedDiagnostics;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.PlatformHardwareInspector;
import com.x504.optimizer.update.GitHubUpdater;
import com.x504.optimizer.optimize.OptimizationCenter;
import com.x504.optimizer.install.CrossDistroGraphicsCatalog;
import com.x504.optimizer.system.LinuxPlatformSupport;
import com.x504.optimizer.system.KernelManager;
import com.x504.optimizer.cleaner.SystemCleaner;
import com.x504.optimizer.steam.SteamLaunchOptionsBuilder;
import java.util.concurrent.Callable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Interfaz gráfica Swing: panel de diagnóstico de solo lectura. */
public final class LinuxOptimizerGui {
    private static final Color BG = new Color(13, 16, 25);
    private static final Color PANEL = new Color(23, 28, 41);
    private static final Color PANEL_ALT = new Color(29, 36, 52);
    private static final Color ACCENT = new Color(132, 99, 255);
    private static final Color TEXT = new Color(235, 239, 249);
    private static final Color MUTED = new Color(151, 162, 184);
    private final JFrame frame = new JFrame("Linux Optimizer");
    private JPanel headerPanel;
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
        });
    }

    private void build() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(820, 620));
        frame.setSize(1180, 780);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout());
        frame.add(sidebar(), BorderLayout.WEST);
        JPanel shell = new JPanel(new BorderLayout(0, 14));
        shell.setBackground(BG);
        shell.setBorder(new EmptyBorder(22, 24, 18, 24));
        headerPanel = header();
        shell.add(headerPanel, BorderLayout.NORTH);
        content.setOpaque(false);
        shell.add(content, BorderLayout.CENTER);
        shell.add(footer(), BorderLayout.SOUTH);
        frame.add(shell, BorderLayout.CENTER);
        showPage("Información general");
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
        for (String page : List.of("Información general", "Resumen", "CPU y memoria", "Gráficos", "Almacenamiento", "Sistema", "Gaming")) {
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
        JLabel safe = new JLabel("<html><font color='#8D9AB2'>● CAMBIOS CON CONFIRMACIÓN<br>Optimización controlada</font></html>");
        safe.setFont(new Font("SansSerif", Font.PLAIN, 11));
        safe.setBorder(new EmptyBorder(12, 8, 0, 0));
        side.add(safe);
        return side;
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setAlignmentX(Component.CENTER_ALIGNMENT);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("OPTIMIZACIÓN DEL SISTEMA");
        eyebrow.setForeground(ACCENT);
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel title = new JLabel("Linux Optimizer");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        distro.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        distro.setForeground(MUTED);
        distro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titles.add(eyebrow); titles.add(Box.createVerticalStrut(5)); titles.add(title);
        titles.add(Box.createVerticalStrut(5)); titles.add(distro);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateButton = new JButton("⇧  Buscar actualizaciones");
        styleButton(updateButton);
        updateButton.setBackground(PANEL_ALT);
        updateButton.addActionListener(e -> checkUpdates(true));
        JButton refresh = new JButton("↻  Actualizar");
        styleButton(refresh);
        refresh.addActionListener(e -> refresh());
        actions.add(updateButton);
        actions.add(refresh);
        p.add(titles);
        p.add(Box.createVerticalStrut(12));
        p.add(actions);
        return p;
    }

    private JPanel footer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        status.setForeground(MUTED);
        status.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel version = new JLabel("LINUX OPTIMIZER");
        version.setForeground(new Color(103, 115, 139));
        version.setFont(new Font("SansSerif", Font.BOLD, 10));
        p.add(status, BorderLayout.WEST);
        p.add(version, BorderLayout.EAST);
        return p;
    }

    private void showPage(String page) {
        List<String> pages = List.of("Información general", "Resumen", "CPU y memoria", "Gráficos", "Almacenamiento", "Sistema", "Gaming");
        headerPanel.setVisible(page.equals("Información general"));
        headerPanel.getParent().revalidate();
        int oldIndex = pages.indexOf(activePage);
        int newIndex = pages.indexOf(page);
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
        JPanel body = new ResponsivePanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (page.equals("Resumen") || page.equals("Información general")) {
            JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
            cards.setOpaque(false);
            cards.add(metricCard("PROCESADOR", cpu));
            cards.add(metricCard("MEMORIA RAM", ram));
            cards.add(metricCard("GRÁFICOS", gpu));
            body.add(cards);
            body.add(Box.createVerticalStrut(16));
        }
        JPanel actionsPanel = actionPanel(page);
        if (actionsPanel != null) {
            body.add(actionsPanel);
            body.add(Box.createVerticalStrut(14));
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
        scroll.setPreferredSize(new Dimension(600, 230));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(Box.createVerticalStrut(14));
        body.add(scroll);
        body.setBorder(new EmptyBorder(4, 8, 12, 8));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane pageScroll = new JScrollPane(body);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(20);
        pageScroll.setWheelScrollingEnabled(true);
        content.add(pageScroll, BorderLayout.CENTER);
        content.revalidate();
        content.startTransition(travelDirection);
        renderDetails();
    }


    /** Acciones reales del centro de optimización; las operaciones modificadoras siempre piden confirmación. */
    private JPanel actionPanel(String page) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (!isLinuxHost()) {
            panel.add(actionCard("Acciones no disponibles en este sistema",
                    "La interfaz es multiplataforma, pero las operaciones de optimización Linux requieren sus herramientas y APIs nativas.",
                    button("Ver información", () -> showMessage("Este módulo de aplicación de cambios está disponible en Linux. En Windows/macOS solo se muestran funciones compatibles y no se ejecutan comandos Linux."))));
            return panel;
        }
        switch (page) {
            case "Información general" -> { return null; }
            case "Resumen" -> {
                panel.add(actionCard("Centro de optimización",
                        "Accede a las acciones disponibles para tu distribución. Cada cambio requiere confirmación.",
                        button("Abrir CPU y memoria", () -> showPage("CPU y memoria")),
                        button("Abrir gráficos", () -> showPage("Gráficos")),
                        button("Instalar herramientas", this::installOptimizationTools)));
            }
            case "CPU y memoria" -> {
                panel.add(actionCard("Perfiles de energía",
                        "Cambia entre equilibrado, rendimiento y ahorro. Se guarda el perfil anterior para poder restaurarlo.",
                        button("Equilibrado", () -> applyPowerProfile("balanced")),
                        button("Rendimiento", () -> applyPowerProfile("performance")),
                        button("Ahorro", () -> applyPowerProfile("power-saver")),
                        button("Restaurar perfil anterior", this::restorePowerProfile)));
                panel.add(Box.createVerticalStrut(10));
                panel.add(actionCard("Gobernador CPU",
                        "Aplica un gobernador disponible en el kernel y guarda el valor anterior.",
                        button("Elegir gobernador", this::chooseGovernor),
                        button("Restaurar gobernador", this::restoreGovernor)));
                panel.add(Box.createVerticalStrut(10));
                panel.add(actionCard("Memoria virtual ZRAM",
                        "Activa o desactiva únicamente la configuración administrada por Linux Optimizer.",
                        button("Configurar / revertir ZRAM", this::toggleZram)));
            }
            case "Gráficos" -> panel.add(actionCard("Controladores y pila gráfica",
                    "Prepara perfiles de paquetes compatibles con tu distribución y GPU. No elimina ni reemplaza controladores automáticamente.",
                    button("Instalar perfil gráfico", this::installGraphicsProfile),
                    button("Instalar herramientas gráficas", this::installOptimizationTools)));
            case "Almacenamiento" -> panel.add(actionCard("TRIM programado",
                    "Habilita fstrim.timer solo si se detecta almacenamiento no rotacional. La acción es reversible.",
                    button("Activar / desactivar TRIM", this::toggleTrim)));
            case "Gaming" -> panel.add(actionCard("Herramientas y compatibilidad gaming",
                    "Instala herramientas disponibles en los repositorios de tu distribución y genera parámetros de Steam.",
                    button("Instalar herramientas gaming", this::installGamingTools),
                    button("Crear parámetros de Steam", this::createSteamOptions)));
            case "Sistema" -> panel.add(actionCard("Mantenimiento seguro",
                    "Revisa cachés de miniaturas y fuentes con más de 30 días. No toca shaders, juegos ni archivos personales.",
                    button("Revisar limpieza", this::previewCleaner),
                    button("Instalar herramientas de optimización", this::installOptimizationTools),
                    button("Opciones de kernel", this::chooseKernel)));
            default -> { return null; }
        }
        return panel;
    }

    private JPanel actionCard(String title, String description, JButton... buttons) {
        JPanel card = new GlassPanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(17, 18, 17, 18));
        JLabel heading = new JLabel(title);
        heading.setForeground(TEXT);
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel subtitle = new JLabel("<html><div style='width:520px'>" + description + "</div></html>");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel buttonRow = new JPanel(new GridLayout(0, 2, 10, 9));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (JButton button : buttons) {
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setPreferredSize(new Dimension(210, 42));
            button.setMinimumSize(new Dimension(0, 42));
            buttonRow.add(button);
        }
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(heading);
        text.add(Box.createVerticalStrut(6));
        text.add(subtitle);
        card.add(text, BorderLayout.NORTH);
        card.add(buttonRow, BorderLayout.SOUTH);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        card.setPreferredSize(new Dimension(600, Math.max(150, 112 + ((buttons.length + 1) / 2) * 51)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        return card;
    }

    private JButton button(String label, Runnable action) {
        JButton b = new JButton(label);
        styleButton(b);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Linux Optimizer", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmAction(String title, String message, Callable<String> task) {
        if (JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        status.setText("Ejecutando: " + title + "…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                try {
                    String result = get();
                    status.setText(title + " • finalizado");
                    JOptionPane.showMessageDialog(frame, result, title, JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    status.setText(title + " • error");
                    JOptionPane.showMessageDialog(frame, cause.getMessage(), title, JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void applyPowerProfile(String profile) {
        confirmAction("Aplicar perfil de energía",
                "Se aplicará '" + profile + "'. Se guardará el perfil anterior para poder revertirlo. ¿Continuar?",
                () -> { new OptimizationCenter().applyPowerProfile(profile); return "Perfil aplicado: " + profile; });
    }

    private void restorePowerProfile() {
        confirmAction("Restaurar perfil", "¿Restaurar el perfil de energía anterior guardado por Linux Optimizer?",
                () -> { new OptimizationCenter().restorePowerProfile(); return "Perfil anterior restaurado."; });
    }

    private void chooseGovernor() {
        try {
            OptimizationCenter center = new OptimizationCenter();
            List<String> governors = center.availableGovernors();
            String selected = (String) JOptionPane.showInputDialog(frame, "Selecciona un gobernador disponible:",
                    "Gobernador CPU", JOptionPane.QUESTION_MESSAGE, null, governors.toArray(), center.currentGovernor());
            if (selected == null || selected.isBlank()) return;
            confirmAction("Cambiar gobernador CPU", "Se aplicará '" + selected + "' mediante sudo y se guardará el valor anterior.",
                    () -> { center.applyGovernor(selected); return "Gobernador aplicado: " + selected; });
        } catch (Exception ex) { showMessage("No se pudo consultar los gobernadores: " + ex.getMessage()); }
    }

    private void restoreGovernor() {
        confirmAction("Restaurar gobernador", "¿Restaurar el gobernador CPU anterior guardado?",
                () -> { new OptimizationCenter().restoreGovernor(); return "Gobernador anterior restaurado."; });
    }

    private void toggleZram() {
        try {
            OptimizationCenter center = new OptimizationCenter();
            boolean active = center.isZramActive();
            if (active && !center.isManagedZram()) {
                showMessage("ZRAM ya está activa, pero no fue configurada por Linux Optimizer. No se modificará.");
                return;
            }
            boolean enable = !active;
            confirmAction(enable ? "Activar ZRAM" : "Desactivar ZRAM",
                    enable ? "Se creará una configuración ZRAM administrada y reversible. Requiere zram-generator y sudo. ¿Continuar?"
                            : "Se retirará únicamente la configuración ZRAM administrada por Linux Optimizer. ¿Continuar?",
                    () -> { int code = center.configureManagedZram(enable); if (code != 0) throw new IllegalStateException("El script terminó con código " + code); return enable ? "ZRAM configurada." : "Configuración ZRAM retirada."; });
        } catch (Exception ex) { showMessage("No se pudo consultar ZRAM: " + ex.getMessage()); }
    }

    private void toggleTrim() {
        confirmAction("Administrar TRIM", "Se comprobará el estado y el tipo de almacenamiento. Si hay SSD/NVMe se habilitará o deshabilitará fstrim.timer. ¿Continuar?",
                () -> {
                    OptimizationCenter center = new OptimizationCenter();
                    boolean enabled = center.isTrimTimerEnabled();
                    if (enabled) {
                        int code = center.disableTrimTimer();
                        if (code != 0) throw new IllegalStateException("systemctl terminó con código " + code);
                        return "fstrim.timer deshabilitado.";
                    }
                    if (!center.hasSolidStateDrive()) return "No se detectó almacenamiento no rotacional. No se aplicó ningún cambio.";
                    int code = center.enableTrimTimer();
                    if (code != 0) throw new IllegalStateException("systemctl terminó con código " + code);
                    return "fstrim.timer habilitado.";
                });
    }

    private void installOptimizationTools() { installToolProfile(false); }
    private void installGamingTools() { installToolProfile(true); }

    private void installToolProfile(boolean gamingOnly) {
        try {
            LinuxPlatformSupport support = new LinuxPlatformSupport();
            LinuxPlatformSupport.Platform platform = support.detect();
            if (support.isImmutableOrImageBased(platform)) {
                showMessage("Esta distribución es inmutable o basada en imágenes. La instalación genérica está bloqueada; utiliza el mecanismo oficial de tu distribución.");
                return;
            }
            Map<String, List<String>> categories = support.toolCategories(platform);
            if (gamingOnly) {
                categories = categories.entrySet().stream()
                        .filter(entry -> entry.getKey().equals("Gaming"))
                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (left, right) -> left, LinkedHashMap::new));
            }
            if (categories.isEmpty()) {
                showMessage("No hay perfiles de paquetes validados para " + platform.name() + ". No se ejecutó ningún comando.");
                return;
            }
            List<String> names = new ArrayList<>(categories.keySet());
            String selected = (String) JOptionPane.showInputDialog(frame,
                    "Distribución: " + platform.name() + "\nSelecciona un perfil de paquetes:",
                    "Instalador de optimización", JOptionPane.QUESTION_MESSAGE, null, names.toArray(), names.get(0));
            if (selected == null) return;
            List<String> packages = categories.get(selected);
            String preview = "Perfil: " + selected + "\nPaquetes: " + String.join(", ", packages)
                    + "\n\nEl gestor mostrará su transacción y pedirá confirmación. No se eliminarán paquetes.";
            confirmAction("Instalar " + selected, preview, () -> {
                int exit = support.install(platform, packages);
                if (exit != 0) throw new IllegalStateException("El gestor de paquetes terminó con código " + exit);
                return "Instalación finalizada: " + selected;
            });
        } catch (Exception ex) { showMessage("No se pudo preparar el instalador: " + ex.getMessage()); }
    }

    private void installGraphicsProfile() {
        try {
            LinuxPlatformSupport support = new LinuxPlatformSupport();
            LinuxPlatformSupport.Platform platform = support.detect();
            if (support.isImmutableOrImageBased(platform)) {
                showMessage("Distribución inmutable: utiliza el flujo oficial de drivers de tu sistema.");
                return;
            }
            CrossDistroGraphicsCatalog catalog = new CrossDistroGraphicsCatalog();
            String[] profiles = {"Intel", "AMD", "NVIDIA", "Compatibilidad Wine/Proton", "Componentes comunes"};
            String selected = (String) JOptionPane.showInputDialog(frame,
                    "Sistema: " + platform.name() + "\nElige el perfil; verifica fabricante y compatibilidad antes de continuar:",
                    "Perfiles gráficos", JOptionPane.WARNING_MESSAGE, null, profiles, profiles[0]);
            if (selected == null) return;
            CrossDistroGraphicsCatalog.Vendor vendor = switch (selected) {
                case "Intel" -> CrossDistroGraphicsCatalog.Vendor.INTEL;
                case "AMD" -> CrossDistroGraphicsCatalog.Vendor.AMD;
                case "NVIDIA" -> CrossDistroGraphicsCatalog.Vendor.NVIDIA;
                case "Componentes comunes" -> CrossDistroGraphicsCatalog.Vendor.COMMON;
                default -> null;
            };
            List<String> packages = selected.equals("Compatibilidad Wine/Proton")
                    ? catalog.compatibilityPackages(platform.family()) : catalog.packages(vendor, platform.family());
            if (packages.isEmpty()) {
                showMessage("No existe un perfil validado para esta combinación de distribución y perfil. No se ejecutó ningún comando.");
                return;
            }
            String command = String.join(" ", support.installCommand(platform, packages));
            confirmAction("Instalar perfil gráfico", "Distribución: " + platform.name() + "\nPaquetes: " + String.join(", ", packages)
                            + "\nComando revisable: " + command
                            + "\n\nNVIDIA requiere verificar GPU, kernel, Secure Boot y repositorios. No se eliminarán drivers.",
                    () -> { int exit = new com.x504.optimizer.install.GraphicsDriverInstaller().install(support, platform, packages);
                        if (exit != 0) throw new IllegalStateException("El gestor terminó con código " + exit);
                        return "Perfil gráfico instalado. Revisa los mensajes del gestor y reinicia solo si la distribución lo requiere."; });
        } catch (Exception ex) { showMessage("No se pudo preparar el perfil gráfico: " + ex.getMessage()); }
    }


    private void chooseKernel() {
        try {
            LinuxPlatformSupport support = new LinuxPlatformSupport();
            LinuxPlatformSupport.Platform platform = support.detect();
            if (support.isImmutableOrImageBased(platform)) {
                showMessage("Sistema inmutable o basado en imágenes: administra el kernel mediante el mecanismo oficial de la distribución.");
                return;
            }
            KernelManager manager = new KernelManager();
            List<KernelManager.KernelOption> options = manager.options(platform);
            if (options.isEmpty()) {
                showMessage("No existe un perfil de instalación de kernel validado para " + platform.name() + ". No se ejecutó ningún comando.");
                return;
            }
            KernelManager.BootInventory inventory = manager.inspectBootInventory();
            String[] labels = options.stream().map(KernelManager.KernelOption::label).toArray(String[]::new);
            String selectedLabel = (String) JOptionPane.showInputDialog(frame,
                    "Kernel activo: " + inventory.activeKernel()
                            + "\\nKernels detectados: " + String.join(", ", inventory.installedModuleTrees())
                            + "\\nPosible respaldo detectado: " + (inventory.hasPossibleFallback() ? "sí (verificación manual necesaria)" : "no")
                            + "\\n\\nSelecciona un paquete de kernel:",
                    "Opciones de kernel", JOptionPane.WARNING_MESSAGE, null, labels, labels[0]);
            if (selectedLabel == null) return;
            KernelManager.KernelOption selected = options.stream().filter(o -> o.label().equals(selectedLabel)).findFirst().orElseThrow();
            List<String> command = manager.installCommand(support, platform, selected);
            if (!inventory.hasPossibleFallback()) {
                showMessage("BLOQUEADO: no se detectó un posible kernel/entrada de respaldo. Conserva y verifica un kernel arrancable antes de instalar otro.");
                return;
            }
            confirmAction("Instalar kernel",
                    "Distribución: " + platform.name() + "\\nPaquetes: " + String.join(", ", selected.packages())
                            + "\\nComando: " + String.join(" ", command)
                            + "\\n\\nLa detección de respaldo es heurística. Verifica manualmente que el kernel actual arranca. Linux Optimizer nunca elimina kernels ni modifica el bootloader.",
                    () -> {
                        int exit = support.install(platform, selected.packages());
                        if (exit != 0) throw new IllegalStateException("El gestor terminó con código " + exit + ". Revisa sus mensajes antes de reiniciar.");
                        return "Instalación de paquetes finalizada. Verifica initramfs y entradas de arranque antes de reiniciar.";
                    });
        } catch (Exception ex) { showMessage("No se pudo preparar la opción de kernel: " + ex.getMessage()); }
    }

    private void createSteamOptions() {
        JCheckBox gameMode = new JCheckBox("GameMode");
        JCheckBox mangoHud = new JCheckBox("MangoHud");
        JCheckBox gamescope = new JCheckBox("Gamescope");
        JCheckBox protonLog = new JCheckBox("Registro Proton");
        JCheckBox fsr = new JCheckBox("FSR de Proton");
        JCheckBox wineD3d = new JCheckBox("WineD3D (alternativa de compatibilidad; puede reducir rendimiento)");
        for (JCheckBox box : List.of(gameMode, mangoHud, gamescope, protonLog, fsr, wineD3d)) {
            box.setOpaque(false); box.setForeground(TEXT);
        }
        JPanel choices = new JPanel();
        choices.setOpaque(false); choices.setLayout(new BoxLayout(choices, BoxLayout.Y_AXIS));
        for (JCheckBox box : List.of(gameMode, mangoHud, gamescope, protonLog, fsr, wineD3d)) choices.add(box);
        if (JOptionPane.showConfirmDialog(frame, choices, "Generador de parámetros Steam", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            var options = new SteamLaunchOptionsBuilder.Options(gameMode.isSelected(), mangoHud.isSelected(),
                    gamescope.isSelected(), protonLog.isSelected(), fsr.isSelected(), wineD3d.isSelected());
            String result = new SteamLaunchOptionsBuilder().build(options);
            JTextArea output = new JTextArea(result, 3, 52);
            output.setLineWrap(true); output.setWrapStyleWord(true); output.setEditable(false);
            if (JOptionPane.showConfirmDialog(frame, new JScrollPane(output), "Copia en Steam → Propiedades → General → Parámetros de lanzamiento",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(result), null);
                status.setText("Parámetros de Steam copiados al portapapeles.");
            }
        } catch (Exception ex) { showMessage("No se pudieron generar los parámetros: " + ex.getMessage()); }
    }

    private void previewCleaner() {
        try {
            SystemCleaner cleaner = new SystemCleaner();
            List<SystemCleaner.Entry> entries = cleaner.preview();
            if (entries.isEmpty()) { showMessage("No hay cachés permitidas con más de 30 días para limpiar."); return; }
            String[] categories = entries.stream().map(SystemCleaner.Entry::category).toArray(String[]::new);
            String selected = (String) JOptionPane.showInputDialog(frame,
                    String.join("\n", entries.stream().map(e -> e.category() + ": " + e.files() + " archivos, " + String.format("%.2f MiB", e.bytes()/1048576.0)).toList())
                            + "\n\nSelecciona una categoría para limpiar:",
                    "Vista previa de limpieza", JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);
            if (selected == null) return;
            confirmAction("Limpiar caché " + selected,
                    "Se eliminarán únicamente archivos antiguos de la categoría seleccionada. No se tocarán shaders, navegadores, juegos ni archivos personales.",
                    () -> "Archivos eliminados: " + cleaner.clean(selected));
        } catch (Exception ex) { showMessage("No se pudo preparar la limpieza: " + ex.getMessage()); }
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
                + "\n\nLa interfaz y el actualizador funcionan en este sistema."
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
            case "Gaming" -> combine("GAMING", lastReport.gaming());
            case "Sistema" -> combine("SERVICIOS", lastReport.services(), "KERNEL", lastReport.kernel());
            default -> combine("CPU", lastReport.cpu(), "MEMORIA", lastReport.memory(),
                    "ALMACENAMIENTO", lastReport.storage(), "GRÁFICOS", lastReport.graphics(),
                    "RED", lastReport.network(), "SERVICIOS", lastReport.services(),
                    "KERNEL", lastReport.kernel(), "GAMING", lastReport.gaming());
        };
        if (hardwareReport != null && (activePage.equals("Gráficos") || activePage.equals("Almacenamiento") || activePage.equals("Resumen") || activePage.equals("Información general") || activePage.equals("Información general"))) {
            lines = new java.util.ArrayList<>(lines);
            if (activePage.equals("Gráficos") || activePage.equals("Resumen") || activePage.equals("Información general")) {
                lines.add("━━ INFORMACIÓN DEL ADAPTADOR GRÁFICO ━━━━━━━━━━━");
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

    /** Panel desplazable que siempre se adapta al ancho visible, sin scroll horizontal. */
    private static final class ResponsivePanel extends JPanel implements Scrollable {
        ResponsivePanel() { super(); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return Math.max(48, (orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width) - 48); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
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

    private void styleButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
