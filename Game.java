import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class Game extends JFrame {
    public JPanel mainPanel;
    private CardLayout cardLayout;
    public GameP1 gameP1;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }

    Game() {
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("挖金礦遊戲");
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new StartPage(this), "StartPage");
        mainPanel.add(new InformationPage(this), "InformationPage");
        //mainPanel.add(new GameP1(this), "GameP1");
        mainPanel.add(new Server(this), "Server");
        mainPanel.add(new Client(this), "Client");
        //mainPanel.add(new GameP2(this), "GameP2");
        mainPanel.add(new Score(this), "Score");

        add(mainPanel);
        setVisible(true);
    }

    public void showScore() {
        Score scorePanel = getScorePanel();
        if (scorePanel != null) {
            scorePanel.refreshScores();
        }
        cardLayout.show(mainPanel, "Score");
    }

    private Score getScorePanel() {
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof Score) {
                return (Score) comp;
            }
        }
        return null;
    }

    public void ShowStartPage() {
        cardLayout.show(mainPanel, "StartPage");
    }

    public void ShowInformationPage() {
        cardLayout.show(mainPanel, "InformationPage");
    }

    public void ShowGameP1() {
        cardLayout.show(mainPanel, "GameP1");
    }

    // public void ShowGameP2() {
    //     cardLayout.show(mainPanel, "GameP2");
    // }

    public void ShowClient() {
        cardLayout.show(mainPanel, "Client");
    }

    public void ShowServer() {
        cardLayout.show(mainPanel, "Server");
    }

    public JButton createRoundedButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(255, 215, 0)); // 金色
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                g.setColor(new Color(184, 134, 11)); // 深金色
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        return button;
    }
}