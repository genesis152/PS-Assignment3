package View;

import Controller.PostmanViewController;

import javax.swing.*;
import java.awt.*;

public class PostmanView extends JFrame{
    private JLabel welcomeLabel;
    private JPanel rootPanel;
    private JTabbedPane tabbedPane1;
    private JTable dataTable;
    private JButton viewParcelsButton;
    private JButton searchParcelByIDButton;
    private JTextField parcelIDTextField;
    private JScrollPane dataTablePane;
    private JPanel mapPane;
    private JPanel comboBoxPanel;
    private JPanel parcelsPane;
    private JComboBox<String> languageComboBox;


    protected JTabbedPane getMainTabbedPane(){
        return tabbedPane1;
    }

    public JScrollPane getDataTablePane(){
        return this.dataTablePane;
    }

    public JTable getDataTable(){
        return this.dataTable;
    }

    public void setDataTable(JTable dataTable){
        this.dataTable = dataTable;
    }

    public String getSearchParcelByIDText(){
        return this.parcelIDTextField.getText();
    }

    public void addContainerToMapPane(Object layout){
        for (Component component : this.mapPane.getComponents()) {
            if (component instanceof Container) {
                this.mapPane.remove(component);
            }
        }
        //((Component)layout).setPreferredSize(new Dimension(this.mapPane.getWidth(),this.mapPane.getHeight()));
        this.mapPane.add((Component) layout);
        this.mapPane.revalidate();
        this.mapPane.repaint();
    }

    public PostmanView(){

    }

    public JComboBox<String> getLanguageComboBox(){
        return this.languageComboBox;
    }

    private void initializeLanguageComboBox() {
        String[] languages = {"RO","EN","IT"};
        languageComboBox = new JComboBox<>(languages);
        languageComboBox.setRenderer(new ComboBoxRenderer());
        languageComboBox.setSelectedIndex(0);
        comboBoxPanel.setLayout(new GridLayout(1,4));
        comboBoxPanel.add(Box.createHorizontalBox());
        comboBoxPanel.add(Box.createHorizontalBox());
        comboBoxPanel.add(Box.createHorizontalBox());
        comboBoxPanel.add(languageComboBox);
        comboBoxPanel.revalidate();
    }


    public PostmanView(PostmanViewController postmanViewController){
        this.setTitle("Postman View");
        this.setSize(1080,840);
        if(rootPanel == null){
            System.out.println("Null root panel");
        }
        this.add(rootPanel);
        initializeLanguageComboBox();
        mapPane.addComponentListener(postmanViewController.mapPaneComponentAdapter());
        viewParcelsButton.addActionListener(postmanViewController.viewParcelsButtonActionListener());
        searchParcelByIDButton.addActionListener(postmanViewController.searchParcelByIDButtonActionListener());
        this.addWindowListener(postmanViewController.windowListener());
        LayoutManager layoutManager = new FlowLayout();
        this.mapPane.setLayout(layoutManager);
        languageComboBox.addActionListener(postmanViewController.comboBoxActionListener());
    }

    public JButton getViewParcelsButton() {
        return this.viewParcelsButton;
    }

    public JButton getSearchByParcelIDButton() {
        return this.searchParcelByIDButton;
    }

    public JTabbedPane getTabbedPanel() {
        return this.tabbedPane1;
    }

    public void setTabbedPane1(JTabbedPane panel){
        tabbedPane1 = panel;
    }
}
