# SDIS-Project2 


##### Backup
* Por cada peer que faça store de mensagens, retirá-lo dos subscribers no groupChannel, assim não envia mais mensagens para ele.
* Guardar apenas temporariamente os peers que guardaram os stores, só para saber se o replication degree foi atingido, depois não. interessa

##### Delete
* De x em x dias, é enviada uma mensagem por chunk, para o tracker para saber se ainda existe o chunk, em caso negativo elimina-se todos os chunks daquele ficheiro. O tracker envia a mensagem INFO com o repDegree, e se for inferior ao desejado, então faz se backup. A validade podia ser definida no Util tipo = 5 dias. Depois no registo dos chunks que temos e outro tipo de informações guardavamos também a próxima data de "renovar" o chunk. Quando o peer inicia sessão ele pode verificar neste registo os peers que são precisos renovar e então envia um check para o tracker, se o tracker retornar uma mensagem info com repDeg = 0 então é porque foi eliminado


##### GUI 
* windows
