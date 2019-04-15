package Network;

import java.util.HashSet;

import Utility.Utility;

public class Peer {
	private String ID;
	private String IP_Address;
	private String port;

	private static HashSet<String> alreadyUsedIDS = new HashSet<String>();

	Peer() {
		this.IP_Address = Utility.generateIP();
		this.port = Utility.generatePort();
//		this.ID = Utility.generateHASH(this.IP_Address + ":" + this.port);
		do {
			this.ID = Utility.generateID();
		} while (!alreadyUsedIDS.add(this.ID));
	}

	public String toString() {
		return this.IP_Address + ":" + this.port + "," + this.ID;
	}

	public String getID() {
		return new String(this.ID);
	}

}
