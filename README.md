# Desafio Java

Aplicação Spring Boot (Standalone)

## Descrição

A solução aplicada foi a implementação de um Job baseado no Framework Spring Batch que tem como característica realizar tarefas de processamento com grande quantidade registros, fazendo leitura e escrita em arquivos ou banco de dados, com capacidade para recuperação de status, reinicialização, escalonamento e tratamento de exceções durante o ciclo de vida do Job, podendo ser considerada uma solução ideal para o cenário de grande quantidade de transações e processamentos, que é exigido de um sistema bancário. 

### Tecnologias/ferramentas utilizadas

* Java 8
* Spring Boot
* Spring Batch
* Maven
* MySql Database (Dados estatísticos da execução do Job) 
* Flyway (Migração dos dados/tabelas)
* Eclipse IDE