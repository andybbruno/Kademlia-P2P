package Start;

import java.util.LinkedList;
import java.io.File;
import java.io.PrintWriter;
import Network.Kademlia;
import Network.Node;

/**
 * @author Andrea Bruno
 *
 */
public class Start {
	public static boolean SHA1 = false;
	public static int bit = 32;
	public static int num_nodes = 10;
	public static int bucket_size = 2;
	public static int alpha = 3;
	public static String filename = "ANALYSIS/Kad_N_" + num_nodes + "_BIT_" + bit + "_K_" + bucket_size + ".csv";

	public static void main(String[] args) {
		
		
		
//		new Analysis();
		
		
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

		int num_edges = 0;

		try (PrintWriter writer = new PrintWriter(new File(filename))) {

			StringBuilder sb = new StringBuilder();

			for (String x : edges) {
				sb.append(x);
				sb.append('\n');
				num_edges++;

			}

			writer.write(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		long endTime = System.nanoTime();

		// get difference of two nanoTime values
		long timeElapsed = endTime - startTime;

		System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000);
		System.out.println(num_edges + " edges");

		
	}
}