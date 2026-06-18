# DeckDealer Marketplace - Microservices (Entrega 2)

Este repositório contém a segunda entrega do trabalho prático da disciplina, contendo uma arquitetura de microserviços em Spring Boot para um marketplace de cartas colecionáveis.

## Integrantes
* **Leonardo Santos Silva** - RA: 22014589
* **Lucas Oliveira Souza** - RA: 22017643
* **Turma**: Segunda e Quarta - Noturno (Engenharia de Software)
* **Tipo**: Dupla

---

## Novidades da Entrega 2
Evoluímos o projeto original com três novos pilares fundamentais de arquiteturas distribuídas:
1. **Comunicação Assíncrona com Kafka**: Criação do tópico `trades.purchases` para envio assíncrono de eventos de compras finalizadas, recalculando de forma independente o preço médio das cartas no catálogo.
2. **Modelo Reativo (Spring WebFlux + MongoDB Reactive)**: O microserviço `card-service` foi completamente convertido para programação reativa assíncrona, usando streams reativos (`Mono`/`Flux`) e Netty para máxima concorrência e throughput nas buscas.
3. **Observabilidade e Rastreamento (Actuator + MDC + Correlation ID)**:
   * Métricas expostas via **Spring Boot Actuator** e formatadas para **Prometheus**.
   * Propagação automática de Correlation ID (`X-Correlation-ID`) a partir do API Gateway para rastreamento de chamadas HTTP síncronas e eventos assíncronos no Kafka.
   * Logs correlacionados configurados em padrão Sleuth/MDC em todos os módulos.

---

## Portas dos Serviços e Bancos de Dados

| Serviço | Responsabilidade | Porta | Banco de Dados | Tipo da Stack |
|---|---|---|---|---|
| `discovery-server` | Discovery Registry (Eureka) | `8761` | *N/A* | Servlet Blocking |
| `api-gateway` | Gateway Roteador e Injetor de Traces | `8080` | *N/A* | Reactive Gateway |
| `card-service` | Catálogo Geral de Cards | `8081` | MongoDB (`card_db`) | **Reativo (WebFlux / MongoDB)** |
| `trade-service` | Ofertas e Compras de Cards | `8082` | PostgreSQL (`trade_db`) | Servlet Blocking (MVC / JPA) |
| **Kafka Broker** | Barramento de Mensagens/Eventos | `9092` | *N/A* | Mensageria Distribuída |
| **Zookeeper** | Coordenação do Broker Kafka | `2181` | *N/A* | Serviços Compartilhados |

---

## Como Executar o Ecossistema

### 1. Iniciar a Infraestrutura (PostgreSQL, MongoDB, Kafka e Zookeeper)
Na raiz do projeto, execute o docker-compose para carregar todos os containers:
```bash
docker-compose up -d
```
*Isso carregará as 4 imagens necessárias isoladas na sua máquina local.*

### 2. Compilar Todos os Projetos
```bash
mvn clean package -DskipTests
```

### 3. Executar os Microserviços (em janelas separadas do terminal)
* **Discovery Server:**
  ```bash
  mvn -pl discovery-server spring-boot:run
  ```
* **API Gateway:**
  ```bash
  mvn -pl api-gateway spring-boot:run
  ```
* **Card Service (Catálogo Reativo):**
  ```bash
  mvn -pl card-service spring-boot:run
  ```
* **Trade Service (Negociações):**
  ```bash
  mvn -pl trade-service spring-boot:run
  ```

---

## Verificação de Observabilidade e Métricas

### 1. Métricas do Actuator no Formato Prometheus
Consulte as métricas expostas acessando as URLs abaixo:
* **card-service**: [http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus)
* **trade-service**: [http://localhost:8082/actuator/prometheus](http://localhost:8082/actuator/prometheus)

### 2. Verificação de logs com Correlation ID (Rastreamento)
Ao realizar chamadas HTTP no API Gateway (porta `8080`), observe a saída nos consoles do `trade-service` e do `card-service`. Todas as ações disparadas a partir de um clique conterão o mesmo `correlationId` nos colchetes do padrão de log:
`[trade-service,correlationId=XYZ-123-ABC...]`

---

## Roteiro de Testes Completo (Demonstração do Fluxo)

Use o `curl` ou Postman para testar o fluxo de ponta a ponta:

### Passo 1: Cadastrar Carta no Catálogo Reativo (`card-service`)
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Black Lotus",
    "game": "Magic: The Gathering",
    "expansion": "Alpha",
    "rarity": "Mythic Rare",
    "averagePrice": 25000.00,
    "attributes": {
      "manaCost": "0",
      "type": "Artifact"
    }
  }'
```
*Copie o id gerado pelo MongoDB retornado no JSON da resposta.*

### Passo 2: Criar um Anúncio no `trade-service`
Substitua `COLOQUE_O_ID_AQUI` pelo ID gerado no Passo 1. Note que o `trade-service` chamará o `card-service` via HTTP passando o Correlation ID para validar a carta.
```bash
curl -X POST http://localhost:8080/api/trades/listings \
  -H "Content-Type: application/json" \
  -d '{
    "cardId": "COLOQUE_O_ID_AQUI",
    "sellerName": "Leo Card Store",
    "price": 24000.00,
    "cardCondition": "Near Mint"
  }'
```

### Passo 3: Comprar a Carta Anunciada (Dispara Evento Kafka)
Realiza a compra da listagem ID `1`.
```bash
curl -X POST http://localhost:8080/api/trades/listings/1/buy
```

* **O que acontece por debaixo dos panos?**
  1. A compra muda o status da listagem no PostgreSQL para `SOLD`.
  2. O `trade-service` publica o evento `CompraRealizadaEvent` contendo o preço de venda (`24000.00`) e o Correlation ID no Kafka.
  3. O `card-service` consome o evento de forma assíncrona. Ele atualiza o preço médio do card no MongoDB (calculando `(25000.00 + 24000.00) / 2 = 24500.00`) de maneira reativa.
  4. O log nos dois consoles registra o processamento unificado com o mesmo Correlation ID gerado no início da transação!

### Passo 4: Verificar Preço Médio Atualizado
```bash
curl -X GET http://localhost:8080/api/cards
```
*Veja que o campo `averagePrice` do card agora reflete a nova média recalculada de forma assíncrona e eventual via Kafka!*
