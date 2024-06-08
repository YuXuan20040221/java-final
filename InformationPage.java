import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class InformationPage extends JPanel {
    private final String informationPageImagePath = "./Information.jpg";
    private Image backgroundImage;

    InformationPage(Game game) {
        setLayout(new BorderLayout());

        try {
            backgroundImage = ImageIO.read(new File(informationPageImagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel infoLabel = new JLabel(
                "<html><div style='text-align: center; color: #FFFFFF; font-size: 25pt; font-weight: bold;'>"
                        + "遊戲說明：<br>這是一個挖金礦遊戲<br>玩家需要在規定時間內挖到盡可能多的金礦<br>"
                        + "1 Player 模式：單人遊戲。<br>2 Player 模式：雙人遊戲。<br>請輸入IP位址以連接多人模式。"
                        + "</div></html>",
                SwingConstants.CENTER);

        JButton backButton = game.createRoundedButton("Back");
        backButton.addActionListener(e -> game.ShowStartPage());

        add(infoLabel, BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
