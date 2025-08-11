CREATE DATABASE ProjetoBD;
USE ProjetoBD;

-- Tabela endereco
CREATE TABLE endereco (
    endereco_id INT AUTO_INCREMENT PRIMARY KEY,
    rua VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    numero VARCHAR(50),
    complemento VARCHAR(255)
);

-- Tabela pessoa
CREATE TABLE pessoa (
    pessoa_id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255),
    email VARCHAR(255),
    cnpj_cpf VARCHAR(255) UNIQUE,
    endereco_id INT,
    FOREIGN KEY (endereco_id) REFERENCES endereco(endereco_id)
);

-- Tabela microempreendedor
CREATE TABLE microempreendedor (
    PRIMARY KEY (pessoa_id),
    pessoa_id INT,
    senha VARCHAR(255),
    caixa FLOAT,
    login VARCHAR(255),
    FOREIGN KEY (pessoa_id) REFERENCES pessoa(pessoa_id)
);

DELIMITER //
CREATE TRIGGER limit_one_microempreendedor
BEFORE INSERT ON microempreendedor
FOR EACH ROW
BEGIN
  DECLARE num_rows INT;
  SELECT COUNT(*) INTO num_rows FROM microempreendedor;
  IF num_rows >= 1 THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'A tabela microempreendedor ja contem um registro.';
  END IF;
END;
//
DELIMITER ;

-- Tabela cliente
CREATE TABLE cliente (
    pessoa_id INT,
    telefone VARCHAR(255),
    PRIMARY KEY (pessoa_id),
    FOREIGN KEY (pessoa_id) REFERENCES pessoa(pessoa_id)
);

-- Tabela produto
CREATE TABLE produto (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255),
    preco_unitario FLOAT,
    quantidade INT,
    categoria VARCHAR(255),
    preco_custo FLOAT
);
-- Tabela venda
CREATE TABLE venda (
    venda_id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    item_id INT,
    data DATE,
    quantidade INT,
    lucro FLOAT,
    FOREIGN KEY (cliente_id) REFERENCES cliente(pessoa_id),
    FOREIGN KEY (item_id) REFERENCES produto(item_id)
);