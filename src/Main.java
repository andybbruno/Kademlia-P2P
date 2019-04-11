import java.util.LinkedList;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * @author Andrea Bruno
 *
 */
public class Main {
	static int bit = 5;
	static int num_nodes = 5;
	static int kbuckets = 2;

	public static void main(String[] args) {
		try {

			// Get <num_nodes> unique random nodes
			LinkedList<Integer> unique_id_random = randomNumbers(num_nodes, bit);

			LinkedList<Node> nodes = new LinkedList<Node>();

			Kademlia kad = new Kademlia();

			// generates nodes
			for (Integer x : unique_id_random) {
				nodes.add(new Node(x));
			}

			for (Node n : nodes) {
				System.out.println(n.toString());
			}
			
			int xor = nodes.get(0).getID() ^ nodes.get(1).getID();
			System.out.println(String.format("%" + Main.bit + "s", Integer.toBinaryString(xor)).replace(' ', '0'));

			// the first one is the bootstrap
			kad.addBooststrap(nodes.pop());

			// the others are common nodes
			kad.join(nodes.pop());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates {@code num} random numbers between 0 and 2 ^ {@code n_bit}
	 * 
	 * @param num
	 * @param n_bit
	 * 
	 * @return a list of {@code num} unique random numbers
	 */
	static LinkedList<Integer> randomNumbers(int num, int n_bit) {
		LinkedList<Integer> res = new LinkedList<Integer>();
		Random rand = new Random();
		Integer bound = (int) Math.pow(2, n_bit);

		while (res.size() < num) {
			Integer tmp = rand.nextInt(bound);
			if (!res.contains(tmp)) {
				res.add(tmp);
			}
		}
		return res;
	}
}