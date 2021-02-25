package DataAccessLayer;

import Controller.MainController;
import Model.User;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryType;
import mil.nga.sf.util.ByteReader;
import mil.nga.sf.wkb.GeometryReader;
import mil.nga.sf.wkb.GeometryTypeInfo;

import java.awt.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractDAO<T> {

	private final Class<T> type;

	private Point readPoint(String pointString){
		int x=0,y=0;
		Pattern pattern = Pattern.compile("[(]([0-9]+),([0-9]+)[)]");
		Matcher matcher = pattern.matcher(pointString);
		if(matcher.find()){
			x = Integer.parseInt(matcher.group(1));
			y = Integer.parseInt(matcher.group(2));
		}
		return new Point(x,y);
	}

	@SuppressWarnings("unchecked")
	public AbstractDAO() {
		this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		
	}
	/**
	 * creates a select query
	 * @param field - the field to select on
	 * @return the query
	 */
	protected String createSelectQuery(String field) {
		StringBuilder sb = new StringBuilder();
		return sb.append("SELECT ").append("* ").append("FROM ").append(type.getSimpleName())
				.append(" WHERE " + field + " =?").toString();
	}

	/**
	 * creates a select *(all) query 
	 * @return the query
	 */
	private String createSelectAllQuery() {
		StringBuilder sb = new StringBuilder();
		return sb.append("SELECT ").append("* ").append("FROM ").append(type.getSimpleName()).toString();
	}
	
	/**
	 * creates a delete query
	 * @param field the field to delete on
	 * @return the query
	 */
	private String createDeleteQuery(String field) {
		StringBuilder sb = new StringBuilder();
		return sb.append("DELETE ").append("FROM ").append(type.getSimpleName()).append(" WHERE " + field + " =?")
				.toString();
	}

	/**
	 * creates an insert query
	 * @return the query
	 */
	private String createInsertQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT ").append("INTO ").append(type.getSimpleName()).append(" ( ");
		for (Field field : type.getDeclaredFields()) {
			if(!field.isAnnotationPresent(MainController.DontSerialize.class)) {
				sb.append(field.getName()).append(",");
			}
		}
		
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") ").append(" VALUES ").append(" ( ");
		for (@SuppressWarnings("unused") Field field : type.getDeclaredFields()) {
			if(!field.isAnnotationPresent(MainController.DontSerialize.class)) {
				sb.append("?").append(",");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") ");
		return sb.toString();
	}
	
	/**
	 * creates an update query
	 * @param field the field to update on
	 * @return the query
	 */
	private String createUpdateQuery(String field) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(type.getSimpleName()).append(" SET ");
		for (Field each_field : type.getDeclaredFields()) {
			if(each_field.getName() != "ID" && each_field.getName() != "count")
				sb.append(each_field.getName()).append(" = ").append(" ?,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" WHERE ").append(field.toString()).append(" =?");
		return sb.toString();
	}
	
	/**
	 * creates a list of objects from a result set of a query execution
	 * @param resultSet the result set
	 * @return a lists of object
	 */
	protected List<T> createObjects(ResultSet resultSet) {
		List<T> list = new ArrayList<T>();
		try {
			while (resultSet.next()) {
				T instance = type.newInstance();
				for (Field field : type.getDeclaredFields()) {
					try {
						if(resultSet.getString(field.getName()) != null) {
							Object value = resultSet.getObject(field.getName());
							if(field.getName()=="coordinates"){
								Point coordinates = readPoint((String)value);
								PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), type);
								Method method = propertyDescriptor.getWriteMethod();
								method.invoke(instance, coordinates);
							}
							else{
								if (value != null) {
									PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), type);
									Method method = propertyDescriptor.getWriteMethod();
									if(field.getType()== User.Type.class){
										method.invoke(instance, Enum.valueOf(User.Type.class,(String)value));
									}
									else{
										method.invoke(instance, value);
									}

								}

							}
						} 
					}catch(java.sql.SQLException e) {
					}
				}
				list.add(instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * inserts an object into the database
	 * @param object the object to be inserted
	 */
	public int insert(T object) {
		Connection connection = null;
		PreparedStatement statement = null;
		String query = createInsertQuery();
		try {
			connection = ConnectionBuilder.getConnection();
			statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			int i = 1;
			for (Field field : object.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if(field.getType() == User.Type.class){
					statement.setObject(i, (field.get(object)).toString());
					i = i + 1;
				}
				else {
					if(field.getType() == Point.class){
						statement.setObject(i,
								String.format("(%d,%d)",
										((Point)field.get(object)).x,
										((Point)field.get(object)).y));
						i = i + 1;
					}

					else {
						if (!field.isAnnotationPresent(MainController.DontSerialize.class)) {
							statement.setObject(i, field.get(object));
							i = i + 1;
						}
					}
				}
			}
			System.out.println(statement.toString());
			int ok = statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return ok;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionBuilder.close(statement);
			ConnectionBuilder.close(connection);
		}
		return 0;
	}

	/**
	 * updates an object having a specific id
	 * @param object the new data of the object to be updated
	 * @param id the specific id
	 */
	public void updateOnId(T object, int id) {
		Connection connection = null;
		PreparedStatement statement = null;
		String query = createUpdateQuery("ID");
		try {
			connection = ConnectionBuilder.getConnection();
			statement = connection.prepareStatement(query);
			//System.out.println(query);
			int i = 1;
			for (Field field : object.getClass().getDeclaredFields()) {
				if (field.getName().equals("ID") == false) {
					field.setAccessible(true);
					if(field.getType() == User.Type.class){
						statement.setObject(i, (field.get(object)).toString());
						i = i + 1;
					}
					else {
						if(field.getType() == Point.class){
							statement.setObject(i,
									String.format("(%d,%d)",
											((Point)field.get(object)).x,
											((Point)field.get(object)).y));
							i = i + 1;
						}
						else {
							if (!field.isAnnotationPresent(MainController.DontSerialize.class)) {
								statement.setObject(i, field.get(object));
								i = i + 1;
							}
						}
					}
				}
			}
			statement.setObject(i, id);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionBuilder.close(statement);
			ConnectionBuilder.close(connection);
		}
	}

	/**
	 * finds an object by a specific id
	 * @param id the id
	 * @return the object
	 */
	public T findById(int id) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String query = createSelectQuery("id");
		try {
			connection = ConnectionBuilder.getConnection();
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);

			resultSet = statement.executeQuery();
			//if(resultSet.first() == false)
			//	return null;
			//System.out.println("YO" + (resultSet.isBeforeFirst() && resultSet.isAfterLast()));
			List<T> resultedObject = (List<T>) createObjects(resultSet);
			if (resultedObject.isEmpty() != true)
				return resultedObject.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionBuilder.close(statement);
			ConnectionBuilder.close(resultSet);
			ConnectionBuilder.close(connection);
		}
		return null;
	}

	/**
	 * finds all the objects 
	 * @return a lists of the objects
	 */
	public List<T> findAll() {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String query = createSelectAllQuery();
		try {
			connection = ConnectionBuilder.getConnection();
			statement = connection.prepareStatement(query);
			resultSet = statement.executeQuery();
			List<T> resultedObject = (List<T>) createObjects(resultSet);
			if (resultedObject.isEmpty() != true)
				return resultedObject;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionBuilder.close(statement);
			ConnectionBuilder.close(resultSet);
			ConnectionBuilder.close(connection);
		}
		return null;
	}
	
	/**
	 * deletes an object having a specific id
	 * @param id the specific id
	 */
	public void deleteById(int id) {
		Connection connection = null;
		PreparedStatement statement = null;
		String query = createDeleteQuery("id");
		try {
			connection = ConnectionBuilder.getConnection();
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);

			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionBuilder.close(statement);
			ConnectionBuilder.close(connection);
		}
	}
}
