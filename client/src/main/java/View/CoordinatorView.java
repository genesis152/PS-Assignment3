package View;

import Controller.CoordinatorViewController;

import javax.swing.*;
import java.awt.*;


public class CoordinatorView extends JFrame{

    private JPanel rootPanel;
    private JTabbedPane tabbedPane1;
    private JTable dataTable;
    private JButton viewParcelsButton;
    private JButton searchParcelByIDButton;
    private JTextField parcelIDTextField;
    private JScrollPane dataTablePane;
    private JPanel mapPane;
    private JButton addParcelButton;
    private JButton deleteParcelButton;
    private JTable updateTable;
    private JButton updateParcelButton;
    private JScrollPane updateTablePane;
    private JTabbedPane tabbedPane2;
    private JTable postmansTable;
    private JScrollPane postmansTablePane;
    private JPanel viewParcelsPane;
    private JComboBox reportTypeComboBox;
    private JButton saveReportButton;
    private JPanel comboBoxPanel;
    private JButton addButton;
    private JComboBox<String> languageComboBox;


    public String getReportTypeComboBoxText(){
        return (String)this.reportTypeComboBox.getSelectedItem();
    }
    public void addContainerToMapPane(Object layout) {
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

    public JScrollPane getUpdateTablePane() { return this.updateTablePane; }

    public JTable getUpdateTable() { return this.updateTable; }

    public void setUpdateTable(JTable updateTable){
        this.updateTable = updateTable;
    }

    public void setUpdateParcelButtonText(String text){ updateParcelButton.setText(text); }

    public String getUpdateParcelButtonText() { return this.updateParcelButton.getText(); }

    public JScrollPane getPostmenTablePane() { return this.postmansTablePane; }

    public JScrollPane getDataTablePane(){
        return this.dataTablePane;
    }

    public JButton getUpdateParcelButton(){ return this.updateParcelButton; }

    public JButton getAddButton(){ return this.addButton; }

    public JButton getViewParcelsButton() { return this.viewParcelsButton; }

    public JButton getDeleteParcelButton() { return deleteParcelButton; }

    public JButton getAddParcelButtonButton() { return addParcelButton; }

    public JButton getSearchParcelByIDButton() { return searchParcelByIDButton; }

    public JButton getSaveReportButton() { return saveReportButton; }

    public JTable getDataTable(){
        return this.dataTable;
    }

    public JTable getPostmenTable() { return this.postmansTable; }

    public JTabbedPane getMainTabbedPane() { return tabbedPane1; }

    public JTabbedPane getSecondaryTabbedPane() { return tabbedPane2; }

    public void setMainTabbedPane(JTabbedPane pane) { tabbedPane1 = pane; }

    public void setSecondaryTabbedPane(JTabbedPane pane) { tabbedPane2 = pane; }

    public void setPostmenTable(JTable postmenTable){
        this.postmansTable = postmenTable;
    }

    public void setDataTable(JTable table) { dataTable = table;}

    public String getSearchParcelByIDText(){
        return this.parcelIDTextField.getText();
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

    public CoordinatorView(CoordinatorViewController coordinatorViewController){//, GraphController graphController){
        this.setTitle("Coordinator View");
        this.setSize(1080,840);
        if(this.rootPanel == null){
            System.out.println("Null root panel");
            return;
        }
        this.add(rootPanel);
        initializeLanguageComboBox();
        viewParcelsButton.addActionListener(coordinatorViewController.viewParcelsButtonActionListener());
        searchParcelByIDButton.addActionListener(coordinatorViewController.searchParcelByIDButtonActionListener());
        addParcelButton.addActionListener(coordinatorViewController.addParcelButtonActionListener());
        updateParcelButton.addActionListener(coordinatorViewController.updateParcelButtonActionListener());
        deleteParcelButton.addActionListener(coordinatorViewController.deleteParcelButtonActionListener());
        mapPane.addComponentListener(coordinatorViewController.mapPaneComponentAdapter());
        this.addWindowListener(coordinatorViewController.windowListener());
        saveReportButton.addActionListener(coordinatorViewController.saveReportButtonActionListener());
        languageComboBox.addActionListener(coordinatorViewController.comboBoxActionListener());
        addButton.addActionListener(coordinatorViewController.addButtonActionListener());
        LayoutManager layoutManager = new FlowLayout();
        this.mapPane.setLayout(layoutManager);
    }


    private void createUIComponents(){
        // TODO: place custom component creation code here
    }
}
