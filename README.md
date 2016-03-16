# DistributedHashTable

Distributed Hash Table
Implemented with Java Sockets and works as client-server. After initilizing server, multiple clients can connect to that server. 
Client has 'putkey(String key, Object value)' and 'getKey(String key)' methods. putKey  hashes the key and send to corresponding
client to store key. Coversly getKey method uses same hash function and gets value from corresponding client. Main.java shows example implementation.
