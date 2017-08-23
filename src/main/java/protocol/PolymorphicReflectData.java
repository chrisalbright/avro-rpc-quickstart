package protocol;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

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
