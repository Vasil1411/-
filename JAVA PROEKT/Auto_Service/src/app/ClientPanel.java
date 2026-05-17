package app;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class ClientPanel extends JPanel {

    // 1. КОМПОНЕНТИ
    private JTextField txtFirstName, txtLastName, txtPhone, txtEmail;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JTable tblClients;
    private int selectedRowIndex = -1; 

    public ClientPanel() {
        // 2. ИНИЦИАЛИЗАЦИЯ
        txtFirstName = new JTextField(25);
        txtLastName = new JTextField(25);
        txtPhone = new JTextField(25);
        txtEmail = new JTextField(25);

        btnAdd = new JButton("Добави");
        btnUpdate = new JButton("Обнови");
        btnDelete = new JButton("Изтрий");
        btnClear = new JButton("Изчисти");
        btnSearch = new JButton("Търсене по име");

        tblClients = new JTable();

        // Стилизация
        btnAdd.setBackground(new Color(144, 238, 144));
        btnUpdate.setBackground(new Color(173, 216, 230));
        btnDelete.setBackground(new Color(255, 182, 193));
        btnSearch.setBackground(new Color(255, 140, 0));
        btnSearch.setForeground(Color.WHITE);

        // 3. ПОДРЕДБА (LAYOUT)
        setLayout(new BorderLayout(10, 10));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0; pnlInput.add(new JLabel("Име:"), gbc);
        gbc.gridy = 1; pnlInput.add(txtFirstName, gbc);
        gbc.gridy = 2; pnlInput.add(new JLabel("Фамилия:"), gbc);
        gbc.gridy = 3; pnlInput.add(txtLastName, gbc);
        gbc.gridy = 4; pnlInput.add(new JLabel("Телефон:"), gbc);
        gbc.gridy = 5; pnlInput.add(txtPhone, gbc);
        gbc.gridy = 6; pnlInput.add(new JLabel("Имейл:"), gbc);
        gbc.gridy = 7; pnlInput.add(txtEmail, gbc);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnSearch);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlInput, BorderLayout.CENTER);
        pnlNorth.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);
        add(new JScrollPane(tblClients), BorderLayout.CENTER);

        // 4. СЛУШАТЕЛИ (EVENTS)
        btnAdd.addActionListener(e -> addClient());
        btnClear.addActionListener(e -> clearFields());
        btnDelete.addActionListener(e -> deleteClient());
        btnUpdate.addActionListener(e -> updateClient());
        btnSearch.addActionListener(e -> searchClient());

        tblClients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRowIndex = tblClients.getSelectedRow();
                if (selectedRowIndex != -1) {
                    txtFirstName.setText(tblClients.getModel().getValueAt(selectedRowIndex, 1).toString());
                    txtLastName.setText(tblClients.getModel().getValueAt(selectedRowIndex, 2).toString());
                    txtPhone.setText(tblClients.getModel().getValueAt(selectedRowIndex, 3).toString());
                    txtEmail.setText(tblClients.getModel().getValueAt(selectedRowIndex, 4).toString());
                }
            }
        });

        refreshTable();
    }

    public void refreshTable() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, first_name AS [Име], last_name AS [Фамилия], phone AS [Телефон], email AS [Имейл] FROM clients";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            tblClients.setModel(new MyModel(rs));
            
            if (tblClients.getColumnCount() > 0) {
                tblClients.removeColumn(tblClients.getColumnModel().getColumn(0));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addClient() {
    	
    	if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() || txtPhone.getText().isEmpty() || txtEmail.getText().isEmpty()) return; 
    	
        String sql = "INSERT INTO clients (first_name, last_name, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtFirstName.getText().trim());
            pstmt.setString(2, txtLastName.getText().trim());
            pstmt.setString(3, txtPhone.getText().trim());
            pstmt.setString(4, txtEmail.getText().trim());
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    public void updateClient() {
        if (selectedRowIndex == -1) return; 

        int id = Integer.parseInt(tblClients.getModel().getValueAt(selectedRowIndex, 0).toString());

        String sql = "UPDATE clients SET first_name=?, last_name=?, phone=?, email=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, txtFirstName.getText().trim());
            pstmt.setString(2, txtLastName.getText().trim());
            pstmt.setString(3, txtPhone.getText().trim());
            pstmt.setString(4, txtEmail.getText().trim());
            pstmt.setInt(5, id);

            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClient() {
        if (selectedRowIndex == -1) return; 
        
        int id = Integer.parseInt(tblClients.getModel().getValueAt(selectedRowIndex, 0).toString());
        
       
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void searchClient() {
        String name = txtFirstName.getText().trim();
        String sql = "SELECT id, first_name AS [Име], last_name AS [Фамилия], phone AS [Телефон], email AS [Имейл] " +
                     "FROM clients WHERE first_name LIKE ? ";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            tblClients.setModel(new MyModel(rs));
            
            if (tblClients.getColumnCount() > 0) {
                tblClients.removeColumn(tblClients.getColumnModel().getColumn(0));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        selectedRowIndex = -1;
    }
}