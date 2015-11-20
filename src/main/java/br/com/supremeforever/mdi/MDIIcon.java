package br.com.supremeforever.mdi;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.InputStream;

/**
 * Created by brisatc171.minto on 12/11/2015.
 */
public class MDIIcon extends Button {

    private Button btnClose;
    private Label lblName;
    private MDICanvas mdiCanvas;
    final String cssDefault = "-fx-border-color:blue;" + "-fx-border-width: 1;"
            + "-fx-spacing:5.0;" + "-fx-alignment:center-left ";

    /**
     * *************************** CONSTRUCTOR
     */
    public MDIIcon(ImageView imgLogo, MDICanvas mdiCanvas, String name) throws Exception {
        super();
        HBox hBox = new HBox();
        hBox.setStyle("-fx-alignment:center-left");
        setStyle("-fx-background-color:  "
                + " linear-gradient(#f2f2f2, #d6d6d6), "
                + " linear-gradient(#fcfcfc 0%, #d9d9d9 20%, #d6d6d6 100%), "
                + " linear-gradient(#dddddd 0%, #f6f6f6 50%); "
                + " -fx-background-radius: 8,7,6; "
                + " -fx-background-insets: 0,1,2; "
                + " -fx-text-fill: black; "
                + " -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );");
        //setStyle(cssDefault);
        hBox.setSpacing(10d);
        hBox.setPadding(new Insets(0, 10, 0, 10));
        this.mdiCanvas = mdiCanvas;
        lblName = new Label(name);
        lblName.setStyle("-fx-font-weight: bold;");
        addEventHandler(MouseEvent.MOUSE_CLICKED, handleMaximize);

        btnClose = new Button("", getImageFromAssets("close.png"));
//Adding the shadow when the mouse cursor is on
        final DropShadow shadowCloseBtn = new DropShadow();
        shadowCloseBtn.setHeight(10d);
        shadowCloseBtn.setWidth(10d);
        btnClose.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btnClose.setEffect(shadowCloseBtn));
//Removing the shadow when the mouse cursor is off
        btnClose.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            btnClose.setEffect(null);
            System.out.println("Height:" + getParent().getLayoutBounds().getHeight());
        });
        btnClose.addEventHandler(MouseEvent.MOUSE_CLICKED, handleClose);
        hBox.getChildren().addAll(imgLogo == null ? new ImageView() : new ImageView(imgLogo.getImage()), lblName, btnClose);
        setGraphic(hBox);
    }

    /**
     * ****************************** GET/SET
     */
    public Label getLblName() {
        return lblName;
    }

    /**
     * ******************************** BUTTON_HANDLERES
     */
    private EventHandler<MouseEvent> handleMaximize = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            MDIWindow win = mdiCanvas.getItemFromMDIContainer(getId());
            if (win != null) {
                win.setVisible(true);
                win.toFront();
                removeIcon();
            }
        }
    };
    private EventHandler<MouseEvent> handleClose = event -> {
        removeMDIWindow();
        removeIcon();
    };

    private void removeMDIWindow() {
        MDIWindow win = mdiCanvas.getItemFromMDIContainer(getId());
        if (win != null) {
            mdiCanvas.getPaneMDIContainer().getChildren().remove(win);
        }

    }

    private void removeIcon() {
        MDIIcon icon = mdiCanvas.getItemFromToolBar(getId());
        if (icon != null) {
            mdiCanvas.getTbWindows().getChildren().remove(icon);
        }
    }

    private ImageView getImageFromAssets(String imageName) throws Exception {
        if (imageName != null) {

            InputStream in = getClass().getResourceAsStream("/assets/" + imageName);
            Image imgClose = new Image(in);
            ImageView imvClose = new ImageView(imgClose);
            return imvClose;
        } else {
            return new ImageView();
        }
    }

    public Button getBtnClose() {
        return btnClose;
    }

    public void setBtnClose(Button btnClose) {
        this.btnClose = btnClose;
    }
}
