package Overlap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class maximalConnect {	
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public static ArrayList<Object> method(String[] strings) {	
				
		ReadLargeGraph rg = new ReadLargeGraph();
		ArrayList<Object> tmp = rg.read(strings);				
		ArrayList<Integer> node_data = (ArrayList<Integer>) tmp.get(0); 						
		ArrayList<Integer[]> edge_data = (ArrayList<Integer[]>) tmp.get(1);	
		ArrayList<Integer[]> cmty_data = (ArrayList<Integer[]>) tmp.get(2);
		ArrayList<ArrayList<Integer>> neighbors = (ArrayList<ArrayList<Integer>>) tmp.get(5);	
		
		// Define and initialize basic variables
		boolean[] flag = new boolean[node_data.size()];
		Integer[] community = new Integer[node_data.size()];
		int count = 0;
		for (int i = 0; i < node_data.size(); i++) {
			flag[i] = false;
			community[i] = 0;
		}								
		// Identity connected component
		for (int i = 0; i < node_data.size(); i++) {
			if (!flag[i]) {
				dfs(i, node_data, edge_data, neighbors, flag, community, count);
				count ++;
			}
		}			
		// Sorting out output connected components
		Set<Integer> set = new HashSet<Integer>(Arrays.asList(community));
		int num = Collections.max(set);
		ArrayList<ArrayList<Integer>> newcommunity = new ArrayList<ArrayList<Integer>>();
		
		// initial community
		for (int i = 0; i < num+1; i++) {
			ArrayList<Integer> subcom = new ArrayList<Integer>();
			newcommunity.add(subcom);
		}
		for (int i = 0; i < node_data.size(); i++) {
			newcommunity.get(community[i]).add(node_data.get(i));
		}				
		// save the max subgraph
		int max = 0;
		ArrayList<Integer> maxcommunity = new ArrayList<Integer>();
		for (int i = 0; i < newcommunity.size(); i++) {
			if (newcommunity.get(i).size()>max) {
				max = newcommunity.get(i).size();
				for (int j = 0; j < newcommunity.get(i).size(); j++) {
					maxcommunity.add(newcommunity.get(i).get(j));
				}
			}
		}
		// update the other variable
		// cmty_data
		for (int i = 0; i < cmty_data.size(); i++) {
			int resutlen = 0;
			for (int j = 0; j < cmty_data.get(i).length; j++) {
				if (maxcommunity.contains(cmty_data.get(i)[j])) {
					cmty_data.get(i)[resutlen] = cmty_data.get(i)[j];
					resutlen ++;
				}
			}			
			Integer[] subcmty = new Integer[resutlen];
			for (int j = 0; j < subcmty.length; j++) {
				subcmty[j] = cmty_data.get(i)[j];
			}
			cmty_data.set(i, subcmty);
			if (cmty_data.get(i).length == 0) {
				cmty_data.remove(i--);
			}
		}
		// edge_data
		for (int i = 0; i < edge_data.size(); i++) {
			if (!(maxcommunity.contains(edge_data.get(i)[0]) && maxcommunity.contains(edge_data.get(i)[1]))) {
				edge_data.remove(i--);
			}
		}
		// belongCmty
		ArrayList<ArrayList<Integer>> belongCmty = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<maxcommunity.size();i++) {
			ArrayList<Integer> each_node = new ArrayList<Integer>();
			for(int j=0;j<cmty_data.size();j++) {
				for(int k=0;k<cmty_data.get(j).length;k++) {
					if (maxcommunity.get(i).equals(cmty_data.get(j)[k])) {
						each_node.add(j);
						break;
					}
				}
			}
			belongCmty.add(each_node);
		}
		// degree
		ArrayList<Integer> degree = new ArrayList<Integer>();
		for(int i=0;i<maxcommunity.size();i++) {
			degree.add(0);
		}		
		
		for(int i=0;i<edge_data.size();i++) {
			if((maxcommunity.contains(edge_data.get(i)[0])) && (maxcommunity.contains(edge_data.get(i)[1]))) {	
				degree.set(maxcommunity.indexOf(edge_data.get(i)[0]), degree.get(maxcommunity.indexOf(edge_data.get(i)[0]))+1);
				degree.set(maxcommunity.indexOf(edge_data.get(i)[1]), degree.get(maxcommunity.indexOf(edge_data.get(i)[1]))+1);
			}
		}
		
		ArrayList<Object> resultArrayList = new ArrayList<Object>();
		resultArrayList.add(maxcommunity);
		resultArrayList.add(edge_data);
		resultArrayList.add(cmty_data);
		resultArrayList.add(belongCmty);
		resultArrayList.add(degree);
		
		return resultArrayList;
		
	}
	// Depth first search
	public static void dfs(int v, ArrayList<Integer> node_data, ArrayList<Integer[]> edge_data, 
			ArrayList<ArrayList<Integer>> neighbors, boolean[] flag, Integer[] community, int count) {
		flag[v] = true;
		community[v] = count;
		ArrayList<Integer> neighbor = neighbors.get(v);
		for (int i = 0; i < neighbor.size(); i++) {
			if (!flag[node_data.indexOf(neighbor.get(i))]) {
				dfs(node_data.indexOf(neighbor.get(i)), node_data, edge_data, neighbors, flag, community, count);
			}
		}
	}
}
