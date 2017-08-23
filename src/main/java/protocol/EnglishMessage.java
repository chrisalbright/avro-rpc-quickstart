package protocol;

import java.io.Serializable;

public class EnglishMessage implements Message, Serializable {
    private final String message;

    public EnglishMessage() {
        this("");
    }

    public EnglishMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "In English: " + message;
    }
}
