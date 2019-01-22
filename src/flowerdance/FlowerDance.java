/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flowerdance;

import java.io.*;
import java.util.Scanner;
import java.awt.*; //needed for graphics
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.*; //needed for graphics
import static javax.swing.JFrame.EXIT_ON_CLOSE; //needed for graphics

/**
 *
 * @author wuj0394
 */
public class FlowerDance extends JFrame{

    //FIELDS
    int numGenerations = 1000;
    int currGeneration = 1;

    int width = 800; //width of the window in pixels
    int height = 800;
    int borderWidth = 50;  

    int numCellsX = 50; //width of the grid (in cells)
    int numCellsY = numCellsX;
   
    int cellWidth = (width - 2*borderWidth)/numCellsX; 
     
    int labelX = width / 2;
    int labelY = borderWidth;
    
    int start = 2;  //number of generations before rain/birds come
    int time = 300;  //for sleep()
    
    //how many generations it takes to change seed stage DEFAULT: 15, 30, 45, 60
    int gen2 = 15; 
    int gen3 = 30;
    int gen4 = 45;
    int finalGen = 60;  //seed blooms into flower
    int finalGenSuper = 10;
    
    int seed = 9;  //value between 1-99 that determines amount of seeds (LOWER means LESS seeds) DEFAULT: 9
    
    int rain = 1;  //value between 1-99 that determines amount of rain (LOWER means LESS rain) DEFAULT: 1
    
    int bird = 4;  //determines amount of birds (num between 1-1000) DEFAULT: 4
    int birdDropSeed = 500;  //chance of bird dropping a seed at its location (0-1000) DEFAULT: 500
    int superChance = 10; //chance of a super seed (0-1000) DEFAULT: 10
    int birdEat = 100;  //chance of bird eating seeds around it (100 = 100%, -1 = 0%) DEFAULT: 30
    
    //Seed Fields
    /*
    0 = dead
    1 = alive
    2 = Super Seed
    */
    int seedAlive [][] = new int [numCellsX][numCellsY];
    int seedAliveNext [][] = new int [numCellsX][numCellsY];
    int seedLife [][] = new int [numCellsX][numCellsY];  //how long the seed has been alive
    
    Color seed1 = new Color(255,148,53);
    Color seed2 = new Color(192,90,0);
    Color seed3 = new Color(115,54,0);
    Color seed4 = new Color(33,15,0);
    Color superSeed = new Color(169,169,169);

    //Rain and Bird Fields
    boolean rainAlive [][] = new boolean[numCellsX][numCellsY];
    boolean rainAliveNext [][] = new boolean[numCellsX][numCellsY];
    
    boolean birdAlive [][] = new boolean[numCellsX][numCellsY];
    boolean birdAliveNext [][] = new boolean[numCellsX][numCellsY];
  
    //Petals Fields
    int petalAlive[][] = new int [numCellsX][numCellsY]; 
    Color color[][] = new Color[numCellsX][numCellsY];
    

    //METHODS
    public void plantFirstGeneration() throws IOException {
        makeEveryoneDead();
        //plantFromFile( fileName );
        
        Random r = new Random();
        for(int j = 0; j < seedAlive.length; j++){

            for (int i = 0; i < seedAlive.length; i++) {      
                int num = r.nextInt(100);        
                int ranBird = r.nextInt(1000);
                
                if(num < seed){             
                    //does not initialize super seeds
                    seedAlive[j][i] = r.nextInt(2);
                    
                }
                   
                //randomly creates rain
                //rain cannot be where a seed or petal is already planted 
                if(num < rain && seedAlive[i][j] == 0 && petalAlive[i][j] == 0 && currGeneration > start)
                    rainAlive[i][j] = true;
    
                //randomly creates birds
                //birds cannot be where a seed, rain, or petal is already planted 
                if(ranBird < bird && seedAlive[i][j] == 0 && petalAlive[i][j] == 0 && !rainAlive[i][j])
                    birdAlive[i][j] = true;
            }   
        }
    }

    
    //Sets all cells to dead
    public void makeEveryoneDead() {
        for (int i = 0; i < numCellsX; i++) {
            for (int j = 0; j < numCellsY; j++) {
                seedAlive[i][j] = 0;
            }
        }
    }


