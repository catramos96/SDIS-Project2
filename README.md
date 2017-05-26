# SDIS-Project2 

### Implementação da DHT

#### Mensagens
PUT     <key> <address> <port1> <port2> <port3> <port4> <CRLF><CRLF>
GET     <key> <numberPeers> <pagination>                <CRLF><CRLF>
CHECK   <key>                                           <CRLF><CRLF>
REMOVE  <key> <address> <port1> <port2> <port3> <port4> <CRLF><CRLF>
DELETE  <key> <CRLF> <key> <CRLF> ...                   <CRLF><CRLF>
        
#### Tracker
Métodos DTH:
* PUT       peer with chunk
* GET       peers with chunk
* CHECK     if peer has chunk + n_peers
* REMOVE    peer that no longer has chunk
* DELETE    delete chunk info

Métodos gerais:
* Obter os x primeiros peers com um chunk com paginação y

#### Peer
Protocolos:

Sempre que um peer iniciar sessão (ativo), ele faz check de todos os chunks que ele tem de outros e dos que fez backup, confirma se ainda existem (se não elimina-os) e faz backup se o número de peers for inferior ao replication degree.

##### Backup
* Ir buscar mais peers no caso de o protocolo começar a repetir muitas vezes por falta de stores.
* Por cada peer que faça store de mensagens, retirá-lo dos subscribers no groupChannel, assim não envia mais mensagens para ele.
* Guardar apenas temporariamente os peers que guardaram os stores, só para saber se o replication degree foi atingido, depois não. interessa

##### Restore
* Ir buscar x peers com aquele chunk com paginação = 1 e enviar para esses.
* Por cada try no restore protocol, ir buscar mais x peers ao tracker com paginação = n+1.

##### Delete
* Remover no tracker os chunks dos ficheiros.

##### Remove
* Remover no tracker a entrada no peer para aquele chunk.
