package protocol;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectResponder;

import java.net.InetSocketAddress;

public class MessagingServer implements Messaging {

    public static final int PORT = 9001;

    @Override
    public Message translate(Message message) {
        if (message instanceof SpanishMessage ) {
            return new EnglishMessage(message.getMessage());
        }
        else if (message instanceof EnglishMessage) {
            return new SpanishMessage(message.getMessage());
        }

        throw new RuntimeException("Whachoogimme?");
    }


    public static void main(String[] args) {
        //System.out.println(ReflectData.get().getSchema(Message.class));
        //Responder r = new ReflectResponder(Messaging.class, new MessagingServer(), new PolymorphicReflectData());
        //Responder r = new ReflectResponder(Messaging.class, new MessagingServer(), new AssignableReflectData(Message.class));
        Responder r = new ReflectResponder(Messaging.class, new MessagingServer(), new UnionAnnotationReflectData(Message.class));
        Server s = new NettyServer(r, new InetSocketAddress(PORT));
        s.start();
        System.out.println("Server ready.");
    }
}


