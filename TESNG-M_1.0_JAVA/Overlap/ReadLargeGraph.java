package Overlap;


import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;



public class ReadLargeGraph {

	public static ArrayList<Object> read(String[] strings) {	
		
		
		String pathname_cmty = strings[0];
		String pathname_edge = strings[1];
		//Read and compute the information of the initial network
		
		//node_data, edge_data. cmty_data, belongCmty, degree
		
		ArrayList<Integer[]> S_cmty = readfile(pathname_cmty,strings);				
		ArrayList<Integer[]> all_edge_data = readfile(pathname_edge,strings);
		
		
		ArrayList<Integer> node_data = new ArrayList<Integer>(); 
		ArrayList<ArrayList<Integer>> belongCmty = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer[]> edge_data = new ArrayList<Integer[]>();
		ArrayList<Integer> degree = new ArrayList<Integer>();
		
		Collections.sort(S_cmty, new Comparator<Integer[]>() {
			@Override
			public int compare(Integer[] o1, Integer[] o2) {
				// TODO Auto-generated method stub
				if(o1.length>o2.length) {
					return 1;
				}
				if(o1.length==o2.length) {
					return 0;
				}
				return -1;
			}			
		}); 
		
		// Remove duplicate communities
		ArrayList<Integer[]> cmty_data = new ArrayList<Integer[]>();
		for (int cmty = 0; cmty < S_cmty.size(); cmty++) {
			int flag = 0;
			for (int new_cmty = 0; new_cmty < cmty_data.size(); new_cmty++) {
				if (Arrays.equals(S_cmty.get(cmty), cmty_data.get(new_cmty))) {
					flag = 1;
					break;
				}
			}
			if (flag == 0) {
				cmty_data.add(S_cmty.get(cmty));
			}
		}
				
		// Nodes contained in the top 5000 community file
		for(int i=0;i<cmty_data.size();i++) {
			for(int j=0;j<cmty_data.get(i).length;j++) {				
				if(!node_data.contains(cmty_data.get(i)[j])) {
					node_data.add(cmty_data.get(i)[j]);
				}	
			}
		}
		ArrayList<ArrayList<Integer>> neighbors = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < node_data.size(); i++) {
			ArrayList<Integer> neighbor = new ArrayList<Integer>();
			neighbors.add(neighbor);
		}		
		// The communities of nodes belong to
		for(int i=0;i<node_data.size();i++) {
			ArrayList<Integer> each_node = new ArrayList<Integer>();
			for(int j=0;j<cmty_data.size();j++) {
				for(int k=0;k<cmty_data.get(j).length;k++) {
					if (node_data.get(i).equals(cmty_data.get(j)[k])) {
						each_node.add(j);
						break;
					}
				}
			}
			belongCmty.add(each_node);
		}
									
	
		// The degree of nodes(The index is same with the array of node)
		for(int i=0;i<node_data.size();i++) {
			degree.add(0);
		}		
		for(int i=0;i<all_edge_data.size();i++) {
			if((node_data.contains(all_edge_data.get(i)[0])) && (node_data.contains(all_edge_data.get(i)[1]))) {
				edge_data.add(all_edge_data.get(i));	
				neighbors.get(node_data.indexOf(all_edge_data.get(i)[0])).add(all_edge_data.get(i)[1]);
				neighbors.get(node_data.indexOf(all_edge_data.get(i)[1])).add(all_edge_data.get(i)[1]);
				degree.set(node_data.indexOf(all_edge_data.get(i)[0]), degree.get(node_data.indexOf(all_edge_data.get(i)[0]))+1);
				degree.set(node_data.indexOf(all_edge_data.get(i)[1]), degree.get(node_data.indexOf(all_edge_data.get(i)[1]))+1);
			}
		}
				
		ArrayList<Object> resultArrayList = new ArrayList<Object>();
		resultArrayList.add(node_data);
		resultArrayList.add(edge_data);
		resultArrayList.add(cmty_data);
		resultArrayList.add(belongCmty);
		resultArrayList.add(degree);
		resultArrayList.add(neighbors);
		
		return resultArrayList;
	}
	
	// Read community file and edge file
	public static ArrayList<Integer[]> readfile(String pathname,String[] string) {
		
		
		ArrayList<Integer[]> list = new ArrayList<Integer[]>();
						
		try (FileReader reader = new FileReader(pathname);
			 BufferedReader br = new BufferedReader(reader)){

			String line;
			// community file 
			if (pathname == string[0]) {				
				while ((line = br.readLine())!=null) {
					String[] source = line.split("\t");	
					Integer[] aa = new Integer[source.length];
					for(int i=0;i< source.length;i++) {
						aa[i] = Integer.valueOf(source[i]).intValue();
					}
					list.add(aa);									
				}				
			}
			// edge file 
			else {

				for (int i=0;i<4;i++) {
					br.readLine();
				}
				while ((line = br.readLine())!=null) {
					String[] source = line.split("\t");	
					Integer[] aa = new Integer[source.length];
					for(int i=0;i< source.length;i++) {
						aa[i] = Integer.valueOf(source[i]).intValue();
					}
					list.add(aa);				
				}			
			}
							
		} catch (Exception e) {
			 e.printStackTrace();
		}
		return list;
	}
}




