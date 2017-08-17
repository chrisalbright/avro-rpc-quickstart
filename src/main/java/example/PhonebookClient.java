package example;

import example.proto.phonebook.Person;
import example.proto.phonebook.Phonebook;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class PhonebookClient {
    public static void main(String[] args) throws IOException {
        final NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(PhonebookServer.PORT));
        Phonebook phonebook = SpecificRequestor.getClient(Phonebook.class, client);
        List<Person> persons = phonebook.findByName("Chris");
        System.out.println(persons);


        Phonebook.Callback phonebookAsync = SpecificRequestor.getClient(Phonebook.Callback.class, client);

        phonebookAsync.findByName("Chris Async", new Callback<List<Person>>() {
            @Override
            public void handleResult(List<Person> persons) {
                System.out.println("I am async!");
                System.out.println(persons);
            }

            @Override
            public void handleError(Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                client.close();
            }
        });
    }
}
