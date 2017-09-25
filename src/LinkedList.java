import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by Yena on 12/31/2016.
 */
public class LinkedList {
    // the linkedlist is the entire text
    // each node in the linkedlist is a typed character
    // there will be an array that represents the lines with each element pointing to a node
    // in the linked list where there is a newline
    // each new character will be added before the cursor
    // so you delete the node before the cursor (when using backspace)
    private static final int STARTING_FONT_SIZE = 20;

    Node start;
    Node end;
    Node cursor;
    Group root;
    String fontName = "Verdana";
    int fontSize = STARTING_FONT_SIZE;
    int windowHeight;
    int windowWidth;
    Rectangle cursorRec;

    public LinkedList(Group root, int windowHeight, int windowWidth/*, String fontName, int fontSize*/) {
        this.root = root;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        start = new Node(5, 0);
        end = new Node(5, 0);
        link(start, end); // start.next = end; end.prev = start;
        cursor = end;
    }

    private class Node {
        // x, y positions of the text refer to the top left corner of the text
        Text text;
        Node prev;
        Node next;

        Node(String newText) {
            text = new Text(cursorX(), cursorY(), newText);
            text.setFont(Font.font(fontName, fontSize));
            text.setTextOrigin(VPos.TOP);
            root.getChildren().add(text);
        }

        Node(int x, int y) {
            // this is to instantiate the start and end nodes
            text = new Text("");
        }
    }

    public void add(String toBeAdded) {
        // add new Node with toBeAdded (should be a character) as text where cursor is
        // change cursor to be at the new Node
        // the new node should be inserted between cursor.prev and cursor
        Node newNode = new Node(toBeAdded);
        link(cursor.prev, newNode);
        link(newNode, cursor);
        cursor.text.setX(cursorX() + newNode.text.getLayoutBounds().getWidth());
        render();
    }

    public void delete() {
        // delete Node that is before the Node where the cursor is at (cursor.prev)
        if (!cursor.equals(start) && !cursor.prev.equals(start)) {
            root.getChildren().remove(cursor.prev.text);
            link(cursor.prev.prev, cursor);
            render();
        }
    }

    public void setCursor(Rectangle cursor) {
        // sets the Rectangle cursor to the correct cursor node
        cursorRec = cursor;
        cursorRec.setFill(Color.BLACK);
        updateCursor();
    }

    private void updateCursor() {
        // updates cursor size and position
        cursorRec.setHeight((int) cursor.text.getLayoutBounds().getHeight());
        cursorRec.setX(cursorX());
        cursorRec.setY(cursorY());
    }

    public void moveCursor() {
        // changes cursor node to new location
    }

    public int cursorX() {
        return (int) cursor.text.getX();
    }

    public int cursorY() {
        return (int) cursor.text.getY();
    }

    public void changeFontSize(int sizeChange) {
        fontSize += sizeChange;
        if (fontSize < 0) fontSize = 0;
        render();
    }

    public void render() {
        Node current = start;
        int x = 5;
        int y = 0;
        while (current != null) {
            Text text = current.text;
            text.setFont(Font.font(fontName, fontSize));
            int width = (int) text.getLayoutBounds().getWidth();
            if (x + width > windowWidth) {
                x = 5;
                y += text.getLayoutBounds().getHeight();
            }
            text.setX(x);
            text.setY(y);
            x += width;
            current = current.next;
        }
        updateCursor();
    }

    public void save(FileWriter writer) {
        // Puts all the text into a string that is to be written to the file.
        String text = "";
        Node current = start.next;
        while (current != end) {
            text += current.text.getText();
            current = current.next;
        }
        try {
            char[] chars = text.toCharArray();
            for (char charRead : chars) {
                writer.write(charRead);
            }
            writer.close();
        } catch (IOException ioException) {
            System.out.println("Error saving file.");
        }
    }

    private void link(Node a, Node b) {
        // sets node a to be before node b
        a.next = b;
        b.prev = a;
    }

    public void updateWindowSize(int newWidth, int newHeight) {
        windowWidth = newWidth;
        windowHeight = newHeight;
        render();
    }

}
