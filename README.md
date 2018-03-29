# HBase - Coprocessor

De uma forma macro e resumida, podemos definir o coprocessor como um framework que provém um caminho fácil para executar seu código customizado. As analogias mais comumentes utilizadas para representar o coprocessor são "Trigger / Store Procedure" e AOP. 

O coprocessor pode ser desenvolvido como: observer ou endpoint. 
- Observer Coprocessor: é como uma "trigger" de banco de dados, ou seja, é acionado antes ou após um determinado evento. 
- Endpoint Coprocessor: é como uma "store procedure" de um banco de dados, sendo que esse tipo de coprocessor deve ser acionado explicitamente através do método 'CoprocessorService ()' da 'HTableInterface' (ou HTable).

Os coprocessadores não são projetados para serem utilizados pelos usuários finais, mas pelos desenvolvedores. Coprocessadores são executados diretamente no Region Server, portanto um código mal intencionado pode um derrubar seu Region Server, ou seja, cuidado ao implementá-lo.

Abaixo criarei um simples exemplo de Observer Coprocessor para mostrar exatamente o funcionamento do mesmo. 
Para iniciar iremos criar 2 tabelas no Hbase, conforme abaixo:

```sh
create 'tbl_001', {NAME=>'person'}, {NAME=>'page'}
create 'log_001', {NAME=>'person'}, {NAME=>'page'}
```

O propósito deste exemplo é capturar qualquer mudança realizada na tabela "tbl_001" e registrar a mudança na tabela "log_001", ou seja, se for inserido, atualizado ou removido um registro em "tbl_001", a mudança será registrada em "log_001", inserindo um novo registro com a "rowkey" igual a "rowkey | operation type | timestamp", ficando assim:

 `${put operation}
```sh
1006 | P | 1522088561272 
```
 `${delete operation}
```sh
1006 | D | 1522088561272
```
