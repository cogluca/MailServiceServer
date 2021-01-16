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
     * Runnable run thread.
     * Handle server functioning. Get, dispatch and serves a command from a given client.
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
                    if (loggedUser == null) return;

                    Mail getMail = (Mail) inStream.readObject();
                    Platform.runLater(() -> serverModel.addLog(loggedUser.getUsername() + " trying to send a mail to " + getMail.getReceiver().toString()));

                    response = FileManager.sendMail(getMail);

                    outStream.writeObject(response);
                    outStream.flush();

                    Response finalResponse = response;
                    Platform.runLater(() -> serverModel.addLog(finalResponse.getResponseText()));

                    break;
                }
                case "READ INBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) return;

                    Platform.runLater(() -> serverModel.addLog("Sending inbox of " + loggedUser.getUsername()));

                    List<Mail> inboxMail = FileManager.readMail(loggedUser, FileManager.INBOX_NAME);
                    outStream.writeObject(inboxMail);
                    outStream.flush();

                    break;
                }
                case "READ OUTBOX": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) return;

                    List<Mail> outboxMail = FileManager.readMail(loggedUser, FileManager.OUTBOX_NAME);

                    Platform.runLater(() -> serverModel.addLog("Invio l'outbox di " + loggedUser.getUsername()));
                    outStream.writeObject(outboxMail);
                    outStream.flush();

                    break;
                }
                case "SYNC": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) return;

                    int clientCount = inStream.readInt();
                    int inboxCount = FileManager.getNumberInbox(loggedUser);
                    int retCount = clientCount != inboxCount ? -1 : 0;

                    outStream.writeInt(retCount);
                    outStream.flush();

                    break;
                }

                case "DELETE": {

                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) return;


                    Mail deleteMail = (Mail) inStream.readObject();

                    try {
                        response = FileManager.deleteMessage(loggedUser, deleteMail);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        response = new Response(-2, "Error deleting the mail");
                    }

                    outStream.writeObject(response);
                    outStream.flush();

                    Response finalResponse = response;
                    Platform.runLater(() -> serverModel.addLog(finalResponse.getResponseText()));

                    break;
                }

                case "LOGOUT": {
                    User loggedUser = serverModel.retrieveUser(sessID);
                    if (loggedUser == null) return;


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