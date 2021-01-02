package server;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.ServerModel;
import models.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ServerCloneException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main extends Application {

    private ServerModel serverModel;
    private ServerGUIController serverController;


    @Override
    public void start(Stage primaryStage) throws Exception {

        serverModel = new ServerModel();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ServerGUI.fxml"));
        primaryStage.setTitle("MailingService");
        primaryStage.setScene(new Scene(loader.load(), 600, 395));
        serverController = loader.getController();
        serverController.setModel(serverModel);
        primaryStage.show();

        runServerThread();

    }


    private void runServerThread() {
        Thread t = new Thread(() -> {
            try {
                ServerSocket s = new ServerSocket(8189);
                //addLog("Server partito");
                serverModel.addLog("Server partito");
                while (true) {
                    Socket incoming = s.accept(); // si mette in attesa di una richiesta di connessione e la apre

                    Runnable r = new ServerThread(this, incoming, new User("user1"), serverModel);

                    new Thread(r).start();
                }
            }
            catch (IOException e) {e.printStackTrace();}

        });
        // Close process when close GUI
        t.setDaemon(true);
        t.start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}




