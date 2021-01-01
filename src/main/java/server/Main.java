package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        runServerThread();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/ServerGUI.fxml"));
        primaryStage.setTitle("MailingService");
        primaryStage.setScene(new Scene(root, 600, 395));

        primaryStage.show();


    }


    private void runServerThread() {
        Thread t = new Thread(() -> {
            try {
                ServerSocket s = new ServerSocket(8189);

                while (true) {
                    Socket incoming = s.accept(); // si mette in attesa di una richiesta di connessione e la apre

                    Runnable r = new ServerThread(incoming, new User("user1"));

                    new Thread(r).start();
                }
            }
            catch (IOException e) {e.printStackTrace();}

        });
        // Close process when close GUI
        t.setDaemon(true);
        t.start();
    }


    public static void main(String[] args) { launch(args); }
}




