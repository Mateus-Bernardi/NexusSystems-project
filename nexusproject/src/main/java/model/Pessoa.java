package model;

public abstract class Pessoa {
    
    private String identificador;
    private String nome;
    private String email;
    private Endereco endereco;

    public Pessoa() {
    }

    public Pessoa(String identificador, String nome, String email, Endereco endereco) {
        this.identificador = identificador;
        this.nome = nome;
        this.email = email;
        this.endereco = endereco;
    }

    public Pessoa(String nome, String email, Endereco endereco) {
        this.nome = nome;
        this.email = email;
        this.endereco = endereco;
    }
    
    

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "Pessoa{" + "identificador=" + identificador + ", nome=" + nome + ", email=" + email + ", endereco=" + endereco + '}';
    }

    
}
