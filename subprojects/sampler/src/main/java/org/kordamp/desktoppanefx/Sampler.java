/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2015-2019 The original authors
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
package org.kordamp.desktoppanefx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.kordamp.desktoppanefx.scene.layout.DesktopPane;
import org.kordamp.desktoppanefx.scene.layout.InternalWindow;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * @author Lincoln Minto
 * @author Andres Almiray
 */
public class Sampler extends Application {
    int count = 0;
    public static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) throws Exception {
        hostServices = getHostServices();
        //Creat main Pane Layout
        AnchorPane mainPane = new AnchorPane();
        mainPane.setPrefSize(800, 600);
        //Creat MDI Canvas Container
        DesktopPane desktopPane = new DesktopPane();
        //Fit it to the main Pane
        AnchorPane.setBottomAnchor(desktopPane, 0d);
        AnchorPane.setLeftAnchor(desktopPane, 0d);
        AnchorPane.setTopAnchor(desktopPane, 25d);//Button snapTo
        AnchorPane.setRightAnchor(desktopPane, 0d);
        //Put the container Into the main pane
        mainPane.getChildren().add(desktopPane);
        //Create a 'New MDI Window' Button
        Button newWindowButton = new Button("New Window");
        //set the button action

        newWindowButton.setOnAction(event -> {
            Node content = null;
            try {
                content = FXMLLoader.load(getClass().getResource("/MyContent.fxml"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            count++;
            //Create a new window
            InternalWindow internalWindow = new InternalWindow("UniqueID" + count,
                new FontIcon("mdi-application:20:RED"),
                "Title " + count,
                content);
            //Add it to the container
            desktopPane.addInternalWindow(internalWindow);
        });
        //Put it into the main pane
        mainPane.getChildren().add(newWindowButton);

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }
}
