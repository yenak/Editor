import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by Yena on 12/31/2016.
 */
public class LinkedList {
    /*
         The LinkedList is the entire text.
         Each node in the LinkedList is a typed character except for the start and end Nodes.
         There will be an array that represents the lines with each element pointing to a Node in the LinkedList where
         there is a newline.
         Each new character will be added before the cursor, so you delete the Node before the cursor (when using
         backspace).
    */
    private static final int STARTING_FONT_SIZE = 20;

    private Node start;
    private Node end;
    private Node cursor;
    private Group root;
    private String fontName = "Verdana";
    private int fontSize = STARTING_FONT_SIZE;
    private int windowHeight;
    private int windowWidth;
    private Rectangle cursorRec;
    private int capacity = 30;
    private int factor = 2;
    private int totalLines;
    private Node[] lines = new Node[capacity];


    public LinkedList(Group root, int windowHeight, int windowWidth/*, String fontName, int fontSize*/) {
        this.root = root;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        start = new Node(5, 0);
        end = new Node(5, 0);
        link(start, end); // start.next = end; end.prev = start;
        cursor = end;
        lines[0] = start;
        totalLines = 1;
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
            // This is to instantiate the start and end nodes
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
//        cursor.text.setX(cursorX() + newNode.text.getLayoutBounds().getWidth());
        render();
    }

    public void addInitial(String initial) {
        // Adds the initial text that is already in the opened file
        System.out.println(initial);
        Node curr = start;
        for (int i = 0; i < initial.length(); i++) {
            String toAdd = Character.toString(initial.charAt(i));
            Node newNode = new Node(toAdd);
            link(curr, newNode);
            link(newNode, end);
            curr = newNode;
        }
        render();
    }

    public void addLine() {
        // Adds a newline where the cursor node is
        // TODO: add a newline
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
        // TODO: change cursor node to new location
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
        /*
           Renders the whole text from the start node to the end node.
           TODO: create new line when word is cut off
           TODO: don't rerender if you're adding to a line that has enough space for the new character
           TODO: keep cursor on spaces at end of line instead of on next line
         */
        Node current = start;
        int x = 5;
        int y = 0;
        while (current != null) {
            Text text = current.text;
            text.setFont(Font.font(fontName, fontSize));
            int width = (int) text.getLayoutBounds().getWidth();
            if (x + width > (windowWidth - 5) && !text.getText().equals(" ")) {
                // if the new text goes over the page width and it is not a space, move onto next line
                x = 5;
                y += text.getLayoutBounds().getHeight();
                totalLines++;
                if (totalLines >= capacity) {
                    resize(capacity * factor);
                }
                lines[totalLines] = current;
            }
            text.setX(x);
            text.setY(y);
            x += width;
            current = current.next;
        }
//        System.out.println(totalLines);
        updateCursor();
    }

    public void save(FileWriter writer) {
        // Puts all the text into a string that is to be written to the file.
//        String text = "";
        StringBuilder text = new StringBuilder("");
        Node current = start.next;
        while (current != end) {
//            text += current.text.getText();
            text.append(current.text.getText());
            current = current.next;
        }
        try {
            char[] chars = text.toString().toCharArray();
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

    public void homeKey() {
        // Moves the cursor to the Node that is in the beginning of the line the cursor is on
        cursor = lines[getLine(cursor)];
        if (cursor == start) {
            cursor = start.next;
        }
        updateCursor();
    }

    private int getLine(Node node) {
        // Gets the index of the line that node is in
        Text ref = new Text("");
        ref.setFont(Font.font(fontName, fontSize));
        int yPos = (int) node.text.getY();
        int yRef = (int) ref.getLayoutBounds().getHeight();
        System.out.println(yPos);
        System.out.println(yRef);
        System.out.println((int) (yPos / yRef));
        return (int) (yPos / yRef);
    }

    private void resize(int newCap) {
        // Resizes the lines array to the newCap by creating a new array of size newCap and copying things over
        Node[] newLines = new Node[newCap];
        System.arraycopy(lines, 0, newLines, 0, capacity);
        this.lines = newLines;
        capacity = newCap;
    }

}
