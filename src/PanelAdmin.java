import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelAdmin extends JPanel {

    public PanelAdmin() {
        setLayout(new BorderLayout());
        setBackground(estilos.BG_COLOR);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(estilos.NORMAL_FONT);

        tabs.addTab("Tipos de Póliza", new PanelCRUDPolizas());
        tabs.addTab("Catálogo de Preguntas", new PanelCRUDPreguntas());

        add(tabs, BorderLayout.CENTER);
    }
}

// --- SUB-PANEL PARA POLIZAS ---
class PanelCRUDPolizas extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public PanelCRUDPolizas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] cols = {"Código", "Descripción"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        estilos.styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = estilos.createStyledButton("Nueva Póliza");
        JButton btnDel = estilos.createStyledButton("Eliminar Selecc.");
        JButton btnRef = estilos.createStyledButton("Recargar");

        btnAdd.addActionListener(e -> showDialog());
        btnDel.addActionListener(e -> deleteSelected());
        btnRef.addActionListener(e -> loadData());

        btnPanel.add(btnRef); btnPanel.add(btnDel); btnPanel.add(btnAdd);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Conexion.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM Ref_Type_of_Policy")) {
            while(rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2)});
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if(r == -1) return;
        String code = (String) table.getValueAt(r, 0);

        if(JOptionPane.showConfirmDialog(this, "¿Borrar Póliza " + code + "?") == JOptionPane.YES_OPTION) {
            try (Connection conn = Conexion.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM Ref_Type_of_Policy WHERE Type_of_Policy_Code=?")) {
                ps.setString(1, code);
                ps.executeUpdate();
                loadData();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error (Puede estar en uso): " + e.getMessage()); }
        }
    }

    private void showDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Póliza", true);
        d.setSize(300, 200); d.setLayout(new GridLayout(3, 2)); d.setLocationRelativeTo(this);
        JTextField tCode = new JTextField(); JTextField tDesc = new JTextField();
        d.add(new JLabel(" Código:")); d.add(tCode);
        d.add(new JLabel(" Descripción:")); d.add(tDesc);
        JButton b = estilos.createStyledButton("Guardar");
        b.addActionListener(e -> {
            try(Connection c = Conexion.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO Ref_Type_of_Policy VALUES(?,?)")){
                ps.setString(1, tCode.getText()); ps.setString(2, tDesc.getText());
                ps.executeUpdate(); d.dispose(); loadData();
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });
        d.add(b); d.setVisible(true);
    }
}

// --- SUB-PANEL PARA PREGUNTAS ---
class PanelCRUDPreguntas extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public PanelCRUDPreguntas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        String[] cols = {"ID", "Pregunta", "Tipo Dato"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        estilos.styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = estilos.createStyledButton("Nueva Pregunta");
        JButton btnDel = estilos.createStyledButton("Eliminar Selecc.");
        JButton btnRef = estilos.createStyledButton("Recargar");

        btnAdd.addActionListener(e -> showDialog());
        btnDel.addActionListener(e -> deleteSelected());
        btnRef.addActionListener(e -> loadData());

        btnPanel.add(btnRef); btnPanel.add(btnDel); btnPanel.add(btnAdd);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Conexion.getConnection(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT Question_ID, Question_Text, Type_of_Question_Code FROM Questions ORDER BY Question_ID")) {
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void deleteSelected() {
        int r = table.getSelectedRow();
        if(r == -1) return;
        int id = (int) table.getValueAt(r, 0);

        if(JOptionPane.showConfirmDialog(this, "¿Borrar Pregunta ID " + id + "?") == JOptionPane.YES_OPTION) {
            try (Connection conn = Conexion.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM Questions WHERE Question_ID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadData();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    private void showDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Pregunta", true);
        d.setSize(400, 200); d.setLayout(new GridLayout(3, 2)); d.setLocationRelativeTo(this);
        JTextField tText = new JTextField();
        String[] types = {"TEXT", "NUMERIC", "YES_NO", "DATE"}; // Simplificado
        JComboBox<String> tType = new JComboBox<>(types);

        d.add(new JLabel(" Texto Pregunta:")); d.add(tText);
        d.add(new JLabel(" Tipo Dato:")); d.add(tType);
        JButton b = estilos.createStyledButton("Guardar");
        b.addActionListener(e -> {
            try(Connection c = Conexion.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO Questions (Question_Text, Type_of_Question_Code) VALUES(?,?)")){
                ps.setString(1, tText.getText()); ps.setString(2, (String)tType.getSelectedItem());
                ps.executeUpdate(); d.dispose(); loadData();
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });
        d.add(b); d.setVisible(true);
    }
}