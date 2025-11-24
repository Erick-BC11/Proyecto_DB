import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelCliente extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public PanelCliente() {
        setLayout(new BorderLayout(20, 20));
        setBackground(estilos.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitle = new JLabel("Gestión de Clientes");
        lblTitle.setFont(estilos.TITLE_FONT);
        lblTitle.setForeground(estilos.SIDEBAR_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // Tabla
        String[] columnNames = {"ID", "Nombre", "Email", "Teléfono"};
        // Hacemos la tabla no editable directamente para forzar uso de botones
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        estilos.styleTable(table);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Botones CRUD
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(estilos.BG_COLOR);

        JButton btnAdd = estilos.createStyledButton("Nuevo (+)");
        JButton btnEdit = estilos.createStyledButton("Editar");
        JButton btnDelete = estilos.createStyledButton("Eliminar");
        JButton btnRefresh = estilos.createStyledButton("Actualizar Tabla");

        // Eventos
        btnAdd.addActionListener(e -> showCustomerDialog(null)); // null = Crear nuevo
        btnEdit.addActionListener(e -> editarClienteSeleccionado());
        btnDelete.addActionListener(e -> eliminarClienteSeleccionado());
        btnRefresh.addActionListener(e -> loadCustomers());

        actionPanel.add(btnRefresh);
        actionPanel.add(btnDelete);
        actionPanel.add(btnEdit);
        actionPanel.add(btnAdd);
        add(actionPanel, BorderLayout.SOUTH);

        loadCustomers();
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customers ORDER BY Customer_ID ASC")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Customer_ID"), rs.getString("Customer_Name"),
                        rs.getString("Contact_Email_Address"), rs.getString("Contact_Phone")
                });
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    // UPDATE LOGIC
    private void editarClienteSeleccionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para editar.");
            return;
        }
        // Obtenemos datos actuales de la tabla
        int id = (int) table.getValueAt(row, 0);
        String nombre = (String) table.getValueAt(row, 1);
        String email = (String) table.getValueAt(row, 2);
        String tel = (String) table.getValueAt(row, 3);

        // Reutilizamos el dialogo pero pasando datos
        CustomerData data = new CustomerData(id, nombre, email, tel);
        showCustomerDialog(data);
    }

    // DELETE LOGIC
    private void eliminarClienteSeleccionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar al cliente ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Customers WHERE Customer_ID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadCustomers();
                JOptionPane.showMessageDialog(this, "Cliente eliminado.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage()); }
        }
    }

    // CREATE & UPDATE DIALOG (Reutilizable)
    private void showCustomerDialog(CustomerData dataToEdit) {
        boolean isEdit = (dataToEdit != null);
        String title = isEdit ? "Editar Cliente" : "Nuevo Cliente";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JTextField txtName = new JTextField(isEdit ? dataToEdit.name : "");
        JTextField txtEmail = new JTextField(isEdit ? dataToEdit.email : "");
        JTextField txtPhone = new JTextField(isEdit ? dataToEdit.phone : "");

        dialog.add(new JLabel(" Nombre:")); dialog.add(txtName);
        dialog.add(new JLabel(" Email:")); dialog.add(txtEmail);
        dialog.add(new JLabel(" Teléfono:")); dialog.add(txtPhone);

        JButton btnSave = estilos.createStyledButton("Guardar");
        btnSave.addActionListener(e -> {
            if (isEdit) updateCustomerDB(dataToEdit.id, txtName.getText(), txtEmail.getText(), txtPhone.getText());
            else createCustomerDB(txtName.getText(), txtEmail.getText(), txtPhone.getText());
            dialog.dispose();
            loadCustomers();
        });
        dialog.add(new JLabel("")); dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private void createCustomerDB(String n, String e, String p) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Customers (Customer_Name, Contact_Email_Address, Contact_Phone) VALUES (?,?,?)")) {
            ps.setString(1, n); ps.setString(2, e); ps.setString(3, p);
            ps.executeUpdate();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void updateCustomerDB(int id, String n, String e, String p) {
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Customers SET Customer_Name=?, Contact_Email_Address=?, Contact_Phone=? WHERE Customer_ID=?")) {
            ps.setString(1, n); ps.setString(2, e); ps.setString(3, p); ps.setInt(4, id);
            ps.executeUpdate();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    // Clase auxiliar simple para pasar datos
    class CustomerData {
        int id; String name, email, phone;
        public CustomerData(int id, String n, String e, String p) { this.id=id; name=n; email=e; phone=p; }
    }
}