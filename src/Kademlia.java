import java.util.ArrayList;

public class Kademlia {
	private ArrayList<Node> network = new ArrayList<Node>();
	private Node bootstrap = null;

	Kademlia() {

	}

	public void join(Node node) throws Exception {
		if (!network.contains(node)) {
			node.DHT[0][0] = node.getID();
			node.DHT[0][1] = bootstrap.getID();
			network.add(node);

			
			//Routing.FIND_NODE(from, to);
			
			
		} else {
			throw new Exception("Node already in the network");
		}
	}

	public void addBooststrap(Node node) throws Exception {
		if (bootstrap == null) {
			this.bootstrap = node;
			node.DHT[0][0] = node.getID();
			network.add(node);
		} else {
			throw new Exception("Can't add another bootstrap");
		}
	}
}
