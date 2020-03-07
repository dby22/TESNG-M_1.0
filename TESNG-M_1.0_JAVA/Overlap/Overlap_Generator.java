package Overlap;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Overlap_Generator {
	

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {	
		//Initialize parameters and maintain the variables node_data, edge_data, cmty_data, belongCmty, degree as the network at every time step
		
		int N = 10;
		String [] strings = {"./data/com-amazon.top5000.cmty.txt","./data/com-amazon.ungraph.txt"};
		//String [] strings = {"./data/com-dblp.top5000.cmty.txt","./data/com-dblp.ungraph.txt"};
		maximalConnect rg = new maximalConnect();
				
		@SuppressWarnings("static-access")
		ArrayList<Object> tmp = rg.method(strings);				
		ArrayList<Integer> node_data = (ArrayList<Integer>) tmp.get(0); 						
		ArrayList<Integer[]> edge_data = (ArrayList<Integer[]>) tmp.get(1);						
		ArrayList<Integer[]> cmty_data = (ArrayList<Integer[]>) tmp.get(2);	
		ArrayList<ArrayList<Integer>> belongCmty = (ArrayList<ArrayList<Integer>>) tmp.get(3);  
		ArrayList<Integer> degree = (ArrayList<Integer>) tmp.get(4);		
		ArrayList<Double> randomDoubles = produceRandDoubles(N);								
		Random r = new Random(1);    	
		double p_node=0.6;				
		ArrayList<Double[]> q_list = null;		
		double sum_Q=0;		
		
		//Generation process
		for(int count=0;count<N;count++) {
						
			//Calculate initial modularity
			if(count == 0) {
				q_list = computQ(node_data, edge_data, cmty_data, belongCmty, degree);												
			}
			sum_Q = 0;
			for(int i=0;i<q_list.size();i++) {				
				sum_Q = sum_Q + (q_list.get(i)[0] - q_list.get(i)[1]/(double)edge_data.size());				
			}
			sum_Q = sum_Q/(double)(2*edge_data.size());
			
			//Expected modularity
			double aim_Q = randomDoubles.get(count);	
			
			//If the difference between the initial modularity and the expected modularity is less than the pre-set threshold, 
			//the generation process will be terminated, otherwise the nodes will be changed.						
			if (Math.abs(sum_Q-aim_Q)<0.001) {
				break;
			}			
			else {		
				NodeChange(p_node, r, node_data, edge_data, cmty_data, degree, belongCmty);				
				q_list = computQ(node_data, edge_data, cmty_data, belongCmty, degree);
				sum_Q = 0;
				for(int i=0;i<q_list.size();i++) {					
					sum_Q = sum_Q + (q_list.get(i)[0] - q_list.get(i)[1]/(double)edge_data.size());															
				}	
				sum_Q = sum_Q/(double)(2*edge_data.size());		
			}
						
			//If the difference between the Node change modularity and the expected modularity is less than the pre-set threshold, 
			//the generation process will be terminated, otherwise the edge will be changed.
			
			if (Math.abs(sum_Q-aim_Q)<0.001) {
				break;
			}
			else {				
				EdgeChange(node_data, edge_data, cmty_data, belongCmty, degree, q_list, r, sum_Q, aim_Q);	
				sum_Q = 0;
				for (int i = 0; i < q_list.size(); i++) {
					sum_Q = sum_Q + (q_list.get(i)[0] - q_list.get(i)[1]/(double)edge_data.size()); 
				}
				sum_Q = sum_Q/(double)(2*edge_data.size());
			}
			//The social network that meets the expected modularity will be exported to the GML file 
			writeToGml(node_data, edge_data, belongCmty, count, strings);
		}
	}
	
	// compute the change of modularity because of node change
	public static void computNodechangeQ(ArrayList<Integer> node_data, ArrayList<Integer[]> edge_data,
			ArrayList<Integer[]> cmty_data, ArrayList<ArrayList<Integer>> belongCmty, ArrayList<Integer> degree, 
			ArrayList<Double[]> q_list, ArrayList<Integer> changeCmty) throws InterruptedException, ExecutionException{
		int num = changeCmty.size();
		if (num>0) {
			ArrayList<ArrayList<ArrayList<Object>>> sortedArrayList;
			ExecutorService pool;		
			
			// Construct different thread pools
			if (num>40) {
				pool = Executors.newFixedThreadPool(40);
				sortedArrayList = new ArrayList<ArrayList<ArrayList<Object>>>(40);
				for(int i=0;i<40;i++) {
					int index = changeCmty.get(i);
					Integer[] integers = cmty_data.get(index);
					ArrayList<Object> arrayList = new ArrayList<Object>();
					arrayList.add(index);
					arrayList.add(integers);
					ArrayList<ArrayList<Object>> tmpArrayList = new ArrayList<ArrayList<Object>>();
					tmpArrayList.add(arrayList);
					sortedArrayList.add(tmpArrayList);
				}		
				for(int i=40;i<cmty_data.size();i++) {
					int index = changeCmty.get(i);
					Integer[] integers = cmty_data.get(index);
					ArrayList<Object> arrayList = new ArrayList<Object>();
					arrayList.add(index);
					arrayList.add(integers);
					sortedArrayList.get(i).add(arrayList);
				}			
			}
			else {
				pool = Executors.newFixedThreadPool(num);
				sortedArrayList = new ArrayList<ArrayList<ArrayList<Object>>>(num);
				for (int i = 0; i < num; i++) {
					int index = changeCmty.get(i);
					Integer[] integers = cmty_data.get(index);
					ArrayList<Object> arrayList = new ArrayList<Object>();
					arrayList.add(index);
					arrayList.add(integers);
					ArrayList<ArrayList<Object>> tmpArrayList = new ArrayList<ArrayList<Object>>();
					tmpArrayList.add(arrayList);
					sortedArrayList.add(tmpArrayList);
				}
			}
			// start 
			ArrayList<Future<ArrayList<ArrayList<Object>>>> list = new ArrayList<Future<ArrayList<ArrayList<Object>>>>();
			for(int i=0;i<sortedArrayList.size();i++) {		
				computNodeQ c1 = new computNodeQ(node_data, edge_data, sortedArrayList.get(i),belongCmty,degree);
				Future<ArrayList<ArrayList<Object>>> f1 = pool.submit(c1);
				list.add(f1);
			}			
			pool.shutdown();
			// Sorting out calculation results, reset the value of modularity
			ArrayList<ArrayList<ArrayList<Object>>> resultlist = new ArrayList<ArrayList<ArrayList<Object>>>();
			for(int i=0;i<list.size();i++) {	
				resultlist.add(list.get(i).get());
			}
			for (int i = 0; i < resultlist.size(); i++) {
				for (int j = 0; j < resultlist.get(i).size(); j++) {
					q_list.set((int) resultlist.get(i).get(j).get(0), (Double[]) resultlist.get(i).get(j).get(1));
				}
			}
		}		
	}
		
	//Write to GML
	public static void writeToGml(ArrayList<Integer> node,ArrayList<Integer[]> edge, ArrayList<ArrayList<Integer>> belongcmty,int count,String[] strings) throws IOException {
		
		String pathname;
		if (strings[0].contains("dblp")) {
			pathname = "./generated networks/dblp"+count+".txt";
		}
		else {
			pathname = "./generated networks/amazon"+count+".txt";
		}
		
		FileWriter fileWriter = new FileWriter(new File(pathname));
		String result = "generated by dby\r\n"+"graph\r\n" + "[\r\n" + "  directed 0\r\n";
		for(int i=0;i<node.size();i++){
			String baseString = "";
			for (int j = 0; j < belongcmty.get(i).size(); j++) {
				if (j == belongcmty.get(i).size()-1) {
					baseString = baseString + belongcmty.get(i).get(j);
				}
				else {
					baseString = baseString + belongcmty.get(i).get(j) + ",";
				}				
			}
			result += "  node\r\n" + 
					  "  [\r\n" + "    id " + node.get(i) + "\r\n"+
					  			  "    label " + baseString + "\r\n"+ 
					  			  "    value "+ baseString +"\r\n" + 
					  "  ]\r\n";
		}
		for(int i=0;i<edge.size();i++) {
			result += "edge\r\n" + 
					"  [\r\n" + 
					"    source "+edge.get(i)[0]+"\r\n"+
					"    target "+edge.get(i)[1]+"\r\n"+
					"  ]\r\n";
		}		
		fileWriter.write(result);
		fileWriter.close();
	}
	
	//Calculate modularity
	public static ArrayList<Double[]> computQ(ArrayList<Integer> node_data,
			ArrayList<Integer[]> edge_data,ArrayList<Integer[]> cmty_data,
			ArrayList<ArrayList<Integer>> belongCmty,ArrayList<Integer> degree) throws InterruptedException, ExecutionException {
					
		//Task allocation for multi-threading based on community size		
		ArrayList<ArrayList<Integer[]>> sortedArrayList = new ArrayList<ArrayList<Integer[]>>(40);
		int num = cmty_data.size();
		if (num>40) {
			for(int i=0;i<40;i++) {
				Integer[] integers = cmty_data.get(i);
				ArrayList<Integer[]> arrayList = new ArrayList<Integer[]>();
				arrayList.add(integers);
				sortedArrayList.add(arrayList);
			}		
			for(int i=40;i<cmty_data.size();i++) {
				if((i/40)%2 == 0) {
					sortedArrayList.get(i%40).add(cmty_data.get(i));
				}	
				else {
					sortedArrayList.get(39-i%40).add(cmty_data.get(i));
				}
			}
		}
		else {
			for(int i=0;i<num;i++) {
				Integer[] integers = cmty_data.get(i);
				ArrayList<Integer[]> arrayList = new ArrayList<Integer[]>();
				arrayList.add(integers);
				sortedArrayList.add(arrayList);
			}		
		}
		//Start multi-threading
		ExecutorService pool=Executors.newFixedThreadPool(40);
		ArrayList<ArrayList<Double[]>> resultlist = new ArrayList<ArrayList<Double[]>>();
		ArrayList<Future<ArrayList<Double[]>>> list = new ArrayList<Future<ArrayList<Double[]>>>();
		ArrayList<Double[]> finalArrayList = new ArrayList<Double[]>();
			
		for(int i=0;i<sortedArrayList.size();i++) {		
			computQCall c1 = new computQCall(node_data, edge_data, sortedArrayList.get(i), belongCmty, degree);
			Future<ArrayList<Double[]>> f1 = pool.submit(c1);
			list.add(f1);
		}			
		pool.shutdown();
		//Get the return result of multi-threading
		for(int i=0;i<list.size();i++) {	
			resultlist.add(list.get(i).get());
		}
		//Restore community distribution before grouping
		int max = 0;
		for(int i=0;i<sortedArrayList.size();i++) {
			if (sortedArrayList.get(i).size()>max) {
				max = sortedArrayList.get(i).size();
			}
		}
		int j=0;
		while (j<max) {
			if(j%2==0) {
				for(int i=0;i<resultlist.size();i++) {
					if (j<resultlist.get(i).size()) {
						finalArrayList.add(resultlist.get(i).get(j));
					}					
				}
				j++;			
			}
			else {
				for(int i=resultlist.size()-1;i>-1;i--) {
					if(j<resultlist.get(i).size()) {
						finalArrayList.add(resultlist.get(i).get(j));
					}				
				}
				j++;	
			}			  
		}	
		return finalArrayList;
	}
	
	//Nodes change
	public static ArrayList<Integer> NodeChange(double p,Random r,ArrayList<Integer> node_data,
			ArrayList<Integer[]> edge_data,ArrayList<Integer[]> cmty_data,ArrayList<Integer> degree,
			ArrayList<ArrayList<Integer>> belongCmty) {
		
		double random_p = r.nextDouble();
		int maxcount = (int) Math.ceil(node_data.size()*0.02);
		ArrayList<Integer> changeCmty = new ArrayList<Integer>();
		if(random_p<p) {
			//When insert a node we update the information of the networks
			int insertcount = r.nextInt(maxcount);	
			int maxID = Collections.max(node_data);										
			for (int i=1;i<insertcount+1;i++) {
				int insertNode = maxID+i; 
				//Update node_data
				node_data.add(insertNode);    	
				degree.add(0);
				int insertCmty = r.nextInt(cmty_data.size());
				changeCmty.add(insertCmty);
				for(int j=0;j<cmty_data.get(insertCmty).length;j++) {
					Integer[] insertEdge = {insertNode,cmty_data.get(insertCmty)[j]};
					//Update edge_data
					edge_data.add(insertEdge);  
					//Update degree
					degree.set(node_data.indexOf(cmty_data.get(insertCmty)[j]), degree.get(node_data.indexOf(cmty_data.get(insertCmty)[j]))+1);
					degree.set(node_data.indexOf(insertNode), degree.get(node_data.indexOf(insertNode))+1);
				}				
				//Update cmty_data 
				Integer[] newcmty = new Integer[cmty_data.get(insertCmty).length+1];
				System.arraycopy(cmty_data.get(insertCmty), 0, newcmty, 0, cmty_data.get(insertCmty).length);
				newcmty[cmty_data.get(insertCmty).length] = insertNode;
				cmty_data.set(insertCmty, newcmty);				
				//Update belongCmty information
				ArrayList<Integer> newbelong = new ArrayList<Integer>();
				newbelong.add(insertCmty);
				belongCmty.add(newbelong);				
			}		
		}
		else {
			//When delete a node we update the information of the networks
			int deleteCount = r.nextInt(maxcount); 		
			for(int i=0;i<deleteCount;i++) {								
				int deleteIndex = r.nextInt(node_data.size()); 				
				int delete_node_name = node_data.get(deleteIndex); 															
				ArrayList<Integer> node_belongCmty = belongCmty.get(deleteIndex); 	
				for (int j = 0; j < node_belongCmty.size(); j++) {
					changeCmty.add(node_belongCmty.get(j));
				}
				//Update cmty_data 
				for(int j=0;j<node_belongCmty.size();j++) {
					int resultname_cmty = 0;
					Integer[] newcmty = new Integer[cmty_data.get(node_belongCmty.get(j)).length-1];
					for(int k=0;k<cmty_data.get(node_belongCmty.get(j)).length;k++) {
						if (!cmty_data.get(node_belongCmty.get(j))[k].equals(delete_node_name) ) {
							newcmty[resultname_cmty] = cmty_data.get(node_belongCmty.get(j))[k];
							resultname_cmty ++;
						}						
					}
					cmty_data.set(node_belongCmty.get(j), newcmty);
				}
				//Update edge_data
				for(int j=0;j<edge_data.size();j++) {
					if(delete_node_name == edge_data.get(j)[0] || delete_node_name == edge_data.get(j)[1]) {
						for(int k=0;k<edge_data.get(j).length;k++) {
							if (!edge_data.get(j)[k].equals(delete_node_name)) {
								degree.set(node_data.indexOf(edge_data.get(j)[k]), degree.get(node_data.indexOf(edge_data.get(j)[k]))-1);
							}
							// Record the community of the node with edge connection with the deleted node
							for (int k2 = 0; k2 < belongCmty.get(node_data.indexOf(edge_data.get(j)[k])).size(); k2++) {
								changeCmty.add(belongCmty.get(node_data.indexOf(edge_data.get(j)[k])).get(k2));
							}
						}
						edge_data.remove(j--);						
					}
				}
				//Update node_data
				node_data.remove(deleteIndex); 
				//Update cmty_data
				belongCmty.remove(deleteIndex); 
				//Update degree
				degree.remove(deleteIndex);
			}
		}
		// Duplicate removal
		Set<Integer> changIntegers = new HashSet<Integer>(changeCmty);
		ArrayList<Integer> newchanArrayList = new ArrayList<Integer>(changIntegers);
		return newchanArrayList;
	}
		
	//Edges change
	public static void EdgeChange(ArrayList<Integer> node_data, ArrayList<Integer[]> edge_data, 
			ArrayList<Integer[]> cmty_data,ArrayList<ArrayList<Integer>> belongCmty,
			ArrayList<Integer> degree,ArrayList<Double[]> q_list,Random r, double sum_Q, double aim_Q) throws InterruptedException, ExecutionException {
		
		for(int t=0;t<10000;t++) {			
			if(Math.abs(sum_Q-aim_Q)<0.001) {				
				break;
			}
			else {				
				//Randomly select two different nodes
				int nodeAindex = r.nextInt(node_data.size());
				int nodeBindex = r.nextInt(node_data.size());
				double temp_Q = 0;
				while (true){			
					if (nodeAindex != nodeBindex) {
						break;
					}
					nodeBindex = r.nextInt(node_data.size());
				}	
				
				//Determine if there is a edge between randomly selected two nodes 
				int flag = 0;
				double m = edge_data.size();
				int nodeA = node_data.get(nodeAindex);
				int nodeB = node_data.get(nodeBindex);
				for (int edge=0;edge<edge_data.size();edge++) {
					if((edge_data.get(edge)[0].equals(nodeA) && edge_data.get(edge)[1].equals(nodeB) )
							|| (edge_data.get(edge)[1].equals(nodeB)  && edge_data.get(edge)[0].equals(nodeA))) {
					flag = 1; 
					m = edge_data.size()-1; 
					break;
					}
				}
				if (flag == 0) {
					m = edge_data.size()+1;
				} 				
				//record the history of the q_list
				ArrayList<Double[]> q_list_copyArrayList = new ArrayList<Double[]>();
				for (int i = 0; i < q_list.size(); i++) {
					q_list_copyArrayList.add(q_list.get(i));
				}
				//Calculate the modularity after flip the connection relationship	
				computTempQ(nodeAindex, nodeBindex, node_data, edge_data, cmty_data, belongCmty, degree, q_list, flag);	
							
				temp_Q = 0;				
				for (int i = 0; i < q_list.size(); i++) {
					temp_Q = temp_Q + (q_list.get(i)[0] - q_list.get(i)[1]/m);				
				}	
				temp_Q = temp_Q/(2*m);	
				
				//Determine whether to receive the flip operation according to the modularity after the flip
				if (Math.abs(temp_Q-aim_Q) < Math.abs(sum_Q-aim_Q)) {
					// If the module difference is smaller after flipping, accept the flip operation						
					sum_Q = temp_Q;
					if (flag == 1) {
						//If there is an edge in the network before the flipping, delete the edge and reduce the degree of the two nodes by one.
						for(int edge=0;edge<edge_data.size();edge++) {
							if((edge_data.get(edge)[0].equals(nodeA) && edge_data.get(edge)[1].equals(nodeB))
								|| (edge_data.get(edge)[0].equals(nodeB) && edge_data.get(edge)[1].equals(nodeA))) {
								edge_data.remove(edge);
								break;
							}
						}
						degree.set(nodeAindex, degree.get(nodeAindex)-1);
						degree.set(nodeBindex, degree.get(nodeBindex)-1);
					}	
					else {
						//If there are no edges between the two nodes in the network before the flipping, add the edges and increase the degree of the two nodes by one..						
						Integer[] tmp1 = {nodeA,nodeB};
						edge_data.add(tmp1);						
						degree.set(nodeAindex, degree.get(nodeAindex)+1);
						degree.set(nodeBindex, degree.get(nodeBindex)+1);
					}																											
				}
				else {
					//If the module difference is larger after flipping, accept the flip operation with decreasing probability	
					double r1 = r.nextDouble();
					if(r1<Math.pow(Math.E, -t/20)) {						
						sum_Q = temp_Q;
						if (flag == 1) {
							//If there is an edge in the network before the flipping, delete the edge and reduce the degree of the two nodes by one.
							for(int edge=0;edge<edge_data.size();edge++) {
								if((edge_data.get(edge)[0].equals(nodeA) && edge_data.get(edge)[1].equals(nodeB))
									|| (edge_data.get(edge)[0].equals(nodeB) && edge_data.get(edge)[1].equals(nodeA))) {
									edge_data.remove(edge);
									break;
								}
							}
							degree.set(nodeAindex, degree.get(nodeAindex)-1);
							degree.set(nodeBindex, degree.get(nodeBindex)-1);
						}					
						else {
							//If there are no edges between the two nodes in the network before the flipping, add the edges and increase the degree of the two nodes by one..
							Integer[] tmp1 = {nodeA,nodeB};
							edge_data.add(tmp1);
							degree.set(nodeAindex, degree.get(nodeAindex)+1);
							degree.set(nodeBindex, degree.get(nodeBindex)+1);
						} 												
					}
					else {
						//Does not receive the flip operation, the variable q_list back to before flip								
						q_list.clear();
						for (int i = 0; i < q_list_copyArrayList.size(); i++) {
							q_list.add(q_list_copyArrayList.get(i));
						}												
					}
				}
			}			
		}			
	}
		
	//Calculate modularity after flip
	public static void computTempQ(int nodeAindex,int nodeBindex,ArrayList<Integer> node_data,
			ArrayList<Integer[]> edge_data,ArrayList<Integer[]> cmty_data,ArrayList<ArrayList<Integer>> belongCmty,
			ArrayList<Integer> degree,ArrayList<Double[]> q_list_nodechange, int flag) throws InterruptedException, ExecutionException {						
		
		// Determine the relationship of the connected nodes		
		ArrayList<Integer> cmtyA = belongCmty.get(nodeAindex);
		ArrayList<Integer> cmtyB = belongCmty.get(nodeBindex);
		
		
		for (int i = 0; i < cmtyA.size(); i++) {
			for (int j = 0; j < cmtyB.size(); j++) {
				
				// Part of the contribution value of the community of the two nodes before and after turning is calculated in parallel
				int A = cmtyA.get(i);
				int B = cmtyB.get(j);
				
				ExecutorService pool=Executors.newFixedThreadPool(4);	
				ArrayList<Future<Double>> list = new ArrayList<Future<Double>>();							
				computTempQCall c1 = new computTempQCall(node_data, cmty_data, degree, belongCmty, nodeAindex, nodeBindex, 0, flag, A);
				computTempQCall c2 = new computTempQCall(node_data, cmty_data, degree, belongCmty, nodeBindex, nodeAindex, 0, flag, B);
				
				computTempQCall c3 = new computTempQCall(node_data, cmty_data, degree, belongCmty, nodeAindex, nodeBindex, 1, flag, A);
				computTempQCall c4 = new computTempQCall(node_data, cmty_data, degree, belongCmty, nodeBindex, nodeAindex, 1, flag, B);

				Future<Double> f1 = pool.submit(c1);
				list.add(f1);
				Future<Double> f2 = pool.submit(c2);
				list.add(f2);
				Future<Double> f3 = pool.submit(c3);
				list.add(f3);
				Future<Double> f4 = pool.submit(c4);
				list.add(f4);
				pool.shutdown();
				
				ArrayList<Double> resultlist = new ArrayList<Double>();
				for(int k=0;k<list.size();k++) {	
					resultlist.add(list.get(k).get());
				}		
				double subA = resultlist.get(0);
				double subB = resultlist.get(1);
				double newsubA = resultlist.get(2);
				double newsubB = resultlist.get(3);
				
				
				// contribution of the communities of before change
				double beforechangeA0 = q_list_nodechange.get(A)[0];	
				double beforechangeA1 = q_list_nodechange.get(A)[1];
				double beforechangeB0 = q_list_nodechange.get(B)[0];
				double beforechangeB1 = q_list_nodechange.get(B)[1];			
				
				double Avv = degree.get(nodeAindex);
				double Bvv = degree.get(nodeBindex);
				double Anew_vv = 0;
				double Bnew_vv = 0;
				double k,newk;
				if (flag == 1) {
					Anew_vv = degree.get(nodeAindex)-1;
					Bnew_vv = degree.get(nodeBindex)-1;
					k = 1;
					newk = 0;
				}
				else {			
					Anew_vv = degree.get(nodeAindex)+1;				
					Bnew_vv = degree.get(nodeBindex)+1;
					k = 0;
					newk = 1;
				}
				
				// If two nodes are in the same community		
				if (A == B) {								
					double change0 = beforechangeA0 - k/(double)(belongCmty.get(nodeAindex).size()*belongCmty.get(nodeBindex).size()) 
							+ newk/(double)(belongCmty.get(nodeAindex).size()*belongCmty.get(nodeBindex).size());
					double change1 = beforechangeA1 - (2*(subA+subB) - Avv*Bvv - Avv*Avv/2 - Bvv*Bvv/2) 
							+ (2*(newsubA + newsubB) - Anew_vv*Bnew_vv - Anew_vv*Anew_vv/2 - Bnew_vv*Bnew_vv/2);	
					Double[] change = {change0,change1};
					q_list_nodechange.set(A, change);
				}
				else {										
					double changeA1 = beforechangeA1 - 2*subA + Avv*Avv/2 + 2*newsubA- Anew_vv*Anew_vv/2;
					double changeB1 = beforechangeB1 - 2*subB + Bvv*Bvv/2 + 2*newsubA- Bnew_vv*Bnew_vv/2;
					Double[] changeA = {beforechangeA0,changeA1};
					Double[] changeB = {beforechangeB0,changeB1};
					
					q_list_nodechange.set(A, changeA);
					q_list_nodechange.set(B, changeB);			
				}					
			}
		}	
	}
		
	//Generating expected modularity
	public static ArrayList<Double> produceRandDoubles(int N) {

		Random r = new Random(0);
 		double min = 0.3;
		double max = 0.7;
		ArrayList<Double> ranDoubles = new ArrayList<Double>();		
		for(int i=0;i<N;i++) {
			ranDoubles.add(min + r.nextDouble()*(max-min));
		}		
		return ranDoubles;
	}
}

