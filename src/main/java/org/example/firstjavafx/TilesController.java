package org.example.firstjavafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TilesController {
    private static final int ROWS = 6;
    private static final int COLUMNS = 5;
    public Label titleLabel;
    private Button[][] buttons = new Button[ROWS][COLUMNS];
    private int[][] clickCounts = new int[ROWS][COLUMNS];
    private Future<?> calculationFuture;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private char[][] characters = new char[ROWS][COLUMNS];

    private int lastActivatedRow = -1; // Initialize the last activated row index
    private int currentColumn = 0;
    @FXML
    private GridPane gridPane;

    GameLogic gameLogic;

    @FXML
    public void initialize() {
        System.out.println("TilesController initialized!"); // Print a message to the console
        titleLabel.setFocusTraversable(false);
        titleLabel.setFont(Font.font("Verdana", 25));
        gameLogic = new GameLogic(ROWS, COLUMNS);
        initializeButtons();
        manageCalculationThread();
        Platform.runLater(() -> gridPane.requestFocus());
    }

    public void updateTitleText(String newText) {
        titleLabel.setText(newText);
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            activateNextRow();
            manageCalculationThread();
        } else if (event.getCode() == KeyCode.BACK_SPACE) {
            if (currentColumn == 0) {
                deactivateLastRow();
                manageCalculationThread();
            } else {
                currentColumn--;
                buttons[lastActivatedRow + 1][currentColumn].setText(String.valueOf(' '));
            }

        } else {
            String letter = event.getText();
            typeLetter(letter);

        }
    }

    private void manageCalculationThread() {
        // Cancel the previous calculation task if it's still running
        updateTitleText("Thinking...");
        if (calculationFuture != null && !calculationFuture.isDone()) {
            calculationFuture.cancel(true);
        }
        // Submit a new task to the executor service
        calculationFuture = executorService.submit(() -> {
            String result = gameLogic.calculate(characters, clickCounts, lastActivatedRow);
            // Update the UI on the JavaFX Application Thread
            Platform.runLater(() -> updateTitleText(result));
        });
    }
    private void typeLetter(String letter){
        if (!letter.isEmpty() && Character.isLetter(letter.charAt(0)) && currentColumn < COLUMNS) {
            buttons[lastActivatedRow + 1][currentColumn].setText(letter.toUpperCase());
            characters[lastActivatedRow + 1][currentColumn] = letter.charAt(0); // Convert to char
            currentColumn++;
        }
    }
    private void handleTileClick(Button button, int row, int col) {
        activateButton(button, row, col);
    }
    private void activateButton(Button button, int row, int col){
        clickCounts[row][col]++;
        manageCalculationThread();
        switch (clickCounts[row][col] % 3) {
            case 0:
                button.setStyle("-fx-background-color: grey;");
                break;
            case 1:
                button.setStyle("-fx-background-color: yellow;");
                break;

            case 2:
                button.setStyle("-fx-background-color: green;");
                break;

        }
    }
    private void initializeButtons() {

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                final int currentRow = row;
                final int currentCol = col;


                Button button = new Button();
                button.setPrefWidth(50); // Set the preferred width
                button.setPrefHeight(50); // Set the preferred height


                // Set the text of the button to the corresponding letter

                button.setStyle("-fx-background-color: white");
                button.setFont(Font.font("verdana", FontWeight.BOLD, 18));
                // Center the text vertically and horizontally
                button.setStyle("-fx-alignment: center;");
                button.setDisable(true);
                button.setFocusTraversable(false);
                button.setOnAction(event -> handleTileClick(button, currentRow, currentCol));
                gridPane.add(button, col, row);

                // Assign the button to the buttons array
                buttons[row][col] = button;
            }
        }
    }

    private void activateNextRow() {

        if (lastActivatedRow < ROWS - 1 && currentColumn == COLUMNS) {
            currentColumn = 0;
            lastActivatedRow++;
            for (int col = 0; col < COLUMNS; col++) {
                Button button = buttons[lastActivatedRow][col];
                button.setDisable(false); // Enable the buttons in the row
                button.setStyle("-fx-background-color: grey;"); // Set background color to grey
            }
        }
    }

    private void clearBoard(){
        while ( lastActivatedRow >=0){
            deactivateLastRow();
        }
    }
    private void typeWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            typeLetter(Character.toString(c));
        }
    }
    private void deactivateLastRow() {
        if (lastActivatedRow >= 0) {
            for (int col = 0; col < COLUMNS; col++) {
                Button button = buttons[lastActivatedRow][col];
                clickCounts[lastActivatedRow][col] = 0;
                button.setDisable(true); // Disable the buttons in the row
                button.setStyle("-fx-background-color: white;"); // Set background color to white
                button.setText(""); // Clear the text
            }
            lastActivatedRow--;
            if (lastActivatedRow >= 0) {
                currentColumn = 0; // Reset the current column index if there are still activated rows
            }
        }
    }

}