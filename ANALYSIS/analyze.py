import networkx as nx
import matplotlib.pyplot as plt
import scipy

filename = "Kad_N_10_BIT_32_K_2.csv"

G = nx.read_edgelist(filename, delimiter=',', nodetype=int, encoding="utf-8")

clustering = nx.average_clustering(G)
print(clustering)

avg_path = nx.average_shortest_path_length(G)
print(avg_path)

diameter = nx.diameter(G)
print(diameter)

degree = nx.average_degree_connectivity(G)
print(degree)


nx.draw_circular(G)
plt.show()