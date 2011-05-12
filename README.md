JMongoStore is a Store class to persistent sessions of Tomcat to MongoDB.

## Usage

1. Copy mongo-store.jar and mongo-java-driver.jar to ${CATALINA_HOME}/lib.
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

## Blog Article

http://d.hatena.ne.jp/hsyd/20110511/1305139683

## License

ASL 2.0
