import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_TILES_X = WIDTH / TILE_SIZE;
    private static final int NUM_TILES_Y = HEIGHT / TILE_SIZE;

    private LinkedList<Point> snake;
    private Point food;
    private boolean gameOver;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private Timer timer;
    private int score;
    private int level;
    private int speed; // Variable to control the snake's speed
    private Clip eatSound; // Clip to play eating sound
    private Clip gameOverSound; // Clip to play game over sound

    public SnakeGame() {
        this.snake = new LinkedList<>();
        this.snake.add(new Point(NUM_TILES_X / 2, NUM_TILES_Y / 2)); // Snake starts in the middle
        spawnFood();
        this.gameOver = false;
        this.movingUp = false;
        this.movingDown = true; // Initial direction down
        this.movingLeft = false;
        this.movingRight = false;
        this.score = 0;
        this.level = 1;
        this.speed = 100; // Starting speed (lower is faster)
        this.timer = new Timer(speed, this);
        this.timer.start();

        // Load sound effects
        loadSounds();

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        setFocusable(true);
    }

    private void loadSounds() {
        try {
            // Load the eat sound effect
            AudioInputStream eatStream = AudioSystem.getAudioInputStream(new File("eat_sound.wav"));
            eatSound = AudioSystem.getClip();
            eatSound.open(eatStream);

            // Load the game over sound effect
            AudioInputStream gameOverStream = AudioSystem.getAudioInputStream(new File("game_over_sound.wav"));
            gameOverSound = AudioSystem.getClip();
            gameOverSound.open(gameOverStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void spawnFood() {
        Random rand = new Random();
        int x = rand.nextInt(NUM_TILES_X);
        int y = rand.nextInt(NUM_TILES_Y);
        food = new Point(x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            return;
        }

        // Move snake in the current direction
        Point head = snake.getFirst();
        Point newHead = new Point(head);

        if (movingUp) {
            newHead.y--;
        } else if (movingDown) {
            newHead.y++;
        } else if (movingLeft) {
            newHead.x--;
        } else if (movingRight) {
            newHead.x++;
        }

        // Check if snake runs into itself or the walls
        if (newHead.x < 0 || newHead.x >= NUM_TILES_X || newHead.y < 0 || newHead.y >= NUM_TILES_Y
                || snake.contains(newHead)) {
            gameOver = true;
            playGameOverSound(); // Play game over sound
            repaint();
            return;
        }

        // Add new head to snake
        snake.addFirst(newHead);

        // Check if snake eats food
        if (newHead.equals(food)) {
            spawnFood(); // Spawn new food
            score++; // Increase score when food is eaten
            playEatSound(); // Play eat sound
            increaseLevel(); // Check if level should be increased
        } else {
            snake.removeLast(); // Remove tail if no food eaten
        }

        repaint();
    }

    private void increaseLevel() {
        // Increase the level every 5 points
        if (score % 5 == 0 && score > 0) {
            level++;
            speed -= 10; // Increase speed (lower time interval)
            if (speed < 50)
                speed = 50; // Limit the minimum speed
            timer.setDelay(speed); // Update timer delay
        }
    }

    private void playEatSound() {
        if (eatSound != null) {
            eatSound.setFramePosition(0); // Rewind to the start
            eatSound.start(); // Play the eat sound
        }
    }

    private void playGameOverSound() {
        if (gameOverSound != null) {
            gameOverSound.setFramePosition(0); // Rewind to the start
            gameOverSound.start(); // Play the game over sound
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", WIDTH / 3, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score, WIDTH / 3, HEIGHT / 2 + 40);
            g.drawString("Final Level: " + level, WIDTH / 3, HEIGHT / 2 + 70);
            return;
        }

        // Draw snake
        g.setColor(Color.GREEN);
        for (Point p : snake) {
            g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Draw food
        g.setColor(Color.RED);
        g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw score and level
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, 10, 30);
        g.drawString("Level: " + level, WIDTH - 100, 30); // Display level at top-right corner
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            return;
        }

        int key = e.getKeyCode();

        // Prevent snake from reversing
        if (key == KeyEvent.VK_UP && !movingDown) {
            movingUp = true;
            movingDown = false;
            movingLeft = false;
            movingRight = false;
        } else if (key == KeyEvent.VK_DOWN && !movingUp) {
            movingDown = true;
            movingUp = false;
            movingLeft = false;
            movingRight = false;
        } else if (key == KeyEvent.VK_LEFT && !movingRight) {
            movingLeft = true;
            movingRight = false;
            movingUp = false;
            movingDown = false;
        } else if (key == KeyEvent.VK_RIGHT && !movingLeft) {
            movingRight = true;
            movingLeft = false;
            movingUp = false;
            movingDown = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
    }
}