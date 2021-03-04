package uilities;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class OutputWindow implements Runnable {

    private static OutputWindow self;
    private String title;
    private JFrame frame;
    private JScrollPane scrollPane;
    private JTextArea outputArea;
    private JPanel panel = new JPanel();

    public OutputWindow(String title) {
        this.title = title;
        panel.setLayout(new BorderLayout());
        this.outputArea = new JTextArea(50,50);
        this.scrollPane = new JScrollPane(outputArea);
        this.outputArea.setEditable(false);
        outputArea.setLineWrap(true); //Wrap the text when it reaches the end of the TextArea.
        outputArea.setWrapStyleWord(true); //Wrap at every word rather than every letter.
        DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(scrollPane, BorderLayout.CENTER);
        self = this;
    }

    public static OutputWindow getLog(){
        return self;
    }

    public void run() {
        frame = new JFrame(this.title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel outputPanel = new JPanel(new FlowLayout());
        outputPanel.add(panel);

        frame.add(outputPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public void println(String msg) {
        outputArea.append(msg + "\n");
    }

}