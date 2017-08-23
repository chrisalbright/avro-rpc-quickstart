package protocol;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.Union;
import org.apache.avro.specific.SpecificData;

import static org.apache.avro.Schema.Type.RECORD;
import static org.apache.avro.Schema.Type.UNION;

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
