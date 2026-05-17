
package app;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class App extends JFrame {

    // 1. Дефинираме панелите като полета на класа, за да имаме достъп до тях
    private ClientPanel clientPanel;
    private VehiclePanel vehiclePanel;
    private ServicesPanel servicesPanel;
    private RepairsPanel repairsPanel;
    private ReportsPanel reportsPanel;

    public App() {
        setTitle("Система за управление на автосервиз");
        setSize(1000, 700); // Малко по-голям размер за по-добра видимост
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрира прозореца на екрана

        // 2. Инициализираме обектите на панелите
        clientPanel = new ClientPanel();
        vehiclePanel = new VehiclePanel();
        servicesPanel = new ServicesPanel();
        repairsPanel = new RepairsPanel();
        reportsPanel = new ReportsPanel();

        JTabbedPane tabs = new JTabbedPane();

        // 3. Добавяме вече създадените обекти в табовете
        tabs.addTab("Клиенти", clientPanel);
        tabs.addTab("Автомобили", vehiclePanel);
        tabs.addTab("Услуги", servicesPanel);
        tabs.addTab("Ремонти", repairsPanel);
        tabs.addTab("Справки", reportsPanel);

        // 4. КЛЮЧЪТ КЪМ СИНХРОНИЗАЦИЯТА: ChangeListener
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Взимаме заглавието на избрания в момента таб
                int selectedIndex = tabs.getSelectedIndex();
                String title = tabs.getTitleAt(selectedIndex);

                // Когато потребителят избере таб, обновяваме данните му
                switch (title) {
                    case "Автомобили":
                        vehiclePanel.updateData(); // Презарежда клиентите в ComboBox
                        break;
                    case "Ремонти":
                        repairsPanel.updateData(); // Презарежда коли и услуги в ComboBox
                        break;
                    case "Услуги":
                        servicesPanel.refreshTable(); // Опреснява цените, ако са променяни
                        break;
                    case "Клиенти":
                        clientPanel.refreshTable();
                        break;
                    case "Справки":
                        reportsPanel.loadFullReport(); // Ако имаш такъв метод за справките
                        break;
                }
            }
        });

        add(tabs);
        setVisible(true);
    }

    public static void main(String[] args) {
        
      
            new App();
    }
}