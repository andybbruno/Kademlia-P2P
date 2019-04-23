package Network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

import Start.Start;

/**
 * @author Andrea Bruno
 *
 */
public class Node {

//	private static ArrayList<String> allPossibleIDs = new ArrayList<String>();

	private Peer peer = new Peer();
	private RoutingTable DHT = new RoutingTable();
	private Kademlia kad;
	private Peer boot;
	private LinkedList<Integer> depth = new LinkedList<Integer>();
	private int real_lookup = 0;
	private boolean refresh = Start.refresh;

	public Node(Kademlia kad) {
		this.kad = kad;
		boot = connect(kad);

		if (boot != null) {
			Peer[] list = this.lookup(this.peer.getID());
			

			// Then insert the results to its own DHT
			this.insert(list);

			if (refresh) {
				bucketRefresh();
			} else {
				Peer[] lookup_list = this.lookup(Utility.Utility.generateID());
				for (Peer x : lookup_list) {
					this.insert(x);
				}
			}
		}

	}

	private void bucketRefresh() {
		for (int i = 0; i < Start.bit; i++) {
			String currentID;
			BigInteger randID;
			BigInteger thisID = new BigInteger(this.getID());
			BigInteger low;
			BigInteger high;

			if (i == 0) {
				randID = BigInteger.valueOf(2).pow(i);
			} else {
				low = BigInteger.valueOf(2).pow(i);
				high = BigInteger.valueOf(2).pow(i + 1).subtract(BigInteger.valueOf(1));

				// THE SAME AS:
				// int rnd = new Random().nextInt(high - low) + low;
				randID = getRandomBigInteger(high.subtract(low)).add(low);
			}

			ArrayList<Peer> toAdd = new ArrayList<Peer>();
			currentID = thisID.xor(randID).toString();
			Peer[] tmp = this.lookup(currentID);
			toAdd.addAll(Arrays.asList(tmp));
			real_lookup++;

//			Peer[] closests = findKClosest(currentID);
//
//			for (Peer x : closests) {
//				Node receiver = kad.getNodeFromPeer(x);
//				Peer[] tmp = receiver.lookup(currentID);
//				toAdd.addAll(Arrays.asList(tmp));
//				real_lookup++;
//			}

			this.insert(toAdd.stream().limit(toAdd.size()).toArray(Peer[]::new));
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
		real_lookup++;
		HashSet<Peer> alreadyVisited = new HashSet<Peer>();
		LinkedHashSet<Peer> kresult = new LinkedHashSet<Peer>();
		boolean somethingToMerge = true;
		int tmp_depth = 0;

		Peer[] alphaDHT = getAlphaPeersFromDHT(Start.alpha);
		tmp_depth++;

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
				tmp_depth++;

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

		this.depth.add(tmp_depth);

		return kresult.toArray(new Peer[kresult.size()]);
	}

//	private ArrayList<String> computeDistances() {
//		ArrayList<String> ids = new ArrayList<String>(allPossibleIDs);
//
//		// HashMap<ID,XOR con this>
//		HashMap<String, String> xorMap = new HashMap<String, String>();
//
//		// elimino me stesso dalla lista
//		ids.remove(this.getID());
//
//		// creo la mappa chiave valore basata sullo xor tra (this) e la lista che arriva
//		for (String x : ids) {
//			BigInteger thisID =  new BigInteger(this.getID());
//			BigInteger xID = new BigInteger(x);
//			xorMap.put(x, thisID.xor(xID).toString());
//		}
//
//		ids.sort(new Comparator<String>() {
//
//			@Override
//			public int compare(String o1, String o2) {
//				String val_o1 = xorMap.get(o1);
//				String val_o2 = xorMap.get(o2);
//				return val_o1.compareTo(val_o2);
//			}
//
//		});
//
//		return ids;
//	}

	private ArrayList<Peer> getKOrdered(HashSet<Peer> kresult) {
		ArrayList<Peer> arr = new ArrayList<Peer>(kresult);
		HashMap<Peer, BigInteger> xorMap = new HashMap<Peer, BigInteger>();

		// elimino me stesso dalla lista
		arr.remove(this.peer);

		// creo la mappa chiave valore basata sullo xor tra (this) e la lista che arriva
		for (Peer px : arr) {
			BigInteger thisID = new BigInteger(this.getID());
			BigInteger pxID = new BigInteger(px.getID());

			xorMap.put(px, thisID.xor(pxID));
		}

		arr.sort(new Comparator<Peer>() {

			@Override
			public int compare(Peer o1, Peer o2) {
				BigInteger val_o1 = xorMap.get(o1);
				BigInteger val_o2 = xorMap.get(o2);
				return val_o1.compareTo(val_o2);
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

	public Peer[] findKClosest(String ID) {

		int k = Start.bucket_size;
		int log2;

		BigInteger peerID = new BigInteger(ID);
		log2 = (int) (Math.floor(Math.log(peerID.doubleValue()) / Math.log(2)));

		if (log2 < 1) {
			log2 = 0;
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

		for (ArrayList<Peer> x : this.DHT.bucket) {
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

	public int getNumLookup() {
		return this.real_lookup;
	}

	public double getAvgDepth() {
		if (this.depth.size() > 0) {
			return this.depth.stream().mapToDouble(x -> x).average().getAsDouble();
		} else {
			return 0;
		}

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

		BigInteger nodeID = new BigInteger(this.getID());
		BigInteger peerID = new BigInteger(peer.getID());
		BigInteger xor = nodeID.xor(peerID);
		log2 = (int) (Math.floor(Math.log(xor.doubleValue()) / Math.log(2)));

		this.DHT.put(peer, log2);
	}

	private void insert(Peer[] list) {
		for (Peer x : list) {
			if (!this.getID().equals(x.getID())) {
				this.insert(x);
			}
		}
	}

//	public static void computeAllIDS() {
//		BigInteger bound = new BigInteger("2").pow(Start.bit);
//
//		BigInteger i = BigInteger.valueOf(0);
//		while (i.compareTo(bound) < 0) {
//			allPossibleIDs.add(i.toString());
//			i = i.add(BigInteger.valueOf(1));
//		}
//	}

	private BigInteger getRandomBigInteger(BigInteger upperlimit) {
		Random rand = new Random();
		BigInteger result;
		do {
			result = new BigInteger(upperlimit.bitLength(), rand);
		} while (result.compareTo(upperlimit) >= 0);
		return result;
	}

	class RoutingTable {
		private int bit = Start.bit;
		private int bucket_size = Start.bucket_size;
		private int active_nodes = 0;

		private ArrayList<Peer>[] bucket = new ArrayList[bit];

		private void put(Peer peer, int pos) {
			if (bucket[pos] == null) {
				bucket[pos] = new ArrayList<Peer>();
			}

			if (bucket[pos].size() >= bucket_size) {
				// remove last
				bucket[pos].remove(bucket[pos].size() - 1);
				bucket[pos].add(peer);
			}
			if (!bucket[pos].contains(peer)) {
				bucket[pos].add(peer);
				active_nodes++;
			}
		}
	}
}
