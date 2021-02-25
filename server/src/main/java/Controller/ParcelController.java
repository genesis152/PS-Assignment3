package Controller;

import DataAccessLayer.ParcelDAO;
import Model.Parcel;
import org.jgrapht.Graph;

import java.util.List;

public class ParcelController {
    private MainController mainController;
    private GraphController graphController;
    private ParcelDAO parcelDAO;

    public ParcelController(MainController mainController){
        this.mainController = mainController;
        this.parcelDAO = new ParcelDAO();
        this.graphController = new GraphController(getParcels());
    }

    public int addParcel(Parcel parcel){
        int ID = parcelDAO.insert(parcel);
        parcel.setID(ID);
        graphController.addParcelToCompleteGraph(parcel);
        return ID;
    }

    public void updateParcel(Parcel parcel){
        parcelDAO.updateOnId(parcel, parcel.getID());
        graphController.updateParcelInGraph(parcel);
    }

    public void printParcelGraph(){
        graphController.printGraph();
    }

    public Object getGraphLayout(){
        return graphController.saveGraphAsLayout();
    }

    public Graph getGraph(){
        return graphController.getGraph();
    }

    public void removeParcel(Parcel parcel){
        parcelDAO.deleteById(parcel.getID());
        graphController.removeParcelFromGraph(parcel);
    }

    public Parcel getParcelByID(int parcelID){
        return parcelDAO.findById(parcelID);
    }

    public List<Parcel> getParcelsByPostmanID(int postmanID){
        return parcelDAO.getParcelsByAssignedPostmanID(postmanID);
    }

    public List<Parcel> getParcels(){
        return parcelDAO.findAll();
    }
}
