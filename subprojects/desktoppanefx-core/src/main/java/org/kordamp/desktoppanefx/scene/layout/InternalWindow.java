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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kordamp.desktoppanefx.scene.layout;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
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

    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);
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

    public void updateStyle(PseudoClass pseudo, boolean newValue) {
        pseudoClassStateChanged(pseudo, newValue);
    }

    @Override
    public void toFront() {
        moveToFront();
        if (desktopPane != null) {
            desktopPane.setActiveWindow(this);
        }
    }

    protected void moveToFront() {
        super.toFront();
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
        boolean previousActive = this.active.get();
        this.active.set(active);

        if (previousActive != active) {
            if (active) {
                fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_ACTIVATED));
            } else {
                fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_DEACTIVATED));
            }
        }
    }

    private void init(String windowId, Node icon, String title, Node content) {
        setId(windowId);
        moveListener();
        setOnMouseClicked(event -> toFront());

        setPrefSize(200, 200);
        getStyleClass().add("internal-window");
        setTop(titleBar = createTitleBar(icon, title));
        setCenter(contentPane = makeContentPane(content));

        detachedWindow = createDetachedWindow();

        showingBinding = createBooleanBinding(() -> isVisible() | detachedWindow.isShowing(),
            visibleProperty(), detachedWindow.showingProperty());

        updateStyle(ACTIVE_CLASS, true);
        activeProperty().addListener((observable, oldValue, newValue) -> updateStyle(ACTIVE_CLASS, newValue));
    }

    protected Stage createDetachedWindow() {
        Stage detachedWindow = new Stage();
        detachedWindow.setScene(new Scene(new BorderPane()));
        detachedWindow.initStyle(StageStyle.TRANSPARENT);
        detachedWindow.getScene().setFill(null);
        return detachedWindow;
    }

    protected TitleBar createTitleBar(Node icon, String title) {
        return new TitleBar(this, icon, title);
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
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MINIMIZING));

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
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MINIMIZED));
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
        if (previousWidth < 1) {
            previousWidth = getPrefWidth();
        }
        if (previousHeight < 1) {
            previousHeight = getPrefHeight();
        }
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
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MAXIMIZING));
        if (recordSizes) {
            captureBounds();
        }
        maximized.set(true);
        wasMaximized = false;
        addListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        toFront();
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MAXIMIZED));
    }

    private void restoreInternalWindow() {
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_RESTORING));
        maximized.set(false);
        removeListenerToResizeMaximizedWindows();
        setVisible(true);
        setManaged(true);
        toFront();
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_RESTORED));
    }

    private void setCapturedBounds() {
        setLayoutX(previousX);
        setLayoutY(previousY);
        setPrefSize(previousWidth, previousHeight);
    }

    private void maximizeWindow(boolean recordSizes) {
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MAXIMIZING));
        if (recordSizes) {
            captureDetachedWindowBounds();
            // previousWidth = getWidth();
        }
        maximizeDetachedWindow();

        maximized.set(true);
        wasMaximized = false;
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_MAXIMIZED));
    }

    private void maximizeDetachedWindow() {
        Screen screen = resolveScreen();
        detachedWindow.setX(0);
        detachedWindow.setY(titleBar.getHeight());
        detachedWindow.setWidth(screen.getBounds().getWidth());
        detachedWindow.setHeight(screen.getBounds().getHeight() - titleBar.getHeight());
    }

    private void restoreWindow() {
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_RESTORING));
        detachedWindow.setX(previousX);
        detachedWindow.setY(previousY);
        detachedWindow.setWidth(previousWidth);
        detachedWindow.setHeight(previousHeight);

        maximized.set(false);
        fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_RESTORED));
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
            } else */
            if (right1 && !top1 && !bottom1) {
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
            } */ else if (right1 && !top1 && bottom1) {
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
            } */ else if (bottom1 && !left1 && !right1) {
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
        InternalWindowEvent event = new InternalWindowEvent(this, InternalWindowEvent.WINDOW_CLOSE_REQUEST);
        fireEvent(event);

        if (event.isConsumed()) {
            return;
        }

        if (isDetached()) {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_HIDING));
            detachedWindow.close();
            detachedWindow = null;
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_HIDDEN));
        } else {
            ScaleTransition st = hideWindow();

            st.setOnFinished(t -> {
                if (desktopPane != null) {
                    desktopPane.removeInternalWindow(this);
                }
                closed.setValue(true);
                fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_HIDDEN));
            });

            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_HIDING));
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
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_DETACHING));

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

            detachedWindow.addEventHandler(MouseEvent.MOUSE_PRESSED, windowMousePressed);
            detachedWindow.addEventHandler(MouseEvent.MOUSE_MOVED, windowMouseMoved);
            detachedWindow.addEventHandler(MouseEvent.MOUSE_DRAGGED, windowMouseDragged);

            detachedWindow.setX(locationOnScreen.getX());
            detachedWindow.setY(locationOnScreen.getY());

            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_DETACHED));
            detachedWindow.show();

            if (isMaximized()) {
                maximizeDetachedWindow();
            } else {
                bp.setMaxWidth(getMaxWidth());
                bp.setMaxHeight(getMaxHeight());
            }
        } else {
            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_ATTACHING));
            detachedWindow.hide();
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_PRESSED, windowMousePressed);
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_MOVED, windowMouseMoved);
            detachedWindow.removeEventHandler(MouseEvent.MOUSE_DRAGGED, windowMouseDragged);

            setTop(titleBar);
            setCenter(contentPane);

            captureDetachedWindowBounds();

            setPrefSize(previousWidth, previousHeight);

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

            fireEvent(new InternalWindowEvent(this, InternalWindowEvent.WINDOW_ATTACHED));
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

    private Screen resolveScreen() {
        double newX = detachedWindow.getX(),
            newY = detachedWindow.getY(),
            newWidth = detachedWindow.getWidth(),
            newHeight = detachedWindow.getHeight();

        return Screen.getScreensForRectangle(newX, newY, newWidth, newHeight).get(0);
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

    // -- Event handling

    private ObjectProperty<EventHandler<InternalWindowEvent>> onShowing;

    public final void setOnShowing(EventHandler<InternalWindowEvent> value) {
        onShowingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnShowing() {
        return onShowing == null ? null : onShowing.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onShowingProperty() {
        if (onShowing == null) {
            onShowing = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_SHOWING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onShowing";
                }
            };
        }
        return onShowing;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onShown;

    public final void setOnShown(EventHandler<InternalWindowEvent> value) {
        onShownProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnShown() {
        return onShown == null ? null : onShown.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onShownProperty() {
        if (onShown == null) {
            onShown = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_SHOWN, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onShown";
                }
            };
        }
        return onShown;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onHiding;

    public final void setOnHiding(EventHandler<InternalWindowEvent> value) {
        onHidingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnHiding() {
        return onHiding == null ? null : onHiding.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onHidingProperty() {
        if (onHiding == null) {
            onHiding = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_HIDING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onHiding";
                }
            };
        }
        return onHiding;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onHidden;

    public final void setOnHidden(EventHandler<InternalWindowEvent> value) {
        onHiddenProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnHidden() {
        return onHidden == null ? null : onHidden.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onHiddenProperty() {
        if (onHidden == null) {
            onHidden = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_HIDDEN, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onHidden";
                }
            };
        }
        return onHidden;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onCloseRequest;

    public final void setOnCloseRequest(EventHandler<InternalWindowEvent> value) {
        onCloseRequestProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnCloseRequest() {
        return (onCloseRequest != null) ? onCloseRequest.get() : null;
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>>
    onCloseRequestProperty() {
        if (onCloseRequest == null) {
            onCloseRequest = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_CLOSE_REQUEST, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onCloseRequest";
                }
            };
        }
        return onCloseRequest;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onMinimizing;

    public final void setOnMinimizing(EventHandler<InternalWindowEvent> value) {
        onMinimizingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnMinimizing() {
        return onMinimizing == null ? null : onMinimizing.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onMinimizingProperty() {
        if (onMinimizing == null) {
            onMinimizing = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_MINIMIZING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onMinimizing";
                }
            };
        }
        return onMinimizing;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onMinimized;

    public final void setOnMinimized(EventHandler<InternalWindowEvent> value) {
        onMinimizedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnMinimized() {
        return onMinimized == null ? null : onMinimized.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onMinimizedProperty() {
        if (onMinimized == null) {
            onMinimized = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_MINIMIZED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onMinimized";
                }
            };
        }
        return onMinimized;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onMaximizing;

    public final void setOnMaximizing(EventHandler<InternalWindowEvent> value) {
        onMaximizingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnMaximizing() {
        return onMaximizing == null ? null : onMaximizing.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onMaximizingProperty() {
        if (onMaximizing == null) {
            onMaximizing = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_MAXIMIZING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onMaximizing";
                }
            };
        }
        return onMaximizing;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onMaximized;

    public final void setOnMaximized(EventHandler<InternalWindowEvent> value) {
        onMaximizedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnMaximized() {
        return onMaximized == null ? null : onMaximized.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onMaximizedProperty() {
        if (onMaximized == null) {
            onMaximized = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_MAXIMIZED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onMaximized";
                }
            };
        }
        return onMaximized;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onRestoring;

    public final void setOnRestoring(EventHandler<InternalWindowEvent> value) {
        onRestoringProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnRestoring() {
        return onRestoring == null ? null : onRestoring.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onRestoringProperty() {
        if (onRestoring == null) {
            onRestoring = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_RESTORING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onRestoring";
                }
            };
        }
        return onRestoring;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onRestored;

    public final void setOnRestored(EventHandler<InternalWindowEvent> value) {
        onRestoredProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnRestored() {
        return onRestored == null ? null : onRestored.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onRestoredProperty() {
        if (onRestored == null) {
            onRestored = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_RESTORED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onRestored";
                }
            };
        }
        return onRestored;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onAttaching;

    public final void setOnAttaching(EventHandler<InternalWindowEvent> value) {
        onAttachingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnAttaching() {
        return onAttaching == null ? null : onAttaching.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onAttachingProperty() {
        if (onAttaching == null) {
            onAttaching = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_ATTACHING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onAttaching";
                }
            };
        }
        return onAttaching;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onAttached;

    public final void setOnAttached(EventHandler<InternalWindowEvent> value) {
        onAttachedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnAttached() {
        return onAttached == null ? null : onAttached.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onAttachedProperty() {
        if (onAttached == null) {
            onAttached = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_ATTACHED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onAttached";
                }
            };
        }
        return onAttached;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onDetaching;

    public final void setOnDetaching(EventHandler<InternalWindowEvent> value) {
        onDetachingProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnDetaching() {
        return onDetaching == null ? null : onDetaching.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onDetachingProperty() {
        if (onDetaching == null) {
            onDetaching = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_DETACHING, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onDetaching";
                }
            };
        }
        return onDetaching;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onDetached;

    public final void setOnDetached(EventHandler<InternalWindowEvent> value) {
        onDetachedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnDetached() {
        return onDetached == null ? null : onDetached.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onDetachedProperty() {
        if (onDetached == null) {
            onDetached = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_DETACHED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onDetached";
                }
            };
        }
        return onDetached;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onActivated;

    public final void setOnActivated(EventHandler<InternalWindowEvent> value) {
        onActivatedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnActivated() {
        return onActivated == null ? null : onActivated.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onActivatedProperty() {
        if (onActivated == null) {
            onActivated = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_ACTIVATED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onActivated";
                }
            };
        }
        return onActivated;
    }

    private ObjectProperty<EventHandler<InternalWindowEvent>> onDeactivated;

    public final void setOnDeactivated(EventHandler<InternalWindowEvent> value) {
        onDeactivatedProperty().set(value);
    }

    public final EventHandler<InternalWindowEvent> getOnDeactivated() {
        return onDeactivated == null ? null : onDeactivated.get();
    }

    public final ObjectProperty<EventHandler<InternalWindowEvent>> onDeactivatedProperty() {
        if (onDeactivated == null) {
            onDeactivated = new ObjectPropertyBase<EventHandler<InternalWindowEvent>>() {
                @Override
                protected void invalidated() {
                    setEventHandler(InternalWindowEvent.WINDOW_DEACTIVATED, get());
                }

                @Override
                public Object getBean() {
                    return InternalWindow.this;
                }

                @Override
                public String getName() {
                    return "onDeactivated";
                }
            };
        }
        return onDeactivated;
    }
}
