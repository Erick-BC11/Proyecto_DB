import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class PanelInformes extends JPanel {

    private JEditorPane reportViewer;
    private JLabel lblStatus;

    public PanelInformes() {
        setLayout(new BorderLayout(20, 20));
        setBackground(estilos.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Encabezado
        JLabel lblTitle = new JLabel("Centro de Informes y Estadísticas");
        lblTitle.setFont(estilos.TITLE_FONT);
        lblTitle.setForeground(estilos.SIDEBAR_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // 2. Visor de Informes (Usamos HTML para dar formato bonito)
        reportViewer = new JEditorPane();
        reportViewer.setContentType("text/html");
        reportViewer.setEditable(false);
        reportViewer.setText("<html><body style='font-family: sans-serif; padding: 20px;'>" +
                "<h2 style='color: #2c3e50;'>Seleccione un informe</h2>" +
                "<p>Utilice los botones de la derecha para generar reportes.</p>" +
                "</body></html>");

        add(new JScrollPane(reportViewer), BorderLayout.CENTER);

        // 3. Panel Lateral de Botones
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setBackground(estilos.BG_COLOR);
        // Aumentamos el ancho a 250px para que el texto quepa bien
        sideBar.setPreferredSize(new Dimension(250, 0));

        // Creamos el panel que contiene los botones, alineado al NORTE (Arriba)
        JPanel buttonGrid = new JPanel(new GridLayout(0, 1, 10, 15)); // 1 Columna, Espacio vertical de 15px
        buttonGrid.setBackground(estilos.BG_COLOR);
        // Borde interno para que no toquen los bordes
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JButton btnReport1 = estilos.createStyledButton("1. Pólizas Populares");
        JButton btnReport2 = estilos.createStyledButton("2. Auditoría Respuestas");
        JButton btnReport3 = estilos.createStyledButton("3. Resumen Mensual");

        // Acciones (Las mismas que tenías)
        btnReport1.addActionListener(e -> generarInformePolizas());
        btnReport2.addActionListener(e -> generarInformeRespuestas());
        btnReport3.addActionListener(e -> generarInformeMensual());

        // Agregamos todo al Grid
        JLabel lblGen = new JLabel("Que desea generar?:");
        lblGen.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonGrid.add(lblGen);
        buttonGrid.add(btnReport1);
        buttonGrid.add(btnReport2);
        buttonGrid.add(btnReport3);
        // Un espacio vacío visual
        buttonGrid.add(Box.createVerticalStrut(20));

        // CLAVE: Agregamos el grid al NORTE (North) del sidebar.
        // Esto evita que los botones se estiren hasta abajo.
        sideBar.add(buttonGrid, BorderLayout.NORTH);

        add(sideBar, BorderLayout.EAST);
    }

    // --- INFORME 1: Cantidad de Solicitudes por Tipo de Póliza ---
    private void generarInformePolizas() {
        StringBuilder html = new StringBuilder();
        iniciarHTML(html, "Popularidad de Pólizas", "Distribución de solicitudes por tipo de seguro.");

        String sql = "SELECT p.Type_of_Policy_Description, COUNT(a.Application_ID) as Total " +
                "FROM Applications a " +
                "JOIN Ref_Type_of_Policy p ON a.Type_of_Policy_Code = p.Type_of_Policy_Code " +
                "GROUP BY p.Type_of_Policy_Description " +
                "ORDER BY Total DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            html.append("<table><tr><th>Tipo de Póliza</th><th>Total Solicitudes</th></tr>");

            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>").append(rs.getString("Type_of_Policy_Description")).append("</td>");
                html.append("<td><b>").append(rs.getInt("Total")).append("</b></td>");
                html.append("</tr>");
            }
            html.append("</table>");
            finalizarHTML(html);

        } catch (Exception e) { showError(e); }
    }

    // --- INFORME 2: Auditoría de Respuestas (Quién respondió qué) ---
    private void generarInformeRespuestas() {
        StringBuilder html = new StringBuilder();
        iniciarHTML(html, "Auditoría de Respuestas", "Detalle de respuestas registradas en el sistema.");

        String sql = "SELECT a.Application_ID, q.Question_Text, ca.Answer_Text, ca.Date_Answer_Received " +
                "FROM Customer_Answers ca " +
                "JOIN Applications a ON ca.Application_ID = a.Application_ID " +
                "JOIN Questions q ON ca.Question_ID = q.Question_ID " +
                "ORDER BY a.Application_ID DESC LIMIT 50";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            html.append("<table><tr><th>ID App</th><th>Pregunta</th><th>Respuesta</th><th>Fecha</th></tr>");

            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>").append(rs.getInt("Application_ID")).append("</td>");
                html.append("<td style='font-size: 10px;'>").append(rs.getString("Question_Text")).append("</td>");
                html.append("<td>").append(rs.getString("Answer_Text")).append("</td>");
                html.append("<td>").append(rs.getTimestamp("Date_Answer_Received")).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            finalizarHTML(html);

        } catch (Exception e) { showError(e); }
    }

    // --- INFORME 3: Resumen de Solicitudes Recientes (Estilo Dashboard) ---
    private void generarInformeMensual() {
        StringBuilder html = new StringBuilder();
        iniciarHTML(html, "Listado General de Solicitudes", "Estado actual de las aplicaciones recibidas.");

        String sql = "SELECT Application_ID, Date_of_Application, Name_of_Applicant, Type_of_Policy_Code " +
                "FROM Applications ORDER BY Date_of_Application DESC LIMIT 20";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            html.append("<table><tr><th>ID</th><th>Fecha</th><th>Solicitante</th><th>Póliza</th></tr>");

            while (rs.next()) {
                html.append("<tr>");
                html.append("<td>#").append(rs.getInt("Application_ID")).append("</td>");
                html.append("<td>").append(rs.getDate("Date_of_Application")).append("</td>");
                html.append("<td>").append(rs.getString("Name_of_Applicant")).append("</td>");
                html.append("<td><span style='background-color: #3498db; color: white; padding: 3px;'>")
                        .append(rs.getString("Type_of_Policy_Code")).append("</span></td>");
                html.append("</tr>");
            }
            html.append("</table>");
            finalizarHTML(html);

        } catch (Exception e) { showError(e); }
    }

    // --- UTILIDADES DE FORMATO HTML ---
    private void iniciarHTML(StringBuilder sb, String titulo, String subtitulo) {
        sb.append("<html><head><style>");
        sb.append("body { font-family: 'Segoe UI', sans-serif; padding: 20px; color: #333; }");
        sb.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        sb.append("th { background-color: #2c3e50; color: white; padding: 10px; text-align: left; }");
        sb.append("td { border-bottom: 1px solid #ddd; padding: 8px; }");
        sb.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        sb.append("</style></head><body>");
        sb.append("<h1>").append(titulo).append("</h1>");
        sb.append("<p><i>").append(subtitulo).append("</i></p>");
    }

    private void finalizarHTML(StringBuilder sb) {
        sb.append("</body></html>");
        reportViewer.setText(sb.toString());
        // Scroll al inicio
        reportViewer.setCaretPosition(0);
    }

    private void showError(Exception e) {
        reportViewer.setText("<html><body><h2 style='color:red'>Error al generar informe</h2><p>" + e.getMessage() + "</p></body></html>");
        e.printStackTrace();
    }
}