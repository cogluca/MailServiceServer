package server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import models.ServerModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerGUIController implements Initializable {

    @FXML
    private ListView<String> accountView;

    @FXML
    private ListView<String> serverLogView;

    private ServerModel serverModel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverModel = new ServerModel();
        serverLogView.setItems(serverModel.getLogList());

        accountView.setItems(serverModel.getLoggedUserList());
        runServerThread();
    }


    public void runServerThread() {

        Thread serverThread = new Thread(() -> {
            try {
                Executor executor = Executors.newFixedThreadPool(6);
                ServerSocket s = new ServerSocket(8189);
                serverModel.setServerSocket(s);
                serverModel.addLog("Server started");
                while (true) {

                    Socket incoming = s.accept();

                    executor.execute(new ServerThread(incoming, serverModel));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        serverThread.setDaemon(true);
        serverThread.start();
    }


    public void closeServer(ActionEvent actionEvent) {

        ButtonType confirm = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.NONE, "Disconnettendo il server, tutti i client connessi non potranno più interagire", confirm, cancel);
        a.setTitle("Disconnect the server");
        a.setHeaderText("Do you want to disconnect the server?");
        a.setResizable(true);
        a.showAndWait().ifPresent(response -> {
            if (response == confirm) {
                Platform.exit();
            }
        });

    }
}
