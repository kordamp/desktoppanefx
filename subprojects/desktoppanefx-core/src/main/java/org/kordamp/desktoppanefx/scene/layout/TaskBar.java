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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private final static int TASKBAR_HEIGHT_WITH_SCROLL = TASKBAR_HEIGHT_WITHOUT_SCROLL + 20;
    private final ScrollPane taskBar;
    private final HBox taskBarContentPane;

    private final ObjectProperty<Position> position = new SimpleObjectProperty<>(this, "position", Position.BOTTOM);

    public enum Position {
        TOP,
        BOTTOM
    }

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

    public ScrollPane getTaskBar() {
        return taskBar;
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

    public ObjectProperty<Position> positionProperty() {
        return position;
    }

    public Position getPosition() {
        return position.get();
    }

    public void setPosition(Position position) {
        this.position.set(position);
    }
}
