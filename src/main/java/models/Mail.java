package models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

// Model
public class Mail implements Serializable {

    private long id;

    private User sender;
    private List<User> receiver;

    private String object;
    private String message;
    private boolean sent;

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Mail(long id, User sender, List<User> receiver, String object, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.object = object;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
                '}';
    }

    public String listAddresses() {
        String addresses = "";
        for(User receivingAddress: receiver) {
            addresses += receivingAddress.getUsername() + "@Parallel.com; ";
        }
        return addresses;
    }


}
