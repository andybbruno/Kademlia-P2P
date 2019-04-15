package Network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import Main.Start;

public class Kademlia {
	private HashMap<String, Node> node_list = new HashMap<String, Node>();

	// if someone calls the bootstrap means that it is a new node
	Peer getBootstrap(Node node) {
		Peer ret = null;

		// if there is at least one node in the network
		if (node_list.size() > 0) {
			// then return a random bootstrap node
			Random rand = new Random();
			String[] keys = node_list.keySet().toArray(new String[0]);
			String random_key = keys[rand.nextInt(node_list.size())];
			ret = node_list.get(random_key).getPeer();
		}

		// add this node to the network
		node_list.put(node.getPeer().getID(), node);
		return ret;
	}

	public Peer[] FIND_NODE_RPC (Node sender, Peer receiver){

		String ID = sender.getID();
		
		//Given the ID, get the node in order to access its own DHT
		Node receiverNode = node_list.get(ID);

		Peer[] closest = receiverNode.findKClosest(ID, Start.bucket_size);
		
		return closest;
	}
	
}
