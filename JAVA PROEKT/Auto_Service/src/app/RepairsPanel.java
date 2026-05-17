package app;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class RepairsPanel extends JPanel {

    // 1. КОМПОНЕНТИ
    private JTextField txtDate;
    private JComboBox<String> cmbVehicles, cmbServices, cmbStatus; 
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JTable tblRepairs;
    private int selectedRowIndex = -1;

    public RepairsPanel() {
        // 2. ИНИЦИАЛИЗАЦИЯ
        txtDate = new JTextField(20);
        
        cmbVehicles = new JComboBox<>();
        cmbServices = new JComboBox<>();
        cmbStatus = new JComboBox<>(new String[]{"Приет", "В ремонт", "Завършен"});

        btnAdd = new JButton("Добави");
        btnUpdate = new JButton("Обнови");
        btnDelete = new JButton("Изтрий");
        btnClear = new JButton("Изчисти");
        btnSearch = new JButton("Търси по Статус");

        tblRepairs = new JTable();

        // 3. СТИЛИЗАЦИЯ
        btnAdd.setBackground(new Color(144, 238, 144));
        btnUpdate.setBackground(new Color(173, 216, 230));
        btnDelete.setBackground(new Color(255, 182, 193));
        btnSearch.setBackground(new Color(255, 140, 0));
        btnSearch.setForeground(Color.WHITE);

        JButton[] allButtons = {btnAdd, btnUpdate, btnDelete, btnClear, btnSearch};
        for (JButton btn : allButtons) {
            btn.setFocusPainted(false);
        }

        // 4. ПОЗИЦИОНИРАНЕ (LAYOUT)
        setLayout(new BorderLayout(10, 10));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0; pnlInput.add(new JLabel("Избери Автомобил:"), gbc);
        gbc.gridy = 1; pnlInput.add(cmbVehicles, gbc);
        gbc.gridy = 2; pnlInput.add(new JLabel("Избери Услуга:"), gbc);
        gbc.gridy = 3; pnlInput.add(cmbServices, gbc);
        gbc.gridy = 4; pnlInput.add(new JLabel("Дата (ГГГГ-ММ-ДД):"), gbc);
        gbc.gridy = 5; pnlInput.add(txtDate, gbc);
        gbc.gridy = 6; pnlInput.add(new JLabel("Статус на ремонта:"), gbc);
        gbc.gridy = 7; pnlInput.add(cmbStatus, gbc);

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
        add(new JScrollPane(tblRepairs), BorderLayout.CENTER);

        // 5. СЪБИТИЯ (LISTENERS)
        btnAdd.addActionListener(e -> addRepair()); 
        btnDelete.addActionListener(e -> deleteRepair());
        btnClear.addActionListener(e -> clearFields());
        btnUpdate.addActionListener(e -> updateRepair());
        btnSearch.addActionListener(e -> searchRepairs());

        tblRepairs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRowIndex = tblRepairs.getSelectedRow();
                if (selectedRowIndex != -1) {
                   
                    txtDate.setText(tblRepairs.getModel().getValueAt(selectedRowIndex, 1).toString());
                    cmbStatus.setSelectedItem(tblRepairs.getModel().getValueAt(selectedRowIndex, 4).toString());

                    String regNum = tblRepairs.getModel().getValueAt(selectedRowIndex, 2).toString();
                    for (int i = 0; i < cmbVehicles.getItemCount(); i++) {
                        if (cmbVehicles.getItemAt(i).contains(regNum)) {
                            cmbVehicles.setSelectedIndex(i);
                            break;
                        }
                    }

                    String serviceName = tblRepairs.getModel().getValueAt(selectedRowIndex, 3).toString();
                    for (int i = 0; i < cmbServices.getItemCount(); i++) {
                        if (cmbServices.getItemAt(i).contains(serviceName)) {
                            cmbServices.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        updateData(); // Първоначално зареждане
    }

    // МЕТОД ЗА ОБНОВЯВАНЕ НА ВСИЧКО (извиква се при смяна на табове)
    public void updateData() {
        loadVehicles();
        loadServices();
        refreshTable();
    }

    public void loadVehicles() {
        cmbVehicles.removeAllItems();
        cmbVehicles.addItem("-- Избери кола --");
        String sql = "SELECT id, reg_number FROM vehicles";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cmbVehicles.addItem(rs.getInt("id") + ": " + rs.getString("reg_number"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void loadServices() {
        cmbServices.removeAllItems();
        cmbServices.addItem("-- Избери услуга --");
        String sql = "SELECT id, service_name FROM services";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cmbServices.addItem(rs.getInt("id") + ": " + rs.getString("service_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void refreshTable() {
        String sql = "SELECT r.id, r.repair_date AS [Дата], v.reg_number AS [Автомобил], " +
                     "s.service_name AS [Услуга], r.status AS [Статус] " +
                     "FROM repairs r " +
                     "JOIN vehicles v ON r.vehicle_id = v.id " +
                     "JOIN services s ON r.service_id = s.id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            tblRepairs.setModel(new MyModel(rs));
            if (tblRepairs.getColumnCount() > 0) {
                tblRepairs.removeColumn(tblRepairs.getColumnModel().getColumn(0)); 
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addRepair() {
        String vehiclePart = (String) cmbVehicles.getSelectedItem();
        String servicePart = (String) cmbServices.getSelectedItem();
        if (vehiclePart.startsWith("--") || servicePart.startsWith("--") || txtDate.getText().isEmpty()) return;

        int vehicleId = Integer.parseInt(vehiclePart.split(":")[0]);
        int serviceId = Integer.parseInt(servicePart.split(":")[0]);

        String sql = "INSERT INTO repairs (vehicle_id, service_id, repair_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, serviceId);
            pstmt.setString(3, txtDate.getText().trim());
            pstmt.setString(4, (String) cmbStatus.getSelectedItem());
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateRepair() {
        if (selectedRowIndex == -1) return;

        int repairId = Integer.parseInt(tblRepairs.getModel().getValueAt(selectedRowIndex, 0).toString());
        
        String vehiclePart = (String) cmbVehicles.getSelectedItem();
        String servicePart = (String) cmbServices.getSelectedItem();
        if (vehiclePart.startsWith("--") || servicePart.startsWith("--")) return;

        int vehicleId = Integer.parseInt(vehiclePart.split(":")[0]);
        int serviceId = Integer.parseInt(servicePart.split(":")[0]);

        String sql = "UPDATE repairs SET vehicle_id = ?, service_id = ?, status = ?, repair_date = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, serviceId);
            pstmt.setString(3, (String) cmbStatus.getSelectedItem());
            pstmt.setString(4, txtDate.getText().trim());
            pstmt.setInt(5, repairId);

            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void deleteRepair() {
        if (selectedRowIndex == -1) return;
        int repairId = Integer.parseInt(tblRepairs.getModel().getValueAt(selectedRowIndex, 0).toString());

        String sql = "DELETE FROM repairs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repairId);
            pstmt.executeUpdate();
            refreshTable();
            clearFields();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void searchRepairs() {
        String selectedStatus = (String) cmbStatus.getSelectedItem();
        String sql = "SELECT r.id, r.repair_date AS [Дата], v.reg_number AS [Автомобил], " +
                     "s.service_name AS [Услуга], r.status AS [Статус] " +
                     "FROM repairs r " +
                     "JOIN vehicles v ON r.vehicle_id = v.id " +
                     "JOIN services s ON r.service_id = s.id " +
                     "WHERE r.status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedStatus);
            ResultSet rs = pstmt.executeQuery();
            tblRepairs.setModel(new MyModel(rs));
            if (tblRepairs.getColumnCount() > 0) {
                tblRepairs.removeColumn(tblRepairs.getColumnModel().getColumn(0));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void clearFields() {
        cmbVehicles.setSelectedIndex(0);
        cmbServices.setSelectedIndex(0);
        txtDate.setText("");
        cmbStatus.setSelectedIndex(0);
        selectedRowIndex = -1;
        refreshTable();
    }
}