package org.telcomp.events;

import java.util.HashMap;
import java.util.Random;
import java.io.Serializable;

public final class EndReceiveIMTelcoServiceEvent implements Serializable {
	
	private final long id;
	private static final long serialVersionUID = 1L;
	
	private String fromUserUri;
	private String message;

	public EndReceiveIMTelcoServiceEvent(HashMap<String, ?> hashMap) {
		id = new Random().nextLong() ^ System.currentTimeMillis();
		this.fromUserUri = (String) hashMap.get("fromUserUri");
		this.message = (String) hashMap.get("message");
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		return (o instanceof EndReceiveIMTelcoServiceEvent) && ((EndReceiveIMTelcoServiceEvent)o).id == id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String getFromUserUri(){
		return this.fromUserUri;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public String toString() {
		return "endReceiveIMEvent[" + hashCode() + "]";
	}
}