    //Applies the rules of The Game of Life to set the true-false values of the aliveNext[][] array,
    //based on the current values in the alive[][] array
    public void computeNextGeneration() {
        
        for(int i = 0; i < numCellsX; i++){
            
            for(int j = 0; j < numCellsY; j ++){
                
                int neighbors = countLivingNeighbors(i, j); 
                
                //if the seeds are ready to bloom
                if((seedAlive[i][j] == 1 && seedLife[i][j] > finalGen) || (seedAlive[i][j] == 2 && seedLife[i][j] > finalGenSuper)){
                   bloom(i, j);   
                }

                //increments each seed's life if it is alive (1) or super (2) 
                if(seedAlive[i][j] == 1 && petalAlive[i][j] == 0){
                    seedAliveNext[i][j] = 1;
                    if(neighbors < 4)   //Only grow seed if they have less than 4 neighbors
                        seedLife[i][j] ++;
                }
                
                else if (seedAlive[i][j] == 2 && petalAlive[i][j] == 0){
                    seedAliveNext[i][j] = 2;
                    seedLife[i][j] ++;
                }
                
                //resets the life of a seed
                else{
                    seedAliveNext[i][j] = 0;
                    seedLife[i][j] = 0;
                }   
                
                //creates new birds
                //determines if the bird will drop a seed/super seed 
                Random r = new Random();
                int numB = r.nextInt(1000);
                int drop = r.nextInt(1000);
                
                if(numB < bird && seedAlive[i][j] == 0 && petalAlive[i][j] == 0 && currGeneration > start){
                    birdAliveNext[i][j] = true;
                    seedsInBirdRange(i, j);
                    
                    if(drop < birdDropSeed){
                        if (drop < superChance){
                            seedAliveNext[i][j] = 2;
                            seedLife[i][j] = -1;  //-1 so that the seed will be painted after the bird leaves
                        }
                        else{
                            seedAliveNext[i][j] = 1;  
                            seedLife[i][j] = -1;
                        }
                    }
                    
                }
                else
                    birdAliveNext[i][j] = false;


                //creates new rain
                int num = r.nextInt(100);

                if(num < rain && seedAlive[i][j] == 0 && petalAlive[i][j] == 0 && currGeneration > start){
                    rainAliveNext[i][j] = true;
                    seedsInRainRange(i, j);  //tests if there are any seeds near rain
                }    
                else{
                    rainAliveNext[i][j] = false;
                }    
                
            } 
        }
    }
  
    //Overwrites the current generation's 2-D array with the values from the next generation's 2-D array
    public void plantNextGeneration() {
        
        for(int i = 0; i < seedAlive.length; i ++){
            
            for(int j = 0; j < seedAlive[i].length; j ++){
                
                seedAlive[i][j] = seedAliveNext[i][j];
                
                rainAlive[i][j] = rainAliveNext[i][j];
                
                birdAlive[i][j] = birdAliveNext[i][j];
                
            }           
        }  
    }

