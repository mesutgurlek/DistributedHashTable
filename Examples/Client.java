

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private class ServerStruct{
        int id, port;
        String serverAddress;
        public ServerStruct(int id, String serverAddress, int port){
            this.id = id;
            this.port = port;
            this.serverAddress = serverAddress;
        }
    }

    int serverSize;
    int clientSize;
    Hashtable<Integer, ServerStruct> servers;
    Hashtable<Integer, BufferedReader> readers;
    Hashtable<Integer, PrintWriter> writers;

    //Library Variables
    private AtomicBoolean wait;
    private int gettedValue;
    private String clientName;
    private Thread t;

    public Client(String name) {
        //Initialize Hashtable
        servers = new Hashtable<Integer, ServerStruct>();
        readers = new Hashtable<Integer, BufferedReader>();
        writers = new Hashtable<Integer, PrintWriter>();

        clientName = name;
        clientSize = 1;
        serverSize = 0;
        wait = new AtomicBoolean(true);
    }

    public static void main(String[] args) throws Exception {
        String name = "";
        if(args.length > 0)
            name = args[0];
        Client c = new Client(name);
        Scanner scanner = new Scanner(System.in);
        String ans = "Y";
        do{
            System.out.println("Do you wanna add server?('Y' or 'N')");
            String line = scanner.nextLine();
            String[] command = line.split(" ");
            if(command.length == 0)
                System.out.println("Please enter a valid command!");
            else if(command[0].equalsIgnoreCase("y")){
                ans = command[0];
                System.out.print("Enter Server Address: " );
                String address = scanner.nextLine();
                System.out.print("Enter Port: " );
                String port = scanner.nextLine();
                c.addServer(address, Integer.parseInt(port));
            }
            else if(command[0].equalsIgnoreCase("n")){
                ans = command[0];
            }
        }while(ans.equalsIgnoreCase("y"));

        String line = scanner.nextLine();
        String[] command = line.split(" ");
        System.out.println(line);
        while(command.length == 2 && !command[0].equals("exit")){
            if(command[0].equals("get")){
                int res = c.getKey(command[1]);
            }else if(command.length == 3 && command[0].equals("put")){
                c.putKey(command[1], command[2]);
            }
            else{
                System.out.println("Wrong Command!!");
            }
            line = scanner.nextLine();
            command = line.split(" ");
        }

    }

    public void addServer(String serverAddress, int port){
        serverSize++;
        ServerStruct s = new ServerStruct(serverSize, serverAddress, port);
        if(!servers.containsKey(s.id) && !servers.contains(s)){
            servers.put(s.id, s);
        }
        try {
            run(s.id, s.serverAddress, s.port);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public int getKey(String key){
        int serverIndex = generateIndex(key);
        writers.get(serverIndex).println("getkey " + clientName + " " + key + " " + serverIndex);
        while(wait.get()){

        }
        wait.set(true);
        return gettedValue;
    }

    public  void putKey(String key, Object val){
        int serverIndex = generateIndex(key);
        System.err.println("Put: " + key + " : " + val.toString() + " into " + "[" + servers.get(serverIndex).serverAddress
                + ":" + servers.get(serverIndex).port + "]");
        writers.get(serverIndex).println("putkey " + key.toString() + " " + val.toString() + " " + serverIndex);
    }

    public  int generateIndex(String key){
        int hash = 7;
        for (int i = 0; i < key.length(); i++) {
            hash = hash*31 + key.charAt(i);
        }
        int index = hash % serverSize;
        return index+1;
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    public void run(final int id, String serverAddress, int port) throws IOException {
        // Make connection and initialize streams
        Socket socket = new Socket(serverAddress, port);

        BufferedReader in;
        PrintWriter out;

        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        readers.put(id, in);
        writers.put(id, out);

        t = new Thread(new Runnable() {
            public void run() {
                // Process all messages from server, according to the protocol.
                while (true) {
                    String line = null;
                    try {
                        line = readers.get(id).readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (line.startsWith("SUBMITNAME")) {
                        writers.get(id).println(clientName);
                    } else if (line.startsWith("NAMEACCEPTED")) {

                    } else if (line.startsWith("MESSAGE")) {
                        String[] str = line.split(" ");
                        if(str.length == 3 && str[1].equals("returnsize")){
                            clientSize = Integer.parseInt(str[2]);
                        }
                        else if(str.length == 6 && str[1].equals("getkey")){
                            System.err.println("[" + str[4] + ":" + str[5] + "]" + " key: " + str[2] + ", value: " + str[3] + "\n");
                            gettedValue = Integer.parseInt(str[3]);
                            wait.set(false);
                        }
                        else{
                            System.err.println("Undefined command..." + "\n");
                        }
                        //System.err.println(line);
                    }
                }
            }
        });

        t.start();
    }
}