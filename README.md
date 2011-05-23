JMongoStore is a Store class to persistent sessions of Tomcat to MongoDB.

## Make Jar

<pre><code>
mvn package -Dmaven.test.skip=true
</code></pre>

if running the tests when packaging, see <a href="https://github.com/masayuki038/JMongoStore/blob/master/src/test/java/net/wrap_trap/tomcat/session/MongoSessionTest.java">here</a> and setting up the MongoDB.

## Usage

1. Copy jars to ${CATALINA_HOME}/lib.
(mongo-store-0.0.1-SNAPSHOT.jar / monganez-0.0.1-SNAPSHOT.jar / mongo-java-driver-2.x.jar)
2. Set to use the PersistentManager in Context. for example,
<pre><code>
&lt;Context antiResourceLocking="false" privileged="true" useHttpOnly="true"&gt;
    &lt;Manager className="org.apache.catalina.session.PersistentManager" 
		saveOnRestart=".." 
		maxActiveSessions=".." 
		minIdleSwap=".." 
		maxIdleSwap=".." 
		maxIdleBackup=".."
	&gt;
		&lt;Store className="net.wrap_trap.tomcat.session.MongoStore"
			host="localhost"
			databaseName="session_store"
			collectionName="sessions"
		/&gt;
    &lt;/Manager&gt; 
&lt;/Context&gt;
</code></pre>

## Parameters

* host
* port
* databaseName
* collectionName
* collectionNameForRemoved - (optional) moving sessions to this collection instead of deleting them.

## Blog Article

http://d.hatena.ne.jp/hsyd/20110511/1305139683

## License

ASL 2.0