        //Counts the number of living cells adjacent to cell (i, j)
    public int countLivingNeighbors(int i, int j) {
        int begRow, begCol, endRow, endCol;
        int numNeighbours = 0;
        if (i == 0)
            begCol = i;
        else 
            begCol = i - 1;

        if (i == numCellsX - 1) 
            endCol = i;
        else 
            endCol = i + 1;
        
        if (j == 0) 
            begRow = j;
        else 
            begRow = j - 1;    

        if (j == numCellsY - 1) 
            endRow = j;
        else 
            endRow = j + 1;
        for (int m = begCol; m <= endCol; m++){
            for (int n = begRow; n <= endRow; n++){
                if (m != i || n != j)
                    if (seedAlive[m][n] == 1 || seedAlive[m][n] == 2)
                        numNeighbours++; 
            }
        }
        return numNeighbours;
    }
    
    
    public void seedsInRainRange(int i, int j){

        int startRow, endRow, startCol, endCol;
 
        if (i==0) //if rain is in top corner
            startCol = i;
        
        else if( i==1)
            startCol = i-1;
        
        else 
            startCol = i-2;
           
        if (i == numCellsX - 1) //if bottom corner
            endCol = i;
        
        else if (i == numCellsX - 2)
            endCol = i+1;
        
        else
            endCol = i+2;
        
        if(j == 0)  //if left corner
            startRow = j;
        
        else if(j == 1)
            startRow = j-1;
            
        else 
            startRow = j-2;
         
        if(j == numCellsY - 1)  //if right corner
            endRow = j;
        
        else if(j == numCellsY - 2)
            endRow = j+1;
        
        else
            endRow = j+2;

        for(int m = startCol; m <= endCol; m ++){
           
            for(int n = startRow; n <= endRow; n ++){
                
                //if there is a seed near the rain, grow it
                if(seedAlive[m][n] == 1 || seedAlive[m][n] == 2){
                    seedLife[m][n]++;                 
                }  
            }
        }
    }
    

