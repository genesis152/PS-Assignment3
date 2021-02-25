package View;

import Controller.LoginViewController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame{
    private JPasswordField passwordField;
    private JButton loginButton;
    private JTextField usernameField;
    private JLabel topLabel;
    private JPanel rootPanel;

    public LoginView(LoginViewController loginViewController) {
        this.setTitle("Login View");
        this.setSize(600,500);
        this.add(rootPanel);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = usernameField.getText();
                String password = new String(passwordField.getPassword());
                loginViewController.verifyLogin(name,password);
            }
        });
    }

    public String getTopLabelText(){
        return this.topLabel.getText();
    }

    public void setTopLabelText(String text){
        this.topLabel.setText(text);
    }
}
