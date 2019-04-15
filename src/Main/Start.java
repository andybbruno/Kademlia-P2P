package Main;
import java.util.LinkedList;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import Network.Kademlia;
import Network.Node;

/**
 * @author Andrea Bruno
 *
 */
public class Start {
	public static int bit = 160;
	public static int num_nodes = 1000;
	public static int bucket_size = 2;
	public static int alpha = 3;

	public static void main(String[] args) {

		// Create the network
		Kademlia kad = new Kademlia();

		// Create the nodes
		LinkedList<Node> nodes = new LinkedList<Node>();

		for (int i = 0; i < num_nodes; i++) {
			nodes.add(new Node(kad));
		}
		
		System.out.println("ciao");
	}

	/**
	 * Generates {@code num} random numbers between 0 and 2 ^ {@code n_bit}
	 * 
	 * @param num
	 * @param n_bit
	 * 
	 * @return a list of {@code num} unique random numbers
	 */
	static int[] randomNumbers(int num, int n_bit) {
		LinkedList<Integer> res = new LinkedList<Integer>();
		Random rand = new Random();
		Integer bound = (int) Math.pow(2, n_bit);

		while (res.size() < num) {
			Integer tmp = rand.nextInt(bound);
			if (!res.contains(tmp)) {
				res.add(tmp);
			}
		}
		return res.parallelStream().mapToInt(x -> x).toArray();
	}
}