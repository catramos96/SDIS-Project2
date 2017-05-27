# SDIS-Project2 

### Implementação da DHT

#### Mensagens
PUT     <key> <address> <port1> <port2> <port3> <port4> <CRLF><CRLF>
GET     <key> <numberPeers> <pagination>                <CRLF><CRLF>
CHECK   <key>                                           <CRLF><CRLF>
REMOVE  <key> <address> <port1> <port2> <port3> <port4> <CRLF><CRLF>
DELETE  <key> <CRLF> <key> <CRLF> ...                   <CRLF><CRLF>
INFO    <key> <repDegree>                               <CRLF><CRLF>
        
#### Tracker
Métodos DTH:
* PUT       peer with chunk
* GET       peers with chunk
* CHECK     if peer has chunk + n_peers
* INFO      has or not peer, + rep degree
* REMOVE    peer that no longer has chunk
* DELETE    delete chunk info

Métodos gerais:
* Obter os x primeiros peers com um chunk com paginação y

#### Peer
Protocolos:

Sempre que um peer iniciar sessão (ativo), ele faz check de todos os chunks que ele tem de outros e dos que fez backup, confirma se ainda existem (se não elimina-os) e faz backup se o número de peers for inferior ao replication degree.

##### Backup
* ~~Ir buscar mais peers no caso de o protocolo começar a repetir muitas vezes por falta de stores.~~
* Por cada peer que faça store de mensagens, retirá-lo dos subscribers no groupChannel, assim não envia mais mensagens para ele.
* Guardar apenas temporariamente os peers que guardaram os stores, só para saber se o replication degree foi atingido, depois não. interessa
* ~~Alterar mensagem de PUTCHUNK e por o ip e a port do canal mdb do peer. Depois o STORED é mandado para este ip e port.~~
* ~~Se um peer que recebe um putchunk já tiver feito backup daquele chunk, então não manda mensagem stored~~

##### Restore
* ~~Ir buscar x peers com aquele chunk com paginação = 1 e enviar para esses.~~
* ~~Por cada try no restore protocol, ir buscar mais x peers ao tracker com paginação = n+1.~~
* ~~Alterar mensagem de GETCHUNK e por o ip e a port do canal mdr do peer. Depois o CHUNK é mandado para este ip e port.~~

##### Delete
* Remover no tracker os chunks dos ficheiros.
* De x em x dias, é enviada uma mensagem por chunk, para o tracker para saber se ainda existe o chunk, em caso negativo elimina-se todos os chunks daquele ficheiro. O tracker envia a mensagem INFO com o repDegree, e se for inferior ao desejado, então faz se backup.

##### Remove
* Remover no tracker a entrada no peer para aquele chunk.
* Receber a mensagem do tracker com o repDegree. Se o repDegree for mais baixo então efetuar o backup (mudar o backup para não enviar stored se já tem o chunk? assim não estamos a receber stored de peers que já tinham o chunk e confundir com o backup bem feito). Outra abordagem: assumir que se um peer remove um chunk então tem que fazer o backup com rep = 1
