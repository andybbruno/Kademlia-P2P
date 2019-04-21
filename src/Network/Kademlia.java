package Network;

import java.util.HashMap;
import java.util.Random;

import Start.Start;

public class Kademlia {
	public HashMap<String, Node> node_list = new HashMap<String, Node>();

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

	public Peer[] FIND_NODE_RPC(Node sender, Peer receiver, String ID) {

		// Given the ID, get the node in order to access its own DHT
		Node receiverNode = getNodeFromPeer(receiver);


		//find the ID of the sender in the receiver
		Peer[] closest = receiverNode.findKClosest(ID, Start.bucket_size);

		// since now I know this peer, I add it to its own DHT
		receiverNode.insert(sender.getPeer());
				
		return closest;
	}

	public Node getNodeFromPeer(Peer peer) {
		String ID = peer.getID();
		return node_list.get(ID);
	}

}
