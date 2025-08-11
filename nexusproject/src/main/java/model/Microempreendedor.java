package model;

import java.math.BigDecimal;

public class Microempreendedor extends Pessoa{
    
    private String senha;
    private String login;
    private BigDecimal caixa;

    public Microempreendedor() {
    }

    public Microempreendedor(String senha, String login, BigDecimal caixa, String identificador, String nome, String email, Endereco endereco) {
        super(identificador, nome, email, endereco);
        this.senha = senha;
        this.login = login;
        this.caixa = caixa;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public BigDecimal getCaixa() {
        return caixa;
    }

    public void setCaixa(BigDecimal caixa) {
        this.caixa = caixa;
    }
    
    
    
}
