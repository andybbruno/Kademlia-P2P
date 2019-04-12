package Network;

import java.util.LinkedList;
import java.util.Random;

import DataStructures.RoutingTable;

/**
 * @author Andrea Bruno
 *
 */
public class Node extends Peer {

	private RoutingTable DHT;
	private Kademlia kad;

	public Node(Kademlia kad) {
		connect(kad);
	}

	private void connect(Kademlia kad) {
		Peer boostrap = kad.getBootstrap(this);
		// TODO fill the DHT
	}

	// public String toString() {
	// String bin_ID = String.format("%" + Main.bit + "s",
	// Integer.toBinaryString(this.id)).replace(' ', '0');
	// return "<" + this.id + "," + bin_ID + "," + this.IP + ":" + this.port + ">";
	// // return "<" + this.id + "," + this.IP + ":" + this.port + ">";
	// }
	//
	// public boolean equals(Node node) {
	// return this.id == node.getID();
	// }
}
