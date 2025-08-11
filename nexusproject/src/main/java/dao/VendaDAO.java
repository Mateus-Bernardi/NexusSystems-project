package dao;

import java.math.BigDecimal; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
import model.Endereco;
import model.Produto;
import model.Venda;

/**
 * DAO para a entidade Venda.
 * Gerencia todas as operações relacionadas a vendas, incluindo a inserção transacional,
 * listagem e a geração de dados para relatórios.
 */
public class VendaDAO {
    
    /**
     * Insere uma nova venda no sistema.
     * Esta é a operação mais crítica, executada como uma transação para garantir
     * a consistência dos dados em quatro etapas:
     * 1. Verifica se há estoque suficiente do produto.
     * 2. Registra a venda na tabela 'venda'.
     * 3. Deduz a quantidade vendida do estoque na tabela 'produto'.
     * 4. Atualiza o caixa total do microempreendedor com o lucro da venda.
     * 
     * @param obj O objeto Venda contendo os detalhes da transação.
     * @throws SQLException Se a quantidade em estoque for insuficiente ou se ocorrer outro erro de banco.
     * @throws IllegalArgumentException Se os objetos Venda, Produto ou Cliente forem nulos.
     */
    public void inserir(Venda obj) throws SQLException {
        if (obj == null || obj.getProduto() == null || obj.getCliente() == null) {
            throw new IllegalArgumentException("Venda, produto e cliente não podem ser nulos.");
        }

        String checkQuantidadeSQL = "SELECT quantidade FROM produto WHERE item_id = ?";
        String insertVendaSQL = "INSERT INTO venda (cliente_id, item_id, data, quantidade, lucro) VALUES ((SELECT p.pessoa_id FROM pessoa p JOIN cliente c ON p.pessoa_id = c.pessoa_id WHERE p.cnpj_cpf = ?), ?, ?, ?, ?)";
        String updateProdutoSQL = "UPDATE produto SET quantidade = quantidade - ? WHERE item_id = ?";
        String sumLucroSQL = "SELECT SUM(lucro) AS total_lucro FROM venda";
        String getMicroempreendedorSQL = "SELECT cnpj_cpf FROM pessoa JOIN microempreendedor ON pessoa.pessoa_id = microempreendedor.pessoa_id LIMIT 1";
        String updateCaixaSQL = "UPDATE microempreendedor SET caixa = ? WHERE pessoa_id = (SELECT pessoa_id FROM pessoa WHERE cnpj_cpf = ?)";
        
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); 

