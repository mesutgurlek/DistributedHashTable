

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area.
 */
public class Client {

    BufferedReader in;
    PrintWriter out;

    //Creating a hashtable
    Hashtable<String, Integer> table;
    private int clientSize;

    //Library Variables
    private int port;
    private String serverAddress;
    private AtomicBoolean wait;
    private int gettedValue;
    private String gettedKey;
    private String clientName;
    private Thread t, t2;
    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public Client(String clientName, String serverAddress, int port) {
        this.port = port;
        this.serverAddress = serverAddress;
        this.clientName = clientName;
        //Initialize Hashtable
        table = new Hashtable<>();
        clientSize = 1;
        wait = new AtomicBoolean(true);

        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getKey(String key){
        int clientIndex = generateIndex(key);
        System.err.println("GETKEY Index is generated as: " + clientIndex);
        if(table.containsKey(clientIndex)){
            return table.get(clientIndex);
        }
        out.println("getkey " + clientIndex + " " + key);
        while(wait.get()){

        }
        wait.set(true);
        return gettedValue;
    }

    public  void putKey(String key, Object val){
        int clientIndex = generateIndex(key);
        System.err.println("PUTKEY Index is generated as: " + clientIndex);
        out.println("putkey " + clientIndex + " " + key.toString() + " " + val.toString());
    }

    public  int generateIndex(String key){
        int hash = 7;
        for (int i = 0; i < key.length(); i++) {
            hash = hash*31 + key.charAt(i);
        }
        //int hash = key.hashCode();
        int index = hash % clientSize;
        System.err.println("GenerateIndex ClientSize: " + clientSize);
        return index;
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return serverAddress;
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    public void run() throws IOException {
        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket  socket = new Socket(serverAddress, port);

        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        t = new Thread(new Runnable() {
            public void run() {
                // Process all messages from server, according to the protocol.
                while (true) {
                    String line = null;
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (line.startsWith("SUBMITNAME")) {
                        out.println(clientName);
                    } else if (line.startsWith("NAMEACCEPTED")) {

                    } else if (line.startsWith("MESSAGE")) {
                        String[] str = line.split(" ");

                        if(str.length == 3 && str[1].equals("returnsize")){
                            clientSize = Integer.parseInt(str[2]);
                            //System.err.println("Returned Client Size: " + clientSize + "\n");
                        }
                        else if(str.length == 4 && str[1].equals("getkey")){
                            String resClientName = str[3];
                            int res = table.get(str[2]);
                            out.println("resget " + str[2] + " " + res + " " + resClientName);
                            //System.err.println("Send Key: key: " + str[2] + " value: " + res + "\n");
                        }
                        else if(str.length == 4 && str[1].equals("resget")){
                            System.err.println("Found key: " + str[2] + " value: " + str[3] + "\n");
                            gettedValue = Integer.parseInt(str[3]);
                            gettedKey = str[2];
                            wait.set(false);
                        }
                        else if(str.length == 4 && str[1].equals("putkey")){
                            table.put(str[2], Integer.parseInt(str[3]));
                            //System.err.println("Successfull Put: key: " + str[2] + " value: " + str[3] + "\n");
                        }
                        else{
                            //System.err.println(line.substring(8, line.length()-1) + "\n");
                        }
                        //System.err.println(line);
                    }
                }
            }
        });

        t.start();
    }
}