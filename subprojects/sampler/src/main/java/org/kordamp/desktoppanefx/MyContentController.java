package org.kordamp.desktoppanefx;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.kordamp.desktoppanefx.scene.layout.DesktopPane;
import org.kordamp.desktoppanefx.scene.layout.InternalWindow;

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
    private Button btnAlignBOTTOM_CENTER;
    @FXML
    private Button btnAlignTOP_CENTER;
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
        btnAlignLEFT_CENTERHandler();
        btnAlignRIGHT_CENTERHandler();
        btnAlignLEFT_TOPHandler();
        btnAlignLEFT_BOTTOMHandler();
        btnAlignRIGHT_BOTTOMHandler();
        btnAlignRIGHT_TOPHandler();
        btnAlignBOTTOM_CENTERHandler();
        btnAlignTOP_CENTERHandler();
        btnRotateHandler();
        linkHandler();
    }

    private void btnAlignTOP_CENTERHandler() {
        btnAlignTOP_CENTER.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.TOP_CENTER);
        });
    }

    private void btnAlignBOTTOM_CENTERHandler() {
        btnAlignBOTTOM_CENTER.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.BOTTOM_CENTER);
        });
    }

    private void btnAlignRIGHT_CENTERHandler() {
        btnAlignRIGHT_CENTER.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.CENTER_RIGHT);
        });
    }

    private void btnAlignLEFT_CENTERHandler() {
        btnAlignLEFT_CENTER.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.CENTER_LEFT);
        });
    }

    private void btnAlignRIGHT_TOPHandler() {
        btnAlignRIGHT_TOP.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.TOP_RIGHT);
        });
    }

    private void btnAlignRIGHT_BOTTOMHandler() {
        btnAlignRIGHT_BOTTOM.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.BOTTOM_RIGHT);
        });
    }

    private void btnAlignLEFT_BOTTOMHandler() {
        btnAlignLEFT_BOTTOM.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(InternalWindow.AlignPosition.BOTTOM_LEFT);
        });
    }

    private void btnAlignLEFT_TOPHandler() {
        btnAlignLEFT_TOP.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.placeMdiWindow(Point2D.ZERO);
        });
    }

    private void linkHandler() {
        link.setOnAction(e -> {
            Sampler.hostServices.showDocument(link.getText());
        });
    }

    private void btnAlignCENTERHandler() {
        btnAlignCENTER.setOnAction(e -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.centerMdiWindow();
        });
    }

    private void btnRotateHandler() {
        btnRotate.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            RotateTransition rt = new RotateTransition(Duration.millis(1000), myMDI);
            rt.setByAngle(360);
            rt.setCycleCount(1);
            // rt.setAutoReverse(true);
            rt.play();
        });
    }

    private void btnCloseHandler() {
        btnClose.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.closeMdiWindow();
        });
    }

    private void btnMaximizeRestoreHandler() {
        btnMaximizeRestore.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.maximizeRestoreMdiWindow();
        });
    }

    private void btnMinimizeHandler() {
        btnMinimize.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.minimizeMdiWindow();
        });
    }

    private void btnDisableEnableCloseHandler() {
        btnDisableEnableClose.setOnAction(event -> {
            InternalWindow myMDI = DesktopPane.resolveInternalWindow(mainPane);
            myMDI.getBtnClose().setDisable(!myMDI.getBtnClose().isDisable());
        });
    }
}
