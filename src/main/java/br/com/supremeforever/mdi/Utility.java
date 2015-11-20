package br.com.supremeforever.mdi;


import javafx.scene.Node;

/**
 * Created by brisatc171.minto on 12/11/2015.
 */


public class Utility {

    /**
     * @param mainPane
     * @return
     */
    public static MDIWindow getMDIWindow(Node mainPane){
        MDIWindow mdiW = (MDIWindow) mainPane.getParent().getParent();
        return mdiW;
    }
}
