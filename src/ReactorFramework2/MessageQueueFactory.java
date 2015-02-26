package ReactorFramework2;

/**
 * Created by NatchaS on 1/21/15.
 */
public class MessageQueueFactory {
    public static InputQueue createInputQueue(){
        return new MessageInputQueue();
    }

    public static OutputQueue createOutputQueue(){
        return new MessageOutputQueue();
    }

}
