package Network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

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
			Peer[] list = this.lookup(this.peer.getID());

			// Then insert the results to its own DHT
			this.insert(list);

			Node bootstrap = kad.getNodeFromPeer(boot);

			// Perform the lookup on the bootstrap

			// this.lookup(RANDOM LONTANO)

			Peer[] lookup_list = this.lookup(Utility.Utility.generateID());

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

	private Peer[] lookup(String ID) {

		HashSet<Peer> alreadyVisited = new HashSet<Peer>();
		LinkedHashSet<Peer> kresult = new LinkedHashSet<Peer>();
		boolean somethingToMerge = true;

		Peer[] alphaDHT = getAlphaPeersFromDHT(Start.alpha);

		// contatta gli alfa nodi
		for (Peer x : alphaDHT) {
			Peer[] ret_list = kad.FIND_NODE_RPC(this, x, ID);

			if (ret_list != null) {
				kresult.addAll(Arrays.asList(ret_list));
			}
		}

		// ordina le risposte ricevute dagli alfa nodi
		kresult = new LinkedHashSet<Peer>(getKOrdered(kresult));

		alreadyVisited.addAll(Arrays.asList(alphaDHT));

		do {
			if (kresult.size() == 0) {
				somethingToMerge = false;
			} else {
				// seleziono alfa nodi da kresult
				int maxSize = Math.min(Start.alpha, kresult.size());
				Peer[] alphaLIST = kresult.stream().limit(maxSize).toArray(Peer[]::new);

				ArrayList<Peer> appendList = new ArrayList<Peer>();

				// contatto gli alfa di kresult
				for (Peer x : alphaLIST) {
					// se non è un nodo già contattato
					if (!alreadyVisited.contains(x)) {
						// esegui la find_node
						Peer[] ret_list = kad.FIND_NODE_RPC(this, x, ID);
						// e imposta questo come visitato
						alreadyVisited.add(x);
						if (ret_list != null) {
							appendList.addAll(Arrays.asList(ret_list));
						}
					}
				}

				// il risultato di alfa iterazioni saranno alfa * K nodi e saranno memorizzati
				// in appendList
				// se appendList contiene qualcosa di nuovo, la addAll() produrrà true
				if (appendList != null) {
					somethingToMerge = kresult.addAll(appendList);
				}
			}

			kresult = new LinkedHashSet<Peer>(getKOrdered(kresult));

		} while (somethingToMerge);

		// SERVE???
		// kresult.addAll(alreadyVisited);

		return kresult.toArray(new Peer[kresult.size()]);
	}

	private ArrayList<Peer> getKOrdered(HashSet<Peer> kresult) {
		ArrayList<Peer> arr = new ArrayList<Peer>(kresult);
		HashMap<Peer, Integer> xorMap = new HashMap<Peer, Integer>();
		
		//elimino me stesso dalla lista
		arr.remove(this.peer);

		// creo la mappa chiave valore basata sullo xor tra (this) e la lista che arriva
		for (Peer px : arr) {
			int thisID = Integer.parseInt(this.getID());
			int pxID = Integer.parseInt(px.getID());
			xorMap.put(px, (thisID ^ pxID));
		}

		arr.sort(new Comparator<Peer>() {

			@Override
			public int compare(Peer o1, Peer o2) {
				int val_o1 = xorMap.get(o1);
				int val_o2 = xorMap.get(o2);
				return val_o1 - val_o2;
			}

		});

		if (arr.size() > Start.bucket_size) {
			arr = (ArrayList<Peer>) arr.stream().limit(Start.bucket_size).collect(Collectors.toList());
		}
		return arr;
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

	public Peer[] findKClosest(String ID, int k) {

		int log2;

		if (Start.SHA1) {
			BigInteger peerID = new BigInteger(ID, 16);
			log2 = (int) (Math.floor(Math.log(peerID.doubleValue()) / Math.log(2)));
		} else {
			int peerID = Integer.parseInt(ID);
			log2 = (int) (Math.floor(Math.log(peerID / Math.log(2))));

			if (log2 < 1) {
				log2 = 0;
			}
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
		if (list.contains(ID)) {
			list.remove(ID);
		}

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

	@Override
	public String toString() {
		return this.peer.toString();
	}

	// insert peer into the DHT of node in the right position
	void insert(Peer peer) {
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
