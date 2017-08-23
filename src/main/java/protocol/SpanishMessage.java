package protocol;

public class SpanishMessage implements Message {

    private final String message;

    public SpanishMessage() {
        this("");
    }

    public SpanishMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "En Espanol: " + message;
    }
}
