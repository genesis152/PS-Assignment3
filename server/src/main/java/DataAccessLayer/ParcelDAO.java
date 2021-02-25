package DataAccessLayer;

import Model.Parcel;
import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ParcelDAO extends AbstractDAO<Parcel>{
    public ParcelDAO(){
        super();
    }

    public List<Parcel> getParcelsByAssignedPostmanID(int assignedPostmanID) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = ConnectionBuilder.getConnection();
            String query = createSelectQuery("assignedPostmanID");
            statement = connection.prepareStatement(query);
            statement.setInt(1, assignedPostmanID);
            resultSet = statement.executeQuery();
            List<Parcel> resultedObject = (List<Parcel>) createObjects(resultSet);
            if (!resultedObject.isEmpty())
                return resultedObject;
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