package server;

import models.Mail;
import models.Response;
import models.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: Mutua esclusione
public class FileManager {


    private static final String workingDir = System.getProperty("user.dir") + File.separator + "persistance";

    public static final String INBOX_NAME = "Inbox";
    public static final String OUTBOX_NAME = "Outbox";

    private static final String usersFile = "users.txt";

    /**
     * Send a mail from sender to a list of receivers
     *
     * @param mail mail to be sent
     * @return Response object as:
     * 0: send successful
     * -1: Wrong sender
     * -2: Wrong receiver
     */
    public static synchronized Response sendMail(Mail mail) {
        ObjectOutputStream objectOut;

        User sender = mail.getSender();
        List<User> receiver = mail.getReceiver();
        String senderPath = workingDir + File.separator + sender.getUsername() + File.separator;

        File senderDir = new File(senderPath);

        if(!userExists(sender)) return new Response(-1, "Wrong Sender");
        if(receiver.isEmpty()) return new Response(-1, "Receiver cannot be empty");

        if (!senderDir.exists()) {
            senderDir.mkdir();
            new File(senderPath + INBOX_NAME).mkdir();
            new File(senderPath + OUTBOX_NAME).mkdir();
        }
        for (User u : receiver) {
            String receiverPath = workingDir + File.separator + File.separator + u.getUsername() + File.separator;
            File receiverDir = new File(receiverPath);

            if (!userExists(u)) return new Response(-1, "One or more receiver does not exists");

            if (!receiverDir.exists()) {
                receiverDir.mkdir();
                new File(receiverPath + INBOX_NAME).mkdir();
                new File(receiverPath + OUTBOX_NAME).mkdir();
            }
        }

        try {

            FileOutputStream fileOut = new FileOutputStream(workingDir + File.separator + sender.getUsername()
                    + File.separator + OUTBOX_NAME + File.separator + mail.getId() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);
            mail.setSent(true);
            objectOut.writeObject(mail);
            objectOut.close();

            for (User u : receiver) {
                fileOut = new FileOutputStream(workingDir + File.separator + u.getUsername()
                        + File.separator + INBOX_NAME + File.separator + mail.getId() + ".txt");
                objectOut = new ObjectOutputStream(fileOut);
                mail.setSent(false);
                objectOut.writeObject(mail);
                objectOut.close();

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Response(0, "Mail sent successfully");
    }


    /**
     * Get the incoming or upcoming messages filename of a user
     *
     * @param user User owner of the messages
     * @param type type of messages: can be INBOX_NAME or OUTBOX_NAME
     * @return list of string (filenames) of messages
     */
    private static synchronized List<String> getExistingMails(User user, String type) {
        String username = user.getUsername();
        List<String> textFiles = new ArrayList<>();
        File dir = new File(workingDir + File.separator + username + File.separator + type);
        if (dir.listFiles() == null) return textFiles;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith((".txt"))) {
                textFiles.add(file.getName());
            }
        }
        return textFiles;
    }

    /**
     * Get the number of inbox elements. useful for syncing logged clients
     *
     * @param user user owner of the messages
     * @return number of inbox elements
     */
    public static int getNumberInbox(User user) {
        return getExistingMails(user, INBOX_NAME).size();
    }

    /**
     * Get the messages of a user (incoming or upcoming)
     *
     * @param user user owner of the messages
     * @param type type of messages: can be INBOX_NAME or OUTBOX_NAME
     * @return a list of mails according type value (incoming or upcoming list)
     */
    public static synchronized List<Mail> readMail(User user, String type) {
        String username = user.getUsername();
        List<String> mails = getExistingMails(user, type);
        List<Mail> retList = new ArrayList<>();

        Mail o;
        ObjectInputStream objectOut = null;

        for (String mailPath : mails) {
            String m = workingDir + File.separator + username + File.separator + type + File.separator + mailPath;
            try {
                objectOut = new ObjectInputStream(new FileInputStream(m));
                o = (Mail) objectOut.readObject();
                retList.add(o);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (objectOut != null) {
                    try {
                        objectOut.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return retList;
    }


    /**
     * @param user          user owner of the message
     * @param deleteMessage message to be deleted
     * @return Response object as:
     * 0: delete successfully
     * -1: generic error
     */
    public static synchronized Response deleteMessage(User user, Mail deleteMessage) {
        Response r = new Response(0, "Message deleted successfully");

        String path = deleteMessage.isSent() ? OUTBOX_NAME : INBOX_NAME;

        try {
            File toDelete = new File(workingDir + File.separator + user.getUsername() +
                    File.separator + path + File.separator + deleteMessage.getId() + ".txt");
            if (!toDelete.delete()) {

                r.setResponseCode(-1);
                r.setResponseText("Error deleting the file");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            r.setResponseCode(-1);
            r.setResponseText("Error deleting the file");
        }
        return r;
    }

    /**
     * Check if an user exists
     *
     * @param user user to be checked
     * @return true if user exists, false otherwise
     */
    public static synchronized boolean userExists(User user) {
        File userList = new File(workingDir + File.separator + usersFile);

        try (BufferedReader br = new BufferedReader(new FileReader(userList))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (user.getUsername().equals(line))
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
