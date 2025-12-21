/*
Author: Kenny Z
Date: May 20th
Program: GameFrame for PONG
Description: Main display of the PONG game
 */
import java.awt.*;
import javax.swing.*;

// class which extends JFrame, managing the GUI
public class GameFrame extends JFrame{
    GamePanel panel; // declares new panel

    // constructor to make a PONG game frame
    public GameFrame(){
        panel = new GamePanel(); //run GamePanel constructor
        this.add(panel);
        this.setTitle("PONG!"); //set title for frame
        this.setResizable(false); //frame can't change size
        this.setBackground(Color.white);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //X button will stop program execution
        this.pack();//makes components fit in window - don't need to set JFrame size, as it will adjust accordingly
        this.setVisible(true); //makes window visible to user
        this.setLocationRelativeTo(null);//set window in middle of screen
    }
}