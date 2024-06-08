import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.ImageIO;

public class Client extends JPanel {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextField ipField;
    private JTextField portField;
    private JTextArea logArea;
    private GameP1 gameP1;

    Client(Game game) {
        setLayout(new BorderLayout());

        JButton backButton = game.createRoundedButton("Home Page");
        backButton.addActionListener(e -> game.ShowStartPage());
        JButton serverButton = game.createRoundedButton("Server");
        serverButton.addActionListener(e -> game.ShowServer());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(backButton);
        buttonPanel.add(serverButton);

        JPanel controlPanel = new JPanel(new GridLayout(3, 2));
        controlPanel.add(new JLabel("Server IP:"));
        ipField = new JTextField("127.0.0.1");
        controlPanel.add(ipField);

        controlPanel.add(new JLabel("Server Port:"));
        portField = new JTextField("12345");
        controlPanel.add(portField);

        JButton connectButton = game.createRoundedButton("Connect");
        connectButton.addActionListener(e -> {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            try {
                startConnection(ip, port, game);
                log("Connected to server at " + ip + ":" + port);
            } catch (IOException ex) {
                log("Error connecting to server: " + ex.getMessage());
            }
        });
        controlPanel.add(connectButton);

        JButton disconnectButton = game.createRoundedButton("Disconnect");
        disconnectButton.addActionListener(e -> {
            try {
                stopConnection();
                log("Disconnected from server");
            } catch (IOException ex) {
                log("Error disconnecting from server: " + ex.getMessage());
            }
        });
        controlPanel.add(disconnectButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void startConnection(String ip, int port, Game game) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("START_GAME")) {
                        log("Received: " + message);
                        sendMessage("START_GAME");
                        SwingUtilities.invokeLater(() -> {
                            if (gameP1 == null) {
                                gameP1 = new GameP1(game);
                                game.mainPanel.add(gameP1, "GameP1");
                                game.ShowGameP1();
                            } else {
                                gameP1.restartGame();
                                game.ShowGameP1();
                            }
                        });
                    } else {
                        log("Received: " + message);
                    }
                }
            } catch (IOException e) {
                log("Connection error: " + e.getMessage());
            }
        }).start();
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }
}
