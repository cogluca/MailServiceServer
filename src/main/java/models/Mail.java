package models;

import java.io.Serializable;
import java.sql.Timestamp;

// Model
public class Mail implements Serializable {
    private String from, to;
    //to deve essere una lista di stringhe considerando l'invio multiplo

    private String object;
    private String message;

    private Timestamp timestamp;

    public Mail() {}
    public Mail(String message) {this.message = message;}


    public Mail(String from, String to, String object, String message, Timestamp timestamp) {
        this.from = from;
        this.to = to;
        this.object = object;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", object='" + object + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
