package server;


import javafx.application.Platform;
import models.Mail;
import models.ServerModel;
import models.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * This class handles the client input for one server socket connection.
 */
class ServerThread implements Runnable {

    private Socket incoming;

    private ServerModel serverModel;

    /**
     * Constructs a handler.
     *
     * @param in the incoming socket
     */
    public ServerThread(Socket in, ServerModel model) {
        incoming = in;
        this.serverModel = model;
    }


    private List<Mail> getMessages(User user, boolean type) {
        if (type)
            return FileManager.readInbox(user.getUsername());
        else
            return FileManager.readOutbox(user.getUsername());

    }

    private boolean login(User user) {
        return FileManager.userExists(user);
    }

    public void run() {
        ObjectInputStream inStream = null;
        ObjectOutputStream outStream = null;
        try {
            outStream = new ObjectOutputStream(incoming.getOutputStream());
            outStream.flush();

            inStream = new ObjectInputStream(incoming.getInputStream());

            String command = inStream.readUTF();
            System.out.println("command: " + command);
            String sessID = inStream.readUTF();

            switch (command) {
                case "LOGIN": {

                    User user = (User)inStream.readObject();
                    Platform.runLater(() -> serverModel.addLog("Attempt to login from " + user.getUsername()));
                    String result = "";
                    if(login(user)) {
                        result = "Login successfully";
                        Platform.runLater(() -> {
                            serverModel.createSession(sessID, user);
                            serverModel.addUser(user.getUsername());
                        });
                    }
                    else {
                        result = ("User does not exists");
                    }

                    outStream.writeUTF(result);
                    outStream.flush();

                    String finalResult = result;
                    Platform.runLater(() -> serverModel.addLog(finalResult));

                    break;
                }

                case "SEND": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }


                    Mail getMail = (Mail) inStream.readObject();
                    Platform.runLater(() -> serverModel.addLog(loggedUser.getUsername() + " trying to send a mail to " + getMail.getReceiver().toString()));

                    int status = FileManager.sendMail(getMail);
                    String result;
                    switch (status) {
                        case 0:
                            result = "Send successful";
                            break;
                        case -1:
                            result = "Wrong sender";
                            break;
                        case -2:
                            result = "Wrong receiver";
                            break;
                        default:
                            result = "An error occurred sending the mail";
                            break;
                    }
                    Platform.runLater(() -> serverModel.addLog(result));
                    outStream.writeUTF(result);
                    outStream.flush();

                    break;
                }
                case "READ INBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }
                    Platform.runLater(() -> serverModel.addLog("Invio l'inbox di " + loggedUser.getUsername()));

                    List<Mail> inboxMail = this.getMessages(loggedUser, true);

                    outStream.writeObject(inboxMail);
                    outStream.flush();

                    break;
                }
                case "READ OUTBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }
                    List<Mail> outboxMail = this.getMessages(loggedUser, false);
                    Platform.runLater(() -> serverModel.addLog("Invio l'outbox di " + loggedUser.getUsername()));


                    outStream.writeObject(outboxMail);
                    outStream.flush();

                    break;
                }
                case "SYNC": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }

                    int retCount = 0;
                    int clientCount = inStream.readInt();

                    int inboxCount = FileManager.getNumberInbox(loggedUser.getUsername());
                    if(clientCount != inboxCount)
                        retCount = -1;

                    //Platform.runLater(() -> serverModel.addLog("Richiesta di sync da parte di " +
                    //        loggedUser.getUsername()));

                    outStream.writeInt(retCount);
                    outStream.flush();

                    break;
                }

                case "DELETE": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }

                    Mail deleteMail = (Mail) inStream.readObject();
                    System.out.println(deleteMail);
                    int deleteStatus = FileManager.deleteMessage(loggedUser, deleteMail);
                    String retStatus;
                    switch (deleteStatus) {
                        case 0:
                            retStatus = "Delete successful";
                            break;
                        default:
                            retStatus = "Not existing mail";
                            break;
                    }
                    Platform.runLater(() -> serverModel.addLog(retStatus));


                    outStream.writeUTF(retStatus);
                    outStream.flush();
                    break;
                }

                case "LOGOUT": {
                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
                        // TODO: NOT AUTHORIZED USER. CLOSE CONNECTION THE CONNECTION
                        return;
                    }

                    Platform.runLater(() -> {
                        serverModel.addLog(loggedUser.getUsername() + " disconnected from server");
                        serverModel.removeUser(loggedUser.getUsername());
                        serverModel.destroySession(sessID);
                    }

                    );


                    break;
                }

                default:
                    // TODO: Handle better this error
                    System.out.println("Wrong command");
                    break;
            }

        } catch (SocketException se) {
            System.exit(0);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (incoming != null)
                try {
                    if(inStream != null) inStream.close();
                    if(outStream != null) outStream.close();
                    incoming.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

}