            try {
                // ETAPA 1: Verificar se há estoque disponível para o produto.
                try (PreparedStatement checkQuantidadeStmt = conn.prepareStatement(checkQuantidadeSQL)) {
                    checkQuantidadeStmt.setInt(1, obj.getProduto().getProdutoId());
                    try (ResultSet rs = checkQuantidadeStmt.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getInt("quantidade") < obj.getQuantidade()) {
                                throw new SQLException("Quantidade insuficiente no estoque.");
                            }
                        } else {
                            throw new SQLException("Produto não encontrado no estoque.");
                        }
                    }
                }

                // ETAPA 2: Inserir o registro da venda. O lucro é calculado no momento da inserção.
                try (PreparedStatement insertVendaStmt = conn.prepareStatement(insertVendaSQL)) {
                    insertVendaStmt.setString(1, obj.getCliente().getIdentificador());
                    insertVendaStmt.setInt(2, obj.getProduto().getProdutoId());
                    insertVendaStmt.setObject(3, obj.getDataVenda());
                    insertVendaStmt.setInt(4, obj.getQuantidade());
                    insertVendaStmt.setBigDecimal(5, obj.calcularLucro());
                    if(insertVendaStmt.executeUpdate() == 0) throw new SQLException("Falha ao registrar a venda.");
                }

                // ETAPA 3: Atualizar (diminuir) a quantidade do produto no estoque.
                try (PreparedStatement updateProdutoStmt = conn.prepareStatement(updateProdutoSQL)) {
                    updateProdutoStmt.setInt(1, obj.getQuantidade());
                    updateProdutoStmt.setInt(2, obj.getProduto().getProdutoId());
                    if(updateProdutoStmt.executeUpdate() == 0) throw new SQLException("Falha ao atualizar o estoque.");
                }

                // ETAPA 4: Atualizar o caixa do microempreendedor.
                // Esta abordagem recalcula o total a cada venda para garantir consistência.
                BigDecimal totalLucro = BigDecimal.ZERO;
                try(PreparedStatement sumLucroStmt = conn.prepareStatement(sumLucroSQL);
                    ResultSet rsSum = sumLucroStmt.executeQuery()){
                    if(rsSum.next()) {
                        BigDecimal sum = rsSum.getBigDecimal("total_lucro");
                        if (sum != null) {
                            totalLucro = sum;
                        }
                    }
                }
                
                String micId = null;
                try(PreparedStatement getMicStmt = conn.prepareStatement(getMicroempreendedorSQL);
                    ResultSet rsMic = getMicStmt.executeQuery()) {
                    if(rsMic.next()) micId = rsMic.getString("cnpj_cpf");
                    else throw new SQLException("Microempreendedor não encontrado para atualizar o caixa.");
                }

                try(PreparedStatement updateCaixaStmt = conn.prepareStatement(updateCaixaSQL)) {
                    updateCaixaStmt.setBigDecimal(1, totalLucro);
                    updateCaixaStmt.setString(2, micId);
                     if(updateCaixaStmt.executeUpdate() == 0) throw new SQLException("Falha ao atualizar o caixa do microempreendedor.");
                }

                conn.commit(); // Confirma todas as operações se nenhuma exceção foi lançada.

            } catch (SQLException e) {
                conn.rollback(); // Desfaz todas as operações em caso de erro.
                throw e; 
            }
        }
    }

    /**
     * Lista todas as vendas registradas com detalhes completos do produto e cliente.
     * 
     * @return Uma lista de objetos Venda.
     * @throws SQLException Se ocorrer um erro durante a consulta.
     */
    public List<Venda> listarVendas() throws SQLException {
        // Consulta complexa que junta 5 tabelas para montar um relatório detalhado.
        // Aliases (v, p, c, c_pes, e) são usados para tornar a consulta mais legível.
        String sql = "SELECT v.venda_id, v.data, v.quantidade, v.lucro, " +
                     "p.item_id, p.nome AS produto_nome, p.preco_unitario, p.preco_custo, " +
                     "c_pes.cnpj_cpf, c_pes.nome AS cliente_nome, c_pes.email, c.telefone, " +
                     "e.rua, e.bairro, e.cidade, e.numero, e.complemento " +
                     "FROM venda v " +
                     "JOIN produto p ON v.item_id = p.item_id " +
                     "JOIN cliente c ON v.cliente_id = c.pessoa_id " +
                     "JOIN pessoa c_pes ON c.pessoa_id = c_pes.pessoa_id " +
                     "JOIN endereco e ON c_pes.endereco_id = e.endereco_id";

        List<Venda> vendas = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Venda venda = new Venda();
                venda.setId(rs.getInt("venda_id"));
                venda.setDataVenda(rs.getObject("data", LocalDate.class));
                venda.setQuantidade(rs.getInt("quantidade"));
                venda.setValorTotal(rs.getBigDecimal("lucro"));

                Produto produto = new Produto();
                produto.setProdutoId(rs.getInt("item_id"));
                produto.setNome(rs.getString("produto_nome"));
                produto.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));

                Endereco endereco = new Endereco(rs.getString("rua"), rs.getString("bairro"), rs.getString("cidade"), rs.getString("numero"), rs.getString("complemento"));
                
                Cliente cliente = new Cliente();
                cliente.setIdentificador(rs.getString("cnpj_cpf"));
                cliente.setNome(rs.getString("cliente_nome"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEndereco(endereco);

                venda.setProduto(produto);
                venda.setCliente(cliente);
                vendas.add(venda);
            }
        }
        return vendas;
    }

    /**
     * Calcula o lucro total para um determinado mês e ano.
     * 
     * @param mes O mês (1-12).
     * @param ano O ano (ex: 2024).
     * @return O valor do lucro total como BigDecimal. Retorna BigDecimal.ZERO se não houver lucro.
     * @throws SQLException Se ocorrer um erro na consulta.
     */
    public BigDecimal obterLucroMensal(int mes, int ano) throws SQLException {
        // Usa a função MONTH() e YEAR() do SQL para filtrar as vendas pelo período desejado.
        // SUM(lucro) é uma função de agregação que soma os valores da coluna.
        String sql = "SELECT SUM(lucro) AS total_lucro FROM venda WHERE MONTH(data) = ? AND YEAR(data) = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes);
            pstmt.setInt(2, ano);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal lucro = rs.getBigDecimal("total_lucro");
                    // Boa prática: SUM() retorna NULL se não houver linhas para somar.
                    // Este tratamento evita um NullPointerException.
                    return lucro == null ? BigDecimal.ZERO : lucro;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Encontra o nome do produto mais vendido em um determinado mês e ano.
     * 
     * @param mes O mês (1-12).
     * @param ano O ano (ex: 2024).
     * @return O nome do produto mais vendido ou uma mensagem padrão se não houver vendas.
     * @throws SQLException Se ocorrer um erro na consulta.
     */
    public String obterProdutoMaisVendido(int mes, int ano) throws SQLException {
        // Agrupa as vendas por nome de produto (GROUP BY) e soma as quantidades vendidas (SUM).
        // Ordena em ordem decrescente (ORDER BY ... DESC) e pega apenas o primeiro (LIMIT 1).
        String sql = "SELECT p.nome, SUM(v.quantidade) AS total_quantidade " +
                     "FROM venda v JOIN produto p ON v.item_id = p.item_id " +
                     "WHERE MONTH(v.data) = ? AND YEAR(v.data) = ? " +
                     "GROUP BY p.nome ORDER BY total_quantidade DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes);
            pstmt.setInt(2, ano);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        }
        return "Nenhum produto vendido";
    }

    /**
     * Lista todos os produtos vendidos em um determinado mês e ano para o relatório.
     * 
     * @param mes O mês (1-12).
     * @param ano O ano (ex: 2024).
     * @return Uma lista de objetos Venda contendo os dados relevantes para o relatório.
     * @throws SQLException Se ocorrer um erro na consulta.
     */
    public List<Venda> listarProdutosVendidosMes(int mes, int ano) throws SQLException {
        String sql = "SELECT v.venda_id, v.data, v.quantidade, v.lucro, " +
                     "p.item_id, p.nome AS produto_nome, p.preco_unitario, p.preco_custo, " +
                     "c_pes.cnpj_cpf " +
                     "FROM venda v " +
                     "JOIN produto p ON v.item_id = p.item_id " +
                     "JOIN pessoa c_pes ON v.cliente_id = c_pes.pessoa_id " +
                     "WHERE MONTH(v.data) = ? AND YEAR(v.data) = ?";
        
        List<Venda> vendas = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes);
            pstmt.setInt(2, ano);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Venda venda = new Venda();
                    venda.setId(rs.getInt("venda_id"));
                    venda.setDataVenda(rs.getObject("data", LocalDate.class));
                    venda.setQuantidade(rs.getInt("quantidade"));
                    venda.setValorTotal(rs.getBigDecimal("lucro"));

                    Produto produto = new Produto();
                    produto.setProdutoId(rs.getInt("item_id"));
                    produto.setNome(rs.getString("produto_nome"));
                    produto.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                    produto.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    
                    Cliente cliente = new Cliente();
                    cliente.setIdentificador(rs.getString("cnpj_cpf"));
                    
                    venda.setProduto(produto);
                    venda.setCliente(cliente);
                    vendas.add(venda);
                }
            }
        }
        return vendas;
    }
}