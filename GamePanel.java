/*
Author: Kenny Z
Date: May 20th
Program: GamePanel for PONG
Description: Displayed panel which runs the Pong game
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.*;

// class to handle the actual execution of the Pong game and its rendering on screen
public class GamePanel extends JPanel implements Runnable, KeyListener{

    //dimensions of window
    public final int GAME_WIDTH;
    public final int GAME_HEIGHT;
    public Thread gameThread; // thread to control the game
    public BufferedImage image; // the image which the pong game is rendered on
    public BufferedImage postProd; // post render logic for animations
    public Graphics2D graphics; // graphics class to help manipulate the screen
    public FontMetrics fm;
    public boolean inGame; // boolean whether a pong game is currently active
    public double animationProgress; // progress of the animation if it is active
    public Paddle paddle1, paddle2; // 2 paddles which are controlled by the keyboard
    public PlayerBall ball; // pong ball
    public int score1, score2; // scores of the respective paddles
    public int winner; // the winner of the last match if any

    // constructor
    public GamePanel(int width, int height){
        GAME_WIDTH = width;
        GAME_HEIGHT = height;

        int diameter = GAME_HEIGHT/144 + 15;

        // instantiates our variables
        paddle1 = new Paddle(GAME_WIDTH/64, 5 * GAME_HEIGHT/12, GAME_WIDTH/64, GAME_HEIGHT/6, KeyEvent.VK_W, KeyEvent.VK_S); //create a player controlled ball, set start location to middle of screen
        paddle2 = new Paddle(31 * GAME_WIDTH/32, 5 * GAME_HEIGHT/12, GAME_WIDTH/64, GAME_HEIGHT/6, KeyEvent.VK_UP, KeyEvent.VK_DOWN); //create a player controlled ball, set start location to middle of screen
        ball = new PlayerBall(GAME_WIDTH * 59 / 64 - diameter - 6 * (GAME_HEIGHT/720), GAME_HEIGHT/2 + 8, diameter);

        GraphicsConfiguration CONFIG = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        image = CONFIG.createCompatibleImage(GAME_WIDTH, GAME_HEIGHT); //draw off screen
        postProd = CONFIG.createCompatibleImage(GAME_WIDTH, GAME_HEIGHT);

        winner = 0;
        score1 = 0;
        score2 = 0;
        animationProgress = 1;
        inGame = false;

        this.setFocusable(true); //make everything in this class appear on the screen
        this.addKeyListener(this); //start listening for keyboard input
        this.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));

        //make this class run at the same time as other classes.
        gameThread = new Thread(this);
        gameThread.start();
    }

    // paint method for rendering
    public void paint(Graphics g){
        graphics = (Graphics2D) image.getGraphics();
        draw(graphics); //update the positions of everything on the screen

        // if animation progress is greater than 0, runs post-processing on it
        if (animationProgress > 0) {
            // rotates the image depending on the winner
            if (winner == 0 || winner == 2) graphics.rotate(3 * Math.PI/2);
            else graphics.rotate(Math.PI/2);

            // sets colour and font
            graphics.setColor(new Color((int) (100 * (255 * (1 - animationProgress)/100)), (int) (100 * (255 * (1 - animationProgress)/100)), (int) (100 * (255 * (1 - animationProgress)/100))));
            graphics.setFont(new Font("Fairfax", Font.PLAIN, 25 * (GAME_HEIGHT/720) + 20));
            fm = graphics.getFontMetrics();

            // draws the PONG logo on either the right or left side depending on the winner
            if (winner == 0 || winner == 2) graphics.drawString("P   NG", -GAME_HEIGHT/2 - SwingUtilities.computeStringWidth(fm, "P   NG")/2, GAME_WIDTH * 59/64);
            else graphics.drawString("P   NG", GAME_HEIGHT/2 - SwingUtilities.computeStringWidth(fm, "P   NG")/2, -GAME_WIDTH * 5/64);

            // sets font to a small font
            graphics.setFont(new Font("Fairfax", Font.PLAIN, 10));
            fm = graphics.getFontMetrics();

            // displays a message explaining who the winner is if there was one
            if (winner == 2) graphics.drawString("Right Won!", -GAME_HEIGHT * 3 / 7 - SwingUtilities.computeStringWidth(fm, "Right Won!")/2, GAME_WIDTH * 60/64);
            else if (winner == 1) graphics.drawString("Left Won!", GAME_HEIGHT * 3 / 7 - SwingUtilities.computeStringWidth(fm, "Left Won!")/2, -GAME_WIDTH * 4/64);

            // sets colour
            graphics.setColor(new Color((int) (100 * animationProgress), (int) (100 * animationProgress), (int) (100 * animationProgress)));

            // displays a string explaining how to start the match
            if (winner == 0 || winner == 2) graphics.drawString("Press Space to Start", -GAME_HEIGHT/2 - SwingUtilities.computeStringWidth(fm, "Press Space to Start")/2, GAME_WIDTH * 63/64 - 6);
            else graphics.drawString("Press Space to Start", GAME_HEIGHT/2 - SwingUtilities.computeStringWidth(fm, "Press Space to Start")/2, -GAME_WIDTH /64 - 6);

            // creates the transformation for the animation
            AffineTransformOp scaleOp = getAnimationTransform(winner == 0 || winner == 2);

            // resets the post-production image
            Graphics postG = postProd.getGraphics();
            postG.setColor(Color.white);
            postG.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

            postProd = scaleOp.filter(image, postProd); // apples the transformation onto the image and renders it on postProd
            g.drawImage(postProd, 0, 0, this); //move the image on the screen
        }
        else{ // otherwise renders the original image
            g.drawImage(image, 0, 0, this); //move the image on the screen
        }
    }

    // method to create the affine transformation for the animation
    private AffineTransformOp getAnimationTransform(boolean rightRotate) {
        AffineTransform at = new AffineTransform(); // creates an empty transformation
        if (rightRotate) { // if the animation is centered on the right paddle
            at.translate((double) (63 * GAME_WIDTH) / 64, (double) GAME_HEIGHT / 2);
            at.rotate(animationProgress * Math.PI / 2);
            at.scale(1 + (3 * animationProgress), 1 + (3 * animationProgress));
            at.translate(-(double) (63 * GAME_WIDTH) / 64, -(double) GAME_HEIGHT / 2);
            at.translate((double) (GAME_HEIGHT / 8 - GAME_WIDTH / 64) * animationProgress, (62 * animationProgress * GAME_WIDTH) / (512));
        }
        else{ // if the animation is centered on the left paddle
            at.translate((double) GAME_WIDTH / 64, (double) GAME_HEIGHT / 2);
            at.rotate(animationProgress * -Math.PI / 2);
            at.scale(1 + (3 * animationProgress), 1 + (3 * animationProgress));
            at.translate(-(double) GAME_WIDTH / 64, -(double) GAME_HEIGHT / 2);
            at.translate((double) (-GAME_HEIGHT / 8 + GAME_WIDTH / 64) * animationProgress, (66 * animationProgress * GAME_WIDTH) / (512));
        }
        return new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR); // returns the transformation
    }

    // call the draw methods in each class to update positions as things move
    public void draw(Graphics2D g){
        // sets the font and colour
        g.setFont(new Font("Fairfax", Font.PLAIN, 45));
        g.setColor(Color.white);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT); // draws the background
        g.setColor(Color.lightGray);

        // draws the two end zones
        g.fillRect(0, 0, GAME_WIDTH/18, GAME_HEIGHT);
        g.fillRect(GAME_WIDTH * 17/18, 0, GAME_WIDTH/18, GAME_HEIGHT);

        // draws the middle line
        g.setColor(Color.gray);
        g.fillRect(GAME_WIDTH/2 - 1, 0, 2, GAME_HEIGHT);

        ball.draw(g);
        paddle1.draw(g);
        paddle2.draw(g);

        //displays the two scores and the top and bottom lines
        fm = g.getFontMetrics();
        int stringOffset = SwingUtilities.computeStringWidth(fm, String.valueOf(score2));
        g.drawString("" + score1, GAME_WIDTH * 3 / 36,GAME_WIDTH/18);
        g.drawString("" + score2, GAME_WIDTH * 33 / 36 - stringOffset,GAME_WIDTH/18);
        g.setColor(Color.gray);
        g.fillRect(GAME_WIDTH * 3 / 72, 0, GAME_WIDTH * 11 / 12, GAME_WIDTH / 72);
        g.fillRect(GAME_WIDTH * 3 / 72, GAME_HEIGHT - GAME_WIDTH /72, GAME_WIDTH * 11 / 12, GAME_WIDTH / 72);

        // displays instructions for the game if the ball has not hit a paddle
        if (animationProgress < 1 && animationProgress >= 0 && inGame){
            graphics.setColor(new Color((int) (100 * animationProgress), (int) (100 * animationProgress), (int) (100 * animationProgress)));
            graphics.drawString("First To 5", GAME_WIDTH/2 - SwingUtilities.computeStringWidth(fm, "First To 5")/2, GAME_HEIGHT*4/9);
            graphics.setFont(new Font("Fairfax", Font.PLAIN, 20));
            fm = graphics.getFontMetrics();
            graphics.drawString("Hit The Ball With A Moving Paddle To Curve", GAME_WIDTH/2 - SwingUtilities.computeStringWidth(fm, "Hit The Ball With A Moving Paddle To Curve")/2, GAME_HEIGHT*5/9);
            graphics.drawString("W / S to Move Paddle", GAME_WIDTH /4 - SwingUtilities.computeStringWidth(fm, "W / S to Move Paddle")/2, GAME_HEIGHT /2);
            graphics.drawString("Up / Down to Move Paddle", GAME_WIDTH * 3/4 - SwingUtilities.computeStringWidth(fm, "Up / Down to Move Paddle")/2, GAME_HEIGHT /2);
        }
    }

    //call the move methods in other classes to update positions
    public void move(){
        ball.move();
        paddle1.move();
        paddle2.move();
    }

    // handles all collision detection and responds accordingly
    public void checkCollision(){
        if (!checkLeftBounce(paddle1.colliders[0], paddle1)) checkLeftBounce(paddle2.colliders[0], paddle2); // check if the ball has hit a left collider
        if (!checkRightBounce(paddle1.colliders[1], paddle1)) checkRightBounce(paddle2.colliders[1], paddle2); // check if the ball has hit a right collider

        //force paddles to remain on screen
        paddleBounds(paddle1);
        paddleBounds(paddle2);

        // check if the ball collides with the ceiling
        if (ball.y < GAME_WIDTH/70) {
            ball.y = GAME_WIDTH/70;
            ball.reflect(null);
        }
        else if(ball.y > GAME_HEIGHT - ball.BALL_DIAMETER - GAME_WIDTH/70){ // check if the ball collides with the floor
            ball.y = GAME_HEIGHT - ball.BALL_DIAMETER - GAME_WIDTH/70;
            ball.reflect(null);
        }
        else if (ball.x < -ball.BALL_DIAMETER){ // check if the ball hits the left wall
            ball.resetPoint(GAME_WIDTH/2, GAME_HEIGHT/2, true); // resets the point
            score2 += 1;
            if (score2 >= 5){ // resets game if the score is greater than 5
                winner = 2;
                resetGame();
            }
        }
        else if(ball.x > GAME_WIDTH){ //check if the ball hits the right wall
            ball.resetPoint(GAME_WIDTH/2, GAME_HEIGHT/2, false); // resets the point
            score1 += 1;
            if (score1 >= 5){ // resets game if the score is greater than 5
                winner = 1;
                resetGame();
            }
        }
    }

    // method to ensure the paddles don't leave the display space
    private void paddleBounds(Paddle paddle) {
        if(paddle.yPos < 0){
            paddle.yPos = 0;
            paddle.y = 0;
            paddle.setYMovement(0);
        }
        else if(paddle.yPos > GAME_HEIGHT - paddle.height){
            paddle.yPos = GAME_HEIGHT - paddle.height;
            paddle.y = GAME_HEIGHT - paddle.height;
            paddle.setYMovement(0);
        }
    }

    // method to reset the game
    public void resetGame() {
        // instantiates all game variables to their default state
        inGame = false;
        animationProgress = 0.005;
        score1 = 0;
        score2 = 0;
        paddle1.moveUp = false;
        paddle1.moveDown = false;
        paddle2.moveUp = false;
        paddle2.moveDown = false;

        // resets the ball depending on which side won for the animation
        if (winner == 0 || winner == 2) ball.resetPosition(GAME_WIDTH * 59 / 64 - ball.BALL_DIAMETER - 6, GAME_HEIGHT/2 + ball.BALL_DIAMETER - 13);
        else ball.resetPosition(GAME_WIDTH * 5 / 64 + ball.BALL_DIAMETER - 15, GAME_HEIGHT/2 - ball.BALL_DIAMETER - 8);
    }

    // method which checks if the ball collides with a right collider of a paddle
    public boolean checkRightBounce(Rectangle collider, Paddle paddle){
        if (collider.intersects(ball)) {
            ball.x = paddle.x + paddle.width + ball.BALL_DIAMETER;
            ball.reflect((int) paddle.yVelocity); // reflects the ball
            animationProgress = -1; // sets animationProgress to -1 if it wasn't already to remove instructions
            return true;
        }
        return false;
    }

    // method which checks if the ball collides with a left collider of a paddle
    public boolean checkLeftBounce(Rectangle collider, Paddle paddle){
        if (collider.intersects(ball)) {
            ball.x = paddle.x - ball.BALL_DIAMETER;
            ball.reflect((int) paddle.yVelocity); // reflects the ball
            animationProgress = -1; // sets animationProgress to -1 if it wasn't already to remove instructions
            return true;
        }
        return false;
    }

    // method with game loop to run PONG
    public void run(){
        // set up time controls
        long lastTime = System.nanoTime();
        double amountOfTicks = 70;
        double ns = 1000000000/amountOfTicks;
        double delta = 0;
        long now;

        while(true){ //this is the infinite game loop
            // finds the change in seconds times 60
            now = System.nanoTime();
            delta = delta + (now-lastTime)/ns;
            lastTime = now;

            //only move objects around and update screen if enough time has passed
            if(delta >= 1){
                // code to make the display look pretty when there is no active game
                if(!inGame){
                    if (animationProgress != 1){
                        animationProgress *= 1.05;
                        if(animationProgress > 1){
                            animationProgress = 1;
                        }
                    }
                    ball.theta += 0.05;
                    paddle1.setYDirection(((5.0 * GAME_HEIGHT)/12 - paddle1.y)/9);
                    paddle2.setYDirection(((5.0 * GAME_HEIGHT)/12 - paddle2.y)/9);
                }
                else if (animationProgress != 1) { // if the game is active but animationProgress is not 1, starts the animation
                    animationProgress /= 1.1;
                    if (animationProgress <= 0.0005 && animationProgress > 0){ // threshold to stop the animation
                        animationProgress = 0;
                    }
                }

                // calls the following methods to run the game
                move();
                checkCollision();
                repaint();
                delta--; // resets delta time
            }
        }
    }

    // method to handle key presses
    public void keyPressed(KeyEvent e){
        if (inGame) { // only moves paddles when a game is active
            paddle1.keyPressed(e);
            paddle2.keyPressed(e);
        }
        else{ // waits for the space key to be pressed otherwise
            if (e.getKeyChar() == KeyEvent.VK_SPACE){
                animationProgress -= 0.005;
                inGame = true;
                ball.resetPoint(ball.x, ball.y, winner == 1);
            }
        }
    }

    // method to handle key releases
    public void keyReleased(KeyEvent e){
        if (inGame) { // only runs when a game is active
            paddle1.keyReleased(e);
            paddle2.keyReleased(e);
        }
    }

    // method to handle key types
    // left empty because we don't need it
    public void keyTyped(KeyEvent e){ }
}