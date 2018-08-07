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
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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

    private TitleBar titleBar;
    private Pane contentPane;
    private Node content;

    private InternalWindow.ResizeMode resizeMode;
    private boolean resize;
    private boolean resizeTop;
    private boolean resizeBottom;
    private boolean resizeRight;
    private boolean resizeLeft;
    private double previousWidth;
    private double previousHeight;
    private double previousY;
    private double previousX;

    private Stage detachedWindow;
    private DesktopPane desktopPane;
    private boolean wasMaximized = false;

    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", true);
    private final BooleanProperty closed = new SimpleBooleanProperty(this, "closed", false);
    private final BooleanProperty minimized = new SimpleBooleanProperty(this, "minimized", false);
    private final BooleanProperty maximized = new SimpleBooleanProperty(this, "maximized", false);
    private final BooleanProperty detached = new SimpleBooleanProperty(this, "detached", false);
    private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", true);

    private static final PseudoClass ACTIVE_CLASS = PseudoClass.getPseudoClass("active");

    private BooleanBinding showingBinding;
    private Point2D pointPressed;

    private boolean sizeWest = false, sizeEast = false, sizeNorth = false, sizeSouth = false;

    private boolean isMouseResizeZone() {
        return sizeWest || sizeEast || sizeNorth || sizeSouth;
    }

    private EventHandler<MouseEvent> windowMousePressed = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            pointPressed = new Point2D(event.getScreenX(), event.getScreenY());
        }
    };

    private EventHandler<MouseEvent> windowMouseMoved = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Cursor cursor = Cursor.DEFAULT;

            BorderPane borderPane = (BorderPane) contentPane.getParent();

            int offset = 5;
            double x = event.getX();
            double y = event.getY();
            double width = borderPane.getWidth();
            double height = borderPane.getHeight();

            sizeWest = x < offset && x > 0d;
            sizeEast = width - offset < x && x < width - 1d;
            sizeNorth = y < offset && y > 0d;
            sizeSouth = height - offset < y && y < height - 1d;

            if (sizeWest) {
                if (sizeNorth) {
                    cursor = Cursor.NW_RESIZE;
                } else if (sizeSouth) {
                    cursor = Cursor.SW_RESIZE;
                } else {
                    cursor = Cursor.W_RESIZE;
                }
            } else if (sizeEast) {
                if (sizeNorth) {
                    cursor = Cursor.NE_RESIZE;
                } else if (sizeSouth) {
                    cursor = Cursor.SE_RESIZE;
                } else {
                    cursor = Cursor.E_RESIZE;
                }
            } else if (sizeNorth) {
                cursor = Cursor.N_RESIZE;
            } else if (sizeSouth) {
                cursor = Cursor.S_RESIZE;
            }

            detachedWindow.getScene().setCursor(cursor);
        }
    };

    private static boolean isContainedInHierarchy(Node container, Node node) {
        Node candidate = node;
        do {
            if (candidate == container) {
                return true;
            }
            candidate = candidate.getParent();
        } while (candidate != null);
        return false;
    }

    private EventHandler<MouseEvent> windowMouseDragged = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Point2D pointDragged = new Point2D(event.getScreenX(), event.getScreenY());
            Point2D pointDelta = pointDragged.subtract(pointPressed);

            if (isMouseResizeZone()) {
                // resize
                double newX = detachedWindow.getX(),
                    newY = detachedWindow.getY(),
                    newWidth = detachedWindow.getWidth(),
                    newHeight = detachedWindow.getHeight();

                Screen screen = Screen.getScreensForRectangle(newX, newY, newWidth, newHeight)
                    .get(0);

                if (sizeNorth) {
                    newHeight -= pointDelta.getY();
                    newY += pointDelta.getY();
                } else if (sizeSouth) {
                    newHeight += pointDelta.getY();
                }

                if (sizeWest) {
                    newWidth -= pointDelta.getX();
                    newX += pointDelta.getX();
                } else if (sizeEast) {
                    newWidth += pointDelta.getX();
                }

                double currentX = pointPressed.getX(),
                    currentY = pointPressed.getY();

                double maxWidth = getMaxWidth() > 0 ? getMaxWidth() : screen.getBounds().getWidth();
                double maxHeight = getMaxHeight() > 0 ? getMaxHeight() : screen.getBounds().getHeight();

                if (getMinWidth() <= newWidth && newWidth <= maxWidth) {
                    detachedWindow.setX(newX);
                    detachedWindow.setWidth(newWidth);
                    currentX = pointDragged.getX();
                }

                if (getMinHeight() <= newHeight && newHeight <= maxHeight) {
                    detachedWindow.setY(newY);
                    detachedWindow.setHeight(newHeight);
                    currentY = pointDragged.getY();
                }

                pointPressed = new Point2D(currentX, currentY);

                event.consume();
            } else if (isContainedInHierarchy(titleBar, (Node) event.getTarget())) {
                // drag
                detachedWindow.setX(detachedWindow.getX() + pointDelta.getX());
                detachedWindow.setY(detachedWindow.getY() + pointDelta.getY());
                pointPressed = pointDragged;
            }
        }
    };

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content) {
        init(mdiWindowID, icon, title, content);
    }

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content, boolean resizable) {
        this(mdiWindowID, icon, title, content);
        this.resizable.set(resizable);
    }

    public InternalWindow(String mdiWindowID, Node icon, String title, Node content, boolean resizable, boolean maximize) {
        this(mdiWindowID, icon, title, content);
        this.resizable.set(resizable);
        if (maximize) {
            center();
            maximizeOrRestoreWindow();
        }
    }

    private void updateStyle(PseudoClass pseudo, boolean newValue) {
        pseudoClassStateChanged(pseudo, newValue);
    }

    @Override
    public void toFront() {
        super.toFront();
        if (desktopPane != null) {
            desktopPane.setActiveWindow(this);
        }
    }

    public DesktopPane getDesktopPane() {
        return desktopPane;
    }

    public void setDesktopPane(DesktopPane desktopPane) {
        this.desktopPane = desktopPane;
    }

    public TitleBar getTitleBar() {
        return titleBar;
    }

    public boolean isActive() {
        return active.get();
    }

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    protected void setActive(boolean active) {
        this.active.set(active);
    }

    private void init(String mdiWindowID, Node icon, String title, Node content) {
        setId(mdiWindowID);
        moveListener();
        setOnMouseClicked((MouseEvent t) -> toFront());

        setPrefSize(200, 200);
        getStyleClass().add("internal-window");
        setTop(titleBar = new TitleBar(this, icon, title));
        setCenter(contentPane = makeContentPane(content));

        detachedWindow = new Stage();
        detachedWindow.setScene(new Scene(new BorderPane()));
        detachedWindow.focusedProperty().addListener((v, o, n) -> setActive(n));
        detachedWindow.initStyle(StageStyle.UNDECORATED);

        showingBinding = createBooleanBinding(() -> isVisible() | detachedWindow.isShowing(), visibleProperty(), detachedWindow.showingProperty());

        updateStyle(ACTIVE_CLASS, true);
        activeProperty().addListener((observable, oldValue, newValue) -> updateStyle(ACTIVE_CLASS, newValue));
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

    public void minimizeWindow() {
        if (isMinimized()) { return; }

        wasMaximized = isMaximized();
        maximized.set(false);
        minimized.set(true);

        if (isDetached()) {
            if (!wasMaximized) {
                captureDetachedWindowBounds();
            }
            detachedWindow.setIconified(true);
        } else {
            if (!wasMaximized) {
                captureBounds();
            }
            setVisible(false);
            setManaged(false);
        }
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MINIMIZED));
    }

    private void captureDetachedWindowBounds() {
        previousX = detachedWindow.getX();
        previousY = detachedWindow.getY();
        previousHeight = detachedWindow.getHeight();
        previousWidth = detachedWindow.getWidth();
    }

    private void captureBounds() {
        previousX = getLayoutX();
        previousY = getLayoutY();
        previousHeight = getHeight();
        previousWidth = getWidth();
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
            captureBounds();
        }
        maximized.set(true);
        wasMaximized = false;
        addListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        toFront();
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MAXIMIZED));
    }

    private void restoreInternalWindow() {
        setCapturedBounds();
        maximized.set(false);
        removeListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        toFront();
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_RESTORED));
    }

    private void setCapturedBounds() {
        setLayoutX(previousX);
        setLayoutY(previousY);
        setWidth(previousWidth);
        setHeight(previousHeight);
    }

    private void maximizeWindow(boolean recordSizes) {
        if (recordSizes) {
            captureDetachedWindowBounds();
            previousWidth = getWidth();
        }
        detachedWindow.setMaximized(true);
        maximized.set(true);
        wasMaximized = false;
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_MAXIMIZED));
    }

    private void restoreWindow() {
        detachedWindow.setX(previousX);
        detachedWindow.setY(previousY);
        detachedWindow.setWidth(previousWidth);
        detachedWindow.setHeight(previousHeight);

        detachedWindow.setMaximized(false);
        maximized.set(false);
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_RESTORED));
    }

    private Pane makeContentPane(Node content) {
        this.content = content;
        AnchorPane paneContent = new AnchorPane(content);
        paneContent.getStyleClass().add("internal-window-content");
        content.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #F2F2F2");
        AnchorPane.setBottomAnchor(content, 0d);
        AnchorPane.setLeftAnchor(content, 0d);
        AnchorPane.setRightAnchor(content, 0d);
        AnchorPane.setTopAnchor(content, 0d);
        return paneContent;
    }

    private void moveListener() {
        setOnMouseDragged((MouseEvent event) -> {
            if (!isMaximized()) {
                //Move
                x += event.getSceneX() - mousex;
                y += event.getSceneY() - mousey;
                //again set current Mouse x AND y position
                mousex = event.getSceneX();
                mousey = event.getSceneY();
                if (resizeMode == resizeMode.NONE && isContainedInHierarchy(titleBar, (Node) event.getTarget())) {
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
                    if (isResizable()) {
                        //Resize
                        //RIGHT AND DOWN
                        //Only the Right Resize
                        if (resizeRight && !resizeTop && !resizeBottom) {
                            if (event.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())) {
                                setPrefWidth(event.getX());
                            } else {
                                setPrefWidth(getParent().getLayoutBounds().getWidth() - getLayoutX());
                            }
                        } //Only The Bottom Resize
                        else if (!resizeRight && !resizeTop && resizeBottom) {
                            if (event.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                setPrefHeight(event.getY());
                            } else {
                                setPrefHeight(getParent().getLayoutBounds().getHeight() - getLayoutY());
                            }
                        } //Only The Bottom with Right Resize
                        else if ((resizeRight && !resizeTop && resizeBottom)) {
                            if (event.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())
                                && event.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                setPrefWidth(event.getX());
                                setPrefHeight(event.getY());
                            } else {
                                if (event.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())) {
                                    setPrefWidth(event.getX());
                                } else {
                                    setPrefWidth(getParent().getLayoutBounds().getWidth() - getLayoutX());
                                }
                                if (event.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                    setPrefHeight(event.getY());
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

        onMouseMovedProperty().set((MouseEvent event) -> {
            final double scaleX = localToSceneTransformProperty().getValue().getMxx();
            final double scaleY = localToSceneTransformProperty().getValue().getMyy();
            final double border1 = 5;
            double diffMinX = Math.abs(getBoundsInLocal().getMinX() - event.getX());
            double diffMinY = Math.abs(getBoundsInLocal().getMinY() - event.getY());
            double diffMaxX = Math.abs(getBoundsInLocal().getMaxX() - event.getX());
            double diffMaxY = Math.abs(getBoundsInLocal().getMaxY() - event.getY());
            boolean left1 = diffMinX * scaleX < border1;
            boolean top1 = diffMinY * scaleY < border1;
            boolean right1 = diffMaxX * scaleX < border1;
            boolean bottom1 = diffMaxY * scaleY < border1;
            resizeTop = false;
            resize = false;
            resizeBottom = false;
            resizeRight = false;

            /*if (left1 && !top1 && !bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.W_RESIZE);
                }
                resizeMode = ResizeMode.LEFT;
                resizeLeft = true;
            } else if (left1 && top1 && !bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.NW_RESIZE);
                }
                resizeMode = ResizeMode.TOP_LEFT;
                resizeLeft = true;
                resizeTop = true;
            } else if (left1 && !top1 && bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.SW_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM_LEFT;
                resizeLeft = true;
                resizeBottom = true;
            } else */if (right1 && !top1 && !bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.E_RESIZE);
                }
                resizeMode = ResizeMode.RIGHT;
                resizeRight = true;
            } /*else if (right1 && top1 && !bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.NE_RESIZE);
                }
                resizeMode = ResizeMode.TOP_RIGHT;
                resizeRight = true;
                resizeTop = true;
            } */else if (right1 && !top1 && bottom1) {
                if (isResizable()) {
                    setCursor(Cursor.SE_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM_RIGHT;
                resizeRight = true;
                resizeBottom = true;
            } /*else if (top1 && !left1 && !right1) {
                if (isResizable()) {
                    setCursor(Cursor.N_RESIZE);
                }
                resizeMode = ResizeMode.TOP;
                resizeTop = true;
            } */else if (bottom1 && !left1 && !right1) {
                if (isResizable()) {
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
            detachedWindow = null;
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

    private List<String> collectStylesheets() {
        List<String> stylesheets = new ArrayList<>();
        Parent parent = getParent();
        while (parent != null) {
            stylesheets.addAll(parent.getStylesheets());
            parent = parent.getParent();
        }
        stylesheets.addAll(getScene().getStylesheets());
        return stylesheets;
    }

    private DesktopPane dp;

    public void detachOrAttachWindow() {
        setDetached(!isDetached());

        if (isDetached()) {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_DETACHED));

            Point2D locationOnScreen = this.localToScreen(0, 0);
            detachedWindow.getScene().getStylesheets().setAll(collectStylesheets());

            captureBounds();

            dp = desktopPane.removeInternalWindow(this);

            double width = contentPane.getWidth();
            double height = titleBar.getHeight() + contentPane.getHeight();
            BorderPane bp = new BorderPane();
            bp.setId(getId());
            bp.getStyleClass().addAll(getStyleClass());

            bp.setMinWidth(getMinWidth());
            bp.setMinHeight(getMinHeight());
            bp.setPrefWidth(width);
            bp.setPrefHeight(height);

            bp.setTop(titleBar);
            bp.setCenter(contentPane);
            detachedWindow.getScene().setRoot(bp);
            detachedWindow.sizeToScene();

            detachedWindow.addEventHandler(MouseEvent.MOUSE_PRESSED, windowMousePressed);
            detachedWindow.addEventHandler(MouseEvent.MOUSE_MOVED, windowMouseMoved);
            detachedWindow.addEventHandler(MouseEvent.MOUSE_DRAGGED, windowMouseDragged);

            detachedWindow.setX(locationOnScreen.getX());
            detachedWindow.setY(locationOnScreen.getY());
            detachedWindow.show();

            if (isMaximized()) {
                detachedWindow.setMaximized(true);
            } else {
                bp.setMaxWidth(getMaxWidth());
                bp.setMaxHeight(getMaxHeight());
            }
        } else {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.EVENT_ATTACHED));
            detachedWindow.hide();
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_PRESSED, windowMousePressed);
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_MOVED, windowMouseMoved);
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_DRAGGED, windowMouseDragged);

            setTop(titleBar);
            setCenter(contentPane);

            captureDetachedWindowBounds();

            setWidth(previousWidth);
            setHeight(previousHeight);

            Bounds boundsInScreen = dp.localToScreen(dp.getBoundsInLocal());
            previousX = Math.max(previousX - boundsInScreen.getMinX(), 0);
            previousY = Math.max(previousY - boundsInScreen.getMinY(), 0);

            double maxX = boundsInScreen.getMaxX() - boundsInScreen.getMinX();
            if (previousX + previousWidth > maxX) {
                previousX = maxX - previousWidth;
            }

            double maxY = boundsInScreen.getMaxY() - boundsInScreen.getMinY();
            if (previousY + previousHeight > maxY) {
                previousY = maxY - previousHeight;
            }

            dp.addInternalWindow(this, new Point2D(previousX, previousY));
        }
    }

    private void removeListenerToResizeMaximizedWindows() {
        AnchorPane.clearConstraints(this);
        setCapturedBounds();
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

    public boolean isDetached() {
        return detached.get();
    }

    public BooleanProperty detachedProperty() {
        return detached;
    }

    public void setDetached(boolean detached) {
        this.detached.set(detached);
    }

    public boolean isResizable() {
        return resizable.get();
    }

    public BooleanProperty resizableProperty() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable.set(resizable);
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
}
