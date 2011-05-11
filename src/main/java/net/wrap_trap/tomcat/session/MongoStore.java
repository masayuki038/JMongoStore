package net.wrap_trap.tomcat.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;

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
			StandardSession standardSession = (StandardSession)manager.createEmptySession();
			standardSession.setManager(manager);
			try {
				restoreStandardSession(standardSession, cursor.next());
		        manager.getContainer().getLogger().debug(getStoreName() + ": No persisted data object found");
				return standardSession;
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
		try {
			if(session instanceof StandardSession){
				collection.save(createDBObject((StandardSession)session));
			}else{
				throw new IllegalArgumentException("unexpected session class: " + session.getClass().getName());
			}
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
	
	@SuppressWarnings("unchecked")
	public void restoreStandardSession(StandardSession session, DBObject object)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		session.setAuthType(null); // Transient only
		session.setCreationTime((Long)object.get("creationTime"));
		FieldUtils.getField(StandardSession.class, "lastAccessedTime", true).set(session, object.get("lastAccessedTime"));
		session.setMaxInactiveInterval((Integer)object.get("maxInactiveInterval"));
		session.setNew((Boolean)object.get("isNew"));
		session.setValid((Boolean)object.get("isValid"));
		FieldUtils.getField(StandardSession.class, "thisAccessedTime", true).set(session, object.get("thisAccessedTime"));
		session.setPrincipal(null); // Transient only
		session.setId((String)object.get("id"));

		Manager manager = session.getManager();
		if (manager.getContainer().getLogger().isDebugEnabled())
            manager.getContainer().getLogger().debug
                ("readObject() loading session " + session.getId());


        boolean isValidSave = session.isValid();
        session.setValid(true);
        Map attributeMap = createRestorer().toMap((DBObject)object.get("attributes"));
        for(Object name : attributeMap.keySet()){
        	session.setAttribute((String)name, attributeMap.get(name));
        }
        session.setValid(isValidSave);
	}
	
	protected Restorer createRestorer(){
		return new Restorer();
	}
	
	@SuppressWarnings("unchecked")
	public DBObject createDBObject(StandardSession standardSession) throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		DBObject object = new BasicDBObject();
        object.put("creationTime", standardSession.getCreationTime());
        object.put("lastAccessedTime", standardSession.getLastAccessedTime());
        object.put("maxInactiveInterval", standardSession.getMaxInactiveInterval());
        object.put("isNew", standardSession.isNew());
        object.put("isValid", standardSession.isValid());
        object.put("thisAccessedTime", FieldUtils.getFieldValue(standardSession, "thisAccessedTime", true));
        object.put("id", standardSession.getId());

        if (standardSession.getManager().getContainer().getLogger().isDebugEnabled())
        	standardSession.getManager().getContainer().getLogger().debug("writeObject() storing session " + standardSession.getId());

        Map<Object, Object> attributes = new HashMap<Object, Object>();
        for(Enumeration<String> e = standardSession.getAttributeNames(); e.hasMoreElements();){
        	String name = e.nextElement();
        	attributes.put(name, standardSession.getAttribute(name));
        }
        object.put("attributes", createDBObjectBuilder().build(attributes));
        return object;
	}
	
	protected DBObjectBuilder createDBObjectBuilder(){
		return new DBObjectBuilder();
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
