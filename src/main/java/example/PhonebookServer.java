package example;

import example.proto.phonebook.*;
import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.avro.util.Utf8;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhonebookServer {

    public static final int PORT = 8080;

    public static class PhonebookImpl implements Phonebook {

        private static Type HOME = new Type("Home");
        private static Type WORK = new Type("Work");

        final ArrayList<Person> persons = new ArrayList<Person>() {
            {
                add(
                        Person.newBuilder()
                                .setId(1)
                                .setFirstName("Chris")
                                .setLastName("Albright")
                                .setPhoneNumbers(Arrays.asList(
                                        Phone.newBuilder()
                                                .setPrefix(805)
                                                .setNumber(5551212)
                                                .setType(HOME)
                                                .build()
                                ))
                                .setEmailAddresses(Arrays.asList(
                                        Email.newBuilder()
                                                .setEmailAddress("calbright@gmail.com")
                                                .setType(HOME)
                                                .build()
                                ))
                                .setStreetAddresses(Arrays.asList(
                                        Address.newBuilder()
                                                .setStreet("555 Thousand Oaks Blvd")
                                                .setCity("Thousand Oaks")
                                                .setState("California")
                                                .setZipCode("91362")
                                                .setType(HOME)
                                                .build()
                                ))
                                .build()
                );
            }
        };

        @Override
        public List<Person> findByName(String name) throws AvroRemoteException {
            System.out.println("Hello");
            System.out.println("Searching for: " + name);
            return persons;
        }
    }


    public static void main(String[] args) {

        Server server;

        server = new NettyServer(
                new SpecificResponder(Phonebook.class, new PhonebookImpl()), new InetSocketAddress(PORT)
        );

        server.start();
    }
}
