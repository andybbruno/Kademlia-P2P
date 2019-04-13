package Network;

import java.math.BigInteger;
import java.util.LinkedList;

import Main.Start;

/**
 * @author Andrea Bruno
 *
 */
public class Node {

	private Host host = new Host();
	private RoutingTable DHT = new RoutingTable();
	private Kademlia kad;
	private Host boot;

	public Node(Kademlia kad) {
		this.kad = kad;
		boot = connect(kad);
		
	}

	private Host connect(Kademlia kad) {
		// Get the bootstrap node
		Host bootstrap = kad.getBootstrap(this.host);

		if (bootstrap != null) {
			this.insert(bootstrap);
		}
		
		return bootstrap;
	}

	String getID() {
		return host.getID();
	}

	public String toString() {
		return this.host.toString();
	}

	// insert host into the DHT of node in the right position
	void insert(Host host) {

		BigInteger nodeID = new BigInteger(this.getID(), 16);
		BigInteger peerID = new BigInteger(host.getID(), 16);
		BigInteger xor = nodeID.xor(peerID);

		int log2 = (int) (Math.floor(Math.log(xor.doubleValue()) / Math.log(2)));

		this.DHT.insert(host, log2);

		return;
	}

	class RoutingTable {
		private int bit = Start.bit;
		private int bucket_size = Start.bucket_size;

		private LinkedList<Host>[] bucket = new LinkedList[bit];

		void insert(Host host, int pos) {
			if (bucket[pos] == null) {
				bucket[pos] = new LinkedList<Host>();
			}

			if (bucket[pos].size() >= bucket_size) {
				bucket[pos].removeLast();
			}
			bucket[pos].add(host);
		}

	}
}
