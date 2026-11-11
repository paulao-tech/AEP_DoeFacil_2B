package doefacil;
import java.sql.*;
final class Banco {
    static Connection abrir() throws SQLException {
        String senha=System.getenv("DB_PASSWORD");
        if(senha==null || senha.isBlank()) throw new SQLException("Defina DB_PASSWORD antes de executar.");
        return DriverManager.getConnection(System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/doefacil"),
                System.getenv().getOrDefault("DB_USER","doefacil"),senha);
    }
    interface Operacao<T>{T executar(Connection c) throws SQLException;}
    static <T>T transacao(Operacao<T> trabalho) throws SQLException {
        try(Connection c=abrir()){
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); c.setAutoCommit(false);
            try {T valor=trabalho.executar(c);c.commit();return valor;}
            catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
