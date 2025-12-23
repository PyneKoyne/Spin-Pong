/*
Author: Kenny Z
Date: May 20th
Program: PlayerBall for PONG
Description: The ball which serves as the main game element of Pong
 */

import java.awt.*;

// class to control the ball in pong
public class PlayerBall extends Rectangle{

    // global variables
    public double yAccel, yVelocity; // controls y movement
    public double theta, alpha, spin; // controls the spin of the ball
    public double xVelocity; // controls x movement
    public final int BALL_DIAMETER; //size of ball

    //constructor creates ball at given location with given dimensions
    public PlayerBall(int x, int y, int diameter){
        super(x, y, diameter, diameter);
        this.BALL_DIAMETER = diameter;

        this.theta = 0;
        this.alpha = 0;
        this.spin = 0;
    }

    //called whenever the acceleration of the ball changes in the y-direction (up/down)
    public void setYAccel(double yAccel){
        this.yAccel = yAccel;
    }

    //called whenever the movement of the ball changes in the y-direction (up/down)
    public void setYDirection(double yDirection){
        this.yVelocity = yDirection;
    }

    //called whenever the movement of the ball changes in the x-direction (left/right)
    public void setXDirection(double xDirection){
        this.xVelocity = xDirection;
    }

    // reflects the ball and adds a transferred velocity
    public void reflect(Integer direction){
        if (direction != null) { // if there is a direction, it was hit by a paddle
            this.setXDirection(-this.xVelocity); // x direction is reversed
            double randSign = Math.random() - 0.5;

            // y direction stays the same, and acceleration is slightly altered
            this.setYAccel((direction + (randSign / Math.abs(randSign)) * ((Math.random()/2 + 2)/(Math.abs(this.yVelocity/2) + 1.5))) / 2);
            this.setYDirection(this.yVelocity);

            // spin is given
            this.spin = (direction + (Math.random() * 2 - 1)) / 2;
        }
        else { // if there is not added direction, it is a wall, reflects both directions
            this.setYDirection(-this.yVelocity);
            this.setYAccel(-this.yAccel);
        }
    }

    // method to reset the position and speed of a ball when a point is scored
    public void resetPoint(int x, int y, boolean winner){
        this.x = x;
        this.y = y;
        this.setXDirection(winner ? (Math.random() * 2.5 + 7) : -(Math.random() * 2.5 + 7));
        this.setYDirection(Math.random() * 5 - 2.5);
    }

    // method to completely default all ball variables
    public void resetPosition(int x, int y){
        this.setXDirection(0);
        this.setYDirection(0);
        this.spin = 0;
        this.yAccel = 0;
        this.x = x;
        this.y = y;
        this.alpha = 0;
    }

    //updates the current location of the ball
    public void move(){
        // changes the spin of the ball
        alpha -= spin/24;
        theta += (yAccel + xVelocity/3)/40;

        // changes the position of the ball from its velocity
        y = (int) (y + yVelocity);
        x = (int) (x + xVelocity);

        xVelocity *= 1.0003; // ball slowly speeds up
        yVelocity += (yAccel + spin/18)/4; // y velocity is affected by acceleration

        // spin and acceleration deteriorate
        spin *= 0.98;
        yAccel *= 0.62;
    }

    //draws the current location of the ball to the screen
    public void draw(Graphics g){
        g.setColor(Color.black);

        // sets the x and y position to temp variables
        int xPos = x;
        int yPos = y;

        // draws the ball
        for (int i = -50; i < 50; i ++) {
            for (int j = -25; j < 25; j ++) {
                g.setColor(new Color((int) (Math.abs(i) * (254/50.0)), (int) (Math.abs(j) * (254/25.0)), 100));
                drawSpin((Math.PI * i / 50.0) + theta, Math.PI * j / 50.0 + alpha,  xPos, yPos, g);
            }
        }
    }

    // method to find where a point on the ball should be drawn on screen
    public void drawSpin(double theta, double alpha, int x, int y, Graphics g){
        // controllable variables of the camera
        double focalLength = 40;
        double radius = 80;

        // polar coordinates
        double dotX = BALL_DIAMETER * Math.cos(theta) * Math.cos(alpha);
        double dotY = BALL_DIAMETER * Math.sin(theta) * Math.cos(alpha);
        double dotZ = BALL_DIAMETER * Math.sin(alpha) * Math.sin(theta) / Math.abs(Math.sin(theta));

        // if the point is in front of the tangent line, the point is rendered
        if (dotY >= BALL_DIAMETER * BALL_DIAMETER / focalLength) {
            // finds the angle of the point to the camera
            double angle = Math.acos((focalLength - dotY) *
                    (invSquare(Math.pow(focalLength - dotY, 2) + Math.pow(dotX, 2) + Math.pow(dotZ, 2)) +
                            0.00012 - (BALL_DIAMETER / 18500.0 - 0.001082)));
            // finds the length of the arc to the point
            double arcLength = radius * (angle / Math.PI);
            // magnitude of the 2D vector of the point of the sphere projected as a circle
            double invMag = invSquare(dotX * dotX + dotZ * dotZ);
            g.fillRect((int) Math.round(x + BALL_DIAMETER/2.0 + (dotX * invMag) * arcLength), (int) Math.round(y + BALL_DIAMETER/2.0 + (dotZ * invMag) * arcLength), 2, 2); //draws the point on screen
        }
    }

    // Fast Quack3 Inverse Square Algorithm
    public double invSquare(double square){
        double half = 0.5d * square;
        long i = java.lang.Double.doubleToLongBits(square);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        square = java.lang.Double.longBitsToDouble(i);
        square *= (1.5d - half * square * square);

        return square;
    }
}