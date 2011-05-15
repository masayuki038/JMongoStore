package net.wrap_trap.tomcat.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardSession;
import org.apache.juli.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import static org.mockito.Mockito.*; 

public class MongoSessionTest {

	private MongoStore mongoStore;
	private Manager manager;
	
	@Before
	public void setUp() throws MongoException, LifecycleException, IOException{
		mongoStore = new MongoStore();
		mongoStore.setHost("localhost");
		mongoStore.setDatabaseName("test");
		mongoStore.setCollectionName("sessions");
		mongoStore.setCollectionNameForRemoved("removed_sessions");
		manager = createMockManager();
		mongoStore.setManager(manager);
		mongoStore.start();
		mongoStore.clear();
		DBCollection collectionForRemoved = mongoStore.getCollectionForRemoved();
		if(collectionForRemoved != null){
			collectionForRemoved.drop();
		}
	}
	
	@After
	public void tearDown() throws IOException{
	}
	
	@Test
	public void testMongoSessionToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		long now = System.currentTimeMillis();
		StandardSession mongoSession = createStandardSession(now);
		String id = mongoSession.getId();

		mongoStore.save(mongoSession);
		StandardSession session = (StandardSession)mongoStore.load(id);

		Assert.assertEquals(Long.toString(now), session.getId());
		Assert.assertEquals(now, session.getCreationTime());
		Assert.assertEquals(30, session.getMaxInactiveInterval());
		Assert.assertEquals(false, session.isNew());
		Assert.assertEquals(true, session.isValid());
	}
	
	@Test
	public void testStandardSessionToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		long now = System.currentTimeMillis();
		StandardSession standardSession = createStandardSession(now);
		String id = standardSession.getId();

		mongoStore.save(standardSession);
		StandardSession session = (StandardSession)mongoStore.load(id);

		Assert.assertEquals(Long.toString(now), session.getId());
		Assert.assertEquals(now, session.getCreationTime());
		Assert.assertEquals(30, session.getMaxInactiveInterval());
		Assert.assertEquals(false, session.isNew());
		Assert.assertEquals(true, session.isValid());
	}

	
	@Test
	public void testMongoSessionAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession standardSession = createStandardSession();
		standardSession.setAttribute("foo", "bar");
		standardSession.setAttribute("hoge", 1);		
		String id = standardSession.getId();
		
		mongoStore.save(standardSession);
		StandardSession session = (StandardSession)mongoStore.load(id);

		Assert.assertEquals("bar", session.getAttribute("foo"));
		Assert.assertEquals(1, session.getAttribute("hoge"));		
	}
	
	@Test
	public void testPojoAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession standardSession = createStandardSession();
		Foo foo = new Foo();
		foo.setId(Long.toString(System.currentTimeMillis()));
		foo.setCount(30);
		standardSession.setAttribute("foo", foo);
		String id = standardSession.getId();
		
		mongoStore.save(standardSession);
		StandardSession session = (StandardSession)mongoStore.load(id);
		
		Foo dbFoo = (Foo)session.getAttribute("foo");
		checkFoo(foo, dbFoo);
	}

	@Test
	public void testNestedListAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession standardSession = createStandardSession();
		Bar bar = new Bar();
		bar.setId(Long.toString(System.currentTimeMillis()));
		// Arrays.asList will fail when restoring collection objects from MongoDB.
		bar.setStringList(new ArrayList(Arrays.asList("hoge", "hogehoge")));
		
		standardSession.setAttribute("bar", bar);
		String id = standardSession.getId();
		
		mongoStore.save(standardSession);
		StandardSession session = (StandardSession)mongoStore.load(id);
		
		Bar dbBar = (Bar)session.getAttribute("bar");
		checkBar(bar, dbBar);		
	}

	@Test
	public void testNestedPojoAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession mongoSession = createStandardSession();
		String id = Long.toString(System.currentTimeMillis());
		Foo foo = new Foo();
		foo.setId(id);
		foo.setCount(20);
		Bar bar = new Bar();
		bar.setId("bar_" + id);
		// Arrays.asList will fail when restoring collection objects from MongoDB.
		bar.setStringList(new ArrayList(Arrays.asList("hoge", "hogehoge")));
		foo.setBar(bar);
		
		mongoSession.setAttribute("foo", foo);
		String fooId = mongoSession.getId();
		
		mongoStore.save(mongoSession);
		StandardSession session = (StandardSession)mongoStore.load(fooId);
		
		Foo dbFoo = (Foo)session.getAttribute("foo");
		checkFoo(foo, dbFoo);
		Bar dbBar = dbFoo.getBar();		
		checkBar(bar, dbBar);		
	}

	@Test
	public void testNestedPojoListAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession mongoSession = createStandardSession();
		String id = Long.toString(System.currentTimeMillis());
		Foo foo = new Foo();
		foo.setId(id);
		foo.setCount(40);
		Bar bar1 = new Bar();
		bar1.setId("bar1_" + id);
		// Arrays.asList will fail when restoring collection objects from MongoDB.
		bar1.setStringList(new ArrayList(Arrays.asList("hoge", "hogehoge")));
		Bar bar2 = new Bar();
		bar2.setId("bar2_" + id);
		bar2.setStringList(new ArrayList(Arrays.asList("foo", "bar", "hoge")));
		foo.setBarList(new ArrayList(Arrays.asList(bar1, bar2)));
		
		mongoSession.setAttribute("foo", foo);
		String fooId = mongoSession.getId();
		
		mongoStore.save(mongoSession);
		StandardSession session = (StandardSession)mongoStore.load(fooId);
		
		Foo dbFoo = (Foo)session.getAttribute("foo");
		checkFoo(foo, dbFoo);
		Assert.assertEquals(foo.getBarList().size(), dbFoo.getBarList().size());
		for(int i = 0; i < foo.getBarList().size(); i++){
			Bar bar = foo.getBarList().get(i);
			Bar dbBar = dbFoo.getBarList().get(i);
			checkBar(bar, dbBar);		
		}
	}

	@Test
	public void testSetAsCollectionToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		StandardSession mongoSession = createStandardSession();
		Set<String> set = new HashSet<String>();
		set.add("foo");
		set.add("bar");
		mongoSession.setAttribute("set", set);
		String id = mongoSession.getId();
		
		mongoStore.save(mongoSession);
		StandardSession session = (StandardSession)mongoStore.load(id);

		Set<String> dbSet = (Set<String>)session.getAttribute("set");
		Assert.assertEquals(set.size(), dbSet.size());
		Assert.assertTrue(dbSet.contains("foo"));
		Assert.assertTrue(dbSet.contains("bar"));
	}
	
	@Test
	public void testSessionRemoved() throws IOException, ClassNotFoundException{
		StandardSession standardSession = createStandardSession();
		standardSession.setAttribute("foo", "bar");
		standardSession.setAttribute("hoge", 1);		
		String id = standardSession.getId();
		
		mongoStore.save(standardSession);
		StandardSession session = (StandardSession)mongoStore.load(id);
		Assert.assertNotNull(session);
		Assert.assertEquals(id, session.getId());
		mongoStore.remove(id);
	
		StandardSession removedSession = (StandardSession)mongoStore.load(id);
		Assert.assertNull(removedSession);
		DBCollection collectionForRemoved = mongoStore.getCollectionForRemoved();
		if(collectionForRemoved != null){
			BasicDBObject query = new BasicDBObject();
			query.put("id", id);
			DBCursor cursor = collectionForRemoved.find(query);
			while(cursor.hasNext()){
				DBObject dbObject = cursor.next();
				Assert.assertNotNull(dbObject);
				Assert.assertEquals(id, dbObject.get("id"));
				DBObject attributes = (DBObject)dbObject.get("attributes");
				Assert.assertNotNull(attributes);
				
				Assert.assertEquals(id, dbObject.get("id"));
				Assert.assertEquals("bar", attributes.get("foo"));
				Assert.assertEquals(1, attributes.get("hoge"));
			}
		}
	}

	@Test
	public void testClear() throws IOException, ClassNotFoundException{
		DBCollection collectionForRemoved = mongoStore.getCollectionForRemoved();
		if(collectionForRemoved != null){
			Assert.assertEquals(0, collectionForRemoved.getCount());
		}
		
		StandardSession standardSession = createStandardSession();
		String id1 = standardSession.getId();
		mongoStore.save(standardSession);

		standardSession = createStandardSession();
		String id2 = standardSession.getId();
		mongoStore.save(standardSession);
		
		mongoStore.clear();
		
		StandardSession removedSession = (StandardSession)mongoStore.load(id1);
		Assert.assertNull(removedSession);
		removedSession = (StandardSession)mongoStore.load(id2);
		Assert.assertNull(removedSession);
		Assert.assertEquals(0, mongoStore.getSize());
		
		if(collectionForRemoved != null){
			Assert.assertEquals(2, collectionForRemoved.getCount());
			BasicDBObject query = new BasicDBObject();
			query.put("id", id1);
			DBCursor cursor = collectionForRemoved.find(query);
			Assert.assertEquals(1, cursor.count());
			
			query.put("id", id2);
			cursor = collectionForRemoved.find(query);
			Assert.assertEquals(1, cursor.count());
		}
	}
		
	private void checkFoo(Foo foo, Foo dbFoo) {
		Assert.assertEquals(foo.getId(), dbFoo.getId());
		Assert.assertEquals(foo.getCount(), dbFoo.getCount());
	}
	
	private void checkBar(Bar bar, Bar dbBar) {
		Assert.assertEquals(bar.getId(), dbBar.getId());
		Assert.assertEquals(bar.getStringList().size(), dbBar.getStringList().size());
		Assert.assertEquals(bar.getStringList().size(), dbBar.getStringList().size());
		for(int i = 0; i < bar.getStringList().size(); i++){
			String barStr = bar.getStringList().get(i);
			String dbBarStr = dbBar.getStringList().get(i);
			Assert.assertEquals(barStr, dbBarStr);
		}
	}

	protected StandardSession createStandardSession() {
		return createStandardSession(System.currentTimeMillis());
	}

	protected StandardSession createStandardSession(long now) {
		StandardSession standardSession = new StandardSession(manager);
		standardSession.setId(Long.toString(now));
		standardSession.setCreationTime(now);
		standardSession.setMaxInactiveInterval(30);
		standardSession.setNew(false);
		standardSession.setValid(true);
		return standardSession;
	}

	protected Manager createMockManager() {
		Manager manager = mock(Manager.class);
		StandardContext context = mock(StandardContext.class);
		Log log = mock(Log.class);
		
		when(manager.getContainer()).thenReturn(context);
		when(context.getLogger()).thenReturn(log);
		when(log.isDebugEnabled()).thenReturn(false);
		when(manager.createEmptySession()).thenReturn(new StandardSession(manager));
		return manager;
	}
}
