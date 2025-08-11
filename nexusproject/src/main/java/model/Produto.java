package model;

import java.math.BigDecimal;

public class Produto {
    
    private int produtoId;
    private String nome;
    private BigDecimal precoUnitario;
    private int quantidade;
    private String categoria;
    private BigDecimal precoCusto;

    public Produto() {
    }

    public Produto(String nome, BigDecimal precoUnitario, int quantidade, String categoria, BigDecimal precoCusto) {
        //this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
        this.categoria = categoria;
        this.precoCusto = precoCusto;
    }

    public Produto(int produtoId, String nome, BigDecimal precoUnitario, int quantidade, String categoria, BigDecimal precoCusto) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
        this.categoria = categoria;
        this.precoCusto = precoCusto;
    }
    
    

    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.produtoId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Produto other = (Produto) obj;
        return this.produtoId == other.produtoId;
    }

    @Override
    public String toString() {
        return "Produto{" + "produtoId=" + produtoId + ", nome=" + nome + ", precoUnitario=" + precoUnitario + ", quantidade=" + quantidade + ", categoria=" + categoria + ", precoCusto=" + precoCusto + '}';
    }
   
}
