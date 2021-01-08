package models;

import java.io.Serializable;

public class Response implements Serializable {

    private int responseCode;

    private String responseText;

    public Response(int responseCode, String responseText) {
        this.responseCode = responseCode;
        this.responseText = responseText;
    }

    public Response() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    /**
     * Use this only if you should visualize an error.
     * In case you want only show a response from server, use
     * get
     */
    public String getError() {
        return "Error " + getResponseCode() + " occurred: " + getResponseText();
    }

    @Override
    public String toString() {
        return "Response{" +
                "errorCode=" + responseCode +
                ", errorText='" + responseText + '\'' +
                '}';
    }
}
