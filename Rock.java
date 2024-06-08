import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Rock extends Rectangle {
    private int weight;
    private int money;
    private Image image;

    public Rock(int x, int y, int width, int height, int weight, int money, String imgSrc) {
        super(x, y, width, height);
        this.weight = weight;
        this.money = money;
        try {
            this.image = ImageIO.read(new File(imgSrc));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSpeed() {
        return 60/weight;
    }

    public int getMoney() {
        return money;
    }

    public Image getImage() {
        return image;
    }
}
