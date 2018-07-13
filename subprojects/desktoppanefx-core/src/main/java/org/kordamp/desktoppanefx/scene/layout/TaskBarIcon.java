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
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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
    private final InternalWindow internalWindow;
    private final DesktopPane desktopPane;
    private Node icon;

    public TaskBarIcon(InternalWindow internalWindow) {
        super();
        this.internalWindow = internalWindow;
        this.icon = internalWindow.getIcon();
        this.desktopPane = internalWindow.getDesktopPane();

        getStyleClass().add("taskbar-icon");
        setId(internalWindow.getId());
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> restoreWindow());

        HBox pane = new HBox();
        pane.setStyle("-fx-alignment:center-left");
        pane.setSpacing(10d);
        pane.setPadding(new Insets(0, 10, 0, 10));

        lblTitle = new Label();
        lblTitle.textProperty().bind(internalWindow.titleProperty());

        btnClose = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_CLOSE));
        btnClose.visibleProperty().bind(closeVisibleProperty());
        btnClose.managedProperty().bind(closeVisibleProperty());
        btnClose.disableProperty().bind(disableCloseProperty());
        btnClose.getStyleClass().add("taskbar-icon-button");

        //Adding the shadow when the mouse cursor is on
        final DropShadow shadowCloseBtn = new DropShadow();
        shadowCloseBtn.setHeight(10d);
        shadowCloseBtn.setWidth(10d);
        btnClose.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btnClose.setEffect(shadowCloseBtn));

        //Removing the shadow when the mouse cursor is off
        btnClose.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btnClose.setEffect(null));
        btnClose.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            removeInternalWindow();
            removeIcon();
        });

        pane.getChildren().addAll(icon, lblTitle, btnClose);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(pane);
    }

    public final InternalWindow getInternalWindow() {
        return internalWindow;
    }

    public void restoreWindow() {
        removeIcon();
        internalWindow.setIcon(icon);
        internalWindow.maximizeOrRestoreWindow();
    }

    private void removeInternalWindow() {
        desktopPane.removeInternalWindow(internalWindow);
    }

    private void removeIcon() {
        desktopPane.findTaskBarIcon(getId())
            .ifPresent(desktopPane.getTaskBar()::removeTaskBarIcon);
    }

    public boolean isCloseVisible() {
        return internalWindow.isCloseVisible();
    }

    public BooleanProperty closeVisibleProperty() {
        return internalWindow.closeVisibleProperty();
    }

    public void setCloseVisible(boolean closeVisible) {
        internalWindow.setCloseVisible(closeVisible);
    }

    public boolean isDisableClose() {
        return internalWindow.isDisableClose();
    }

    public BooleanProperty disableCloseProperty() {
        return internalWindow.disableCloseProperty();
    }

    public void setDisableClose(boolean disableClose) {
        internalWindow.setDisableClose(disableClose);
    }

    public String getTitle() {
        return internalWindow.getTitle();
    }

    public StringProperty titleProperty() {
        return internalWindow.titleProperty();
    }

    public void setTitle(String title) {
        internalWindow.setTitle(title);
    }
}
