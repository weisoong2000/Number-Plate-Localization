
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
public class DrawBox {
    BufferedImage image;
    int [][] location;
    int count=0;
    public DrawBox(BufferedImage image,int[][] location){
        this.image= image;
        this.location = location;
    }
    public void draw(){
        Graphics2D g = (Graphics2D) image.getGraphics();
        Stroke stroke=new BasicStroke(5.0f);
        g.setStroke(stroke);
        g.setColor(Color.RED);
        count++;
        try {
            g.drawImage(image, 0, 0, null);
            g.drawLine(location[0][0], location[0][1], location[0][0], location[1][1]);
            g.drawLine(location[1][0], location[0][1], location[1][0], location[1][1]);
            g.drawLine(location[0][0], location[1][1], location[1][0], location[1][1]);
            g.drawLine(location[0][0], location[0][1], location[1][0], location[0][1]);
            //BufferedImage
            ImageIO.write(image,"jpg",new File("Path for test_image.jpg"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
