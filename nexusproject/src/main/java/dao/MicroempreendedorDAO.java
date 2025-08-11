package dao;

import model.Microempreendedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Endereco;

/**
 * DAO para a entidade Microempreendedor.
 * Esta classe gerencia os dados do usuário principal do sistema.
 * Inclui uma regra de negócio para permitir apenas um cadastro e a funcionalidade
 * crítica de resetar o banco de dados.
 */
public class MicroempreendedorDAO {
    
    /**
     * Insere o único microempreendedor no sistema.
     * Esta operação é transacional e verifica se já existe um usuário cadastrado
     * antes de prosseguir, garantindo a regra de negócio de "usuário único".
     * 
     * @param micemp O objeto Microempreendedor a ser inserido.
     * @throws SQLException Se já existir um microempreendedor cadastrado ou se ocorrer outro erro de banco.
     */
    public void inserirMicroempreendedor(Microempreendedor micemp) throws SQLException {
        String verificaSql = "SELECT COUNT(*) FROM microempreendedor";
        String enderecoSQL = "INSERT INTO endereco (rua, bairro, cidade, numero, complemento) VALUES (?, ?, ?, ?, ?)";
        String pessoaSQL = "INSERT INTO pessoa (nome, email, cnpj_cpf, endereco_id) VALUES (?, ?, ?, ?)";
        String micempSQL = "INSERT INTO microempreendedor (pessoa_id, senha, caixa, login) VALUES (?,?,?,?)";

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false); 
            
            try {
                // Regra de Negócio: Impede o cadastro de mais de um microempreendedor.
                try (Statement verificaStmt = connection.createStatement();
                     ResultSet rs = verificaStmt.executeQuery(verificaSql)) {
                    if (rs.next() && rs.getInt(1) >= 1) {
                        throw new SQLException("Já existe um microempreendedor cadastrado no sistema.");
                    }
                }

                // 1. Insere o endereço e recupera o ID gerado.
                int enderecoId;
                try (PreparedStatement enderecoStmt = connection.prepareStatement(enderecoSQL, Statement.RETURN_GENERATED_KEYS)) {
                    Endereco endereco = micemp.getEndereco();
                    enderecoStmt.setString(1, endereco.getRua());
                    enderecoStmt.setString(2, endereco.getBairro());
                    enderecoStmt.setString(3, endereco.getCidade());
                    enderecoStmt.setString(4, endereco.getNumero());
                    enderecoStmt.setString(5, endereco.getComplemento());
                    enderecoStmt.executeUpdate();
                    try (ResultSet generatedKeys = enderecoStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            enderecoId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Falha ao inserir endereço, nenhum ID obtido.");
                        }
                    }
                }
                
