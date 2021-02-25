package Model;

import java.io.Serializable;

public class Administrator extends User implements Serializable {
    public Administrator(String username, String name, String password, Type type) {
        super(username,name,password,type);
    }
}
