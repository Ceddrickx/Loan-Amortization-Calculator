import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;

// ---------- Calculation Methods ----------
class LoanCalculatorMethod {

    // Calculates the monthly payment for a loan
    public static double calculateMonthlyPayment(double loanAmount, double annualRate, int months) {

        // Handle zero-interest loans
        if (annualRate == 0) {
            double payment = loanAmount / months;
            return roundToTwoDecimals(payment);
        }

        double monthlyRate = (annualRate / 100) / 12;

        // Standard amortized loan formula
        double monthlyPayment = (loanAmount * monthlyRate) /
                (1 - Math.pow(1 + monthlyRate, -months));

        return roundToTwoDecimals(monthlyPayment);
    }

    // Calculates the total payment over the entire loan term
    public static double calculateTotalPayment(double monthlyPayment, int months) {
        double totalPayment = monthlyPayment * months;
        return roundToTwoDecimals(totalPayment);
    }

    // Utility method to round values to 2 decimal places
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

// ---------- Results Display Window ----------
class LoanCalculatorResultWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "Calculation Results";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 550;

    private static final Color THEME_COLOR = new Color(70, 130, 180);
    private static final Font HEADER_FONT = new Font("Arial Black", Font.PLAIN, 20);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 14);

    private static final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    public LoanCalculatorResultWindow(double principal, double annualRate, int months,
            double monthlyPayment, double totalPayment) {

        // Window Setup
        setTitle(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(THEME_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel headerLabel = new JLabel("CALCULATION SUMMARY");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Summary + Table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Summary Data Section
        JPanel summaryDataPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        summaryDataPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        addSummaryRow(summaryDataPanel, "Loan Amount:", principal);
        addSummaryRow(summaryDataPanel, "Interest Rate:", annualRate, "%");
        addSummaryRow(summaryDataPanel, "Term:", months, " Months");
        addSummaryRow(summaryDataPanel, "Monthly Payment:", monthlyPayment);
        addSummaryRow(summaryDataPanel, "Total Payment:", totalPayment);

        centerPanel.add(summaryDataPanel);

        // Amortization Table
        String[] columnNames = { "Month", "Principal", "Interest", "Balance" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);

        // Calculate amortization schedule
        double balance = principal;
        double monthlyRate = annualRate / 100 / 12;

        for (int i = 1; i <= months; i++) {
            double interestForMonth = balance * monthlyRate;
            double principalForMonth = monthlyPayment - interestForMonth;
            balance = balance - principalForMonth;

            if (balance < 0 || i == months)
                balance = 0;

            tableModel.addRow(new Object[] {
                    i,
                    moneyFormat.format(principalForMonth),
                    moneyFormat.format(interestForMonth),
                    moneyFormat.format(balance)
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Amortization Schedule"));
        centerPanel.add(scrollPane);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Section: Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton closeButton = new JButton("Close Results");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addSummaryRow(JPanel panel, String text, double value) {
        addSummaryRow(panel, text, value, "");
    }

    private void addSummaryRow(JPanel panel, String text, double value, String suffix) {
        JLabel label = new JLabel(text);
        label.setFont(CONTENT_FONT);

        String formatted;
        if (suffix.equals("%")) {
            formatted = value + "%";
        } else if (suffix.contains("Months")) {
            formatted = (int) value + suffix;
        } else {
            formatted = "PHP " + moneyFormat.format(value);
        }

        JLabel valLabel = new JLabel(formatted);
        valLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valLabel.setForeground(THEME_COLOR);

        panel.add(label);
        panel.add(valLabel);
    }
}

// ---------- Event Handler ----------
class LoanCalculatorEventHandler implements ActionListener {

    private final JTextField principalField;
    private final JTextField interestField;
    private final JTextField termField;
    private final JButton calculateButton;
    private final JButton clearButton;

    public LoanCalculatorEventHandler(
            JTextField principalField,
            JTextField interestField,
            JTextField termField,
            JButton calculateButton,
            JButton clearButton) {

        this.principalField = principalField;
        this.interestField = interestField;
        this.termField = termField;
        this.calculateButton = calculateButton;
        this.clearButton = clearButton;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == calculateButton) {
            calculateLoan();
        }

        if (e.getSource() == clearButton) {
            clearFields();
        }
    }

    private void calculateLoan() {
        try {
            // Get user input from UI fields
            double principal = Double.parseDouble(principalField.getText());
            double interest = Double.parseDouble(interestField.getText());
            int months = Integer.parseInt(termField.getText());

            // Validate inputs
            if (principal <= 0 || interest < 0 || months <= 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter positive values for all fields.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call computation methods
            double monthlyPayment = LoanCalculatorMethod.calculateMonthlyPayment(
                    principal, interest, months);

            double totalPayment = LoanCalculatorMethod.calculateTotalPayment(
                    monthlyPayment, months);

            // Open the result window with detailed amortization schedule
            new LoanCalculatorResultWindow(principal, interest, months,
                    monthlyPayment, totalPayment);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter valid numeric values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        principalField.setText("");
        interestField.setText("");
        termField.setText("");
    }
}

// ---------- Main UI ----------
public class Loan_Calculator extends JFrame {
    private JTextField principalField, interestField, termField;
    private JButton calculateButton, clearButton;

    public Loan_Calculator() {
        setTitle("Loan Calculator System");
        setSize(450, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Content pane
        Container c = getContentPane();
        c.setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel headerLabel = new JLabel("LOAN CALCULATOR");
        headerLabel.setFont(new Font("Arial Black", Font.PLAIN, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        c.add(headerPanel, BorderLayout.NORTH);

        // Create panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Principal Amount
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Principal Amount (PHP):"), gbc);

        gbc.gridx = 1;
        principalField = new JTextField(13);
        panel.add(principalField, gbc);

        // Annual Interest Rate
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Annual Interest Rate (%):"), gbc);

        gbc.gridx = 1;
        interestField = new JTextField(13);
        panel.add(interestField, gbc);

        // Loan Term
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Loan Term (Months):"), gbc);

        gbc.gridx = 1;
        termField = new JTextField(13);
        panel.add(termField, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        calculateButton = new JButton("Calculate");
        clearButton = new JButton("Clear");

        buttonPanel.add(calculateButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Add panel to frame
        c.add(panel, BorderLayout.CENTER);

        // Connect event handler to buttons
        LoanCalculatorEventHandler handler = new LoanCalculatorEventHandler(
                principalField, interestField, termField,
                calculateButton, clearButton);

        calculateButton.addActionListener(handler);
        clearButton.addActionListener(handler);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Loan_Calculator());
    }
}
