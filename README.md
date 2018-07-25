[![Hbase](https://hbase.apache.org/images/hbase_logo_with_orca_large.png)](https://hbase.apache.org/)

# Coprocessor

De uma forma macro e resumida, podemos definir o coprocessor como um framework que provém um caminho fácil para executar seu código customizado dentro do HBase. As analogias mais comumentes utilizadas para representar o coprocessor são "Trigger / Store Procedure" e AOP. 

O coprocessor pode ser desenvolvido como:
- Observer Coprocessor: é como uma "trigger" de banco de dados, ou seja, é acionado antes ou após um determinado evento. 
- Endpoint Coprocessor: é como uma "store procedure" de um banco de dados, sendo que esse tipo de coprocessor deve ser acionado explicitamente através do método 'CoprocessorService ()' da 'HTableInterface' (ou HTable).

Os coprocessadores não são projetados para serem utilizados pelos usuários finais, mas pelos desenvolvedores. Coprocessadores são executados diretamente no Region Server, portanto um código mal intencionado pode derrubar seu Region Server, ou seja, cuidado ao implementá-lo.

Abaixo criarei um simples exemplo de Observer Coprocessor para mostrar exatamente o funcionamento do mesmo. O exemplo criado foi executado na VM da Cloudera e exige um pouco de conhecimento em Hadoop, Hbase e Java
Para iniciar iremos criar 2 tabelas no Hbase, conforme abaixo:

```sh
create 'tbl_001', {NAME=>'person'}, {NAME=>'page'}
create 'log_001', {NAME=>'person'}, {NAME=>'page'}
```

O propósito deste exemplo é capturar qualquer mudança realizada na tabela tbl_001 e registrar a mudança na tabela log_001, ou seja, se for inserido, atualizado ou removido um registro em tbl_001, a mudança será registrada em log_001, inserindo um novo registro com a "rowkey" igual a "rowkey | tipo de operação | timestamp", ficando assim:

```sh
--exemplo de como fica rowkey em uma operação put
1006 | P | 1522088561272 
--exemplo de como fica rowkey em uma operação delete
1006 | D | 1522088561272
```

Outro ponto interessante a observar, é que os valores armazenados na tabela log_001 na operação de update e delete, são os valores antes da alteração e quando realizado a operação de insert, então é armazenado o valor que está sendo inserido naquele momento.

Faça download do arquivo [tbl_001.csv](https://github.com/easofiati/HBase-coprocessor/blob/master/tbl_001.csv)

Agora vamos importar o arquivo tbl_001.csv para a tabela tbl_001 no HBase. 
1. Copie o arquivo tbl_001.csv para o HDFS. Caso não exista a pasta /tmp, então crie-a.
```sh
hdfs dfs -put tbl_001 /tmp
```

2. Importe o arquivo para o HBase, para isso utilizaremos da ferramenta [ImportTSV](https://github.com/easofiati/HBase-ingestao) do próprio HBase.
```sh
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dmapreduce.job.queuename=hdqueue -Dimporttsv.separator=',' -Dimporttsv.columns=HBASE_ROW_KEY,person:name,person:address,person:country,person:company,person:email,page:url,page:checked,page:valid tbl_001 hdfs:///tmp/tbl_001.csv
```
* Obs.: Uma outra alternativa para isso seria utilizar o PIG, o que envolveria uma outra ferramenta, mas para simplificar optei por me ater ao HBase.

Tendo agora a tabela tbl_001 do HBase populada, vamos implementar o coprocessor.
1. Escreva o coprocessor [HbaseCopro001.java](https://github.com/easofiati/HBase-coprocessor/blob/master/HBaseCopro001.java)
2. Exporte o código Java do coprocessor para um arquivo ".jar".
3. Copie o arquivo ".jar" gerado anteriormente para a pasta "/tmp" do HDFS. Um ponto de atenção é que o HBase deve ser capaz de localizar o ".jar".
4. Anexe o coprocessor a tabela tbl_001 do HBase. Estou exemplificando com o load dinâmico do coprocessor no HBase, o que julgo a maneira mais interessante, mas existe também o load estático, que não vou citar e pode ser pesquisado posteriormente nos links de referência no final deste.

```sh
--Load coprocessor
alter 'tbl_001', METHOD => 'table_att', 'coprocessor'=>'hdfs:///tmp/copro001.jar|com.eas.HBaseCopro001|1001|arg1=1,arg2=2'
--Unload coprocessor
alter 'tbl_001', METHOD => 'table_att_unset', NAME => 'coprocessor$1'
```

Pronto! O coprocessor está implementado, ou seja, agora você já pode testá-lo. Para testar você pode realizar novamente a ingestão de dados na tabela tbl_001 do HBase através da ferramenta TImportTSV do HBase, desta forma todos os registros atualizados na tabela tbl_001 serão inseridos na tabela log_001 do HBase. Outro teste a ser realizado é apagar, alterar ou inserir um registro na tabela tbl_001 e analisar o resultado na tabela log_001. 

Uma dica importante, é que caso necessite alterar o código do coprocessor, mesmo que você atualize o arquivo ".jar" no HDFS o HBase não irá reconhecer a alteração, pois a classe já subiu em memória, nesse caso será necessário reiniciar o serviço do HBase.

Isso é apenas para demonstrar um pouco do que pode ser feito com o coprocessor no HBase, é apenas uma introdução, tem muito mais sobre esse assunto a ser abordado, mas serve como uma primeira referência para entender o funcionamento do mesmo.

Abaixo segue alguns links de referência.
- [https://www.3pillarglobal.com/insights/hbase-coprocessors](https://www.3pillarglobal.com/insights/hbase-coprocessors)
- [https://blogs.apache.org/hbase/entry/coprocessor_introduction](https://blogs.apache.org/hbase/entry/coprocessor_introduction)
- [https://hbase.apache.org/](https://hbase.apache.org/)
