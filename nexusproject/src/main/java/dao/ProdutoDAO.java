package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Produto;

/**
 * DAO (Data Access Object) para a entidade Produto.
 * Contém os métodos para interagir com a tabela 'produto' no banco de dados.
 * Implementa as operações de CRUD (Create, Read, Update, Delete).
 */
public class ProdutoDAO {

    /**
     * Insere um novo produto no banco de dados.
     * 
     * @param obj O objeto Produto a ser inserido.
     * @throws SQLException Se ocorrer um erro durante a inserção no banco de dados.
     */
    public void inserir(Produto obj) throws SQLException {
        // Instrução SQL parametrizada para evitar Injeção de SQL.
        String sql = "INSERT INTO produto (nome, preco_unitario, quantidade, categoria, preco_custo) VALUES (?,?,?,?,?);";

        // 'try-with-resources' garante que a conexão e o statement sejam fechados automaticamente.
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, obj.getNome());
            pstmt.setBigDecimal(2, obj.getPrecoUnitario());
            pstmt.setInt(3, obj.getQuantidade());
            pstmt.setString(4, obj.getCategoria());
            pstmt.setBigDecimal(5, obj.getPrecoCusto());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Falha ao inserir o produto, nenhuma linha foi modificada.");
            }
        }
    }

    /**
     * Lista todos os produtos cadastrados no banco de dados.
     * 
     * @return uma lista de objetos Produto.
     * @throws SQLException Se ocorrer um erro durante a consulta no banco de dados.
     */
    public List<Produto> listarProdutos() throws SQLException {
        String sql = "SELECT * FROM produto";
        List<Produto> produtos = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Itera sobre o resultado da consulta, criando um objeto Produto para cada registro.
            while (rs.next()) {
                Produto produto = new Produto(
                    rs.getInt("item_id"),
                    rs.getString("nome"),
                    rs.getBigDecimal("preco_unitario"),
                    rs.getInt("quantidade"),
                    rs.getString("categoria"),
                    rs.getBigDecimal("preco_custo")
                );
                produtos.add(produto);
            }
        }
        return produtos;
    }

    /**
     * Consulta um produto específico pelo seu ID.
     * 
     * @param id O ID (item_id) do produto a ser consultado.
     * @return um objeto Produto se encontrado, ou null caso contrário.
     * @throws SQLException Se ocorrer um erro durante a consulta no banco de dados.
     */
    public Produto consultarProduto(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE item_id = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Produto prod = new Produto();
                    prod.setProdutoId(rs.getInt("item_id"));
                    prod.setNome(rs.getString("nome"));
                    prod.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                    prod.setQuantidade(rs.getInt("quantidade"));
                    prod.setCategoria(rs.getString("categoria"));
                    prod.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    return prod;
                }
            }
        }
        return null;
    }

    /**
     * Atualiza os dados de um produto existente no banco de dados.
     * 
     * @param obj O objeto Produto com os dados atualizados. O ID do produto é usado para localizá-lo.
     * @throws SQLException Se ocorrer um erro durante a atualização ou se o produto não for encontrado.
     */
    public void atualizar(Produto obj) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, preco_unitario = ?, quantidade = ?, categoria = ?, preco_custo = ? WHERE (item_id = ?);";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, obj.getNome());
            pstmt.setBigDecimal(2, obj.getPrecoUnitario());
            pstmt.setInt(3, obj.getQuantidade());
            pstmt.setString(4, obj.getCategoria());
            pstmt.setBigDecimal(5, obj.getPrecoCusto());
            pstmt.setInt(6, obj.getProdutoId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Falha ao atualizar o produto, nenhuma linha foi modificada ou produto não encontrado.");
            }
        }
    }

    /**
     * Deleta um produto do banco de dados pelo seu ID.
     * Contém uma lógica de negócio para impedir a exclusão de um produto que já foi vendido,
     * garantindo a integridade referencial dos dados.
     * 
     * @param id O ID do produto a ser deletado.
     * @throws SQLException Se o produto estiver associado a uma venda ou se ocorrer outro erro de banco.
     */
    public void deletar(int id) throws SQLException {
        // Validação de regra de negócio: não permitir exclusão se o produto está em uma venda.
        String checkVendaSql = "SELECT COUNT(*) FROM venda WHERE item_id = ?";
        String deleteSql = "DELETE FROM produto WHERE item_id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Primeiro, verifica se o produto está associado a alguma venda.
            try (PreparedStatement checkStmt = conn.prepareStatement(checkVendaSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    // Se a contagem for maior que 0, significa que existem registros de venda para este produto.
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Não é possível deletar o produto, pois ele está associado a uma ou mais vendas.");
                    }
                }
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Falha ao deletar o produto, produto não encontrado.");
                }
            }
        }
    }
}