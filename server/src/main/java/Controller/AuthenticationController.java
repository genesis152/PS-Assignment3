package Controller;

import DataAccessLayer.UserDAO;
import Model.User;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthenticationController {

    private MainController mainController;
    private MessageDigest hasher;
    private UserDAO userDAO;


    public AuthenticationController(MainController mainController){
        this.mainController = mainController;
        try {
            this.userDAO = new UserDAO();
            hasher = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private String getMd5FromString(String string){
        hasher.update(string.getBytes());
        byte[] md5 = hasher.digest();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< md5.length ;i++)
        {
            sb.append(Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1));
        }
        String md5String = sb.toString();
        return md5String;
    }

    public User verifyLogin(String username, String password){
        User user = userDAO.getUserByUsername(username);
        if(user != null){
            hasher.update(password.getBytes());
            byte[] md5 = hasher.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< md5.length ;i++)
            {
                sb.append(Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1));
            }
            String md5Password = sb.toString();
            if(user.getPassword().equals(md5Password)){
                return user;
            }
        }
        //authentication failed
        return null;
    }

    public int addUser(User user){
        if(userDAO.getUserByUsername(user.getUsername()) == null){
            user.setPassword(getMd5FromString(user.getPassword()));
            int ID = userDAO.insert(user);
            return ID;
        }
        return 0;
    }

    public void deleteUser(User user){
        userDAO.deleteById(user.getID());
    }

    public void updateUser(int id, User user){
        userDAO.updateOnId(user, id);
    }

    public List<User> getUsers(){
        return userDAO.findAll();
    }

    public User getUserByID(int id){
        return userDAO.findById(id);
    }

    public User getUserByUsername(String username){
        return userDAO.getUserByUsername(username);
    }
}
