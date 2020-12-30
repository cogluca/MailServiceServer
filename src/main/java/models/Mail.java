package models;

import java.io.Serializable;

public class Mail implements Serializable {

    private String property = "1";

    public Mail(String property) {
        this.property = property;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return this.property;
    }
}