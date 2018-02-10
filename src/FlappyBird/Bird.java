package FlappyBird;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * +x -> right
 * -x -> left
 * +y -> down
 * -y -> up
 *
 * @author JAForewit
 */
public class Bird extends Rectangle {

    private static final Logger LOGGER = Logger.getLogger( Bird.class.getName() );
    private static final int WIDTH = 50, HEIGHT = 40;
    private int initialY;
    private float vy;
    private Image img;

    Bird(int x, int y) {
        initialY = y;
        setBounds(x,y,WIDTH,HEIGHT);

        try { img = ImageIO.read(new File("bird.png")); }
        catch(IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load bird image.");
            e.printStackTrace();
        }
    }

    void jump() { vy = -8; }

    void physics() {
        y+=vy;
        vy+=0.5f;
    }

    void update(Graphics g) {
        g.drawImage(img, x,y,WIDTH,HEIGHT, null);
    }

    void reset() {
        y = initialY;
        vy = 0;
    }

    float getVy() { return vy; }
}
