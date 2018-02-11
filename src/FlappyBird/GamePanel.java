package FlappyBird;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import Network.Network;
import OrganicNN.*;
import sun.jvm.hotspot.memory.OneContigSpaceCardGeneration;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private static final Logger LOGGER = Logger.getLogger( GamePanel.class.getName() );
    private static final int FPS = 60, WIDTH = 640, HEIGHT = 480, SPEED = 3, PIPE_W = 50;
    private static final int GAP_MAX = 220, GAP_MIN = 150, GAP_CENTER_VARIANCE = 80;
    private static final Color bg = new Color(0, 158, 158);
    private static final Font scoreFont = new Font("Comic Sans MS", Font.BOLD, 18);
    private static final Font pauseFont = new Font("Arial", Font.BOLD, 48);
    private static final Font helpFont = new Font("Arial", Font.BOLD, 18);
    private int time, score;
    private boolean paused, dead, usingAI;
    private Bird bird;
    private JFrame frame;
    private HashMap<Pipe[], int[]> pipes;
    private ArrayList<double[][]> gameDataSet;
    private Timer timer;
    private OrganicNN ONN;
    private Network ANN;
    private Robot AIRobot;
    // pipes format:
    // Pipe[0]=top, Pipe[1]=bottom
    // int[0]=gap, int[1]=gap y center

    public void start() {
        bird = new Bird(110, HEIGHT/2-70);
        pipes = new HashMap<>();
        gameDataSet = new ArrayList<>();
        time = 0;
        score = 0;
        paused = true;
        dead = false;

        // setup AI robot
        ONN = null;
        ANN = null;
        usingAI = false;
        try { AIRobot = new Robot(); }
        catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Could not create AI robot.");
            e.printStackTrace();
        }

        frame = new JFrame("Flappy Bird");
        frame.add(this);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(this);

        timer = new Timer(1000/FPS, this);
        timer.start();
    }
    public void stop() {
        timer.stop();
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(bg);
        g.fillRect(0,0,WIDTH, HEIGHT);

        bird.update(g);
        for (Pipe[] set : pipes.keySet()) {
            for (Pipe p : set) p.update(g);
        }

        g.setFont(scoreFont);
        g.setColor(Color.BLACK);
        g.drawString("Score: "+ score, 10, 30);

        if (usingAI) g.drawString("AI", 12, 55);

        if(paused) {
            g.setFont(pauseFont);
            g.setColor(new Color(0,0,0,170));
            g.drawString("PAUSED", WIDTH/2-100, HEIGHT/2-30);
            g.setFont(helpFont);
            g.drawString("press space to start", WIDTH/2-88, HEIGHT/2-5);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();

        if (paused) return;

        captureData();

        // Check if the bird is dead
        if (dead) {
            JOptionPane.showMessageDialog(frame, "You lose!\n"+"Your score was: "+score+".");
            bird.reset();
            pipes.clear();
            score = 0;
            paused = true;
            dead = false;
            return;
        }

        // create new pipes
        if (time % 90 == 0) {
            int gap = (int) (Math.random() * (GAP_MAX - GAP_MIN) + 1) + GAP_MIN;
            int yCenter = (int) (Math.random() * 2 * GAP_CENTER_VARIANCE + 1) + HEIGHT/2 - GAP_CENTER_VARIANCE;
            Pipe[] set = new Pipe[2];
            set[0] = new Pipe(SPEED, WIDTH, 0, PIPE_W, yCenter - gap/2);
            set[1] = new Pipe(SPEED, WIDTH, yCenter + gap/2, PIPE_W, HEIGHT - (yCenter + gap/2));
            int[] gapInfo = {gap, yCenter};
            pipes.put(set, gapInfo);
        }

        // check for collisions with walls
        if (!this.getBounds().contains(bird)) dead = true;
        bird.physics();

        ArrayList<Pipe[]> toRemove = new ArrayList<>();
        for (Pipe[] set : pipes.keySet()) {
            for (Pipe p : set) {

                // check for collisions with pipe
                if (bird.intersects(p)) dead = true;
                else p.physics();
            }
            // remove passed pipes and add to score
            int setX = set[0].x;
            if (setX >= bird.x && setX < bird.x + SPEED) score++;
            if (setX+PIPE_W <= 0) toRemove.add(set);
        }
        for (Pipe[] set : toRemove) pipes.remove(set);

        time++;
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_UP) bird.jump();
        else if(e.getKeyCode()==KeyEvent.VK_SPACE) paused = !paused;
        else if(e.getKeyCode()==KeyEvent.VK_ALT) {
            if (ONN != null || ANN != null) usingAI = !usingAI;
            else usingAI = false;
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    private void captureData() {

        if ((ONN != null || ANN != null) && !usingAI) return;

        double[][] data = new double[2][];
        data[0] = new double[5];
        data[1] = new double[1];

        if (bird.getVy() == -8 && gameDataSet.size()>1) {
            gameDataSet.get(gameDataSet.size()-1)[1][0] = 1;
        }

        int distance = Integer.MAX_VALUE, gap = 0, gapY = 0;;
        int nextDistance;
        for (Pipe[] set : pipes.keySet()) {
            nextDistance = set[0].x - bird.x;
            if (nextDistance < distance && nextDistance > 0) {
                distance = nextDistance;
                gap = pipes.get(set)[0];
                gapY = pipes.get(set)[1];
            }
        }

        data[0][0] = bird.y/100d ;
        data[0][1] = bird.getVy()/2d;
        data[0][2] = gap/100d;
        data[0][3] = gapY/100d;
        data[0][4] = distance/100d;
        data[1][0] = 0;

        if (usingAI) {
            if (ONN != null) {
                if (ONN.calculateOutput(data[0])[0] >= 0.5) {
                    AIRobot.keyPress(KeyEvent.VK_UP);
                }
            }
            else if (ANN != null) {
                if (ANN.calculateOutput(data[0])[0] >= 0.5) {
                    AIRobot.keyPress(KeyEvent.VK_UP);
                }
            }

        } else gameDataSet.add(data);
    }

    public ArrayList<double[][]> getDataSet(int trim) {

        // trims the given amount from the top and bottom of the data set
        if (gameDataSet.size() <= trim*2) return null;
        for (int i = 0; i < trim; i++) {
            gameDataSet.remove(0);
            gameDataSet.remove(gameDataSet.size()-1);
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < gameDataSet.size(); j++) {
                if (j % 2 == 0 && gameDataSet.get(j)[1][0] == 0) gameDataSet.remove(j);
            }
        }

        return gameDataSet;
    }

    public void loadONN(OrganicNN net) { ONN = net; }
    public void loadANN(Network net) { ANN = net; }
}
