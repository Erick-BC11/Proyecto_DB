import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
public class estilos {
        public static final Color SIDEBAR_COLOR = new Color(44, 62, 80);
        public static final Color ACCENT_COLOR = new Color(52, 152, 219);
        public static final Color BG_COLOR = new Color(236, 240, 241);
        public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
        public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

        public static JButton createStyledButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(NORMAL_FONT);
            btn.setBackground(ACCENT_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        public static void styleTable(JTable table) {
            table.setRowHeight(30);
            table.setFont(NORMAL_FONT);
            table.getTableHeader().setBackground(SIDEBAR_COLOR);
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            table.setSelectionBackground(ACCENT_COLOR);
            table.setSelectionForeground(Color.WHITE);
        }
    }
