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

    private Executor executor;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverModel = new ServerModel();

        serverLogView.setItems(serverModel.getLogList());
        accountView.setItems(serverModel.getLoggedUserList());

        runServerThread();
    }

    /**
     * This method start a thread that open a server socket
     * and listen on connection. For each client that send a request,
     * is created a runnable and executed in a thread pool
     */
    public void runServerThread() {

        Thread serverThread = new Thread(() -> {
            try {
                executor = Executors.newFixedThreadPool(6);

                ServerSocket s = new ServerSocket(8189);
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


    /**
     * handle the "disconnect server" button.
     * Open an alert to ask to confirm the action
     */
    public void closeServer(ActionEvent actionEvent) {

        ButtonType confirm = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.NONE, "Shutting down the server all clients will not be able to interact", confirm, cancel);
        a.setTitle("Disconnect the server");
        a.setHeaderText("Do you want to disconnect the server?");
        a.setResizable(true);
        a.showAndWait().ifPresent(response -> {
            if (response.equals(confirm)) {
                Platform.exit();
                System.exit(0);
            }
        });

    }
}
