package example;

import example.proto.phonebook.*;
import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.avro.util.Utf8;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PhonebookServer {

    public static final int PORT = 8080;

    public static class PhonebookImpl implements Phonebook {

        private final Random r = new SecureRandom();

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

        @Override
        public Result savePerson(Person person) throws AvroRemoteException {
            if (r.nextBoolean()) {
                System.out.println("Looks like were in for some shit!");
                ShitHappensException shitHappensException = new ShitHappensException();
                shitHappensException.setMessage$("Why is there a dollar sign in this method?");
                shitHappensException.setShit(BigDecimal.valueOf(r.nextInt()/33d).setScale(3, BigDecimal.ROUND_HALF_DOWN));
                throw shitHappensException;
            }
            try {
                Thread.sleep(1000);
                System.out.println("Saving " + person.getFirstName() + " " + person.getLastName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result result = new Result("Saved");
            System.out.println("returning result: " + result.toString());
            return result;
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
