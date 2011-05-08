package net.wrap_trap.tomcat.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;


import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.BSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoSession extends StandardSession {

	/** serialVersionUID */
	private static final long serialVersionUID = 5437987286958208205L;
	
	/** CLASS_NAME */
	private static final String CLASS_NAME = "class";
	private static final String COLLECTION_CLASS_NAME = "collectionClass";
	private static final String COLLECTION_VALUE = "collectionValue";

	public MongoSession(Manager manager) {
		super(manager);
	}

	@SuppressWarnings("unchecked")
	public void readDBObject(DBObject object)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        authType = null;  // Transient only
        creationTime = (Long)object.get("creationTime");
        lastAccessedTime = (Long)object.get("lastAccessedTime");
        maxInactiveInterval = (Integer)object.get("maxInactiveInterval");
        isNew = (Boolean)object.get("isNew");
        isValid = (Boolean)object.get("isValid");
        thisAccessedTime = (Long)object.get("thisAccessedTime");
        principal = null; // Transient only
        id = (String)object.get("id");
        if (manager.getContainer().getLogger().isDebugEnabled())
            manager.getContainer().getLogger().debug
                ("readObject() loading session " + id);


        if (attributes == null){
            attributes = new Hashtable();
        }
        boolean isValidSave = isValid;
        isValid = true;
        Map attributeMap = getMap((DBObject)object.get("attributes"), new HashMap<DBObject, Object>());
        attributes.putAll(attributeMap);

        isValid = isValidSave;

        if (listeners == null) {
            listeners = new ArrayList();
        }

        if (notes == null) {
            notes = new Hashtable();
        }
	}
	
	@SuppressWarnings("unchecked")
	protected Map getMap(DBObject target, Map<DBObject, Object> cached) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		if(cached.containsKey(target)){
			return (Map)cached.get(target);
		}
		
		Map map = new HashMap();
		for(String key : target.keySet()){
			Object value = target.get(key);
			if(value instanceof DBObject){
				DBObject dbObject = (DBObject)value;
				if(dbObject.containsField(COLLECTION_CLASS_NAME)){
					Collection restoredCollection = getList(dbObject, cached);
					map.put(key, restoredCollection);
				}else{
					Object object = getObject((DBObject)value, cached);
					map.put(key, object);
				}
			}else{
				map.put(key, value);
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	protected Collection getList(DBObject target, Map<DBObject, Object> cached) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if(cached.containsKey(target)){
			return (Collection)cached.get(target);
		}
		List<Object> list = new ArrayList<Object>();
		for(Object o : (BasicDBList)target.get(COLLECTION_VALUE)){
			if(o instanceof DBObject){
				DBObject dbObject = (DBObject)o;
				if(dbObject.containsField(COLLECTION_CLASS_NAME)){
					Collection restoredCollection = getList(dbObject, cached);
					list.add(restoredCollection);
				}else{
					Object object = getObject((DBObject)o, cached);
					list.add(object);
				}
			}else{
				list.add(o);
			}
		}
		Class<?> collectionClass = Class.forName((String)target.get(COLLECTION_CLASS_NAME));
		Collection collection = (Collection)collectionClass.newInstance();
		collection.addAll(list);
		return collection;
	}

	@SuppressWarnings("unchecked")
	protected Object getObject(DBObject dbObject, Map<DBObject, Object> cached) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException{
		Map restoredMap = getMap((DBObject)dbObject, cached);
		if(restoredMap.containsKey(CLASS_NAME)){
			String className = (String)restoredMap.get(CLASS_NAME);
			Class<?> clazz = Class.forName(className);
			Object restoredObject = clazz.newInstance();
			PropertyUtils.copyProperties(restoredObject, restoredMap);
			return restoredObject;
		}
		return restoredMap;
	}

	public DBObject createDBObject() throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		DBObject object = new BasicDBObject();
        object.put("creationTime", creationTime);
        object.put("lastAccessedTime", lastAccessedTime);
        object.put("maxInactiveInterval", maxInactiveInterval);
        object.put("isNew", isNew);
        object.put("isValid", isValid);
        object.put("thisAccessedTime", thisAccessedTime);
        object.put("id", id);

        if (manager.getContainer().getLogger().isDebugEnabled())
            manager.getContainer().getLogger().debug("writeObject() storing session " + id);

        object.put("attributes", convertMapToDBObject(attributes, new HashMap<Object, DBObject>()));
        return object;
	}
	
	@SuppressWarnings("unchecked")
	protected DBObject getDBObject(Object target, Map<Object, DBObject> cached)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if(target instanceof Map){
			return convertMapToDBObject((Map)target, cached);	
		}else if(target instanceof Iterable){
			return convertIterableToDBObject((Iterable)target, cached);
		}else{
			return convertPojoToDBObject(target, cached);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected DBObject convertMapToDBObject(Map map, Map<Object, DBObject> cached) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		if(cached.containsKey(map)){
			return cached.get(map);
		}
		
		DBObject ret = new BasicDBObject();
		for(Object key : map.keySet()){
			Object target = map.get(key);
			if(isConvertableValue(target)){
				ret.put(key.toString(), target);
			}else{
				ret.put(key.toString(), getDBObject(target, cached));
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	protected DBObject convertIterableToDBObject(Iterable target, Map<Object, DBObject> cached) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if(cached.containsKey(target)){
			return cached.get(target);
		}

		BasicDBList list = new BasicDBList();
		for(Object object : (Iterable)target){
			if(isConvertableValue(object)){
				list.add(object);
			}else{
				list.add(getDBObject(object, cached));
			}
		}
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(COLLECTION_VALUE, list);
		dbObject.put(COLLECTION_CLASS_NAME, target.getClass().getName());
		cached.put(target, dbObject);
		return dbObject;
	}

	@SuppressWarnings("unchecked")
	protected DBObject convertPojoToDBObject(Object object, Map<Object, DBObject> cached) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		if(cached.containsKey(object)){
			return cached.get(object);
		}
		Map nestedMap = PropertyUtils.describe(object);
		nestedMap.put(CLASS_NAME, object.getClass().getName());
		return convertMapToDBObject(nestedMap, cached);	
	}
	
	protected boolean isConvertableValue(Object val){
        return (
    			val == null
    			|| val instanceof Date
    			|| val instanceof Number
    			|| val instanceof String
    			|| val instanceof ObjectId
    			|| val instanceof BSONObject
    			|| val instanceof Boolean
    			|| val instanceof Pattern
    			|| val instanceof byte[]
    			|| val instanceof Binary
    			|| val instanceof UUID
    			|| val.getClass().isArray()
    			|| val instanceof Symbol
    			|| val instanceof BSONTimestamp
    			|| val instanceof CodeWScope
    			|| val instanceof Code
    	);
	}
}
