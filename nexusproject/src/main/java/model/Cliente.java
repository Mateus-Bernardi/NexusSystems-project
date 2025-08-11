package model;

public class Cliente extends Pessoa{
    
    private String telefone;

    public Cliente() {
    }
    
    public Cliente(String identificador, String nome, String email, Endereco endereco, String telefone) {
        super(identificador, nome, email, endereco);
        this.telefone = telefone;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "identificador='" + getIdentificador() + '\'' +
                ", nome='" + getNome() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", endereco=" + getEndereco() +
                ", telefone='" + telefone + '\'' +
                '}';
    }
    
    
    
  
}
