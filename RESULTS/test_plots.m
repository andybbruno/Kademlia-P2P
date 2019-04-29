clear all
close all
load res.mat;

a_bit = [ 20, 40, 60, 80, 100, 120, 140, 160 ];
a_nod = [ 625, 1250, 1875, 2500, 3125, 3750, 4375, 5000 ];
a_buck = [ 2, 5, 7, 10, 12, 15, 17, 20 ];

data = struct;


nodes = [res.Nodes];
bits = [res.Bit];
kbuck = [res.K];
edges = [res.edgs];

time = [res.Time];
hops = [res.AvgHops];
diam = [res.diam];
cluster = [res.cl_coeff];
shpath = [res.sh_path];
avgdeg = [res.avg_deg];


i=1;
for x = a_buck
    idx = find(kbuck == x);
    for w = a_bit
        tmp = find(bits == w);
        idw = intersect(tmp,idx);
        data(i).label = x + " - " + w;

        data(i).mean_time = mean(time(idw));
        data(i).mean_hops = mean(hops(idw));
        data(i).mean_edges = mean(edges(idw));
        
        % remove NaN
        tmp_diam = diam(idw);
        noNaN = tmp_diam(~isnan(tmp_diam));
        data(i).mean_diam = mean(noNaN);

        
        data(i).mean_cluster = mean(cluster(idw));
        data(i).mean_shpath = mean(shpath(idw));
        data(i).mean_avgdeg = mean(avgdeg(idw));

        data(i).std_edges = std(edges(idw));
        data(i).std_time = std(time(idw));
        data(i).std_avghops = std(hops(idw));
        data(i).std_diam = std(diam(idw));
        data(i).std_cluster = std(cluster(idw));
        data(i).std_shpath = std(shpath(idw));
        data(i).std_avgdeg = std(avgdeg(idw));


        i = i + 1;
    end
end

mean_edges = [data.mean_edges].';
mean_time = [data.mean_time].';
mean_hops = [data.mean_hops].';
mean_diam = [data.mean_diam].';
mean_cluster = [data.mean_cluster].';
mean_shpath = [data.mean_shpath].';
mean_avgdeg = [data.mean_avgdeg].';


m_edges = []; m_time = []; m_avd = []; m_diam = []; m_cluster = []; m_shpath = []; m_avgdeg = [];
idz = 1;

for i=1:numel(a_buck)
    for j=1:numel(a_bit)
        m_edges(i,j) = mean_edges(idz);
        m_time(i,j) = mean_time(idz);
        m_hops(i,j) = mean_hops(idz);
        m_diam(i,j) = mean_diam(idz);
        m_cluster(i,j) = mean_cluster(idz);
        m_shpath(i,j) = mean_shpath(idz);
        m_avgdeg(i,j) = mean_avgdeg(idz);
        idz = idz + 1;
    end
end

figure;
heatmap(a_buck,a_bit, m_edges,'Colormap',parula);
set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
title("Graph Diameter"); ylabel('# Bit'); xlabel('K Bucket');

% figure;
% heatmap(a_buck,a_bit,round(m_cluster,3),'Colormap',parula);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% title("Cluster Coefficient"); ylabel('# Bit'); xlabel('K Bucket');
% 
% 
% figure;
% heatmap(a_buck,a_bit,round(m_avgdeg,3),'Colormap',parula);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% title("Average Degree"); ylabel('# Bit'); xlabel('K Bucket');



