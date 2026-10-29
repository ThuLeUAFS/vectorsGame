package game;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Vectors {

    private static final int SIZE = 8;
    private static final int DEFAULT_ROUNDS = 20;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private final Scanner input = new Scanner(System.in);
    private final Random random = new Random();

    private final GameBoard board = new GameBoard();
    private final Player player = new Player("Player", 'X');
    private final Player computer = new Player("Computer", 'C');
    private final HighScoreManager highScoreManager =
            new HighScoreManager(HIGH_SCORE_FILE);

    private String highScoreName;
    private int highScore;

    public static void main(String[] args) {
        Vectors game = new Vectors();
        game.play();
    }

    public void play() {
        System.out.println("**************************************");
        System.out.println("- V E C T O R S                      *");
        System.out.println("**************************************");

        highScoreManager.load();
        highScore = highScoreManager.getScore();
        highScoreName = highScoreManager.getName();

        System.out.printf("- High Score: %-20s *%n",
                highScoreName + " \u2013 " + highScore);

        System.out.println("**************************************");

        System.out.print("Enter your name: ");
        String name = input.nextLine().trim();

        if (!name.isEmpty()) {
            player.setName(name);
        }

        int rounds = askForRounds();

        for (int round = 1; round <= rounds; round++) {
            displayGame(round, rounds);

            int row;
            int col;

            while (true) {
                System.out.print("Player(X) Turn (Row Col): ");

                String line = input.nextLine().trim();
                String[] parts = line.split("\\s+");

                if (parts.length != 2) {
                    System.out.println(
                            "Invalid input. Enter row and column, e.g. 3 6.");
                    continue;
                }

                try {
                    row = Integer.parseInt(parts[0]);
                    col = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.out.println(
                            "Invalid input. Please enter two whole numbers.");
                    continue;
                }

                if (!board.isValidMove(row, col)) {
                    System.out.println(
                            "Invalid move. Choose an empty cell from 0 to 7.");
                    continue;
                }

                break;
            }

            board.placeToken(row, col, player.getToken());

            int[] computerMove = chooseComputerMove();
            board.placeToken(
                    computerMove[0], computerMove[1], computer.getToken());

            System.out.printf("Computer(C) played: %d %d%n",
                    computerMove[0], computerMove[1]);

            int playerPoints = board.calculateScore(player.getToken());
            int computerPoints = board.calculateScore(computer.getToken());

            player.addScore(playerPoints);
            computer.addScore(computerPoints);

            player.setLastRoundScore(playerPoints);
            computer.setLastRoundScore(computerPoints);
        }

        displayGame(rounds, rounds);

        System.out.println("**************************************");
        System.out.println("GAME OVER");
        System.out.println("**************************************");

        int finalScore = player.getTotalScore()
                - computer.getTotalScore();

        System.out.println("Player Total: " + player.getTotalScore());
        System.out.println("Computer Total: " + computer.getTotalScore());
        System.out.println("Final Score: " + finalScore);

        if (finalScore > highScore) {
            highScore = finalScore;
            highScoreName = player.getName();

            highScoreManager.save(highScoreName, highScore);

            System.out.println("Congratulations! You set a new high score!");
        }

        System.out.printf("- High Score: %-20s *%n",
                highScoreName + " \u2013 " + highScore);

        input.close();
    }

    private int askForRounds() {
        System.out.print("Enter number of rounds (default 20): ");

        String line = input.nextLine().trim();

        if (line.isEmpty()) {
            return DEFAULT_ROUNDS;
        }

        try {
            int rounds = Integer.parseInt(line);

            if (rounds >= 1 && rounds <= 32) {
                return rounds;
            }
        } catch (NumberFormatException e) {
        }

        System.out.println("Enter a number between 1 and 32.");
        return DEFAULT_ROUNDS;
    }

    private void displayGame(int turn, int totalRounds) {
        System.out.printf("- Player Score: %4d Last Round %3d *%n",
                player.getTotalScore(), player.getLastRoundScore());

        System.out.printf("- Computer Score: %2d Last Round %3d *%n",
                computer.getTotalScore(), computer.getLastRoundScore());

        System.out.println("**************************************");
        System.out.printf("- Turn %d of %d%n", turn, totalRounds);

        board.display();
    }

    private int[] chooseComputerMove() {
        int row;
        int col;

        do {
            row = random.nextInt(SIZE);
            col = random.nextInt(SIZE);
        } while (!board.isValidMove(row, col));

        return new int[] { row, col };
    }
}

