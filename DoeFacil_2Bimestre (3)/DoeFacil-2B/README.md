# DoeFácil

**Unicesumar • Engenharia de Software • ESOFT4-M-A • Maringá/PR . 11/11**

Arthur Correia Ramos Fonseca 25240314-2, Igor kasikawa da Silva 25165089-2 e Paulo Fortunato 25142855-2.

## 1. Finalidade

O DoeFácil registra o recebimento e a distribuição de roupas e alimentos. Pelo terminal, o responsável consulta a origem de cada doação, os itens já distribuídos e o saldo disponível. O projeto se relaciona ao ODS 1 — Erradicação da Pobreza — ao apoiar a organização da distribuição de itens básicos.

## 2. Requisitos funcionais

| Código | Operação |
|---|---|
| RF01 | Cadastrar doadores com nome e telefone. |
| RF02 | Registrar a doação com doador, data e um ou mais itens. Para roupas, informar tamanho e condição; para alimentos, validade. |
| RF03 | Consultar doações, detalhes dos itens e quantidades disponíveis, inclusive após reiniciar a aplicação. |
| RF04 | Editar a doação e seus itens. A quantidade recebida não pode ficar abaixo da já distribuída, e o tipo do item não pode mudar após uma saída. |
| RF05 | Excluir uma doação e seus itens somente quando nenhum deles tiver sido distribuído. |
| RF06 | Registrar a data e a quantidade de cada distribuição, atualizando o saldo sem permitir saídas acima da quantidade disponível. |

## 3. Implementação

A aplicação utiliza Java 17, JDBC e MySQL 8.4. Cada `Doacao` contém um ou mais objetos do tipo `ItemDoacao`. As subclasses `Roupa` e `Alimento` sobrescrevem `descreverDetalhes()`; na consulta, o menu exibe os dados específicos de cada tipo. Para alimentos, o sistema também verifica a validade na data da distribuição.

O saldo de cada item é a quantidade recebida menos a soma das saídas. A verificação do saldo e o registro da saída ocorrem na mesma transação, com bloqueio da doação para evitar que duas distribuições simultâneas consumam as mesmas unidades.

O [código Java](src/main/java/doefacil/), o [esquema SQL](database/01_schema.sql) e o [planejamento e os diagramas](docs/) estão organizados em diretórios separados.

## 4. Execução

**Com Docker Compose.** Na pasta que contém `compose.yaml`, copie `.env.example` para `.env`, defina uma senha em `DB_PASSWORD` e mantenha `DB_NAME=doefacil`. Execute:

```sh
docker compose build app
docker compose up -d --wait db
docker compose run --rm app
```

O banco cria as tabelas na primeira inicialização de um volume vazio. A opção **0** encerra o menu. O comando `docker compose down` encerra os serviços e preserva o volume de dados.

**Com MySQL local (Windows/PowerShell).** Requisitos: JDK 17 ou 21, Maven 3.9 e MySQL 8.4. Em um banco vazio chamado `doefacil`, execute [01_schema.sql](database/01_schema.sql). Use uma conta com permissão de leitura e escrita nesse banco:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/doefacil'
$env:DB_USER = 'seu_usuario'
$env:DB_PASSWORD = 'sua_senha_local'
mvn package
java -cp "target/classes;target/dependency/*" doefacil.Main
```

## 5. Verificação

O teste de domínio verifica composição, polimorfismo, saldo e validade. Após `mvn package`, execute no PowerShell:

```powershell
java -cp "target/classes;target/test-classes" doefacil.TesteDominio
```

O teste de integração percorre os seis requisitos no MySQL, consulta os registros em novas conexões e verifica duas distribuições simultâneas do mesmo item. Para executá-lo em um banco isolado, use o Docker Compose com o arquivo `.env` configurado na seção 4:

```powershell
$env:DB_NAME = 'doefacil_teste'
docker compose -p doefacilteste build app
docker compose -p doefacilteste up -d --wait db
docker compose -p doefacilteste run --rm app java -cp "classes:test-classes:lib/*" doefacil.TesteMySql
docker compose -p doefacilteste down
Remove-Item Env:DB_NAME
```

## 6. Cronograma do primeiro bimestre e participação

Plano original :

| Período | Atividade | Responsável proposto |
|---|---|---|
| 19/10 á 23/10 | Conexão e cadastro de doadores (RF01) | Arthur |
| 26/10 á 30/10 | Recebimento, consulta e subtipos (RF02 e RF03) | Igor |
| 31/10 á 03/11 | Edição, exclusão e validações (RF04 e RF05) | Paulo |
| 08/11 á 11/11 | Distribuição (Paulo), validação do banco (Arthur), revisão do fluxo no terminal (Igor) e documentação conjunta | Todos |
