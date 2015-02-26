 package ReactorFramework2;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by NatchaS on 1/21/15.
 */
public class AcceptorHandlerAdapter extends HandlerAdapter {
    private Acceptable clientAcceptor;
    public AcceptorHandlerAdapter(SelectableChannel socketChannel, Reactor reactor, Acceptable clientAcceptor){
        super(socketChannel, reactor, null);
        this.clientAcceptor = clientAcceptor;
        this.name = String.format("%s/Acceptor",this.logger.getName());
        this.logger.setName(this.name);
        logger.log("Acceptor ready.");
    }

    @Override
    public HandlerAdapter call(){
        logger.log("turn");
        try {
            ServerSocketChannel ssc = (ServerSocketChannel)this.socketChannel;
            SocketChannel socket = ssc.accept();
            socket.configureBlocking(false);
            this.reactor.registerConnection(socket, this.clientAcceptor.createInputHandler());
        } catch (IOException e){
            e.printStackTrace();
        } finally{
            return this;
        }
    }
}
