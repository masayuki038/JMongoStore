package net.wrap_trap.tomcat.session;

import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;

public class MongoManager extends StandardManager {

	@Override
	protected StandardSession getNewSession() {
		return new MongoSession(this);
	}
}
