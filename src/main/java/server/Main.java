package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Mail;
import models.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Thread t = new Thread(() -> {
            try {
                int i = 1;
                ServerSocket s = new ServerSocket(8189);

                while (true) {
                    Socket incoming = s.accept(); // si mette in attesa di richiesta di connessione e la apre
                    System.out.println("Spawning " + i);
                    Runnable r = new ThreadedEchoHandler(incoming, i, new User("user1"));
                    new Thread(r).start();
                    i++;
                }
            }
            catch (IOException e) {e.printStackTrace();}

        });
        // Close process when close GUI
        t.setDaemon(true);
        t.start();


        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));

        primaryStage.show();

    }



    public static void main(String[] args) { launch(args); }
}




/**
 This class handles the client input for one server socket connection.
 */
class ThreadedEchoHandler implements Runnable {

    private Socket incoming;
    private int counter;

    private User user;
    private ExecutorService executor;

    /**
     Constructs a handler.
     @param in the incoming socket
     @param c the counter for the handlers (used in prompts)
     */
    public ThreadedEchoHandler(Socket in, int c, User user) {
        incoming = in;
        counter = c;
        this.user = user;
    }

    private void sendMessage(Mail message) {
        executor.execute(() -> FileManager.sendMail(message));
    }

    private List<Mail> getMessages(boolean type) {
        Future<List<Mail>> retVal = null;
        if(type)
            retVal = executor.submit(() -> FileManager.readInbox(user.getUsername()));
        else //read outbox
            retVal = executor.submit(() -> FileManager.readInbox(user.getUsername()));

        try {
            return retVal.get();
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }

    }

    public void run() {
        executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads

        try {
            try {
                InputStream inStream = incoming.getInputStream();
                OutputStream outStream = incoming.getOutputStream();

                Scanner in = new Scanner(inStream);
                PrintWriter out = new PrintWriter(outStream, true /* autoFlush */);

                out.println( "Hello! Enter BYE to exit." );

                // echo client input
                boolean done = false;
                while (!done && in.hasNextLine()) {
                    String line = in.nextLine();
                    if(line.equals("SEND")) {
                        out.println("Scrivi il messagio che vuoi inviare");
                        line = in.nextLine();
                        sendMessage(new Mail(line));
                        out.println("Message sent successfully");
                    }
                    else if(line.equals("READ INBOX")) {
                        out.print("Lista dei messaggi di user1:");
                        for (Mail m : this.getMessages(false)) {
                            out.println(m.toString());
                            System.out.print(m.toString());
                        }
                    }
                    else if (line.trim().equals("BYE"))
                        done = true;
                    else
                        out.println(line);
                }
            }
            finally {
                incoming.close();
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }

}

