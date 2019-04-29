% pyversion /usr/bin/python3
py.importlib.import_module('networkx');

clear all;

% prefix = "ANALYSIS/csv/Kad_N_"
prefix = "csv/Kad_";


arr_bit = [ 20, 40, 60, 80, 100, 120, 140, 160 ];
arr_nod = [ 625, 1250, 1875, 2500, 3125, 3750, 4375, 5000 ];
arr_buck = [ 2, 5, 7, 10, 12, 15, 17, 20 ];


results = [];

parfor i = 1:length(arr_bit)
    b = arr_bit(i);
    bits = b;
    
    for k = arr_buck
        kbucket = k;
        
        for n = arr_nod
            nodes = n;
            
            res = struct;
            
            filename = "N_" + nodes + "_BIT_" + bits + "_K_" + kbucket + ".csv";
            csv_file = prefix + filename;
            
            G = py.networkx.read_edgelist(csv_file,'#',',',py.networkx.DiGraph());
            
            nods = double(py.networkx.number_of_nodes(G));
            edgs = double(py.networkx.number_of_edges(G));
            
            diam = -1;
            try
                diam = double(py.networkx.diameter(G));
            catch exception         
            end
                
            cl_coeff = double(py.networkx.average_clustering(G));
            sh_path = double(py.networkx.average_shortest_path_length(G));
            
            
            info = string(py.networkx.info(G));
            tmp_in = strsplit(info, "Average in degree:");
            tmp_in = strsplit(tmp_in(2));
            avg_deg_in = str2double(tmp_in(2));
            
            tmp_out = strsplit(info, "Average out degree:");
            avg_deg_out = str2double(tmp_out(2));
            
            
            res.nods = nods;
            res.bit = bits;
            res.k = kbucket;
            res.edgs = edgs;
            res.diam = diam;
            res.cl_coeff = cl_coeff;
            res.sh_path = sh_path;
            res.avg_deg_in = avg_deg_in;
            res.avg_deg_out = avg_deg_out;
            
            writetable(struct2table(res), "log/log_" + datestr(now,'HH_MM_SS_FFF') + ".csv");
            
            disp(res);
            
            results = [results res];
        end
    end
end

writetable(struct2table(results), 'complete_results.csv');