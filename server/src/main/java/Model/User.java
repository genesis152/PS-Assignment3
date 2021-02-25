package Model;



import Controller.MainController;

import java.io.Serializable;

public class User implements Serializable {
    public static enum Type {POSTMAN,COORDINATOR,ADMINISTRATOR};
    @MainController.DontSerialize
    private int ID;
    private String username;
    private String name;
    private String password;
    private Type type;

    public User(){
        this.ID = 0;
    }

    public User(String username, String name, String password, Type type){
        this.username = username;
        this.ID = 0;
        this.name = name;
        this.password = password;
        this.type = type;
    }

    public int getID(){
        return this.ID;
    }

    public Type getType(){
        return this.type;
    }

    public void setType(Type type){
        this.type = type;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setID(int ID){
        this.ID = ID;
    }
}
