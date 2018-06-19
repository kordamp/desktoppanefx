/*
 * Copyright 2015-2018 Andres Almiray
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lincoln Minto
 */
public class InternalWindow extends BorderPane {

    private double mousex = 0;
    private double mousey = 0;
    private double x = 0;
    private double y = 0;
    private Button btnClose;
    private Button btnMinimize;
    private Button btnMaximize;
    private BorderPane borderPane = this;
    private boolean isMaximized = false;
    private BooleanProperty isClosed = new SimpleBooleanProperty(false);
    private InternalWindow.ResizeMode resizeMode;
    private InternalWindow.AlignPosition alignPosition;
    private boolean RESIZE_TOP;
    private boolean RESIZE_LEFT;
    private boolean RESIZE_BOTTOM;
    private boolean RESIZE_RIGHT;
    private double previousWidthToResize;
    private double previousHeightToResize;
    private ImageView imgLogo;
    private AnchorPane mdiContent;
    private AnchorPane loadingScreen;
    private HBox loadingScreenHBox;
    Label lblTitle;
    private boolean disableResize = false;
    double lastLeftAnchor;
    double lastRightAnchor;
    double lastTopAnchor;
    double lastBottomAnchor;
    double lastY;
    double lastX;
    private String windowsTitle;

    /**
     * @param logoImage
     * @param title
     * @param content
     * @throws Exception
     */
    public InternalWindow(String mdiWindowID, ImageView logoImage, String title, Node content) {
        init(mdiWindowID, logoImage, title, content);
    }

    /**
     * @param logoImage
     * @param title
     * @param content
     * @param disableResize
     * @throws Exception
     */
    public InternalWindow(String mdiWindowID, ImageView logoImage, String title, Node content, boolean disableResize) {
        this.disableResize = disableResize;
        init(mdiWindowID, logoImage, title, content);
    }

    /**
     * @param logoImage
     * @param title
     * @param content
     * @param disableResize
     * @param maximize
     * @throws Exception
     */
    public InternalWindow(String mdiWindowID, ImageView logoImage, String title, Node content, boolean disableResize, boolean maximize) {
        this.disableResize = disableResize;
        init(mdiWindowID, logoImage, title, content);
        if (maximize) {
            centerMdiWindow();
            maximizeRestoreMdiWindow();
        }
    }

    private void init(String mdiWindowID, ImageView logoImage, String title, Node content) {
        this.setId(mdiWindowID);
        this.windowsTitle = title;
        imgLogo = logoImage;
        moveListener();
        bringToFrontListener();

        this.setPrefSize(200, 200);
        getStyleClass().add("mdiWindow");
//        this.styleProperty().bind(StylesCSS.mdiStyleProperty);

        this.setTop(makeTitlePane(title));
        mdiContent = makeContentPane(content);
        this.setCenter(mdiContent);
    }

    public Node getContent() {
        return ((AnchorPane) getCenter()).getChildren().get(0);
    }

    public void setMdiTitle(String title) {
        lblTitle.setText(title);
    }

