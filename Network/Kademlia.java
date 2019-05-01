package Network;

import java.util.HashMap;
import java.util.Random;

import Start.Start;

/**
 * This class simulates the Kademlia network. The methods inside this class are
 * a simulation of TCP/IP requests or RPC that are normally executed over the
 * internet. 
 * 
 * @author Andrea Bruno
 *
 */
public class Kademlia {
	public HashMap<String, Node> node_list = new HashMap<String, Node>();

	/**
	 * This method is called when a node connects to Kademlia. Of course, if someone
	 * calls the bootstrap means that it is a new node, so it becomes a connected
	 * node.
	 * 
	 * @param node a node who requires a bootstrap to connect to Kademlia
	 * @return a Peer object of a bootstrap.
	 */
	Peer getBootstrap(Node node) {
		Peer ret = null;

		// if there is at least one node in the network
		if (node_list.size() > 0) {
			// then return a random bootstrap node from the connected ones
			Random rand = new Random();
			String[] keys = node_list.keySet().toArray(new String[0]);
			String random_key = keys[rand.nextInt(node_list.size())];
			ret = node_list.get(random_key).getPeer();
		}

		// add this node to the network
		node_list.put(node.getPeer().getID(), node);
		return ret;
	}

	/**
	 * This function simulates the find_node remote procedure call of Kademlia.
	 * There is a {@code sender}, a {@code receiver} and an {@code ID} to find.
	 * 
	 * @param sender
	 * @param receiver
	 * @param ID
	 * @return a list of peer
	 */
	public Peer[] FIND_NODE_RPC(Node sender, Peer receiver, String ID) {

		// Given the ID, get the node in order to access its own DHT
		Node receiverNode = getNodeFromPeer(receiver);

		// find the ID of the sender in the receiver
		Peer[] closest = receiverNode.findKClosest(ID);

		// since now I know this peer, I add it to its own DHT
		receiverNode.insert(sender.getPeer());

		return closest;
	}

	/**
	 * Given a Peer it returns a Node in order to access its own DHT
	 * 
	 * @param peer
	 * @return
	 */
	private Node getNodeFromPeer(Peer peer) {
		String ID = peer.getID();
		return node_list.get(ID);
	}

}
