package Controller;

import Model.Parcel;
import Model.Postman;
import Model.User;
import View.CoordinatorView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CoordinatorViewController{
    protected User currentUser;
    private CoordinatorView coordinatorView;
    private List<Parcel> parcels;
    private Integer selectedParcelID;
    private ServerCommunication serverCommunication;
    protected MainController mainController;
    private Map<Integer, User> postmen;
    private Map<String, String> componentsText;
    private Map<String, Component> components;
    private String[] dataTableColumns = {"", "", "", "", "", ""};
    private String[] postmanTableColumns = {"" , ""};
    private String[] updateTableColumns = {"", ""};
    private String[] updateTableFields = {"", "", ""};
    private final String ROMANIAN_TEXT_FILE_PATH = "src/main/resources/language_text/coordinator/ro.txt";
    private final String ENGLISH_TEXT_FILE_PATH = "src/main/resources/language_text/coordinator/en.txt";
    private final String ITALIAN_TEXT_FILE_PATH = "src/main/resources/language_text/coordinator/it.txt";


    public CoordinatorViewController(MainController mainController, User user){
        this.currentUser = user;
        this.coordinatorView = new CoordinatorView(this);
        this.coordinatorView.setVisible(true);
        this.mainController = mainController;
        serverCommunication = ServerCommunication.getInstance();
        this.parcels = serverCommunication.getParcels();
        updatePostmenTable(serverCommunication.getUsers());
        updateParcelsTable(parcels);
        updateSecondaryTable(parcels.get(0).getID());
        getComponents();

        try (FileReader f = new FileReader(ROMANIAN_TEXT_FILE_PATH); BufferedReader br = new BufferedReader(f)) {
            getComponentTextFromFile(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTextFromMap();
        coordinatorView.getAddButton().setVisible(false);
        coordinatorView.getUpdateParcelButton().setVisible(false);
    }

    private JTable createTable(String[][] tableData, String[] tableCol) {
        DefaultTableModel model = new DefaultTableModel(tableData, tableCol);
        final JTable mainTable = new JTable();
        mainTable.setModel(model);
        mainTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Object parcelId = mainTable.getValueAt(mainTable.getSelectedRow(), 0);
                    if (parcelId == null)
                        return;
                    selectedParcelID = Integer.parseInt((String)parcelId);
                    updateSecondaryTable(selectedParcelID);
                }
            }
        });
        return mainTable;
    }

    public void updateSecondaryTable(Integer parcelId){
        Parcel parcel = serverCommunication.getParcelByID(parcelId);
        if(parcel!=null){
            String[][] tableData = {
                    {updateTableFields[0], parcel.getAddress()},
                    {updateTableFields[1], Parcel.pointToString(parcel.getCoordinates())},
                    {updateTableFields[2], Integer.toString(parcel.getAssignedPostmanID())}
            };
            DefaultTableModel model = new DefaultTableModel(tableData, updateTableColumns);
            final JTable mainTable = new JTable();
            mainTable.setModel(model);
            JTable updateTable = coordinatorView.getUpdateTable();
            updateTable.setModel(model);
            coordinatorView.setUpdateTable(updateTable);
            JScrollPane tablePane = coordinatorView.getUpdateTablePane();
            tablePane.setViewportView(updateTable);
            tablePane.revalidate();
            coordinatorView.getUpdateParcelButton().setVisible(true);
            coordinatorView.getUpdateTablePane().setVisible(true);
        }
    }

    public ActionListener viewParcelsButtonActionListener() {
        return actionEvent ->{
            parcels = serverCommunication.getParcels();
            updateParcelsTable(parcels);
        };
    }

    protected void updateParcelsTable(List<Parcel> parcels){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        int size = parcels.size();
        Object[] tempArray = parcels.toArray();
        String[][] tableData = new String[size+10][6];
        for (int i = 0; i < size; i++) {
            tableData[i][0] = Integer.toString(((Parcel)tempArray[i]).getID());
            tableData[i][1] = ((Parcel)tempArray[i]).getAddress();
            tableData[i][2] = Parcel.pointToString(((Parcel)tempArray[i]).getCoordinates());
            tableData[i][3] = formatter.format(((Parcel)tempArray[i]).getDate());
            tableData[i][4] = postmen.get(((Parcel)tempArray[i]).getAssignedPostmanID()).getName();
            tableData[i][5] = Integer.toString(((Parcel)tempArray[i]).getAssignedPostmanID());
        }
        JTable table = createTable(tableData,dataTableColumns);
        coordinatorView.setDataTable(table);
        JScrollPane pane = coordinatorView.getDataTablePane();
        pane.setVisible(true);
        pane.setViewportView(table);
        pane.revalidate();

    }

    public ActionListener searchParcelByIDButtonActionListener() {
        return actionEvent ->{
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Parcel parcel = serverCommunication.getParcelByID(Integer.parseInt(coordinatorView.getSearchParcelByIDText()));
            if(parcel!=null) {
                String[][] tableData = new String[1][6];
                tableData[0][0] = Integer.toString(parcel.getID());
                tableData[0][1] = parcel.getAddress();
                tableData[0][2] = Parcel.pointToString(parcel.getCoordinates());
                tableData[0][3] = formatter.format(parcel.getDate());
                tableData[0][4] = postmen.get(parcel.getAssignedPostmanID()).getName();
                tableData[0][5] = Integer.toString(parcel.getAssignedPostmanID());

                JTable table = createTable(tableData, dataTableColumns);
                coordinatorView.setDataTable(table);
                JScrollPane pane = coordinatorView.getDataTablePane();
                pane.setViewportView(table);
                pane.revalidate();
            }
        };
    }

    public ActionListener addParcelButtonActionListener() {
        return actionEvent -> {
            String[][] tableData = {
                    {updateTableFields[0], ""},
                    {updateTableFields[1], ""},
                    {updateTableFields[2], ""}
            };
            DefaultTableModel model = new DefaultTableModel(tableData, updateTableColumns);
            final JTable mainTable = new JTable();
            mainTable.setModel(model);
            coordinatorView.setUpdateTable(mainTable);
            JScrollPane pane = coordinatorView.getUpdateTablePane();
            pane.setViewportView(mainTable);
            pane.revalidate();
            coordinatorView.getAddButton().setVisible(true);
            coordinatorView.getUpdateParcelButton().setVisible(false);
            coordinatorView.getUpdateTablePane().setVisible(true);
        };
    }

    public ActionListener deleteParcelButtonActionListener() {
        return actionEvent -> {
            Parcel parcel = serverCommunication.getParcelByID(selectedParcelID);
            if(parcel != null) {
                serverCommunication.deleteParcel(parcel);
            }
            for(ActionListener a: coordinatorView.getViewParcelsButton().getActionListeners()){
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
            coordinatorView.getUpdateTablePane().setVisible(false);
            coordinatorView.getUpdateParcelButton().setVisible(false);
        };
    }

    public ActionListener updateParcelButtonActionListener() {
        return actionEvent -> {
            TableModel model = coordinatorView.getUpdateTable().getModel();
            String address = (String)model.getValueAt(0,1);
            Scanner in = new Scanner((String)model.getValueAt(1,1)).useDelimiter("[^0-9]+");
            Point point = new Point(in.nextInt(), in.nextInt());
            int postmanID = Integer.parseInt((String)model.getValueAt(2,1));
            Parcel parcel = new Parcel.ParcelBuilder()
                                    .address(address)
                                    .coordinates(point)
                                    .assignedPostmanID(postmanID)
                                    .build();

            parcel.setID(selectedParcelID);
            serverCommunication.updateParcel(parcel);

            for(ActionListener a: coordinatorView.getViewParcelsButton().getActionListeners()){
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
            }

            coordinatorView.getUpdateTablePane().setVisible(false);
            coordinatorView.getUpdateParcelButton().setVisible(false);
        };
    }

    public ActionListener addButtonActionListener() {
        return actionEvent -> {
            TableModel model = coordinatorView.getUpdateTable().getModel();
            String address = (String)model.getValueAt(0,1);
            Scanner in = new Scanner((String)model.getValueAt(1,1)).useDelimiter("[^0-9]+");
            Point point = new Point(in.nextInt(), in.nextInt());
            int postmanID = Integer.parseInt((String)model.getValueAt(2,1));
            Parcel parcel = new Parcel.ParcelBuilder()
                    .address(address)
                    .coordinates(point)
                    .assignedPostmanID(postmanID)
                    .build();
            serverCommunication.insertParcel(parcel);

            for(ActionListener a: coordinatorView.getViewParcelsButton().getActionListeners()){
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,null));
            }
            coordinatorView.getUpdateTablePane().setVisible(false);
            coordinatorView.getAddButton().setVisible(false);
        };
    }

    public void updatePostmenTable(List<User> users){
        System.out.println("Updating postmen");
        postmen = new HashMap<>();
        for(User user : users){
            if(user.getType() == User.Type.POSTMAN){
                postmen.put(user.getID(), user);
            }
        }
        String[][] tableData = new String[postmen.size() + 10][2];
        int size = postmen.size();
        int i = 0;
        for (int id : postmen.keySet()){
            tableData[i][0] = Integer.toString(postmen.get(id).getID());
            tableData[i][1] = postmen.get(id).getName();
            i++;
        }
        JTable table = createTable(tableData, postmanTableColumns);
        coordinatorView.setPostmenTable(table);
        JScrollPane pane = coordinatorView.getPostmenTablePane();
        pane.setViewportView(table);
        pane.revalidate();
        table.setVisible(true);
    }

    public ComponentAdapter mapPaneComponentAdapter(){
        return new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                coordinatorView.addContainerToMapPane(mainController.createContainerFromGraph(serverCommunication.getGraphLayout()));
            }
        };
    }

    public ActionListener saveReportButtonActionListener(){
        return actionEvent -> {
            String option = coordinatorView.getReportTypeComboBoxText();
            if(option!=null) {
                if (option.equals("XML")) {
                    Serializer.serializeParcelsAsXML(serverCommunication.getParcels());
                } else if (option.equals("CSV")) {
                    Serializer.serializeParcelsAsCSV(serverCommunication.getParcels());
                } else if (option.equals("JSON")) {
                    Serializer.serializeReportAsJson(serverCommunication.getParcels());
                }
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
                            dataTableColumns[i] = componentsText.get(componentName + "i" + i);
                            ((JTable) component).getTableHeader().getColumnModel().getColumn(i).setHeaderValue(componentsText.get(componentName + "i" + i));
                        }
                        coordinatorView.setDataTable((JTable)component);
                        coordinatorView.getDataTablePane().setViewportView((JTable)component);
                        coordinatorView.getDataTablePane().revalidate();
                    }if(componentName.equals("Postmen Table")){
                        postmanTableColumns = new String[columns];
                        for (int i = 0; i < columns; i++) {
                            postmanTableColumns[i] = componentsText.get(componentName + "i" + i);
                            ((JTable) component).getTableHeader().getColumnModel().getColumn(i).setHeaderValue(componentsText.get(componentName + "i" + i));
                        }
                        coordinatorView.setPostmenTable((JTable)component);
                        coordinatorView.getPostmenTablePane().setViewportView((JTable)component);
                        coordinatorView.getPostmenTablePane().revalidate();
                    }
                    if(componentName.equals("Update Table")){
                        updateTableColumns = new String[columns];
                        int rows = ((JTable)component).getRowCount();
                        updateTableFields = new String[rows];
                        for(int i=0; i<columns; i++) {
                            updateTableColumns[i] = componentsText.get(componentName+"i"+i);
                            System.out.println(i);
                            ((JTable)component).getTableHeader().getColumnModel().getColumn(i).setHeaderValue(componentsText.get(componentName+"i"+i));
                        }
                        for(int i=0; i<rows; i++){
                            updateTableFields[i] = componentsText.get(componentName+"j"+i);
                            ((JTable)component).getModel().setValueAt(componentsText.get(componentName+"j"+i), i, 0);
                        }
                        coordinatorView.setUpdateTable((JTable)component);
                        coordinatorView.getUpdateTablePane().setViewportView((JTable)component);
                        coordinatorView.getUpdateTablePane().revalidate();
                    }
                }
                if(component instanceof JTabbedPane){
                    JTabbedPane panel = (JTabbedPane) component;
                    int tabs = panel.getTabCount();
                    String[] titles = new String[tabs];
                    for(int i = 0; i < tabs; i++){
                        titles[i] = componentsText.get(componentName + i);
                        System.out.println(titles[i]);
                        panel.setTitleAt(i, titles[i]);
                    }
                    if(componentName.equals("Main Tabbed Pane")){
                        coordinatorView.setMainTabbedPane(panel);
                    }if(componentName.equals("Secondary Tabbed Pane")){
                        coordinatorView.setSecondaryTabbedPane(panel);
                    }
                }

            }else{
                System.out.println("Bad key for " + componentName);
            }
        }
    }

    private void dumpComponentsToFiles(){
        FileWriter eng = null;
        FileWriter rom = null;
        FileWriter ita = null;
        try{
            eng = new FileWriter(ENGLISH_TEXT_FILE_PATH);
            rom = new FileWriter(ROMANIAN_TEXT_FILE_PATH);
            ita = new FileWriter(ITALIAN_TEXT_FILE_PATH);
            for(String component : components.keySet()) {
                rom.write(component + ":\n");
                eng.write(component + ":\n");
                ita.write(component + ":\n");
            }
            eng.close();
            rom.close();
            ita.close();
        }catch(Exception e){
            e.printStackTrace();
        }
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

    private void getComponents(){
        components = new HashMap<>();
        components.put("View Parcels Button", coordinatorView.getViewParcelsButton());
        components.put("Search By Parcel ID Button", coordinatorView.getSearchParcelByIDButton());
        components.put("Add Parcel Button", coordinatorView.getAddParcelButtonButton());
        components.put("Delete Parcel Button", coordinatorView.getDeleteParcelButton());
        components.put("Save Report Button", coordinatorView.getSaveReportButton());
        components.put("Update Button", coordinatorView.getUpdateParcelButton());
        components.put("Add Button", coordinatorView.getAddButton());
        components.put("Update Table", coordinatorView.getUpdateTable());
        components.put("Data Table", coordinatorView.getDataTable());
        components.put("Postmen Table", coordinatorView.getPostmenTable());
        components.put("Main Tabbed Pane", coordinatorView.getMainTabbedPane());
        components.put("Secondary Tabbed Pane", coordinatorView.getSecondaryTabbedPane());
    }

    public ActionListener comboBoxActionListener() {
        return actionEvent ->{
            getComponents();
            JComboBox<String> languageComboBox = coordinatorView.getLanguageComboBox();
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
}
