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
 * In this implementation a Node is composed by an object Peer (containing
 * <ID,IP,Port>) and by an object Rounting Table. This way, the two logics are
 * placed in different classes.
 * 
 * @author Andrea Bruno
 *
 */
public class Node {
	private Peer peer = new Peer();
	private RoutingTable DHT = new RoutingTable();
	private Kademlia kad;
	private Peer boot;

	// configuration option
	private boolean refresh = Start.refresh;

	// Data structure to collect stats
	private LinkedList<Integer> hops = new LinkedList<Integer>();

	/**
	 * In order to create a Node object, I need to know to which network you want to
	 * connect
	 * 
	 * @param kad
	 */
	public Node(Kademlia kad) {
		this.kad = kad;

		// Retrieve a bootstrap Peer
		boot = connect(kad);

		// If I am not the first who is connecting to Kademlia
		if (boot != null) {

			// Perform a lookup of myself, as the paper suggests
			Peer[] list = this.lookup(this.peer.getID());

			// Then insert the results to its own DHT
			this.insert(list);

			// Two possibilities
			if (refresh) {
				// refreshBuckets() simulates the algorithm described in the paper that is
				// called "Bucket Refreshing". This procedure, for each bucket, will perform a
				// lookup of an ID that belongs to that bucket. This way after m-bit
				// lookups, all the buckets should be filled (partially).
				refreshBuckets();
			} else {
				// Simulates a lookup similar to Ethereum.
				// That is, a lookup of myself, and afterwards, a lookup of a random address
				// with ID smaller (XOR) than the bootstrap node.
				// https://github.com/ethereum/wiki/wiki/Kademlia-Peer-Selection#lookup
				ethereumLookup();
			}
		}

	}

	/**
	 * Connects a node to a given Kademlia network
	 * 
	 * @param kad
	 * @return the bootstrap info
	 */
	private Peer connect(Kademlia kad) {
		// Get the bootstrap node
		Peer bootstrap = kad.getBootstrap(this);

		if (bootstrap != null) {
			this.insert(bootstrap);
		}

		return bootstrap;
	}

	/**
	 * Performs a lookup of a random address with ID smaller (XOR) than the
	 * bootstrap node
	 * 
	 * https://github.com/ethereum/wiki/wiki/Kademlia-Peer-Selection#lookup
	 * 
	 */
	private void ethereumLookup() {
		String rnd;
		int result;

		// While the the random ID is not closer (XOR) that the bootstrap, generates a
		// new
		// random ID
		do {
			rnd = Utility.Utility.generateID();
			BigInteger tmpID = new BigInteger(rnd);
			BigInteger tmpXOR = tmpID.xor(new BigInteger(this.getID()));
			BigInteger bootXOR = new BigInteger(boot.getID()).xor(new BigInteger(this.getID()));
			result = tmpXOR.compareTo(bootXOR);
		} while (result >= 0);

		// Then perform the lookup, as Ethereum wiki suggests.
		Peer[] lookup_list = this.lookup(rnd);

		// Store the results
		for (Peer x : lookup_list) {
			this.insert(x);
		}
	}

	/**
	 * Simulates the algorithm described in the paper that is called "Bucket
	 * Refreshing". This procedure, for each bucket, will perform a lookup of an ID
	 * that belongs to that bucket. This way after m-bit lookups, all the buckets
	 * should be filled (partially).
	 */
	private void refreshBuckets() {
		// For each bucket
		for (int i = 0; i < Start.bit; i++) {
			String currentID;
			BigInteger randID;
			BigInteger thisID = new BigInteger(this.getID());
			ArrayList<Peer> toAdd = new ArrayList<Peer>();

			// The first bucket contains only one ID
			if (i == 0) {
				randID = BigInteger.valueOf(2).pow(i);
			} else {
				// Compute a random value between 2^i and 2^(i+1)
				BigInteger low = BigInteger.valueOf(2).pow(i);
				BigInteger high = BigInteger.valueOf(2).pow(i + 1).subtract(BigInteger.valueOf(1));
				randID = randomBigInt(low, high);
			}

			// Since the inverse of XOR is XOR, to find an ID that belongs to that bucket I
			// simply compute the XOR between this.ID and randID just computed.
			currentID = thisID.xor(randID).toString();

			// Perform a lookup
			Peer[] tmp = this.lookup(currentID);

			// Store the results
			toAdd.addAll(Arrays.asList(tmp));
			this.insert(toAdd.stream().limit(toAdd.size()).toArray(Peer[]::new));
		}

	}

