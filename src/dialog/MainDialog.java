package dialog;
import app.Application;
import config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDialog extends JDialog {
    public static MainDialog instance = null;
    private JList<String> logList;
    private DefaultListModel<String> logListModel;

    public MainDialog() {
        setTitle(Config.APP_NAME);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a list model for the log list
        logListModel = new DefaultListModel<>();
        logList = new JList<>(logListModel);

        // Create a menu
        JMenuBar menuBar = new JMenuBar();
        JMenu modelMenu = new JMenu("Model");
        JMenu trainMenu = new JMenu("Train");

        JMenuItem m2Item = new JMenuItem("M2");
        m2Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.change(2);
            }
        });

        JMenuItem m7Item = new JMenuItem("M7");
        m7Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.change(7);
            }
        });

        JMenuItem m15Item = new JMenuItem("M15");
        m15Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.change(15);
            }

        });

        modelMenu.add(m2Item);
        modelMenu.add(m7Item);
        modelMenu.add(m15Item);

        JMenuItem t2Item = new JMenuItem("M2");
        t2Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.train(2);
            }
        });

        JMenuItem t7Item = new JMenuItem("M7");
        t7Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.train(7);
            }
        });

        JMenuItem t15Item = new JMenuItem("M15");
        t15Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Application.instance.classifier.train(15);
            }
        });

        trainMenu.add(t2Item);
        trainMenu.add(t7Item);
        trainMenu.add(t15Item);

        menuBar.add(modelMenu);
        menuBar.add(trainMenu);

        // Create a scroll pane for the log list
        JScrollPane scrollPane = new JScrollPane(logList);

        // Create a start button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add your start button logic here
                System.out.println("Start button clicked");
                try {
                    Application.instance.Run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add components to the dialog
        add(menuBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setSize(720, 360);

        instance = this;
    }

    // Method to add log messages to the list
    public void addLogMessage(String message) {
        int count = logListModel.getSize();
        if (count > 300) {
            logListModel.removeRange(0, count - 300);
        }
        logListModel.addElement(message);
    }

}