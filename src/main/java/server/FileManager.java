package server;

import models.Mail;
import models.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Implementare mutua esclusione
public class FileManager {


    private static final String filepath= System.getProperty("user.dir") + "\\persistance\\";

    private static final String INBOX_NAME = "Inbox";
    private static final String OUTBOX_NAME = "Outbox";

    public static void main(String[] args) {


    }

    /**
     * Send a mail from sender to receiver
     * @param mail
     * @return integer with status code:
     *         0: send successful
     *         -1: Wrong sender
     *         -2: Wrong receiver
     */
    public static synchronized int sendMail(Mail mail) {
        ObjectOutputStream objectOut;

        User sender = mail.getSender();

        List<User> receiver = mail.getReceiver();

        File senderDir = new File(filepath + "\\" + sender.getUsername());
        if (userExists(sender)) {
            if(!senderDir.exists()) {
                senderDir.mkdir();
                new File(filepath + "\\" + sender.getUsername() + "\\" + INBOX_NAME).mkdir();
                new File(filepath + "\\" + sender.getUsername() + "\\" + OUTBOX_NAME).mkdir();
            }
        }
        else {
            System.out.println("ERROR: WRONG SENDER");
            return -1;
        }

        for (User u : receiver) {
            File receiverDir = new File(filepath + "\\" + u.getUsername());
            if(userExists(u)) {
                if (!receiverDir.exists()) {
                    receiverDir.mkdir();
                    new File(filepath + "\\" + u.getUsername() + "\\" + INBOX_NAME).mkdir();
                    new File(filepath + "\\" + u.getUsername() + "\\" + OUTBOX_NAME).mkdir();
                }
            }
            else {
                System.out.println("ERROR: WRONG RECEIVER");
                return -2;
            }
        }

        try {

            // Invia il messaggio nella casella degli
            FileOutputStream fileOut = new FileOutputStream(filepath + "\\" + sender.getUsername() + "\\" + OUTBOX_NAME + "\\" + System.currentTimeMillis() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(mail);
            objectOut.close();

            for(User u : receiver) {
                fileOut = new FileOutputStream(filepath + "\\" + u.getUsername() + "\\"  + INBOX_NAME + "\\" + System.currentTimeMillis() + ".txt");
                objectOut = new ObjectOutputStream(fileOut);
                objectOut.writeObject(mail);
                objectOut.close();

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static synchronized List<String> getNameMail(String username) {
        List<String> textFiles = new ArrayList<String>();
        File dir = new File(filepath +"\\" + username + "\\" +  INBOX_NAME );
        if(dir.listFiles() == null) return textFiles;
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".txt"))) {
                textFiles.add(file.getName());
            }
        }
        return textFiles;
    }

    /*
    * Se sta scrivendo, teoricamente dovresti aspettare per leggere!!!
    */
    public static synchronized List<Mail> readInbox(String username) {
        List<Mail> retList = new ArrayList<>();
        List<String> mails = getNameMail(username);
        System.out.println(mails.size());

        Mail o;
        ObjectInputStream objectOut;

        for (String mailPath : mails) {
            try {
                String m = filepath + "\\" + username + "\\" + INBOX_NAME + "\\" + mailPath;
                System.out.println(m);
                FileInputStream fileOut = new FileInputStream(m);
                objectOut = new ObjectInputStream(fileOut);
                o = (Mail)objectOut.readObject();
                retList.add(o);
                objectOut.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return retList;
    }

    public static synchronized List<Mail> readOutbox (String username) {

        List<Mail> retOutboxList = new ArrayList<>();
        List<String> mails = getNameMail(username);

        FileInputStream fetchStream = null;
        ObjectInputStream fetchFile = null;

        Mail cachedMail;

        for (String outmail : mails) {
            try {
                String path = filepath + "\\" + username +"\\" + OUTBOX_NAME + "\\" + outmail;
                fetchStream = new FileInputStream(path);
                fetchFile = new ObjectInputStream(fetchStream);
                cachedMail = (Mail) fetchFile.readObject();
                retOutboxList.add(cachedMail);

            }
            catch(IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
            finally {
                try {
                    if (fetchFile != null)
                        fetchFile.close();
                    if (fetchStream != null)
                        fetchStream.close();
                }
                catch (IOException ex){
                    System.out.println(ex.getMessage());
                }
            }
        }
        return retOutboxList;
    }

    public static synchronized int deleteMessage (String mailId, String user) {
        try {
            File toDelete = new File(filepath + "\\" + user + "\\" + mailId);
            toDelete.delete();
            System.out.println("Successfully deleted file");
        } catch (NullPointerException e) {
            System.out.println("Unsuccessfully deleted file ");
            System.out.println(e.getMessage());
            return -1;
        }
        return 1;
    }

    public static synchronized boolean userExists (User user) {
        File userList = new File(filepath + "\\users.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(userList))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(user.getUsername().equals(line))
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;
    }
}
