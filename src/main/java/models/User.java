package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
// TODO: add timestamp
public class User implements Externalizable {

    private static final long serialVersionUID = -3579562875503665712L;
    private transient StringProperty username;

    public User(String username) {
        this.username = new SimpleStringProperty(username);
    }

    public User() {
        init();
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    private void init() {
        this.username = new SimpleStringProperty();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeUTF(getUsername());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        init();
        setUsername(in.readUTF());
    }
}
