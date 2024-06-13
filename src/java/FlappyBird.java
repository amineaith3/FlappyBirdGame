package src.java;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 640;
    int boardHeight = 640;

    //images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    private JFrame mainFrame;
    private JPanel menuPanel;
    private boolean inGame = true;

    //game logic
    Bird bird;
    int velocityX = -4; //move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; //move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird(JFrame frame, JPanel menuPanel) {
        this.mainFrame = frame;
        this.menuPanel = menuPanel;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("/src/ressources/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/src/ressources/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/src/ressources/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/src/ressources/bottompipe.png")).getImage();

        //bird
        bird = new Bird(boardWidth / 8, boardHeight / 2, 34, 24, birdImg);
        pipes = new ArrayList<Pipe>();

        //place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        //game timer
        gameLoop = new Timer(1000 / 60, this); //how long it takes to start timer, milliseconds gone between frames
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (- boardHeight / 4 - Math.random() * (boardHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(boardWidth, randomPipeY, 64, 512, topPipeImg);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(boardWidth, topPipe.y + topPipe.height + openingSpace, 64, 512, bottomPipeImg);
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        //bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
            g.drawString("Click Q to return to the menu", 10, 65);
        }
    }

    public void move() {
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); //apply gravity to current bird.y, limit the bird.y to top of the canvas

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; //0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Remove pipes that are off the screen
        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                //restart game by resetting conditions
                bird.y = boardHeight / 2;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            returnToMenu();
        }
    }

    private void returnToMenu() {
        inGame = false; // Set to menu state

        // Stop game-related timers or processes if needed
        gameLoop.stop();
        placePipeTimer.stop();

        // Remove all components from current frame and add menuPanel back
        mainFrame.getContentPane().removeAll();
        mainFrame.add(menuPanel);

        // Resize and revalidate the frame
        mainFrame.pack();

        // Make the frame visible
        mainFrame.setVisible(true);
        mainFrame.setSize(640, 640);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}