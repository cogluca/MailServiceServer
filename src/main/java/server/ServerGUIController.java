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
import models.User;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerGUIController implements Initializable {

    @FXML
    private ListView<String> accountView;

    @FXML
    private ListView<String> serverLogView;


    private ServerModel serverModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }


    public void setModel(ServerModel model) {
        this.serverModel = model;
        serverLogView.setItems(model.getLogList());
        accountView.setItems(model.getLoggedUserList());

    }

    public void closeServer(ActionEvent actionEvent) {

        ButtonType confirm = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.NONE, "Promote pawn to:", confirm, cancel);
        a.setTitle("Disconnetti il server");
        a.setHeaderText("Vuoi disconnettere il server?");
        a.setResizable(true);
        a.setContentText("Disconnettendo il server, tutti i client connessi non potranno più interagire");
        a.showAndWait().ifPresent(response -> {
            if (response == confirm) {
                Platform.exit();
            }
        });

    }
}
