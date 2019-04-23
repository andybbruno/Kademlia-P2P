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
//	static int[] arr_bit = { 32, 64, 128, 160, 256, 512, 1024, 2048 };
//	static int[] arr_nod = { 50, 100, 200, 500, 1000, 2000, 5000, 10000 };
//	static int[] arr_buck = { 2, 3, 5, 8, 10, 13, 15, 20 };

	static int[] arr_bit = { 160 };
	static int[] arr_nod = { 1000 };
	static int[] arr_buck = { 20 };

	public static boolean SHA1 = false;
	public static boolean refresh = false;
	public static int bit = arr_bit[0];
	public static int num_nodes;
	public static int bucket_size;
	public static int alpha = 3;
	public static String filename;

	public static void main(String[] args) {

		LinkedList<String> results = new LinkedList<String>();
		results.add(new String("#Nodes,#Bit,K,Time,Avg Lookups,Avg Depth"));

		long start = System.nanoTime();

		for (int i = 0; i < arr_bit.length; i++) {
			bit = arr_bit[i];
			for (int j = 0; j < arr_nod.length; j++) {
				num_nodes = arr_nod[j];
				for (int k = 0; k < arr_buck.length; k++) {
					bucket_size = arr_buck[k];

					filename = "ANALYSIS/csv/Kad_N_" + num_nodes + "_BIT_" + bit + "_K_" + bucket_size + ".csv";
					System.out.println(filename);

					// Create the network
					Kademlia kad = new Kademlia();

					// Create the nodes
					LinkedList<Node> nodes = new LinkedList<Node>();

					// Create the edges
					HashSet<String> edges = new HashSet<String>();

					long startTime = System.nanoTime();

					for (int q = 0; q < num_nodes; q++) {
						nodes.add(new Node(kad));
					}

					LinkedList<Double> depths = new LinkedList<Double>();
					LinkedList<Integer> lookups = new LinkedList<Integer>();

					for (int c = 0; c < num_nodes; c++) {
						edges.addAll(nodes.get(c).getEdges());
						depths.add(nodes.get(c).getAvgDepth());
						lookups.add(nodes.get(c).getNumLookup());
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
					long timeElapsed = (endTime - startTime) / 1000000;

					Double avg_depths = depths.stream().mapToDouble(x -> x).average().getAsDouble();
					Double avg_lookups = lookups.stream().mapToDouble(x -> x).average().getAsDouble();

					String res = num_nodes + "," + bit + "," + bucket_size + "," + timeElapsed + "," + avg_lookups + ","
							+ avg_depths;

					results.add(res);

					System.out.println("Execution time in milliseconds : " + timeElapsed);
					System.out.println("Average lookup calls : " + avg_lookups);
					System.out.println("Average lookup depth : " + avg_depths);
					System.out.println(num_edges + " edges");
				}
			}
		}

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