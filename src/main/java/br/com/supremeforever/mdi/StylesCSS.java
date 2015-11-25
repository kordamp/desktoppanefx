package br.com.supremeforever.mdi;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Created by brisatc171.minto on 24/11/2015.
 */
public class StylesCSS {
    /**
     * TASK BAR CSS STYLES
     */
    public static String defaultTaskBarStyleCSS = " -fx-border-color: #C4C4C4; "
            + " -fx-faint-focus-color: transparent; "
            + " -fx-focus-color: transparent; ";

    public static String darkTaskBarStyleCSS =
            // "-fx-background-color:  linear-gradient(to bottom, rgba(23,23,23,1) 10%, rgba(55,55,55,1) 49%, rgba(23,23,23,1) 10%); " +
            "-fx-background-color:   linear-gradient(to bottom, rgba(23,23,23,0.8) 10%, rgba(55,55,55,1) 49%); " +
                    " -fx-border-color: #000000; "
                    + " -fx-faint-focus-color: transparent; "
                    + " -fx-focus-color: transparent; " +
                    "-fx-background-insets: 0, 0, 1, 2;";

    /**
     * TASK BAR ICON CSS STYLES
     */
    public static String defaultTaskBarIconStyleCSS = "-fx-background-color:  "
            + " linear-gradient(#f2f2f2, #d6d6d6), "
            + " linear-gradient(#fcfcfc 0%, #d9d9d9 20%, #d6d6d6 100%), "
            + " linear-gradient(#dddddd 0%, #f6f6f6 50%); "
            + " -fx-background-radius: 8,7,6; "
            + " -fx-background-insets: 0,1,2; "
            // + " -fx-text-fill: black; "
            + " -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );";

    public static String darkTaskBarIconStyleCSS = "-fx-background-color: " +
            "        #090a0c," +
            "        linear-gradient(#38424b 10%, #1f2429 30%, #191d22 100%);" +
            "        -fx-background-radius: 8,7,6; " +
            "        -fx-background-insets: 0,1,2; " +
            "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );" +
            "    -fx-text-fill: white; ";
    /**
     * TASK BAR ICON TEXT CSS STYLES
     */
    public static String defaultTaskBarIconTextStyleCSS = "-fx-font-weight: bold; -fx-text-fill: black;";

    public static String darkTaskBarIconTextStyleCSS = "-fx-font-weight: bold; -fx-text-fill: white;";
    /**
     * CONTROL BUTTONS CSS STYLES
     */
    public static String defaultControlButtonsStyleCSS = "";

    public static String darkControlButtonsStyleCSS = "-fx-background-color: transparent";
    /**
     * MDI WINDOW TITLE BAR
     */
    public static String defaultMdiTitleBarStyleCSS = "-fx-background-color: linear-gradient(to bottom, -fx-base, derive(#E9E9E9,10%),derive(#D0D0D0,10%),derive(#E9E9E9,10%));"
            + " -fx-border-color: transparent transparent #C4C4C4 transparent;"
            + " -fx-background-radius: 10 10 0 0;"
            + " -fx-border-radius: 10 10 0 0;";

    public static String darkMdiTitleBarStyleCSS = "-fx-background-color:" +
            "#090a0c," +
            "        linear-gradient(#5C6D7C 10%, #222222 30%, #424D59 100%);"
            + " -fx-border-color: transparent transparent #C4C4C4 transparent;"
            + " -fx-background-radius: 10 10 0 0;"
            + " -fx-border-radius: 10 10 0 0;";
    /**
     * MDI STYLE
     */
    public static String defaultMdiStyleCSS = "-fx-border-color: #C4C4C4;"
            + "-fx-background-radius: 10 10 0 0;"
            + "-fx-border-radius: 10 10 0 0;";

    public static String darkMdiStyleCSS = "-fx-border-color: #000000;"
            + "-fx-background-radius: 10 10 0 0;"
            + "-fx-border-radius: 10 10 0 0;";
    //-------MDI ICON BIND PROPERTY-------
    public static StringProperty taskBarIconStyleProperty = new SimpleStringProperty(defaultTaskBarIconStyleCSS);
    //-------TASK BAR BIND PROPERTY-------
    public static StringProperty taskBarStyleProperty = new SimpleStringProperty(defaultTaskBarStyleCSS);
    //-------TASK BAR ICON TEXT BIND PROPERTY-------
    public static StringProperty taskBarIconTextStyleProperty = new SimpleStringProperty(defaultTaskBarIconTextStyleCSS);
    //-------CONTROL BUTTONS PROPERTY-------
    public static StringProperty controlButtonsStyleProperty = new SimpleStringProperty(defaultControlButtonsStyleCSS);
    //-------MDI WINDOW TITLE BAR PROPERTY-------
    public static StringProperty mdiTitleBarStyleProperty = new SimpleStringProperty(defaultMdiTitleBarStyleCSS);
    //-------MDI WINDOW TITLE BAR PROPERTY-------
    public static StringProperty mdiStyleProperty = new SimpleStringProperty(defaultMdiStyleCSS);
}
