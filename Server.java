
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;

public class Server {
    private static Hashtable<String, PrintWriter> namePrinterTable = new Hashtable<>();
    private static Hashtable<Integer, String> idNameTable = new Hashtable<>();
    private static int clientSize = 0;
    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
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
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
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

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
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

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);

                //Put names and printers to tables
                namePrinterTable.put(name, out);
                int clientId = names.size()-1;
                idNameTable.put(clientId, name);
                clientSize++;

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

                    if(str[0].equals("putkey")){
                        int index = Integer.parseInt(str[1]);
                        PrintWriter writer = namePrinterTable.get(idNameTable.get(index));
                        writer.println("MESSAGE " + "putkey " + str[2] + " " + str[3]);
                    }
                    else if(str[0].equals("getsize")){
                        PrintWriter writer = namePrinterTable.get(name);
                        writer.println("MESSAGE " + "returnsize " + clientSize);
                    }
                    else if(str[0].equals("getkey")){
                        int index = Integer.parseInt(str[1]);
                        System.err.println("Servet getkey " + "index: " + index + " toname: " + name);
                        PrintWriter writer = namePrinterTable.get(idNameTable.get(index));
                        writer.println("MESSAGE " + "getkey " + str[2] + " " + name);
                    }
                    else if(str[0].equals("resget")){
                        String clientName = str[3];
                        PrintWriter writer = namePrinterTable.get(clientName);
                        writer.println("MESSAGE " + "resget " + str[1] + " " + str[2]);
                    }
                    else{
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input + " " + clientSize);
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