package Model;

import java.io.Serializable;

public class Coordinator extends User implements Serializable {
    public Coordinator(String username, String name, String password, Type type) {
        super(username,name,password,type);
    }
}
