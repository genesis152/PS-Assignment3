package Controller;

import Model.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import org.jgrapht.Graph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class MainController {
    private LoginViewController loginViewController;
    private final String CLIENT_CONFIGURATION_FILE_PATH = "client.cfg";
    private Socket socket;
    private User loggedUser;
    private AdministratorController administratorController;
    private PostmanViewController postmanViewController;
    private CoordinatorViewController coordinatorViewController;
    private ServerCommunication communication;

    public MainController() {
        clientSetup();
        this.loginViewController = new LoginViewController();
        this.loggedUser = null;
        this.coordinatorViewController = null;
        this.postmanViewController = null;
        this.administratorController = null;
        //this.communication.verifyLogin("John", "pass");
        //this.communication.verifyLogin("Cicada", "coord");


    }

    public void clientSetup(){
        BufferedReader reader;
        Pattern ipAddressReg = Pattern.compile("IP Address\\s*:\\s*(([0-9]{1,3}\\.){3}[0-9]{1,3})");
        Pattern portReg = Pattern.compile("Port\\s*:\\s*([0-9]+)");
        String ipAddress = null;
        int port = 0;
        try{
            reader = new BufferedReader(new FileReader(CLIENT_CONFIGURATION_FILE_PATH));
            String line = reader.readLine();
            do{
                Matcher ipAddressMatcher = ipAddressReg.matcher(line);
                Matcher portMatcher = portReg.matcher(line);
                if(ipAddressMatcher.matches()){
                    ipAddress = ipAddressMatcher.group(1);

                }
                if(portMatcher.matches()){
                    port = Integer.parseInt(portMatcher.group(1));

                }
                line=reader.readLine();
            }while(line!=null);
            if(ipAddress == null || port == 0){
                exit(0);
            }
            socket = new Socket(ipAddress,port);
            //System.out.println(CommunicationProtocol.EXIT);
            communication = ServerCommunication.buildInstance(this, socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchView(User user){
        loggedUser = user;

        if(user.getType()== User.Type.ADMINISTRATOR){
            loginViewController.endView();
            administratorController = new AdministratorController(this);
        }
        if(user.getType() == User.Type.POSTMAN){
            loginViewController.endView();
            postmanViewController = new PostmanViewController(this, user);
        }
        if(user.getType() == User.Type.COORDINATOR){
            loginViewController.endView();
            coordinatorViewController = new CoordinatorViewController(this, user);
        }
    }

    protected void updatePostmen(List<User> users){
        if(loggedUser.getType() == User.Type.ADMINISTRATOR){
            administratorController.updatePostmenTable(users);
        }
        if(loggedUser.getType() == User.Type.COORDINATOR){
            coordinatorViewController.updatePostmenTable(users);
        }
    }

    protected void updateParcels(List<Parcel> parcels){
        if(loggedUser.getType() == User.Type.COORDINATOR){
            System.out.println("Invoking view update");
            coordinatorViewController.updateParcelsTable(parcels);
        }
        if(loggedUser.getType() == User.Type.POSTMAN){
            List<Parcel> filteredParcels = new ArrayList<>();
            for(Parcel parcel : parcels){
                if(parcel.getAssignedPostmanID() == loggedUser.getID()){
                    filteredParcels.add(parcel);
                }
            }
            postmanViewController.updateParcelsTable(filteredParcels);
        }
    }

    protected void updateCoordinators(List<User> users){
        if(loggedUser.getType() == User.Type.ADMINISTRATOR){
            administratorController.updateCoordinatorsTable(users);
        }
    }


    public Object createContainerFromGraph(Graph graph){
        JGraphXAdapter<Parcel, GraphController.WeightedEdge> graphAdapter =
                new JGraphXAdapter<Parcel, GraphController.WeightedEdge>(graph);

        mxGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        mxGraphModel graphModel  = (mxGraphModel)graphComponent.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();
        mxUtils.setCellStyles(graphComponent.getGraph().getModel(),
                cells.toArray(), mxConstants.STYLE_ENDARROW, mxConstants.NONE);
        graphComponent.setPreferredSize(new Dimension(400,400));


        return graphComponent;
    }
}
