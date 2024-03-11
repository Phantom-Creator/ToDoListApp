import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList; // Import ArrayList class
import java.util.Collections; // Import Collections class
import java.util.Comparator; // Import Comparator class

public class ToDoListApp extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JTextField taskNameField, dateField, timeField;
    private JLabel currentTimeLabel, userInputLabel;

    public ToDoListApp() {
        setTitle("ToDo List App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Creating input fields
        taskNameField = new JTextField(20);
        dateField = new JTextField(10);
        timeField = new JTextField(10);

        // Creating buttons
        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");

        // Adding action listeners to buttons
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTask();
                sortTasks(); // Sort tasks after adding a new one
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });

        // Creating labels
        JLabel taskNameLabel = new JLabel("Task Name:");
        JLabel dateLabel = new JLabel("Date:");
        JLabel timeLabel = new JLabel("Time:");
        currentTimeLabel = new JLabel();
        userInputLabel = new JLabel("User Input:");

        // Creating table
        model = new DefaultTableModel();
        model.addColumn("Task Name");
        model.addColumn("Date");
        model.addColumn("Time");
        model.addColumn("Time Left");
        table = new JTable(model);

        // Center aligning the cells in the table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Setting layout
        setLayout(new BorderLayout());

        // Adding components to top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(userInputLabel);
        inputPanel.add(taskNameLabel);
        inputPanel.add(taskNameField);
        inputPanel.add(dateLabel);
        inputPanel.add(dateField);
        inputPanel.add(timeLabel);
        inputPanel.add(timeField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);

        // Adding current time display above the user input
        JPanel timePanel = new JPanel(new FlowLayout());
        timePanel.add(currentTimeLabel);

        topPanel.add(timePanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Adding table to frame
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Centering the frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.8);
        int height = (int) (screenSize.height * 0.8);
        setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        setSize(width, height);

        // Displaying current time and date
        updateTimeAndDate();

        // Update time and date every second
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                updateTimeAndDate();
            }
        }, 0, 1000);

        // Update table every minute
        java.util.Timer refreshTimer = new java.util.Timer();
        refreshTimer.schedule(new TimerTask() {
            public void run() {
                refreshTable();
            }
        }, 0, 60000);

    }

    private void updateTimeAndDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(now);
        currentTimeLabel.setText(formattedDate);
    }

    private void addTask() {
        String taskName = taskNameField.getText();
        String date = dateField.getText();
        String time = timeField.getText();

        // Adding task to table
        model.addRow(new Object[] { taskName, date, time, calculateTimeLeft(date, time) });
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String calculateTimeLeft(String date, String time) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date taskDateTime = dateFormat.parse(date + " " + time);
            long diff = taskDateTime.getTime() - System.currentTimeMillis();
            long minutes = diff / (60 * 1000);
            if (minutes <= 0) {
                return "Task Overdue"; // Task overdue, automatically tick the "Done" column
            }
            return minutes + " minutes";
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    private void refreshTable() {
        for (int i = 0; i < model.getRowCount(); i++) {
            String date = (String) model.getValueAt(i, 1);
            String time = (String) model.getValueAt(i, 2);
            String timeLeft = calculateTimeLeft(date, time);
            model.setValueAt(timeLeft, i, 3); // Refreshing time left for each task

            if (timeLeft.equals("Task Overdue")) {
                JOptionPane.showMessageDialog(this, "Task \"" + model.getValueAt(i, 0) + "\" is overdue!",
                        "Task Overdue", JOptionPane.WARNING_MESSAGE);
                model.removeRow(i);
            }
        }
    }

    private void sortTasks() {
        // Create a list to hold the tasks
        ArrayList<Object[]> tasks = new ArrayList<>();

        // Add all tasks to the list
        for (int i = 0; i < model.getRowCount(); i++) {
            tasks.add(new Object[] { model.getValueAt(i, 0), model.getValueAt(i, 1), model.getValueAt(i, 2),
                    model.getValueAt(i, 3) });
        }

        // Sort the tasks based on date and time
        Collections.sort(tasks, new Comparator<Object[]>() {
            public int compare(Object[] task1, Object[] task2) {
                String dateTime1 = task1[1] + " " + task1[2];
                String dateTime2 = task2[1] + " " + task2[2];
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date date1 = dateFormat.parse(dateTime1);
                    Date date2 = dateFormat.parse(dateTime2);
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        // Remove all rows from the table
        model.setRowCount(0);

        // Add sorted tasks back to the table
        for (Object[] task : tasks) {
            model.addRow(task);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToDoListApp().setVisible(true);
            }
        });
    }
}
