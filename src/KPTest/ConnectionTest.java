package KPTest; /**
 * Created by NatchaS on 1/1/15.
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ConnectionTest {
    public class Client{
        public Client() throws IOException{
            Socket socket = new Socket("localhost", 8080);
        }

        public void write(String txt){
//            Socket
        }
    }
    public void test1(){
        for (int i = 0; i < 100; i++){
            try {ConnectionTest.Client cl = new ConnectionTest.Client();}
            catch (IOException e){};
        }

    }
    public static void main(String[] args){
        ConnectionTest t = new ConnectionTest();
        t.test1();

        // input test
        KPLogger logger = new KPLogger(">");
        Scanner sc = new Scanner(System.in);
        String input = "";
        while(!input.equals("q")) {
            System.out.print(">");
            input = sc.nextLine();
            logger.log(String.format("reply: %s", input.toUpperCase()));
        }
    }

}
