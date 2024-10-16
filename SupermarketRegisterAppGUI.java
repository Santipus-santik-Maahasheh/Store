package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class SupermarketRegisterAppGUI extends JFrame {
    private JTextField productIdField;
    private JTextField quantityField;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private List<Product> cart;

    // JDBC URL, username, and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/supermarket";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public SupermarketRegisterAppGUI() {
        cart = new ArrayList<>(); // Initialize the cart
        setTitle("Supermarket Register");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel("Enter Product ID:");
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Set label font
        inputPanel.add(label);

        productIdField = new JTextField(15);
        productIdField.setFont(new Font("Arial", Font.PLAIN, 14)); // Set text field font
        inputPanel.add(productIdField);

        JLabel quantityLabel = new JLabel("Enter Quantity:");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(quantityLabel);

        quantityField = new JTextField(5);
        quantityField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(quantityField);

        JButton searchButton = new JButton("Search");
        inputPanel.add(searchButton);

        add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Quantity"}, 0);
        productTable = new JTable(tableModel);
        customizeTable(productTable); // Customize the table appearance
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        
        JButton addButton = new JButton("Add to Cart");
        JButton continueButton = new JButton("Continue");
        
        buttonPanel.add(addButton);
        buttonPanel.add(continueButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listener for Search button
        searchButton.addActionListener(e -> searchProduct());

        // Action listener for Add to Cart button
        addButton.addActionListener(e -> addToCart());

        // Action listener for Continue button
        continueButton.addActionListener(e -> generateSalesToken());
    }

    private void customizeTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font for table cells
        table.setRowHeight(25); // Set row height
        table.setShowGrid(true); // Show grid lines
        table.setGridColor(Color.LIGHT_GRAY); // Set grid color

        // Set header properties
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16)); // Set font for header
        header.setBackground(Color.GRAY); // Set header background color
        header.setForeground(Color.WHITE); // Set header text color

        // Center align the text in the table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void searchProduct() {
        String productId = productIdField.getText();
        String query = "SELECT * FROM products WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(productId));
            ResultSet resultSet = statement.executeQuery();

            tableModel.setRowCount(0); // Clear previous results

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                tableModel.addRow(new Object[]{id, name, price, quantity});
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) productTable.getValueAt(selectedRow, 0);
            String name = (String) productTable.getValueAt(selectedRow, 1);
            double price = (double) productTable.getValueAt(selectedRow, 2);
            int availableQuantity = (int) productTable.getValueAt(selectedRow, 3);
            String quantityStr = quantityField.getText();

            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity > 0 && quantity <= availableQuantity) {
                    cart.add(new Product(id, name, price, quantity));
                    JOptionPane.showMessageDialog(this, name + " (x" + quantity + ") has been added to the cart.");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid quantity. Available: " + availableQuantity, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to add.");
        }
    }

    private void generateSalesToken() {
        // Generate an itemized sales token based on the selected products in the table
        int rowCount = productTable.getRowCount();
        if (rowCount > 0) {
            StringBuilder salesToken = new StringBuilder();
            
            // Header
            salesToken.append("Receipt\n");
            salesToken.append("Address: 1234 Lorem Ipsum, Dolor\n");
            salesToken.append("Tel: 123-456-7890\n");
            salesToken.append("-----------------------------------------\n");
            salesToken.append("Date: ").append(java.time.LocalDate.now()).append(" ")
                      .append(java.time.LocalTime.now()).append("\n\n");
            
            // Itemized list
            salesToken.append(String.format("%-20s %-10s %-10s %-10s\n", "Item", "Price", "Qty", "Total"));
            salesToken.append("-----------------------------------------\n");
    
            double subtotal = 0;
            for (int i = 0; i < rowCount; i++) {
                int id = (int) productTable.getValueAt(i, 0);
                String name = (String) productTable.getValueAt(i, 1);
                double price = (double) productTable.getValueAt(i, 2);
                int quantity = (int) productTable.getValueAt(i, 3);
                double total = price * quantity;
                subtotal += total;
                
                salesToken.append(String.format("%-20s $%-9.2f %-10d $%-9.2f\n", name, price, quantity, total));
            }
    
            // Calculate tax and total
            double taxRate = 0.10; // 10% GST
            double salesTax = subtotal * taxRate;
            double totalAmount = subtotal + salesTax;
    
            salesToken.append("-----------------------------------------\n");
            salesToken.append(String.format("Subtotal: $%.2f\n", subtotal));
            salesToken.append(String.format("Sales Tax (10%%): $%.2f\n", salesTax));
            salesToken.append("-----------------------------------------\n");
            salesToken.append(String.format("Total Amount: $%.2f\n", totalAmount));
            salesToken.append("-----------------------------------------\n");
            salesToken.append("Thank you for shopping with us!");
    
            JOptionPane.showMessageDialog(this, salesToken.toString(), "Sales Token", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No products to generate a sales token.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateInventory() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            for (Product product : cart) {
                String updateQuery = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setInt(1, product.getQuantity());
                    statement.setInt(2, product.getId());
                    statement.executeUpdate();
                }
            }
            cart.clear(); // Clear the cart after updating inventory
            JOptionPane.showMessageDialog(this, "Inventory updated successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating inventory: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SupermarketRegisterAppGUI app = new SupermarketRegisterAppGUI();
            app.setVisible(true);
        });
    }

    // Product class to represent products in the cart
    static class Product {
        private int id;
        private String name;
        private double price;
        private int quantity;

        public Product(int id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}

