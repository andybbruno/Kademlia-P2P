package Network;

import java.util.ArrayList;
import java.util.Random;

public class Kademlia {
	private ArrayList<Host> peer_list = new ArrayList<Host>();

	// if someone calls the bootstrap means that it is a new peer
	Host getBootstrap(Host peer) {
		Host ret = null;
		
		// if there is at least a node in the network
		if (peer_list.size() > 0) {
			// then return a random bootstrap node
			Random rand = new Random();
			ret = peer_list.get(rand.nextInt(peer_list.size()));
		}
		
		//add this peer to the network
		peer_list.add(peer);
		return ret;
	}
}
