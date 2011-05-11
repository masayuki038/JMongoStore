package net.wrap_trap.tomcat.session;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import static net.wrap_trap.tomcat.session.DBObjectConstants.*;

public class Restorer{
	
	private Map<Object, DBObject> cached = new HashMap<Object, DBObject>();

	@SuppressWarnings("unchecked")
	public Map toMap(DBObject target) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		if(cached.containsKey(target)){
			return (Map)cached.get(target);
		}
		
		Map map = new HashMap();
		for(String key : target.keySet()){
			Object value = target.get(key);
			if(value instanceof DBObject){
				DBObject dbObject = (DBObject)value;
				if(dbObject.containsField(COLLECTION_CLASS_NAME)){
					Collection restoredCollection = toCollection(dbObject);
					map.put(key, restoredCollection);
				}else{
					Object object = toObject((DBObject)value);
					map.put(key, object);
				}
			}else{
				map.put(key, value);
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Collection toCollection(DBObject target) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if(cached.containsKey(target)){
			return (Collection)cached.get(target);
		}
		List<Object> list = new ArrayList<Object>();
		for(Object o : (BasicDBList)target.get(COLLECTION_VALUE)){
			if(o instanceof DBObject){
				DBObject dbObject = (DBObject)o;
				if(dbObject.containsField(COLLECTION_CLASS_NAME)){
					Collection restoredCollection = toCollection(dbObject);
					list.add(restoredCollection);
				}else{
					Object object = toObject((DBObject)o);
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
	public Object toObject(DBObject dbObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException{
		Map restoredMap = toMap((DBObject)dbObject);
		if(restoredMap.containsKey(CLASS_NAME)){
			String className = (String)restoredMap.get(CLASS_NAME);
			Class<?> clazz = Class.forName(className);
			Object restoredObject = (Serializable)clazz.newInstance();
			PropertyUtils.copyProperties(restoredObject, restoredMap);
			return restoredObject;
		}
		return restoredMap;
	}
}
