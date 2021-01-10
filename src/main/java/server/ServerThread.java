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
                    String result = "";

                    if (FileManager.userExists(user)) {

                        result = "Login successfully";
                        response = new Response(0, "Login successfully");
                        Platform.runLater(() -> {
                            serverModel.createSession(sessID, user);
                            serverModel.addUser(user.getUsername());
                        });
                    } else {
                        response = new Response(-1, "User does not exists");
                        result = ("User does not exists");
                    }

                    // SCOMMENTA QUESTO E TOGLI IL WRITE UTF
                    outStream.writeObject(response);
                    //outStream.writeUTF(result);
                    outStream.flush();

                    String finalResult = result;
                    Platform.runLater(() -> serverModel.addLog(finalResult));

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
                    String result;
                    switch (response.getResponseCode()) {
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
                    System.out.println(response.getResponseText());
                    // SCOMMENTA QUESTO E TOGLI IL WRITE UTF
                    outStream.writeObject(response);
                    //outStream.writeUTF(result);
                    outStream.flush();

                    break;
                }
                case "READ INBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if(loggedUser == null) {
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
                    if(loggedUser == null) {
                        outStream.writeObject(handleNotAuthorized());
                        return;
                    }
                    List<Mail> outboxMail = FileManager.readMail(loggedUser, FileManager.OUTBOX_NAME);

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

                    int inboxCount = FileManager.getNumberInbox(loggedUser);
                    if (clientCount != inboxCount)
                        retCount = -1;

                    //Platform.runLater(() -> serverModel.addLog("Richiesta di sync da parte di " +
                    //        loggedUser.getUsername()));

                    outStream.writeInt(retCount);
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
                    response = FileManager.deleteMessage(loggedUser, deleteMail);
                    String retStatus = "";
                    switch (response.getResponseCode()) {
                        case 0:
                            response.setResponseText("Delete successful");
                            break;
                        default:
                            response.setResponseText("Non existent mail");
                            break;
                    }
                    Platform.runLater(() -> serverModel.addLog(retStatus));

                    // SCOMMENTA QUESTO E TOGLI IL WRITE UTF
                    outStream.writeObject(response);
                    //outStream.writeUTF(retStatus);
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
                    response = new Response(-1, "Wrong command");
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