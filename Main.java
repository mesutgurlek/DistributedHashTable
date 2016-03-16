import java.util.Scanner;

/**
 * Created by Mesut on 16.3.2016.
 */
public class Main {
    public static void main ( String [] arguments )
    {
        Scanner scanner = new Scanner(System.in);
        Client client1 = new Client("mesut", "localhost", 9001);
        Client client2 = new Client("yusuf", "localhost", 9001);
        Client client3 = new Client("osman", "localhost", 9001);

        String line = scanner.nextLine();
        String[] command = line.split(" ");
        System.out.println(line);
        while(!command[0].equals("exit")){
            if(command[0].equals("get")){
                int res = client1.getKey(command[1]);
                System.out.println("Getted: " + command[1] + " : " + res);
            }else if(command[0].equals("put")){
                client1.putKey(command[1], command[2]);
                System.out.println("Putted: " + command[1] + " : " + command[2]);
            }
            line = scanner.nextLine();
            command = line.split(" ");
            System.out.println(line);
        }

        scanner.close();
    }
}
