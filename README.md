

# Nexus Systems: Sistema de Gestão para Microempreendedores

![Status: Concluído](https://img.shields.io/badge/status-concluído-brightgreen)
![Linguagem](https://img.shields.io/badge/linguagem-Java_21-blue.svg)
![Banco de Dados](https://img.shields.io/badge/banco_de_dados-MySQL_8.0-orange.svg)
![UI](https://img.shields.io/badge/ui-Java_Swing-red.svg)

O **Nexus Systems** é um sistema de gestão de desktop robusto, desenvolvido em Java com a biblioteca Swing. Ele foi projetado para capacitar microempreendedores, oferecendo um conjunto de ferramentas centralizadas e intuitivas para o gerenciamento completo de suas operações de negócio. A aplicação utiliza um banco de dados relacional MySQL para garantir a integridade, a segurança e a organização das informações.

Construído sobre os pilares da **Programação Orientada a Objetos (POO)** e do padrão arquitetural **Data Access Object (DAO)**, o sistema se destaca pela modularidade, manutenibilidade e pela implementação de práticas de desenvolvimento seguras.

Este é um projeto de código aberto desenvolvido para fins de aprendizagem e prática. O objetivo principal foi aplicar e aprofundar conhecimentos em desenvolvimento de software com Java, modelagem de banco de dados com MySQL e implementação de padrões de arquitetura como o DAO.

## Índice

- [Visão Geral](#visão-geral)
- [Principais Funcionalidades](#principais-funcionalidades)
- [Arquitetura e Boas Práticas](#arquitetura-e-boas-práticas)
- [Tecnologias e Ferramentas](#tecnologias-e-ferramentas)
- [Como Executar o Projeto](#como-executar-o-projeto)
- [Requisitos do Sistema](#requisitos-do-sistema)
- [Melhorias Futuras](#melhorias-futuras)

## Visão Geral

O projeto visa solucionar os desafios diários de gestão enfrentados por pequenos negócios, oferecendo uma solução completa que abrange:

- **Controle Financeiro**: Acompanhamento do caixa, que é atualizado automaticamente com base no lucro de cada venda.
- **Gestão de Relacionamento com o Cliente (CRM)**: Cadastro e manutenção de uma base de clientes completa.
- **Controle de Estoque**: Gerenciamento de produtos, incluindo quantidade, preços e categorias.
- **Análise de Desempenho**: Geração de relatórios que fornecem insights valiosos para a tomada de decisões estratégicas.

A interface gráfica, desenvolvida com Java Swing, foi projetada para ser limpa e funcional, garantindo uma curva de aprendizado suave para o usuário final.

## Principais Funcionalidades

O sistema oferece uma gama completa de funcionalidades para a rotina administrativa:

- **Autenticação Segura**: Tela de login (`Login.java`) para proteger o acesso ao sistema e aos dados da empresa.
- **Gestão de Perfil do Usuário**:
  - Permite o cadastro de um **único microempreendedor**, garantindo que o sistema seja de uso exclusivo.
  - Exibição dos dados do perfil e do caixa total acumulado (`PerfilGUI.java`).
  - Funcionalidade para **resetar completamente o sistema** (`MicroempreendedorDAO.reset()`), uma operação crítica e segura para reiniciar a operação ou para um novo usuário.
- **Controle de Clientes (CRUD)**:
  - Cadastro, consulta, atualização e exclusão de clientes (`ClienteGUI.java`, `ClienteDAO.java`).
  - As operações de inserção e exclusão são **transacionais**, garantindo que os dados nas tabelas `pessoa` e `endereco` permaneçam consistentes.
- **Gerenciamento de Produtos (CRUD)**:
  - Controle total sobre o catálogo de produtos, incluindo nome, categoria, quantidade, preço de custo e preço de venda (`ProdutoGUI.java`, `ProdutoDAO.java`).
  - Regra de negócio que **impede a exclusão de um produto se ele já estiver associado a uma venda**, mantendo a integridade do histórico.
- **Registro de Vendas Transacional**:
  - Lançamento de novas vendas associadas a um cliente e a um produto (`VendaGUI.java`).
  - **Lógica transacional robusta** (`VendaDAO.inserir()`) que garante a atomicidade da operação:
    1.  **Validação de Estoque**: A venda é bloqueada se a quantidade solicitada for maior que a disponível.
    2.  **Atualização Atômica**: O estoque do produto é debitado e o caixa da empresa é creditado com o lucro da venda. Se qualquer uma dessas etapas falhar, a transação inteira é revertida (`rollback`).
- **Relatórios de Desempenho**:
  - Geração de **relatórios de vendas mensais** detalhados (`RelatorioVendas.java`).
  - Cálculo automático do **lucro total** do período selecionado.
  - Identificação do **produto mais vendido** no mês, auxiliando em estratégias de compra e marketing.

## Arquitetura e Boas Práticas

A qualidade do software foi uma prioridade, e para isso, foram aplicadas as seguintes práticas e padrões:

-   **Padrão DAO (Data Access Object)**: A lógica de negócio (`view`) está completamente desacoplada da lógica de persistência de dados (`dao`). Cada entidade do modelo (`Cliente`, `Produto`, etc.) possui uma classe DAO correspondente, responsável por todas as interações com o banco (CRUD). Isso torna o código mais organizado, testável e fácil de manter.

-   **Transações de Banco de Dados (ACID)**: Operações críticas que envolvem múltiplas tabelas, como `inserirCliente` e, principalmente, `inserirVenda`, são tratadas de forma transacional com `commit` e `rollback`. Isso garante a **atomicidade e a consistência dos dados**: se uma etapa da operação falhar (ex: falta de estoque), todas as alterações anteriores são desfeitas, evitando inconsistências no banco.

-   **ConnectionFactory**: Uma classe dedicada (`ConnectionFactory.java`) centraliza a criação de conexões com o banco de dados. As credenciais (URL, usuário, senha) são lidas de um arquivo externo `config.properties`, evitando que informações sensíveis fiquem expostas no código-fonte.

-   **Prevenção de SQL Injection**: Todas as consultas ao banco de dados são realizadas utilizando `PreparedStatement`. Esta prática de segurança essencial parametriza as queries, prevenindo ataques de injeção de SQL.

-   **Tratamento de Exceções Específico**: O sistema captura exceções SQL específicas (ex: `getErrorCode() == 1062` para `Duplicate entry`) para fornecer feedback claro e amigável ao usuário, como "O CPF/CNPJ informado já está cadastrado" ou "Não é possível excluir um cliente com vendas registradas".

-   **Modelo de Dados Relacional**: O esquema do banco (`Criação BD.txt`) foi projetado para ser normalizado, utilizando herança (tabela `pessoa` como base para `cliente` e `microempreendedor`) e chaves estrangeiras para garantir a integridade referencial. Uma *trigger* no banco de dados (`limit_one_microempreendedor`) impõe a regra de negócio de um único usuário.

## Tecnologias e Ferramentas

-   **Linguagem**: Java 21
-   **Banco de Dados**: MySQL 8.0
-   **Interface Gráfica**: Java Swing
-   **Driver de Conexão**: JDBC (Java Database Connectivity)
-   **IDE**: Apache NetBeans
-   **Ferramenta de BD**: MySQL Workbench
-   **Controle de Versão**: Git / GitHub 


## Como Executar o Projeto

Para configurar e executar o projeto localmente, siga os passos abaixo.

### Pré-requisitos

-   **Java Development Kit (JDK)** na versão 21 ou superior.
-   **Servidor de Banco de Dados MySQL** 8.0 instalado e em execução.
-   Uma **IDE Java** compatível com projetos NetBeans (ex: Apache NetBeans, IntelliJ IDEA).
-   A ferramenta **MySQL Workbench** ou similar para gerenciar o banco de dados.

### Passos para Instalação

1.  **Clone o Repositório**
    ```bash
    git clone https://github.com/Mateus-Bernardi/NexusSystems-project.git
    ```

2.  **Crie e Configure o Banco de Dados**
    -   Abra o MySQL Workbench e conecte-se ao seu servidor de banco de dados.
    -   Execute o script SQL contido no arquivo `Criação BD.sql` para criar o banco de dados `ProjetoBD` e todo o esquema de tabelas, relacionamentos e o `TRIGGER` de usuário único.
    -   (Opcional) Execute o script `Popular BD.sql` para popular o banco com dados de exemplo e testar as funcionalidades imediatamente.

3.  **Configure a Conexão com o Banco**
    -   Dentro do projeto, navegue até nexusproject\src\main\resources e modifique o arquivo de propriedades chamado `config.properties`.
    -   Substitua os dados atuais pelos configurados na sua máquina.

4.  **Execute a Aplicação**
    -   Abra o projeto na sua IDE.
    -   Certifique-se de que o driver JDBC para MySQL está adicionado às bibliotecas do projeto (o NetBeans geralmente gerencia isso).
    -   Localize a classe `Login.java` no pacote `view`.
    -   Execute o método `main` desta classe para iniciar o sistema.
    -   Se você populou o banco de dados, pode usar o login `mateus.m` e a senha `senha123`. Caso contrário, cadastre um novo microempreendedor.


## Requisitos do Sistema

### Requisitos Funcionais (RF)

| ID | Descrição | Status |
| :--- | :--- | :--- |
| RF01 | O sistema deve permitir o cadastro de um **único usuário** (Microempreendedor). | Concluído |
| RF02 | O sistema deve permitir o gerenciamento completo (CRUD) de **produtos**. | Concluído |
| RF03 | O sistema deve permitir o gerenciamento completo (CRUD) de **clientes**. | Concluído |
| RF04 | O sistema deve permitir o registro de **vendas**, com atualização automática do estoque e caixa. | Concluído |
| RF05 | O sistema deve gerar um **relatório de vendas mensal**, com lucro total e produto mais vendido. | Concluído |
| RF06 | O sistema deve ter uma tela de **login** para autenticar o usuário. | Concluído |
| RF07 | O sistema deve permitir a **remoção completa dos dados (reset)**. | Concluído |

### Requisitos Não Funcionais (RNF)

| ID | Descrição | Status |
| :--- | :--- | :--- |
| RNF01| A aplicação é para ambiente **desktop** e desenvolvida em Java. | Concluído |
| RNF02| As senhas são armazenadas em texto plano (ponto de melhoria). | Concluído |
| RNF03| As credenciais do banco de dados são externalizadas em `config.properties`. | Concluído |
| RNF04| As operações críticas de banco de dados são **transacionais (ACID)**. | Concluído |
| RNF05| A interface do sistema deve ser intuitiva e de fácil utilização. | Concluído |


## Melhorias Futuras

-   **Hashing de Senhas**: Implementar um algoritmo de hashing (como o **bcrypt**) para armazenar as senhas de forma segura.
-   **Geração de Gráficos**: Aprimorar a tela de relatórios com gráficos visuais (ex: lucro ao longo do tempo, pizza de categorias de produtos) usando bibliotecas como JFreeChart.
-   **Exportação de Relatórios**: Adicionar a funcionalidade de exportar relatórios para formatos como PDF ou CSV.
-   **Testes Unitários**: Criar testes unitários (com JUnit 5) para os métodos dos DAOs e para as regras de negócio.
-   **Refatoração da UI**: Considerar a migração da interface para **JavaFX**, que oferece um design mais moderno e maior flexibilidade.
-   **Logs**: Implementar um sistema de logs (com Log4j ou SLF4J) para registrar eventos importantes e erros.


