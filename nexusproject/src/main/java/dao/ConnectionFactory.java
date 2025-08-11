package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões com o banco de dados.
 * Esta classe é responsável por centralizar a criação de conexões com o MySQL,
 * lendo as configurações de um arquivo externo (config.properties).
 * Isso evita que as credenciais do banco fiquem expostas no código-fonte.
 */
public class ConnectionFactory {
    
    private static final Properties properties = new Properties();

    // Bloco estático: é executado apenas uma vez, quando a classe é carregada pela primeira vez.
    static {
        // Usa o ClassLoader para encontrar o arquivo de configuração no classpath.
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Desculpe, não foi possível encontrar o arquivo config.properties");
                throw new IOException("Arquivo de configuração não encontrado.");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao carregar o arquivo de configuração do banco de dados.", ex);
        }
    }

    /**
     * Obtém uma nova conexão com o banco de dados.
     * Utiliza as propriedades carregadas do arquivo config.properties.
     * 
     * @return um objeto Connection com a conexão estabelecida.
     * @throws RuntimeException se ocorrer uma falha ao tentar se conectar ao banco (ex: banco fora do ar, usuário/senha inválidos).
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao obter conexão com o banco de dados.", e);
        }
    }
}