    private AnchorPane makeTitlePane(String title) {
        // HEADER:
        AnchorPane paneTitle = new AnchorPane();
        paneTitle.setPrefHeight(32);
        paneTitle.getStyleClass().add("mdiWindowTitleBar");
//        paneTitle.styleProperty().bind(StylesCSS.mdiTitleBarStyleProperty);
        // TITLE:
        paneTitle.getChildren().add(makeTitle(title));
        paneTitle.setPadding(new Insets(0, 11, 0, 0));
        // BUTTONS:
        // Read from an input stream

        btnClose = new Button("", getImageFromAssets("close.png"));
        btnClose.getStyleClass().add("controlButtons");
//        btnClose.styleProperty().bind(StylesCSS.controlButtonsStyleProperty);
        btnClose.setOnMouseClicked((MouseEvent t) -> {
            closeMdiWindow();
        });
        btnMinimize = new Button("", getImageFromAssets("minimize.png"));
        btnMinimize.getStyleClass().add("controlButtons");
//        btnMinimize.styleProperty().bind(StylesCSS.controlButtonsStyleProperty);
        btnMinimize.setOnMouseClicked((MouseEvent t) -> {
            minimizeMdiWindow();
        });
        btnMaximize = new Button("", getImageFromAssets("maximize.png"));
        btnMaximize.getStyleClass().add("controlButtons");
//        btnMaximize.styleProperty().bind(StylesCSS.controlButtonsStyleProperty);
        btnMaximize.setOnMouseClicked((MouseEvent t) -> {
            maximizeRestoreMdiWindow();
        });
        if (!disableResize) {
            paneTitle.getChildren().add(makeControls(btnMinimize, btnMaximize, btnClose));
            //double click on title bar
            paneTitle.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    maximizeRestoreMdiWindow();
                }
            });
        } else {
            paneTitle.getChildren().add(makeControls(btnMinimize, btnClose));
        }

        return paneTitle;
    }

    public void minimizeMdiWindow() {
        borderPane.setVisible(false);
        borderPane.fireEvent(new MDIEvent(imgLogo, MDIEvent.EVENT_MINIMIZED));
    }

    public void maximizeRestoreMdiWindow() {

        Pane parent = (Pane) getParent();
        if (isMaximized == false) {
            lastX = getLayoutX();
            lastY = getLayoutY();
            previousHeightToResize = getHeight();
            previousWidthToResize = getWidth();
            isMaximized = true;
            try {
                btnMaximize.setGraphic(getImageFromAssets("restore.png"));
            } catch (Exception ex) {
                Logger.getLogger(InternalWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            addListenerToResizeMaximizedWindows();
        } else {
            setPrefSize(previousWidthToResize, previousHeightToResize);
            isMaximized = false;
            try {
                btnMaximize.setGraphic(getImageFromAssets("maximize.png"));
            } catch (Exception ex) {
                Logger.getLogger(InternalWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            removeListenerToResizeMaximizedWindows();
        }
    }

    private AnchorPane makeContentPane(Node content) {
        AnchorPane paneContent = new AnchorPane(content);
        content.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #F2F2F2");
        AnchorPane.setBottomAnchor(content, 0d);
        AnchorPane.setLeftAnchor(content, 0d);
        AnchorPane.setRightAnchor(content, 0d);
        AnchorPane.setTopAnchor(content, 0d);
        return paneContent;
    }

    private HBox makeTitle(String title) {
        HBox hbLeft = new HBox();
        hbLeft.setSpacing(10d);
        ImageView imvLogo = imgLogo != null ? imgLogo : new ImageView();
        lblTitle = new Label(title);
        lblTitle.getStyleClass().add("titleText");
//        lblTitle.setStyle("-fx-font-weight: bold;");
        //lblTitle.styleProperty().bind(StylesCSS.taskBarIconTextStyleProperty);

        hbLeft.getChildren().add(imvLogo);
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

    private void moveListener() {
        this.setOnMouseDragged((MouseEvent dragEvent) -> {
            if (!isMaximized) {
                //Move
                x += dragEvent.getSceneX() - mousex;
                y += dragEvent.getSceneY() - mousey;
                //again set current Mouse x AND y position
                mousex = dragEvent.getSceneX();
                mousey = dragEvent.getSceneY();
                if (resizeMode == resizeMode.NONE) {
                    //set the positon of Node after calculation
                    if (borderPane.getWidth() < borderPane.getParent().getLayoutBounds().getWidth()) {//if the panel is not biger then the window: Move
                        borderPane.setLayoutX(x);
                    }
                    if (borderPane.getHeight() < borderPane.getParent().getLayoutBounds().getHeight()) {//if the panel is not biger then the window: Move
                        borderPane.setLayoutY(y);
                    }

                    //LEFT AND RIGHT
                    if (borderPane.getLayoutX() <= borderPane.getParent().getLayoutX()) {
                        borderPane.setLayoutX(borderPane.getParent().getLayoutX());
                    } else if ((borderPane.getLayoutX() + borderPane.getWidth()) >= borderPane.getParent().getLayoutBounds().getWidth()) {
                        borderPane.setLayoutX(borderPane.getParent().getLayoutBounds().getWidth() - borderPane.getWidth());
                    }
                    //UP AND DOWN
                    if (borderPane.getLayoutY() <= borderPane.getParent().getLayoutX()) {
                        borderPane.setLayoutY(borderPane.getParent().getLayoutY());
                    } else if ((borderPane.getLayoutY() + borderPane.getHeight()) >= borderPane.getParent().getLayoutBounds().getHeight()) {
                        borderPane.setLayoutY(borderPane.getParent().getLayoutBounds().getHeight() - borderPane.getHeight());
                    }
                } else {
                    if (!disableResize) {
                        //Resize
                        //RIGHT AND DOWN
                        //Only the Right Resize
                        if (RESIZE_RIGHT && !RESIZE_TOP && !RESIZE_BOTTOM) {
                            if (dragEvent.getX() <= (getParent().getLayoutBounds().getWidth() - getLayoutX())) {
                                setPrefWidth(dragEvent.getX());
                            } else {
                                setPrefWidth(getParent().getLayoutBounds().getWidth() - getLayoutX());
                            }
                        } //Only The Bottom Resize
                        else if (!RESIZE_RIGHT && !RESIZE_TOP && RESIZE_BOTTOM) {
                            if (dragEvent.getY() <= (getParent().getLayoutBounds().getHeight() - getLayoutY())) {
                                setPrefHeight(dragEvent.getY());
                            } else {
                                setPrefHeight(getParent().getLayoutBounds().getHeight() - getLayoutY());
                            }
                        } //Only The Bottom with Right Resize
                        else if ((RESIZE_RIGHT && !RESIZE_TOP && RESIZE_BOTTOM)) {
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
                        } //Only The Left Resize
                        else if (RESIZE_LEFT && !RESIZE_TOP && !RESIZE_BOTTOM) {
                            // DO NOTHING YET:
                            //TODO
                        }
                    }
                }
            }
        });

        setOnMousePressed((MouseEvent event) -> {
            borderPane.toFront();
            mousex = event.getSceneX();
            mousey = event.getSceneY();
            x = borderPane.getLayoutX();
            y = borderPane.getLayoutY();
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
            RESIZE_TOP = false;
            RESIZE_LEFT = false;
            RESIZE_BOTTOM = false;
            RESIZE_RIGHT = false;
            if (left1 && !top1 && !bottom1) {
            } else if (left1 && top1 && !bottom1) {
            } else if (left1 && !top1 && bottom1) {
            } else if (right1 && !top1 && !bottom1) {
                if (!disableResize) {
                    setCursor(Cursor.E_RESIZE);
                }
                resizeMode = ResizeMode.RIGHT;
                RESIZE_RIGHT = true;
            } else if (right1 && top1 && !bottom1) {
            } else if (right1 && !top1 && bottom1) {
                if (!disableResize) {
                    setCursor(Cursor.SE_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM_RIGHT;
                RESIZE_RIGHT = true;
                RESIZE_BOTTOM = true;
            } else if (top1 && !left1 && !right1) {
            } else if (bottom1 && !left1 && !right1) {
                if (!disableResize) {
                    setCursor(Cursor.S_RESIZE);
                }
                resizeMode = ResizeMode.BOTTOM;
                RESIZE_BOTTOM = true;
            } else {
                setCursor(Cursor.DEFAULT);
                resizeMode = ResizeMode.NONE;
            }
        });

    }

    public void placeMdiWindow(AlignPosition alignPosition) {
        Platform.runLater(() -> {
            ((DesktopPane) this.getParent().getParent()).placeMdiWindow(this, alignPosition);
        });
    }

    public void placeMdiWindow(Point2D point) {
        Platform.runLater(() -> {
            ((DesktopPane) this.getParent().getParent()).placeMdiWindow(this, point);
        });
    }

    public void centerMdiWindow() {
        Platform.runLater(() -> {
            ((DesktopPane) this.getParent().getParent()).centerMdiWindow(this);
        });
    }

    public void closeMdiWindow() {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), borderPane);
        st.setToX(0);
        st.setToY(0);
        st.setByX(1);
        st.setByY(1);

        st.setCycleCount(1);

        st.play();
        borderPane.fireEvent(new MDIEvent(null, MDIEvent.EVENT_CLOSED));
        st.setOnFinished((ActionEvent t) -> {

            DesktopPane desktopPane = (DesktopPane) this.getParent().getParent();
            for (int i = 0; i < desktopPane.getPaneMDIContainer().getChildren().size(); i++) {
                InternalWindow window = (InternalWindow) desktopPane.getPaneMDIContainer().getChildren().get(i);
                if (window.getId().equals(borderPane.getId())) {
                    desktopPane.getPaneMDIContainer().getChildren().remove(i);
                }
            }
            isClosed.setValue(true);
        });
    }

    private void bringToFrontListener() {

        this.setOnMouseClicked((MouseEvent t) -> {
            borderPane.toFront();
        });
    }

    private ImageView getImageFromAssets(String imageName) {
        InputStream in = getClass().getResourceAsStream("/assets/" + imageName);
        Image imgClose = new Image(in);
        ImageView imvClose = new ImageView(imgClose);
        return imvClose;
    }

    private void removeListenerToResizeMaximizedWindows() {
        AnchorPane.clearConstraints(borderPane);
        setLayoutX((int) lastX);
        setLayoutY((int) lastY);
    }

    private void addListenerToResizeMaximizedWindows() {
        AnchorPane.setBottomAnchor(borderPane, 0d);
        AnchorPane.setTopAnchor(borderPane, 0d);
        AnchorPane.setLeftAnchor(borderPane, 0d);
        AnchorPane.setRightAnchor(borderPane, 0d);
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

    public BooleanProperty getIsClosedProperty() {
        return isClosed;
    }


    public boolean isClosed() {
        return isClosed.getValue();
    }

    public void isClosed(boolean value) {
        isClosed.setValue(value);
    }

    public Button getBtnClose() {
        return btnClose;
    }

    public void setBtnClose(Button btnClose) {
        this.btnClose = btnClose;
    }

    public String getWindowsTitle() {
        return windowsTitle;
    }

    public void setWindowsTitle(String windowsTitle) {
        this.windowsTitle = windowsTitle;
    }

    public Button getBtnMinimize() {
        return btnMinimize;
    }

    public void setBtnMinimize(Button btnMinimize) {
        this.btnMinimize = btnMinimize;
    }

}
