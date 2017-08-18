package example;

import example.proto.phonebook.*;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class PhonebookClient {
    public static void main(String[] args) throws IOException {

        final NettyTransceiver syncClient = new NettyTransceiver(new InetSocketAddress(PhonebookServer.PORT));
        final NettyTransceiver asyncClient = new NettyTransceiver(new InetSocketAddress(PhonebookServer.PORT));

        Phonebook phonebook = SpecificRequestor.getClient(Phonebook.class, syncClient);
        Phonebook.Callback phonebookAsync = SpecificRequestor.getClient(Phonebook.Callback.class, asyncClient);


        List<Person> persons = phonebook.findByName("Chris");
        phonebookAsync.findByName("Chris Async", new Callback<List<Person>>() {
            @Override
            public void handleResult(List<Person> persons) {
                System.out.println("I am async!");
                System.out.println(persons);
            }

            @Override
            public void handleError(Throwable throwable) {
                System.err.println(new java.util.Date() +  " - Error is: " + throwable);
            }
        });
        System.out.println(persons);

        final Person p = persons.get(0);




//        phonebook.savePerson(p);

        phonebookAsync.savePerson(p, new Callback<Result>() {
            @Override
            public void handleResult(Result result) {
                System.out.println("Result is: " + result.getMessage());
            }

            @Override
            public void handleError(Throwable throwable) {
                System.err.println(new java.util.Date() + " - Error is: " + throwable.getMessage());
                ShitHappensException she = (ShitHappensException)throwable;
                System.err.println(String.format("value: %s scale: %d", she.getShit(), she.getShit().scale()));
            }
        });

        System.out.println(new java.util.Date() + " - Trying to save, but it may take a second");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutdown hook");
                asyncClient.close();
                syncClient.close();
            }
        });
    }
}
