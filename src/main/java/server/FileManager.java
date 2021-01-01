package server;

import models.Mail;

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

        ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
        for (int i = 0; i < 10; i++) {
            final int finali = i;
            Runnable worker = () -> {
                Mail f = new Mail(Integer.toString(finali));
                f.setFrom("user1");
                f.setTo("user1");

                //sendMail(f);
                System.out.println("Leggo: ");
                for(Mail m : readInbox("user1")) {
                    System.out.print(m.toString() + " , " );

                }
                System.out.println("");

            };

            executor.execute(worker);//calling execute method of ExecutorService
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        executor.shutdown();

        while (!executor.isTerminated()) {   }

        System.out.println("Finished all threads");

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

        String sender = mail.getFrom();

        String receiver = mail.getTo();

        File senderDir = new File(filepath + "\\" + sender);
        if (userExists(sender)) {
            if(!senderDir.exists()) {
                senderDir.mkdir();
                new File(filepath + "\\" + sender + "\\" + INBOX_NAME).mkdir();
                new File(filepath + "\\" + sender + "\\" + OUTBOX_NAME).mkdir();
            }
        }
        else {
            System.out.println("ERROR: WRONG SENDER");
            return -1;
        }

        //TODO: Handle multiple receivers
        File receiverDir = new File(filepath + "\\" + receiver);
        if(userExists(receiver)) {
            if (!receiverDir.exists()) {
                receiverDir.mkdir(); // && isValidUser
                new File(filepath + "\\" + receiver + "\\" + INBOX_NAME).mkdir();
                new File(filepath + "\\" + receiver + "\\" + OUTBOX_NAME).mkdir();
            }
        }
        else {
            System.out.println("ERROR: WRONG RECEIVER");
            return -2;
        }

        try {

            // Invia il messaggio nella casella degli
            FileOutputStream fileOut = new FileOutputStream(filepath + "\\" + sender + "\\" + OUTBOX_NAME + "\\" + System.currentTimeMillis() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(mail);
            objectOut.close();

            fileOut = new FileOutputStream(filepath + receiver + "\\"  + INBOX_NAME + "\\" + System.currentTimeMillis() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(mail);
            objectOut.close();


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


    public static synchronized boolean userExists (String user) {
        File userList = new File(filepath + "\\users.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(userList))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(user.equals(line))
                    return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;
    }
}
