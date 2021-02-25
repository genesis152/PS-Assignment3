package Controller;

import DataAccessLayer.UserDAO;
import Model.*;


import javax.management.Query;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class MainController{
    private AuthenticationController authenticationController;
    private ParcelController parcelController;
    private UserDAO userDAO;
    private ServerSocket serverSocket;
    private Socket socket;
    private static final String SERVER_CONFIGURATION_FILE_PATH = "server.cfg";
    private List<QueryHandler> clients;

    public MainController() {
        this.authenticationController = new AuthenticationController(this);
        this.parcelController = new ParcelController(this);
        this.clients = new ArrayList<QueryHandler>();

        User user1 = new User("John","JohnDoe","pass", User.Type.POSTMAN);
        User user2 = new User("Marie","Marie1","word", User.Type.POSTMAN);
        User user3 = new User("Cicada","Cicada","coord", User.Type.COORDINATOR);
        User user4 = new User("admin","admin","root", User.Type.ADMINISTRATOR);



//        Parcel parcel2 = new Parcel.ParcelBuilder()
//                                .address("Constanta")
//                                .coordinates(new Point(6,3))
//                                .assignedPostmanID(4)
//                                .build();
//        Parcel parcel3 = new Parcel.ParcelBuilder()
//                                .address("Oradea")
//                                .coordinates(new Point(3,5))
//                                .assignedPostmanID(4)
//                                .build();
//        Parcel parcel4 = new Parcel.ParcelBuilder()
//                                .address("Timisoara")
//                                .coordinates(new Point(4,4))
//                                .assignedPostmanID(4)
//                                .build();
//        Parcel parcel1 = new Parcel.ParcelBuilder()
//                                .address("Cluj")
//                                .coordinates(new Point(10,10))
//                                .assignedPostmanID(4)
//                                .build();
//        Parcel parcel5 = new Parcel.ParcelBuilder()
//                                .address("Deva1")
//                                .coordinates(new Point(0,5))
//                                .assignedPostmanID(4)
//                                .build();
        serverSetup();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface DontSerialize {
    }

    public void serverSetup() {
        BufferedReader reader;
        Pattern portReg = Pattern.compile("Port\\s*:\\s*([0-9]+)");
        int port = 0;
        try {
            reader = new BufferedReader(new FileReader(SERVER_CONFIGURATION_FILE_PATH));
            String line = reader.readLine();
            do {
                Matcher portMatcher = portReg.matcher(line);
                if (portMatcher.matches()) {
                    port = Integer.parseInt(portMatcher.group(1));
                }
                line = reader.readLine();
            } while (line != null);
            if (port == 0) {
                exit(0);
            }
            serverSocket = new ServerSocket(port);
            while (true) {
                synchronized (serverSocket) {
                    socket = serverSocket.accept();
                }
                QueryHandler queryHandler = new QueryHandler(this,authenticationController,parcelController,socket);
                queryHandler.start();
                addClient(queryHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClient(QueryHandler queryHandler){
        clients.add(queryHandler);
    }

    protected void removeClient(QueryHandler queryHandler){
        clients.remove(queryHandler);
    }

    public void userChanged(User changedUser, QueryHandler original){
        for(QueryHandler queryHandler : clients) {
            if (queryHandler != original) {
                User user = queryHandler.getLoggedUser();
                if (user != null) {
                    if(changedUser.getType() == User.Type.POSTMAN){
                        if (user.getType() == User.Type.COORDINATOR || user.getType() == User.Type.ADMINISTRATOR) {
                            queryHandler.requestPostmenUpdate();
                        }
                    }
                    if(changedUser.getType() == User.Type.COORDINATOR){
                        if (user.getType() == User.Type.ADMINISTRATOR) {
                            queryHandler.requestCoordinatorsUpdate();
                        }
                    }

                }
            }
        }
    }

    public void parcelChanged(Parcel changedParcel, QueryHandler original){
        for(QueryHandler queryHandler : clients) {
            if (queryHandler != original) {
                User user = queryHandler.getLoggedUser();
                if (user != null) {
                    if (user.getType() == User.Type.COORDINATOR) {
                        queryHandler.requestParcelsUpdate();
                    }
                    if (user.getType() == User.Type.POSTMAN){
                        if(user.getID() == changedParcel.getAssignedPostmanID()){
                            queryHandler.requestParcelsUpdate();
                        }
                    }
                }
            }
        }
    }

    public Object getGraphLayout(){
        return parcelController.getGraphLayout();
    }

}
