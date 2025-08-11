package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Endereco;

/**
 * DAO para a entidade Cliente.
 * Gerencia as operações de CRUD para clientes, interagindo com as tabelas 'cliente',
 * 'pessoa' e 'endereco' de forma transacional para garantir a consistência dos dados.
 */
public class ClienteDAO {

    /**
     * Insere um novo cliente no banco de dados.
     * A operação é transacional: insere primeiro o endereço, depois a pessoa e, por fim, o cliente.
     * Se qualquer uma das inserções falhar, todas as operações são desfeitas (rollback).
     * 
     * @param cliente O objeto Cliente a ser inserido.
     * @throws SQLException Se ocorrer um erro de banco, como violação de chave única (CPF/CNPJ duplicado)
     *                      ou campos obrigatórios nulos.
     */
    public void inserirCliente(Cliente cliente) throws SQLException {
        String enderecoSQL = "INSERT INTO endereco (rua, bairro, cidade, numero, complemento) VALUES (?, ?, ?, ?, ?)";
        String pessoaSQL = "INSERT INTO pessoa (nome, email, cnpj_cpf, endereco_id) VALUES (?, ?, ?, ?)";
        String clienteSQL = "INSERT INTO cliente (pessoa_id, telefone) VALUES (?, ?)";

        try (Connection connection = ConnectionFactory.getConnection()) {
            // Desabilita o auto-commit para controlar a transação manualmente.
            // Isso garante que todas as inserções sejam tratadas como uma única operação.
            connection.setAutoCommit(false); 

            try {
                // 1. Inserir Endereço e obter o ID gerado
                int enderecoId;
                // Statement.RETURN_GENERATED_KEYS informa ao JDBC que queremos recuperar a chave primária gerada.
                try (PreparedStatement enderecoStmt = connection.prepareStatement(enderecoSQL, Statement.RETURN_GENERATED_KEYS)) {
                    Endereco endereco = cliente.getEndereco();
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

                // 2. Inserir Pessoa (usando o ID do endereço) e obter o ID gerado
                int pessoaId;
                try (PreparedStatement pessoaStmt = connection.prepareStatement(pessoaSQL, Statement.RETURN_GENERATED_KEYS)) {
                    pessoaStmt.setString(1, cliente.getNome());
                    pessoaStmt.setString(2, cliente.getEmail());
                    pessoaStmt.setString(3, cliente.getIdentificador());
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
                
                // 3. Inserir Cliente (usando o ID da pessoa)
                try (PreparedStatement clienteStmt = connection.prepareStatement(clienteSQL)) {
                    clienteStmt.setInt(1, pessoaId);
                    clienteStmt.setString(2, cliente.getTelefone());
                    clienteStmt.executeUpdate();
                }

                // Se todas as operações foram bem-sucedidas, confirma a transação.
                connection.commit(); 

            } catch (SQLException e) {
                // Se qualquer erro ocorrer, desfaz todas as alterações feitas na transação.
                connection.rollback();

                // Tratamento de erro específico para fornecer feedback melhor ao usuário.
                if (e.getErrorCode() == 1062) { // Código de erro do MySQL para 'Duplicate entry'
                    throw new SQLException("O CPF/CNPJ informado já está cadastrado no sistema.", e);

                } else if (e.getErrorCode() == 1048) { // Código para 'Column cannot be null'
                    throw new SQLException("Todos os campos obrigatórios devem ser preenchidos. Verifique os dados e tente novamente.", e);

                } else {
                    throw new SQLException("Ocorreu um erro inesperado ao salvar o cliente.", e);
                }
            }
        } 
    }
    
    /**
     * Lista todos os clientes, juntando dados das tabelas 'cliente', 'pessoa' e 'endereco'.
     * 
     * @return Uma lista de objetos Cliente.
     * @throws SQLException Se ocorrer um erro durante a consulta.
     */
    public List<Cliente> listarClientes() throws SQLException {
        // A consulta usa JOIN para combinar os dados das três tabelas relacionadas.
        String sql = "SELECT p.pessoa_id, p.nome, p.email, p.cnpj_cpf, e.rua, e.bairro, e.cidade, e.numero, e.complemento, c.telefone " +
                     "FROM pessoa p " +
                     "JOIN endereco e ON p.endereco_id = e.endereco_id " +
                     "JOIN cliente c ON p.pessoa_id = c.pessoa_id ";

        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Endereco endereco = new Endereco(
                    rs.getString("rua"),
                    rs.getString("bairro"),
                    rs.getString("cidade"),
                    rs.getString("numero"),
                    rs.getString("complemento")
                );
                Cliente cliente = new Cliente();
                cliente.setIdentificador(rs.getString("cnpj_cpf"));
                cliente.setNome(rs.getString("nome"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEndereco(endereco);
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    /**
     * Consulta um cliente específico pelo seu identificador (CPF/CNPJ).
     * 
     * @param identificador O CPF ou CNPJ do cliente.
     * @return um objeto Cliente se encontrado, ou null caso contrário.
     * @throws SQLException Se ocorrer um erro durante a consulta.
     */
    public Cliente consultarCliente(String identificador) throws SQLException {
        String sql = "SELECT p.pessoa_id, p.nome, p.email, p.cnpj_cpf, e.rua, e.bairro, e.cidade, e.numero, e.complemento, c.telefone " +
                     "FROM pessoa p " +
                     "JOIN endereco e ON p.endereco_id = e.endereco_id " +
                     "JOIN cliente c ON p.pessoa_id = c.pessoa_id " +
                     "WHERE p.cnpj_cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, identificador);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Endereco endereco = new Endereco(
                        rs.getString("rua"),
                        rs.getString("bairro"),
                        rs.getString("cidade"),
                        rs.getString("numero"),
                        rs.getString("complemento")
                    );

                    Cliente cliente = new Cliente();
                    cliente.setIdentificador(rs.getString("cnpj_cpf"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setEndereco(endereco);
                    cliente.setTelefone(rs.getString("telefone"));
                    return cliente;
                }
            }
        }
        return null;
    }

    /**
     * Atualiza os dados de um cliente existente de forma transacional.
     * 
     * @param cliente O objeto Cliente com os dados atualizados.
     * @throws SQLException Se ocorrer um erro durante a atualização.
     */
    public void atualizarCliente(Cliente cliente) throws SQLException {
        String enderecoSQL = "UPDATE endereco SET rua = ?, bairro = ?, cidade = ?, numero = ?, complemento = ? WHERE endereco_id = (SELECT endereco_id FROM pessoa WHERE cnpj_cpf = ?)";
        String pessoaSQL = "UPDATE pessoa SET nome = ?, email = ? WHERE cnpj_cpf = ?";
        String clienteSQL = "UPDATE cliente SET telefone = ? WHERE pessoa_id = (SELECT pessoa_id FROM pessoa WHERE cnpj_cpf = ?)";

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1. Atualizar Endereço
                try (PreparedStatement enderecoStmt = connection.prepareStatement(enderecoSQL)) {
                    Endereco endereco = cliente.getEndereco();
                    enderecoStmt.setString(1, endereco.getRua());
                    enderecoStmt.setString(2, endereco.getBairro());
                    enderecoStmt.setString(3, endereco.getCidade());
                    enderecoStmt.setString(4, endereco.getNumero());
                    enderecoStmt.setString(5, endereco.getComplemento());
                    enderecoStmt.setString(6, cliente.getIdentificador());
                    if (enderecoStmt.executeUpdate() == 0) throw new SQLException("Falha ao atualizar endereço, nenhuma linha afetada.");
                }

                // 2. Atualizar Pessoa
                try (PreparedStatement pessoaStmt = connection.prepareStatement(pessoaSQL)) {
                    pessoaStmt.setString(1, cliente.getNome());
                    pessoaStmt.setString(2, cliente.getEmail());
                    pessoaStmt.setString(3, cliente.getIdentificador());
                    if (pessoaStmt.executeUpdate() == 0) throw new SQLException("Falha ao atualizar pessoa, nenhuma linha afetada.");
                }

                // 3. Atualizar Cliente
                try (PreparedStatement clienteStmt = connection.prepareStatement(clienteSQL)) {
                    clienteStmt.setString(1, cliente.getTelefone());
                    clienteStmt.setString(2, cliente.getIdentificador());
                    if (clienteStmt.executeUpdate() == 0) throw new SQLException("Falha ao atualizar cliente, nenhuma linha afetada.");
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Erro ao atualizar cliente: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Deleta um cliente e seus dados associados (pessoa e endereço) de forma transacional.
     * 
     * @param identificador O CPF ou CNPJ do cliente a ser deletado.
     * @throws SQLException Se o cliente tiver vendas registradas (violação de chave estrangeira) ou se ocorrer outro erro.
     */
    public void deletarCliente(String identificador) throws SQLException {
        // A ordem de exclusão é importante para respeitar as restrições de chave estrangeira:
        // primeiro deletamos das tabelas "filhas" (cliente) e depois das "mães" (pessoa, endereco).
        String sqlSelectEndereco = "SELECT endereco_id FROM pessoa WHERE cnpj_cpf = ?";
        String sqlDeleteCliente = "DELETE FROM cliente WHERE pessoa_id = (SELECT pessoa_id FROM pessoa WHERE cnpj_cpf = ?)";
        String sqlDeletePessoa = "DELETE FROM pessoa WHERE cnpj_cpf = ?";
        String sqlDeleteEndereco = "DELETE FROM endereco WHERE endereco_id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Obter o endereco_id antes de deletar a pessoa, para não perdê-lo.
                int enderecoId = 0;
                try (PreparedStatement pstmtSelectEndereco = conn.prepareStatement(sqlSelectEndereco)) {
                    pstmtSelectEndereco.setString(1, identificador);
                    try (ResultSet rs = pstmtSelectEndereco.executeQuery()) {
                        if (rs.next()) {
                            enderecoId = rs.getInt("endereco_id");
                        } else {
                            throw new SQLException("Cliente não encontrado para exclusão.");
                        }
                    }
                }
                
                // 2. Deletar da tabela 'cliente'.
                try(PreparedStatement pstmtDeleteCliente = conn.prepareStatement(sqlDeleteCliente)) {
                    pstmtDeleteCliente.setString(1, identificador);
                    pstmtDeleteCliente.executeUpdate();
                }
                
                // 3. Deletar da tabela 'pessoa'.
                try(PreparedStatement pstmtDeletePessoa = conn.prepareStatement(sqlDeletePessoa)) {
                    pstmtDeletePessoa.setString(1, identificador);
                    int rowsAffectedPessoa = pstmtDeletePessoa.executeUpdate();
                     if (rowsAffectedPessoa == 0) {
                         throw new SQLException("Pessoa não encontrada para exclusão.");
                     }
                }

                // 4. Deletar da tabela 'endereco' (se o ID foi encontrado).
                if (enderecoId != 0) {
                    try(PreparedStatement pstmtDeleteEndereco = conn.prepareStatement(sqlDeleteEndereco)) {
                        pstmtDeleteEndereco.setInt(1, enderecoId);
                        pstmtDeleteEndereco.executeUpdate();
                    }
                }
                
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                // Código 1451: 'Cannot delete or update a parent row: a foreign key constraint fails'.
                // Isso significa que o cliente tem vendas associadas.
                if (e.getErrorCode() == 1451) { 
                    throw new SQLException("Não é possível excluir este cliente, pois ele possui vendas registradas.", e);
                } else {
                    throw new SQLException("Erro ao deletar cliente: " + e.getMessage(), e);
                }
            }
        }
    }
}