import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardheight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Bird
    int birdX = boardWidth/8;
    int birdY = boardheight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }


    //Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; //scaled by 1/6
    int pipeHeight = 512;

    class Pipe{
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }
    }

    //Game logic
    Bird bird;
    int velocityX = -4; //pipe moves to the left speed
    int velocityY = 0; //bird move up/down speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer  placePipesTimer;

    boolean gameOver = false;

    double score = 0;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardheight));
        //setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();


        //place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();;

        //game timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();


    }


    public void placePipes(){

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2)); //range is between 1/4 and 3/4 of pipe height
        int openingSpace = boardheight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);


    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){

        //background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardheight, null);

        //bird
        if (gravity < velocityY) {
            BufferedImage rotatedBirdImg = rotateImage(bird.img, 45);
            g.drawImage(rotatedBirdImg, bird.x, bird.y, bird.width, bird.height, null);
        } else if (gravity > velocityY) {
            BufferedImage rotatedBirdImg = rotateImage(bird.img, -45);
            g.drawImage(rotatedBirdImg, bird.x, bird.y, bird.width, bird.height, null);


        } else {
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null );
        }





        //pipes
        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver){
            g.drawString("Game Over: " + String. valueOf((int) score), 10, 35);
        }
        else{
            g.drawString(String.valueOf((int) score), 10, 35     );
        }
    }

    public void move(){
        //bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y,0);

        //pipes
        for (int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0.5 because theres two pipes
            }

            if (collision(bird, pipe)){
                gameOver = true;
            }
        }

        if (bird.y > boardheight){
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b){
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public BufferedImage rotateImage(Image img, double angle) {
        BufferedImage bufferedImage = toBufferedImage(img);
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin) + 3;
        int newHeight = (int) Math.floor(h * cos + w * sin) + 3;
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, bufferedImage.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);
        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();
        return rotated;
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    public Image getScaledImage(Image srcImg, int w, int h) {
        return srcImg.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }


    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
        if (gameOver){
            placePipesTimer.stop();
            gameLoop.stop();
        }


    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE){
            velocityY = -9;
            if (gameOver){
                //restart the game by resetting the conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
