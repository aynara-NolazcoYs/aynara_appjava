package vallegrande.edu.pe.view;

import vallegrande.edu.pe.controller.ContactController;
import vallegrande.edu.pe.model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ContactView extends JFrame {

    private final ContactController controller;
    private DefaultTableModel tableModel;
    private JTable table;
    private boolean darkMode = false; // tema claro por defecto
    private JPanel contentPanel;
    private JPanel buttonsPanel;
    private JLabel title;

    public ContactView(ContactController controller) {
        super("Agenda MVC Swing - Vallegrande");

        // 🔹 Forzar Look & Feel neutral para que se vean los estilos personalizados
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.controller = controller;
        initUI();       // aquí va el método que pegaste en tu mensaje
        loadContacts();
        showToast("Bienvenido a la Agenda de Contactos", ToastType.INFO);
    }


    /* ========================== UI & THEME ========================== */

    private enum ToastType { SUCCESS, ERROR, INFO }

    private static final class Theme {
        // Paleta clara (azul/blanco)
        final Color bg = new Color(246, 248, 251);
        final Color card = Color.WHITE;
        final Color text = new Color(33, 37, 41);
        final Color subtext = new Color(90, 98, 104);
        final Color border = new Color(225, 230, 235);
        final Color stripeEven = new Color(247, 249, 252);
        final Color selection = new Color(209, 229, 255);
        final Color headerBg = new Color(27, 111, 233);
        final Color headerFg = Color.WHITE;

        final Color primary = new Color(0, 123, 255);
        final Color danger = new Color(220, 53, 69);
        final Color neutral = new Color(108, 117, 125);

        // Toasts
        final Color toastSuccess = new Color(40, 167, 69);
        final Color toastError = new Color(220, 53, 69);
        final Color toastInfo = new Color(33, 150, 243);
    }

    private static final class ThemeDark extends Theme {
        // Paleta oscura
        final Color bg = new Color(24, 26, 27);
        final Color card = new Color(34, 36, 38);
        final Color text = new Color(238, 238, 238);
        final Color subtext = new Color(180, 180, 180);
        final Color border = new Color(60, 62, 64);
        final Color stripeEven = new Color(40, 42, 44);
        final Color selection = new Color(52, 91, 161);
        final Color headerBg = new Color(33, 88, 196);
        final Color headerFg = Color.WHITE;

        final Color primary = new Color(0, 123, 255);
        final Color danger = new Color(232, 74, 95);
        final Color neutral = new Color(134, 142, 150);

        final Color toastSuccess = new Color(33, 150, 83);
        final Color toastError = new Color(214, 64, 84);
        final Color toastInfo = new Color(33, 150, 243);
    }

    private Theme theme() { return darkMode ? new ThemeDark() : new Theme(); }

    private Font baseFont() {
        // Usa Segoe UI si existe, sino SansSerif
        String[] candidates = {"Segoe UI", "Inter", "SF Pro Display", "Roboto", "SansSerif"};
        for (String f : candidates) {
            if (isFontAvailable(f)) return new Font(f, Font.PLAIN, 16);
        }
        return new Font("SansSerif", Font.PLAIN, 16);
    }

    private boolean isFontAvailable(String name) {
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (f.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /* ========================== COMPONENTES CUSTOM ========================== */

    /** Botón moderno redondeado con hover y sombra suave */
    private static class ModernButton extends JButton {
        private final Color baseColor;
        private boolean hover = false;

        ModernButton(String text, Color baseColor, Font font) {
            super(text);
            this.baseColor = baseColor;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFont(font.deriveFont(Font.BOLD, 16f));
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 22, 10, 22));
            setPreferredSize(new Dimension(140, 44));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 16;
            // Sombra suave
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, arc, arc);

            // Fondo
            Color fill = hover ? baseColor.darker() : baseColor;
            g2.setPaint(fill);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);

            // Texto
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2 - 4;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 4;
            g2.setColor(getForeground());
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    /** Panel tipo “card” con esquinas redondeadas, borde y sombra suave */
    private static class CardPanel extends JPanel {
        private final java.util.function.Supplier<Theme> themeSupplier;

        CardPanel(LayoutManager layout, java.util.function.Supplier<Theme> themeSupplier) {
            super(layout);
            this.themeSupplier = themeSupplier;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 16, 16, 16));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Theme t = themeSupplier.get();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;
            // Sombra
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, arc, arc);
            // Fondo
            g2.setColor(t.card);
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);
            // Borde
            g2.setColor(t.border);
            g2.drawRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

            g2.dispose();
        }

        @Override public boolean isOptimizedDrawingEnabled() { return false; }
    }

    /** Renderer con zebra + selección y hover opcional */
    private static class ModernRowRenderer extends DefaultTableCellRenderer {
        private final java.util.function.Supplier<Theme> themeSupplier;

        ModernRowRenderer(java.util.function.Supplier<Theme> themeSupplier) {
            this.themeSupplier = themeSupplier;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Theme t = themeSupplier.get();
            if (isSelected) {
                setBackground(t.selection);
                setForeground(Color.BLACK);
            } else {
                setBackground(row % 2 == 0 ? t.stripeEven : t.card);
                setForeground(t.text);
            }
            setBorder(new EmptyBorder(6, 8, 6, 8));
            return this;
        }
    }

    /* ========================== INIT UI ========================== */

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Font baseFont = baseFont();

        // Panel principal
        contentPanel = new JPanel(new BorderLayout(18, 18));
        contentPanel.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(contentPanel);

        // Título
        title = new JLabel("Agenda de Contactos", SwingConstants.LEFT);
        title.setFont(baseFont.deriveFont(Font.BOLD, 28f));
        title.setBorder(new EmptyBorder(0, 4, 6, 4));

        // Barra superior con título + botón de tema
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);

        JButton themeBtn = new ModernButton("Tema", theme().neutral, baseFont);
        themeBtn.addActionListener(e -> toggleTheme());
        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRight.setOpaque(false);
        actionsRight.add(themeBtn);
        topBar.add(actionsRight, BorderLayout.EAST);
        contentPanel.add(topBar, BorderLayout.NORTH);

        // Tabla dentro de CardPanel
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Email", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(baseFont.deriveFont(15f));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new ModernRowRenderer(this::theme));

        JTableHeader header = table.getTableHeader();
        header.setFont(baseFont.deriveFont(Font.BOLD, 16f));
        header.setReorderingAllowed(false);
        header.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        JPanel tableCard = new CardPanel(new BorderLayout(), this::theme);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Separador sutil superior del card
        JSeparator sep = new JSeparator();
        sep.setBorder(new EmptyBorder(0,0,8,0));
        sep.setOpaque(false);
        tableCard.add(sep, BorderLayout.NORTH);

        contentPanel.add(tableCard, BorderLayout.CENTER);

        // Panel de botones (card estilizado)
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        buttonsPanel.setOpaque(false);

        JButton addBtn = new ModernButton("Agregar", theme().primary, baseFont);
        JButton deleteBtn = new ModernButton("Eliminar", theme().danger, baseFont);

        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);

        JPanel bottomCard = new CardPanel(new BorderLayout(), this::theme);
        bottomCard.add(buttonsPanel, BorderLayout.EAST);
        contentPanel.add(bottomCard, BorderLayout.SOUTH);

        // Eventos (lógica intacta)
        addBtn.addActionListener(e -> {
            showAddContactDialog();
            showToast("Contacto agregado correctamente", ToastType.SUCCESS);
        });

        deleteBtn.addActionListener(e -> {
            if (deleteSelectedContact()) {
                showToast("Contacto eliminado", ToastType.ERROR);
            }
        });

        applyTheme();
    }

    /* ========================== DATA ========================== */

    private void loadContacts() {
        tableModel.setRowCount(0);
        List<Contact> contacts = controller.list();
        for (Contact c : contacts) {
            tableModel.addRow(new Object[]{c.id(), c.name(), c.email(), c.phone()});
        }
    }

    private void showAddContactDialog() {
        AddContactDialog dialog = new AddContactDialog(this, controller);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) loadContacts();
    }

    private boolean deleteSelectedContact() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contacto para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String id = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar este contacto?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.delete(id);
            loadContacts();
            return true;
        }
        return false;
    }

    /* ========================== TOASTS ========================== */

    private void showToast(String message, ToastType type) {
        Theme t = theme();
        Color bg;
        switch (type) {
            case SUCCESS -> bg = t.toastSuccess;
            case ERROR -> bg = t.toastError;
            default -> bg = t.toastInfo;
        }

        JWindow toast = new JWindow();
        toast.setBackground(new Color(0, 0, 0, 0)); // transparente
        JPanel pill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 20;
                // sombra
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, arc, arc);
                // fondo
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, arc, arc);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setLayout(new BorderLayout());
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(12, 20, 12, 20));
        label.setFont(baseFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(Color.WHITE);
        pill.add(label, BorderLayout.CENTER);
        toast.add(pill);
        toast.pack();

        // Posicionar esquina inferior derecha
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width - toast.getWidth() - 30;
        int y = screen.height - toast.getHeight() - 60;
        toast.setLocation(x, y);

        // Animación simple de “slide up”
        Timer slideTimer = new Timer(8, null);
        final int[] steps = {14};
        final int[] current = {y + 20};
        toast.setLocation(x, current[0]);
        toast.setVisible(true);
        slideTimer.addActionListener(e -> {
            if (steps[0]-- <= 0) ((Timer) e.getSource()).stop();
            current[0] -= 2;
            toast.setLocation(x, current[0]);
        });
        slideTimer.start();

        // Autocierre
        new Timer(2200, e -> {
            toast.setVisible(false);
            toast.dispose();
        }).start();
    }

    /* ========================== THEME SWITCH ========================== */

    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    private void applyTheme() {
        Theme t = theme();

        // Fondo ventana
        contentPanel.setBackground(t.bg);

        // Título
        title.setForeground(t.text);

        // Tabla
        table.setForeground(t.text);
        table.setBackground(t.card);
        table.setSelectionBackground(t.selection);
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setBackground(t.headerBg);
        header.setForeground(t.headerFg);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, t.border));

        // Botonera y cards
        buttonsPanel.setBackground(new Color(0,0,0,0)); // transparente en card

        // Repintar todo
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }

    /* ========================== MAIN (opcional de prueba) ========================== */
    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> new ContactView(new ContactController()).setVisible(true));
    // }
}
