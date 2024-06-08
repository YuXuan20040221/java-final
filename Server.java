import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JPanel {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private JTextField portField;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea logArea;
    private GameP1 gameP1;

    Server(Game game) {
        setLayout(new BorderLayout());

        JButton backButton = game.createRoundedButton("Home Page");
        backButton.addActionListener(e -> game.ShowStartPage());
        JButton clientButton = game.createRoundedButton("Client");
        clientButton.addActionListener(e -> game.ShowClient());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(backButton);
        buttonPanel.add(clientButton);

        JPanel controlPanel = new JPanel(new GridLayout(3, 2));
        controlPanel.add(new JLabel("Port: (1024~49151)"));
        portField = new JTextField("12345");
        controlPanel.add(portField);

        JButton startButton = game.createRoundedButton("Start Server");
        startButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText());
            try {
                log("Server started on port " + port);
                start(port, game);
            } catch (IOException ex) {
                log("Error starting server: " + ex.getMessage());
            }
        });
        controlPanel.add(startButton);

        JButton stopButton = game.createRoundedButton("Stop Server");
        stopButton.addActionListener(e -> {
            try {
                log("Server stopped");
                stop();
            } catch (IOException ex) {
                log("Error stopping server: " + ex.getMessage());
            }
        });
        controlPanel.add(stopButton);

        JButton startGameButton = game.createRoundedButton("Start Game");
        startGameButton.addActionListener(e -> {
            sendMessage("START_GAME");
        });
        controlPanel.add(startGameButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void start(int port, Game game) throws IOException {
        serverSocket = new ServerSocket(port);
        int assignedPort = serverSocket.getLocalPort();
        log("Server started on port " + assignedPort);

        // 获取并显示本地 IP 地址
        log("Local IP Addresses:");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        log("  " + inetAddress.getHostAddress());
                        System.out.println(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            log("Error retrieving IP addresses: " + e.getMessage());
        }

        log("Waiting for client to connect...");
        clientSocket = serverSocket.accept();
        log("Client connected!");

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    log("Received: " + message);
                    if ("START_GAME".equals(message)) {
                        // 只有在接收到 "START_GAME" 消息时才回复
                        out.println("Game is starting!");
                        log("Sent reply: Game is starting!");
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
                        log("Unknown message, no reply sent.");
                    }
                }
            } catch (IOException e) {
                log("Connection error: " + e.getMessage());
            }
        }).start();
    }

    public void stop() throws IOException {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (clientSocket != null)
            clientSocket.close();
        if (serverSocket != null)
            serverSocket.close();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            log("Sent message: " + message);
        } else {
            log("Error: Output stream is not available.");
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }
}