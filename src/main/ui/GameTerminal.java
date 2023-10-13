package ui;

import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import model.*;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import model.Character;

import java.io.IOException;
import java.util.List;

@SuppressWarnings({"checkstyle:AvoidEscapedUnicodeCharacters", "checkstyle:SuppressWarnings"})
public class GameTerminal {
    private static final char CHARACTER = '\u265E';
    private static final char BLOCK = '\u25a3';
    private static final char HAZARD = '\u2716';
    private static final char SPEED = '\u26a1';
    private static final char INVULNERABLE = '\u262f';
    private Game game;
    private Screen screen;
    private WindowBasedTextGUI endGui;
    private int centerX;
    private int keyY;

    // temporary method for creating a test map for developers to test the UI
    private void initializeTestMap(Game testgame) {
        testgame.addBlock(new Block(new Position(26, 15)));

        testgame.addBlock(new Block(new Position(27, 15)));
        testgame.addBlock(new Block(new Position(28, 16)));
        testgame.addBlock(new Block(new Position(29, 16)));
        testgame.addBlock(new Block(new Position(30, 17)));
        testgame.addBlock(new Block(new Position(31, 17)));
        testgame.addBlock(new Block(new Position(32, 17)));
        testgame.addBlock(new Block(new Position(33, 17)));
        testgame.addBlock(new Block(new Position(34, 17)));
        testgame.addBlock(new Block(new Position(35, 17)));
        testgame.addBlock(new Block(new Position(35, 16)));
        testgame.addBlock(new Block(new Position(25, 15)));
        testgame.addBlock(new Block(new Position(24, 15)));
        testgame.addBlock(new Block(new Position(23, 15)));
        testgame.addBlock(new Block(new Position(22, 15)));
        testgame.addBlock(new Block(new Position(21, 15)));
        testgame.addBlock(new Block(new Position(20, 15)));
        testgame.addBlock(new Block(new Position(19, 15)));
        testgame.addBlock(new Block(new Position(18, 15)));
        testgame.addBlock(new Block(new Position(18, 14)));

        testgame.addBlock(new Hazard(new Position(20, 14)));
        testgame.addBlock(new PowerUp(new Position(28, 14), Game.INVULNERABLE));
        testgame.addBlock(new PowerUp(new Position(33, 16), Game.SPEED));
    }

    // based on startGame() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // REQUIRES: a terminal does not already exist
    // MODIFIES: this
    // EFFECTS: initializes the terminal screen, the game and game map,
    // and starts the tick cycle of the game state
    public void startGame() throws IOException, InterruptedException {
        this.screen = new DefaultTerminalFactory().createScreen();
        this.screen.startScreen();

        TerminalSize terminalSize = this.screen.getTerminalSize();

        this.game = new Game(terminalSize.getColumns() - 2,terminalSize.getRows() - 2);
        centerX = game.getMaxX() / 2;
        keyY = game.getMaxY() - 1;
        initializeTestMap(this.game);
        cycleTicks();
    }

    // based on beginTicks() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // EFFECTS: ticks to progress the game state every 1/10th of a second
    // as long as the game has not ended or there is an end screen present;
    // if the game has ended and there is no end screen then exit the program
    private void cycleTicks() throws IOException, InterruptedException {
        while (!this.game.isEnded() || this.endGui.getActiveWindow() != null) {
            uiTick();
            Thread.sleep(1000L / Game.TICKS_PER_SECOND);
        }

        System.exit(0);
    }

    // based on tick() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: reads user inputs and updates game variables as needed before
    // ticking the game state; the screen is cleared and the new game state
    // is rendered on the terminal at the end of the tick
    private void uiTick() throws IOException {
        handleUserInputs();
        this.game.tick();

        this.screen.setCursorPosition(new TerminalPosition(0, 0));
        this.screen.clear();
        render();
        this.screen.refresh();
    }

    // modelled after handleUserInputs() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: handles different user inputs including keys for using power-ups,
    // jumping, and moving left or right
    private void handleUserInputs() throws IOException {
        KeyStroke key = this.screen.pollInput();
        if (key == null) {
            return;
        }
        int currentMultiplier = this.game.getCharacter().getVelocityXMultiplier();
        switch (key.getKeyType()) {
            case Character:
                handleCharacter(key.getCharacter());
                break;
            case ArrowRight:
                kickStart();
                if (currentMultiplier < 0) {
                    this.game.getCharacter().setVelocityXMultiplier(currentMultiplier * -1);
                }
                break;
            case ArrowLeft:
                kickStart();
                if (currentMultiplier > 0) {
                    this.game.getCharacter().setVelocityXMultiplier(currentMultiplier * -1);
                }
                break;
            default:
        }
    }

    // REQUIRES: character is ' ', '1', '2', or '3'
    // MODIFIES: this
    // EFFECTS: makes the character jump if space is pressed
    // and the character is currently on a platform;
    // a power-up may be used if '1', '2', or '3' is pressed
    private void handleCharacter(char character) {
        switch (character) {
            case (' '):
                if (this.game.onPlatform(game.getCharacter().getPosition())) {
                    this.game.getCharacter().setVelocityY(-3);
                }
                break;
            case ('1'):
                searchAndUse("1");
                break;
            case ('2'):
                searchAndUse("2");
                break;
            case ('3'):
                searchAndUse("3");
                break;
            default:
        }
    }

