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



    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ServerGUI.fxml"));
        primaryStage.setTitle("MailingService");
        primaryStage.setScene(new Scene(loader.load(), 600, 395));
        primaryStage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }
}