	/**
	 * Compute a random BigInteger in the range [lower, upper]
	 * 
	 * @param lower bound
	 * @param upper bound
	 * @return a BigInteger
	 */
	private BigInteger randomBigInt(BigInteger lower, BigInteger upper) {
		BigInteger randID = getRandomBigInteger(upper.subtract(lower)).add(lower);
		return randID;
	}

	/**
	 * Compute a random BigInteger in [0, upperlimit]
	 * 
	 * @param upperlimit
	 * @return
	 */
	private BigInteger getRandomBigInteger(BigInteger upperlimit) {
		Random rand = new Random();
		BigInteger result;
		do {
			result = new BigInteger(upperlimit.bitLength(), rand);
		} while (result.compareTo(upperlimit) >= 0);
		return result;
	}

	/**
	 * This procedure, implements all the functionalities of the lookup procedure
	 * described in the Kademlia paper. Basically, every Node, given an ID, first
	 * contacts alpha Nodes, then will store the K best results (based on XOR
	 * closeness). If these nodes have returned new info, then continue to contact
	 * "recursively" the resulting nodes, eliminating the ones already visited.
	 * Finally, once there are no new info, stop the procedure and store the best K
	 * results (based on XOR closeness).
	 * 
	 * @param ID
	 * @return the best K Peers found
	 */
	private Peer[] lookup(String ID) {
		HashSet<Peer> alreadyVisited = new HashSet<Peer>();
		LinkedHashSet<Peer> kresult = new LinkedHashSet<Peer>();
		boolean somethingToMerge = true;
		int num_hops = 0;

		Peer[] alphaDHT = getAlphaPeersFromDHT(Start.alpha);

		// Count the number of hops
		num_hops++;

		// Contact alpha Peers
		for (Peer x : alphaDHT) {
			Peer[] ret_list = kad.FIND_NODE_RPC(this, x, ID);

			// if the list is not void
			if (ret_list != null) {
				kresult.addAll(Arrays.asList(ret_list));
			}
		}

		// Sort and prune to K the results
		kresult = new LinkedHashSet<Peer>(getKOrdered(kresult));

		alreadyVisited.addAll(Arrays.asList(alphaDHT));

		// While there is something to merge
		do {
			if (kresult.size() == 0) {
				somethingToMerge = false;
			} else {
				// Select alpha nodes from kresult
				int maxSize = Math.min(Start.alpha, kresult.size());
				Peer[] alphaLIST = kresult.stream().limit(maxSize).toArray(Peer[]::new);

				// Count the number of hops
				num_hops++;

				// Temporary data structure
				ArrayList<Peer> appendList = new ArrayList<Peer>();

				// Contact alpha Peers
				for (Peer x : alphaLIST) {

					// If it is not an already visited node
					if (!alreadyVisited.contains(x)) {

						// Do the FIND_NODE_RPC
						Peer[] ret_list = kad.FIND_NODE_RPC(this, x, ID);

						// Save this node as already visited
						alreadyVisited.add(x);

						// if the list is not void
						if (ret_list != null) {
							appendList.addAll(Arrays.asList(ret_list));
						}
					}
				}

				// if the list is not void
				if (appendList != null) {
					/**
					 * 
					 * N.B: The result of alpha iterations will produce alpha * K nodes. These nodes
					 * will be stored in appendList. If appendList contains something new, then the
					 * addAll() will produce TRUE.
					 * 
					 */
					somethingToMerge = kresult.addAll(appendList);
				}
			}

			// Sort and prune to K the results
			kresult = new LinkedHashSet<Peer>(getKOrdered(kresult));

		} while (somethingToMerge);

		// Count the number of hops
		this.hops.add(num_hops);

		// Return the best K results
		return kresult.toArray(new Peer[kresult.size()]);
	}

