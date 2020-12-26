package server;

import models.Mail;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileManager {


    private static final String filepath="C:\\Users\\Alessio\\Desktop\\file\\";


    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads
        for (int i = 0; i < 10; i++) {
            final int finali = i;
            Runnable worker = () -> {
                Mail f = new Mail(Integer.toString(finali));

               // sendMail(f);
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
     *
     * @param obj
     */
    public static synchronized void sendMail(Object obj) {
        ObjectOutputStream objectOut;

        File directory = new File(filepath + "\\user1");
        if(!directory.exists()) {
            directory.mkdir();
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(filepath + "\\user1\\" + System.currentTimeMillis() + ".txt");
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(obj);
            objectOut.close();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static synchronized List<String> getNameMail(String username) {
        List<String> textFiles = new ArrayList<String>();
        File dir = new File("C:\\Users\\Alessio\\Desktop\\file\\" + username);
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

        Mail o;
        ObjectInputStream objectOut;

        for (String mailPath : mails) {
            try {
                String m = filepath + "\\" + username + "\\" + mailPath;
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

}
