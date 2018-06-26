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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private final Button btnClose;
    private final Label lblTitle;
    private final DesktopPane desktopPane;
    private Node icon;

    private final BooleanProperty closeVisible = new SimpleBooleanProperty(this, "closeVisible", true);
    private final BooleanProperty disableClose = new SimpleBooleanProperty(this, "disableClose", false);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final String cssDefault = "-fx-border-color:blue;" + "-fx-border-width: 1;"
        + "-fx-spacing:5.0;" + "-fx-alignment:center-left ";

    /**
     * *************************** CONSTRUCTOR
     */
    public TaskBarIcon(Node icon, DesktopPane desktopPane, String title) {
        super();
        HBox hBox = new HBox();
        hBox.setStyle("-fx-alignment:center-left");

        getStyleClass().add("taskbar-icon");

        hBox.setSpacing(10d);
        hBox.setPadding(new Insets(0, 10, 0, 10));
        this.icon = icon;
        this.desktopPane = desktopPane;
        lblTitle = new Label();
        lblTitle.textProperty().bind(titleProperty());
        setTitle(title);
        addEventHandler(MouseEvent.MOUSE_CLICKED, handleMaximize);

        btnClose = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_CLOSE));
        btnClose.visibleProperty().bind(closeVisible);
        btnClose.managedProperty().bind(closeVisible);
        btnClose.disableProperty().bind(disableClose);
        btnClose.getStyleClass().add("controlButtons");
        //Adding the shadow when the mouse cursor is on
        final DropShadow shadowCloseBtn = new DropShadow();
        shadowCloseBtn.setHeight(10d);
        shadowCloseBtn.setWidth(10d);
        btnClose.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btnClose.setEffect(shadowCloseBtn));
        //Removing the shadow when the mouse cursor is off
        btnClose.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btnClose.setEffect(null));
        btnClose.addEventHandler(MouseEvent.MOUSE_CLICKED, handleClose);
        //hBox.getChildren().addAll(imgLogo == null ? new ImageView() : new ImageView(imgLogo.getImage()), lblName, btnClose);
        hBox.getChildren().addAll(icon, lblTitle, btnClose);
        setGraphic(hBox);
    }

    private EventHandler<MouseEvent> handleMaximize = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            InternalWindow internalWindow = desktopPane.findInternalWindow(getId());
            if (internalWindow != null) {
                removeIcon();
                internalWindow.setIcon(icon);
                internalWindow.maximizeOrRestoreWindow();
            }
        }
    };

    private EventHandler<MouseEvent> handleClose = event -> {
        removeInternalWindow();
        removeIcon();
    };

    private void removeInternalWindow() {
        InternalWindow win = desktopPane.findInternalWindow(getId());
        if (win != null) {
            desktopPane.removeInternalWindow(win);
        }
    }

    private void removeIcon() {
        TaskBarIcon icon = desktopPane.findTaskBarIcon(getId());
        if (icon != null) {
            desktopPane.getTaskBar().removeTaskNode(icon);
        }
    }

    public boolean isCloseVisible() {
        return closeVisible.get();
    }

    public BooleanProperty closeVisibleProperty() {
        return closeVisible;
    }

    public void setCloseVisible(boolean closeVisible) {
        this.closeVisible.set(closeVisible);
    }

    public boolean isDisableClose() {
        return disableClose.get();
    }

    public BooleanProperty disableCloseProperty() {
        return disableClose;
    }

    public void setDisableClose(boolean disableClose) {
        this.disableClose.set(disableClose);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}
