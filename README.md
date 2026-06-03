# Trabalho 6: Comparação de Tecnologias de Invocação de Serviços Remotos

**Disciplina:** Computação Distribuída  
**Professor:** Nabor Mendonça  
**Equipe:**
* Fernanda Ortega - 2310305
* Guadalupe Prado - 2310300
* Letícia Cunha - 2315055

---

## 1. Descrição do Trabalho e Arquitetura do Sistema

Este projeto tem como objetivo realizar uma análise comparativa aprofundada de desempenho e comportamento entre quatro das principais tecnologias de invocação de serviços remotos utilizadas na indústria: **REST**, **GraphQL**, **SOAP** e **gRPC**. 

A avaliação é feita a partir da implementação de uma mesma especificação de domínio em duas plataformas distintas: **Python** (FastAPI e ecossistema assíncrono) e **Java** (Spring Boot e ecossistema multithread da JVM).

### O Domínio: Serviço de Streaming de Músicas
O domínio simula a estrutura simplificada de uma plataforma de streaming de música, gerenciando três tipos de recursos interligados:
1. **Usuários (`Usuario`):** Contêm `id`, `nome` e `idade`.
2. **Músicas (`Musica`):** Contêm `id`, `nome` e `artista`.
3. **Playlists (`Playlist`):** Contêm `id`, `nome`, uma associação do tipo muitos-para-um (`ManyToOne`) com o `Usuario` dono, e uma relação de muitos-para-muitos (`ManyToMany`) com as músicas que a compõem.

O diagrama de classes simplificado do domínio é apresentado a seguir:

```
              +-------------------+             +------------------+
              |     Usuarios      |             |     Musicas      |
              +-------------------+             +------------------+
                        |                                 |
                        | 0..*                            | 0..*
                        v                                 v
              +-------------------+   1..1      +------------------+
              |      Usuario      |------------>|    Playlists     |
              | - id: Long        |             +------------------+
              | - nome: String    |                       |
              | - idade: Integer  |                       | 0..*
              +-------------------+                       v
                                                +------------------+
                                                |     Playlist     |
                                                | - id: Long       |
                                                | - nome: String   |
                                                | - musicas: Set   |
                                                +------------------+
                                                          | 0..*
                                                          |
                                                          v
                                                +------------------+
                                                |      Musica      |
                                                | - id: Long       |
                                                | - nome: String   |
                                                | - artista: String|
                                                +------------------+
```

### Operações Avaliadas
As APIs de ambos os servidores foram configuradas para expor as seguintes operações de consulta e gerenciamento:
* Listar todos os usuários, músicas ou playlists.
* Buscar um usuário, música ou playlist por seu identificador único (`id`).
* Listar as playlists de um determinado usuário.
* Listar as músicas de uma determinada playlist.
* Listar todas as playlists que contêm uma determinada música.

---

## 2. Ferramentas Utilizadas

O projeto emprega uma variedade de frameworks e bibliotecas modernas para implementar os servidores e realizar os testes de carga:

* **FastAPI (Python):** Framework web assíncrono de alto desempenho baseado em Python e ASGI, utilizado para expor as rotas REST e servir como host para as APIs GraphQL e SOAP.
* **Strawberry GraphQL (Python):** Biblioteca baseada em *type hints* do Python utilizada para declarar o esquema GraphQL e acoplá-lo diretamente às rotas do FastAPI.
* **Spyne (Python):** Framework SOAP legado para Python que permite a definição de Web Services com geração dinâmica de WSDL, integrado ao FastAPI por meio de um middleware WSGI-para-ASGI (`a2wsgi`).
* **gRPC & Protocol Buffers:** Framework de RPC de alto desempenho desenvolvido pelo Google. Os dados e serviços são descritos em arquivos `.proto` (`streaming.proto`), compilados para gerar stubs em Java e Python.
* **Java / Spring Boot (Maven):** Plataforma para a implementação alternativa de alto desempenho, configurada com o Spring Initializr.
* **Spring Web (Java):** Usado para implementar os endpoints REST clássicos.
* **Spring GraphQL (Java):** Integração nativa do Spring para processamento de consultas GraphQL baseadas em esquemas (`.graphqls`).
* **Spring Web Services (Java):** Módulo do Spring que facilita a criação de serviços SOAP orientados a contratos XML baseados em esquemas XSD (`streaming.xsd`).
* **gRPC Spring Boot Starter (Java):** Permite expor facilmente serviços gRPC no Spring Boot por meio da anotação `@GrpcService`, rodando sobre um servidor Netty de alto desempenho.
* **Banco de Dados In-Memory H2 (Java):** Banco de dados relacional em memória integrado ao Spring Boot, permitindo simular o acesso a dados estruturados realistas com JPA/Hibernate.
* **Spring Data JPA & Hibernate (Java):** Camada de persistência para mapear as entidades Java e realizar as consultas SQL no H2.
* **Locust:** Ferramenta de testes de desempenho baseada em Python, configurada para gerar concorrência massiva por meio de co-rotinas assíncronas (*greenlets*).
* **Python (Pandas, Matplotlib e Seaborn):** Bibliotecas utilizadas nos scripts de pós-processamento para consolidar os relatórios CSV gerados pelo Locust e gerar gráficos de latência.