    // REQUIRES: key is "1", "2," or "3"
    // MODIFIES: this
    // EFFECTS: if the key is not available to be assigned, a power-up can be
    // located from the inventory with the assigned key and will be used
    private void searchAndUse(String key) {
        if (!this.game.getAvailableKeys().contains(key)) {
            PowerUp toUse = null;
            for (PowerUp pu : this.game.getInventory()) {
                if (pu.getKeyAssignment().equals(key)) {
                    toUse = pu;
                    break;
                }
            }
            if (toUse != null) {
                this.game.usePowerUp(toUse);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: sets the game character's base velocity to 1 grid unit/tick
    private void kickStart() {
        this.game.getCharacter().setVelocityX(1);
    }

    // based on render() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: renders the end screen if the game is over;
    // otherwise display the seconds since game start, all the blocks
    // in the game, the character, and the inventory
    private void render() {
        if (this.game.isEnded()) {
            if (this.endGui == null) {
                drawEndScreen();
            }
            return;
        }

        drawTime();
        drawBlocks();
        drawCharacter();
        drawInventory();
    }

    // based on drawEndScreen() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: draws an end screen showing the time for game completion in seconds,
    // also shows a close button that can be trigger by clicking enter to exit the program
    private void drawEndScreen() {
        this.endGui = new MultiWindowTextGUI(this.screen);

        MessageDialogBuilder messageDialogBuilder = new MessageDialogBuilder()
                .setTitle("Game over!")
                .setText("Game ended in " + (this.game.getTime() / Game.TICKS_PER_SECOND) + " seconds!"
                        + "\nClick ENTER to close.")
                .addButton(MessageDialogButton.Close);

        MessageDialog dialog = messageDialogBuilder.build();
        dialog.showDialog(this.endGui);
    }

    // based on drawScore() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: displays the time since game start in seconds
    private void drawTime() {
        TextGraphics text = screen.newTextGraphics();
        text.setForegroundColor(TextColor.ANSI.WHITE);
        text.putString(1, 0, "TIME: ");

        text = this.screen.newTextGraphics();
        text.setForegroundColor(TextColor.ANSI.GREEN);
        text.putString(8, 0, String.valueOf(this.game.getTime() / Game.TICKS_PER_SECOND));
    }

    // MODIFIES: this
    // EFFECTS: displays the blocks in the game using assigned symbols/characters
    private void drawBlocks() {
        for (Block block : this.game.getBlocks()) {
            drawBlock(block, block.getPosition());
        }
    }

    // MODIFIES: this
    // EFFECTS: displays the player's character in the game
    private void drawCharacter() {
        Character character = this.game.getCharacter();
        drawPosition(character.getPosition(), TextColor.ANSI.WHITE, CHARACTER);
    }

    // MODIFIES: this
    // EFFECTS: displays the player's inventory of power-ups
    private void drawInventory() {
        List<PowerUp> inventory = game.getInventory();
        drawPosition(new Position(centerX - 5, keyY), TextColor.ANSI.WHITE, '1');
        drawPosition(new Position(centerX, keyY), TextColor.ANSI.WHITE, '2');
        drawPosition(new Position(centerX + 5, keyY), TextColor.ANSI.WHITE, '3');
        if (inventory.size() != 0) {
            for (PowerUp pu : inventory) {
                switch (pu.getKeyAssignment()) {
                    case "1":
                        drawBlock(pu, new Position(centerX - 5, game.getMaxY()));
                        break;
                    case "2":
                        drawBlock(pu, new Position(centerX, game.getMaxY()));
                        break;
                    case "3":
                        drawBlock(pu, new Position(centerX + 5, game.getMaxY()));
                        break;
                }
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: displays a single block at p using assigned symbols/characters
    private void drawBlock(Block block, Position p) {
        switch (block.getName()) {
            case Game.BLOCK:
                drawPosition(p, TextColor.ANSI.YELLOW, BLOCK);
                break;
            case Game.HAZARD:
                drawPosition(p, TextColor.ANSI.RED, HAZARD);
                break;
            case Game.SPEED:
                drawPosition(p, TextColor.ANSI.GREEN, SPEED);
                break;
            case Game.INVULNERABLE:
                drawPosition(p, TextColor.ANSI.MAGENTA, INVULNERABLE);
                break;
        }
    }

    // based on drawPosition() method in the TerminalGame class of SnakeConsole-Lanterna by Mazen Kotb
    // MODIFIES: this
    // EFFECTS: displays c with a custom color at the specified p in the terminal
    private void drawPosition(Position p, TextColor color, char c) {
        TextGraphics text = this.screen.newTextGraphics();
        text.setForegroundColor(color);
        text.putString(p.getPositionX(), p.getPositionY(), String.valueOf(c));
    }

}
