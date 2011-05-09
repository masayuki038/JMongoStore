package net.wrap_trap.tomcat.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;

public class MongoManager extends StandardManager {
	
	private PersistentManager persistentManager;
	

	public void add(Session session) {
		persistentManager.add(session);
	}


	public void addLifecycleListener(LifecycleListener listener) {
		persistentManager.addLifecycleListener(listener);
	}


	public void addPropertyChangeListener(PropertyChangeListener listener) {
		persistentManager.addPropertyChangeListener(listener);
	}


	public void backgroundProcess() {
		persistentManager.backgroundProcess();
	}


	public void changeSessionId(Session session) {
		persistentManager.changeSessionId(session);
	}


	public void clearStore() {
		persistentManager.clearStore();
	}


	public Session createSession() {
		return persistentManager.createSession();
	}


	public Session createSession(String sessionId) {
		return persistentManager.createSession(sessionId);
	}


	public void destroy() {
		persistentManager.destroy();
	}


	public boolean equals(Object obj) {
		return persistentManager.equals(obj);
	}


	public void expireSession(String sessionId) {
		persistentManager.expireSession(sessionId);
	}


	public LifecycleListener[] findLifecycleListeners() {
		return persistentManager.findLifecycleListeners();
	}


	public Session findSession(String id) throws IOException {
		return persistentManager.findSession(id);
	}


	public Session[] findSessions() {
		return persistentManager.findSessions();
	}


	public int getActiveSessions() {
		return persistentManager.getActiveSessions();
	}


	public String getAlgorithm() {
		return persistentManager.getAlgorithm();
	}


	public String getClassName() {
		return persistentManager.getClassName();
	}


	public Container getContainer() {
		return persistentManager.getContainer();
	}


	public String getCreationTime(String sessionId) {
		return persistentManager.getCreationTime(sessionId);
	}


	public long getCreationTimestamp(String sessionId) {
		return persistentManager.getCreationTimestamp(sessionId);
	}


	public MessageDigest getDigest() {
		return persistentManager.getDigest();
	}


	public boolean getDistributable() {
		return persistentManager.getDistributable();
	}


	public String getDomain() {
		return persistentManager.getDomain();
	}


	public int getDuplicates() {
		return persistentManager.getDuplicates();
	}


	public Engine getEngine() {
		return persistentManager.getEngine();
	}


	public String getEntropy() {
		return persistentManager.getEntropy();
	}


	public int getExpiredSessions() {
		return persistentManager.getExpiredSessions();
	}


	public String getInfo() {
		return persistentManager.getInfo();
	}


	public String getJvmRoute() {
		return persistentManager.getJvmRoute();
	}


	public String getLastAccessedTime(String sessionId) {
		return persistentManager.getLastAccessedTime(sessionId);
	}


	public long getLastAccessedTimestamp(String sessionId) {
		return persistentManager.getLastAccessedTimestamp(sessionId);
	}


	public int getMaxActive() {
		return persistentManager.getMaxActive();
	}


	public int getMaxActiveSessions() {
		return persistentManager.getMaxActiveSessions();
	}


	public int getMaxIdleBackup() {
		return persistentManager.getMaxIdleBackup();
	}


	public int getMaxIdleSwap() {
		return persistentManager.getMaxIdleSwap();
	}


	public int getMaxInactiveInterval() {
		return persistentManager.getMaxInactiveInterval();
	}


	public int getMinIdleSwap() {
		return persistentManager.getMinIdleSwap();
	}


	public String getName() {
		return persistentManager.getName();
	}


	public ObjectName getObjectName() {
		return persistentManager.getObjectName();
	}


	public int getProcessExpiresFrequency() {
		return persistentManager.getProcessExpiresFrequency();
	}


	public long getProcessingTime() {
		return persistentManager.getProcessingTime();
	}


	public Random getRandom() {
		return persistentManager.getRandom();
	}


	public String getRandomClass() {
		return persistentManager.getRandomClass();
	}


	public String getRandomFile() {
		return persistentManager.getRandomFile();
	}


	public int getRejectedSessions() {
		return persistentManager.getRejectedSessions();
	}


	public boolean getSaveOnRestart() {
		return persistentManager.getSaveOnRestart();
	}


	public HashMap getSession(String arg0) {
		return persistentManager.getSession(arg0);
	}


	public String getSessionAttribute(String sessionId, String key) {
		return persistentManager.getSessionAttribute(sessionId, key);
	}


	public int getSessionAverageAliveTime() {
		return persistentManager.getSessionAverageAliveTime();
	}


	public int getSessionCounter() {
		return persistentManager.getSessionCounter();
	}


