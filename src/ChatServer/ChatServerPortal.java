package ChatServer;

import ReactorFramework2.Acceptable;
import ReactorFramework2.InputHandler;

/**
 * Created by NatchaS on 1/26/15.
 */
public class ChatServerPortal implements Acceptable {

    public InputHandler createInputHandler(){
        return new ChatHandler();
    }

    public static void main(String[] args){

    }
}
