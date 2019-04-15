package Network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;

import Main.Start;

/**
 * @author Andrea Bruno
 *
 */
public class Node {

	private Peer peer = new Peer();
	private RoutingTable DHT = new RoutingTable();
	private Kademlia kad;
	private Peer boot;

	public Node(Kademlia kad) {
		this.kad = kad;
		boot = connect(kad);

		if (boot != null) {
			// In order to join, the node sends a FIND_NODE to the bootstrap
			Peer[] list = kad.FIND_NODE_RPC(this, boot);

			// Then insert the results to its own DHT
			this.insert(list);

			// Perform the lookup
			this.lookup(this.peer);

		}

		System.out.println();
	}

	private Peer[] lookup(Peer peer) {
		String ID = peer.getID();
		Peer[] alphaPeers = getAlphaPeers(Start.alpha);
		Peer[] tmp;
				
		for (Peer x : alphaPeers) {
			// aggiungi a tmp x.FIND_NODE_RPC(this, boot);
		}

		return null;
	}

	private Peer[] getAlphaPeers(int alpha) {
		return findKClosest(this.peer.getID(), alpha);
	}

	public Peer[] findKClosest(String ID, int k) {
		BigInteger nodeID = new BigInteger(ID, 16);
		int log2 = (int) (Math.floor(Math.log(nodeID.doubleValue()) / Math.log(2)));

		int sizeAtLog = 0;

		if (this.DHT.bucket[log2] != null) {
			sizeAtLog = this.DHT.bucket[log2].size();
		}
		// if the bucket contains K elements return the entire bucket
		if (sizeAtLog == k) {
			return this.DHT.bucket[log2].toArray(new Peer[k]);
		}

		// if the DHT contains less(or =) elements than K, then return all nodes it owns
		else if (DHT.active_nodes <= k) {
			return getAllPeers();
		}

		// if there are more than K elements but the selected bucket does not have K
		// elements
		// then select K elements from the neighbours of the bucket at position log2
		else if ((DHT.active_nodes > k) && (sizeAtLog < k)) {
			return getNeighbourPeers(log2);
		}

		return null;
	}

	private Peer[] getNeighbourPeers(int bucket_position) {
		int k = Start.bucket_size;
		ArrayList<Peer> tmp = new ArrayList<Peer>();

		tmp.addAll(this.DHT.bucket[bucket_position]);

		boolean go_up = true;
		int offset = 1;

		while (tmp.size() < k) {
			int pos;
			if (go_up) {
				pos = bucket_position - offset;
			} else {
				pos = bucket_position + offset;
			}

			if (this.DHT.bucket[pos] != null) {
				tmp.addAll(this.DHT.bucket[pos]);
			}

			offset++;
		}

		Peer[] lst = tmp.toArray(new Peer[tmp.size()]);

		return lst;
	}

	private Peer[] getAllPeers() {
		ArrayList<Peer> tmp = new ArrayList<Peer>();

		for (LinkedList<Peer> x : this.DHT.bucket) {
			if (x != null) {
				tmp.addAll(x);
			}
		}
		Peer[] lst = tmp.toArray(new Peer[tmp.size()]);

		return lst;
	}

	private Peer connect(Kademlia kad) {
		// Get the bootstrap node
		Peer bootstrap = kad.getBootstrap(this);

		if (bootstrap != null) {
			this.insert(bootstrap);
		}

		return bootstrap;
	}

	String getID() {
		return peer.getID();
	}

	public Peer getPeer() {
		return this.peer;
	}

	public String toString() {
		return this.peer.toString();
	}

	// insert peer into the DHT of node in the right position
	private void insert(Peer peer) {

		BigInteger nodeID = new BigInteger(this.getID(), 16);
		BigInteger peerID = new BigInteger(peer.getID(), 16);
		BigInteger xor = nodeID.xor(peerID);

		int log2 = (int) (Math.floor(Math.log(xor.doubleValue()) / Math.log(2)));

		this.DHT.put(peer, log2);

	}

	private void insert(Peer[] list) {
		for (Peer x : list) {
			this.insert(x);
		}
	}

	class RoutingTable {
		private int bit = Start.bit;
		private int bucket_size = Start.bucket_size;
		private int active_nodes = 0;

		private LinkedList<Peer>[] bucket = new LinkedList[bit];

		private void put(Peer peer, int pos) {
			if (bucket[pos] == null) {
				bucket[pos] = new LinkedList<Peer>();
			}

			if (bucket[pos].size() >= bucket_size) {
				bucket[pos].removeLast();
			}
			if (!bucket[pos].contains(peer)) {
				bucket[pos].add(peer);
				active_nodes++;
			}
		}

	}
}
