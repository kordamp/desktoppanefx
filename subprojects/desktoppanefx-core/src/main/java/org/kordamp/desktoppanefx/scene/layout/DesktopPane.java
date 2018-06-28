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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class DesktopPane extends BorderPane {
    private final AnchorPane internalWindowContainer;
    private final TaskBar taskBar;
    private final ObservableList<InternalWindow> internalWindows = FXCollections.observableArrayList();
    private final ObservableList<InternalWindow> unmodifiableInternalWindows = FXCollections.unmodifiableObservableList(internalWindows);

    public DesktopPane() {
        super();
        getStylesheets().add("/org/kordamp/desktoppanefx/scene/layout/default-desktoppane-stylesheet.css");

        internalWindowContainer = new AnchorPane();
        internalWindowContainer.getStyleClass().add("desktoppane");

        taskBar = new TaskBar();
        setCenter(internalWindowContainer);
        setBottom(taskBar.getTaskBar());

        addEventHandler(InternalWindowEvent.EVENT_CLOSED, mdiCloseHandler);
        addEventHandler(InternalWindowEvent.EVENT_MINIMIZED, mdiMinimizedHandler);

        taskBar.positionProperty().addListener((v, o, position) -> {
            getChildren().remove(taskBar.getTaskBar());
            switch (position) {
                case TOP:
                    setTop(taskBar.getTaskBar());
                    break;
                case BOTTOM:
                    setBottom(taskBar.getTaskBar());
            }
        });
    }

    public ObservableList<InternalWindow> getInternalWindows() {
        return unmodifiableInternalWindows;
    }

    public TaskBar getTaskBar() {
        return taskBar;
    }

    public DesktopPane addInternalWindow(InternalWindow internalWindow) {
        if (internalWindow != null) {
            if (findInternalWindow(internalWindow.getId()).isPresent()) {
                restoreExisting(internalWindow);
            } else {
                addNew(internalWindow, null);
            }
            internalWindow.setDesktopPane(this);
        }
        return this;
    }

    public DesktopPane addInternalWindow(InternalWindow internalWindow, Point2D position) {
        if (internalWindow != null) {
            if (findInternalWindow(internalWindow.getId()).isPresent()) {
                restoreExisting(internalWindow);
            } else {
                addNew(internalWindow, position);
            }
            internalWindow.setDesktopPane(this);
        }
        return this;
    }

    public DesktopPane removeInternalWindow(InternalWindow internalWindow) {
        ObservableList<Node> windows = internalWindowContainer.getChildren();
        if (internalWindow != null && windows.contains(internalWindow)) {
            internalWindows.remove(internalWindow);
            windows.remove(internalWindow);
            internalWindow.setDesktopPane(null);
        }

        return this;
    }

    public DesktopPane removeInternalWindow(String windowId) {
        findInternalWindow(windowId).ifPresent(internalWindow -> {
            internalWindow.setClosed(true);
            internalWindows.remove(internalWindow);
            internalWindowContainer.getChildren().remove(internalWindow);
        });

        findTaskBarIcon(windowId).ifPresent(taskBar::removeTaskBarIcon);

        return this;
    }

    private void addNew(InternalWindow internalWindow, Point2D position) {
        // internalWindow.setVisible(false);
        internalWindows.add(internalWindow);
        internalWindowContainer.getChildren().add(internalWindow);
        if (position == null) {
            internalWindow.layoutBoundsProperty().addListener(new WidthChangeListener(this, internalWindow));
        } else {
            placeInternalWindow(internalWindow, position);
        }
        internalWindow.toFront();
        // internalWindow.setVisible(true);
    }

    private void restoreExisting(InternalWindow internalWindow) {
        findTaskBarIcon(internalWindow.getId())
            .ifPresent(taskBar::removeTaskBarIcon);

        if (internalWindows.contains(internalWindow)) {
            internalWindow.toFront();
            internalWindow.setVisible(true);
        }
    }

    private final EventHandler<InternalWindowEvent> mdiCloseHandler = new EventHandler<InternalWindowEvent>() {
        @Override
        public void handle(InternalWindowEvent event) {
            findTaskBarIcon(((InternalWindow) event.getTarget()).getId())
                .ifPresent(taskBar::removeTaskBarIcon);
        }
    };

    private final EventHandler<InternalWindowEvent> mdiMinimizedHandler = new EventHandler<InternalWindowEvent>() {
        @Override
        public void handle(InternalWindowEvent event) {
            InternalWindow internalWindow = (InternalWindow) event.getTarget();
            String id = internalWindow.getId();
            if (!findTaskBarIcon(id).isPresent()) {
                try {
                    TaskBarIcon icon = new TaskBarIcon(event.getInternalWindow().getIcon(), DesktopPane.this, internalWindow.getTitle());
                    icon.setId(internalWindow.getId());
                    icon.disableCloseProperty().bind(internalWindow.disableCloseProperty());
                    taskBar.addTaskBarIcon(icon);
                } catch (Exception ex) {
                    Logger.getLogger(DesktopPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };


    public Optional<TaskBarIcon> findTaskBarIcon(String id) {
        return taskBar.getTaskBarIcons().stream()
            .filter(icon -> icon.getId().equals(id))
            .findFirst();
    }

    public Optional<InternalWindow> findInternalWindow(String id) {
        return internalWindows.stream()
            .filter(window -> window.getId().equals(id))
            .findFirst();
    }

    public void snapTo(InternalWindow internalWindow, InternalWindow.AlignPosition alignPosition) {
        double canvasH = internalWindowContainer.getLayoutBounds().getHeight();
        double canvasW = internalWindowContainer.getLayoutBounds().getWidth();
        double mdiH = internalWindow.getLayoutBounds().getHeight();
        double mdiW = internalWindow.getLayoutBounds().getWidth();

        switch (alignPosition) {
            case CENTER:
                centerInternalWindow(internalWindow);
                break;
            case CENTER_LEFT:
                placeInternalWindow(internalWindow, new Point2D(0, (int) (canvasH / 2) - (int) (mdiH / 2)));
                break;
            case CENTER_RIGHT:
                placeInternalWindow(internalWindow, new Point2D((int) canvasW - (int) mdiW, (int) (canvasH / 2) - (int) (mdiH / 2)));
                break;
            case TOP_CENTER:
                placeInternalWindow(internalWindow, new Point2D((int) (canvasW / 2) - (int) (mdiW / 2), 0));
                break;
            case TOP_LEFT:
                placeInternalWindow(internalWindow, Point2D.ZERO);
                break;
            case TOP_RIGHT:
                placeInternalWindow(internalWindow, new Point2D((int) canvasW - (int) mdiW, 0));
                break;
            case BOTTOM_LEFT:
                placeInternalWindow(internalWindow, new Point2D(0, (int) canvasH - (int) mdiH));
                break;
            case BOTTOM_RIGHT:
                placeInternalWindow(internalWindow, new Point2D((int) canvasW - (int) mdiW, (int) canvasH - (int) mdiH));
                break;
            case BOTTOM_CENTER:
                placeInternalWindow(internalWindow, new Point2D((int) (canvasW / 2) - (int) (mdiW / 2), (int) canvasH - (int) mdiH));
                break;
        }
    }

    public void placeInternalWindow(InternalWindow internalWindow, Point2D point) {
        double windowsWidth = internalWindow.getLayoutBounds().getWidth();
        double windowsHeight = internalWindow.getLayoutBounds().getHeight();
        internalWindow.setPrefSize(windowsWidth, windowsHeight);

        double containerWidth = internalWindowContainer.getLayoutBounds().getWidth();
        double containerHeight = internalWindowContainer.getLayoutBounds().getHeight();
        if (containerWidth <= point.getX() || containerHeight <= point.getY()) {
            throw new PositionOutOfBoundsException(
                "Tried to snapTo MDI Window with ID " + internalWindow.getId() +
                    " at a coordinate " + point.toString() +
                    " that is beyond current size of the MDI container " +
                    containerWidth + "px x " + containerHeight + "px."
            );
        }

        if ((containerWidth - point.getX() < 40) ||
            (containerHeight - point.getY() < 40)) {
            throw new PositionOutOfBoundsException(
                "Tried to snapTo MDI Window with ID " + internalWindow.getId() +
                    " at a coordinate " + point.toString() +
                    " that is too close to the edge of the parent of size " +
                    containerWidth + "px x " + containerHeight + "px " +
                    " for user to comfortably grab the title bar with the mouse."
            );
        }

        internalWindow.setLayoutX((int) point.getX());
        internalWindow.setLayoutY((int) point.getY());
        //internalWindow.setVisible(true);
    }

    public void centerInternalWindow(InternalWindow internalWindow) {
        double w = internalWindowContainer.getLayoutBounds().getWidth();
        double h = internalWindowContainer.getLayoutBounds().getHeight();

        Platform.runLater(() -> {
            double windowsWidth = internalWindow.getLayoutBounds().getWidth();
            double windowsHeight = internalWindow.getLayoutBounds().getHeight();

            Point2D centerCoordinate = new Point2D(
                (int) (w / 2) - (int) (windowsWidth / 2),
                (int) (h / 2) - (int) (windowsHeight / 2)
            );
            this.placeInternalWindow(internalWindow, centerCoordinate);
        });
    }

    public static InternalWindow resolveInternalWindow(Node node) {
        if (node == null) {
            return null;
        }

        Node candidate = node;
        while (candidate != null) {
            if (candidate instanceof InternalWindow) {
                return (InternalWindow) candidate;
            }
            candidate = candidate.getParent();
        }

        return null;
    }

    public void toFront(String id) {
        if (id == null && id.isEmpty()) {
            return;
        }

        findTaskBarIcon(id).ifPresent(TaskBarIcon::restoreWindow);
        findInternalWindow(id).ifPresent(InternalWindow::toFront);
    }

    public void toFront(InternalWindow internalWindow) {
        if (internalWindow == null || !internalWindows.contains(internalWindow)) {
            return;
        }

        findTaskBarIcon(internalWindow.getId()).ifPresent(TaskBarIcon::restoreWindow);
        internalWindow.toFront();
    }

    private static class WidthChangeListener implements ChangeListener {
        private DesktopPane desktopPane;
        private InternalWindow window;

        public WidthChangeListener(DesktopPane desktopPane, InternalWindow window) {
            this.desktopPane = desktopPane;
            this.window = window;
        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            this.desktopPane.centerInternalWindow(this.window);
            observable.removeListener(this);
        }
    }
}