---

## 3. Configuração dos Servidores e APIs

Para garantir que a comparação fosse justa, ambos os servidores foram populados com a **mesma semente aleatória (`seed = 42`)** e a mesma quantidade de registros no banco de dados / memória:
* **500 Usuários** cadastrados.
* **1000 Músicas** cadastradas.
* **Playlists aleatórias** (entre 1 e 3 por usuário, contendo de 5 a 15 músicas aleatórias cada).

### A. Servidor Python (FastAPI + GraphQL + SOAP + gRPC)
O servidor está centralizado no arquivo [**`python_server.py`**](./python_server.py). 

1. **Dados em Memória:** Os dados mockados são instanciados em listas de dicionários Python diretamente na memória global do processo.
2. **REST (FastAPI):** Exposto por meio de decoradores clássicos `@app.get("/api/...")`. Como as funções de rota são declaradas com `def` (síncronas), o FastAPI delega a execução para uma pool de threads síncronas (`anyio`).
3. **GraphQL (Strawberry):** O esquema é gerado programmaticamente usando classes decoradas com `@strawberry.type`. O roteador do Strawberry é acoplado ao FastAPI em `/graphql`.
4. **SOAP (Spyne):** Definido a partir da classe `StreamingSoapService` herdando de `ServiceBase`. A aplicação Spyne é empacotada em uma aplicação WSGI e montada na rota `/ws` usando o wrapper `WSGIMiddleware`.
5. **gRPC (grpcio):** O serviço implementa a classe `StreamingGrpcServicer` derivada de `streaming_pb2_grpc.StreamingGrpcServicer`. Na inicialização do FastAPI (`@app.on_event("startup")`), o servidor gRPC assíncrono é disparado em uma tarefa separada (`asyncio.create_task`) na porta `9090`.

### B. Servidor Java (Spring Boot)
A implementação Java está contida em [**`pom.xml`**](./pom.xml) e no pacote `com.example.trabalho6`.

1. **Persistência Real (H2 & JPA):** Ao contrário do Python que utiliza dicionários puros na memória, a aplicação Java mapeia as tabelas relacionais em memória usando JPA/Hibernate.
2. **Carga de Dados (DataLoader):** O arquivo [**`DataLoader.java`**](./src/main/java/com/example/trabalho6/config/DataLoader.java) implementa `CommandLineRunner`. No momento em que o Spring Boot inicializa, ele gera a mesma base aleatória de dados e insere tudo no H2 usando Repositories (`saveAll`).
3. **REST (Spring Web):** Controlado pelo [**`StreamingRestController.java`**](./src/main/java/com/example/trabalho6/controller/StreamingRestController.java), que injeta o `StreamingService` e expõe endpoints no caminho `/api/*` rodando na porta padrão 8080 (Embedded Tomcat).
4. **GraphQL (Spring GraphQL):** Implementado no [**`StreamingGraphQLController.java`**](./src/main/java/com/example/trabalho6/controller/StreamingGraphQLController.java) mapeando queries via `@QueryMapping`. O esquema é declarado no arquivo de recursos [**`schema.graphqls`**](./src/main/resources/graphql/schema.graphqls).
5. **SOAP (Spring WS):** Configurado em [**`WebServiceConfig.java`**](./src/main/java/com/example/trabalho6/config/WebServiceConfig.java) e exposto no [**`StreamingSoapEndpoint.java`**](./src/main/java/com/example/trabalho6/endpoint/StreamingSoapEndpoint.java). Utiliza o esquema XML declarado em [**`streaming.xsd`**](./src/main/resources/xsd/streaming.xsd), gerando as classes de envelope SOAP automaticamente via plugin JAXB.
6. **gRPC (grpc-server-spring-boot-starter):** Controlado pelo [**`StreamingGrpcController.java`**](./src/main/java/com/example/trabalho6/controller/StreamingGrpcController.java) anotado com `@GrpcService`. Ele roda um servidor Netty escutando por padrão na porta `9090`.

