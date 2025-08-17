# Awards API

Esta é uma API RESTful desenvolvida em Java com Spring Boot que fornece dados sobre os indicados e vencedores da categoria "Pior Filme" do Golden Raspberry Awards.

A aplicação lê um arquivo CSV na inicialização, persiste os dados em um banco em memória (H2) e expõe endpoints para consultar os filmes e obter informações sobre os produtores premiados.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA**
- **Lombok**
- **H2 Database (In-Memory)**
- **OpenCSV**
- **Maven**
- **SpringDoc OpenAPI (Swagger)**

## Pré-requisitos

- **JDK 17** ou superior
- **Apache Maven 3.8** ou superior

## Como Executar a Aplicação

1.  **Clone o repositório:**
    ```bash
    git clone <url-do-repositorio>
    cd awards-api
    ```
2. **Deploy da aplicação utilizando docker**
    
Na raiz do projeto:
    ```bash
    docker compose up
    ```
 
A aplicação estará disponível em `http://localhost:8080`.

## Documentação da API (Swagger)

A API possui uma documentação interativa gerada com Swagger (OpenAPI 3), que permite visualizar e testar todos os endpoints diretamente pelo navegador.

Após iniciar a aplicação, acesse os seguintes links:

-   **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

A interface do Swagger UI facilita o uso da API.

## Endpoints da API

Abaixo está um resumo dos endpoints disponíveis. Para uma documentação interativa e completa, com exemplos de requisição e resposta, **acesse o Swagger UI**.

| Método | Endpoint                               | Descrição                                                                                               |
| :----- | :------------------------------------- | :------------------------------------------------------------------------------------------------------ |
| `GET`    | `/movies`                              | Retorna uma lista paginada de todos os filmes. Parâmetros: `page` (nº da pág), `size` (tam. da pág).      |
| `GET`    | `/movies/{id}`                         | Retorna os detalhes de um filme específico pelo seu ID.                                                 |
| `POST`   | `/movies`                              | Cria um novo registro de filme. O corpo da requisição deve conter o JSON do filme.                      |
| `PUT`    | `/movies/{id}`                         | Atualiza completamente um filme existente.                                                              |
| `DELETE` | `/movies/{id}`                         | Deleta um filme pelo seu ID.                                                                            |
| `GET`    | `/awards/producers/intervals`          | Retorna os produtores com o maior e o menor intervalo entre dois prêmios consecutivos.                  |

### Exemplo de Resposta para `/awards/producers/intervals`

```json
{
    "min": [
        {
            "producer": "Joel Silver",
            "interval": 1,
            "previousWin": 1990,
            "followingWin": 1991
        }
    ],
    "max": [
        {
            "producer": "Matthew Vaughn",
            "interval": 13,
            "previousWin": 2002,
            "followingWin": 2015
        }
    ]
}
```

## Como Executar os Testes de Integração
Necessário maven.
Para rodar os testes de integração, execute o seguinte comando na raiz do projeto:

No Linux/macOS:
```bash
./mvnw test
```

No Windows:
```bash
mvnw.cmd test
```

Caso não tenha maven, é possível rodar os testes diretamente em uma IDE como o Intellij IDEA, onde o projeto foi desenvolvido.
Basta abrir o projeto, procurar a pasta: 
```
src/main/java/com/awards/integration
```
Clicar com o botão direito sobre a pasta e escolher a opção: 
```
Run 'Tests In Integration'
```
