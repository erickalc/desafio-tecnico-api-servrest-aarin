# Testes de API ServeRest - Desafio Aarin

Projeto de automação de testes da API [ServeRest](https://serverest.dev), com Java, RestAssured, JUnit 5 e relatórios Allure.

Os testes verificam cadastro de usuários, autenticação, cadastro de produtos e fluxos de carrinho, incluindo baixa e reposição de estoque.

## Tecnologias

| Tecnologia | Versão           | Finalidade                               |
| --- |------------------|------------------------------------------|
| Java | Source/target 15 | Linguagem dos testes                     |
| Maven | 3.x              | Dependências e execução                  |
| JUnit Jupiter | 5.10.2           | Organização e ciclo de vida dos testes   |
| RestAssured | 5.4.0            | Requisições HTTP e validações            |
| Java Faker | 1.0.2            | Geração de dados de teste                |
| Allure | 2.27.0           | Relatórios, etapas e anexos              |
| AspectJ Weaver | 1.9.22.1         | Captura dos métodos anotados com `@Step` |

## Pre-requisitos

- JDK - recomenda-se JDK 17.
- Maven instalado e disponível no terminal, com `JAVA_HOME` apontando para o JDK.
- Acesso aos repositórios Maven e a API pública `https://serverest.dev`.

## Cenários implementados 

| Classe | Cenários | Cenários mais críticos e justificativa |
| --- | --- | --- |
| `UsuarioTest` | Cadastro de usuário administrador, bloqueio de cadastro com email em branco e com formatos de email inválidos (parametrizado) | **Cadastro de usuário administrador (`BLOCKER`/`CRITICAL`):** é pré-condição para autenticação e para todas as operações administrativas e de compra da plataforma. |
| `LoginTest` | Login com sucesso, senha inválida e email em branco | **Login com sucesso e bloqueio de senha inválida (`BLOCKER`/`CRITICAL`):** a emissão do token Bearer viabiliza o acesso às rotas protegidas, enquanto o bloqueio de credenciais inválidas evita acesso não autorizado. |
| `ProdutosTest` | Cadastro de produto com token de administrador, bloqueio para usuário comum, bloqueio sem token de autenticação e bloqueio de produto com nome duplicado | **Cadastro com admin e bloqueio para usuário comum/sem token (`BLOCKER`):** garante a disponibilidade do catálogo para vendas e impede falha grave de controle de acesso (escalação de privilégio) e duplicidade de itens. |
| `CarrinhoTest` | Conclusão de compra, consulta de carrinho por query param (`_id` e `idUsuario`), cancelamento com reposição de estoque, bloqueio de segundo carrinho e quantidade superior ao estoque | **Fluxo de compra, cancelamento e validação de estoque (`BLOCKER`/`CRITICAL`):** representa o *core business* (geração de receita) e protege a integridade do inventário contra venda sem estoque (*overselling*), baixa duplicada ou perda de itens no cancelamento. |

`OBS: A classificação em BLOCKER/CRITICAL e as demais anotações seguem a convenção do Allure, com base no impacto do cenário no fluxo de negócio.`

### Riscos identificados durante o mapeamento

| Risco | Impacto no fluxo                                                                                                                                                                             |
| --- |--------------------------------------------------------------------------------------------------------------------------------------------                                                    |
| Acesso indevido a operações administrativas | Um usuário comum conseguir cadastrar, alterar ou excluir produtos sem a permissão necessária.                                                          |
| Estoque insuficiente não validado | Permitir um carrinho com quantidade superior à disponível pode gerar venda sem estoque.                                                                          |
| Baixa incorreta de estoque | A criação do carrinho pode descontar uma quantidade diferente da solicitada ou deixar de realizar a baixa.                                                              |
| Desconto duplicado na conclusão | Como a reserva ocorre na criação do carrinho, concluir a compra não deve descontar o estoque novamente.                                                            |
| Reposição incorreta no cancelamento | Cancelar uma compra pode não devolver os itens reservados ou devolver uma quantidade superior à original.                                                      |
| Mais de um carrinho por usuário | Pode violar a regra de carrinho único e gerar reservas adicionais de estoque.                                                                                      |
| Alteração de estoque em requisições rejeitadas | Uma tentativa inválida, como criar um segundo carrinho, não deve modificar o estoque.                                                               |
| Carrinho com múltiplos produtos parcialmente processado | Se um item for inválido (inexistente, duplicação) ou não tiver estoque, existe o risco de reservar os demais sem criar um carrinho válido. |
| Referências inconsistentes entre recursos | A exclusão de usuário ou produto associado a um carrinho pode deixar registros inválidos se as restrições não forem respeitadas.                         |
| Repetição de conclusão ou cancelamento | Repetir a operação não deve provocar novas alterações indevidas no estoque.                                                                                 |
### Escopo testado

Foram priorizados os fluxos funcionais de maior impacto e suas
principais regras de negócio:

- Cadastro de usuário administrador e validação de e-mail obrigatório e formato válido.
- Login com sucesso, senha inválida e email em branco.
- Cadastro de produto com token de administrador.
- Bloqueio de cadastro de produto com usuário comum, sem token de autenticação e com nome duplicado.
- Criação e consulta de carrinho (por path param e por query param com `_id` e `idUsuario`).
- Baixa de estoque ao criar o carrinho.
- Conclusão da compra e manutenção do estoque já descontado.
- Cancelamento da compra com reposição do estoque.
- Rejeição de um segundo carrinho para o mesmo usuário.
- Rejeição de quantidade superior ao estoque disponível.

As validações incluem status HTTP e campos relevantes da resposta,
além da consulta ao produto para verificar os efeitos no estoque.

### Fora do escopo desta entrega (Critérios de Priorização)

A priorização seguiu a estratégia de **Testes Baseados em Risco**, concentrando o esforço nos fluxos que sustentam a operação comercial:

| Não testado | Justificativa de priorização                                                                                                                                                                                                                                                                                       |
| --- |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Todas as combinações de campos inválidos e valores-limite | **Menor impacto no negócio:** falhas de validação sintática de campos isolados têm impacto baixo comparadas a erros funcionais que impeçam a finalização da compra ou corrompam o estoque. Então as validações essenciais foram cobertas (como e-mail e credenciais) e priorizamos as regras de negócio de compra. |
| Matriz completa de autorização (tokens expirados e todas as rotas) | **Foco no vetor de maior risco:** priorizamos validar o controle de acesso onde há maior impacto direto no catálogo (`POST /produtos` sem token e com usuário comum) e no uso de token válido no fluxo de compra.                                                                                                  |
| CRUD completo de usuários e produtos (`PUT`, listagens gerais) | **Dependência do fluxo crítico (`E2E`):** a criação (`POST`), consulta (`GET`) e exclusão (`DELETE`) são suficientes para viabilizar e validar ponta a ponta a jornada de compra; operações de edição (`PUT`) têm criticidade secundária frente ao fluxo transacional de vendas.                                   |
| Carrinhos com múltiplos produtos | **Validação da regra central com menor complexidade:** o mecanismo de baixa, manutenção e reposição de estoque na criação/conclusão/cancelamento já é comprovado com 1 item por carrinho, entregando alta confiança sobre a regra de negócio principal com menor custo de manutenção de massa.                     |
| Concorrência, carga e desempenho | **Adequação ao ambiente e objetivo:** testes funcionais determinísticos agregam mais valor imediato na validação de contrato e regras de negócio, além de a API pública compartilhada (`serverest.dev`) não ser um ambiente controlado para aferição de performance.                          |

## Executar os testes

Executar toda a suíte com resultados novos:

```powershell
mvn clean test
```
Executar somente uma classe:

```powershell
mvn clean test "-Dtest=CarrinhoTest"
```
Os resultados e os dados do Allure em `target\allure-results`.

## Visualizar o relatório Allure

Depois de executar os testes:

```powershell
mvn allure:serve
```
O comando gera o relatório e inicia um servidor local para visualização no navegador. Use `Ctrl+C` para encerrar o servidor.

As anotações `@Epic`, `@Feature`, `@Story` e `@Severity` organizam o relatório.

## Exportar um HTML único para compartilhar

Com resultados de testes disponíveis, execute:

```powershell
mvn allure:report
.\.allure\allure-2.27.0\bin\allure.bat generate .\target\allure-results --single-file --clean -o .\target\allure-compartilhavel
```
O arquivo `target\allure-compartilhavel\index.html` pode ser aberto diretamente no navegador de outra máquina, sem Maven ou servidor local.

## Desafio de Investigação 

```
Cenário: “Às vezes, o cliente paga, mas o pedido não aparece na tela de ‘Meus Pedidos’.”

Pergunta: Qual seria sua primeira ação para investigar esse problema e por quê?
Detalhe sua hipótese inicial e como reduziria a incerteza rapidamente.
```

`` RESPOSTA: ``

Minha primeira ação seria conversar com meu time para deixar todos cientes do problema e verificarmos as possibilidades de investigação disponíveis. 

Verificar qual abordagem seria melhor para comunicar ao time de produtos e analisar os impactos financeiros dos clientes afetados.
Coletando junto a ele amostras de clientes para iniciar a investigação com os logs disponíveis.

A partir disso, com algumas amostras de evidências, levar ao time de BeckEnd para analisar os logs de transações e identificar se há algum padrão ou condição específica que leva à falha.

Dessa forma, seria mais eficiente pois teríamos uma visão mais clara do problema com dados de logs em mãos. Exemplos claros de clientes afetados e suas transações ajudariam a reduzir a incerteza rapidamente, permitindo que o time de BackEnd possa investigar e identificar a causa raiz do problema.
