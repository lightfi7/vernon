package app;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDialog extends JDialog {
    public static MainDialog instance = null;
    private JList<String> logList;
    private DefaultListModel<String> logListModel;

    public MainDialog() {
        setTitle("Vernon");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a list model for the log list
        logListModel = new DefaultListModel<>();
        logList = new JList<>(logListModel);

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
        if(count>300){
            logListModel.removeRange(0, count-300);
        }
        logListModel.addElement(message);
    }

}