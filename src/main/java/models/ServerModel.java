package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ServerModel {

    private ObservableList<String> logList = FXCollections.observableArrayList();
    private ObservableList<String> loggedUserList = FXCollections.observableArrayList();

    private Map<String, User> session_store = new HashMap<>();

    public ObservableList<String> getLogList() {
        return logList;
    }

    public ObservableList<String> getLoggedUserList() {
        return loggedUserList;
    }

    public void createSession(String sessionID, User user) {
        session_store.put(sessionID, user);
        System.out.println(session_store.toString());
    }

    public void destroySession(String sessionID) {
        session_store.remove(sessionID);
    }

    public User retrieveUser(String sessionID) {
        return session_store.getOrDefault(sessionID, null);
    }

    public void addLog(String log) {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());

        logList.add("[" + timeStamp + "] " + log);
    }

    public void addUser(String user) {
        loggedUserList.add(user);

    }

    public void removeUser(String user) {
        if (loggedUserList.contains(user))
            loggedUserList.remove(user);
    }

}
