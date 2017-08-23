package protocol;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.Union;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

class UnionAnnotationReflectData extends ReflectData {

    private final Map<Set<String>,Class<?>> classNameMap = new HashMap<>();

    public UnionAnnotationReflectData (Class<?> ... classes) {

        for (Class<?> clazz : classes) {
            final Union[] unionAnnotations = clazz.getAnnotationsByType(Union.class);

            if (unionAnnotations.length == 0) {
                throw new RuntimeException("Class does not have @Union annotation: " + clazz);
            }

            if (unionAnnotations.length > 1) {
                throw new RuntimeException("Class has more than one @Union annotation: " + clazz);
            }

            Union annotation = unionAnnotations[0];

            Set<String> classNames = Arrays.stream(annotation.value())
                    .filter(clazz::isAssignableFrom)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.toSet());

            if (classNames.size() == annotation.value().length) {
                classNameMap.put(classNames, clazz);
            } else {
                System.err.printf("warning: all union classes did not implement the union interface %s - skipping\n", clazz);
            }
        }
    }


    @Override
    public Class getClass(Schema schema) {

        System.out.printf("schema name=%s, full name=%s, type=%s\n", schema.getName(), schema.getFullName(), schema.getType());

        if (schema.getType() == UNION && allSubTypesAreRecords(schema)) {

            Set<String> schemaClassNames = schema.getTypes().stream().map(Schema::getFullName).collect(Collectors.toSet());

            Class<?> unionInterface = classNameMap.get(schemaClassNames);

            System.out.println("union interface:" + unionInterface);

            if (unionInterface != null ) return unionInterface;
        }

        return super.getClass(schema);
    }

    private boolean allSubTypesAreRecords(Schema schema) {
        return schema.getTypes().stream().allMatch(t -> t.getType() == RECORD);
    }
}
