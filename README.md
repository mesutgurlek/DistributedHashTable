# DistributedHashTable

Distributed Hash Table
Implemented with Java Sockets and works as client-server. After initilizing server, multiple clients can connect to that server. 
Client has 'putkey(String key, Object value)' and 'getKey(String key)' methods. putKey  hashes the key and send to corresponding
client to store key. Coversly getKey method uses same hash function and gets value from corresponding client. Following codes show example implementation of this library for now:

Client client1 = new Client("mesut", "localhost", 9001);
Client client2 = new Client("yusuf", "localhost", 9001);
Client client3 = new Client("osman", "localhost", 9001);


client1.putKey("Key1", 4);
client2.putKey("Key2", 7);
