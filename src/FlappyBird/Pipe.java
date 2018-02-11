package FlappyBird;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pipe extends Rectangle {
    private static final Logger LOGGER = Logger.getLogger( Pipe.class.getName() );
    private int vx;
    private Image img;

    Pipe(int vx, int x, int y, int w, int h) {
        this.vx = vx;
        setBounds(x,y,w,h);

        try { img = ImageIO.read(new File("../FlappyBird/pipe.png")); }
        catch(IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load pipe image.");
            e.printStackTrace();
        }
    }

    void update(Graphics g) {
        // TODO: draw graphics
        g.drawImage(img, x,y,width,height, null);

    }

    void physics() { x -= vx; }
}
