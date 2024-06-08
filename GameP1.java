import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

class GameP1 extends JPanel {
    private Timer timer;
    private Timer countdownTimer; // 倒計時器
    private int countdown = 50; // 倒計時初始值50秒

    private int hookX = 400;
    private int hookY = 100;
    private int hookLength = 50;
    private double hookAngle = 20; // 使用double以获得更平滑的角度变化
    private boolean hookMovingDown = false;
    private boolean hookMovingUp = false;
    private int hookSpeed = 10;
    private double hookAngleSpeed = 1.0; // 将角度变化速度设为较小值以降低摆动速度
    private boolean swingRight = true; // 添加控制方向的变量

    private boolean isHooked = false;

    private Image hookImage;
    private Image playerImage; // 添加图像变量
    private Image scaledPlayerImage;

    private JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 修改为FlowLayout
    private List<Rock> GoldRects = new ArrayList<>();
    private List<Rock> stoneRects = new ArrayList<>();
    private List<Rock> DiamondRects = new ArrayList<>();

    private Game game; // 保存Game实例的引用

    private int score = 0;
    private JLabel scoreLabel = new JLabel("Score: " + score);
    private JLabel countdownLabel = new JLabel("Time: " + countdown); // 倒计时标签

    GameP1(Game game) {
        this.game = game; // 初始化Game实例

        // 背景設定
        setLayout(new BorderLayout());
        setBackground(new Color(128, 85, 0));

        // 回首頁
        JButton backButton = game.createRoundedButton("Home Page");
        backButton.addActionListener(e -> {
            game.ShowStartPage();
            restartGame();
        });
        add(backButton, BorderLayout.SOUTH);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600)); // 假设JPanel的大小为800x600
        add(layeredPane, BorderLayout.CENTER);

        // 设置顶部面板背景颜色和大小
        bar.setBackground(new Color(255, 194, 102));
        bar.setBounds(0, 0, 800, 100);
        layeredPane.add(bar, JLayeredPane.DEFAULT_LAYER);

        // 设置字体和颜色
        Font labelFont = new Font("Serif", Font.BOLD, 30);
        countdownLabel.setFont(labelFont);
        countdownLabel.setForeground(Color.BLACK);
        scoreLabel.setFont(labelFont);
        scoreLabel.setForeground(Color.BLACK);

        // 添加标签到bar
        bar.add(countdownLabel);
        bar.add(Box.createHorizontalStrut(10)); // 添加空格
        bar.add(scoreLabel);

        // 加载图像
        try {
            playerImage = ImageIO.read(new File("./player.png"));
            // 缩小图像
            scaledPlayerImage = playerImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }

        // 创建包含图像的面板并添加到 layeredPane 中
        ImagePanel imagePanel = new ImagePanel(scaledPlayerImage);
        imagePanel.setBounds(375, 52, 50, 50); // 图像位置和大小
        layeredPane.add(imagePanel, JLayeredPane.PALETTE_LAYER); // 将图像面板添加到较高的层次

        // 遊戲標題
        game.setTitle("Gold Miner Game");

        loadRandomMap();

        // 设置定时器，每隔20毫秒更新游戏逻辑
        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
            }
        });
        timer.start();
        setFocusable(true);
        requestFocusInWindow();

        // 设置倒计时定时器，每秒更新一次
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                countdownLabel.setText("Time: " + countdown);
                if (countdown <= 0) {
                    countdownTimer.stop();
                    timer.stop();
                    showGameOverDialog();
                }
            }
        });
        countdownTimer.start();

        // 鍵盤監聽器
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && !hookMovingDown && !hookMovingUp) {
                    hookMovingDown = true;
                }
            }
        });

        // 請求焦點確保能處理鍵盤
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                requestFocusInWindow();
            }
        });

        // 加载钩子的图像
        try {
            hookImage = ImageIO.read(new File("./hook.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制钩子
        g.setColor(Color.WHITE);
        int endX = hookX + (int) (hookLength * Math.cos(Math.toRadians(hookAngle)));
        int endY = hookY + (int) (hookLength * Math.sin(Math.toRadians(hookAngle)));
        g.drawLine(hookX, hookY, endX, endY);

        // 绘制钩子图像
        if (hookImage != null) {
            int imgWidth = hookImage.getWidth(this);
            int imgHeight = hookImage.getHeight(this);
            g.drawImage(hookImage, endX - imgWidth / 2, endY - imgHeight / 2, this);
        }

        // 绘制金矿
        for (Rock rect : GoldRects) {
            g.drawImage(rect.getImage(), rect.x, rect.y, rect.width, rect.height, this);
        }

        for (Rock rect : stoneRects) {
            g.drawImage(rect.getImage(), rect.x, rect.y, rect.width, rect.height, this);
        }

        for (Rock rect : DiamondRects) {
            g.drawImage(rect.getImage(), rect.x, rect.y, rect.width, rect.height, this);
        }
    }

    // 游戏逻辑更新
    public void updateGame() {
        int endX = hookX + (int) (hookLength * Math.cos(Math.toRadians(hookAngle)));
        int endY = hookY + (int) (hookLength * Math.sin(Math.toRadians(hookAngle)));

        if (hookMovingDown) {
            hookLength += hookSpeed;

            // 检查是否抓取到金矿或石块
            for (Rock goldRect : GoldRects) {
                if (goldRect.contains(endX, endY)) {
                    hookSpeed=goldRect.getSpeed();
                    hookMovingDown = false;
                    hookMovingUp = true;
                    isHooked = true;
                    GoldRects.remove(goldRect);
                    goldRect.setLocation(endX - goldRect.width / 2, endY - goldRect.height / 2);
                    score += goldRect.getMoney();
                    scoreLabel.setText("Score: " + score);
                    bar.add(scoreLabel);
                    break;
                }
            }

            for (Rock stoneRect : stoneRects) {
                if (stoneRect.contains(endX, endY)) {
                    hookSpeed=stoneRect.getSpeed();
                    hookMovingDown = false;
                    hookMovingUp = true;
                    isHooked = true;
                    stoneRects.remove(stoneRect);
                    stoneRect.setLocation(endX - stoneRect.width / 2, endY - stoneRect.height / 2);
                    score += stoneRect.getMoney();
                    scoreLabel.setText("Score: " + score);
                    bar.add(scoreLabel);
                    break;
                }
            }

            for (Rock diamond : DiamondRects) {
                if (diamond.contains(endX, endY)) {
                    hookSpeed=diamond.getSpeed();
                    hookMovingDown = false;
                    hookMovingUp = true;
                    isHooked = true;
                    DiamondRects.remove(diamond);
                    diamond.setLocation(endX - diamond.width / 2, endY - diamond.height / 2);
                    score += diamond.getMoney();
                    scoreLabel.setText("Score: " + score);
                    bar.add(scoreLabel);
                    break;
                }
            }

            // 检查是否到达边界
            if (endX <= 0 || endX >= getWidth() || endY >= getHeight()) {
                hookSpeed=10;
                hookMovingDown = false;
                hookMovingUp = true;
            }
        } else if (hookMovingUp) {
            hookLength -= hookSpeed;

            if (hookLength <= 50) {
                hookMovingUp = false;
                isHooked = false;
                hookSpeed=10;
            }
        } else {
            // 更新擺動角度
            if (swingRight) {
                hookAngle += hookAngleSpeed;
                if (hookAngle >= 160) {
                    swingRight = false;
                }
            } else {
                hookAngle -= hookAngleSpeed;
                if (hookAngle <= 20) {
                    swingRight = true;
                }
            }
        }

        // 重新畫圖
        repaint();
    }

    // Panel用于绘制图像
    class ImagePanel extends JPanel {
        private Image image;

        public ImagePanel(Image image) {
            this.image = image;
            setPreferredSize(new Dimension(50, 50));
            setOpaque(false); // 使面板透明
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void loadRandomMap() {
        String[] maps = { "./map/map1.txt", "./map/map2.txt","./map/map3.txt" ,"./map/map4.txt","./map/map5.txt"};
        Random rand = new Random();
        String selectedMap = maps[rand.nextInt(maps.length)];
    
        try {
            List<String> lines = Files.readAllLines(Paths.get(selectedMap));
            for (int y = 0; y < lines.size(); y++) {
                String[] values = lines.get(y).split(" ");
                for (int x = 0; x < values.length; x++) {
                    int value = Integer.parseInt(values[x]);
                    switch (value) {
                        case 1:
                            stoneRects.add(new Rock(x * 50, (y + 2) * 50, 50, 50, 30, 10, "./stone.png"));
                            break;
                        case 2:
                            DiamondRects.add(new Rock(x * 50, (y + 2) * 50, 50, 50, 45, 200, "./diamond.png"));
                            break;
                        case 3:
                            GoldRects.add(new Rock(x * 50, (y + 2) * 50, 50, 50, 15, 50, "./gold.png"));
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartGame() {
        // 重新设置倒计时
        countdown = 51;
        // 清空金矿、石头和重石列表
        GoldRects.clear();
        stoneRects.clear();
        DiamondRects.clear();
        // 重新加载地图
        loadRandomMap();
        hookSpeed=10;
        bar.remove(scoreLabel);

        // 创建新的 scoreLabel 并添加到 bar 中
        score=0;
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 30));
        scoreLabel.setForeground(Color.BLACK);
        bar.add(scoreLabel);
        

        hookX = 400;
        hookY = 100;
        hookLength = 50;
        hookAngle = 20;
        hookMovingDown = false;
        hookMovingUp = false;
        swingRight = true;
        isHooked = false;

        // 重開
        countdownTimer.restart();
        timer.restart();
        // 重新繪圖
        repaint();
    }


 //////
   private void handleGameOver() {
        String playerName = JOptionPane.showInputDialog(this, "Enter your name:", "Game Over", JOptionPane.PLAIN_MESSAGE);
        if (playerName != null && !playerName.isEmpty()) {

            saveScore(playerName, score);
        } else {
            
            saveScore("未命名", score);
        }
    }

    // 保存分数到 score.txt 文件
    private void saveScore(String playerName, int score) {
        try {
            FileWriter writer = new FileWriter("score.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(playerName + "," + score + "\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // 結束
    private void showGameOverDialog() {
        handleGameOver();
        int option = JOptionPane.showOptionDialog(
                this,
                "Time's up!",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] { "Home Page", "Restart" },
                "Home Page");

        if (option == JOptionPane.YES_OPTION) {
            game.ShowStartPage();
            restartGame();
        } else if (option == JOptionPane.NO_OPTION) {
            restartGame();
        }
    }
}
