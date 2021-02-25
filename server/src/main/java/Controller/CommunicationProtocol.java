package Controller;

import java.io.Serializable;
import java.util.List;

public class CommunicationProtocol <T> implements Serializable {
    //DATA_PARSING
    public static final int VERIFY_LOGIN = 1;
    public static final int INSERT_USER = 2;
    public static final int DELETE_USER = 3;
    public static final int UPDATE_USER = 4;
    public static final int INSERT_PARCEL = 5;
    public static final int DELETE_PARCEL = 6;
    public static final int UPDATE_PARCEL = 7;
    //QUERIES
    public static final int GET_ALL_PARCELS = 10;
    public static final int GET_ALL_USERS = 11;
    public static final int GET_PARCELS_BY_POSTMAN_ID = 12;
    public static final int GET_PARCELS_BY_ID = 13;
    public static final int GET_USER_BY_USERNAME = 14;
    public static final int GET_USER_BY_ID = 15;
    public static final int GET_GRAPH_LAYOUT = 16;
    //SERVER REQUESTS
    public static final int REQUEST_UPDATE_PARCELS = 20;
    public static final int REQUEST_UPDATE_COORDINATORS = 21;
    public static final int REQUEST_UPDATE_POSTMEN = 22;
    public static final int EXIT = 0;

    //THE ANSWERS OF THE QUERIES AND DATA_PARSING MESSAGES WILL HAVE THEIR COMMUNICATIONPURPOSE = -COMMUNICATIONPURPOSE

    public int communicationPurpose;
    public List<T> objects;

    public CommunicationProtocol(int communicationPurpose, List<T> objects){
        this.communicationPurpose = communicationPurpose;
        this.objects = objects;
    }
}