---

## 4. Teste de Carga e Comportamento do Usuário

Os testes de carga foram desenhados para avaliar a capacidade máxima de vazão (*throughput*) e a latência de cada protocolo sob estresse concorrente.

### Ferramenta de Teste: Locust
O arquivo [**`locustfile.py`**](./locustfile.py) simula o comportamento dos usuários virtuais. Ele envia requisições simultâneas e mede as latências de resposta.

### Comportamento do Usuário Simulado
A classe `ApiLoadTestUser` define o seguinte comportamento:
* **Tempo de Espera (*Pacing*):** Após completar uma requisição, cada usuário aguarda um intervalo de tempo aleatório entre **1 e 2 segundos** (`wait_time = between(1, 2)`) antes de disparar a próxima tarefa. Isso simula o comportamento real de humanos interagindo com um aplicativo.
* **Distribuição de Tarefas:** Cada usuário realiza chamadas para as quatro tecnologias em formato round-robin/aleatório com pesos iguais (anotações `@task` de mesmo peso):
  1. **REST:** Faz um `GET` no endpoint `/api/usuarios`.
  2. **GraphQL:** Faz um `POST` no endpoint `/graphql` solicitando a lista de usuários: `query { buscarTodosUsuarios { id nome } }`.
  3. **SOAP:** Faz um `POST` no endpoint `/ws/` passando o envelope XML da requisição `<BuscarTodosUsuariosRequest/>`.
  4. **gRPC:** Dispara o método RPC `BuscarTodosUsuarios` através de um stub gRPC persistente.
* **Carga de dados das requisições:** Em todos os casos, a chamada retorna a lista completa de **500 usuários**. Isso força os servidores a processarem uma quantidade não-trivial de dados e serializarem uma lista grande de objetos. O tamanho médio das cargas de resposta trafegadas é:
  * **SOAP:** ~54.4 KB (Devido à verbosidade das tags XML)
  * **REST:** ~22.7 KB (JSON bruto)
  * **GraphQL:** ~18.2 KB (JSON estruturado solicitando apenas `id` e `nome`)
  * **gRPC:** ~11.2 KB (Payload binário comprimido gerado pelos Protocol Buffers)

---

## 5. Resultados e Análise Gráfica

Abaixo estão dispostos os gráficos comparativos da latência P95 (o tempo em que 95% das requisições foram concluídas) nos cenários com **100, 300 e 600 usuários simultâneos**.

### Resumo dos Resultados de Latência (P95 em ms)

| Usuários | Protocolo | Servidor Python (P95) | Servidor Java (P95) | Fator de Diferença | Taxa de Falhas (Python) | Taxa de Falhas (Java) |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **100** | gRPC | 17 ms | 3 ms | ~5.6x mais rápido | 0% | 0% |
| | REST | 23 ms | 5 ms | ~4.6x mais rápido | 0% | 0% |
| | GraphQL | 26 ms | 8 ms | ~3.2x mais rápido | 0% | 0% |
| | SOAP | 55 ms | 13 ms | ~4.2x mais rápido | 0% | 0% |
| **300** | gRPC | 110 ms | 1 ms | 110x mais rápido | 0% | 0% |
| | REST | 250 ms | 3 ms | ~83x mais rápido | <0.1% | 0% |
| | GraphQL | 260 ms | 4 ms | 65x mais rápido | <0.1% | 0% |
| | SOAP | 280 ms | 7 ms | 40x mais rápido | <0.1% | 0% |
| **600** | gRPC | 140 ms | 1 ms | 140x mais rápido | 0% | 0% |
| | REST | 2500 ms | 2 ms | **1250x mais rápido** | **5.6%** | 0% |
| | GraphQL | 2500 ms | 3 ms | **833x mais rápido** | **5.4%** | 0% |
| | SOAP | 2600 ms | 6 ms | **433x mais rápido** | **4.7%** | 0% |

---

### Cenário 1: 100 Usuários Simultâneos

Com 100 usuários, a carga sobre o servidor ainda é moderada.

**Gráfico P95 - Python (100 Usuários):**  
![Comparação P95 - 100 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_100_usuarios.png)

**Gráfico P95 - Java (100 Usuários):**  
![Comparação P95 - 100 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_100_usuarios.png)

