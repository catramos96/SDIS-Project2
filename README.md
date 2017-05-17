# SDIS-Project2 

### Tracker

* root - Subscriber
* HashMap - key: Subscriber, value: lista de subscribers "filhos" <- Não é demasiada informação em grande escala ? Outra solução ?
* HashMap - key: Subscriber, value: true/false /(ativo/inativo)

Lidar com a inatividade de peers:
* Função calendarizada de x em x tempos que envia uma mensagem para os peers;
* Quando os peers recebem esta mensagem, enviam para o traker uma mensagem de confirmação (ativos) e o valor da entrada do hashmap para aquele peer fica a true. A mensagem de confirmação contém o pai do Subscriber (+/- informação ?);
* A cada chamada da função, se tiver entradas no hashmap a false então o peer está inativo e é eliminado das tabelas, avisando os peers filhos e pai de forma a ajustarem a topologia do canal com "REMSUBSCRIBER" (remover filho) ou "PARENT" (novo pai);
* A cada chamada da função, inicia todos os valores da entrada no hashmap a false após o passo anterior;

### Peer (Subscriber -> informação do peer na rede)

* Informação como subscriber - Subscriber (InetAddress,port)
* Lista de subscribers filhos
* Pai - Subscriber
* root - Subscriber -> será o tracker ??
* tracker - Subscriber

Adesão ao grupo:
* Envio de mensagem para o tracker "WHOISROOT"
* Tracker envia uma mensagem a avisar que é o root
* Peer pede adesão ao tracker "ADDSUBSCRIBER"
* Tracker analisa quem tem menos de 5 filhos e está ativo e envia-lhe diretamente quem vai ser o pai com "PARENT"
* Peer manda mensagem "SUBSCRIBER" para o pai avisando-o que é seu filho (atualiza as variáveis necessárias)

### Comunicação

Mensagens de Protocolo
* Envio de mensagens de protocolo para o root
* Ao receber uma mensagem, o nó envia para os seus filhos, e interpreta a mensagem;

Mensagens de Topologia
* Envio de mensagens para um target específico
* Ao receber uma mensagem, processa-a e só a envia para os seus filhos em caso de ser uma mensagem "ROOT" (informativa)

### Mensagens

Mensagens de Protocolo: "PUTCHUNK" "CHUNK" "GETCHUNK" "STORED" "DELETE" "RECLAIM" "STATE"
Mensagens de Topologia: "WHOISROOT" "ROOT" "SUBSCRIBER" "PARENT" "REMSUBSCRIBER" "MOVSUBSCRIBER" "INLINE" "ONLINE" "OFFLINE" ...

* WHOISROOT: pedido para devolver a root atual
* ROOT: informação sobre quem é o root atual
* SUBSCRIBER: informação sobre quem é o filho atual
* PARENT: informação sobre quem é o pai atual
* REMSUBSCRIBER: pedido para remover um subscriber (filho)
* MOVSUBSCRIBER: ?
* INLINE: pedido para devolver o estado atual na rede (ONLINE/OFFLINE - default)
* ONLINE: informação de que se encontra ativo (rotina)
* OFFLINE: informação de que vai deixar de estar ativo
