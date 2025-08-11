package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Venda {
    
    private int id;
    private LocalDate dataVenda;
    private int quantidade;
    private BigDecimal valorTotal;
    private Cliente cliente;
    private Produto produto;

    public Venda() {
    }

    public Venda(LocalDate dataVenda, int quantidade, Cliente cliente, Produto produto) {
        this.dataVenda = dataVenda;
        this.quantidade = quantidade;
        this.cliente = cliente;
        this.produto = produto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDate dataVenda) {
        this.dataVenda = dataVenda;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal calcularLucro() {
        BigDecimal precoUnitario = produto.getPrecoUnitario();
        BigDecimal precoCusto = produto.getPrecoCusto();
        BigDecimal lucroPorUnidade = precoUnitario.subtract(precoCusto);
        return lucroPorUnidade.multiply(new BigDecimal(this.quantidade));
    }
    
    @Override
    public String toString() {
        return "Venda{" +
                "id='" + getId()+ '\'' +
                ", data='" + getDataVenda()+ '\'' +
                ", quantidade='" + getQuantidade()+ '\'' +
                ", lucro=" + getValorTotal()+
                ", clienteID='" + getCliente() + '\'' +
                ", produto='" + getProduto() + '\'' +
                '}';
    }
    
}
