package Network;

import Utility.Utility;

public class Peer {
	private String ID;
	private String IP_Address;
	private String port;

	Peer() {
		this.IP_Address = Utility.generateIP();
		this.port = Utility.generatePort();
//		this.ID = "" + (new Random().nextInt(64));
		this.ID = Utility.generateID(this.IP_Address + ":" + this.port);
	}

	public String toString() {
		return this.IP_Address + ":" + this.port + "," + this.ID;
	}

	public String getID() {
		return new String(this.ID);
	}

}
