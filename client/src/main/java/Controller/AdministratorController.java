package Controller;

import Model.User;
import View.AdministratorView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class AdministratorController {
    private AdministratorView administratorView;
    private List<String> postmen;
    private List<String> coordinators;
    private User.Type currentShowingType;
    private String selectedUserName;
    private MainController mainController;
    private ServerCommunication serverCommunication;
    private Map<String, String> componentsText;
    private Map<String, Component> components;
    private String[] dataTableColumns = {""};
    private String[] updateTableColumns = {"", ""};
    private String[] updateTableFields = {"", "", ""};
    private final String ROMANIAN_TEXT_FILE_PATH = "src/main/resources/language_text/administrator/ro.txt";
    private final String ENGLISH_TEXT_FILE_PATH = "src/main/resources/language_text/administrator/en.txt";
    private final String ITALIAN_TEXT_FILE_PATH = "src/main/resources/language_text/administrator/it.txt";


    public AdministratorController(MainController mainController){
        this.mainController = mainController;
        administratorView = new AdministratorView(this);
        serverCommunication = ServerCommunication.getInstance();
        classifyUsers(serverCommunication.getUsers());
        updatePostmenTable(serverCommunication.getUsers());
        updateSecondaryTable(postmen.get(0));
        selectedUserName = postmen.get(0);

        getComponents();
        try (FileReader f = new FileReader(ROMANIAN_TEXT_FILE_PATH); BufferedReader br = new BufferedReader(f)) {
            getComponentTextFromFile(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTextFromMap();

        administratorView.setVisible(true);
        administratorView.getAddButton().setVisible(false);
        administratorView.getUpdateEntryButton().setVisible(false);
        //dumpComponentsToFiles();
    }

    private void getComponents(){
        components = new HashMap<>();
        components.put("Update Entry Button", administratorView.getUpdateEntryButton());
        components.put("Add Entry Button", administratorView.getAddEntryButton());
        components.put("Delete Entry Button", administratorView.getDeleteEntryButton());
        components.put("View Coordinators Button", administratorView.getViewCoordinatorsButton());
        components.put("View Postmen Button", administratorView.getViewPostmenButton());
        components.put("Add Button", administratorView.getAddButton());
        components.put("Data Table", administratorView.getDataTable());
        components.put("Update Table", administratorView.getUpdateTable());
    }

    public void classifyUsers(List<User> users){
        postmen = new LinkedList<>();
        coordinators = new LinkedList<>();
        for(User user : users){
            User.Type type = user.getType();
            if(type == User.Type.POSTMAN){
                postmen.add(user.getUsername());
            }
            if(type == User.Type.COORDINATOR){
                coordinators.add(user.getUsername());
            }
        }
    }

    private JTable createTable(String[][] tableData, String[] tableCol) {
        DefaultTableModel model = new DefaultTableModel(tableData, tableCol);
        final JTable mainTable = new JTable();
        mainTable.setModel(model);
        mainTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Object username = mainTable.getValueAt(mainTable.getSelectedRow(), 0);
                    if (username == null){
                        return;
                    }
                    System.out.println(username);
                    updateSecondaryTable((String)username);
                    selectedUserName = (String)username;
                }
            }
        });
        return mainTable;
    }

    public ActionListener viewPostmenButtonActionListener() {
        return actionEvent ->{
            updatePostmenTable(serverCommunication.getUsers());
        };
    }

    protected void updatePostmenTable(List<User> users){
        classifyUsers(users);
        String[][] tableData = new String[postmen.size() + 10][1];
        int size = postmen.size();
        for (int i = 0; i < size; i++) {
            tableData[i][0] = postmen.get(i);
        }

        JTable table = createTable(tableData, dataTableColumns);
        JScrollPane pane = administratorView.getDataTablePane();
        administratorView.setDataTable(table);
        pane.setViewportView(table);
        pane.revalidate();
        currentShowingType = User.Type.POSTMAN;
        table.setVisible(true);
    }

    public ActionListener viewCoordinatorsButtonActionListener(){
        return actionEvent -> {
            updateCoordinatorsTable(serverCommunication.getUsers());
        };
    }

    protected void updateCoordinatorsTable(List<User> users){
        classifyUsers(users);
        String[][] tableData = new String[coordinators.size()+10][1];
        int size = coordinators.size();
        for(int i=0;i<size;i++){
            tableData[i][0] = coordinators.get(i);
        }
        JTable dataTable = createTable(tableData, dataTableColumns);
        JScrollPane pane = administratorView.getDataTablePane();
        administratorView.setDataTable(dataTable);
        pane.setViewportView(dataTable);
        pane.revalidate();
        currentShowingType = User.Type.COORDINATOR;
        dataTable.setVisible(true);
    }

    public ActionListener updateEntryButtonActionListener(){
        return actionEvent -> {
            String oldUsername = selectedUserName;
            TableModel model = administratorView.getUpdateTable().getModel();
            String name = (String)model.getValueAt(0,1);
            String username = (String)model.getValueAt(1,1);
            String password = (String)model.getValueAt(2,1);
            User user = new User(username, name, password, currentShowingType);
            User oldUser = serverCommunication.getUserByUsername(oldUsername);
            serverCommunication.updateUser(oldUser, user);
            if(currentShowingType == User.Type.POSTMAN){
                for(ActionListener a: administratorView.getViewPostmenButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }else if(currentShowingType == User.Type.COORDINATOR){
                for(ActionListener a: administratorView.getViewCoordinatorsButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }
            administratorView.getUpdateEntryButton().setVisible(false);
            administratorView.getUpdateTablePane().setVisible(false);
        };
    }

    public ActionListener addButtonActionListener() {
        return actionEvent -> {
            TableModel model = administratorView.getUpdateTable().getModel();
            String name = (String)model.getValueAt(0,1);
            String username = (String)model.getValueAt(1,1);
            String password = (String)model.getValueAt(2,1);
            User user = new User(username, name, password, currentShowingType);
            serverCommunication.addUser(user);
            if(currentShowingType == User.Type.POSTMAN){
                for(ActionListener a: administratorView.getViewPostmenButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }else if(currentShowingType == User.Type.COORDINATOR){
                for(ActionListener a: administratorView.getViewCoordinatorsButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }
            administratorView.getAddButton().setVisible(false);
            administratorView.getUpdateTablePane().setVisible(false);
        };
    }

    public ActionListener addEntryButtonActionListener(){
        return actionEvent -> {
            String[][] tableData = {
                    {updateTableFields[0], ""},
                    {updateTableFields[1], ""},
                    {updateTableFields[2], ""}
            };
            DefaultTableModel model = new DefaultTableModel(tableData, updateTableColumns);
            final JTable mainTable = new JTable();
            mainTable.setModel(model);
            administratorView.setUpdateTable(mainTable);
            JScrollPane pane = administratorView.getUpdateTablePane();
            pane.setViewportView(mainTable);
            pane.revalidate();
            administratorView.getAddButton().setVisible(true);
            administratorView.getUpdateEntryButton().setVisible(false);
            administratorView.getUpdateTablePane().setVisible(true);
        };
    }

    public void updateSecondaryTable(String name){
        User user = serverCommunication.getUserByUsername(name);
        if(user!=null){
            String[][] tableData = {
                    {updateTableFields[0], user.getName()},
                    {updateTableFields[1], user.getUsername()},
                    {updateTableFields[2], user.getPassword()}
            };
            DefaultTableModel model = new DefaultTableModel(tableData, updateTableColumns);
            final JTable mainTable = new JTable();
            mainTable.setModel(model);
            JTable updateTable = administratorView.getUpdateTable();
            updateTable.setModel(model);
            administratorView.setUpdateTable(updateTable);
            JScrollPane tablePane = administratorView.getUpdateTablePane();
            tablePane.setVisible(true);
            tablePane.setViewportView(updateTable);
            tablePane.revalidate();
            administratorView.getUpdateEntryButton().setVisible(true);

        }
    }

    public ActionListener deleteButtonActionListener(){
        return actionEvent -> {
            User user = serverCommunication.getUserByUsername(selectedUserName);
            if(user != null) {
                serverCommunication.deleteUser(user);
            }
            if(currentShowingType == User.Type.POSTMAN){
                for(ActionListener a: administratorView.getViewPostmenButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }else if(currentShowingType == User.Type.COORDINATOR){
                for(ActionListener a: administratorView.getViewCoordinatorsButton().getActionListeners()){
                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
                }
            }
            administratorView.getUpdateTablePane().setVisible(false);
        };
    }

    private void getComponentTextFromFile(BufferedReader reader){
        componentsText = new HashMap<>();
        try{
            String line = reader.readLine();;
            while(line!=null){
                System.out.println(line);
                String[] componentText = line.split(":", 2);
                componentsText.put(componentText[0],componentText[1]);
                line = reader.readLine();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void setTextFromMap(){
        for (String componentName : components.keySet()){
            if(componentsText.containsKey(componentName)){
                Component component = components.get(componentName);
                if(component instanceof JButton){
                    ((JButton)component).setText(componentsText.get(componentName));
                }
                if(component instanceof JTable){
                    int columns = ((JTable)component).getColumnCount();
                    if(componentName.equals("Data Table")) {
                        dataTableColumns = new String[columns];
                        for (int i = 0; i < columns; i++) {
                            dataTableColumns[i] = componentsText.get(componentName + "i" + Integer.toString(i));
                            ((JTable) component).getTableHeader().getColumnModel().getColumn(i).setHeaderValue(componentsText.get(componentName + "i" + Integer.toString(i)));
                        }
                        administratorView.setDataTable((JTable)component);
                        administratorView.getDataTablePane().setViewportView((JTable)component);
                        administratorView.getDataTablePane().revalidate();

                    }if(componentName.equals("Update Table")){
                        updateTableColumns = new String[columns];
                        int rows = ((JTable)component).getRowCount();
                        updateTableFields = new String[rows];
                        for(int i=0; i<columns; i++) {
                            updateTableColumns[i] = componentsText.get(componentName+"i"+Integer.toString(i));
                            System.out.println(i);
                            ((JTable)component).getTableHeader().getColumnModel().getColumn(i).setHeaderValue(componentsText.get(componentName+"i"+Integer.toString(i)));
                        }
                        for(int i=0; i<rows; i++){
                            updateTableFields[i] = componentsText.get(componentName+"j"+Integer.toString(i));
                            ((JTable)component).getModel().setValueAt(componentsText.get(componentName+"j"+Integer.toString(i)), i, 0);
                        }
                        administratorView.setUpdateTable((JTable)component);
                        administratorView.getUpdateTablePane().setViewportView((JTable)component);
                        administratorView.getUpdateTablePane().revalidate();
                    }
                }
            }else{
                System.out.println("Bad key for " + componentName);
            }
        }
    }

    public ActionListener comboBoxActionListener(){
        return actionEvent ->{
            getComponents();
            JComboBox<String> languageComboBox = administratorView.getLanguageComboBox();
            if(languageComboBox.getSelectedItem() != null) {
                String selectedLanguage = (String) languageComboBox.getSelectedItem();
                FileReader languageFile = null;
                BufferedReader reader;
                try {
                    switch(selectedLanguage) {
                        case "EN": {
                            languageFile = new FileReader(ENGLISH_TEXT_FILE_PATH);
                            break;
                        }
                        case "RO": {
                            languageFile = new FileReader(ROMANIAN_TEXT_FILE_PATH);
                            break;
                        }
                        case "IT": {
                            languageFile = new FileReader(ITALIAN_TEXT_FILE_PATH);
                            break;
                        }
                        default:
                            return;
                    }
                    reader = new BufferedReader(languageFile);
                    getComponentTextFromFile(reader);
                    reader.close();
                    languageFile.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
                assert languageFile != null;
                setTextFromMap();
            }
        };
    }

    public WindowListener windowListener(){
        return new WindowListener(){

            @Override
            public void windowOpened(WindowEvent windowEvent) {

            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                serverCommunication.exit();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {

            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {

            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {

            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {

            }
        };

    }
}
