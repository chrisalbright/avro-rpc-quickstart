package protocol;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MessagingClient {
    public static void main(String[] args) throws IOException {
        final NettyTransceiver syncClient = new NettyTransceiver(new InetSocketAddress(MessagingServer.PORT));
        final Messaging client = ReflectRequestor.getClient(Messaging.class, syncClient);

        Message translation1 = client.translate(new EnglishMessage("Hello World"));
        Message translation2 = client.translate(new SpanishMessage("Hola Mundo"));

        System.out.println(translation1);
        System.out.println(translation2);

        syncClient.close();
    }
}
