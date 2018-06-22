package org.kordamp.desktoppanefx.scene.layout;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

/**
 * @author Andres Almiray
 */
public class TaskBar {
    private final static int TASKBAR_HEIGHT_WITHOUT_SCROLL = 44;
    private final static int TASKBAR_HEIGHT_WITH_SCROLL = TASKBAR_HEIGHT_WITHOUT_SCROLL + 10;
    private final ScrollPane taskBar;
    private final HBox taskBarContentPane;

    public TaskBar() {
        taskBarContentPane = new HBox();
        taskBarContentPane.setSpacing(3);
        taskBarContentPane.setMaxHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
        taskBarContentPane.setMinHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
        taskBarContentPane.setAlignment(Pos.CENTER_LEFT);

        taskBar = new ScrollPane(taskBarContentPane);
        taskBar.setMaxHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
        taskBar.setMinHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
        taskBar.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        taskBar.setVmax(0);
        taskBar.getStyleClass().add("desktoppane-taskbar");

        taskBarContentPane.widthProperty().addListener((o, v, n) -> {
            Platform.runLater(() -> {
                if (n.doubleValue() <= taskBar.getWidth()) {
                    taskBar.setMaxHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
                    taskBar.setPrefHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
                    taskBar.setMinHeight(TASKBAR_HEIGHT_WITHOUT_SCROLL);
                } else {
                    taskBar.setMaxHeight(TASKBAR_HEIGHT_WITH_SCROLL);
                    taskBar.setPrefHeight(TASKBAR_HEIGHT_WITH_SCROLL);
                    taskBar.setMinHeight(TASKBAR_HEIGHT_WITH_SCROLL);
                }
            });
        });
    }

    ScrollPane getTaskBar() {
        return taskBar;
    }

    public HBox getTaskBarContentPane() {
        return taskBarContentPane;
    }

    public void addTaskNode(Node node) {
        taskBarContentPane.getChildren().add(node);
    }

    public void removeTaskNode(Node node) {
        taskBarContentPane.getChildren().remove(node);
    }

    public ObservableList<Node> getTaskNodes() {
        return taskBarContentPane.getChildren();
    }
}
