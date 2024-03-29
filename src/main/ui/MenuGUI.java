package ui;

import persistence.JsonReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

// Represents the component of the GUI that will display main menu components
public class MenuGUI extends JPanel implements ActionListener {
    private static final String JSON_STORE = "./data/save-state.json";
    private static final String FONT_NAME = "Consolas";
    private static final Font REGULAR_TEXT = new Font(FONT_NAME, Font.PLAIN, 14);
    private static final int WIDTH_PX = 1000;
    private static final int HEIGHT_PX = 700;
    private final int centerX;
    private final JsonReader jsonReader;
    private final MainWindow display;

    // EFFECTS: constructs a panel to represent the GUI of the main menu,
    // initializes reader for loading a game state and displays menu text and buttons
    public MenuGUI(MainWindow display) {
        this.jsonReader = new JsonReader(JSON_STORE);
        this.centerX = WIDTH_PX / 2;
        this.display = display;
        this.setFocusable(false);
        drawMenu();
    }

    // MODIFIES: this
    // EFFECTS: adds components to the main menu with buttons to
    // start a new game or load a saved game
    private void drawMenu() {
        this.setLayout(null);
        this.setPreferredSize(new Dimension(WIDTH_PX, HEIGHT_PX));
        JLabel welcome = new JLabel("J A V A   R U N N E R");
        welcome.setBounds(0, 100, WIDTH_PX, 200);
        welcome.setHorizontalAlignment(JLabel.CENTER);
        welcome.setFont(new Font(FONT_NAME, Font.BOLD, 40));
        this.add(welcome);

        JButton newButton = new JButton("New Game");
        newButton.setActionCommand("new");
        newButton.setBounds(centerX - 200, 350, 175, 40);
        newButton.setFont(REGULAR_TEXT);
        newButton.addActionListener(this);
        this.add(newButton);

        JButton loadButton = new JButton("Load Game");
        loadButton.setActionCommand("load");
        loadButton.setBounds(centerX + 25, 350, 175, 40);
        loadButton.setFont(REGULAR_TEXT);
        loadButton.addActionListener(this);
        this.add(loadButton);

        JButton mapButton = new JButton("Upload Map .csv");
        mapButton.setActionCommand("map");
        mapButton.setBounds(centerX - 200, 400, 175, 40);
        mapButton.setFont(REGULAR_TEXT);
        mapButton.addActionListener(this);
        this.add(mapButton);

        JButton reset = new JButton("Reset Default Map");
        reset.setActionCommand("reset");
        reset.setBounds(centerX + 25, 400, 175, 40);
        reset.setFont(REGULAR_TEXT);
        reset.addActionListener(this);
        this.add(reset);
    }

    // MODIFIES: this
    // EFFECTS: creates a new game panel instance and maps menu buttons to actions;
    // if the "new" button is clicked show the new game panel,
    // if the "load" button is clicked load a saved game into the game panel before showing
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("map")) {
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fileChooser.setAcceptAllFileFilterUsed(false);

            fileChooser.setDialogTitle("Select a .txt file");

            FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .csv files", "csv");
            fileChooser.addChoosableFileFilter(restrict);

            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                this.display.setMap(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (actionCommand.equals("reset")) {
            this.display.setMap("/maps/testmap.csv");
        } else if (actionCommand.equals("new")) {
            this.display.startNewGame();
            startGameAndShow();
        } else if (actionCommand.equals("load")) {
            this.display.startNewGame();
            loadGame();
            startGameAndShow();
        }
    }

    // MODIFIES: this
    // EFFECTS: shows the game panel in the window, gives the game panel focus
    private void startGameAndShow() {
        SwingUtilities.invokeLater(() -> {
            this.display.getCardLayout().show(this.display.getMainPanel(), "game");
            this.display.getGame().requestFocusInWindow();
        });
    }

    // modelled after JsonSerializationDemo provided by CPSC 210 at UBC
    // MODIFIES: this
    // EFFECTS: loads game state from file
    private void loadGame() {
        try {
            this.display.getGame().setGame(jsonReader.read());
        } catch (IOException e) {
            startGameAndShow();
        }
    }
}
