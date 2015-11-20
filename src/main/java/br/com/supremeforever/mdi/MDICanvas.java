package br.com.supremeforever.mdi;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by brisatc171.minto on 12/11/2015.
 */
public class MDICanvas extends VBox {

    private final ScrollPane taskBar;
    private HBox tbWindows;
    private final AnchorPane paneMDIContainer;
    private MDICanvas mdiCanvas = this;
    private final int taskbarHeightWithoutScrool = 44;
    private final int taskbarHeightWithScrool = 54;

    /**
     * *********** CONSTRUICTOR *************
     */
    public MDICanvas() {
        super();
        setAlignment(Pos.TOP_LEFT);

        paneMDIContainer = new AnchorPane();
        paneMDIContainer.setId("MDIContainer");

        tbWindows = new HBox();
        tbWindows.setSpacing(3);
        tbWindows.setMaxHeight(taskbarHeightWithoutScrool);
        tbWindows.setMinHeight(taskbarHeightWithoutScrool);
        tbWindows.setAlignment(Pos.CENTER_LEFT);
        setVgrow(paneMDIContainer, Priority.ALWAYS);
        taskBar = new ScrollPane(tbWindows);
        taskBar.setMaxHeight(taskbarHeightWithoutScrool);
        taskBar.setMinHeight(taskbarHeightWithoutScrool);
        taskBar.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        taskBar.setVmax(0);
        taskBar.setStyle(" -fx-border-color: #C4C4C4; "
                + " -fx-faint-focus-color: transparent; "
                + " -fx-focus-color: transparent; ");
        tbWindows.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Platform.runLater(() -> {
                if ((double) newValue <= taskBar.getWidth()) {
                    taskBar.setMaxHeight(taskbarHeightWithoutScrool);
                    taskBar.setPrefHeight(taskbarHeightWithoutScrool);
                    taskBar.setMinHeight(taskbarHeightWithoutScrool);
                } else {
                    taskBar.setMaxHeight(taskbarHeightWithScrool);
                    taskBar.setPrefHeight(taskbarHeightWithScrool);
                    taskBar.setMinHeight(taskbarHeightWithScrool);
                }
            });
        });
        getChildren().addAll(paneMDIContainer, taskBar);

        addEventHandler(MDIEvent.EVENT_CLOSED, mdiCloseHandler);
        addEventHandler(MDIEvent.EVENT_MINIMIZED, mdiMinimizedHandler);
    }

    /**
     * ***********************GETTER***********************************
     */
    public AnchorPane getPaneMDIContainer() {
        return paneMDIContainer;
    }

    public HBox getTbWindows() {
        return tbWindows;
    }

    /**
     * *************************REMOVE_WINDOW******************************
     */
    public void removeMDIWindow(String mdiWindowID) {
        Node mdi = getItemFromMDIContainer(mdiWindowID);
        Node iconBar = getItemFromToolBar(mdiWindowID);

        if (mdi != null) {
            getItemFromMDIContainer(mdiWindowID).isClosed(true);

            paneMDIContainer.getChildren().remove(mdi);
        }
        if (iconBar != null) {
            tbWindows.getChildren().remove(iconBar);
        }
    }

    /**
     * *****************************ADD_WINDOW*********************************
     */
    public void addMDIWindow(MDIWindow mdiWindow) {
        if (getItemFromMDIContainer(mdiWindow.getId()) == null) {
            mdiWindow.setVisible(false);
            paneMDIContainer.getChildren().add(mdiWindow);
            Platform.runLater(() -> {
                centerMdiWindow(mdiWindow);
                mdiWindow.setVisible(true);
            });
            mdiWindow.toFront();
        } else {
            if (getItemFromToolBar(mdiWindow.getId()) != null) {
                tbWindows.getChildren().remove(getItemFromToolBar(mdiWindow.getId()));
            }
            for (int i = 0; i < paneMDIContainer.getChildren().size(); i++) {
                Node node = paneMDIContainer.getChildren().get(i);
                if (node.getId().equals(mdiWindow.getId())) {
                    node.toFront();
                    node.setVisible(true);
                }
            }
        }
    }

    /**
     * *****************************MDI_EVENT_HANDLERS**************************
     */
    public EventHandler<MDIEvent> mdiCloseHandler = new EventHandler<MDIEvent>() {
        @Override
        public void handle(MDIEvent event) {
            MDIWindow win = (MDIWindow) event.getTarget();
            tbWindows.getChildren().remove(getItemFromToolBar(win.getId()));
            win = null;
        }
    };
    public EventHandler<MDIEvent> mdiMinimizedHandler = new EventHandler<MDIEvent>() {
        @Override
        public void handle(MDIEvent event) {
            MDIWindow win = (MDIWindow) event.getTarget();
            String id = win.getId();
            if (getItemFromToolBar(id) == null) {
                try {
                    MDIIcon icon = new MDIIcon(event.imgLogo, mdiCanvas, win.getWindowsTitle());
                    icon.setId(win.getId());
                    icon.getBtnClose().disableProperty().bind(win.getBtnClose().disableProperty());
                    tbWindows.getChildren().add(icon);
                } catch (Exception ex) {
                    Logger.getLogger(MDICanvas.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    };

    /**
     * ***************** UTILITIES******************************************
     */
    public MDIIcon getItemFromToolBar(String id) {
        for (Node node : tbWindows.getChildren()) {
            if (node instanceof MDIIcon) {
                MDIIcon icon = (MDIIcon) node;
                //String key = icon.getLblName().getText();
                String key = icon.getId();
                if (key.equalsIgnoreCase(id)) {
                    return icon;
                }
            }
        }
        return null;
    }

    public MDIWindow getItemFromMDIContainer(String id) {
        for (Node node : paneMDIContainer.getChildren()) {
            if (node instanceof MDIWindow) {
                MDIWindow win = (MDIWindow) node;
                if (win.getId().equals(id)) {

                    return win;
                }
            }
        }
        return null;
    }

    public void centerMdiWindow(MDIWindow mdiWindow) {
        try {
            double w = getPaneMDIContainer().getLayoutBounds().getWidth();
            double h = getPaneMDIContainer().getLayoutBounds().getHeight();
            double windowsHeight = mdiWindow.getLayoutBounds().getHeight();//((AnchorPane) ((AnchorPane) mdiWindow.getCenter()).getChildren().get(0)).getHeight() + ((AnchorPane) mdiWindow.getTop()).getHeight();//
            double windowsWidth = mdiWindow.getLayoutBounds().getWidth();//((AnchorPane) ((AnchorPane) mdiWindow.getCenter()).getChildren().get(0)).getWidth();//
            mdiWindow.setPrefSize(windowsWidth, windowsHeight);
            mdiWindow.setLayoutX((int) (w / 2) - (int) (windowsWidth / 2));
            mdiWindow.setLayoutY((int) (h / 2) - (int) (windowsHeight / 2));

        } catch (Exception e) {
        }
    }
}
