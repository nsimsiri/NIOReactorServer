package ReactorFramework2Test;

import ReactorFramework2.Acceptable;
import ReactorFramework2.InputHandler;
import ReactorFramework2.ReactorServer2;

/**
 * Created by NatchaS on 1/24/15.
 */
public class ServerAcceptorPortal implements Acceptable {
    public InputHandler createInputHandler(){
        return new EchoHandler();
    }

    public static void main(String[] args){
        ReactorServer2 rs2 = new ReactorServer2(ReactorServer2._DEFAULT_PORT, new ServerAcceptorPortal());
        (new Thread(rs2)).start();
    }
}
