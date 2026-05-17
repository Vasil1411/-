package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ServicesPanel extends JPanel {

    private JTextField txtServiceName, txtPrice;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private JTable tblServices;
    private int selectedRowIndex = -1; 

    public ServicesPanel() {
        // 1. ИНИЦИАЛИЗАЦИЯ
        txtServiceName = new JTextField(20);
        txtPrice = new JTextField(20);

        btnAdd = new JButton("Добави");
        btnUpdate = new JButton("Обнови");
        btnDelete = new JButton("Изтрий");
        btnClear = new JButton("Изчисти");

        tblServices = new JTable();

        // 2. СТИЛИЗАЦИЯ
        btnAdd.setBackground(new Color(144, 238, 144));
        btnUpdate.setBackground(new Color(173, 216, 230));
        btnDelete.setBackground(new Color(255, 182, 193));

        JButton[] allButtons = {btnAdd, btnUpdate, btnDelete, btnClear};
        for (JButton btn : allButtons) {
            btn.setFocusPainted(false);
        }

        // 3. LAYOUT
        setLayout(new BorderLayout(10, 10));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0; pnlInput.add(new JLabel("Име на услугата:"), gbc);
        gbc.gridy = 1; pnlInput.add(txtServiceName, gbc);
        gbc.gridy = 2; pnlInput.add(new JLabel("Цена (лв.):"), gbc);
        gbc.gridy = 3; pnlInput.add(txtPrice, gbc);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnClear);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlInput, BorderLayout.CENTER);
        pnlNorth.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);
        add(new JScrollPane(tblServices), BorderLayout.CENTER);

        // 4. СЪБИТИЯ (LISTENERS)
        tblServices.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRowIndex = tblServices.getSelectedRow();
                if (selectedRowIndex != -1) {
                    // Използваме модела, защото първата колона (ID) е скрита в JTable
                    txtServiceName.setText(tblServices.getModel().getValueAt(selectedRowIndex, 1).toString());
                    txtPrice.setText(tblServices.getModel().getValueAt(selectedRowIndex, 2).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> addService());
        btnUpdate.addActionListener(e -> updateService());
        btnDelete.addActionListener(e -> deleteService());
        btnClear.addActionListener(e -> clearFields());

        refreshTable();
    }

    // ==========================================
    // ЛОГИКА ЗА БАЗАТА ДАННИ
    // ==========================================

    public void refreshTable() {
        // Извличаме и id за нуждите на обновяването и триенето
        String sql = "SELECT id, service_name AS [Услуга], price AS [Цена (лв)] FROM services";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            tblServices.setModel(new MyModel(rs));
            
            // Скриваме колоната с ID от потребителя
            if (tblServices.getColumnCount() > 0) {
                tblServices.removeColumn(tblServices.getColumnModel().getColumn(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addService() {
        String name = txtServiceName.getText().trim();
        String price = txtPrice.getText().trim();

        if (name.isEmpty() || price.isEmpty()) return;

        String sql = "INSERT INTO services (service_name, price) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, Double.parseDouble(price));
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateService() {
        if (selectedRowIndex == -1) return;

      
        int id = Integer.parseInt(tblServices.getModel().getValueAt(selectedRowIndex, 0).toString());
        
        String newName = txtServiceName.getText().trim();
        String newPrice = txtPrice.getText().trim();

        if (newName.isEmpty() || newPrice.isEmpty()) return;

        // UPDATE по уникално ID
        String sql = "UPDATE services SET service_name = ?, price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setDouble(2, Double.parseDouble(newPrice));
            pstmt.setInt(3, id);
            
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteService() {
        if (selectedRowIndex == -1) return;

       
        int id = Integer.parseInt(tblServices.getModel().getValueAt(selectedRowIndex, 0).toString());

        String sql = "DELETE FROM services WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearFields() {
        txtServiceName.setText("");
        txtPrice.setText("");
        selectedRowIndex = -1;
        refreshTable();
    }
}