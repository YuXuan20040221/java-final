import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class StartPage extends JPanel {
    private final String startPageImagePath = "./startPage.jpg";
    private Image backgroundImage;
    private GameP1 gameP1;

    StartPage(Game game) {
        setLayout(new BorderLayout());

        try {
            // Load the background image for the information page
            backgroundImage = ImageIO.read(new File(startPageImagePath));
        } catch (IOException e) {
            e.printStackTrace(); // 輸出異常信息
            // 可以選擇設置一個默認的背景圖像或處理加載失敗的情況
        }

        JButton informationButton = game.createRoundedButton("Information");
        informationButton.addActionListener(e -> game.ShowInformationPage());

        JButton onePlayerButton = game.createRoundedButton("1 Player");
        onePlayerButton.addActionListener(e -> {
            if (gameP1 == null) {
                gameP1 = new GameP1(game);
                game.mainPanel.add(gameP1, "GameP1");
                game.ShowGameP1();
            }else{
                gameP1.restartGame();
                game.ShowGameP1();
            }
        });

        JButton twoPlayerButton = game.createRoundedButton("2 Player");
        twoPlayerButton.addActionListener(e -> game.ShowServer());

        JButton scoreButton = game.createRoundedButton("Show Score");
        scoreButton.addActionListener(e -> game.showScore());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(informationButton);
        buttonPanel.add(onePlayerButton);
        buttonPanel.add(twoPlayerButton);
        buttonPanel.add(scoreButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
