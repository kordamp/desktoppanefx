/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2015-2022 The original authors
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

/**
 * @author Andres Almiray
 */
public class TaskBar {
    private final static int TASKBAR_HEIGHT_WITHOUT_SCROLL = 46;
    private final static int TASKBAR_HEIGHT_WITH_SCROLL = TASKBAR_HEIGHT_WITHOUT_SCROLL + 20;

    private final ScrollPane taskBar;
    private final HBox taskBarContentPane;
    private final ObservableList<TaskBarIcon> icons = FXCollections.observableArrayList();
    private final ObservableList<TaskBarIcon> unmodifiableIcons = FXCollections.unmodifiableObservableList(icons);

    private final ObjectProperty<Position> position = new SimpleObjectProperty<>(this, "position", Position.BOTTOM);
    private final BooleanProperty visible = new SimpleBooleanProperty(this, "visible", true);

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

        taskBar.visibleProperty().bind(visible);
        taskBar.managedProperty().bind(visible);

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

    public ObservableList<TaskBarIcon> getTaskBarIcons() {
        return unmodifiableIcons;
    }

    ScrollPane getTaskBarComponent() {
        return taskBar;
    }

    public Optional<TaskBarIcon> findTaskBarIcon(String id) {
        return getTaskBarIcons().stream()
            .filter(icon -> icon.getId().equals(id))
            .findFirst();
    }

    public void addTaskBarIcon(TaskBarIcon icon) {
        if (icon != null && !icons.contains(icon)) {
            icons.add(icon);
            taskBarContentPane.getChildren().add(icon);
        }
    }

    public void removeTaskBarIcon(TaskBarIcon icon) {
        if (icon != null) {
            icons.remove(icon);
            taskBarContentPane.getChildren().remove(icon);
        }
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

    public boolean isVisible() {
        return visible.get();
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }
}
