/*
Author: Kenny Z
Date: May 20th
Program: Paddle for PONG
Description: Manages the paddles in PONG
 */
import java.awt.*;
import java.awt.event.*;

// creates a paddle class which controls the paddles in PONG
public class Paddle extends Rectangle{

    public boolean moveUp, moveDown; // variables to keep track of where the paddle is moving
    public double yVelocity, yAccel, yPos; // controls the y movement
    public final double SPEED = 13; //movement speed of paddle
    public final Rectangle[] colliders = new Rectangle[2]; // 2 colliders
    public int offsetX; // the x offset of one of the colliders
    public int upKey, downKey; // which keys are up and down

    //constructor creates paddle at given location with given dimensions
    public Paddle(int x, int y, int PADDLE_WIDTH, int PADDLE_LENGTH, int upKey, int downKey){
        super(x, y, PADDLE_WIDTH, PADDLE_LENGTH);
        this.yPos = y;
        this.upKey = upKey;
        this.downKey = downKey;

        // creates the colliders of the paddle as rectangles
        this.colliders[0] = new Rectangle(0, 0, PADDLE_WIDTH/10, PADDLE_LENGTH);
        this.colliders[1] = new Rectangle(9 * PADDLE_WIDTH/10, 0, PADDLE_WIDTH/10, PADDLE_LENGTH);
        this.offsetX = 9 * PADDLE_WIDTH/10;
    }

    // method to check which direction the paddle should move or if it should stop moving when a key is released
    public void keyReleased(KeyEvent e){
        if(e.getKeyCode() == this.upKey){
            this.moveUp = false;
            this.setYMovement(SPEED);
        }

        if(e.getKeyCode() == this.downKey){
            this.moveDown = false;
            this.setYMovement(-SPEED);
        }
    }

    // method to check which direction the paddle should move or if it should stop moving when a key is released
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == this.upKey){
            this.moveUp = true;
            this.setYMovement(-SPEED);
        }

        if(e.getKeyCode() == this.downKey){
            this.moveDown = true;
            this.setYMovement(SPEED);
        }
    }

    // called whenever the movement of the paddle changes in the y-direction (up/down)
    public void setYDirection(double yDirection){
        this.yVelocity = yDirection;
    }

    // sets the y movement of the pdadle
    public void setYMovement(double yAccel) {
        if (this.moveUp || this.moveDown) { // if move up or move down are true, sets the paddle to move in the given direction
            this.yVelocity = yAccel;
            this.yAccel = yAccel/20;
        }
        else{ // else stops movement
            this.yVelocity = 0;
            this.yAccel = 0;
        }
    }

    //updates the current location of the paddle
    public void move(){
        // updates the position of the paddle
        yVelocity += yAccel;
        yPos = yPos + yVelocity;
        y = (int) Math.round(yPos);

        if (!this.moveDown && !this.moveUp) { // if the paddle is not moving, decreases the y acceleration and velocity of the paddle drastically
            yVelocity *= 0.01;
            yAccel *= 0.01;
        }

        // updates the position of the colliders by the position of the paddle
        this.colliders[0].setLocation(new Point(x, y));
        this.colliders[1].setLocation(new Point(x + offsetX, y));
    }

    //draws the current location of the paddle to the screen
    public void draw(Graphics g){
        g.setColor(Color.black);
        g.fillRect(x, y, width, height);
    }
}