	/**
	 * This function compute the best K Peers of the {@code input} set of peer.
	 * 
	 * 
	 * @param input set of Peers
	 * @return the best K Peers
	 */
	private ArrayList<Peer> getKOrdered(HashSet<Peer> input) {
		ArrayList<Peer> arr = new ArrayList<Peer>(input);
		HashMap<Peer, BigInteger> xorMap = new HashMap<Peer, BigInteger>();

		// Remove myself from the list
		arr.remove(this.peer);

		// Create the HashMap < given_ID , XOR > based on the XOR between (this)
		// and the given ID
		for (Peer px : arr) {
			BigInteger thisID = new BigInteger(this.getID());
			BigInteger pxID = new BigInteger(px.getID());

			xorMap.put(px, thisID.xor(pxID));
		}

		// Sort them based on the XOR metric
		arr.sort(new Comparator<Peer>() {

			@Override
			public int compare(Peer o1, Peer o2) {
				BigInteger val_o1 = xorMap.get(o1);
				BigInteger val_o2 = xorMap.get(o2);
				return val_o1.compareTo(val_o2);
			}

		});

		// Prune them to K results
		if (arr.size() > Start.bucket_size) {
			arr = (ArrayList<Peer>) arr.stream().limit(Start.bucket_size).collect(Collectors.toList());
		}
		return arr;
	}

	/**
	 * Retrieves the alpha closest peer to (this).
	 * 
	 * @param alpha
	 * @return
	 */
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

	/**
	 * This function is part of the FIND_NODE_RPC functionalities described in the
	 * paper. Basically retrieves the K closest Peers (to this) of a given ID.
	 * 
	 * @param ID
	 * @return K closest Peers to the given ID
	 */
	public Peer[] findKClosest(String ID) {

		int k = Start.bucket_size;
		int log2;

		// Compute in which bucket belongs that ID
		BigInteger peerID = new BigInteger(ID);
		log2 = (int) (Math.floor(Math.log(peerID.doubleValue()) / Math.log(2)));

		if (log2 < 1) {
			log2 = 0;
		}

		int sizeAtLog = 0;
		Peer[] ret = null;

		// if the bucket of peerID is not void
		if (this.DHT.bucket[log2] != null) {
			// check how many peers this bucket contains
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

		// if there are more than K elements but the selected bucket
		// does not have K elements, then select K elements from the neighbours of the
		// bucket at position log2
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

	/**
	 * Retrieve K Peers going up and down with respect to the given position
	 * 
	 * @param bucket_position
	 * @return K Peers
	 */
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

	/**
	 * Retrieve an entire bucket
	 * 
	 * @return
	 */
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

	/**
	 * Compute the average number of hops, based on the historical data
	 * 
	 * @return the average number of hops
	 */
	public double getAvgHops() {
		if (this.hops.size() > 0) {
			return this.hops.stream().mapToDouble(x -> x).average().getAsDouble();
		} else {
			return 0;
		}

	}

	/**
	 * This function will returns all the edges of this node. It is useful to create
	 * the graphs.
	 * 
	 * @return a list of edges
	 */
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

	/**
	 * @return the ID
	 */
	String getID() {
		return peer.getID();
	}

	/**
	 * @return the Peer part of this Node
	 */
	public Peer getPeer() {
		return this.peer;
	}

	@Override
	public String toString() {
		return this.peer.toString();
	}

	/**
	 * Insert {@code peer} into the DHT of the node in the right position
	 * 
	 * @param peer
	 */
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

		if (log2 < 1) {
			log2 = 0;
		}

		this.DHT.put(peer, log2);
	}

	/**
	 * Given a {@code list} of Peers, insert them into the DHT into the right
	 * position
	 * 
	 * @param list
	 */
	private void insert(Peer[] list) {
		for (Peer x : list) {
			if (!this.getID().equals(x.getID())) {
				this.insert(x);
			}
		}
	}

	/**
	 * This class gives all the functionalities of a Routing Table as described into
	 * the Kademlia paper.
	 * 
	 * @author Andrea Bruno
	 *
	 */
	class RoutingTable {
		private int bit = Start.bit;
		private int bucket_size = Start.bucket_size;
		private int active_nodes = 0;

		private ArrayList<Peer>[] bucket = new ArrayList[bit];

		private void put(Peer peer, int pos) {

			// If the pointed bucket is void then create it
			if (bucket[pos] == null) {
				bucket[pos] = new ArrayList<Peer>();
			}

			// If there is no space and the node is not already in the Routing Table then
			// remove the last and add the given node
			if (bucket[pos].size() >= bucket_size) {
				if (!bucket[pos].contains(peer)) {
					bucket[pos].remove(bucket[pos].size() - 1);
					bucket[pos].add(peer);
				}
			}
			// If the bucket does not contain the given peer,
			// then add it to the Routing Table
			else if (!bucket[pos].contains(peer)) {
				bucket[pos].add(peer);
				active_nodes++;
			}
		}
	}
}
