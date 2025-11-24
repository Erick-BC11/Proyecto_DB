import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Conexion {
        // Credenciales según tu solicitud
        private static final String URL = "jdbc:postgresql://localhost:5432/Insurance_Applications";
        private static final String USER = "developer";
        private static final String PASSWORD = "Windows2016";

        public static Connection getConnection() {
            Connection conn = null;
            try {
                // Asegúrate de tener el driver de PostgreSQL en tu Build Path
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión exitosa a PostgreSQL.");
            } catch (SQLException e) {
                System.err.println("Error al conectar a la BD: " + e.getMessage());
                e.printStackTrace();
            }
            return conn;
        }
    }
