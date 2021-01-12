package models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Model
public class Mail implements Externalizable {

    private static final long serialVersionUID = -4034326865454415426L;
    private transient LongProperty id;


    private transient User sender;
    private transient ListProperty<User> receiver;

    private transient StringProperty object;
    private transient StringProperty message;
    private transient BooleanProperty sent;



    public Mail(long id, User sender, List<User> receiver, String object, String message) {
        this.id = new SimpleLongProperty(id);
        this.sender = sender;
        this.receiver = new SimpleListProperty<>();
        if (receiver != null) setReceiver(receiver);
        this.object = new SimpleStringProperty(object);
        this.message = new SimpleStringProperty(message);
        this.sent = new SimpleBooleanProperty();
    }

    public Mail() {
        init();
    }


    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public void init() {
        this.id = new SimpleLongProperty();
        this.sender = new User();
        this.receiver = new SimpleListProperty<>();
        this.object = new SimpleStringProperty();
        this.message = new SimpleStringProperty();
        this.sent = new SimpleBooleanProperty();
    }

    public User getSender() {
        return sender;
    }
    public void setSender(User sender) {
        this.sender = sender;
    }



    public List<User> getReceiver() {
        return receiver.get();
    }

    public void setReceiver(List<User> receiver) {
        //this.receiver.clear();
        this.receiver.set(FXCollections.observableList(receiver));
    }

    public String getObject() {
        return object.get();
    }

    public void setObject(String object) {
        this.object.set(object);
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public boolean isSent() {
        return sent.get();
    }

    public void setSent(boolean sent) {
        this.sent.set(sent);
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


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeUTF(getMessage());
        out.writeUTF(getObject());
        //out.writeObject(getReceiver());
        out.writeObject(getSender());
        out.writeBoolean(isSent());


    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        init();
        setId(in.readLong());
        setMessage(in.readUTF());
        setObject(in.readUTF());
        List<User> asd = new ArrayList<>();
        asd.add(new User("luca.cognigni"));
        setReceiver(asd);
        setSender((User) in.readObject());
        setSent(in.readBoolean());
    }
}
