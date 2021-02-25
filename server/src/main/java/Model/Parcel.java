package Model;

import Controller.MainController;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;

public class Parcel implements Serializable {
    @MainController.DontSerialize
    private int ID;
    private String address;
    private Point coordinates;
    private int assignedPostmanID;
    private Date date;


    public Parcel(ParcelBuilder builder){
        this.ID = builder.ID;
        this.address = builder.address;
        this.coordinates = builder.coordinates;
        this.assignedPostmanID = builder.assignedPostmanID;
        this.date = builder.date;
    }

    public static String pointToString(Point point){
        return String.format("(%d %d)",point.x,point.y);
    }

    //only for DAO
    public Parcel(){

    }
    public void setID(int ID){this.ID = ID;}
    public void setDate(Date date){this.date = date;}

    public int getID(){
        return this.ID;
    }

    public Date getDate(){
        return this.date;
    }

    public String getAddress(){
        return this.address;
    }
    public void setAddress(String address){
        this.address = address;
    }

    public Point getCoordinates() { return this.coordinates; }
    public void setCoordinates(Point coordinates) { this.coordinates = coordinates; }

    public int getAssignedPostmanID(){
        return this.assignedPostmanID;
    }

    public boolean equals(Parcel parcel){
        if( this.ID == parcel.getID() &&
            this.address.equals(parcel.getAddress()) &&
            this.coordinates.equals(parcel.getCoordinates()) &&
            this.assignedPostmanID == parcel.getAssignedPostmanID() &&
            this.date.equals(parcel.getDate()))
            {
                return true;
            }
        return false;
    }

    public String toString(){
        return new StringBuilder()
                .append(Integer.toString(this.ID)).append("." )
                .append(this.address)
                .append(pointToString(this.coordinates))
                .toString();
    }

    public void setAssignedPostmanID(int assignedPostmanID){
        this.assignedPostmanID = assignedPostmanID;
    }

    public static class ParcelBuilder {
        private final int ID;
        private Date date;
        private String address;
        private Point coordinates;
        private int assignedPostmanID;

        public ParcelBuilder() {
            this.ID = 0;
            this.date = new Date();
        }

        public ParcelBuilder coordinates(Point coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        public ParcelBuilder address(String address) {
            this.address = address;
            return this;
        }

        public ParcelBuilder assignedPostmanID(int assignedPostmanID) {
            this.assignedPostmanID = assignedPostmanID;
            return this;
        }

        public Parcel build() {
            return new Parcel(this);
        }
    }
}