%
% sh = []; sh_std = [];
%
% for i = a_buck
%     idx = find(kbuck==i);
%     sh = [sh , mean(shpath(idx))];
%     sh_std = [sh_std , std(shpath(idx))];
% end
%
% errorbar(a_buck,sh,sh_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([1 21 2.5 6.5]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% title("Shortest Path"); xlabel('K Bucket');
%




% diNODS = []; diNODS_std = [];
% 
% for i = a_nod
%     idx = find(nodes==i);
%     
%     % remove NaN
%     tmp_diam = diam(idx);
%     noNaN = tmp_diam(~isnan(tmp_diam));
%     
%     diNODS = [diNODS , mean(noNaN)];
%     diNODS_std = [diNODS_std , std(noNaN)];
% end
% 
% 
% 
% diBIT = []; diBIT_std = [];
% 
% for i = a_bit
%     idx = find(bits==i);
%     
%     % remove NaN
%     tmp_diam = diam(idx);
%     noNaN = tmp_diam(~isnan(tmp_diam));
%     
%     diBIT = [diBIT , mean(noNaN)];
%     diBIT_std = [diBIT_std , std(noNaN)];
% end
% 
% 
% diBUCK = []; diBUCK_std = [];
% 
% for i = a_buck
%     idx = find(kbuck==i);
%     
%     % remove NaN
%     tmp_diam = diam(idx);
%     noNaN = tmp_diam(~isnan(tmp_diam));
%     
%     diBUCK = [diBUCK , mean(noNaN)];
%     diBUCK_std = [diBUCK_std , std(noNaN)];
% end
% 
% 
% 
% 
% figure('units','normalized','outerposition',[0 1 0.3 1]);
% subplot(3,1,1);
% errorbar(a_nod,diNODS,diNODS_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([500 5100 3.5 7]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Diameter"); xlabel('Nodes');
% 
% subplot(3,1,2);
% errorbar(a_bit,diBIT,diBIT_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([15 165 3.5 7]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Diameter"); xlabel('Bit');
% 
% 
% subplot(3,1,3);
% errorbar(a_buck,diBUCK,diBUCK_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([4 21 3.5 7]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Diameter"); xlabel('K Buckets');




% diNODS = []; diNODS_std = [];
% 
% for i = a_nod
%     idx = find(nodes==i);
%     diNODS = [diNODS , mean(shpath(idx))];
%     diNODS_std = [diNODS_std , std(shpath(idx))];
% end
% 
% 
% 
% diBIT = []; diBIT_std = [];
% 
% for i = a_bit
%     idx = find(bits==i);
%     diBIT = [diBIT , mean(shpath(idx))];
%     diBIT_std = [diBIT_std , std(shpath(idx))];
% end
% 
% 
% diBUCK = []; diBUCK_std = [];
% 
% for i = a_buck
%     idx = find(kbuck==i);
%     diBUCK = [diBUCK , mean(shpath(idx))];
%     diBUCK_std = [diBUCK_std , std(shpath(idx))];
% end
% 
% 
% 
% 
% figure('units','normalized','outerposition',[0 1 0.3 1]);
% subplot(3,1,1);
% errorbar(a_nod,diNODS,diNODS_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([500 5100 2 6]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Shortest Path"); xlabel('Nodes');
% 
% subplot(3,1,2);
% errorbar(a_bit,diBIT,diBIT_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([15 165 2 6]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Shortest Path"); xlabel('Bit');
% 
% 
% subplot(3,1,3);
% errorbar(a_buck,diBUCK,diBUCK_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([1 21 2 6]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Shortest Path"); xlabel('K Buckets');




% diNODS = []; diNODS_std = [];
% 
% for i = a_nod
%     idx = find(nodes==i);
%     diNODS = [diNODS , mean(cluster(idx))];
%     diNODS_std = [diNODS_std , std(cluster(idx))];
% end
% 
% 
% 
% diBIT = []; diBIT_std = [];
% 
% for i = a_bit
%     idx = find(bits==i);
%     diBIT = [diBIT , mean(cluster(idx))];
%     diBIT_std = [diBIT_std , std(cluster(idx))];
% end
% 
% 
% diBUCK = []; diBUCK_std = [];
% 
% for i = a_buck
%     idx = find(kbuck==i);
%     diBUCK = [diBUCK , mean(cluster(idx))];
%     diBUCK_std = [diBUCK_std , std(cluster(idx))];
% end
% 
% 
% 
% 
% figure('units','normalized','outerposition',[0 1 0.3 1]);
% subplot(3,1,1);
% errorbar(a_nod,diNODS,diNODS_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([500 5100 0.2 0.4]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Cluster Coefficient"); xlabel('Nodes');
% 
% subplot(3,1,2);
% errorbar(a_bit,diBIT,diBIT_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([15 165 0.2 0.4]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Cluster Coefficient"); xlabel('Bit');
% 
% 
% subplot(3,1,3);
% errorbar(a_buck,diBUCK,diBUCK_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([1 21 0.2 0.4]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Cluster Coefficient"); xlabel('K Buckets');




% diNODS = []; diNODS_std = [];
% 
% for i = a_nod
%     idx = find(nodes==i);
%     diNODS = [diNODS , mean(avgdeg(idx))];
%     diNODS_std = [diNODS_std , std(avgdeg(idx))];
% end
% 
% 
% 
% diBIT = []; diBIT_std = [];
% 
% for i = a_bit
%     idx = find(bits==i);
%     diBIT = [diBIT , mean(avgdeg(idx))];
%     diBIT_std = [diBIT_std , std(avgdeg(idx))];
% end
% 
% 
% diBUCK = []; diBUCK_std = [];
% 
% for i = a_buck
%     idx = find(kbuck==i);
%     diBUCK = [diBUCK , mean(avgdeg(idx))];
%     diBUCK_std = [diBUCK_std , std(avgdeg(idx))];
% end
% 
% 
% 
% 
% figure('units','normalized','outerposition',[0 1 0.3 1]);
% subplot(3,1,1);
% errorbar(a_nod,diNODS,diNODS_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([500 5100 0 50]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Average Degree"); xlabel('Nodes');
% 
% subplot(3,1,2);
% errorbar(a_bit,diBIT,diBIT_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([15 165 0 50]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Average Degree"); xlabel('Bit');
% 
% 
% subplot(3,1,3);
% errorbar(a_buck,diBUCK,diBUCK_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([1 21 0 50]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("Average Degree"); xlabel('K Buckets');




% 
% diNODS = []; diNODS_std = [];
% 
% for i = a_nod
%     idx = find(nodes==i);
%     diNODS = [diNODS , mean(hops(idx))];
%     diNODS_std = [diNODS_std , std(hops(idx))];
% end
% 
% 
% 
% diBIT = []; diBIT_std = [];
% 
% for i = a_bit
%     idx = find(bits==i);
%     diBIT = [diBIT , mean(hops(idx))];
%     diBIT_std = [diBIT_std , std(hops(idx))];
% end
% 
% 
% diBUCK = []; diBUCK_std = [];
% 
% for i = a_buck
%     idx = find(kbuck==i);
%     diBUCK = [diBUCK , mean(hops(idx))];
%     diBUCK_std = [diBUCK_std , std(hops(idx))];
% end
% 
% 
% 
% 
% figure('units','normalized','outerposition',[0 1 0.3 1]);
% subplot(3,1,1);
% errorbar(a_nod,diNODS,diNODS_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([500 5100 3.7 3.85]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("RPC Depth"); xlabel('Nodes');
% 
% subplot(3,1,2);
% errorbar(a_bit,diBIT,diBIT_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([15 165 3.7 3.85]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("RPC Depth"); xlabel('Bit');
% 
% 
% subplot(3,1,3);
% errorbar(a_buck,diBUCK,diBUCK_std,'-s','MarkerSize',10,'MarkerEdgeColor','red','MarkerFaceColor','red');
% axis([1 21 3.7 3.85]);
% set(gca,'FontSize',get(gcf,'defaultaxesfontsize')+4);
% ylabel("RPC Depth"); xlabel('K Buckets');