                // 2. Insere a pessoa e recupera o ID gerado.
                int pessoaId;
                try (PreparedStatement pessoaStmt = connection.prepareStatement(pessoaSQL, Statement.RETURN_GENERATED_KEYS)) {
                    pessoaStmt.setString(1, micemp.getNome());
                    pessoaStmt.setString(2, micemp.getEmail());
                    pessoaStmt.setString(3, micemp.getIdentificador());
                    pessoaStmt.setInt(4, enderecoId);
                    pessoaStmt.executeUpdate();
                    try (ResultSet generatedKeys = pessoaStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            pessoaId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Falha ao inserir pessoa, nenhum ID obtido.");
                        }
                    }
                }

                // 3. Insere o microempreendedor.
                try (PreparedStatement micempStmt = connection.prepareStatement(micempSQL)) {
                    micempStmt.setInt(1, pessoaId);
                    micempStmt.setString(2, micemp.getSenha());
                    micempStmt.setBigDecimal(3, micemp.getCaixa()); // Caixa inicial
                    micempStmt.setString(4, micemp.getLogin());
                    micempStmt.executeUpdate();
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback(); 
                throw e; 
            }
        }
    }

    /**
     * Consulta os dados do microempreendedor cadastrado no sistema.
     * 
     * @return um objeto Microempreendedor com os dados do perfil, ou null se não houver cadastro.
     * @throws SQLException Se ocorrer um erro durante a consulta.
     */
    public Microempreendedor consultarMicroempreendedor() throws SQLException {
        // A consulta usa JOINs para buscar todos os dados relacionados ao microempreendedor de uma só vez.
        // LIMIT 1 é usado para garantir que apenas um registro seja retornado, reforçando a regra de negócio.
        String sql = "SELECT p.pessoa_id, p.nome, p.email, p.cnpj_cpf, e.rua, e.bairro, e.cidade, e.numero, e.complemento, m.senha, m.caixa, m.login " +
                     "FROM pessoa p " +
                     "JOIN endereco e ON p.endereco_id = e.endereco_id " +
                     "JOIN microempreendedor m ON p.pessoa_id = m.pessoa_id " +
                     "ORDER BY p.pessoa_id ASC LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Endereco endereco = new Endereco(rs.getString("rua"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("numero"), rs.getString("complemento"));
                
                Microempreendedor micemp = new Microempreendedor();
                micemp.setIdentificador(rs.getString("cnpj_cpf"));
                micemp.setNome(rs.getString("nome"));
                micemp.setEmail(rs.getString("email"));
                micemp.setEndereco(endereco);
                micemp.setLogin(rs.getString("login"));
                micemp.setSenha(rs.getString("senha"));
                micemp.setCaixa(rs.getBigDecimal("caixa"));
                return micemp;
            }
        }
        return null;
    }

    /**
     * Atualiza os dados do microempreendedor no banco de dados.
     * Operação transacional para garantir consistência entre as tabelas.
     * 
     * @param micemp O objeto Microempreendedor com os dados atualizados.
     * @throws SQLException Se ocorrer um erro durante a atualização.
     */
    public void atualizarMicroempreendedor(Microempreendedor micemp) throws SQLException {
        String enderecoSQL = "UPDATE endereco SET rua = ?, bairro = ?, cidade = ?, numero = ?, complemento = ? WHERE endereco_id = (SELECT endereco_id FROM pessoa WHERE cnpj_cpf = ?)";
        String pessoaSQL = "UPDATE pessoa SET nome = ?, email = ? WHERE cnpj_cpf = ?";
        String micempSQL = "UPDATE microempreendedor SET senha = ?, login = ? WHERE pessoa_id = (SELECT pessoa_id FROM pessoa WHERE cnpj_cpf = ?)";

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            
            try {
                // Atualiza o endereço associado à pessoa.
                try(PreparedStatement stmt = connection.prepareStatement(enderecoSQL)){
                    stmt.setString(1, micemp.getEndereco().getRua());
                    stmt.setString(2, micemp.getEndereco().getBairro());
                    stmt.setString(3, micemp.getEndereco().getCidade());
                    stmt.setString(4, micemp.getEndereco().getNumero());
                    stmt.setString(5, micemp.getEndereco().getComplemento());
                    stmt.setString(6, micemp.getIdentificador());
                    if(stmt.executeUpdate() == 0) throw new SQLException("Endereço não encontrado para atualização.");
                }
                
                // Atualiza os dados na tabela pessoa.
                try(PreparedStatement stmt = connection.prepareStatement(pessoaSQL)){
                    stmt.setString(1, micemp.getNome());
                    stmt.setString(2, micemp.getEmail());
                    stmt.setString(3, micemp.getIdentificador());
                    if(stmt.executeUpdate() == 0) throw new SQLException("Pessoa não encontrada para atualização.");
                }
                
                // Atualiza os dados específicos na tabela microempreendedor.
                try(PreparedStatement stmt = connection.prepareStatement(micempSQL)){
                    stmt.setString(1, micemp.getSenha());
                    stmt.setString(2, micemp.getLogin());
                    stmt.setString(3, micemp.getIdentificador());
                    if(stmt.executeUpdate() == 0) throw new SQLException("Microempreendedor não encontrado para atualização.");
                }
                
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Verifica as credenciais de login do microempreendedor.
     *
     * @param login O login do usuário.
     * @param senha A senha do usuário.
     * @return true se o login e a senha corresponderem, false caso contrário.
     * @throws SQLException Se ocorrer um erro de banco.
     */
    public boolean verificarLogin(String login, String senha) throws SQLException {
        String sql = "SELECT 1 FROM microempreendedor WHERE login = ? AND senha = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, login);
            pstmt.setString(2, senha);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                // Se a consulta retornar qualquer linha, significa que a combinação login/senha existe.
                return resultSet.next(); 
            }
        }
    }

    /**
     * ATENÇÃO: OPERAÇÃO DESTRUTIVA.
     * Reseta completamente o banco de dados, apagando todos os dados de todas as tabelas
     * e reiniciando os contadores de auto-incremento. Esta ação é IRREVERSÍVEL.
     * É usada para permitir que o sistema seja "limpo" para um novo cadastro de microempreendedor.
     * 
     * @throws SQLException Se ocorrer um erro durante o reset.
     */
    public void reset() throws SQLException {
        String[] tables = {"venda", "produto", "cliente", "microempreendedor", "pessoa", "endereco"};
        
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            
            try (Statement statement = connection.createStatement()) {
                // Desabilita temporariamente a verificação de chaves estrangeiras.
                // Isso é necessário para permitir a exclusão de dados em qualquer ordem
                // sem causar erros de violação de restrição.
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                for (String table : tables) {
                    statement.execute("DELETE FROM " + table); // Apaga todos os registros.
                    statement.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1"); // Reinicia o contador.
                }

                // Reabilita a verificação de chaves estrangeiras, uma prática de segurança crucial.
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
                
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Falha ao resetar o banco de dados: " + e.getMessage(), e);
            }
        }
    }
}