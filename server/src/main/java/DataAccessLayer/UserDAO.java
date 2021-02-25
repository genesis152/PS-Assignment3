package DataAccessLayer;

import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO extends AbstractDAO<User>{
    public UserDAO(){
        super();
    }

    public User getUserByUsername(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = ConnectionBuilder.getConnection();
            String query = createSelectQuery("username");
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            List<User> resultedObject = (List<User>) createObjects(resultSet);
            if (!resultedObject.isEmpty())
                return resultedObject.get(0);
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(statement!=null) ConnectionBuilder.close(statement);
            if(resultSet!=null) ConnectionBuilder.close(resultSet);
            if(connection!=null) ConnectionBuilder.close(connection);
        }
        //if no user found or something happened
        return null;
    }
}