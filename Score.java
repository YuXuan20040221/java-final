import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class Score extends JPanel {
    private List<PlayerScore> scores;
    private JTextArea scoreArea;

    Score(Game game) {
        scores = new ArrayList<>();
        setLayout(new BorderLayout());

        // 创建返回按钮
        JButton backButton = game.createRoundedButton("Home Page");
        backButton.addActionListener(e -> game.ShowStartPage());
        add(backButton, BorderLayout.SOUTH);

        // 显示排行榜区域
        scoreArea = new JTextArea();
        scoreArea.setEditable(false);
        add(new JScrollPane(scoreArea), BorderLayout.CENTER);

        // 加载并显示分数
        loadScores();
        displayScores();
    }

    public void refreshScores() {
        scores.clear();
        loadScores();
        displayScores();
    }

    private void loadScores() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("score.txt"));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.add(new PlayerScore(name, score));
                }
            }
            scores.sort((s1, s2) -> Integer.compare(s2.score, s1.score));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayScores() {
        StringBuilder scoreText = new StringBuilder();
        scoreText.append("Top 10 Scores:\n\n");
        for (int i = 0; i < Math.min(10, scores.size()); i++) {
            PlayerScore ps = scores.get(i);
            scoreText.append((i + 1) + ". " + ps.name + " - " + ps.score + "\n");
        }
        scoreArea.setText(scoreText.toString());
    }

    private static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlayerScore that = (PlayerScore) o;
            return score == that.score && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, score);
        }
    }
}