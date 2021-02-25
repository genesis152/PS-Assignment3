package Controller;

import Model.Parcel;
import Model.User;
import org.jgrapht.Graph;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class QueryHandler extends Thread  {
    private AuthenticationController authenticationController;
    private ParcelController parcelController;
    private MainController mainController;
    private Socket socket;
    private User loggedUser;

    public QueryHandler(MainController mainController, AuthenticationController authenticationController, ParcelController parcelController, Socket socket) {
        this.authenticationController = authenticationController;
        this.parcelController = parcelController;
        this.mainController = mainController;
        this.socket = socket;
        this.loggedUser = null;
    }

    public void run() {
        System.out.println("Client connected!");
        while (true) {
            CommunicationProtocol<Object> message = Serializer.deserialize(socket);
            assert message != null;
            System.out.println(message.communicationPurpose);
            switch (message.communicationPurpose) {
                case CommunicationProtocol.VERIFY_LOGIN:
                    verifyLogin(message);
                    break;
                case CommunicationProtocol.GET_PARCELS_BY_ID:
                    getParcelsByID(message);
                    break;
                case CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID:
                    getParcelsByPostmanID(message);
                    break;
                case CommunicationProtocol.GET_ALL_PARCELS:
                    getAllParcels(message);
                    break;
                case CommunicationProtocol.GET_ALL_USERS:
                    getAllUsers(message);
                    break;
                case CommunicationProtocol.INSERT_USER:
                    insertUser(message);
                    break;
                case CommunicationProtocol.UPDATE_USER:
                    updateUser(message);
                    break;
                case CommunicationProtocol.DELETE_USER:
                    deleteUser(message);
                    break;
                case CommunicationProtocol.GET_USER_BY_ID:
                    getUserByID(message);
                    break;
                case CommunicationProtocol.INSERT_PARCEL:
                    insertParcel(message);
                    break;
                case CommunicationProtocol.UPDATE_PARCEL:
                    updateParcel(message);
                    break;
                case CommunicationProtocol.DELETE_PARCEL:
                    deleteParcel(message);
                    break;
                case CommunicationProtocol.GET_USER_BY_USERNAME:
                    getUserByUsername(message);
                    break;
                case CommunicationProtocol.GET_GRAPH_LAYOUT:
                    getGraphLayout(message);
                    break;
                case CommunicationProtocol.EXIT:
                    System.out.println("Client disconnected");
                    mainController.removeClient(this);
                    return;
                default:

                    return;
            }
        }
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void requestPostmenUpdate(){
        System.out.println("Notifying postmen");
        CommunicationProtocol<User> request = new CommunicationProtocol<>(
                CommunicationProtocol.REQUEST_UPDATE_POSTMEN, authenticationController.getUsers());
        Serializer.serialize(request, socket);
    }

    public void requestCoordinatorsUpdate(){
        System.out.println("Notifying coordinators");
        CommunicationProtocol<User> request = new CommunicationProtocol<>(
                CommunicationProtocol.REQUEST_UPDATE_COORDINATORS, authenticationController.getUsers());
        Serializer.serialize(request, socket);
    }

    public void requestParcelsUpdate(){
        System.out.println("Notifying parcel");
        CommunicationProtocol<Parcel> request = new CommunicationProtocol<>(
                CommunicationProtocol.REQUEST_UPDATE_PARCELS, parcelController.getParcels());
        Serializer.serialize(request, socket);
    }

    public void verifyLogin(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.VERIFY_LOGIN);
        String username = (String) message.objects.get(0);
        String password = (String) message.objects.get(1);
        User user = authenticationController.verifyLogin(username, password);
        List<User> list = new ArrayList<>(1);
        if (user == null) {
            list = null;
        } else {
            list.add(user);
            this.loggedUser = user;
        }
        CommunicationProtocol<User> response = new CommunicationProtocol<>(
                -CommunicationProtocol.VERIFY_LOGIN, list);
        Serializer.serialize(response, socket);
    }

    public void getParcelsByPostmanID(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID);
        int id = (Integer) message.objects.get(0);
        List<Parcel> parcels = parcelController.getParcelsByPostmanID(id);
        CommunicationProtocol<Parcel> response = new CommunicationProtocol<>(
                -CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID, parcels);
        Serializer.serialize(response, socket);
    }

    public void getParcelsByID(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_PARCELS_BY_POSTMAN_ID);
        int id = (Integer) message.objects.get(0);
        List<Parcel> parcels = new ArrayList<>(1);
        Parcel parcel = parcelController.getParcelByID(id);
        parcels.add(parcel);
        CommunicationProtocol<Parcel> response = new CommunicationProtocol<>(
                -CommunicationProtocol.GET_PARCELS_BY_ID, parcels);
        Serializer.serialize(response, socket);
    }

    public void getAllParcels(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_ALL_PARCELS);
        CommunicationProtocol<Parcel> response = new CommunicationProtocol<>(
                -CommunicationProtocol.GET_ALL_PARCELS, parcelController.getParcels());
        Serializer.serialize(response, socket);
    }

    public void getAllUsers(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_ALL_USERS);
        CommunicationProtocol<User> response = new CommunicationProtocol<>(
                -CommunicationProtocol.GET_ALL_USERS, authenticationController.getUsers());
        Serializer.serialize(response, socket);
    }

    public void insertUser(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.INSERT_USER);
        if (message.objects != null) {
            User user = (User) message.objects.get(0);
            Integer ID = authenticationController.addUser(user);
            mainController.userChanged(user, this);
            List<Integer> list = new ArrayList<>(1);
            list.add(ID);
            CommunicationProtocol<Integer> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.INSERT_USER, list);
            Serializer.serialize(response, socket);
        }
    }

    public void updateUser(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.UPDATE_USER);
        if (message.objects != null) {
            User user = (User) message.objects.get(1);
            authenticationController.updateUser(
                    ((User) message.objects.get(0)).getID(),
                    user);
            mainController.userChanged(user, this);
            CommunicationProtocol<Object> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.UPDATE_USER, null);
            Serializer.serialize(response, socket);
        }
    }

    public void deleteUser(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.DELETE_USER);
        if (message.objects != null) {
            User user = (User) message.objects.get(0);
            authenticationController.deleteUser(user);
            mainController.userChanged(user, this);
            CommunicationProtocol<Object> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.DELETE_USER, null);
            Serializer.serialize(response, socket);
        }
    }

    public void getUserByUsername(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_USER_BY_USERNAME);
        if (message.objects != null) {
            List<User> args = new ArrayList<>(1);
            args.add(authenticationController.getUserByUsername((String) message.objects.get(0)));
            CommunicationProtocol<User> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.GET_USER_BY_USERNAME, args);
            Serializer.serialize(response, socket);
        }
    }

    public void getUserByID(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_USER_BY_ID);
        if (message.objects != null) {
            List<User> args = new ArrayList<>(1);
            args.add(authenticationController.getUserByID((int) message.objects.get(0)));
            CommunicationProtocol<User> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.GET_USER_BY_ID, args);
            Serializer.serialize(response, socket);
        }
    }

    public void insertParcel(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.INSERT_PARCEL);
        if (message.objects != null) {
            Integer ID = parcelController.addParcel((Parcel) message.objects.get(0));
            mainController.parcelChanged((Parcel) message.objects.get(0), this);
            List<Integer> list = new ArrayList<>(1);
            list.add(ID);
            CommunicationProtocol<Integer> response = new CommunicationProtocol<Integer>(
                    -CommunicationProtocol.INSERT_PARCEL, list);
            Serializer.serialize(response, socket);
        }
    }

    public void updateParcel(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.UPDATE_PARCEL);
        if (message.objects != null) {
            parcelController.updateParcel((Parcel) message.objects.get(0));
            mainController.parcelChanged((Parcel) message.objects.get(0), this);
            CommunicationProtocol<Integer> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.UPDATE_PARCEL, null);
            Serializer.serialize(response, socket);
        }
    }

    public void deleteParcel(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.DELETE_PARCEL);
        if (message.objects != null) {
            parcelController.removeParcel((Parcel) message.objects.get(0));
            mainController.parcelChanged((Parcel) message.objects.get(0), this);
            CommunicationProtocol<Integer> response = new CommunicationProtocol<>(
                    -CommunicationProtocol.DELETE_PARCEL, null);
            Serializer.serialize(response, socket);
        }
    }

    public void getGraphLayout(CommunicationProtocol<Object> message) {
        assert (message.communicationPurpose == CommunicationProtocol.GET_GRAPH_LAYOUT);
        List<Graph> args = new ArrayList<>(1);
        args.add(parcelController.getGraph());
        CommunicationProtocol<Graph> response = new CommunicationProtocol<>(
                -CommunicationProtocol.GET_GRAPH_LAYOUT, args);
        Serializer.serialize(response, socket);
    }
}