**Comparativo Geral (100 Usuários):**  
![Comparação Geral P95 - 100 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_100_usuarios.png)

*Análise:* O Java apresenta tempos excelentes de resposta (entre 3ms e 13ms P95). Em Python, os tempos já são superiores, variando de 17ms (gRPC) a 55ms (SOAP), mas mantendo estabilidade e 0% de erros.

---

### Cenário 2: 300 Usuários Simultâneos

Com 300 usuários concorrentes, os servidores começam a ser pressionados.

**Gráfico P95 - Python (300 Usuários):**  
![Comparação P95 - 300 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_300_usuarios.png)

**Gráfico P95 - Java (300 Usuários):**  
![Comparação P95 - 300 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_300_usuarios.png)

**Comparativo Geral (300 Usuários):**  
![Comparação Geral P95 - 300 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_300_usuarios.png)

*Análise:* O servidor Python demonstra os primeiros sinais de saturação de CPU. Suas latências saltam de cerca de 25ms para mais de 250ms em protocolos HTTP. O gRPC em Python se mantém em 110ms. No Java, ocorre o efeito inverso: devido ao aquecimento da JVM (*JIT compilation*), a latência P95 cai ou se mantém extremamente baixa (gRPC vai a 1ms, REST a 3ms, SOAP a 7ms).

---

### Cenário 3: 600 Usuários Simultâneos

Cenário de estresse extremo que evidencia as diferenças arquiteturais.

**Gráfico P95 - Python (600 Usuários):**  
![Comparação P95 - 600 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_600_usuarios.png)

**Gráfico P95 - Java (600 Usuários):**  
![Comparação P95 - 600 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_600_usuarios.png)

**Comparativo Geral (600 Usuários):**  
![Comparação Geral P95 - 600 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_600_usuarios.png)

*Análise:* Em Python, a concorrência de 600 usuários causa um colapso de desempenho nas APIs HTTP (REST, GraphQL e SOAP), cujas latências P95 atingem **2500ms - 2600ms**, acompanhadas por taxas de erro próximas de **5%** (erros de timeout e conexão). O gRPC em Python é o único que sobrevive de forma estável (P95 de 140ms e 0% de falhas). Em Java, os tempos permanecem assustadoramente baixos e estáveis: **gRPC (1ms), REST (2ms), GraphQL (3ms) e SOAP (6ms)**, sem nenhuma falha registrada.

---

## 6. Por que o Java se saiu melhor em todos os casos?

A superioridade do Java sobre o Python em todos os cenários avaliados não se deve apenas à velocidade de execução bruta das linguagens, mas sim a uma conjunção de fatores arquiteturais e de infraestrutura de rede de ambas as tecnologias sob concorrência massiva:

### 1. Modelo de Concorrência e Multithreading vs GIL
* **O gargalo do Python (GIL):** O interpretador padrão do Python (CPython) possui o **Global Interpreter Lock (GIL)**, que impede que múltiplas threads executem código Python em paralelo de forma real em múltiplos núcleos de CPU. Embora o FastAPI utilize rotas assíncronas e loops de eventos assíncronos (capazes de lidar com I/O de rede de forma eficiente), as operações de processamento e, principalmente, a **serialização de grandes objetos** (conversão de 500 registros para strings JSON ou XML) são tarefas intensivas para a CPU. Esse processamento de CPU bloqueia a thread única do loop de eventos. À medida que o Locust aumentava a carga (300 e 600 usuários), as requisições se acumulavam na fila aguardando tempo de CPU, fazendo com que a latência P95 escalasse exponencialmente até gerar falhas de timeout.
* **A eficiência multithread do Java (Tomcat):** O Spring Boot roda sobre o servidor web embarcado Tomcat, que emprega um modelo multi-threaded nativo (*thread-per-request*). O Tomcat aloca uma pool dedicada (geralmente até 200 threads) que rodam **em paralelo real** distribuindo-se por todos os núcleos de CPU disponíveis no computador host. Sem barreiras como o GIL, a JVM processa a serialização de múltiplas requisições concorrentes de forma verdadeiramente concorrente.

### 2. Otimização de Código Dinâmica (Compilação JIT)
* O Python executa código interpretado de alto nível. Cada loop sobre a coleção de usuários e cada conversão de tipo precisa ser avaliada dinamicamente a cada requisição.
* A JVM (Java Virtual Machine) emprega a compilação **JIT (Just-In-Time)**. Quando o servidor inicializa, o código roda de forma interpretada. Porém, à medida que os primeiros testes com 100 usuários são disparados, a JVM detecta quais métodos são chamados repetidamente ("hotspots") e os compila dinamicamente para código de máquina nativo e altamente otimizado. É por isso que, curiosamente, os tempos do Java para 300 e 600 usuários foram **ainda melhores** do que o cenário inicial de 100 usuários.

