package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ServerModel {

    private ObservableList<String> logList = FXCollections.observableArrayList();
    private ObservableList<String> loggedUserList = FXCollections.observableArrayList();

    private Map<String, User> sessions = new HashMap<>();

    public ObservableList<String> getLogList() {
        return logList;
    }


    public ObservableList<String> getLoggedUserList() {
        return loggedUserList;
    }

    public void addLog(String log) {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());

        logList.add("[" + timeStamp + "] " + log);
    }

    public void addUser(String user) {
        loggedUserList.add(user);

    }

    public void removeUser(String user) {
        loggedUserList.remove(user);
    }


    public void createSession(String sessionID, User user) {
        sessions.put(sessionID, user);
    }

    public void destroySession(String sessionID) {
        sessions.remove(sessionID);
    }

    public User retrieveUser(String sessionID) {
        return sessions.getOrDefault(sessionID, null);
    }

}
