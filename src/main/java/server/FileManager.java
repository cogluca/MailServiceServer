package server;

import models.Mail;
import models.Response;
import models.User;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileManager {


    private static final String workingDir = System.getProperty("user.dir") + File.separator + "persistance";

    public static final String INBOX_NAME = "Inbox";
    public static final String OUTBOX_NAME = "Outbox";

    private static final String usersFile = "users.txt";


    public static FileChannel getLockExclusive(String path) throws IOException {
        return FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    public static FileChannel getLockShared(String path) throws IOException {
        return FileChannel.open(Paths.get(path), StandardOpenOption.READ);
    }


    private static boolean createUserFolder(String basePath) throws IOException {
        boolean ret = new File(basePath).mkdir();
        ret = ret && new File(basePath + INBOX_NAME).mkdir();
        ret = ret && new File(basePath + INBOX_NAME + File.separator + "lock").createNewFile();
        ret = ret && new File(basePath + OUTBOX_NAME).mkdir();
        ret = ret && new File(basePath + OUTBOX_NAME + File.separator + "lock").createNewFile();

        return ret;
    }

    /**
     * Send a mail from sender to a list of receivers
     *
     * @param mail mail to be sent
     * @return Response object as:
     * 0: send successful
     * -1: Wrong sender
     * -2: Wrong receiver
     */
    public static synchronized Response sendMail(Mail mail) throws IOException {

        ObjectOutputStream objectOut;
        User sender = mail.getSender();
        List<User> receiver = mail.getReceiver();
        String senderPath = workingDir + File.separator + sender.getUsername() + File.separator;

        if (!userExists(sender)) return new Response(-1, "Wrong Sender");
        if (receiver.isEmpty()) return new Response(-1, "Receiver cannot be empty");

        if (Files.notExists(Paths.get(senderPath)))
            if(!createUserFolder(senderPath))
                return new Response(-2, "Error in file system");

        // check if user exists
        for (User u : receiver) {
            String receiverPath = workingDir + File.separator + u.getUsername() + File.separator;

            if (!userExists(u)) return new Response(-1, "One or more receiver does not exists");

            if (Files.notExists(Paths.get(receiverPath)))
                if(!createUserFolder(receiverPath))
                    return new Response(-2, "Error in file system");

        }


        // sender user write
        FileChannel fileLock = getLockShared(senderPath +
                OUTBOX_NAME + File.separator + "lock");
        fileLock.lock(0, Long.MAX_VALUE, true);

        mail.generateUUID();

        FileOutputStream fileOut = new FileOutputStream(senderPath +
                OUTBOX_NAME + File.separator + mail.getId() + ".txt");
        objectOut = new ObjectOutputStream(fileOut);
        mail.setSent(true);

        objectOut.writeObject(mail);
        objectOut.close();

        fileLock.close();


        // receivers user write
        mail.setSent(false);
        for (User u : receiver) {
            String receiverPath = workingDir + File.separator + u.getUsername() + File.separator;
            mail.generateUUID();

            fileLock = getLockShared(receiverPath + INBOX_NAME + File.separator + "lock");
            fileLock.lock(0, Long.MAX_VALUE, true);

            fileOut = new FileOutputStream(receiverPath + INBOX_NAME + File.separator + mail.getId() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);

            objectOut.writeObject(mail);
            objectOut.close();

            fileLock.close();

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
    private static synchronized List<String> getExistingMails(User user, String type) throws IOException {

        List<String> textFiles = new ArrayList<>();
        String userPath = workingDir + File.separator + user.getUsername() + File.separator;

        if (Files.notExists(Paths.get(userPath))) {
            createUserFolder(userPath);
            return textFiles;
        }


        FileChannel fileLock = getLockExclusive(userPath + type + File.separator + "lock");
        fileLock.lock();

        File dir = new File(userPath + type);

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith((".txt"))) {
                textFiles.add(file.getName());
            }
        }

        fileLock.close();

        return textFiles;
    }

    /**
     * Get the number of inbox elements. useful for syncing logged clients
     *
     * @param user user owner of the messages
     * @return number of inbox elements
     */
    public static int getNumberInbox(User user) throws IOException {
        return getExistingMails(user, INBOX_NAME).size();
    }

    /**
     * Get the messages of a user (incoming or upcoming)
     *
     * @param user user owner of the messages
     * @param type type of messages: can be INBOX_NAME or OUTBOX_NAME
     * @return a list of mails according type value (incoming or upcoming list)
     */
    public static synchronized List<Mail> readMail(User user, String type) throws IOException, ClassNotFoundException {
        List<String> mails = getExistingMails(user, type);
        List<Mail> retList = new ArrayList<>();
        Mail o;
        ObjectInputStream objectOut;

        String userPath = workingDir + File.separator + user.getUsername() + File.separator + type + File.separator;

        FileChannel fileLock = getLockExclusive(userPath + "lock");
        fileLock.lock();

        for (String mail : mails) {

            objectOut = new ObjectInputStream(new FileInputStream(userPath + mail));
            o = (Mail) objectOut.readObject();
            retList.add(o);
            objectOut.close();

        }

        fileLock.close();
        return retList;
    }


    /**
     * @param user          user owner of the message
     * @param deleteMessage message to be deleted
     * @return Response object as:
     * 0: delete successfully
     * -1: generic error
     */
    public static synchronized Response deleteMessage(User user, Mail deleteMessage) throws IOException {

        Response r = new Response(0, "Message deleted successfully");

        String path = deleteMessage.isSent() ? OUTBOX_NAME : INBOX_NAME;
        String userPath = workingDir + File.separator + user.getUsername() + File.separator;

        if (Files.notExists(Paths.get(userPath))) {
            if (Files.notExists(Paths.get(userPath)))
                if(!createUserFolder(userPath))
                    return new Response(-2, "Error in file system");

            return new Response(-1, "Message does not exists");
        }

        FileChannel fileLock = getLockShared(userPath + path + File.separator + "lock");
        fileLock.lock(0, Long.MAX_VALUE, true);

        File toDelete = new File(userPath + path + File.separator + deleteMessage.getId() + ".txt");
        if (!toDelete.delete()) {
            r = new Response(-1,"Error deleting the file");
        }

        fileLock.close();

        return r;
    }

    /**
     * Check if an user exists
     *
     * @param user user to be checked
     * @return true if user exists, false otherwise
     */
    public static boolean userExists(User user) throws IOException {
        File userList = new File(workingDir + File.separator + usersFile);
        String line;

        BufferedReader br = new BufferedReader(new FileReader(userList));
        while ((line = br.readLine()) != null) {
            if (user.getUsername().equals(line))
                return true;
        }

        return false;
    }
}
