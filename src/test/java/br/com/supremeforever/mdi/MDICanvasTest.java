package br.com.supremeforever.mdi;

import br.com.supremeforever.mdi.Exception.PositionOutOfBoundsException;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.junit.Test;
import static org.junit.Assert.*;
import org.loadui.testfx.GuiTest;
import jfxtras.test.TestUtil;

public class MDICanvasTest extends GuiTest {
    
    private MDICanvas mdi;
    
    private int testCanvasWidth = 800;
    private int testCanvasHeight = 600;
    
    @Override
    public Parent getRootNode() {
        this.mdi = new MDICanvas();
        this.mdi.setPrefWidth(this.testCanvasWidth);
        this.mdi.setPrefHeight(this.testCanvasHeight);
        return this.mdi;
    }
    
    @Test
    public void testPlaceMdiWindowAtAPoint() {
        
        // ====== Setup ========
        MDIWindow mdiWindow = new MDIWindow(
                "testPlaceSpecificPoint",
                new ImageView("/assets/WindowIcon.png"),
                "Placed at point ",
                new AnchorPane()
        );
        Point2D position = new Point2D(5, 10);
        
        // ====== Act ==========
        TestUtil.runThenWaitForPaintPulse(() -> this.mdi.addMDIWindow(mdiWindow, position));

        // ====== Verify =======
        MDIWindow placedWindow = find("#testPlaceSpecificPoint");
        assertEquals("Window's x position should be same as x value of point parameter.",
                position.getX(), placedWindow.layoutXProperty().getValue(), 0
        );
        assertEquals("Window's y position should be same as y value of point parameter.",
                position.getY(), placedWindow.layoutYProperty().getValue(), 0
        );
        
    }
    
    @Test
    public void testPlaceMdiWindowInCenter() {
        
        // ====== Setup ========
        AnchorPane childContent = new AnchorPane();
        childContent.prefWidth(200);
        childContent.prefWidth(150);
        
        MDIWindow mdiWindow = new MDIWindow(
                "testPlaceCenter",
                new ImageView("/assets/WindowIcon.png"),
                "Placed in Center ",
                childContent
        );
        
        // ====== Act ==========
        TestUtil.runThenWaitForPaintPulse(() -> this.mdi.addMDIWindow(mdiWindow));
        
        // ====== Verify =======
        MDIWindow placedWindow = find("#testPlaceCenter");
        
        // allow for error margin of 1 pixel
        // horizontal center = (half of parent width) - (half of child width)
        // 300 = 800/2 - 200/2
        assertEquals("Window's x position should be half of canvas width.",
                300, placedWindow.layoutXProperty().getValue(), 1
        );
        
        // vertical center = (parent height - status bar)/2 - ((child height + title bar height)/2 + padding)
        // 178 ~~ (600 - 44)/2 - ((150 + 32)/2 + 11)
        assertEquals("Window's y position should be half of canvas height.",
                178, placedWindow.layoutYProperty().getValue(), 1
        );
        
    }
    
    @Test
    public void testPositionOutOfBoundsException() {
        // ====== Setup ========
        AnchorPane childContent = new AnchorPane();
        childContent.prefWidth(200);
        childContent.prefWidth(150);
        
        MDIWindow mdiWindow = new MDIWindow(
                "testPlaceInvalidCoordinate",
                new ImageView("/assets/WindowIcon.png"),
                "Placed beyond the parent",
                childContent
        );
        Point2D position = new Point2D(1000, 1000);
        
        // ====== Act ==========
        TestUtil.runThenWaitForPaintPulse(() -> {
            try{
                this.mdi.addMDIWindow(mdiWindow, position);
                fail("Should throw invalid exception when beyond bounds");
            } catch (PositionOutOfBoundsException e) {}
        });
    }
    
    @Test
    public void testPositionOutOfBoundsExceptionForUnusableLocation() {
        // ====== Setup ========
        AnchorPane childContent = new AnchorPane();
        childContent.prefWidth(200);
        childContent.prefWidth(150);
        
        MDIWindow mdiWindow = new MDIWindow(
                "testPlaceInvalidCoordinate",
                new ImageView("/assets/WindowIcon.png"),
                "Placed beyond the parent",
                childContent
        );
        Point2D position = new Point2D(780, 580);
        
        // ====== Act ==========
        TestUtil.runThenWaitForPaintPulse(() -> {
            try{
                this.mdi.addMDIWindow(mdiWindow, position);
                fail("Should throw invalid exception when too close to bounds");
            } catch (PositionOutOfBoundsException e) {}
        });
    }
}
