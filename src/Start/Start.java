package Start;

import java.util.LinkedList;
import java.io.File;
import java.io.PrintWriter;
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
	private static final String FILE_NAME = "kad.csv";
	
	public static boolean SHA1 = false;
	public static int bit = 8;
	public static int num_nodes = 15;
	public static int bucket_size = 4;
	public static int alpha = 3;

	public static void main(String[] args) {

		// Create the network
		Kademlia kad = new Kademlia();

		// Create the nodes
		LinkedList<Node> nodes = new LinkedList<Node>();

		LinkedList<String> edges = new LinkedList<String>();

		long startTime = System.nanoTime();

		for (int i = 0; i < num_nodes; i++) {
			nodes.add(new Node(kad));
			if ((i % 1000) == 0) {
				System.out.println(i);
			}
		}

		for (int i = 0; i < num_nodes; i++) {
			edges.addAll(nodes.get(i).getEdges());
			if ((i % 1000) == 0) {
				System.out.println(i);
			}
		}

		try (PrintWriter writer = new PrintWriter(new File(FILE_NAME))) {

			StringBuilder sb = new StringBuilder();

			for (String x : edges) {
				sb.append(x);
				sb.append('\n');
			}

			writer.write(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		long endTime = System.nanoTime();

		// get difference of two nanoTime values
		long timeElapsed = endTime - startTime;

		System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000);

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