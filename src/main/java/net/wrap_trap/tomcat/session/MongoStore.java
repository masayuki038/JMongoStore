package net.wrap_trap.tomcat.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoStore extends StoreBase implements Store {

	private static final int DEFAULT_PORT = 27017;

	/** hostname */
	private String host;
	
	/** port */
	private Integer port;

	/** databaseName */
	private String databaseName;
	
	/** collectionName */
	private String collectionName;
	
	/** user */
	private String user;
	
	/** password */
	private String password;
	
	/** MongoDB */
	private DB db;
	
	/** Collection */
	private DBCollection collection;
	
	@Override
	public void clear() throws IOException {
		collection.drop();
	}

	@Override
	public int getSize() throws IOException {
		return (int)collection.count();
	}

	@Override
	public String[] keys() throws IOException {
		List<String> list = new ArrayList<String>();
		DBCursor cursor = collection.find();
		while(cursor.hasNext()){
			String id = (String)cursor.next().get("id");
			if(!StringUtils.isEmpty(id)){
				list.add(id);
			}
		}
		String[] ret = new String[list.size()];
		list.toArray(ret);
		return ret;
	}

	@Override
	public Session load(String id) throws ClassNotFoundException, IOException {
		DBObject query = new BasicDBObject();
		query.put("id", id);
		DBCursor cursor = collection.find(query);
		while(cursor.hasNext()){
            if (manager.getContainer().getLogger().isDebugEnabled()) {
                manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".loading",
                        id, collectionName));
            }
			MongoSession session = new MongoSession(manager);
			try {
				session.readDBObject(cursor.next());
				session.setManager(manager);
		        manager.getContainer().getLogger().debug(getStoreName() + ": No persisted data object found");
				return session;
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void remove(String id) throws IOException {
		DBObject query = new BasicDBObject();
		query.put("id", id);
		DBCursor cursor = collection.find(query);
		while(cursor.hasNext()){
			collection.remove(cursor.next());
		}
	}

	@Override
	public void save(Session session) throws IOException {
		remove(session.getIdInternal());
		MongoSession mongoSession = null;
		try {
			if(session instanceof MongoSession){
				mongoSession = (MongoSession)session;
			}else if(session instanceof StandardSession){
				mongoSession = new MongoSession(manager);
				PropertyUtils.copyProperties(mongoSession, session);
			}
			collection.save(mongoSession.createDBObject());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
        if (manager.getContainer().getLogger().isDebugEnabled()) {
            manager.getContainer().getLogger().debug(sm.getString(getStoreName() + ".saving",
                    session.getIdInternal(), collectionName));
        }
	}

	@Override
	public void start() throws LifecycleException {
		super.start();
		try{
			Mongo mongo = new Mongo(host, (port == null)? DEFAULT_PORT : port);
			db = mongo.getDB(databaseName);
			if(!StringUtils.isEmpty(user)){
				if(!db.authenticate(user, password.toCharArray())){
					throw new IllegalAccessException("Authentication for MongoDB has failed. Please make sure that MongoDB options(especially secure mode) and credentials.");
				}
			}
			collection = db.getCollection(collectionName);
		}catch(Exception e){
			throw new LifecycleException(e);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
}
