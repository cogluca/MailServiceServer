package models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

// Model
public class Mail implements Serializable {

    private int id;
    private Timestamp timestamp;

    private User sender;
    private List<User> receiver;

    private String object;
    private String message;


    public Mail() {}
    public Mail(String message) {this.message = message;}


    public Mail(User sender, List<User> receiver, String object, String message, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.object = object;
        this.message = message;
        this.timestamp = timestamp;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public List<User> getReceiver() {
        return receiver;
    }

    public void setReceiver(List<User> receiver) {
        this.receiver = receiver;
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
                "sender=" + sender +
                ", receiver=" + receiver +
                ", object='" + object + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
