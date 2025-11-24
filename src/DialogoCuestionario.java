import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DialogoCuestionario extends JDialog {

    private int applicationId;
    private JPanel questionsPanel;
    // Mapa para guardar referencia: QuestionID -> Componente Visual (JTextField)
    private Map<Integer, JTextField> answerInputs = new HashMap<>();

    public DialogoCuestionario(Frame owner, int appId, String applicantName) {
        super(owner, "Cuestionario - " + applicantName, true);
        this.applicationId = appId;

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(estilos.BG_COLOR);

        // Header
        JLabel lblTitle = new JLabel("Responda las preguntas asignadas", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Panel Central con Scroll
        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(questionsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Footer con Botón Guardar
        JPanel footer = new JPanel();
        footer.setBackground(estilos.BG_COLOR);
        JButton btnSave = estilos.createStyledButton("Guardar Respuestas");
        btnSave.addActionListener(e -> saveAnswers());
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        loadQuestions();
    }

    private void loadQuestions() {
        // Query: Trae el texto de la pregunta y si ya existe una respuesta guardada
        String sql =
                "SELECT q.Question_ID, q.Question_Text, ca.Answer_Text " +
                        "FROM Question_in_Application qa " +
                        "JOIN Questions q ON qa.Question_ID = q.Question_ID " +
                        "LEFT JOIN Customer_Answers ca ON ca.Application_ID = qa.Application_ID " +
                        "     AND ca.Question_ID = q.Question_ID " +
                        "WHERE qa.Application_ID = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int qId = rs.getInt("Question_ID");
                String qText = rs.getString("Question_Text");
                String prevAnswer = rs.getString("Answer_Text");

                addQuestionComponent(qId, qText, prevAnswer);
            }

            // Refrescar UI
            questionsPanel.revalidate();
            questionsPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addQuestionComponent(int qId, String text, String prevAnswer) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 20, 10, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lbl = new JLabel(text);
        lbl.setFont(estilos.NORMAL_FONT);

        JTextField txtAnswer = new JTextField();
        if (prevAnswer != null) txtAnswer.setText(prevAnswer);

        p.add(lbl, BorderLayout.NORTH);
        p.add(txtAnswer, BorderLayout.CENTER);

        questionsPanel.add(p);
        questionsPanel.add(new JSeparator());

        // Guardamos referencia para usarla al guardar
        answerInputs.put(qId, txtAnswer);
    }

    private void saveAnswers() {
        // Guardamos (Upsert) cada respuesta
        // Nota: Asumimos Customer_ID = 1 por defecto para este ejemplo,
        // en real deberías pasar el CustomerID correcto.
        String sqlDelete = "DELETE FROM Customer_Answers WHERE Application_ID = ? AND Question_ID = ?";
        String sqlInsert = "INSERT INTO Customer_Answers (Application_ID, Question_ID, Customer_ID, Answer_Text) VALUES (?, ?, 1, ?)";

        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);

            for (Map.Entry<Integer, JTextField> entry : answerInputs.entrySet()) {
                int qId = entry.getKey();
                String val = entry.getValue().getText();

                if (val.isEmpty()) continue; // Saltamos vacíos

                // 1. Limpiamos anterior (manera simple de hacer upsert sin lógica compleja)
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                    psDel.setInt(1, applicationId);
                    psDel.setInt(2, qId);
                    psDel.executeUpdate();
                }

                // 2. Insertamos nueva
                try (PreparedStatement psIn = conn.prepareStatement(sqlInsert)) {
                    psIn.setInt(1, applicationId);
                    psIn.setInt(2, qId);
                    psIn.setString(3, val);
                    psIn.executeUpdate();
                }
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Respuestas guardadas correctamente.");
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }
}