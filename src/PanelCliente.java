import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelCliente extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public PanelCliente() {
        setLayout(new BorderLayout(20, 20));
        // Usamos tu clase 'estilos'
        setBackground(estilos.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título
        JLabel lblTitle = new JLabel("Gestión de Clientes");
        lblTitle.setFont(estilos.TITLE_FONT);
        lblTitle.setForeground(estilos.SIDEBAR_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // 2. Tabla de Datos
        String[] columnNames = {"ID", "Nombre", "Email", "Teléfono"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        estilos.styleTable(table); // Aplicamos tus estilos

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // 3. Botones de Acción
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(estilos.BG_COLOR);

        // Botones estilizados
        JButton btnAdd = estilos.createStyledButton("Nuevo Cliente (+)");
        JButton btnRefresh = estilos.createStyledButton("Actualizar Tabla");

        btnAdd.addActionListener(e -> showAddCustomerDialog());
        btnRefresh.addActionListener(e -> loadCustomers());

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);
        add(actionPanel, BorderLayout.SOUTH);

        // Cargar datos al iniciar
        loadCustomers();
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);
        // Nota: Las tablas de la BD siguen en inglés (Customers) según tu SQL original
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Customer_ID, Customer_Name, Contact_Email_Address, Contact_Phone FROM Customers ORDER BY Customer_ID ASC")) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("Customer_ID"),
                        rs.getString("Customer_Name"),
                        rs.getString("Contact_Email_Address"),
                        rs.getString("Contact_Phone")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage());
        }
    }

    private void showAddCustomerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuevo Cliente", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();

        dialog.add(new JLabel(" Nombre Completo:")); dialog.add(txtName);
        dialog.add(new JLabel(" Email:")); dialog.add(txtEmail);
        dialog.add(new JLabel(" Teléfono:")); dialog.add(txtPhone);
        dialog.add(new JLabel(""));

        JButton btnSave = estilos.createStyledButton("Guardar");
        btnSave.addActionListener(e -> {
            saveCustomer(txtName.getText(), txtEmail.getText(), txtPhone.getText());
            dialog.dispose();
            loadCustomers();
        });

        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private void saveCustomer(String name, String email, String phone) {
        String sql = "INSERT INTO Customers (Customer_Name, Contact_Email_Address, Contact_Phone) VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }
}
