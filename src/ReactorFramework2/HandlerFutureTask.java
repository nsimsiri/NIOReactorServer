package ReactorFramework2;

import java.util.concurrent.FutureTask;

/**
 * Created by NatchaS on 1/22/15.
 */
public class HandlerFutureTask extends FutureTask {
    public HandlerFutureTask(HandlerAdapter adapter){
        super(adapter);
    }

    public void done(){

    }


}
