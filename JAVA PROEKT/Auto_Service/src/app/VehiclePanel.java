package app;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class VehiclePanel extends JPanel {

    private JTextField txtRegNumber, txtBrandModel;
    private JComboBox<String> cmbClients; 
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JTable tblVehicles;
    private int selectedRowIndex = -1;

    public VehiclePanel() {
        txtRegNumber = new JTextField(20);
        txtBrandModel = new JTextField(20);
        cmbClients = new JComboBox<>(); 

        btnAdd = new JButton("Добави");
        btnUpdate = new JButton("Обнови");
        btnDelete = new JButton("Изтрий");
        btnClear = new JButton("Изчисти");
        btnSearch = new JButton("Търси по Рег. №");

        tblVehicles = new JTable();

        // Стилизация (запазена)
        btnAdd.setBackground(new Color(144, 238, 144));
        btnUpdate.setBackground(new Color(173, 216, 230));
        btnDelete.setBackground(new Color(255, 182, 193));
        btnSearch.setBackground(new Color(255, 140, 0));
        btnSearch.setForeground(Color.WHITE);

        setLayout(new BorderLayout(10, 10));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0; pnlInput.add(new JLabel("Регистрационен номер:"), gbc);
        gbc.gridy = 1; pnlInput.add(txtRegNumber, gbc);
        gbc.gridy = 2; pnlInput.add(new JLabel("Марка/Модел:"), gbc);
        gbc.gridy = 3; pnlInput.add(txtBrandModel, gbc);
        gbc.gridy = 4; pnlInput.add(new JLabel("Избери Собственик:"), gbc);
        gbc.gridy = 5; pnlInput.add(cmbClients, gbc);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnSearch);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlInput, BorderLayout.CENTER);
        pnlNorth.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);
        add(new JScrollPane(tblVehicles), BorderLayout.CENTER);

        // СЛУШАТЕЛИ
        btnAdd.addActionListener(e -> addVehicle());
        btnClear.addActionListener(e -> clearFields());
        btnDelete.addActionListener(e -> deleteVehicle());
        btnUpdate.addActionListener(e -> updateVehicle());
        btnSearch.addActionListener(e -> searchVehicle());

        tblVehicles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRowIndex = tblVehicles.getSelectedRow();
                if (selectedRowIndex != -1) {
                   
                    txtRegNumber.setText(tblVehicles.getModel().getValueAt(selectedRowIndex, 1).toString());
                    txtBrandModel.setText(tblVehicles.getModel().getValueAt(selectedRowIndex, 2).toString());
                    
                    String owner = tblVehicles.getModel().getValueAt(selectedRowIndex, 3).toString();
                    selectClientInCombo(owner);
                }
            }
        });

        // Първоначално зареждане
        updateData(); 
    }

    // МЕТОД ЗА ОБНОВЯВАНЕ НА ВСИЧКО (извиквай го при смяна на табове)
    public void updateData() {
        loadClientsToCombo();
        refreshTable();
    }

    public void refreshTable() {
        String sql = "SELECT v.id, v.reg_number AS [Рег. Номер], " +
                     "v.brand_model AS [Марка/Модел], " +
                     "c.first_name + ' ' + c.last_name AS [Собственик] " +
                     "FROM vehicles v " +
                     "JOIN clients c ON v.client_id = c.id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            tblVehicles.setModel(new MyModel(rs));
            if (tblVehicles.getColumnCount() > 0) {
                tblVehicles.removeColumn(tblVehicles.getColumnModel().getColumn(0));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadClientsToCombo() {
       
        Object currentSelection = cmbClients.getSelectedItem();
        
        cmbClients.removeAllItems();
        cmbClients.addItem("-- Избери клиент --");
        
        String sql = "SELECT id, first_name, last_name FROM clients ORDER BY first_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cmbClients.addItem(rs.getInt("id") + ": " + rs.getString("first_name") + " " + rs.getString("last_name"));
            }
            
            // Опитваме се да върнем селекцията
            if (currentSelection != null) cmbClients.setSelectedItem(currentSelection);
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void selectClientInCombo(String fullName) {
        for (int i = 0; i < cmbClients.getItemCount(); i++) {
            if (cmbClients.getItemAt(i).contains(fullName)) {
                cmbClients.setSelectedIndex(i);
                return;
            }
        }
    }

    public void addVehicle() {
        String selectedClient = (String) cmbClients.getSelectedItem();
        if (txtRegNumber.getText().isEmpty() || selectedClient.equals("-- Избери клиент --") || txtBrandModel.getText().isEmpty() ) return;

        int clientId = Integer.parseInt(selectedClient.split(":")[0]);
        String sql = "INSERT INTO vehicles (reg_number, brand_model, client_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtRegNumber.getText().trim());
            pstmt.setString(2, txtBrandModel.getText().trim());
            pstmt.setInt(3, clientId);
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void updateVehicle() {
        if (selectedRowIndex == -1) return;
        int id = Integer.parseInt(tblVehicles.getModel().getValueAt(selectedRowIndex, 0).toString());
        
        String selectedClient = (String) cmbClients.getSelectedItem();
        if (selectedClient.equals("-- Избери клиент --")) return;
        int clientId = Integer.parseInt(selectedClient.split(":")[0]);

        String sql = "UPDATE vehicles SET reg_number=?, brand_model=?, client_id=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtRegNumber.getText().trim());
            pstmt.setString(2, txtBrandModel.getText().trim());
            pstmt.setInt(3, clientId);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void deleteVehicle() {
        if (selectedRowIndex == -1) return;
        int id = Integer.parseInt(tblVehicles.getModel().getValueAt(selectedRowIndex, 0).toString());
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void searchVehicle() {
        String searchReg = txtRegNumber.getText().trim();
        if (searchReg.isEmpty()) { refreshTable(); return; }

        String sql = "SELECT v.id, v.reg_number AS [Рег. Номер], v.brand_model AS [Марка/Модел], " +
                     "c.first_name + ' ' + c.last_name AS [Собственик] " +
                     "FROM vehicles v JOIN clients c ON v.client_id = c.id WHERE v.reg_number LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchReg + "%");
            ResultSet rs = pstmt.executeQuery();
            tblVehicles.setModel(new MyModel(rs));
            if (tblVehicles.getColumnCount() > 0) {
                tblVehicles.removeColumn(tblVehicles.getColumnModel().getColumn(0));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void clearFields() {
        txtRegNumber.setText("");
        txtBrandModel.setText("");
        cmbClients.setSelectedIndex(0);
        selectedRowIndex = -1;
    }
}