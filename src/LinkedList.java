import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileWriter;
import java.io.IOException;


public class LinkedList {
    /**
         The LinkedList is the entire text.
         Each node in the LinkedList is a typed character except for the start and end Nodes.
         There will be an array that represents the lines with each element pointing to a Node in the LinkedList where
         there is a newline.
         Each new character will be added before the cursor, so you delete the Node before the cursor (when using
         backspace).
         Newlines are implemented through a boolean in the Node class. This is because the Text with a newline
         character is double the height of a regular character. To keep heights consistent, we utilize the boolean.
         This means that when we save, we have to convert the text in the Nodes with newLine as true to "\n".
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
    private int totalLines;
    private Node[] lines = new Node[capacity];


    public LinkedList(Group root, int windowHeight, int windowWidth) {
        this.root = root;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        start = new Node(5, 0);
        end = new Node(5, 0);
        link(start, end); // start.next = end; end.prev = start;
        cursor = end;
        lines[0] = start;
        totalLines = 0;
    }

    /** Node is each element of the LinkedList. Each node holds one character in the text.*/
    private class Node {
        // x, y positions of the text refer to the top left corner of the text
        Text text;
        Node prev;
        Node next;
        boolean newline = false; // only true if newline character
        boolean startLine = false; // only true if it is the start of a line

        Node(String newText) {
            if (newText.equals("\n")) {
                text = new Text(cursorX(), cursorY(), "");
                newline = true;
            } else {
                text = new Text(cursorX(), cursorY(), newText);
            }
            text.setFont(Font.font(fontName, fontSize));
            text.setTextOrigin(VPos.TOP);
            root.getChildren().add(text);
        }

        Node(int x, int y) {
            // This is to instantiate the start and end nodes
            text = new Text("");
        }
    }

    /** Adds a new Node where the cursor is. */
    public void add(String toBeAdded) {
        // add new Node with toBeAdded (should be a character) as text where cursor is
        // change cursor to be at the new Node
        // the new node should be inserted between cursor.prev and cursor
        // If the cursor is at the start of a line, the new text should appear before the cursor on that line
        Node newNode = new Node(toBeAdded);
        link(cursor.prev, newNode);
        link(newNode, cursor);
        render();
    }


