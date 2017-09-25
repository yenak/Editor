import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.*;


/**
 * Created by Yena on 12/31/2016.
 */
public class Editor extends Application {

    private Group root;
    private static final int STARTING_WINDOW_WIDTH = 500;
    private static final int STARTING_WINDOW_HEIGHT = 500;
    private static int WINDOW_WIDTH = STARTING_WINDOW_WIDTH;
    private static int WINDOW_HEIGHT= STARTING_WINDOW_HEIGHT;
    private static LinkedList text;
    private static Rectangle cursor;
    private static FileWriter writer;
    private static String initial = "";
    private static String fileName;

    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
//        private static final int STARTING_FONT_SIZE = 20;
//        private static final int STARTING_TEXT_POSITION_X = 0;
//        private static final int STARTING_TEXT_POSITION_Y = 0;

        /** The Text to display on the screen. */
//        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
//        private int fontSize = STARTING_FONT_SIZE;

//        private String fontName = "Verdana";

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            text = new LinkedList(root, windowHeight, windowWidth);
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8 && !keyEvent.isShortcutDown()) {
                    // Ignore control keys, which have non-zero length, as well as the backspace
                    // key, which is represented as a character of value = 8 on Windows.
                    text.add(characterTyped);
                    keyEvent.consume();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (keyEvent.isShortcutDown()) {
                    if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                        text.changeFontSize(4);
                    } else if (code == KeyCode.MINUS) {
                        text.changeFontSize(-4);
                    } else if (code == KeyCode.S) {
                        // Makes new FileWriter everytime it saves so it rewrites the file
                        try {
                            writer = new FileWriter(fileName);
                        } catch (IOException ioexception) {
                            System.out.println("ERROR.");
                        }
                        text.save(writer);
                    }
                } else {
                    if (code == KeyCode.BACK_SPACE) {
                        text.delete();
                    }
                }
            }
        }
    }

    private void updateWindowSize(int newWidth, int newHeight) {
        WINDOW_WIDTH = newWidth;
        WINDOW_HEIGHT = newHeight;
        text.updateWindowSize(newWidth, newHeight);
    }

    /** An EventHandler to handle blinking the cursor. */
    private class CursorHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors = {Color.BLACK, Color.TRANSPARENT};

        CursorHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    /** Makes the text bounding box change color periodically. */
    public void makeCurosrBlink() {
        // Create a Timeline that will call the "handle" function of CursorHandler
        // every 1 second.
        final Timeline timeline = new Timeline();
        // The cursor should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorHandler cursorChange = new CursorHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @Override
    public void start(Stage primaryStage){

        // Create a Node that will be the parent of all things displayed on the screen.
        root = new Group();
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);


        // Dealing with window resizing
        scene.widthProperty().addListener((observableValue, oldScreenWidth, newScreenWidth) ->
                updateWindowSize(newScreenWidth.intValue(), WINDOW_HEIGHT));

        scene.heightProperty().addListener((observableValue, oldScreenHeight, newScreenHeight) ->
                updateWindowSize(WINDOW_WIDTH, newScreenHeight.intValue()));


        // Initialize cursor and send it to the LinkedList where it gets updated every time it renders
        // Call makeCursorBlink() to make cursor blink forever
        cursor = new Rectangle(1, 1);
        text.setCursor(cursor);
        root.getChildren().add(cursor);
        makeCurosrBlink();

        // Adds everything from the initial file into text
        System.out.println(initial);
        for (int i = 0; i < initial.length(); i++) {
            text.add(Character.toString(initial.charAt(i)));
        }
        text.save(writer);
        text.render();


        primaryStage.setTitle("Editor 2.0");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // there should be one argument, the name of the file
        // if the file exists then open that file
        // else create new file with that name
        if (args.length == 0) {
            System.out.println("No filename was provided.");
            System.exit(1);
        } else {
            fileName = args[0];
            try {
                File file = new File(fileName);
                if (!file.createNewFile()) {
                    // if file already exists, read contents and render them
                    FileReader reader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    int intRead;
                    initial = "";
                    while ((intRead = bufferedReader.read()) != -1) {
                        initial += (char) intRead;
                    }

                    bufferedReader.close();
                }
                writer = new FileWriter(fileName);

            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found!");
            } catch (IOException ioException) {
                System.out.println("Error.");
            }

        }
        launch(args);
    }

}
