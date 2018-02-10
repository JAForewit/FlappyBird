package FlappyBird;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private static final int FPS = 60, WIDTH = 640, HEIGHT = 480, SPEED = 3, PIPE_W = 50;
    private static final int GAP_MAX = 220, GAP_MIN = 150, GAP_CENTER_VARIANCE = 80;
    private static final Color bg = new Color(0, 158, 158);
    private static final Font scoreFont = new Font("Comic Sans MS", Font.BOLD, 18);
    private static final Font pauseFont = new Font("Arial", Font.BOLD, 48);
    private static final Font helpFont = new Font("Arial", Font.BOLD, 18);
    private int time, score;
    private boolean paused, dead;
    private Bird bird;
    private JFrame frame;
    private HashMap<Pipe[], int[]> pipes;
    // pipes format:
    // Pipe[0]=top, Pipe[1]=bottom
    // int[0]=gap, int[0]=gap y center, int[3]=leftmost x position

    public void start() {
        bird = new Bird(110, HEIGHT/2-70);
        pipes = new HashMap<>();
        time = 0;
        score = 0;
        paused = true;
        dead = false;

        frame = new JFrame("Flappy Bird");
        frame.add(this);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(this);

        Timer t = new Timer(1000/FPS, this);
        t.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(bg);
        g.fillRect(0,0,WIDTH, HEIGHT);

        bird.update(g);
        for (Pipe[] set : pipes.keySet()) {
            for (Pipe p : set) p.update(g);
            pipes.get(set)[2] -= SPEED;
        }

        g.setFont(scoreFont);
        g.setColor(Color.BLACK);
        g.drawString("Score: "+ score, 10, 30);

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
            int[] gapInfo = {gap, yCenter, WIDTH};
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
            int setX = pipes.get(set)[2];
            if (setX >= bird.x && setX < bird.x + SPEED) score++;
            if (setX+PIPE_W <= 0) toRemove.add(set);
        }
        for (Pipe[] set : toRemove) pipes.remove(set);

        time++;
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_UP) { bird.jump(); }
        else if(e.getKeyCode()==KeyEvent.VK_SPACE) { paused = !paused; }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}
}
