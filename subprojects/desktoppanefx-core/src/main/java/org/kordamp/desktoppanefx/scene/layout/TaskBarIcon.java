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
package org.kordamp.desktoppanefx.scene.layout;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class TaskBarIcon extends Button {
    private Button btnClose;
    private Label lblName;
    private final DesktopPane desktopPane;
    private Node icon;
    private final String cssDefault = "-fx-border-color:blue;" + "-fx-border-width: 1;"
        + "-fx-spacing:5.0;" + "-fx-alignment:center-left ";

    /**
     * *************************** CONSTRUCTOR
     */
    public TaskBarIcon(Node icon, DesktopPane desktopPane, String name) throws Exception {
        super();
        HBox hBox = new HBox();
        hBox.setStyle("-fx-alignment:center-left");

        getStyleClass().add("taskbar-icon");
        //        styleProperty().bind(StylesCSS.taskBarIconStyleProperty);

        //setStyle(cssDefault);
        hBox.setSpacing(10d);
        hBox.setPadding(new Insets(0, 10, 0, 10));
        this.icon = icon;
        this.desktopPane = desktopPane;
        lblName = new Label(name);
        lblName.getStyleClass().add("titleText");
        //lblName.styleProperty().bind(StylesCSS.taskBarIconTextStyleProperty);
        addEventHandler(MouseEvent.MOUSE_CLICKED, handleMaximize);

        btnClose = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_CLOSE));
        btnClose.getStyleClass().add("controlButtons");
        //        btnClose.styleProperty().bind(StylesCSS.controlButtonsStyleProperty);
        //Adding the shadow when the mouse cursor is on
        final DropShadow shadowCloseBtn = new DropShadow();
        shadowCloseBtn.setHeight(10d);
        shadowCloseBtn.setWidth(10d);
        btnClose.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btnClose.setEffect(shadowCloseBtn));
        //Removing the shadow when the mouse cursor is off
        btnClose.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btnClose.setEffect(null));
        btnClose.addEventHandler(MouseEvent.MOUSE_CLICKED, handleClose);
        //hBox.getChildren().addAll(imgLogo == null ? new ImageView() : new ImageView(imgLogo.getImage()), lblName, btnClose);
        hBox.getChildren().addAll(icon, lblName, btnClose);
        setGraphic(hBox);
    }

    /**
     * ****************************** GET/SET
     */
    public Label getLblName() {
        return lblName;
    }

    /**
     * ******************************** BUTTON_HANDLERS
     */
    private EventHandler<MouseEvent> handleMaximize = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            InternalWindow win = desktopPane.getItemFromMDIContainer(getId());
            if (win != null) {
                removeIcon();
                win.setIcon(icon);
                win.setVisible(true);
                win.toFront();
            }
        }
    };
    private EventHandler<MouseEvent> handleClose = event -> {
        removeInternalWindow();
        removeIcon();
    };

    private void removeInternalWindow() {
        InternalWindow win = desktopPane.getItemFromMDIContainer(getId());
        if (win != null) {
            desktopPane.getInternalWindowContainer().getChildren().remove(win);
        }

    }

    private void removeIcon() {
        TaskBarIcon icon = desktopPane.getItemFromToolBar(getId());
        if (icon != null) {
            desktopPane.getTbWindows().getChildren().remove(icon);
        }
    }

    public Button getBtnClose() {
        return btnClose;
    }

    public void setBtnClose(Button btnClose) {
        this.btnClose = btnClose;
    }
}
