# ⚙️ ERP Logístico e Financeiro - Back-end

Motor lógico e base de dados do sistema ERP projetado para centralizar, cruzar e analisar grandes volumes de dados logísticos, antes fragmentados em dezenas de planilhas de Excel.

## 🛠️ Tecnologias e Ferramentas
* **Java 17:** Linguagem base de alta performance.
* **Spring Boot:** Framework principal estruturando a aplicação em padrão RESTful.
* **Spring Data MongoDB:** Mapeamento de documentos para o banco de dados NoSQL, ideal para a flexibilidade de dados do sistema.
* **Apache POI:** Motor de leitura e gravação de arquivos Microsoft Excel (`.xlsx`), essencial para a ingestão dos dados.
* **Lombok:** Biblioteca para redução de boilerplate (código repetitivo) via anotações.

## 📂 Arquitetura e Estrutura de Pastas
O projeto segue a arquitetura multicamadas padrão do ecossistema Spring:
* `/controller`: Expõe os endpoints REST (APIs) para consumo do Front-end.
* `/service`: Contém toda a lógica de negócios, cálculos matemáticos, cruzamento de planilhas e regras de estoque.
* `/model`: Entidades que representam os documentos no MongoDB (Ex: PedidoCompra, ProdutoEstoque).
* `/repository`: Interfaces de comunicação direta e queries complexas para o banco de dados.
* `/config`: Configurações globais de segurança (CORS) e ambiente.
* `/exceptions`: Tratamento global de erros para devolver respostas JSON padronizadas ao Front-end.

## 🧠 Núcleo de Inteligência e Funcionalidades
* **Processamento de Planilhas Sem Schema Fixo:** O Back-end não exige que as colunas do Excel estejam sempre no mesmo lugar. O sistema lê o cabeçalho dinamicamente e mapeia colunas de custos e códigos de fornecedores, independentemente da ordem.
* **Análise de Estoque e Alertas:** Algoritmo que cruza o inventário físico atual com o histórico de vendas (Mês/Semana) para classificar o estoque em duas frentes de dor financeira:
    * **Capital Parado:** Produtos com zero vendas no mês (Dinheiro morto).
    * **Superestocados (Baixo Giro):** Itens com cobertura estimada para mais de meses de operação.
* **Gestão de Pedidos e Extratos:** Rastreio de status logístico e financeiro de PDCs, com um sistema de "Carteira de Crédito" que abate ou devolve valores de devoluções (RMA) no momento da finalização do pedido.
* **Motor "Não Vem":** Lógica de manipulação de strings que recebe listas despadronizadas do fornecedor, encontra a equivalência no banco de dados e processa o recálculo do carrinho de compras em milissegundos.

## 🧗 Desafios Superados
A transição das macros do Excel para o Java exigiu a construção de queries avançadas no MongoDB (Aggregation Framework e Regex) para conseguir fazer pesquisas flexíveis. Além disso, foi necessário criar um "Dicionário de Limpeza" no Back-end, capaz de varrer e purificar os nomes dos produtos vindos das planilhas matrizes, removendo ruídos como nomes de lojas e abreviações antes de cadastrá-los no banco.

---

## ⚖️ Licença e Direitos Autorais

**Copyright (c) 2026 Carlos Eduardo Ferreira Coelho. Todos os direitos reservados.**

Este software é propriedade intelectual exclusiva de Carlos Eduardo Ferreira Coelho. É estritamente **PROIBIDA** a cópia, reprodução, distribuição, engenharia reversa, modificação ou uso não autorizado, parcial ou integral, deste código-fonte e de suas lógicas de negócio, sob pena de responsabilização civil e criminal conforme a **Lei de Direitos Autorais (Lei Nº 9.610/98)** do Brasil.

**Contato para Aquisição e Licenciamento corporativo:**
📧 carloseduardof191@gmail.com