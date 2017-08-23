package protocol;

import org.apache.avro.Schema;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.Union;
import org.apache.avro.specific.SpecificData;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

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



class UnionAnnotationReflectData extends ReflectData {

    final Set<String> classNames;
    final Class<?> unionInterface;

    public UnionAnnotationReflectData (Class<?> clazz) {

        final Union[] unionAnnotations = clazz.getAnnotationsByType(Union.class);

        if (unionAnnotations.length == 0) {
            throw new RuntimeException("Class does not have @Union annotation: " + clazz);
        }

        if (unionAnnotations.length > 1) {
            throw new RuntimeException("Class has more than one @Union annotation: " + clazz);
        }

        Union annotation = unionAnnotations[0];

        classNames = Arrays.stream(annotation.value()).map(Class::getCanonicalName).collect(Collectors.toSet());
        this.unionInterface = clazz;
    }

    @Override
    public Class getClass(Schema schema) {

        System.out.printf("schema name=%s, full name=%s, type=%s\n", schema.getName(), schema.getFullName(), schema.getType());

        if (schema.getType() == UNION && allSubTypesAreRecords(schema)) {

            Set<String> schemaClassNames = schema.getTypes().stream().map(Schema::getFullName).collect(Collectors.toSet());

            if (schemaClassNames.equals(classNames)) return unionInterface;
        }

        return super.getClass(schema);
    }

    private boolean allSubTypesAreRecords(Schema schema) {
        return schema.getTypes().stream().allMatch(t -> t.getType() == RECORD);
    }
}


class AssignableReflectData extends ReflectData {

    final Class<?> unionInterface;

    AssignableReflectData(Class<?> clazz) {
        this.unionInterface = clazz;

        if (unionInterface.getAnnotationsByType(Union.class).length == 0) {
            throw new RuntimeException("Class does not have @Union annotation: " + clazz);
        }
    }

    @Override
    public Class getClass(Schema schema) {

        if (schema.getType() == UNION &&
                allSubTypesAreRecords(schema) &&
                allSubTypesImplementUnionInterface(schema)) {

            return unionInterface;
        }

        return super.getClass(schema);
    }

    private boolean allSubTypesAreRecords(Schema schema) {
        return schema.getTypes().stream().allMatch(t -> t.getType() == RECORD);
    }

    private boolean allSubTypesImplementUnionInterface(Schema schema) {
        return schema.getTypes().stream()
                .map(this::classForSchema)
                .allMatch(unionInterface::isAssignableFrom);
    }

    private Class<?> classForSchema(Schema s) {
        try {
            String className = SpecificData.getClassName(s);
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load class for schema: " + s, e);
        }
    }
}


class PolymorphicReflectData extends ReflectData {

    private Set<Class<?>> intersection(Set<Class<?>> a, Set<Class<?>> b) {
        return a.stream().filter(b::contains).collect(Collectors.toSet());
    }

    private Set<Class<?>> interfacesForClass(Class<?> c) {
        System.out.println("interfacesForClass " + c);
        return Arrays.stream(c.getInterfaces()).collect(Collectors.toSet());
    }

    private Class<?> classForSchema(Schema s) {
        System.out.println("classForSchema " + s.getFullName());
        try {
            String className = SpecificData.getClassName(s);
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't load class for schema: " + s, e);
        }
    }

    @Override
    public Class getClass(Schema schema) {

        System.out.printf("schema name=%s, full name=%s, type=%s\n", schema.getName(), schema.getFullName(), schema.getType());
        //System.out.println(schema.toString(true));

        Class<?> commonInterface = null;

        if (schema.getType() == UNION) {

            boolean allSubTypesAreRecords = schema.getTypes().stream().allMatch(t -> t.getType() == RECORD);

            if (allSubTypesAreRecords) {

                commonInterface = schema.getTypes().stream()
                        .map(this::classForSchema)
                        .map(this::interfacesForClass)
                        .reduce(this::intersection)
                        .flatMap(s -> s.stream().findFirst())
                        .orElse(null);

                System.out.println("returning " + commonInterface);
            }
        }

        return commonInterface == null ? super.getClass(schema) : commonInterface;
    }
}

