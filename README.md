# HBase - Coprocessor

De uma forma macro e resumida, podemos definir o coprocessor como um framework que provém um caminho fácil para executar seu código customizado. As analogias mais utilizadas para representar o coprocessor são "Trigger / Store Procedure" e AOP. 

O coprocessor pode ser desenvolvido como: observer ou endpoint. 
- Observer Coprocessor: é como uma "trigger" de banco de dados, ou seja, é acionado antes ou após um determinado evento. 
- Endpoint Coprocessor: é como uma "store procedure" de um banco de dados, sendo que esse tipo de coprocessor deve ser acionado explicitamente através do método 'CoprocessorService ()' da 'HTableInterface' (ou HTable).

Os coprocessadores não são projetados para serem utilizados pelos usuários finais, mas pelos desenvolvedores. Coprocessadores são executados diretamente no Region Server, portanto um código mal intencionado pode um derrubar seu Region Server, ou seja, cuidado ao implementar um coprocessor.
