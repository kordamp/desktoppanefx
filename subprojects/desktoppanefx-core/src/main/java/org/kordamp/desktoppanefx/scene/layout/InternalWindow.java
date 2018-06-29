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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kordamp.desktoppanefx.scene.layout;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import static javafx.beans.binding.Bindings.createBooleanBinding;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class InternalWindow extends BorderPane {
    private double mousex = 0;
    private double mousey = 0;
    private double x = 0;
    private double y = 0;

    private AnchorPane headerPane;
    private Node content;
    private Pane contentPane;
    private Pane titlePane;

    private Node icon;
    private Label lblTitle;
    private Button btnClose;
    private Button btnMinimize;
    private Button btnMaximize;
    private Button btnDetach;

    private InternalWindow.ResizeMode resizeMode;
    private boolean resizeTop;
    private boolean resize;
    private boolean resizeBottom;
    private boolean resizeRight;
    private double previousWidthToResize;
    private double previousHeightToResize;
    private boolean disableResize = false;
    private double lastY;
    private double lastX;

    private Stage detachedWindow;
    private DesktopPane desktopPane;
    private boolean wasMaximized = false;

    private final BooleanProperty closed = new SimpleBooleanProperty(this, "closed", false);
    private final BooleanProperty minimized = new SimpleBooleanProperty(this, "minimized", false);
    private final BooleanProperty maximized = new SimpleBooleanProperty(this, "maximized", false);
    private final BooleanProperty detached = new SimpleBooleanProperty(this, "detached", false);

    private final BooleanProperty minimizeVisible = new SimpleBooleanProperty(this, "minimizeVisible", true);
    private final BooleanProperty maximizeVisible = new SimpleBooleanProperty(this, "maximizeVisible", true);
    private final BooleanProperty closeVisible = new SimpleBooleanProperty(this, "closeVisible", true);
    private final BooleanProperty detachVisible = new SimpleBooleanProperty(this, "detachVisible", true);

    private final BooleanProperty disableMinimize = new SimpleBooleanProperty(this, "disableMinimize", false);
    private final BooleanProperty disableMaximize = new SimpleBooleanProperty(this, "disableMaximize", false);
    private final BooleanProperty disableClose = new SimpleBooleanProperty(this, "disableClose", false);
    private final BooleanProperty disableDetach = new SimpleBooleanProperty(this, "disableDetach", false);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private BooleanBinding showingBinding;

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content) {
        init(mdiWindowID, icon, title, content);
    }

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content, boolean disableResize) {
        this.disableResize = disableResize;
        init(mdiWindowID, icon, title, content);
    }

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content, boolean disableResize, boolean maximize) {
        this.disableResize = disableResize;
        init(mdiWindowID, icon, title, content);
        if (maximize) {
            center();
            maximizeOrRestoreWindow();
        }
    }

    public DesktopPane getDesktopPane() {
        return desktopPane;
    }

    public void setDesktopPane(DesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    public Node getIcon() {
        return icon;
    }

    private void init(String mdiWindowID, Node icon, String title, Node content) {
        setId(mdiWindowID);
        this.icon = icon;
        moveListener();
        bringToFrontListener();

        setPrefSize(200, 200);
        getStyleClass().add("internal-window");
        setTop(headerPane = makeHeader(title));
        setCenter(contentPane = makeContentPane(content));

        detachedWindow = new Stage();
        detachedWindow.setScene(new Scene(new BorderPane()));

        showingBinding = createBooleanBinding(() -> isVisible() | detachedWindow.isShowing(), visibleProperty(), detachedWindow.showingProperty());
    }

    public Node getContent() {
        return content;
    }

    public void setContent(Node content) {
        if (content != null) {
            contentPane.getChildren().setAll(content);
        } else {
            contentPane.getChildren().clear();
        }
    }

    private AnchorPane makeHeader(String title) {
        // HEADER:
        AnchorPane header = new AnchorPane();
        header.setPrefHeight(32);
        header.getStyleClass().add("internal-window-titlebar");

        titlePane = makeTitlePane(title);
        header.getChildren().add(titlePane);
        header.setPadding(new Insets(0, 11, 0, 0));

        btnDetach = new Button("", new FontIcon(resolveDetachIcon()));
        btnDetach.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        detachedProperty().addListener((v, o, n) -> btnDetach.setGraphic(new FontIcon(resolveDetachIcon())));
        btnDetach.getStyleClass().add("internal-window-titlebar-button");
        btnDetach.visibleProperty().bind(detachVisible);
        btnDetach.managedProperty().bind(detachVisible);
        btnDetach.disableProperty().bind(disableDetach);
        btnDetach.setOnMouseClicked(e -> detachOrAttachWindow());

        btnClose = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_CLOSE));
        btnClose.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnClose.getStyleClass().add("internal-window-titlebar-button");
        btnClose.visibleProperty().bind(closeVisible);
        btnClose.managedProperty().bind(closeVisible);
        btnClose.disableProperty().bind(disableClose);
        btnClose.setOnMouseClicked(e -> closeWindow());

        btnMinimize = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_MINIMIZE));
        btnMinimize.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnMinimize.getStyleClass().add("internal-window-titlebar-button");
        btnMinimize.visibleProperty().bind(minimizeVisible);
        btnMinimize.managedProperty().bind(minimizeVisible);
        btnMinimize.disableProperty().bind(disableMinimize);
        btnMinimize.setOnMouseClicked(e -> minimizeWindow());

        btnMaximize = new Button("", new FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE));
        btnMaximize.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnMaximize.getStyleClass().add("internal-window-titlebar-button");
        btnMaximize.visibleProperty().bind(maximizeVisible);
        btnMaximize.managedProperty().bind(maximizeVisible);
        btnMaximize.disableProperty().bind(disableMaximize);
        btnMaximize.setOnMouseClicked(e -> maximizeOrRestoreWindow());

        if (!disableResize) {
            // header.getChildren().add(makeControls(btnDetach, btnMinimize, btnMaximize, btnClose));
            header.getChildren().add(makeControls(btnMinimize, btnMaximize, btnClose));
            //double click on title bar
            header.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    maximizeOrRestoreWindow();
                }
            });
        } else {
            header.getChildren().add(makeControls(btnDetach, btnMinimize, btnClose));
            // header.getChildren().add(makeControls(btnMinimize, btnClose));
        }

        return header;
    }

    private Ikon resolveDetachIcon() {
        return isDetached() ? MaterialDesign.MDI_ARROW_DOWN_BOLD : MaterialDesign.MDI_ARROW_UP_BOLD;
    }

    public void minimizeWindow() {
        if (isMinimized()) { return; }

        wasMaximized = isMaximized();
        maximized.set(false);
        minimized.set(true);

        if (isDetached()) {
            if (!wasMaximized) {
                lastX = detachedWindow.getX();
                lastY = detachedWindow.getY();
                previousHeightToResize = detachedWindow.getHeight();
                previousWidthToResize = detachedWindow.getWidth();
            }
            detachedWindow.setIconified(true);
        } else {
            if (!wasMaximized) {
                lastX = getLayoutX();
                lastY = getLayoutY();
                previousHeightToResize = getHeight();
                previousWidthToResize = getWidth();
            }
            setVisible(false);
            setManaged(false);
        }
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MINIMIZED));
    }

    public void maximizeOrRestoreWindow() {
        boolean wasMinimized = isMinimized();
        minimized.set(false);

        if (isDetached()) {
            if (wasMinimized) {
                if (wasMaximized) {
                    // maximize
                    maximizeWindow(false);
                } else {
                    // restore
                    restoreWindow();
                }
            } else if (isMaximized()) {
                // restore
                restoreWindow();
            } else {
                // maximize
                maximizeWindow(true);
            }
        } else {
            if (wasMinimized) {
                if (wasMaximized) {
                    // maximize
                    maximizeInternalWindow(false);
                } else {
                    // restore
                    restoreInternalWindow();
                }
            } else if (isMaximized()) {
                // restore
                restoreInternalWindow();
            } else {
                // maximize
                maximizeInternalWindow(true);
            }
        }
    }

    private void maximizeInternalWindow(boolean recordSizes) {
        if (recordSizes) {
            lastX = getLayoutX();
            lastY = getLayoutY();
            previousHeightToResize = getHeight();
            previousWidthToResize = getWidth();
        }
        maximized.set(true);
        wasMaximized = false;
        btnMaximize.setGraphic(new FontIcon(MaterialDesign.MDI_WINDOW_RESTORE));
        addListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MAXIMIZED));
    }

    private void restoreInternalWindow() {
        setLayoutX(lastX);
        setLayoutY(lastY);
        setPrefSize(previousWidthToResize, previousHeightToResize);
        maximized.set(false);
        btnMaximize.setGraphic(new FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE));
        removeListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        toFront();
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_RESTORED));
    }

    private void maximizeWindow(boolean recordSizes) {
        if (recordSizes) {
            lastX = detachedWindow.getX();
            lastY = detachedWindow.getY();
            previousHeightToResize = detachedWindow.getHeight();
            previousWidthToResize = detachedWindow.getWidth();
            previousWidthToResize = getWidth();
        }
        detachedWindow.setMaximized(true);
        wasMaximized = false;
        btnMaximize.setGraphic(new FontIcon(MaterialDesign.MDI_WINDOW_RESTORE));
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MAXIMIZED));
    }

    private void restoreWindow() {
        detachedWindow.setX(lastX);
        detachedWindow.setY(lastY);
        detachedWindow.setWidth(previousWidthToResize);
        detachedWindow.setHeight(previousHeightToResize);

        detachedWindow.setMaximized(false);
        maximized.set(false);
        btnMaximize.setGraphic(new FontIcon(MaterialDesign.MDI_WINDOW_MAXIMIZE));
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_RESTORED));
    }

    private Pane makeContentPane(Node content) {
        this.content = content;
        AnchorPane paneContent = new AnchorPane(content);
        content.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #F2F2F2");
        AnchorPane.setBottomAnchor(content, 0d);
        AnchorPane.setLeftAnchor(content, 0d);
        AnchorPane.setRightAnchor(content, 0d);
        AnchorPane.setTopAnchor(content, 0d);
        return paneContent;
    }

    private HBox makeTitlePane(String title) {
        HBox hbLeft = new HBox();
        hbLeft.setSpacing(10d);
        lblTitle = new Label();
        lblTitle.textProperty().bind(titleProperty());
        setTitle(title);
        lblTitle.getStyleClass().add("internal-window-titlebar-title");
        //        lblTitle.setStyle("-fx-font-weight: bold;");
        //lblTitle.styleProperty().bind(StylesCSS.taskBarIconTextStyleProperty);

        if (icon != null) { hbLeft.getChildren().add(icon); }
        hbLeft.getChildren().add(lblTitle);
        hbLeft.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setLeftAnchor(hbLeft, 10d);
        AnchorPane.setBottomAnchor(hbLeft, 0d);
        AnchorPane.setRightAnchor(hbLeft, 20d);
        AnchorPane.setTopAnchor(hbLeft, 0d);
        return hbLeft;
    }

    public void setIcon(Node icon) {
        this.icon = icon;
        if (titlePane.getChildren().size() == 1) {
            titlePane.getChildren().add(0, icon);
        } else {
            titlePane.getChildren().set(0, icon);
        }
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

    private void moveListener() {
        this.setOnMouseDragged((MouseEvent dragEvent) -> {
            if (!isMaximized()) {
                //Move
                x += dragEvent.getSceneX() - mousex;
                y += dragEvent.getSceneY() - mousey;
                //again set current Mouse x AND y position
                mousex = dragEvent.getSceneX();
                mousey = dragEvent.getSceneY();
                if (resizeMode == resizeMode.NONE) {
                    //set the position of Node after calculation
                    if (getWidth() < getParent().getLayoutBounds().getWidth()) {//if the panel is not biger then the window: Move
                        setLayoutX(x);
                    }
                    if (getHeight() < getParent().getLayoutBounds().getHeight()) {//if the panel is not biger then the window: Move
                        setLayoutY(y);
                    }

                    //LEFT AND RIGHT
                    if (getLayoutX() <= getParent().getLayoutX()) {
                        setLayoutX(getParent().getLayoutX());
                    } else if ((getLayoutX() + getWidth()) >= getParent().getLayoutBounds().getWidth()) {
                        setLayoutX(getParent().getLayoutBounds().getWidth() - getWidth());
                    }
                    //UP AND DOWN
                    if (getLayoutY() <= getParent().getLayoutX()) {
                        setLayoutY(getParent().getLayoutY());
                    } else if ((getLayoutY() + getHeight()) >= getParent().getLayoutBounds().getHeight()) {
                        setLayoutY(getParent().getLayoutBounds().getHeight() - getHeight());
                    }
                } else {
                    if (!disableResize) {
                        //Resize
                        //RIGHT AND DOWN
                        //Only the Right Resize
                        if (resizeRight && !resizeTop && !resizeBottom) {
                            if (dragEvent.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())) {
                                setPrefWidth(dragEvent.getX());
                            } else {
                                setPrefWidth(getParent().getLayoutBounds().getWidth() - getLayoutX());
                            }
                        } //Only The Bottom Resize
                        else if (!resizeRight && !resizeTop && resizeBottom) {
                            if (dragEvent.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                setPrefHeight(dragEvent.getY());
                            } else {
                                setPrefHeight(getParent().getLayoutBounds().getHeight() - getLayoutY());
                            }
                        } //Only The Bottom with Right Resize
                        else if ((resizeRight && !resizeTop && resizeBottom)) {
                            if (dragEvent.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())
                                && dragEvent.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                setPrefWidth(dragEvent.getX());
                                setPrefHeight(dragEvent.getY());
                            } else {
                                if (dragEvent.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())) {
                                    setPrefWidth(dragEvent.getX());
                                } else {
                                    setPrefWidth(getParent().getLayoutBounds().getWidth() - getLayoutX());
                                }
                                if (dragEvent.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                    setPrefHeight(dragEvent.getY());
                                } else {
                                    setPrefHeight(getParent().getLayoutBounds().getHeight() - getLayoutY());
                                }
                            }
                        }/* //Only The Left Resize
                        else if (resize && !resizeTop && !resizeBottom) {
                            // DO NOTHING YET:
                            //TODO
                        }*/
                    }
                }
            }
        });

        setOnMousePressed((MouseEvent event) -> {
            toFront();
            mousex = event.getSceneX();
            mousey = event.getSceneY();
            x = getLayoutX();
            y = getLayoutY();
        });

        onMouseMovedProperty().set((MouseEvent t) -> {
            final double scaleX = localToSceneTransformProperty().getValue().getMxx();
            final double scaleY = localToSceneTransformProperty().getValue().getMyy();
            final double border1 = 5;
            double diffMinX = Math.abs(getBoundsInLocal().getMinX() - t.getX());
            double diffMinY = Math.abs(getBoundsInLocal().getMinY() - t.getY());
            double diffMaxX = Math.abs(getBoundsInLocal().getMaxX() - t.getX());
            double diffMaxY = Math.abs(getBoundsInLocal().getMaxY() - t.getY());
            boolean left1 = diffMinX * scaleX < border1;
            boolean top1 = diffMinY * scaleY < border1;
            boolean right1 = diffMaxX * scaleX < border1;
            boolean bottom1 = diffMaxY * scaleY < border1;
            resizeTop = false;
            resize = false;
            resizeBottom = false;
            resizeRight = false;
            /*if (left1 && !top1 && !bottom1) {
            } else if (left1 && top1 && !bottom1) {
            } else if (left1 && !top1 && bottom1) {
            } else*/
            if (right1 && !top1 && !bottom1) {
                if (!disableResize) {
                    setCursor(Cursor.E_RESIZE);
                }
                resizeMode = ResizeMode.RIGHT;
                resizeRight = true;
                //} else if (right1 && top1 && !bottom1) {
            } else if (right1 && !top1 && bottom1) {
                if (!disableResize) {
                    setCursor(Cursor.SE_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM_RIGHT;
                resizeRight = true;
                resizeBottom = true;
                //} else if (top1 && !left1 && !right1) {
            } else if (bottom1 && !left1 && !right1) {
                if (!disableResize) {
                    setCursor(Cursor.S_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM;
                resizeBottom = true;
            } else {
                setCursor(Cursor.DEFAULT);
                resizeMode = ResizeMode.NONE;
            }
        });

    }

    public void snapTo(AlignPosition alignPosition) {
        Platform.runLater(() -> desktopPane.snapTo(this, alignPosition));
    }

    public void place(Point2D point) {
        Platform.runLater(() -> desktopPane.placeInternalWindow(this, point));
    }

    public void center() {
        Platform.runLater(() -> desktopPane.centerInternalWindow(this));
    }

    public void closeWindow() {
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_CLOSED));
        if (isDetached()) {
            detachedWindow.close();
        } else {
            ScaleTransition st = hideWindow();

            st.setOnFinished(t -> {
                desktopPane.removeInternalWindow(this);
                closed.setValue(true);
            });

            st.play();
        }
    }

    private ScaleTransition hideWindow() {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), this);
        st.setToX(0);
        st.setToY(0);
        st.setByX(1);
        st.setByY(1);
        st.setCycleCount(1);
        return st;
    }

    private DesktopPane dp;

    public void detachOrAttachWindow() {
        setDetached(!isDetached());

        if (isDetached()) {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_DETACHED));

            Window window = desktopPane.getScene().getWindow();
            double cx = window.getX() + (window.getWidth() / 2);
            double cy = window.getY() + (window.getHeight() / 2);
            detachedWindow.getScene().getStylesheets().setAll(getScene().getStylesheets());

            dp = desktopPane.removeInternalWindow(this);

            BorderPane bp = (BorderPane) detachedWindow.getScene().getRoot();
            bp.setTop(headerPane);
            bp.setCenter(contentPane);
            detachedWindow.sizeToScene();
            detachedWindow.setMaximized(isMaximized());

            // if showing for the first time then width/height == NaN
            if (Double.isNaN(detachedWindow.getWidth())) {
                detachedWindow.show();
                cx -= detachedWindow.getWidth() / 2;
                cy -= detachedWindow.getHeight() / 2;
                detachedWindow.setX(cx);
                detachedWindow.setY(cy);
            } else {
                cx -= detachedWindow.getWidth() / 2;
                cy -= detachedWindow.getHeight() / 2;
                detachedWindow.setX(cx);
                detachedWindow.setY(cy);
                detachedWindow.show();
            }
            detachedWindow.setMaximized(isMaximized());
        } else {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_ATTACHED));
            detachedWindow.hide();
            setTop(headerPane);
            setCenter(contentPane);
            dp.addInternalWindow(this);
        }
    }

    private void bringToFrontListener() {
        this.setOnMouseClicked((MouseEvent t) -> toFront());
    }

    private void removeListenerToResizeMaximizedWindows() {
        AnchorPane.clearConstraints(this);
        setLayoutX((int) lastX);
        setLayoutY((int) lastY);
    }

    private void addListenerToResizeMaximizedWindows() {
        AnchorPane.setBottomAnchor(this, 0d);
        AnchorPane.setTopAnchor(this, 0d);
        AnchorPane.setLeftAnchor(this, 0d);
        AnchorPane.setRightAnchor(this, 0d);
    }

    enum ResizeMode {
        NONE,
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public enum AlignPosition {
        CENTER,
        CENTER_LEFT,
        CENTER_RIGHT,
        TOP_CENTER,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER
    }

    public BooleanProperty closedProperty() {
        return closed;
    }

    public boolean isClosed() {
        return closed.getValue();
    }

    public void setClosed(boolean value) {
        closed.setValue(value);
    }

    public boolean isMinimizeVisible() {
        return minimizeVisible.get();
    }

    public BooleanProperty minimizeVisibleProperty() {
        return minimizeVisible;
    }

    public void setMinimizeVisible(boolean minimizeVisible) {
        this.minimizeVisible.set(minimizeVisible);
    }

    public boolean isMaximizeVisible() {
        return maximizeVisible.get();
    }

    public BooleanProperty maximizeVisibleProperty() {
        return maximizeVisible;
    }

    public void setMaximizeVisible(boolean maximizeVisible) {
        this.maximizeVisible.set(maximizeVisible);
    }

    public boolean isCloseVisible() {
        return closeVisible.get();
    }

    public BooleanProperty closeVisibleProperty() {
        return closeVisible;
    }

    public boolean isDetachVisible() {
        return detachVisible.get();
    }

    public BooleanProperty detachVisibleProperty() {
        return detachVisible;
    }

    public void setDetachVisible(boolean detachVisible) {
        this.detachVisible.set(detachVisible);
    }

    public void setCloseVisible(boolean closeVisible) {
        this.closeVisible.set(closeVisible);
    }

    public boolean isDisableMinimize() {
        return disableMinimize.get();
    }

    public BooleanProperty disableMinimizeProperty() {
        return disableMinimize;
    }

    public void setDisableMinimize(boolean disableMinimize) {
        this.disableMinimize.set(disableMinimize);
    }

    public boolean isDisableMaximize() {
        return disableMaximize.get();
    }

    public BooleanProperty disableMaximizeProperty() {
        return disableMaximize;
    }

    public void setDisableMaximize(boolean disableMaximize) {
        this.disableMaximize.set(disableMaximize);
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

    public boolean isDisableDetach() {
        return disableDetach.get();
    }

    public BooleanProperty disableDetachProperty() {
        return disableDetach;
    }

    public void setDisableDetach(boolean disableDetach) {
        this.disableDetach.set(disableDetach);
    }

    public boolean isDetached() {
        return detached.get();
    }

    public BooleanProperty detachedProperty() {
        return detached;
    }

    public void setDetached(boolean detached) {
        this.detached.set(detached);
    }

    public boolean isMinimized() {
        return minimized.get();
    }

    public ReadOnlyBooleanProperty minimizedProperty() {
        return minimized;
    }

    public boolean isMaximized() {
        return maximized.get();
    }

    public ReadOnlyBooleanProperty maximizedProperty() {
        return maximized;
    }

    public boolean isShowing() {
        return showingBinding.getValue();
    }

    public BooleanBinding showingBinding() {
        return showingBinding;
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
