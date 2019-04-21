import networkx as nx
import matplotlib.pyplot as plt
import scipy

prefix = "ANALYSIS/csv/Kad_N_"
# prefix = "csv/Kad_N_"

arr_bit = {32, 64, 128, 160, 256, 512, 1024, 2048}
arr_nod = {50, 100, 200, 500, 1000, 2000, 5000, 10000}
arr_buck = {2, 3, 5, 8, 10, 13, 15, 20}


for n in arr_nod:
    nodes = n
    for b in arr_bit:
        bits = b
        for k in arr_buck:
            kbucket = k
            filename = prefix + str(nodes) + "_BIT_" + str(bits) + "_K_" + str(kbucket) + ".csv"
                        
            G = nx.read_edgelist(filename, delimiter=',',
                                 nodetype=int, encoding="utf-8")
            
            print("\n\n\nN_" + str(nodes) + "_BIT_" + str(bits) + "_K_" + str(kbucket))
            print("Number of nodes:\t\t%15.0f" %  nx.number_of_nodes(G))
            print("Number of edges:\t\t%15.0f" % nx.number_of_edges(G))        
            print("Diameter:\t\t\t%15.0f" % nx.diameter(G))
            print("Average clustering coefficient:\t%15.4f" % nx.average_clustering(G))
            print("Average shortest path:\t\t%15.4f" % nx.average_shortest_path_length(G))
            print("Average degree:\t\t\t%15.4f" % (float(sum(dict(G.degree()).values())) / float(G.number_of_nodes())))

# plt.suptitle("N_" + str(nodes) + "_BIT_" + str(bits) + "_K_" + str(kbucket))
# nx.draw(G,node_size=50)
# plt.show()
