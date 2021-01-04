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
import java.security.MessageDigest;
import java.util.List;

/**
 * This class handles the client input for one server socket connection.
 */
class ServerThread implements Runnable {

    private Socket incoming;
    private Main main;


    private ServerModel serverModel;

    /**
     * Constructs a handler.
     *
     * @param in the incoming socket
     */
    public ServerThread(Main m, Socket in, ServerModel model) {
        main = m;
        incoming = in;
        this.serverModel = model;
    }

    private int sendMessage(Mail message) {
        return FileManager.sendMail(message);

    }

    private List<Mail> getMessages(User user, boolean type) {
        if (type)
            return FileManager.readInbox(user.getUsername());
        else
            return FileManager.readOutbox(user.getUsername());

    }

    private int deleteMessage(Mail message) {
        // TODO: Add id value to Mail
        return FileManager.deleteMessage("as", message.getSender().getUsername());
    }

    private boolean login(User user) {
        return FileManager.userExists(user);
    }

    public void run() {
        System.out.println("Entro in questo thread");

        ObjectInputStream inStream = null;
        ObjectOutputStream outStream = null;
        try {
            System.out.println("Deadlock?");
            outStream = new ObjectOutputStream(incoming.getOutputStream());
            System.out.println("1NoDEADLOCK?");

            inStream = new ObjectInputStream(incoming.getInputStream());
            System.out.println("2NoDEADLOCK?");

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
                        serverModel.createSession(sessID, user);
                        serverModel.addUser(user.getUsername());
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

                    int status = sendMessage(getMail);
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
                    System.out.println("Invio l'inbox");
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
                    Platform.runLater(() -> serverModel.addLog("Invio l'outbox di XXX"));


                    outStream.writeObject(outboxMail);
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
                    int deleteStatus = sendMessage(deleteMail);
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
                    System.out.println("arrivato qui");
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