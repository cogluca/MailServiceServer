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


    @Override
    public String toString() {
        return "Response{" +
                "errorCode=" + responseCode +
                ", errorText='" + responseText + '\'' +
                '}';
    }
}
