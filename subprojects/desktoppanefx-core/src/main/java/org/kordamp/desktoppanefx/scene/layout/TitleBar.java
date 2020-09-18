/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2015-2020 The original authors
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 * @author Andres Almiray
 */
public class TitleBar extends AnchorPane {
    private final BooleanProperty minimizeVisible = new SimpleBooleanProperty(this, "minimizeVisible", true);
    private final BooleanProperty maximizeVisible = new SimpleBooleanProperty(this, "maximizeVisible", true);
    private final BooleanProperty closeVisible = new SimpleBooleanProperty(this, "closeVisible", true);
    private final BooleanProperty detachVisible = new SimpleBooleanProperty(this, "detachVisible", true);
    private final BooleanProperty disableMinimize = new SimpleBooleanProperty(this, "disableMinimize", false);
    private final BooleanProperty disableMaximize = new SimpleBooleanProperty(this, "disableMaximize", false);
    private final BooleanProperty disableClose = new SimpleBooleanProperty(this, "disableClose", false);
    private final BooleanProperty disableDetach = new SimpleBooleanProperty(this, "disableDetach", false);
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final InternalWindow internalWindow;
    private final ContextMenu contextMenu;
    private final Button btnClose;
    private final Button btnMinimize;
    private final Button btnMaximize;
    private final Button btnDetach;
    private Pane titlePane;
    private Node icon;
    private Label lblTitle;

    public TitleBar(InternalWindow internalWindow, Node icon, String title) {
        this.internalWindow = internalWindow;
        this.icon = icon;

        setPrefHeight(32);
        getStyleClass().add("internal-window-titlebar");

        titlePane = makeTitlePane(title);
        getChildren().add(titlePane);
        setPadding(new Insets(0, 11, 0, 0));

        contextMenu = createContextMenu(internalWindow);
        btnDetach = createButtonDetach(internalWindow);
        btnClose = createButtonClose(internalWindow);
        btnMinimize = createButtonMinimize(internalWindow);
        btnMaximize = createButtonMaximize(internalWindow);

        boolean detachableWindows = Boolean.getBoolean(IncubatingFeatures.DETACHABLE_WINDOWS);

        //double click on title bar
        setOnMouseClicked((MouseEvent event) -> {
            if (internalWindow.isResizable()) {
                if (event.getClickCount() == 2) {
                    internalWindow.maximizeOrRestoreWindow();
                }
            }
        });

        if (detachableWindows) {
            getChildren().add(makeControls(btnDetach, btnMinimize, btnMaximize, btnClose));
        } else {
            getChildren().add(makeControls(btnMinimize, btnMaximize, btnClose));
        }

        btnMaximize.visibleProperty().bind(internalWindow.resizableProperty());
        btnMaximize.managedProperty().bind(internalWindow.resizableProperty());

        internalWindow.maximizedProperty().addListener((v, o, n) -> ((FontIcon) btnMaximize.getGraphic()).setIconCode(n ?
            MaterialDesign.MDI_WINDOW_RESTORE :
            MaterialDesign.MDI_WINDOW_MAXIMIZE));
    }

    protected Button createButtonDetach(InternalWindow internalWindow) {
        Button btnDetach = new Button("", new FontIcon(resolveDetachIcon()));
        btnDetach.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnDetach.getStyleClass().add("internal-window-titlebar-button");
        btnDetach.visibleProperty().bind(detachVisible);
        btnDetach.managedProperty().bind(detachVisible);
        btnDetach.disableProperty().bind(disableDetach);
        btnDetach.setOnMouseClicked(e -> internalWindow.detachOrAttachWindow());
        internalWindow.detachedProperty().addListener((v, o, n) -> btnDetach.setGraphic(new FontIcon(resolveDetachIcon())));
        return btnDetach;
    }

