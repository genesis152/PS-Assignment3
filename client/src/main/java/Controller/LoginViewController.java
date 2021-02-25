package Controller;

import Model.User;
import View.LoginView;

public class LoginViewController {

    private LoginView loginView;
    private ServerCommunication serverCommunication;

    public LoginViewController( ){
        this.loginView = new LoginView(this);
        loginView.setVisible(true);
        serverCommunication = ServerCommunication.getInstance();
    }

    public boolean verifyLogin(String name, String password){
        User user = serverCommunication.verifyLogin(name,password);
        if(user == null){
            return false;
        }
        return true;
    }

    public void endView(){
        loginView.setVisible(false);
    }

}
