package server;

import javafx.application.Platform;
import models.Mail;
import models.Response;
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
     * ServerThread constructor
     */
    public ServerThread(Socket in, ServerModel model) {
        incoming = in;
        this.serverModel = model;
    }

    /**
     * Utility method. Raise a "not authorized user" error in case some user
     * tries to exploit the authentication. Basically, if doesn't exists the pair
     * <sess_id, user> given on authentication in the Map server model, raise this
     * error.
     * Also this mechanism will block not logged users
     *
     * @return a response object containing the error
     */
    private Response handleNotAuthorized() {
        return new Response(-5, "FATAL ERROR: Not authorized user");
    }

    /**
     * Runnable run thread.
     * Handle server functioning. Get, dispatch and servers a command from a given client.
     */
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
            Response response;

            switch (command) {
                case "LOGIN": {

                    User user = (User) inStream.readObject();
                    Platform.runLater(() -> serverModel.addLog("Attempt to login from " + user.getUsername()));

                    if (FileManager.userExists(user)) {

                        response = new Response(0, "Login successfully");
                        Platform.runLater(() -> {
                            serverModel.createSession(sessID, user);
                            serverModel.addUser(user.getUsername());
                        });
                    } else {
                        response = new Response(-1, "User does not exists");
                    }

                    outStream.writeObject(response);
                    outStream.flush();
                    Response finalResponse = response;
                    Platform.runLater(() -> serverModel.addLog(finalResponse.getResponseText()));

                    break;
                }

                case "SEND": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }

                    Mail getMail = (Mail) inStream.readObject();
                    Platform.runLater(() -> serverModel.addLog(loggedUser.getUsername() + " trying to send a mail to " + getMail.getReceiver().toString()));

                    System.out.println(getMail.getReceiver().toString());
                    response = FileManager.sendMail(getMail);

                    Response finalResponse = response;
                    Platform.runLater(() -> serverModel.addLog(finalResponse.getResponseText()));
                    outStream.writeObject(response);
                    outStream.flush();

                    break;
                }
                case "READ INBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }
                    Platform.runLater(() -> serverModel.addLog("Invio l'inbox di " + loggedUser.getUsername()));

                    List<Mail> inboxMail = FileManager.readMail(loggedUser, FileManager.INBOX_NAME);
                    outStream.writeObject(inboxMail);
                    outStream.flush();

                    break;
                }
                case "READ OUTBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }
                    List<Mail> outboxMail = null;
                    try {
                        outboxMail = FileManager.readMail(loggedUser, FileManager.OUTBOX_NAME);
                    }
                    catch (IOException e) {
                        response = new Response(-2, "Internal error. Contact the admin");
                        System.out.println(e.toString());
                        e.printStackTrace();
                    }

                    Platform.runLater(() -> serverModel.addLog("Invio l'outbox di " + loggedUser.getUsername()));


                    outStream.writeObject(outboxMail);
                    outStream.flush();

                    break;
                }
                case "SYNC": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }

                    int retCount = 0;
                    int clientCount = inStream.readInt();

                    response = new Response(0, "Synced mails");
                    int inboxCount = clientCount;

                    try {
                        inboxCount = FileManager.getNumberInbox(loggedUser);
                    }
                    catch (IOException e) {
                        response = new Response(-2, "Internal error. Contact the admin");
                        System.out.println(e.toString());
                    }

                    if (clientCount != inboxCount) {
                        retCount = -1;
                        response = new Response(-1, "Not synced mails");
                    }

                    outStream.writeInt(retCount);
                    //outStream.writeObject(response);
                    outStream.flush();

                    break;
                }

                case "DELETE": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }

                    Mail deleteMail = (Mail) inStream.readObject();
                    try {
                        response = FileManager.deleteMessage(loggedUser, deleteMail);
                    }
                    catch (IOException e) {
                        System.out.println(e.getMessage());
                        response = new Response(-2, "can't delete the file");
                    }
                    Response finalResponse = response;
                    Platform.runLater(() -> serverModel.addLog(finalResponse.getResponseText()));

                    outStream.writeObject(response);
                    outStream.flush();
                    break;
                }

                case "LOGOUT": {
                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
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
                    if (inStream != null) inStream.close();
                    if (outStream != null) outStream.close();
                    incoming.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

}