    protected Button createButtonClose(InternalWindow internalWindow) {
        Button btnClose = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_CLOSE));
        btnClose.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnClose.getStyleClass().add("internal-window-titlebar-button");
        btnClose.visibleProperty().bind(closeVisible);
        btnClose.managedProperty().bind(closeVisible);
        btnClose.disableProperty().bind(disableClose);
        btnClose.setOnMouseClicked(e -> internalWindow.closeWindow());
        return btnClose;
    }

    protected Button createButtonMinimize(InternalWindow internalWindow) {
        Button btnMinimize = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE));
        btnMinimize.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnMinimize.getStyleClass().add("internal-window-titlebar-button");
        btnMinimize.visibleProperty().bind(minimizeVisible);
        btnMinimize.managedProperty().bind(minimizeVisible);
        btnMinimize.disableProperty().bind(disableMinimize);
        btnMinimize.setOnMouseClicked(e -> internalWindow.minimizeWindow());
        return btnMinimize;
    }

    protected Button createButtonMaximize(InternalWindow internalWindow) {
        Button btnMaximize = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE));
        btnMaximize.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnMaximize.getStyleClass().add("internal-window-titlebar-button");
        btnMaximize.visibleProperty().bind(maximizeVisible);
        btnMaximize.managedProperty().bind(maximizeVisible);
        btnMaximize.disableProperty().bind(disableMaximize);
        btnMaximize.setOnMouseClicked(e -> internalWindow.maximizeOrRestoreWindow());
        return btnMaximize;
    }

    protected ContextMenu createContextMenu(InternalWindow internalWindow) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
            createMinimizeMenu(internalWindow),
            createMaximizeMenu(internalWindow),
            createCloseMenu(internalWindow));

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!internalWindow.isDetached() && event.isSecondaryButtonDown()) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            }
        });
        return contextMenu;
    }

    protected Menu createMinimizeMenu(InternalWindow internalWindow) {
        Menu minimizeMenu = new Menu("Minimize");
        MenuItem minimize = new MenuItem("Minimize");
        minimize.setOnAction(e -> internalWindow.minimizeWindow());
        MenuItem minimizeAll = new MenuItem("Minimize All");
        minimizeAll.setOnAction(e -> internalWindow.getDesktopPane().minimizeAllWindows());
        MenuItem minimizeOthers = new MenuItem("Minimize Others");
        minimizeOthers.setOnAction(e -> internalWindow.getDesktopPane().minimizeOtherWindows());
        minimizeMenu.getItems().addAll(minimize, minimizeAll, minimizeOthers);
        return minimizeMenu;
    }

    protected Menu createMaximizeMenu(InternalWindow internalWindow) {
        Menu maximizeMenu = new Menu("Maximize");
        MenuItem maximize = new MenuItem("Maximize");
        maximize.setOnAction(e -> internalWindow.maximizeOrRestoreWindow());
        maximize.disableProperty().bind(internalWindow.maximizedProperty());
        MenuItem maximizeAll = new MenuItem("Maximize All");
        maximizeAll.setOnAction(e -> internalWindow.getDesktopPane().maximizeAllWindows());
        MenuItem maximizeVisible = new MenuItem("Maximize Visible");
        maximizeVisible.setOnAction(e -> internalWindow.getDesktopPane().maximizeVisibleWindows());
        maximizeMenu.getItems().addAll(maximize, maximizeAll, maximizeVisible);
        return maximizeMenu;
    }

    protected Menu createCloseMenu(InternalWindow internalWindow) {
        Menu closeMenu = new Menu("Close");
        MenuItem close = new MenuItem("Close");
        close.setOnAction(e -> internalWindow.closeWindow());
        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction(e -> internalWindow.getDesktopPane().closeAllWindows());
        MenuItem closeOthers = new MenuItem("Close Others");
        closeOthers.setOnAction(e -> internalWindow.getDesktopPane().closeOtherWindows());
        closeMenu.getItems().addAll(close, closeAll, closeOthers);
        return closeMenu;
    }

    private Pane makeTitlePane(String title) {
        HBox hbLeft = new HBox();
        hbLeft.setSpacing(10d);
        lblTitle = new Label();
        lblTitle.textProperty().bind(titleProperty());
        setTitle(title);
        lblTitle.getStyleClass().add("internal-window-titlebar-title");

        if (icon != null) {
            hbLeft.getChildren().add(icon);
        }
        hbLeft.getChildren().add(lblTitle);
        hbLeft.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setLeftAnchor(hbLeft, 10d);
        AnchorPane.setBottomAnchor(hbLeft, 0d);
        AnchorPane.setRightAnchor(hbLeft, 20d);
        AnchorPane.setTopAnchor(hbLeft, 0d);
        return hbLeft;
    }

    private HBox makeControls(Node... nodes) {
        HBox hbRight = new HBox();
        hbRight.getChildren().addAll(nodes);
        hbRight.setAlignment(Pos.CENTER_RIGHT);
        AnchorPane.setBottomAnchor(hbRight, 0d);
        AnchorPane.setRightAnchor(hbRight, 0d);
        AnchorPane.setTopAnchor(hbRight, 0d);
        return hbRight;
    }

    private Ikon resolveDetachIcon() {
        return internalWindow.isDetached() ? MaterialDesign.MDI_ARROW_DOWN_BOLD : MaterialDesign.MDI_ARROW_UP_BOLD;
    }

    public Node getIcon() {
        return icon;
    }

    public void setIcon(Node icon) {
        this.icon = icon;
        if (titlePane.getChildren().size() == 1) {
            titlePane.getChildren().add(0, icon);
        } else {
            titlePane.getChildren().set(0, icon);
        }
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public boolean isMinimizeVisible() {
        return minimizeVisible.get();
    }

    public void setMinimizeVisible(boolean minimizeVisible) {
        this.minimizeVisible.set(minimizeVisible);
    }

    public BooleanProperty minimizeVisibleProperty() {
        return minimizeVisible;
    }

    public boolean isMaximizeVisible() {
        return maximizeVisible.get();
    }

    public void setMaximizeVisible(boolean maximizeVisible) {
        this.maximizeVisible.set(maximizeVisible);
    }

    public BooleanProperty maximizeVisibleProperty() {
        return maximizeVisible;
    }

    public boolean isCloseVisible() {
        return closeVisible.get();
    }

    public void setCloseVisible(boolean closeVisible) {
        this.closeVisible.set(closeVisible);
    }

    public BooleanProperty closeVisibleProperty() {
        return closeVisible;
    }

    public boolean isDetachVisible() {
        return detachVisible.get();
    }

    public void setDetachVisible(boolean detachVisible) {
        this.detachVisible.set(detachVisible);
    }

    public BooleanProperty detachVisibleProperty() {
        return detachVisible;
    }

    public boolean isDisableMinimize() {
        return disableMinimize.get();
    }

    public void setDisableMinimize(boolean disableMinimize) {
        this.disableMinimize.set(disableMinimize);
    }

    public BooleanProperty disableMinimizeProperty() {
        return disableMinimize;
    }

    public boolean isDisableMaximize() {
        return disableMaximize.get();
    }

    public void setDisableMaximize(boolean disableMaximize) {
        this.disableMaximize.set(disableMaximize);
    }

    public BooleanProperty disableMaximizeProperty() {
        return disableMaximize;
    }

    public boolean isDisableClose() {
        return disableClose.get();
    }

    public void setDisableClose(boolean disableClose) {
        this.disableClose.set(disableClose);
    }

    public BooleanProperty disableCloseProperty() {
        return disableClose;
    }

    public boolean isDisableDetach() {
        return disableDetach.get();
    }

    public void setDisableDetach(boolean disableDetach) {
        this.disableDetach.set(disableDetach);
    }

    public BooleanProperty disableDetachProperty() {
        return disableDetach;
    }
}
