import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class KeyboardTeacher extends JFrame implements ActionListener {
    private static JTextArea typingArea = new JTextArea();
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu fileMenu = new JMenu("File");
    private static JMenu helpMenu = new JMenu("Help");
    private static JLabel defaultHeader = new JLabel("Select your file..", JLabel.CENTER);
    private static JLabel infoLabel = new JLabel("", JLabel.CENTER);
    private static JButton tryAgainButton = new JButton("Try again");
    private static JButton retryButton = new JButton("Retry");
    private Timer timer = new Timer();
    private String currentLine;
    private static Scanner scanner;
    private Instant typingStart;
    private Instant typingEnd;

    class FileOpener {
        File openedFile = null;

        // Filechooser used in other JPanel to handle NullPointerException when choosing file.
        // When default JFileChooser used, the programme didn't wait for user to choose file and the other code
        // using the file was executed throwing NullPointerException
        FileOpener() {
            JPanel panel = new JPanel();
            panel.setLayout(null);
            JButton button = new JButton("Open File");

            button.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();

                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    openedFile = chooser.getSelectedFile();
                } else
                    openedFile = null;
            });
            panel.add(button);
            JFrame frame = new JFrame();
            frame.add(panel);
            frame.setVisible(false);
            button.doClick();

        }

        File getOpenedFile() {
            return openedFile;
        }

    }

    private KeyboardTeacher() {
        super("KeyboardTeacher");
        setSize(600, 500);
        typingArea.setBounds(20, 200, 540, 70);
        infoLabel.setBounds(20, 250, 500, 70);

        typingArea.setLineWrap(true);
        typingArea.setEditable(false);

        menuBar.setBounds(0, 0, 600, 30);
        defaultHeader.setBounds(20, 40, 500, 90);

        defaultHeader.setFont(new Font(defaultHeader.getName(), Font.BOLD, 19));

        JMenuItem openMenuItem = new JMenuItem("Open file..");
        openMenuItem.setActionCommand("fileOpen");
        openMenuItem.addActionListener(this);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(this);

        JMenuItem helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setActionCommand("help");
        helpMenuItem.addActionListener(this);

        tryAgainButton.setBounds(20, 350, 90, 60);
        tryAgainButton.addActionListener(this);
        tryAgainButton.setActionCommand("fileOpen");

        retryButton.setBounds(20, 350, 90, 60);
        retryButton.addActionListener(this);
        retryButton.setActionCommand("retry");


        helpMenu.add(helpMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        /*Making "ENTER" key event to check if the entered sentence matches
        the one given by programme, after pressing key textArea is cleared
        instead of making new line.
        */
        typingArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    try {
                        if (typingArea.getText().equals(currentLine)) {
                            if (scanner.hasNext()) {
                                currentLine = scanner.nextLine();
                                defaultHeader.setText("<html>" + currentLine + "<html>");
                            } else {
                                typingEnd = Instant.now();
                                infoLabel.setText("<html>Correctly passed all lines! Congratulations! Your typing time: " + Duration.between(typingStart, typingEnd).getSeconds() + " seconds</html>");
                                typingArea.setEditable(false);
                                typingArea.getCaret().setVisible(false);
                                tryAgainButton.setVisible(true);
                                add(tryAgainButton);
                                validate();
                                repaint();
                            }
                        } else {
                            typingArea.setEditable(false);
                            typingArea.getCaret().setVisible(false);
                            findMistake(currentLine, typingArea.getText());

                            tryAgainButton.setVisible(true);
                            add(tryAgainButton);
                            validate();
                            repaint();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == (KeyEvent.VK_ENTER)) {
                    try {
                        typingArea.setText(null);
                        typingArea.setCaretPosition(0);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        add(defaultHeader);
        add(infoLabel);
        add(menuBar);
        add(typingArea);

        setLayout(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /*Method finds mistake when user types wrong sentence, sets infoLabel telling
    user in which word he made typo and the index of first wrong letter*/
    private static void findMistake(String givenLine, String answer) {
        int i = 0;
        String[] givenLineWords = givenLine.split("\\s+");
        String[] answerWords = answer.split("\\s+");
        for (String s : answerWords) {
            if (!s.equals(givenLineWords[i])) {
                break;
            }
            i++;
        }
        for (int j = 0; j < answer.length(); j++) {
            if (givenLine.charAt(j) != answer.charAt(j)) {
                infoLabel.setText("<html>" + "Incorrect! Your answer differs at index " + j +
                        " in word <b>" + givenLineWords[i] + ", you typed </b>" + answerWords[i] + "</html>");
                break;
            }
        }

    }

    /*Implementing ActionListener Interface actionPerformed() method to set particular event to each
    interactive component of the GUI*/
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fileOpen")) {

            FileOpener fileOpener = new FileOpener();

            if (fileOpener.getOpenedFile() != null) {
                try {
                    FileReader fr = new FileReader(fileOpener.getOpenedFile(), StandardCharsets.UTF_8);
                    scanner = new Scanner(fr);
                }catch (IOException ex) {
                    ex.printStackTrace();
                }

                defaultHeader.setText("File selected successfully, prepare your fingers!");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        typingStart = Instant.now();
                        tryAgainButton.setVisible(false);
                        infoLabel.setText(null);
                        typingArea.setEditable(true);
                        typingArea.getCaret().setVisible(true);
                        currentLine = scanner.nextLine();
                        defaultHeader.setText("<html>" + currentLine + "<html>");
                    }
                }, 4500);
            }
        }
        if (e.getActionCommand().equals("exit")) {
            System.exit(0);
        }

        if (e.getActionCommand().equals("help")) {
            JFrame popup = new JFrame("Help");
            popup.setSize(400, 250);
// set help
            JLabel helpLabel = new JLabel("<html>\n" +
                    "<body>\n" +
                    "\t<ul>\t\n" +
                    "\t\t<li> To open a file click file -> open file.. and then choose the .txt file of your choice,\n" +
                    "\t\t</li>\n" +
                    "\t\t<li> Then you will be given few seconds before typing start,\n" +
                    "\t\t</li>\n" +
                    "\t\t<li> After few seconds first line of text will appear in the window,\n" +
                    "\t\t</li>\n" +
                    "\t\t<li>\n" +
                    "\t\t\t When you make a mistake, the test will be canceled and you can take it again, by pressing Try again button\n" +
                    "\t\t</li>\n" +
                    "\t\t<li>\n" +
                    "\t\t\t If you complete whole text, passing all lines correctly, you will be given your test time.\n" +
                    "\t\t</li>\n" +
                    "\t\t<li> You can start test again by choosing new file, or click 'try again' and select new, or the same file to improve your time result\n" +
                    "\t\t</li>\n" +
                    "\t</ul>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>", JLabel.CENTER);

            popup.add(helpLabel);
            popup.setResizable(false);
            popup.setLocationRelativeTo(null);
            popup.setVisible(true);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(KeyboardTeacher::new);

    }
}
