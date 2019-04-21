package Start;

import java.util.HashSet;
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
	public static int bit;
	public static int num_nodes;
	public static int bucket_size;
	public static int alpha = 3;
	public static String filename;

	static int[] arr_bit = { 32, 64, 128, 160, 256, 512, 1024, 2048 };
	static int[] arr_nod = { 50, 100, 200, 500, 1000, 2000, 5000, 10000};
	static int[] arr_buck = { 1, 2, 3, 5, 8, 13, 15, 20 };

	public static void main(String[] args) {

		for (int b : arr_bit) {
			bit = b;
			for (int nod : arr_nod) {
				num_nodes = nod;
				for (int buck : arr_buck) {
					bucket_size = buck;

					filename = "ANALYSIS/Kad_N_" + num_nodes + "_BIT_" + bit + "_K_" + bucket_size + ".csv";
					System.out.println(filename);

					// Create the network
					Kademlia kad = new Kademlia();

					// Create the nodes
					LinkedList<Node> nodes = new LinkedList<Node>();

					// Create the edges
					HashSet<String> edges = new HashSet<String>();

					long startTime = System.nanoTime();

					for (int i = 0; i < num_nodes; i++) {
						nodes.add(new Node(kad));
					}

					for (int i = 0; i < num_nodes; i++) {
						edges.addAll(nodes.get(i).getEdges());
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
		}
	}
}