package View;

import Controller.AdministratorController;

import javax.swing.*;
import java.awt.*;

public class AdministratorView extends JFrame{
    private JTable dataTable;
    private JButton viewPostmenButton;
    private JButton viewCoordinatorsButton;
    private JButton deleteEntryButton;
    private JTable updateTable;
    private JButton updateEntryButton;
    private JButton addEntryButton;
    private JScrollPane dataTablePane;
    private JScrollPane updateTablePane;
    private JPanel rootPanel;
    private JComboBox<String> languageComboBox;
    private JPanel comboBoxPanel;
    private JButton addButton;
    private JPanel tablePanel;


    public JTable getDataTable(){
        return dataTable;
    }

    public void setDataTable(JTable dataTable){
        this.dataTable = dataTable;
    }

    public void setUpdateTable(JTable updateTable) { this.updateTable = updateTable; }

    public JScrollPane getDataTablePane(){
        return dataTablePane;
    }

    public JTable getUpdateTable() { return updateTable;}

    public JScrollPane getUpdateTablePane(){
        return updateTablePane;
    }

    public JButton getAddButton() { return addButton; }

    public JButton getUpdateEntryButton() { return updateEntryButton;}

    public JButton getViewPostmenButton(){ return viewPostmenButton; }

    public JButton getViewCoordinatorsButton() { return viewCoordinatorsButton; }

    public JButton getDeleteEntryButton() { return deleteEntryButton; }

    public JButton getAddEntryButton() { return addEntryButton; }

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

    public AdministratorView(AdministratorController administratorController) {
        this.setTitle("Administrator");
        this.setSize(860,640);
        if(rootPanel == null){
            System.out.println("Null root panel");
        }
        this.add(rootPanel);
        initializeLanguageComboBox();
        updateEntryButton.setVisible(false);
        updateTable.setVisible(true);
        this.rootPanel.repaint();
        addButton.addActionListener(administratorController.addButtonActionListener());
        viewPostmenButton.addActionListener(administratorController.viewPostmenButtonActionListener());
        viewCoordinatorsButton.addActionListener(administratorController.viewCoordinatorsButtonActionListener());
        deleteEntryButton.addActionListener(administratorController.deleteButtonActionListener());
        updateEntryButton.addActionListener(administratorController.updateEntryButtonActionListener());
        addEntryButton.addActionListener(administratorController.addEntryButtonActionListener());
        this.addWindowListener(administratorController.windowListener());
        languageComboBox.addActionListener(administratorController.comboBoxActionListener());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
