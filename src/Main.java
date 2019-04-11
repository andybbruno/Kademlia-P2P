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
	static int bit = 4;
	static int num_nodes = 5;
	static int kbuckets = 2;

	public static void main(String[] args) {
		
		// Get <num_nodes> unique random nodes
		LinkedList<Integer> unique_random = randomNumbers(num_nodes, bit);

		// The first is used as the bootstrap node
		Integer bootstrap = unique_random.pop();

		Kademlia kad = new Kademlia();
		
		
		
		
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
		Integer bound = (int) Math.pow(2, n_bit) + 1;

		while (res.size() < num) {
			Integer tmp = rand.nextInt(bound);
			if (!res.contains(tmp)) {
				res.add(tmp);
			}
		}

		return res;

	}
}
