package Model;

import java.io.Serializable;

public class Postman extends User implements Serializable {

    public Postman(String username, String name, String password, Type type) {
        super(username,name,password,type);
    }
}
