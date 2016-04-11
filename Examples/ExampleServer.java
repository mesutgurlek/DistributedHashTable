
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;

public class Server {
    private static Hashtable<String, PrintWriter> namePrinterTable = new Hashtable();
    private static Hashtable<String, Integer> serverHashTable = new Hashtable<String, Integer>();
    private static Integer clientSize = 0;
    private static String serverAdd;
    private static String port;
    /**
     * The set of all names of clients in the room so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        port = args[0];
        serverAdd = InetAddress.getLocalHost().toString();
        ServerSocket listener = new ServerSocket(Integer.parseInt(args[0]));
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            //clientTable.put(clientCounter++, name);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);

                //Put names and printers to tables
                namePrinterTable.put(name, out);
                synchronized (clientSize){
                    clientSize++;
                }

                for(PrintWriter writer : writers){
                    writer.println("MESSAGE " + "returnsize " + clientSize);
                }

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    String[] str = input.split(" ");

                    synchronized (str[0]){
                        if(str[0].equals("putkey")){
                            serverHashTable.put(str[1], Integer.parseInt(str[2]));
                            System.err.println("[" + serverAdd + "]" + " put: " + str[1] + " : " + Integer.parseInt(str[2]));
                        }
                        else if(str[0].equals("getsize")){
                            PrintWriter writer = namePrinterTable.get(name);
                            writer.println("MESSAGE " + "returnsize " + clientSize);
                        }
                        else if(str[0].equals("getkey")){
                            String clientName = str[1];
                            System.err.println("[" + serverAdd + ":" + port + "]" + " : " + " get:" + str[2] + " : " + serverHashTable.get(str[2]));
                            PrintWriter writer = namePrinterTable.get(clientName);
                            writer.println("MESSAGE " + "getkey " + str[2] + " " + serverHashTable.get(str[2]) + " " + serverAdd + " " + port);
                        }
                        else{
                            for (PrintWriter writer : writers) {
                                writer.println("MESSAGE " + name + ": " + input + " " + clientSize);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                clientSize--;
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}