	public int getSessionCreateRate() {
		return persistentManager.getSessionCreateRate();
	}


	public int getSessionExpireRate() {
		return persistentManager.getSessionExpireRate();
	}


	public int getSessionIdLength() {
		return persistentManager.getSessionIdLength();
	}


	public int getSessionMaxAliveTime() {
		return persistentManager.getSessionMaxAliveTime();
	}


	public Store getStore() {
		return persistentManager.getStore();
	}


	public int hashCode() {
		return persistentManager.hashCode();
	}


	public void init() {
		persistentManager.init();
	}


	public boolean isLoaded(String arg0) {
		return persistentManager.isLoaded(arg0);
	}


	public String listSessionIds() {
		return persistentManager.listSessionIds();
	}


	public void load() {
		persistentManager.load();
	}


	public void postDeregister() {
		persistentManager.postDeregister();
	}


	public void postRegister(Boolean registrationDone) {
		persistentManager.postRegister(registrationDone);
	}


	public void preDeregister() throws Exception {
		persistentManager.preDeregister();
	}


	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		return persistentManager.preRegister(server, name);
	}


	public void processExpires() {
		persistentManager.processExpires();
	}


	public void processPersistenceChecks() {
		persistentManager.processPersistenceChecks();
	}


	public void propertyChange(PropertyChangeEvent arg0) {
		persistentManager.propertyChange(arg0);
	}


	public void remove(Session session) {
		persistentManager.remove(session);
	}


	public void removeLifecycleListener(LifecycleListener listener) {
		persistentManager.removeLifecycleListener(listener);
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
		persistentManager.removePropertyChangeListener(listener);
	}


	public void removeSuper(Session session) {
		persistentManager.removeSuper(session);
	}


	public void setAlgorithm(String algorithm) {
		persistentManager.setAlgorithm(algorithm);
	}


	public void setContainer(Container container) {
		persistentManager.setContainer(container);
	}


	public void setDistributable(boolean distributable) {
		persistentManager.setDistributable(distributable);
	}


	public void setDuplicates(int duplicates) {
		persistentManager.setDuplicates(duplicates);
	}


	public void setEntropy(String entropy) {
		persistentManager.setEntropy(entropy);
	}


	public void setExpiredSessions(int expiredSessions) {
		persistentManager.setExpiredSessions(expiredSessions);
	}


	public void setMaxActive(int maxActive) {
		persistentManager.setMaxActive(maxActive);
	}


	public void setMaxActiveSessions(int max) {
		persistentManager.setMaxActiveSessions(max);
	}


	public void setMaxIdleBackup(int backup) {
		persistentManager.setMaxIdleBackup(backup);
	}


	public void setMaxIdleSwap(int max) {
		persistentManager.setMaxIdleSwap(max);
	}


	public void setMaxInactiveInterval(int interval) {
		persistentManager.setMaxInactiveInterval(interval);
	}


	public void setMinIdleSwap(int min) {
		persistentManager.setMinIdleSwap(min);
	}


	public void setProcessExpiresFrequency(int processExpiresFrequency) {
		persistentManager.setProcessExpiresFrequency(processExpiresFrequency);
	}


	public void setProcessingTime(long processingTime) {
		persistentManager.setProcessingTime(processingTime);
	}


	public void setRandomClass(String randomClass) {
		persistentManager.setRandomClass(randomClass);
	}


	public void setRandomFile(String arg0) {
		persistentManager.setRandomFile(arg0);
	}


	public void setRejectedSessions(int rejectedSessions) {
		persistentManager.setRejectedSessions(rejectedSessions);
	}


	public void setSaveOnRestart(boolean saveOnRestart) {
		persistentManager.setSaveOnRestart(saveOnRestart);
	}


	public void setSessionAverageAliveTime(int sessionAverageAliveTime) {
		persistentManager.setSessionAverageAliveTime(sessionAverageAliveTime);
	}


	public void setSessionCounter(int sessionCounter) {
		persistentManager.setSessionCounter(sessionCounter);
	}


	public void setSessionIdLength(int idLength) {
		persistentManager.setSessionIdLength(idLength);
	}


	public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
		persistentManager.setSessionMaxAliveTime(sessionMaxAliveTime);
	}


	public void setStore(Store store) {
		persistentManager.setStore(store);
	}


	public void start() throws LifecycleException {
		persistentManager.start();
	}


	public void stop() throws LifecycleException {
		persistentManager.stop();
	}


	public String toString() {
		return persistentManager.toString();
	}


	public void unload() {
		persistentManager.unload();
	}


	@Override
	protected StandardSession getNewSession() {
		return new MongoSession(this);
	}
}
