# Trabalho 6: Testes de Desempenho - Arquitetura de Microsserviços com Múltiplos Protocolos

**Disciplina:** Computação Distribuída  
**Professor:** Nabor Mendonça  
**Equipe:**
* Guadalupe Prado - 2310300

---

## Objetivo do Projeto
Este projeto tem como objetivo avaliar o comportamento e o desempenho de uma arquitetura de microsserviços implementando múltiplos protocolos de comunicação (REST, GraphQL, SOAP e gRPC). Realizamos testes de carga variando a quantidade de usuários simultâneos para comparar o desempenho de cada protocolo em diferentes cenários.

## Ferramentas Utilizadas
* **FastAPI (Python):** Framework moderno para construção de APIs REST assíncronas e integração de múltiplos protocolos.
* **Strawberry GraphQL:** Biblioteca Python para implementação de APIs GraphQL com type hints.
* **Spyne:** Framework SOAP para construção de Web Services SOAP em Python.
* **gRPC:** Framework de comunicação de alto desempenho baseado em Protocol Buffers.
* **Locust:** Ferramenta de teste de carga baseada em Python para simular usuários virtuais (VUs).
* **Python (Pandas & Matplotlib):** Utilizados para extração e plotagem dos dados comparativos a partir dos relatórios CSV gerados pelo Locust.
* **Java/Spring Boot (Maven):** Implementação alternativa da arquitetura para comparação de desempenho com Python.

---

## Metodologia e Configuração dos Testes

### Ferramenta de Teste de Carga e Comportamento do Usuário
Foi utilizado o **Locust**, uma ferramenta de teste de carga baseada em Python, para simular o comportamento de usuários reais. O script de configuração ([**`locustfile.py`**](./locustfile.py)) define exatamente como cada usuário interage com a aplicação.

---

## Cenários de Teste Avaliados e Gráficos

Os testes foram estruturados em 3 cenários principais, variando a carga de usuários simultâneos: **100, 300 e 600 usuários**.

### 1. Teste com 100 Usuários Simultâneos

**Comparação de Latência P95 (em ms) - Python**
![Comparação P95 - 100 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_100_usuarios.png)

**Comparação de Latência P95 (em ms) - Java**
![Comparação P95 - 100 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_100_usuarios.png)

### 2. Teste com 300 Usuários Simultâneos

**Comparação de Latência P95 (em ms) - Python**
![Comparação P95 - 300 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_300_usuarios.png)

**Comparação de Latência P95 (em ms) - Java**
![Comparação P95 - 300 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_300_usuarios.png)

### 3. Teste com 600 Usuários Simultâneos

**Comparação de Latência P95 (em ms) - Python**
![Comparação P95 - 600 Usuários - Python](./graficos_desempenho/python%20graficos/P95_python_600_usuarios.png)

**Comparação de Latência P95 (em ms) - Java**
![Comparação P95 - 600 Usuários - Java](./graficos_desempenho/java%20graficos/P95_java_600_usuarios.png)

### Comparativo Geral - Todos os Protocolos

**Comparação P95 - 100 Usuários**
![Comparação Geral P95 - 100 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_100_usuarios.png)

**Comparação P95 - 300 Usuários**
![Comparação Geral P95 - 300 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_300_usuarios.png)

**Comparação P95 - 600 Usuários**
![Comparação Geral P95 - 600 Usuários](./graficos_desempenho/graficos%20geral/Comparacao_P95_600_usuarios.png)

---

## Implementações de API

### 1. Servidor Python (FastAPI + GraphQL + SOAP + gRPC)
**Arquivo:** [**`python_server.py`**](./python_server.py)

Este servidor implementa os 4 protocolos em uma única aplicação:
- **REST:** Via FastAPI
- **GraphQL:** Via Strawberry GraphQL
- **SOAP:** Via Spyne
- **gRPC:** Via grpcio com Protocol Buffers

**Execução:**
```bash
python python_server.py
# Servidor rodará em:
# - REST/GraphQL/SOAP: http://localhost:8080
# - gRPC: localhost:9090
```
