/*
Author: Kenny Z
Date: May 20th
Program: MAIN for PONG
Description: Runs the game
 */
// MAIN class which is called to create the game
class Main {
    // the main method which is run
    public static void main(String[] args) {
        int width = 1280;
        int height = 720;
        if (args.length == 2){
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
        }
        new GameFrame(width, height); // creates a new game frame
    }
}