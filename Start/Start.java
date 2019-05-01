package Start;

import java.util.HashSet;
import java.util.LinkedList;
import java.io.File;
import java.io.PrintWriter;
import Network.Kademlia;
import Network.Node;

/**
 * This class contains all the variables and all the procedures of the
 * experiments
 * 
 * @author Andrea Bruno
 *
 */
public class Start {
	static int[] arr_bit = { 20, 40, 60, 80, 100, 120, 140, 160 };
	static int[] arr_nod = { 625, 1250, 1875, 2500, 3125, 3750, 4375, 5000 };
	static int[] arr_buck = { 2, 5, 7, 10, 12, 15, 17, 20 };

	public static boolean refresh = false;
	public static int bit = arr_bit[0];
	public static int num_nodes;
	public static int bucket_size;
	public static int alpha = 3;
	public static String filename;

	public static void main(String[] args) {

		// Data Structures for stats
		LinkedList<String> results = new LinkedList<String>();
		LinkedList<Double> hops = new LinkedList<Double>();

		// First row of the results CSV file
		results.add(new String("#Nodes,#Bit,K,Time,Avg Hops"));

		long start = System.nanoTime();

		// For each bit in arr_bit
		for (int i = 0; i < arr_bit.length; i++) {
			bit = arr_bit[i];

			// For each num_nodes in arr_nod
			for (int j = 0; j < arr_nod.length; j++) {
				num_nodes = arr_nod[j];

				// For each bucket_size in arr_buck
				for (int k = 0; k < arr_buck.length; k++) {
					bucket_size = arr_buck[k];

					filename = "ANALYSIS/csv/Kad_N_" + num_nodes + "_BIT_" + bit + "_K_" + bucket_size + ".csv";
					System.out.println(filename);

					// Create the network
					Kademlia kad = new Kademlia();

					// Create a void list of nodes
					LinkedList<Node> nodes = new LinkedList<Node>();

					// Create a void list of edges
					HashSet<String> edges = new HashSet<String>();

					long startTime = System.nanoTime();

					// creates num_nodes nodes
					for (int q = 0; q < num_nodes; q++) {
						nodes.add(new Node(kad));
					}

					for (int c = 0; c < num_nodes; c++) {
						edges.addAll(nodes.get(c).getEdges());
						hops.add(nodes.get(c).getAvgHops());
					}

					int num_edges = 0;

					// Creates a file for each triple < N_Bit, Bucket_Size, Num_Nodes >
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
					long timeElapsed = (endTime - startTime) / 1000000;

					// Compute the average number of hops
					Double avg_hops = hops.stream().mapToDouble(x -> x).average().getAsDouble();

					String res = num_nodes + "," + bit + "," + bucket_size + "," + timeElapsed + "," + avg_hops;

					results.add(res);

					System.out.println("Execution time in milliseconds : " + timeElapsed);
					System.out.println("Average lookup depth : " + avg_hops);

					if (num_edges > 100000) {
						System.err.println(num_edges + " edges");
					} else {
						System.out.println(num_edges + " edges");
					}
				}
			}
		}

		// Create a CSV file containg the overall statistics
		try (PrintWriter writer = new PrintWriter(new File("ANALYSIS/results.csv"))) {

			StringBuilder sb = new StringBuilder();

			for (String x : results) {
				sb.append(x);
				sb.append('\n');
			}

			writer.write(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.nanoTime();
		// get difference of two nanoTime values
		long time = end - start;

		System.out.println("\n\nTotal time in seconds : " + time / 1000000000);
	}
}