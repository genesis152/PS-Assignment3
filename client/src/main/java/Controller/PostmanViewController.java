package Controller;

import Model.Parcel;
import Model.User;
import View.PostmanView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostmanViewController {
    private User currentUser;
    private PostmanView postmanView;
    private List<Parcel> parcels;
    protected MainController mainController;
    private ServerCommunication serverCommunication;
    private Map<String, String> componentsText;
    private Map<String, Component> components;
    private String[] dataTableColumns = {"", "", "", ""};
    private final String ROMANIAN_TEXT_FILE_PATH = "src/main/resources/language_text/postman/ro.txt";
    private final String ENGLISH_TEXT_FILE_PATH = "src/main/resources/language_text/postman/en.txt";
    private final String ITALIAN_TEXT_FILE_PATH = "src/main/resources/language_text/postman/it.txt";

    public PostmanViewController(MainController mainController, User user) {
        this.currentUser = user;
        this.postmanView = new PostmanView(this);
        this.postmanView.setVisible(true);
        this.mainController = mainController;
        serverCommunication = ServerCommunication.getInstance();
        this.parcels = serverCommunication.getParcelsByPostmanID(user.getID());
        updateParcelsTable(parcels);

        getComponents();
        try (FileReader f = new FileReader(ROMANIAN_TEXT_FILE_PATH); BufferedReader br = new BufferedReader(f)) {
            getComponentTextFromFile(br);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTextFromMap();
    }

    private JTable createTable(String[][] tableData, String[] tableCol) {
        DefaultTableModel model = new DefaultTableModel(tableData, tableCol);
        final JTable mainTable = new JTable();
        mainTable.setModel(model);
        return mainTable;
    }

    public ActionListener viewParcelsButtonActionListener() {
        return actionEvent -> {
            updateParcelsTable(serverCommunication.getParcelsByPostmanID(currentUser.getID()));
        };
    }

    protected void updateParcelsTable(List<Parcel> parcels){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        int size = parcels.size();
        Object[] tempArray = parcels.toArray();
        String[][] tableData = new String[size + 10][4];
        for (int i = 0; i < size; i++) {
            tableData[i][0] = Integer.toString(((Parcel) tempArray[i]).getID());
            tableData[i][1] = ((Parcel) tempArray[i]).getAddress();
            tableData[i][2] = Parcel.pointToString(((Parcel) tempArray[i]).getCoordinates());
            tableData[i][3] = formatter.format(((Parcel) tempArray[i]).getDate());
        }
        JTable table = createTable(tableData, dataTableColumns);
        postmanView.setDataTable(table);
        JScrollPane pane = postmanView.getDataTablePane();
        pane.setViewportView(table);
        pane.revalidate();
    }

    public ActionListener searchParcelByIDButtonActionListener() {
        return actionEvent -> {
            parcels = serverCommunication.getParcelsByPostmanID(currentUser.getID());
            int size = parcels.size();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Parcel parcel = serverCommunication.getParcelByID(Integer.parseInt(postmanView.getSearchParcelByIDText()));
            if (parcel != null) {
                String[][] tableData = new String[1][4];
                tableData[0][0] = Integer.toString(parcel.getID());
                tableData[0][1] = parcel.getAddress();
                tableData[0][2] = Parcel.pointToString(parcel.getCoordinates());
                tableData[0][3] = formatter.format(parcel.getDate());
                JTable table = createTable(tableData, dataTableColumns);
                postmanView.setDataTable(table);
                JScrollPane pane = postmanView.getDataTablePane();
                pane.setViewportView(table);
                pane.revalidate();
            }
        };
    }

    public ComponentAdapter mapPaneComponentAdapter() {
        return new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                postmanView.addContainerToMapPane(mainController.createContainerFromGraph(serverCommunication.getGraphLayout()));
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

    private void getComponents(){
        components = new HashMap<>();
        components.put("View Parcels Button", postmanView.getViewParcelsButton());
        components.put("Search By Parcel ID Button", postmanView.getSearchByParcelIDButton());
        components.put("Data Table", postmanView.getDataTable());
        components.put("Tabbed Panel", postmanView.getTabbedPanel());
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
                        postmanView.setDataTable((JTable) component);
                        postmanView.getDataTablePane().setViewportView((JTable) component);
                        postmanView.getDataTablePane().revalidate();
                    }
                }
                if(component instanceof JTabbedPane){
                    JTabbedPane panel = (JTabbedPane) component;
                    int tabs = panel.getTabCount();
                    System.out.println(tabs);
                    String[] titles = new String[tabs];
                    for(int i = 0; i < tabs; i++){
                        titles[i] = componentsText.get(componentName + i);
                        System.out.println(titles[i]);
                        panel.setTitleAt(i, titles[i]);
                    }
                    postmanView.setTabbedPane1(panel);
                    postmanView.repaint();
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

    public ActionListener comboBoxActionListener() {
        return actionEvent -> {
            getComponents();
            JComboBox<String> languageComboBox = postmanView.getLanguageComboBox();
            if (languageComboBox.getSelectedItem() != null) {
                String selectedLanguage = (String) languageComboBox.getSelectedItem();
                FileReader languageFile = null;
                BufferedReader reader;
                try {
                    switch (selectedLanguage) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert languageFile != null;
                setTextFromMap();
            }
        };
    }
}

