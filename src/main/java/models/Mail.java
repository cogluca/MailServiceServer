package models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mail model implemented using the Java Beans and Properties standard, implements Externalizable in order to be
 * transmitted correctly to the server and back through sockets
 */
public class Mail implements Externalizable {

    private static final long serialVersionUID = -4034326865454415426L;
    private transient LongProperty id;
    private transient LongProperty timeSent;

    private transient User sender;
    private transient ListProperty<User> receiver;

    private transient StringProperty object;
    private transient StringProperty message;
    private transient BooleanProperty sent;


    public Mail(long id, User sender, List<User> receiver, String object, String message, long setTime) {

        this.id = new SimpleLongProperty(id);
        this.sender = sender;
        this.receiver = new SimpleListProperty<>();
        if (receiver != null) setReceiver(receiver);
        this.object = new SimpleStringProperty(object);
        this.message = new SimpleStringProperty(message);
        this.sent = new SimpleBooleanProperty();
        this.timeSent = new SimpleLongProperty(setTime);
    }

    public Mail() {
        init();
    }


    public long getId() {
        return id.get();
    }

    public long getTimeSent() {
        return timeSent.get();
    }

    public void setTimeSent(long timestampToSet) {
        this.timeSent.set(timestampToSet);
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
        this.timeSent = new SimpleLongProperty();
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

    // Pacchetto getProperty e non campi

    public SimpleLongProperty idProperty() {
        return (SimpleLongProperty) id;
    }

    public SimpleListProperty<User> receiverProperty() {
        return (SimpleListProperty<User>) receiver;
    }

    public SimpleStringProperty objectProperty() {
        return (SimpleStringProperty) object;
    }

    public SimpleStringProperty messageProperty() {
        return (SimpleStringProperty) message;
    }

    public SimpleBooleanProperty isSentProperty() {
        return (SimpleBooleanProperty) sent;
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

    public SimpleStringProperty listAddresses() {

        String mails = "";
        SimpleStringProperty addresses = new SimpleStringProperty();

        for (User receivingAddress : receiver) {
            mails += receivingAddress.getUsername() + "@Parallel.com; ";
            addresses.setValue(mails);
        }
        return addresses;
    }

    public void generateUUID() {

        UUID identification = UUID.randomUUID();
        long id = identification.getMostSignificantBits() & Long.MAX_VALUE;
        this.id.set(id);

    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeObject(getSender());
        List<User> receiver = new ArrayList<User>(getReceiver());
        out.writeObject(receiver);
        out.writeUTF(getObject()); // object to send
        out.writeUTF(getMessage()); //message to send
        out.writeBoolean(isSent());
        out.writeLong(getTimeSent());


    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        init();
        setId(in.readLong());
        setSender((User) in.readObject());
        List<User> toSet = (List<User>) in.readObject();
        setReceiver(toSet);
        setObject(in.readUTF());
        setMessage(in.readUTF());
        setSent(in.readBoolean());
        setTimeSent(in.readLong());
    }
}
