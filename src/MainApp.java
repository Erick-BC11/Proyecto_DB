import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainApp() {
        setTitle("Sistema de Seguros - Developer Mode");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR (Izquierda) ---
        JPanel sidebar = new JPanel();
        // AQUÍ: Referencia a tu nueva clase
        sidebar.setBackground(estilos.SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Título en Sidebar
        JLabel lblBrand = new JLabel("INSURE APP");
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblBrand);

        // Botones del Menú
        addMenuButton(sidebar, "Clientes", "CUSTOMERS");
        addMenuButton(sidebar, "Solicitudes", "APPLICATIONS");
        addMenuButton(sidebar, "Configuración", "SETTINGS");

        add(sidebar, BorderLayout.WEST);

        // --- CONTENIDO PRINCIPAL (Centro) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(estilos.BG_COLOR); // Referencia actualizada

        // Agregamos los paneles
        contentPanel.add(new PanelCliente(), "CUSTOMERS");

        JPanel pnlApps = new JPanel();
        pnlApps.setBackground(estilos.BG_COLOR);
        pnlApps.add(new JLabel("Módulo de Solicitudes (En construcción)"));
        contentPanel.add(pnlApps, "APPLICATIONS");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addMenuButton(JPanel sidebar, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setBackground(estilos.SIDEBAR_COLOR); // Referencia actualizada
        btn.setForeground(Color.GRAY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.GRAY);
            }
        });

        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        sidebar.add(btn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}
