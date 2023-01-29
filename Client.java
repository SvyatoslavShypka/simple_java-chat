import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.io.StringReader;

public class Client {

  private String host;
  private int port;

  public static void main(String[] args) throws UnknownHostException, IOException {
    new Client("127.0.0.1", 12345).run();
  }

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws UnknownHostException, IOException {
    // connect client to server
    Socket client = new Socket(host, port);
    System.out.println("Client successfully connected to server!");

    // Get Socket output stream (where the client sends its messages)
    PrintStream output = new PrintStream(client.getOutputStream());

    // ask for a nickname
    Scanner sc = new Scanner(System.in);
    System.out.print("Enter a nickname: ");
    String nickname = sc.nextLine();

    // send nickname to server
    output.println(nickname);

    // create a new thread for server messages handling
    new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

    // read messages from keyboard and send to server
    System.out.println("Messages: \n");

    // while new messages
    String str;
    while (sc.hasNextLine()) {
      str = sc.nextLine();
      if (!str.equals("")) {
        output.println(str);
      }
    }

    // end ctrl D
    output.close();
    sc.close();
    client.close();
  }
}

class ReceivedMessagesHandler implements Runnable {

  private InputStream server;
  private static ArrayList<String> userTags;

  public ReceivedMessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {
    // receive server messages and print out to screen
    Scanner s = new Scanner(server);
    String tmp = "";
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
        userTags = new ArrayList<>();
        for (String x: tmp.split(",")
             ) {
        userTags.add(getTagUserValue(x));
        }
//        One USER or many USERS
        int userQuantity = userTags.size();
        System.out.println("USERS LIST: Total - " + userQuantity + (userQuantity > 1 ? " users" : " user"));
        for (String x: userTags
             ) {
          System.out.println("\t\t\t" + x);
        }
      }else{
        try {
          System.out.println(getTagWelcomeValue(tmp) + getTagUserValue(tmp) + "!");
          // System.out.println(tmp);
        } catch(Exception ignore){
//          System.out.println("Exception");
        }
      }
    }
    s.close();
  }

  // I could use a javax.xml.parsers but the goal of Client.java is to keep everything tight and simple
  public static String getTagUserValue(String xml){
    String tmp3 = xml.split("<span")[1];
    String tmp4 = tmp3.split("</span>")[0];
    String tmp5 = tmp4.split(">")[1];
    String tmp2 = tmp5;

    return  tmp2;
  }

  public static String getTagWelcomeValue(String xml){
    String leftCutWelcome = xml.split(">")[2];
    String rightCutWelcome = leftCutWelcome.split("<")[0];

    return  rightCutWelcome  + "\t";
  }

}
