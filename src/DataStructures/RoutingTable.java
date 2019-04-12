package DataStructures;

import java.util.LinkedList;

import Main.Start;
import Network.Peer;

public class RoutingTable {
	private int bit;
	private int kbuckets;

	private Bucket[] bucket;

	RoutingTable() {
		this.bit = Start.bit;
		this.kbuckets = Start.kbuckets;
		
		bucket = new Bucket[kbuckets];
	}

	private class Bucket {
		LinkedList<Peer>[] list;

		Bucket() {
			list = new LinkedList[bit];
		}
	}

	void addPeer(Peer peer) {
		//TODO insert this peer into the right bucket
	}

}
