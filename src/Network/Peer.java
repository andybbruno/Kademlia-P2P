package Network;

import java.util.HashSet;

import Utility.Utility;

/**
 * The Peer class consists of tree private fields that simulates what in the
 * original paper is called [ID,IP,Port] triple. Basic functionality has been
 * developed such as {@code getID()} and {@code toString()}. There is a private
 * static HashSet that guarantees no duplicates ID.
 * 
 * @author Andrea Bruno
 *
 */
public class Peer {
	private String ID;
	private String IP_Address;
	private String port;

	// HashSet of IDs
	private static HashSet<String> alreadyUsedIDS = new HashSet<String>();

	/**
	 * The constructor can be called only by the class inside the network package
	 */
	Peer() {
		this.IP_Address = Utility.generateIP();
		this.port = Utility.generatePort();
		do {
			this.ID = Utility.generateID();
		} while (!alreadyUsedIDS.add(this.ID));
	}

	/**
	 * @return the ID of a Peer object
	 */
	public String getID() {
		return new String(this.ID);
	}

	@Override
	public String toString() {
		return this.IP_Address + ":" + this.port + "," + this.ID + " ";
	}
}
