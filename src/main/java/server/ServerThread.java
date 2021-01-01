package server;


import models.Mail;
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
    private int counter;

    private User user;

    /**
     * Constructs a handler.
     *
     * @param in the incoming socket
     */
    public ServerThread(Socket in, User user) {
        incoming = in;
        this.user = user;
    }

    private int sendMessage(Mail message) {
        return FileManager.sendMail(message);

    }

    private List<Mail> getMessages(boolean type) {
        if (type)
            return FileManager.readInbox(user.getUsername());
        else //read outbox
            return FileManager.readOutbox(user.getUsername());

    }

    public void run() {

        ObjectInputStream inStream = null;
        ObjectOutputStream outStream = null;
        try {
            inStream = new ObjectInputStream(incoming.getInputStream());

            String command = inStream.readUTF();
            System.out.println("command: " + command);

            switch (command) {
                case "SEND":
                    Mail getMail = (Mail) inStream.readObject();
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

                    outStream = new ObjectOutputStream(incoming.getOutputStream());

                    outStream.writeUTF(result);
                    outStream.flush();

                    break;
                case "READ INBOX":
                    System.out.println("Invio i l'inbox");
                    List<Mail> inboxMail = this.getMessages(true);

                    outStream = new ObjectOutputStream(incoming.getOutputStream());
                    outStream.writeObject(inboxMail);
                    outStream.flush();

                    break;
                case "READ OUTBOX":
                    System.out.println("Invio i l'outbox");
                    List<Mail> outboxMail = this.getMessages(false);

                    outStream = new ObjectOutputStream(incoming.getOutputStream());
                    outStream.writeObject(outboxMail);
                    outStream.flush();

                    break;

                case "DELETE":
                    break;

                default:
                    // error: wrong command!!!
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
                    if(inStream != null)
                        inStream.close();
                    if(outStream != null)
                        outStream.close();
                    incoming.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

}