    /** Adds the initial text that is already in the opened file. */
    public void addInitial(String initial) {
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

    /** Deletes Node that is before the Node where the cursor is at (cursor.prev). */
    public void delete() {
        if (!cursor.equals(start) && !cursor.prev.equals(start)) {
            root.getChildren().remove(cursor.prev.text);
            link(cursor.prev.prev, cursor);
            render();
        }
    }

    /** Sets the Rectangle cursor to the correct cursor node. */
    public void setCursor(Rectangle cursor) {
        cursorRec = cursor;
        cursorRec.setFill(Color.BLACK);
        updateCursor();
    }

    /** Updates the cursor size and position. */
    private void updateCursor() {
        cursorRec.setHeight((int) cursor.text.getLayoutBounds().getHeight());
        cursorRec.setX(cursorX());
        cursorRec.setY(cursorY());
    }

    /** Moves the cursor to the closest Node to (xPos, yPos). */
    public void moveCursor(int xPos, int yPos) {
        int height = (int) cursor.text.getLayoutBounds().getHeight();
        int line = yPos / height;
        if (line > totalLines) {
            line = totalLines;
        }
        cursor = getClosest(line, xPos);
        updateCursor();
    }

    public int cursorX() {
        return (int) cursor.text.getX();
    }

    public int cursorY() {
        return (int) cursor.text.getY();
    }

    /** Returns the Y position of the end of the document (y pos of end plus height of end). */
    public int getEnd() {
        return (int) (end.text.getY() + end.text.getLayoutBounds().getHeight());
    }

    /** Changes the font size for the whole text. */
    public void changeFontSize(int sizeChange) {
        fontSize += sizeChange;
        if (fontSize < 0) fontSize = 0;
        render();
    }

    /** Renders the whole text. */
    public void render() {
        Node current = start;
        Node prev = null;
        int x = 5;
        int y = 0;
        totalLines = 0;
        int factor = 2; // The factor by which to multiply the capacity
        Node wordStart = current;
        int wordLength = 0;
        while (current != null) {
            Text text = current.text;
            text.setFont(Font.font(fontName, fontSize));
            int width = (int) text.getLayoutBounds().getWidth();
            if (prev != null && (prev == start || prev.newline || prev.text.getText().equals(" "))) {
                wordStart = current;
                wordLength = width;
            } else {
                wordLength += width;
            }
            if ((x + width > (windowWidth - 5) && !text.getText().equals(" ")) || (prev != null && prev.newline)) {
                // If the new text goes over the page width and it is not a space, move onto next line
                // If the prev text is a newline character, move onto next line

                x = 5;
                y += text.getLayoutBounds().getHeight();
                totalLines++;
                if (totalLines >= capacity) {
                    resize(capacity * factor);
                }
                if (!wordStart.startLine) {
                    lines[totalLines] = wordStart;
                    wordStart.startLine = true;
                    Node word = wordStart;
                    while (word != current) {
                        Text wordText = word.text;
                        wordText.setX(x);
                        wordText.setY(y);
                        x += wordText.getLayoutBounds().getWidth();
                        word = word.next;
                    }
                } else {
                    lines[totalLines] = current;
                    current.startLine = true;
                }
            } else {
                current.startLine = false;
            }
            text.setX(x);
            text.setY(y);
            x += width;
            prev = current;
            current = current.next;
        }
        if (totalLines < capacity / factor && totalLines > 30) {
            resize(capacity / factor);
        }
        updateCursor();
    }

    /** Puts all the text into a string that is to be written to the file. */
    public void save(FileWriter writer) {
        StringBuilder text = new StringBuilder("");
        Node current = start.next;
        while (current != end) {
            String newText = current.text.getText();
            if (current.newline) {
                newText = "\n";
            }
            text.append(newText);
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

    /** Sets Node a to be before Node b. */
    private void link(Node a, Node b) {
        a.next = b;
        b.prev = a;
    }

    /** Updates the size of the window. */
    public void updateWindowSize(int newWidth, int newHeight) {
        windowWidth = newWidth;
        windowHeight = newHeight;
        render();
    }

    /** Moves the cursor to the Node that is at the beginning of the line the cursor is on. */
    public void homeKey() {
        cursor = lines[getLine()];
        if (cursor == start) {
            cursor = start.next;
        }
        updateCursor();
    }

    /** Moves the cursor to the Node that is at the end of the line the cursor is on. */
    public void endKey() {
        int line = getLine();
        if (line < totalLines) {
            cursor = lines[line + 1].prev;
        } else {
            cursor = end;
        }
        updateCursor();
    }

    /** Moves the cursor up. */
    public void moveUp() {
        int line = getLine();
        int xPos = cursorX();
        if (line != 0) {
            cursor = getClosest(line - 1, xPos);
            updateCursor();
        }
    }

    /** Moves the cursor down. */
    public void moveDown() {
        int line = getLine();
        int xPos = cursorX();
        if (line != totalLines) {
            cursor = getClosest(line + 1, xPos);
            updateCursor();
        }
    }

    /** Helper function to get the Node that is closest to the xPos in the line. */
    private Node getClosest(int line, int xPos) {
        Node curr = lines[line];
        Node next = curr.next;
        int currX = 0;
        while (!next.startLine && next != end) {
            int nextX = (int) next.text.getX();
            if (Math.abs(xPos - nextX) <= Math.abs(xPos - currX)) {
                currX = nextX;
                curr = next;
                next = curr.next;
            } else {
                break;
            }
        }
        if (next == end) {
            return end;
        }
        return curr;
    }

    /** Moves the cursor to the right. */
    public void moveRight() {
        if (cursor != end) {
            cursor = cursor.next;
            updateCursor();
        }
    }

    /** Moves the cursor to the left. */
    public void moveLeft() {
        if (cursor != start) {
            cursor = cursor.prev;
            updateCursor();
        }
    }

    /** Gets the line number of the line the cursor is on. */
    private int getLine() {
        int yPos = cursorY();
        int height = (int) cursor.text.getLayoutBounds().getHeight();
        return yPos / height;
    }

    /** Resizes the lines array to the newCap by creating a new array of size newCap and copying things over. */
    private void resize(int newCap) {
        Node[] newLines = new Node[newCap];
        System.arraycopy(lines, 0, newLines, 0, totalLines);
        this.lines = newLines;
        capacity = newCap;
    }

}