### 3. Acesso a Dados: Banco de Dados H2 vs Busca Linear em Dicionários
* **Python:** O servidor Python realiza buscas em memória filtrando listas de dicionários lineares (`[p for p in FAKE_PLAYLISTS if p["usuario"]["id"] == usuario_id]`). Embora pareça rápido, essa busca linear em nível de interpretador consome muitos ciclos de CPU do único thread disponível sob alta concorrência.
* **Java:** Utiliza o banco de dados em memória relacional H2 indexado. Embora haja o overhead teórico de fazer consultas através de SQL, Hibernate e JPA, a indexação do H2 e o pool de conexões otimizados processam as buscas com custo de tempo irrisório, deixando a CPU livre para cuidar das conexões de rede de forma ultra-veloz.

### 4. Eficiência na Serialização (REST vs GraphQL vs SOAP vs gRPC)
A eficiência do protocolo impacta diretamente o uso de CPU e banda de rede de cada servidor:
* **gRPC (Protocol Buffers):** Utiliza codificação binária estruturada extremamente eficiente. A conversão de dados não envolve manipulação pesada de strings (como JSON ou XML) e o gRPC é executado no Python utilizando bibliotecas altamente otimizadas escritas em C/C++ (`grpcio`), explicando por que o gRPC em Python se saiu muito melhor do que REST/GraphQL/SOAP. Em Java, o gRPC roda sobre o Netty, uma biblioteca assíncrona de rede baseada em eventos altamente otimizada, alcançando latências P95 irrisórias de **1ms**.
* **GraphQL:** Adiciona um overhead de parseamento e validação de query dinamicamente em cada requisição. Em Python, isso agrava o gargalo da CPU.
* **SOAP:** Utiliza XML, que possui tags redundantes e verbosas (payload médio de 54 KB, quase 5 vezes maior que o gRPC). O processamento de parseamento e validação contra o XSD é uma tarefa de alta demanda de processamento de texto. Em Python (Spyne), esse processamento sobrecarregou drasticamente a thread principal. Em Java (Spring WS + JAXB), a compilação estática das classes XML e os parsers baseados em SAX nativos mitigaram totalmente esse custo.

---

## 7. Conclusões e Descobertas

O experimento demonstrou de forma prática e mensurável os limites arquiteturais de diferentes abordagens tecnológicas e linguagens sob estresse concorrente:

1. **A arquitetura da linguagem determina o limite de escala horizontal:** Para microsserviços sob alta concorrência concorrentes e intensivos em CPU/Serialização, o modelo multithread real da JVM (Java) supera dramaticamente o loop assíncrono de thread única do Python devido ao bloqueio da CPU por tarefas síncronas de serialização e restrições do GIL. O Python FastAPI brilha pela velocidade de desenvolvimento e excelente suporte I/O assíncrono, mas exige a execução de múltiplos processos (utilizando Gunicorn com múltiplos workers Uvicorn) para poder escalar paralelamente em máquinas multi-core.
2. **gRPC é a escolha definitiva para comunicação inter-serviços (East-West):** A serialização binária com Protocol Buffers e o transporte HTTP/2 provaram-se infinitamente superiores aos demais protocolos em termos de latência e consumo de banda de rede. Mesmo no saturado servidor Python com 600 usuários, o gRPC permaneceu estável (140ms P95, 0% falhas) enquanto os protocolos HTTP clássicos entraram em colapso completo (2500ms P95, ~5.5% falhas).
3. **SOAP carrega um alto custo legado:** Embora o SOAP tenha se saído surpreendentemente bem no Java devido a otimizações de baixo nível da biblioteca Spring WS, o consumo de recursos (banda 5x maior que gRPC) e a complexidade de processamento o tornam inviável para novos microsserviços escaláveis, limitando seu uso a integrações com sistemas legados corporativos.
4. **GraphQL vs REST:** O GraphQL oferece maior flexibilidade e reduz o tráfego de dados desnecessários (*overfetching*), diminuindo o tamanho dos payloads no tráfego de rede (18KB vs 22KB do REST). No entanto, o custo de parseamento dinâmico da query na camada de aplicação exige atenção, necessitando de caches de query parseada para evitar estresse desnecessário da CPU em cenários de alta concorrência.
