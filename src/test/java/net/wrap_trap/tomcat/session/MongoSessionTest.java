package net.wrap_trap.tomcat.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.juli.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
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
		manager = createMockManager();
		mongoStore.setManager(manager);
		mongoStore.start();
		mongoStore.clear();
	}
	
	@After
	public void tearDown() throws IOException{
	}
	
	@Test
	public void testMongoSessionToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		long now = System.currentTimeMillis();
		MongoSession mongoSession = createMongoSession(now);
		DBObject object = mongoSession.createDBObject();
		String id = (String)object.get("id");

		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(id);

		Assert.assertEquals(Long.toString(now), session.getId());
		Assert.assertEquals(now, session.getCreationTime());
		Assert.assertEquals(30, session.getMaxInactiveInterval());
		Assert.assertEquals(false, session.isNew());
		Assert.assertEquals(true, session.isValid());
	}
	
	@Test
	public void testStandardSessionToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		long now = System.currentTimeMillis();
		StandardSession standardSession = createMongoSession(now);
		DBObject object = MongoSession.createDBObject(standardSession);
		String id = (String)object.get("id");

		mongoStore.save(standardSession);
		MongoSession session = (MongoSession)mongoStore.load(id);

		Assert.assertEquals(Long.toString(now), session.getId());
		Assert.assertEquals(now, session.getCreationTime());
		Assert.assertEquals(30, session.getMaxInactiveInterval());
		Assert.assertEquals(false, session.isNew());
		Assert.assertEquals(true, session.isValid());
	}

	
	@Test
	public void testMongoSessionAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		MongoSession mongoSession = createMongoSession();
		mongoSession.setAttribute("foo", "bar");
		mongoSession.setAttribute("hoge", 1);		
		DBObject object = mongoSession.createDBObject();
		String id = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(id);

		Map map = (Map)object.get("attributes");
		Assert.assertNotNull(map);
		Assert.assertEquals("bar", session.getAttribute("foo"));
		Assert.assertEquals(1, session.getAttribute("hoge"));		
	}
	
	@Test
	public void testPojoAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		MongoSession mongoSession = createMongoSession();
		Foo foo = new Foo();
		foo.setId(Long.toString(System.currentTimeMillis()));
		foo.setCount(30);
		mongoSession.setAttribute("foo", foo);
		DBObject object = mongoSession.createDBObject();
		String id = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(id);
		
		Foo dbFoo = (Foo)session.getAttribute("foo");
		checkFoo(foo, dbFoo);
	}

	@Test
	public void testNestedListAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		MongoSession mongoSession = createMongoSession();
		Bar bar = new Bar();
		bar.setId(Long.toString(System.currentTimeMillis()));
		// Arrays.asList will fail when restoring collection objects from MongoDB.
		bar.setStringList(new ArrayList(Arrays.asList("hoge", "hogehoge")));
		
		mongoSession.setAttribute("bar", bar);
		DBObject object = mongoSession.createDBObject();
		String id = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(id);
		
		Bar dbBar = (Bar)session.getAttribute("bar");
		checkBar(bar, dbBar);		
	}

	@Test
	public void testNestedPojoAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		MongoSession mongoSession = createMongoSession();
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
		DBObject object = mongoSession.createDBObject();
		String fooId = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(fooId);
		
		Foo dbFoo = (Foo)session.getAttribute("foo");
		checkFoo(foo, dbFoo);
		Bar dbBar = dbFoo.getBar();		
		checkBar(bar, dbBar);		
	}

	@Test
	public void testNestedPojoListAsAttributeToDBObject() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		MongoSession mongoSession = createMongoSession();
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
		DBObject object = mongoSession.createDBObject();
		String fooId = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(fooId);
		
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
		MongoSession mongoSession = createMongoSession();
		Set<String> set = new HashSet<String>();
		set.add("foo");
		set.add("bar");
		mongoSession.setAttribute("set", set);
		DBObject object = mongoSession.createDBObject();
		String id = (String)object.get("id");
		
		mongoStore.save(mongoSession);
		MongoSession session = (MongoSession)mongoStore.load(id);

		Set<String> dbSet = (Set<String>)session.getAttribute("set");
		Assert.assertEquals(set.size(), dbSet.size());
		Assert.assertTrue(dbSet.contains("foo"));
		Assert.assertTrue(dbSet.contains("bar"));
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
	
	protected MongoSession createMongoSession(){
		return createMongoSession(System.currentTimeMillis());
	}

	protected MongoSession createMongoSession(long now) {
		MongoSession mongoSession = new MongoSession(manager);
		mongoSession.setId(Long.toString(now));
		mongoSession.setCreationTime(now);
		mongoSession.setMaxInactiveInterval(30);
		mongoSession.setNew(false);
		mongoSession.setValid(true);
		return mongoSession;
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
		when(manager.createEmptySession()).thenReturn(new MongoSession(manager));
		return manager;
	}
}
