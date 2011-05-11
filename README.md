MongoStore is a Store class to persistent sessions of Tomcat to MongoDB.

## Usage

1. Copy mongo-store.jar and mongo-java-driver.jar to ${CATALINA_HOME}/lib.
2. Setting to use the PersistentManager in Context. for example,
	<Context antiResourceLocking="false" privileged="true" useHttpOnly="true">
		<Manager className="org.apache.catalina.session.PersistentManager" 
			debug="0" 
			saveOnRestart="true" 
			maxActiveSessions=".." 
			minIdleSwap=".." 
			maxIdleSwap=".." 
			maxIdleBackup=".."
		>
			<Store className="net.wrap_trap.tomcat.session.MongoStore"
				host="localhost"
				databaseName="session_store"
				collectionName="sessions"
			/>
		</Manager> 
	</Context>

## Parameters of MongoStore.

* host
* port
* databaseName
* collectionName

## License

ASL 2.0
