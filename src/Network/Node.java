package Network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import Start.Start;

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
//			// In order to join, the node sends a FIND_NODE to the bootstrap
//			Peer[] list = kad.FIND_NODE_RPC(this, boot);

			Peer[] list = this.lookup(this.peer);

			// Then insert the results to its own DHT
			this.insert(list);

			// Perform the lookup
			Peer[] lookup_list = this.lookup(this.peer);

			for (Peer x : lookup_list) {
				this.insert(x);
			}
		}
	}

	public LinkedList<String> getEdges() {
		LinkedList<Peer> tmp = new LinkedList<Peer>();

		for (int i = 0; i < Start.bit; i++) {
			if (this.DHT.bucket[i] != null) {
				tmp.addAll(this.DHT.bucket[i]);
			}
		}

		LinkedList<String> result = new LinkedList<String>();

		for (Peer x : tmp) {
			result.add(this.getID() + "," + x.getID());
		}

		return result;
	}

	private Peer[] lookup(Peer peer) {

		HashSet<Peer> alreadyVisited = new HashSet<Peer>();
		HashSet<Peer> result = new HashSet<Peer>();
		boolean somethingToMerge = true;

		Peer[] alphaDHT = getAlphaPeersFromDHT(Start.alpha);

		for (Peer x : alphaDHT) {
			Peer[] ret_list = kad.FIND_NODE_RPC(this, x);

			if (ret_list != null) {
				result.addAll(Arrays.asList(ret_list));
			}
		}

		do {
			if (result.size() == 0) {
				somethingToMerge = false;
			} else {
				int maxSize = Math.min(Start.alpha, result.size());
				Peer[] alphaLIST = result.toArray(new Peer[maxSize]);

				for (Peer x : alphaLIST) {
					if (!alreadyVisited.contains(x)) {
						Node sender = kad.getNodeFromPeer(x);
						Peer[] ret_list = kad.FIND_NODE_RPC(sender, peer);
						alreadyVisited.add(x);

						if (ret_list != null) {
							somethingToMerge = result.addAll(Arrays.asList(ret_list));
						}
					}
				}
			}
		} while (somethingToMerge);

		result.addAll(alreadyVisited);

		return result.toArray(new Peer[result.size()]);
	}

	private Peer[] getAlphaPeersFromDHT(int alpha) {
		LinkedList<Peer> list = new LinkedList<Peer>();
		int i = 0;
		int maxSize = Math.min(alpha, this.DHT.active_nodes);

		while (list.size() < maxSize) {
			if (this.DHT.bucket[i] != null) {
				list.addAll(this.DHT.bucket[i]);
			}
			i++;
		}
		return list.toArray(new Peer[list.size()]);
	}

	public Peer[] findKClosest(Peer peer, int k) {

		int log2;

		if (Start.SHA1) {
			BigInteger peerID = new BigInteger(peer.getID(), 16);
			log2 = (int) (Math.floor(Math.log(peerID.doubleValue()) / Math.log(2)));
		} else {
			int peerID = Integer.parseInt(getID());
			log2 = (int) (Math.floor(Math.log(peerID / Math.log(2))));
		}
		int sizeAtLog = 0;
		Peer[] ret = null;

		if (this.DHT.bucket[log2] != null) {
			sizeAtLog = this.DHT.bucket[log2].size();
		}
		// if the bucket contains K elements return the entire bucket
		if (sizeAtLog == k) {
			ret = this.DHT.bucket[log2].toArray(new Peer[k]);
		}

		// if the DHT contains less(or =) elements than K, then return all nodes it owns
		else if (DHT.active_nodes <= k) {
			ret = getAllPeers();
		}

		// if there are more than K elements but the selected bucket does not have K
		// elements
		// then select K elements from the neighbours of the bucket at position log2
		else if ((DHT.active_nodes > k) && (sizeAtLog < k)) {
			ret = getNeighbourPeers(log2);
		}

		// delete the caller of the procedure if for some reasons is in the list
		HashSet<Peer> list = new HashSet<Peer>(Arrays.asList(ret));
		if (list.contains(peer)) {
			list.remove(peer);
		}

		// since now I know this peer, I add it to its own DHT
		this.insert(peer);

		return list.toArray(new Peer[list.size()]);
	}

	private Peer[] getNeighbourPeers(int bucket_position) {
		int k = Start.bucket_size;
		HashSet<Peer> tmp = new HashSet<Peer>();

		boolean go_up = true;
		int offset = 0;

		int maxPossibleSize = Math.min(this.DHT.active_nodes, k);

		while (tmp.size() < maxPossibleSize) {
			int pos;
			if (go_up) {
				pos = bucket_position - offset;
			} else {
				pos = bucket_position + offset;
				offset++;
			}

			if ((pos >= 0) && (pos < Start.bit)) {
				if (this.DHT.bucket[pos] != null) {
					tmp.addAll(this.DHT.bucket[pos]);
				}
			}

			go_up = !go_up;
		}

		Peer[] lst = tmp.toArray(new Peer[tmp.size()]);

		return lst;
	}

	private Peer[] getAllPeers() {
		HashSet<Peer> tmp = new HashSet<Peer>();

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
		// Verify that I do not insert myself in my own DHT
		if (this.getID().equals(peer.getID())) {
			return;
		}

		int log2;

		if (Start.SHA1) {
			BigInteger nodeID = new BigInteger(this.getID(), 16);
			BigInteger peerID = new BigInteger(peer.getID(), 16);
			BigInteger xor = nodeID.xor(peerID);
			log2 = (int) (Math.floor(Math.log(xor.doubleValue()) / Math.log(2)));
		} else {
			int xor = Integer.parseInt(this.getID()) ^ Integer.parseInt(peer.getID());
			log2 = (int) (Math.floor(Math.log(xor / Math.log(2))));
		}
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
				bucket[pos].add(peer);
			}
			if (!bucket[pos].contains(peer)) {
				bucket[pos].add(peer);
				active_nodes++;
			}
		}

	}
}
