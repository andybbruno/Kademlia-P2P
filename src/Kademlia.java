import java.util.ArrayList;

public class Kademlia {
	private ArrayList<Node> network = new ArrayList<Node>();
	private Node bootstrap = null;

	Kademlia() {

	}

	public void join(Node node) throws Exception {
		if (!network.contains(node)) {
			network.add(node);
		} else {
			throw new Exception("Node already in the network");
		}
	}

	public void addBooststrap(Node node) throws Exception {
		if (bootstrap == null) {
			this.bootstrap = node;
			join(node);
		} else {
			throw new Exception("Can't add another bootstrap");
		}
	}
}
