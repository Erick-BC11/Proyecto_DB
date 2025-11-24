import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelSolicitudes extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public PanelSolicitudes() {
        setLayout(new BorderLayout(20, 20));
        setBackground(estilos.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Gestión de Solicitudes");
        lblTitle.setFont(estilos.TITLE_FONT);
        lblTitle.setForeground(estilos.SIDEBAR_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        String[] colNames = {"ID", "Fecha", "Solicitante", "Tipo Póliza", "Descripción"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        estilos.styleTable(table); // Ahora usa el estilo nativo
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(estilos.BG_COLOR);

        JButton btnNew = estilos.createStyledButton("Nueva Solicitud");
        JButton btnDelete = estilos.createStyledButton("Cancelar/Eliminar"); // DELETE
        JButton btnAnswers = estilos.createStyledButton("Cuestionario");
        JButton btnRefresh = estilos.createStyledButton("Refrescar");

        btnRefresh.addActionListener(e -> loadApplications());
        btnNew.addActionListener(e -> showNewAppDialog());
        btnDelete.addActionListener(e -> deleteApplication());

        btnAnswers.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int appId = (int) table.getValueAt(row, 0);
                String applicant = (String) table.getValueAt(row, 2);
                new DialogoCuestionario((Frame) SwingUtilities.getWindowAncestor(this), appId, applicant).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una solicitud.");
            }
        });

        actionPanel.add(btnRefresh);
        actionPanel.add(btnDelete);
        actionPanel.add(btnNew);
        actionPanel.add(btnAnswers);
        add(actionPanel, BorderLayout.SOUTH);

        loadApplications();
    }

    private void loadApplications() {
        tableModel.setRowCount(0);
        String sql = "SELECT Application_ID, Date_of_Application, Name_of_Applicant, Type_of_Policy_Code, Application_Description FROM Applications ORDER BY Application_ID DESC";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Application_ID"), rs.getTimestamp("Date_of_Application"),
                        rs.getString("Name_of_Applicant"), rs.getString("Type_of_Policy_Code"),
                        rs.getString("Application_Description")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteApplication() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud para eliminar.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar solicitud #" + id + "? Se borrarán también las respuestas asociadas.", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Applications WHERE Application_ID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadApplications();
                JOptionPane.showMessageDialog(this, "Solicitud eliminada.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    private void showNewAppDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Solicitud", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        JTextField txtNombre = new JTextField();
        JTextField txtPoliza = new JTextField("AUTO-BAS");
        JTextField txtDesc = new JTextField();

        dialog.add(new JLabel(" Nombre Solicitante:")); dialog.add(txtNombre);
        dialog.add(new JLabel(" Código Póliza (Ej. AUTO-BAS):")); dialog.add(txtPoliza);
        dialog.add(new JLabel(" Descripción:")); dialog.add(txtDesc);

        JButton btnSave = estilos.createStyledButton("Crear");
        btnSave.addActionListener(e -> {
            crearSolicitudEnBD(txtNombre.getText(), txtPoliza.getText(), txtDesc.getText());
            dialog.dispose();
            loadApplications();
        });

        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private void crearSolicitudEnBD(String nombre, String poliza, String desc) {
        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);
            // 1. Insertar App
            String sqlApp = "INSERT INTO Applications (Name_of_Applicant, Type_of_Policy_Code, Application_Description) VALUES (?, ?, ?) RETURNING Application_ID";
            long newId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlApp)) {
                ps.setString(1, nombre); ps.setString(2, poliza); ps.setString(3, desc);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) newId = rs.getLong(1);
            }
            // 2. Asignar Preguntas (Lógica simple: primeras 5)
            String sqlMap = "INSERT INTO Question_in_Application (Application_ID, Question_ID) SELECT ?, Question_ID FROM Questions LIMIT 5";
            try (PreparedStatement ps = conn.prepareStatement(sqlMap)) {
                ps.setLong(1, newId);
                ps.executeUpdate();
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Solicitud creada.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }
}