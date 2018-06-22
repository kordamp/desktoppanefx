/*
 * Copyright 2015-2018 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.desktoppanefx;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.kordamp.desktoppanefx.scene.layout.InternalWindow;

import java.net.URL;
import java.util.ResourceBundle;

import static org.kordamp.desktoppanefx.scene.layout.DesktopPane.resolveInternalWindow;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class MyContentController implements Initializable {
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
        btnAlignTOP_CENTER.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.TOP_CENTER));
    }

    private void btnAlignBOTTOM_CENTERHandler() {
        btnAlignBOTTOM_CENTER.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.BOTTOM_CENTER));
    }

    private void btnAlignRIGHT_CENTERHandler() {
        btnAlignRIGHT_CENTER.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.CENTER_RIGHT));
    }

    private void btnAlignLEFT_CENTERHandler() {
        btnAlignLEFT_CENTER.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.CENTER_LEFT));
    }

    private void btnAlignRIGHT_TOPHandler() {
        btnAlignRIGHT_TOP.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.TOP_RIGHT));
    }

    private void btnAlignRIGHT_BOTTOMHandler() {
        btnAlignRIGHT_BOTTOM.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.BOTTOM_RIGHT));
    }

    private void btnAlignLEFT_BOTTOMHandler() {
        btnAlignLEFT_BOTTOM.setOnAction(e -> resolveInternalWindow(mainPane).place(InternalWindow.AlignPosition.BOTTOM_LEFT));
    }

    private void btnAlignLEFT_TOPHandler() {
        btnAlignLEFT_TOP.setOnAction(e -> resolveInternalWindow(mainPane).place(Point2D.ZERO));
    }

    private void linkHandler() {
        link.setOnAction(e -> Sampler.hostServices.showDocument(link.getText()));
    }

    private void btnAlignCENTERHandler() {
        btnAlignCENTER.setOnAction(e -> {
            resolveInternalWindow(mainPane).center();
        });
    }

    private void btnRotateHandler() {
        btnRotate.setOnAction(e -> {
            RotateTransition rt = new RotateTransition(Duration.millis(1000), resolveInternalWindow(mainPane));
            rt.setByAngle(360);
            rt.setCycleCount(1);
            // rt.setAutoReverse(true);
            rt.play();
        });
    }

    private void btnCloseHandler() {
        btnClose.setOnAction(e -> resolveInternalWindow(mainPane).closeWindow());
    }

    private void btnMaximizeRestoreHandler() {
        btnMaximizeRestore.setOnAction(e -> resolveInternalWindow(mainPane).maximizeOrRestoreWindow());
    }

    private void btnMinimizeHandler() {
        btnMinimize.setOnAction(e -> resolveInternalWindow(mainPane).minimizeWindow());
    }

    private void btnDisableEnableCloseHandler() {
        btnDisableEnableClose.setOnAction(e -> {
            InternalWindow myMDI = resolveInternalWindow(mainPane);
            myMDI.setDisableClose(!myMDI.isDisableClose());
        });
    }
}
