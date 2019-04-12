package Network;

import java.util.ArrayList;
import java.util.Random;

public class Kademlia {
	private ArrayList<Peer> peer_list = new ArrayList<Peer>();

	// if someone calls the bootstrap means that it is a new peer
	Peer getBootstrap(Peer peer) {
		Peer ret = null;

		// if there isn't any node add it to the list
		if (peer_list.size() == 0) {
			peer_list.add(peer);
		} else {
			Random rand = new Random();
			ret = peer_list.get(rand.nextInt(peer_list.size()));
		}
		return ret;
	}
}
