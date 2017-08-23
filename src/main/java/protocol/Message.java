package protocol;

import org.apache.avro.reflect.Union;

@Union({EnglishMessage.class, SpanishMessage.class})
public interface Message {
    public String getMessage();
}