    public void seedsInBirdRange(int i, int j) {
     
        int startRow, endRow, startCol, endCol;
        
        if (i==0)  //if bird is in top corner
            startCol = i;
        
        else 
            startCol = i-1;
        
        if (i == numCellsX - 1)  //if bottom corner
            endCol = i;
        
        else
            endCol = i+1;
        
        if(j==0)  //if left corner
            startRow = j;
        
        else 
            startRow = j-1;
        
        if(j==numCellsY - 1)  //if right corner
            endRow = j;
        
        else
            endRow = j+1;
                
        for(int m = startCol; m <= endCol; m ++){
           
            for(int n = startRow; n <= endRow; n ++){
                
                Random r = new Random();
                int ranBird = r.nextInt(100);

                if(seedAlive[m][n] == 1){
                    
                    if(ranBird < birdEat){
                        seedAlive[m][n] = 0;
                        seedAliveNext[m][n] = 0;  //bird eats the seed 
                    }
                }
            }
        }
    }
    
    
    public void bloom(int i, int j){     
        Random r = new Random();
        int type = r.nextInt(2);
        int c = r.nextInt(3);  //0, 1, 2 determines the color of the flower petals
        Color C;
        
        if(c == 1){  //purple
            C = new Color(171,0,255);     
        }

        else if (c == 2){  //pink
            C = new Color(255,0,179);   
        }

        else{  //orange
            C = new Color(255,102,0);   
        }
        
        
        if(i>1 && i<numCellsX - 2 && j>1 && j<numCellsY-2){
            

            if(type == 1){
                seedAlive[i][j] = 0; //kills seed

                petalAlive[i][j-1] = 1;
                petalAlive[i-1][j] = 1;
                petalAlive[i][j] = 2;
                petalAlive[i+1][j] = 1;
                petalAlive[i][j+1] = 1;
                
                color[i][j-1] = C;
                color[i-1][j] = C;
                color[i][j] = Color.YELLOW;
                color[i+1][j] = C;
                color[i][j+1] = C;
            }

    
            else{
                seedAlive[i][j] = 0; //kills seed
                
                petalAlive[i][j-2] = 1;
                petalAlive[i-1][j-1] = 1;
                petalAlive[i][j-1] = 1;
                petalAlive[i+1][j-1] = 1;
                petalAlive[i-2][j] = 1;
                petalAlive[i-1][j] = 1;
                petalAlive[i][j] = 2;
                petalAlive[i+1][j] = 1;
                petalAlive[i+2][j] = 1;
                petalAlive[i-1][j+1] = 1;
                petalAlive[i][j+1] = 1;
                petalAlive[i+1][j+1] = 1;
                petalAlive[i][j+2] = 1;
                
                color[i][j-2] = C;
                color[i-1][j-1] = C;
                color[i][j-1] = C;
                color[i+1][j-1] = C;
                color[i-2][j] = C;
                color[i-1][j] = C;
                color[i][j] = Color.YELLOW;
                color[i+1][j] = C;
                color[i+2][j] = C;
                color[i-1][j+1] = C;
                color[i][j+1] = C;
                color[i+1][j+1] = C;
                color[i][j+2] = C;

            }    
        }    
    }

    
    //Makes the pause between generations
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } 
        catch (Exception e) {}
    }

    
    //Displays the statistics at the top of the screen
    void drawLabel(Graphics g, int state) {
        g.setColor(Color.black);
        g.fillRect(0, 0, width, borderWidth);
        g.setColor(Color.yellow);
        g.drawString("Generation: " + state, labelX, labelY);
    }

    
    //Draws the current generation of living cells on the screen
    public void paint( Graphics g){
        Image img = createImage();
        g.drawImage(img,8,30,this);
    }
    
    //Draws the current generation of living cells on the screen
    public Image createImage(){
        BufferedImage bufferedImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        drawLabel(g, currGeneration);
        int x, y, i, j;
        
        x = borderWidth;  ///dimesion of cell
        y = borderWidth;


        //creates the grid
        for (i = 0; i < numCellsX; i++) {
            //Fill this in        
            for (j = 0; j < numCellsY; j++) {  

                int xSeed = i * cellWidth;
                int ySeed = j * cellWidth;
                
                
                //paints a seed if alive and changes color
                if(seedAlive[i][j] == 1 && seedLife[i][j] >= 0){
                    
                    if (seedLife[i][j] >= gen4)
                        g.setColor(seed4);
                    
                    else if (seedLife[i][j] >= gen3)
                        g.setColor(seed3);
                    
                    else if (seedLife[i][j] >= gen2)
                        g.setColor(seed2);
                    
                    else
                        g.setColor(seed1);
                }
                
                //paints super seed
                else if(seedAlive[i][j] == 2 && seedLife[i][j] >= 0)
                    g.setColor(superSeed);
                
                
                //paints rain
                else if(rainAlive[i][j])
                    g.setColor(Color.BLUE);            

                //paints birds
                else if(birdAlive[i][j])
                    g.setColor(Color.RED);
                
                //paints petals
                else if (petalAlive[i][j] == 1)
                    g.setColor(color[i][j]);
          
                else if (petalAlive[i][j] == 2)
                    g.setColor(Color.YELLOW);
                
                else
                    g.setColor(Color.GREEN);

                
                //paints seeds, rain, and birds
                g.fillRect(borderWidth +xSeed, borderWidth+ySeed, cellWidth, cellWidth);
                    
                x += cellWidth;         
            }
            x = borderWidth;  //set x back to the left
            y += cellWidth;  //move y down a row
        }    
        return bufferedImage;
    }


    //Sets up the JFrame screen
    public void initializeWindow() {
        setTitle("Game of Life Simulator");
        setSize(height, width);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.black);
        setVisible(true); //calls paint() for the first time
    }
    
    
    //Main algorithm
    public static void main(String args[]) throws IOException {

        FlowerDance currGame = new FlowerDance();


        currGame.plantFirstGeneration(); //Sets the initial generation of living cells, either by reading from a file or creating them algorithmically
        currGame.initializeWindow();

        for (int i = 1; i < currGame.numGenerations; i++) {
            //fill this in
            currGame.repaint();
            currGame.computeNextGeneration();
            currGame.plantNextGeneration();  //fill cellsOnNext
            currGame.sleep(currGame.time);

            
            currGame.currGeneration ++;  //update the gen counter
            
            
        }
        
    } 
    
} //end of class

