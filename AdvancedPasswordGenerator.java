import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Advanced Password Generator Application with GUI
 * This program generates secure random passwords with additional features like
 * saving passwords to a file and clipboard functionality.
 */
public class AdvancedPasswordGenerator extends JFrame {
    // Character pools for password generation
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>/?";
    
    // Using SecureRandom for better randomization
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // GUI Components
    private JSlider lengthSlider;
    private JLabel lengthLabel;
    private JCheckBox lowercaseCheckBox;
    private JCheckBox uppercaseCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox specialCharsCheckBox;
    private JTextField passwordField;
    private JLabel strengthLabel;
    private JButton generateButton;
    private JButton copyButton;
    private JButton saveButton;
    
    public AdvancedPasswordGenerator() {
        // Set up the JFrame
        super("Advanced Password Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Create panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Length selection
        JPanel lengthPanel = new JPanel(new BorderLayout(5, 5));
        lengthPanel.setBorder(BorderFactory.createTitledBorder("Password Length"));
        
        lengthSlider = new JSlider(JSlider.HORIZONTAL, 8, 50, 16);
        lengthSlider.setMajorTickSpacing(10);
        lengthSlider.setMinorTickSpacing(1);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);
        
        lengthLabel = new JLabel("Length: 16", JLabel.CENTER);
        lengthSlider.addChangeListener(e -> lengthLabel.setText("Length: " + lengthSlider.getValue()));
        
        lengthPanel.add(lengthSlider, BorderLayout.CENTER);
        lengthPanel.add(lengthLabel, BorderLayout.SOUTH);
        
        // Character types panel
        JPanel charsPanel = new JPanel();
        charsPanel.setLayout(new GridLayout(2, 2, 10, 10));
        charsPanel.setBorder(BorderFactory.createTitledBorder("Character Types"));
        
        lowercaseCheckBox = new JCheckBox("Lowercase (a-z)", true);
        uppercaseCheckBox = new JCheckBox("Uppercase (A-Z)", true);
        numbersCheckBox = new JCheckBox("Numbers (0-9)", true);
        specialCharsCheckBox = new JCheckBox("Special (!@#$%^&*)", true);
        
        charsPanel.add(lowercaseCheckBox);
        charsPanel.add(uppercaseCheckBox);
        charsPanel.add(numbersCheckBox);
        charsPanel.add(specialCharsCheckBox);
        
        // Password display panel
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 5));
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Generated Password"));
        
        passwordField = new JTextField();
        passwordField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        passwordField.setEditable(false);
        
        strengthLabel = new JLabel("Strength: Not Generated", JLabel.CENTER);
        
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(strengthLabel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        generateButton = new JButton("Generate Password");
        copyButton = new JButton("Copy to Clipboard");
        saveButton = new JButton("Save Password");
        
        // Initially disable buttons until password is generated
        copyButton.setEnabled(false);
        saveButton.setEnabled(false);
        
        buttonPanel.add(generateButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);
        
        // Add all panels to main panel
        mainPanel.add(lengthPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(charsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);
        
        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Add action listeners
        generateButton.addActionListener(e -> generatePassword());
        copyButton.addActionListener(e -> copyToClipboard());
        saveButton.addActionListener(e -> savePassword());
        
        // Add checkbox listener to ensure at least one is selected
        ItemListener checkBoxListener = e -> {
            if (!lowercaseCheckBox.isSelected() && 
                !uppercaseCheckBox.isSelected() && 
                !numbersCheckBox.isSelected() && 
                !specialCharsCheckBox.isSelected()) {
                // If all are unchecked, force the current one to stay checked
                ((JCheckBox)e.getSource()).setSelected(true);
                JOptionPane.showMessageDialog(this, 
                    "At least one character type must be selected.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
        };
        
        lowercaseCheckBox.addItemListener(checkBoxListener);
        uppercaseCheckBox.addItemListener(checkBoxListener);
        numbersCheckBox.addItemListener(checkBoxListener);
        specialCharsCheckBox.addItemListener(checkBoxListener);
    }
    
    /**
     * Generates a password based on the selected criteria
     */
    private void generatePassword() {
        int length = lengthSlider.getValue();
        boolean includeLowercase = lowercaseCheckBox.isSelected();
        boolean includeUppercase = uppercaseCheckBox.isSelected();
        boolean includeNumbers = numbersCheckBox.isSelected();
        boolean includeSpecial = specialCharsCheckBox.isSelected();
        
        String password = generatePasswordString(length, includeLowercase, includeUppercase, 
                                             includeNumbers, includeSpecial);
        
        passwordField.setText(password);
        assessPasswordStrength(password);
        
        // Enable buttons after password is generated
        copyButton.setEnabled(true);
        saveButton.setEnabled(true);
    }
    
    /**
     * Generates a password string based on specified criteria
     */
    private String generatePasswordString(int length, boolean includeLowercase, boolean includeUppercase,
                                       boolean includeNumbers, boolean includeSpecial) {
        // Build character pool based on user preferences
        StringBuilder charPool = new StringBuilder();
        if (includeLowercase) charPool.append(LOWERCASE_CHARS);
        if (includeUppercase) charPool.append(UPPERCASE_CHARS);
        if (includeNumbers) charPool.append(NUMBER_CHARS);
        if (includeSpecial) charPool.append(SPECIAL_CHARS);
        
        // Generate password
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each selected type is included
        if (includeLowercase) {
            password.append(LOWERCASE_CHARS.charAt(secureRandom.nextInt(LOWERCASE_CHARS.length())));
        }
        if (includeUppercase) {
            password.append(UPPERCASE_CHARS.charAt(secureRandom.nextInt(UPPERCASE_CHARS.length())));
        }
        if (includeNumbers) {
            password.append(NUMBER_CHARS.charAt(secureRandom.nextInt(NUMBER_CHARS.length())));
        }
        if (includeSpecial) {
            password.append(SPECIAL_CHARS.charAt(secureRandom.nextInt(SPECIAL_CHARS.length())));
        }
        
        // Fill the rest of the password with random characters from the pool
        for (int i = password.length(); i < length; i++) {
            int randomIndex = secureRandom.nextInt(charPool.length());
            password.append(charPool.charAt(randomIndex));
        }
        
        // Shuffle the password to ensure randomness
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int randomIndex = secureRandom.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[randomIndex];
            passwordArray[randomIndex] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Assesses and displays the strength of the generated password
     */
    private void assessPasswordStrength(String password) {
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        // Character variety check
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()-_=+\\[\\]{}|;:,.<>/?].*")) score++;
        
        String strength;
        Color strengthColor;
        
        if (score <= 3) {
            strength = "Weak";
            strengthColor = Color.RED;
        } else if (score <= 5) {
            strength = "Medium";
            strengthColor = Color.ORANGE;
        } else if (score <= 7) {
            strength = "Strong";
            strengthColor = Color.GREEN;
        } else {
            strength = "Very Strong";
            strengthColor = new Color(0, 128, 0); // Dark Green
        }
        
        strengthLabel.setText("Strength: " + strength);
        strengthLabel.setForeground(strengthColor);
    }
    
    /**
     * Copies the generated password to the system clipboard
     */
    private void copyToClipboard() {
        String password = passwordField.getText();
        if (password.isEmpty()) return;
        
        StringSelection selection = new StringSelection(password);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
        
        JOptionPane.showMessageDialog(this, 
            "Password copied to clipboard!", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Saves the generated password to a file
     */
    private void savePassword() {
        String password = passwordField.getText();
        if (password.isEmpty()) return;
        
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Password");
        
        // Show save dialog
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter writer = new FileWriter(fileChooser.getSelectedFile());
                
                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                
                // Write password with timestamp
                writer.write("Password generated on " + formattedDateTime + ":\n");
                writer.write(password);
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Password saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving password: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            AdvancedPasswordGenerator app = new AdvancedPasswordGenerator();
            app.setVisible(true);
        });
    }
} 