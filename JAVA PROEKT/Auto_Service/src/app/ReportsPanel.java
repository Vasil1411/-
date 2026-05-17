package app;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class ReportsPanel extends JPanel {

    // 1. ДЕКЛАРИРАНЕ НА КОМПОНЕНТИ
    private JTextField txtSearchClientName, txtSearchVehicleBrand;
    private JButton btnSearch, btnClear;
    private JTable tblReports;
    private JScrollPane scrollPane;

    public ReportsPanel() {
        // 2. ИНИЦИАЛИЗАЦИЯ
        txtSearchClientName = new JTextField(12);
        txtSearchVehicleBrand = new JTextField(12);

        btnSearch = new JButton("Търси");
        btnClear = new JButton("Изчисти");

        tblReports = new JTable();
        tblReports.setAutoCreateRowSorter(true);

        // Инициализираме ScrollPane и го СКРИВАМЕ първоначално
        scrollPane = new JScrollPane(tblReports);
        scrollPane.setVisible(false); 

        // 3. СТИЛИЗАЦИЯ
        btnSearch.setBackground(new Color(255, 140, 0));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        
        btnClear.setBackground(new Color(200, 200, 200));

        // 4. ПОЗИЦИОНИРАНЕ (LAYOUT)
        setLayout(new BorderLayout(10, 10));

        JPanel pnlSearch = new JPanel(new GridBagLayout());
        pnlSearch.setBackground(new Color(245, 245, 245)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlSearch.add(new JLabel("Фамилия на клиент:"), gbc);
        gbc.gridx = 1; pnlSearch.add(txtSearchClientName, gbc);

        gbc.gridx = 2;
        pnlSearch.add(new JLabel("Марка автомобил:"), gbc);
        gbc.gridx = 3; pnlSearch.add(txtSearchVehicleBrand, gbc);

        gbc.gridx = 4;
        pnlSearch.add(btnSearch, gbc);
        
        gbc.gridx = 5;
        pnlSearch.add(btnClear, gbc);

        add(pnlSearch, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 5. СЪБИТИЯ
        btnSearch.addActionListener(e -> loadFullReport());

        btnClear.addActionListener(e -> {
            clearFields();
            scrollPane.setVisible(false); 
            revalidate();
            repaint();
        });
    }

    // ==========================================
    // ЛОГИКА ЗА ТЪРСЕНЕ И SQL
    // ==========================================

    public void loadFullReport() {
        String clientFilter = txtSearchClientName.getText().trim();
        String brandFilter = txtSearchVehicleBrand.getText().trim();

        // Проверка: Търсенето се извършва САМО ако и двете полета са попълнени.
        // Без диалогов прозорец - ако са празни, просто скриваме таблицата и спираме.
        if (clientFilter.isEmpty() || brandFilter.isEmpty()) {
            scrollPane.setVisible(false);
            revalidate();
            repaint();
            return;
        }

        String sql = "SELECT " +
                     "c.last_name AS [Клиент], " +
                     "v.brand_model AS [Марка], " +
                     "v.reg_number AS [Рег. Номер], " +
                     "s.service_name AS [Услуга], " +
                     "r.repair_date AS [Дата], " +
                     "r.status AS [Статус] " +
                     "FROM repairs r " +
                     "JOIN vehicles v ON r.vehicle_id = v.id " +
                     "JOIN clients c ON v.client_id = c.id " +
                     "JOIN services s ON r.service_id = s.id " +
                     "WHERE c.last_name LIKE ? AND v.brand_model LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + clientFilter + "%");
            pstmt.setString(2, "%" + brandFilter + "%");

            ResultSet rs = pstmt.executeQuery();

            // Ако няма намерени резултати, таблицата остава скрита (без диалог)
            if (!rs.isBeforeFirst()) {
                scrollPane.setVisible(false);
            } else {
                tblReports.setModel(new MyModel(rs));
                scrollPane.setVisible(true); // Показваме таблицата само при открити данни
            }

           
            revalidate();
            repaint();

        } catch (Exception e) {
            // Само системна грешка в конзолата, без да се прекъсва работата на потребителя
            e.printStackTrace();
        }
    }

    // Метод за опресняване при превключване на табове
    public void refreshTable() {
        scrollPane.setVisible(false);
        clearFields();
        revalidate();
        repaint();
    }

    public void clearFields() {
        txtSearchClientName.setText("");
        txtSearchVehicleBrand.setText("");
    }
}