class GameBoard {

    private static final int SIZE = 8;
    private final char[][] grid = new char[SIZE][SIZE];

    public GameBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = ' ';
            }
        }
    }

    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < SIZE
                && col >= 0 && col < SIZE
                && grid[row][col] == ' ';
    }

    public void placeToken(int row, int col, char token) {
        if (isValidMove(row, col)) {
            grid[row][col] = token;
        }
    }

    public void display() {
        System.out.print("  ");

        for (int col = 0; col < SIZE; col++) {
            System.out.printf(" %d  ", col);
        }

        System.out.println();
        printSeparator();

        for (int row = 0; row < SIZE; row++) {
            System.out.print("|");

            for (int col = 0; col < SIZE; col++) {
                System.out.printf(" %c |", grid[row][col]);
            }

            System.out.println(" " + row);
            printSeparator();
        }
    }

    private void printSeparator() {
        System.out.println("---------------------------------");
    }

    public int calculateScore(char token) {
        int totalPoints = 0;

        int[][] directions = {
            { 0, 1 },
            { 1, 0 },
            { 1, 1 },
            { 1, -1 }
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {

                if (grid[row][col] != token) {
                    continue;
                }

                for (int[] direction : directions) {
                    int dr = direction[0];
                    int dc = direction[1];

                    int previousRow = row - dr;
                    int previousCol = col - dc;

                    if (isInside(previousRow, previousCol)
                            && grid[previousRow][previousCol] == token) {
                        continue;
                    }

                    int length = 0;
                    int r = row;
                    int c = col;

                    while (isInside(r, c) && grid[r][c] == token) {
                        length++;
                        r += dr;
                        c += dc;
                    }

                    totalPoints += pointsForLength(length);
                }
            }
        }

        return totalPoints;
    }

    private int pointsForLength(int length) {
        if (length < 3) {
            return 0;
        } else if (length == 3) {
            return 4;
        } else if (length == 4) {
            return 6;
        } else {
            return 10;
        }
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < SIZE
                && col >= 0 && col < SIZE;
    }
}

class Player {

    private String name;
    private final char token;
    private int totalScore;
    private int lastRoundScore;

    public Player(String name, char token) {
        this.name = name;
        this.token = token;
        this.totalScore = 0;
        this.lastRoundScore = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getToken() {
        return token;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addScore(int points) {
        totalScore += points;
    }

    public int getLastRoundScore() {
        return lastRoundScore;
    }

    public void setLastRoundScore(int points) {
        lastRoundScore = points;
    }
}

class HighScoreManager {

    private final String filename;
    private String name = "None";
    private int score = 0;

    public HighScoreManager(String filename) {
        this.filename = filename;
    }

    public void load() {
        File file = new File(filename);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String savedName = reader.readLine();
            String savedScore = reader.readLine();

            if (savedName != null && savedScore != null) {
                name = savedName;

                try {
                    score = Integer.parseInt(savedScore);
                } catch (NumberFormatException e) {
                    name = "None";
                    score = 0;
                }
            }

        } catch (IOException e) {
            System.out.println("Could not read high-score file.");
        }
    }

    public void save(String name, int score) {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(filename))) {

            writer.println(name);
            writer.println(score);

        } catch (IOException e) {
            System.out.println("Could not save high score.");
        }
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
