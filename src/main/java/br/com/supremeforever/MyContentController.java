package br.com.supremeforever;

import br.com.supremeforever.mdi.MDIWindow;
import br.com.supremeforever.mdi.Utility;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author brisatc171.minto - 20/11/2015
 */

public class MyContentController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane mainPane;
    @FXML
    private Button btnDisableEnableClose;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnMaximizeRestore;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnAlignCENTER;
    @FXML
    private Button btnAlignRIGHT_TOP;
    @FXML
    private Button btnAlignRIGHT_BOTTOM;
    @FXML
    private Button btnAlignRIGHT_CENTER;
    @FXML
    private Button btnAlignLEFT_TOP;
    @FXML
    private Button btnAlignLEFT_BOTTOM;
    @FXML
    private Button btnAlignLEFT_CENTER;
    @FXML
    private Button btnRotate;
    @FXML
    private Hyperlink link;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnDisableEnableCloseHandler();
        btnMinimizeHandler();
        btnMaximizeRestoreHandler();
        btnCloseHandler();
        btnAlignCENTERHandler();
        btnRotateHandler();
        linkHandler();
    }

    private void linkHandler() {
        link.setOnAction(e -> {
            Main.hostServices.showDocument(link.getText());
        });
    }

    private void btnAlignCENTERHandler() {
        btnAlignCENTER.setOnAction(e -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            myMDI.centerMdiWindow();
        });
    }

    private void btnRotateHandler() {
        btnRotate.setOnAction(event -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            RotateTransition rt = new RotateTransition(Duration.millis(1000), myMDI);
            rt.setByAngle(360);
            rt.setCycleCount(1);
            // rt.setAutoReverse(true);
            rt.play();
        });
    }

    private void btnCloseHandler() {
        btnClose.setOnAction(event -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            myMDI.closeMdiWindow();
        });
    }

    private void btnMaximizeRestoreHandler() {
        btnMaximizeRestore.setOnAction(event -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            myMDI.maximizeRestoreMdiWindow();
        });
    }

    private void btnMinimizeHandler() {
        btnMinimize.setOnAction(event -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            myMDI.minimizeMdiWindow();
        });
    }

    private void btnDisableEnableCloseHandler() {
        btnDisableEnableClose.setOnAction(event -> {
            MDIWindow myMDI = Utility.getMDIWindow(mainPane);
            myMDI.getBtnClose().setDisable(!myMDI.getBtnClose().isDisable());
        });
    }
}
