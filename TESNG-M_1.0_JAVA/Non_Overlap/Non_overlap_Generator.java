package Non_Overlap;

// The main flow functions of the generator, including the evolution of nodes and edges, and the calculation of modularity
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Non_overlap_Generator {

	@SuppressWarnings({ "static-access", "unchecked" })
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		//Initialize parameters and maintain the variables node_data, edge_data, cmty_data, belong_data, detail_data 
		int N = 10;
		// Three data sets
		//String string = "football";
		//String string = "polbooks";
		String string = "netscience";
		
		String pathnameString = "./data/" + string + ".txt";
		ReadSmallGraph rGraph = new ReadSmallGraph();
		ArrayList<Object> tmpArrayList = rGraph.readSmallGraph(pathnameString);
		ArrayList<Integer> node_data = (ArrayList<Integer>) tmpArrayList.get(0);
		ArrayList<Integer[]> edge_data = (ArrayList<Integer[]>) tmpArrayList.get(1);
		ArrayList<ArrayList<Integer>> cmty_data = (ArrayList<ArrayList<Integer>>) tmpArrayList.get(2); 
		ArrayList<Integer> belongcmty = (ArrayList<Integer>) tmpArrayList.get(3);
		ArrayList<ArrayList<ArrayList<Integer[]>>> detail_edge = (ArrayList<ArrayList<ArrayList<Integer[]>>>) tmpArrayList.get(4);		
		ArrayList<Double> aim_Qlist = produceRandDoubles(N);
		Random r = new Random(1);
		double Q=0;
		int T = 20;
		//Generation process
		for(int count=0;count<N;count++) {

			//Expected modularity
			double aim_Q = aim_Qlist.get(count);
			
			ArrayList<double[]> q_list = new ArrayList<double[]>();
			//Calculate initial modularity
			if (count==0) {
				 q_list = conputQ(edge_data,cmty_data,detail_edge);	
				 for (int i = 0; i < q_list.size(); i++) {
					Q += q_list.get(i)[0]/edge_data.size() - Math.pow(q_list.get(i)[1]/(edge_data.size()*edge_data.size()), 2);
				 }
			}	

			//If the difference between the initial modularity and the expected modularity is less than the pre-set threshold, 
			//the generation process will be terminated, otherwise the nodes will be changed.
			if(Math.abs(aim_Q-Q)<0.001) {
				break;
			}
			else {
				changNode(node_data,edge_data,cmty_data,belongcmty,detail_edge,r);
				q_list = conputQ(edge_data,cmty_data,detail_edge);	
				Q = 0;
				for (int i = 0; i < q_list.size(); i++) {
					Q += q_list.get(i)[0]/edge_data.size() - Math.pow(q_list.get(i)[1]/(edge_data.size()*edge_data.size()), 2);
				 }
			}
					
			//If the difference between the Nodechange modularity and the expected modularity is less than the pre-set threshold, 
			//the generation process will be terminated, otherwise the edge will be changed.
			if(Math.abs(aim_Q-Q)<0.001) {
				break;
			}
			else {
				changEdge(node_data, edge_data, belongcmty, cmty_data, detail_edge, aim_Q, Q, r, T, q_list);
				Q = 0;
				for (int i = 0; i < q_list.size(); i++) {
					Q += q_list.get(i)[0]/edge_data.size() - Math.pow(q_list.get(i)[1]/(edge_data.size()*edge_data.size()), 2);
				 }
				
			}
			//The social network that meets the expected modularity will be exported to the GML file 			
			writeToGml(node_data, edge_data, belongcmty,count,string);
		}
		
	}
	
	
	//Write to GML
	public static void writeToGml(ArrayList<Integer> node,ArrayList<Integer[]> edge, ArrayList<Integer> belongcmty,int count,String pathname) throws IOException {
		
		if (pathname.equals("polbooks")) {
			pathname = "./generated networks/" + pathname+"_"+count+".gml";
			FileWriter fileWriter = new FileWriter(new File(pathname));
			String result = "generated by dby\r\n" +"graph\r\n" + "[\r\n" + "  directed 0\r\n";
			for(int i=0;i<node.size();i++){
				result += "  node\r\n" + 
						  "  [\r\n" + "    id " + node.get(i) + "\r\n"+
						  			  "    label "+ belongcmty.get(i)+"\r\n" + 
						  			  "    value "+ belongcmty.get(i)+"\r\n" + 
						  "  ]\r\n";
			}
			for(int i=0;i<edge.size();i++) {
				result += "  edge\r\n" + 
						  "  [\r\n" + 
						  "    source "+edge.get(i)[0]+"\r\n"+
						  "    target "+edge.get(i)[1]+"\r\n"+
						  "  ]\r\n";
			}	
			
			fileWriter.write(result);
			fileWriter.close();
		}
		else {
			pathname = "./generated networks/" + pathname+"_"+count+".gml";
			FileWriter fileWriter = new FileWriter(new File(pathname));
			String result = "generated by dby\r\n" +"graph\r\n" + "[\r\n" + "  directed 0\r\n";
			for(int i=0;i<node.size();i++){
				result += "  node\r\n" + 
						  "  [\r\n" + "    id " + node.get(i) + "\r\n"+
						  			  "    label "+ belongcmty.get(i)+"\r\n" + 						  			 
						  "  ]\r\n";
			}
			for(int i=0;i<edge.size();i++) {
				result += "  edge\r\n" + 
						  "  [\r\n" + 
						  "    source "+edge.get(i)[0]+"\r\n"+
						  "    target "+edge.get(i)[1]+"\r\n"+
						  "  ]\r\n";
			}	
			
			fileWriter.write(result);
			fileWriter.close();
		}

	}
	
	
	
	//Edges change
	public static void changEdge(ArrayList<Integer> node_data,ArrayList<Integer[]> edge_data,ArrayList<Integer> belongcmty,
			ArrayList<ArrayList<Integer>> cmty_data,ArrayList<ArrayList<ArrayList<Integer[]>>> detail_edge,
			double aim_Q,double Q,Random r,int T, ArrayList<double[]> q_list) throws InterruptedException {
		
		double temp_Q = 0;
		int t = 0;
		for(t=0;t<100000;t++) {
			if (Math.abs(aim_Q-Q)<=0.001) {			
				break;
			}
			else {
				//Randomly select two different nodes
				int nodeAindex = 0;
				int nodeBindex = 0;		
				nodeAindex = r.nextInt(node_data.size());
				nodeBindex = r.nextInt(node_data.size());	
				while (true){			
					if (nodeAindex != nodeBindex) {
						break;
					}
					nodeBindex = r.nextInt(node_data.size());
				}						
				//Get the name of the nodes and the communities them belong to based on the index
				int node_a = node_data.get(nodeAindex);
				int node_b = node_data.get(nodeBindex);				
				int cmty_a = belongcmty.get(nodeAindex);
				int cmty_b = belongcmty.get(nodeBindex);														
				//Determine if there is a edge between randomly selected two nodes 
				int flag1=1;	                        		
				for(int j=0;j<edge_data.size();j++) {
					if((edge_data.get(j)[0].equals(node_a) && edge_data.get(j)[1].equals(node_b)) ||
							(edge_data.get(j)[0].equals(node_b) && edge_data.get(j)[1].equals(node_a))) {	
						flag1 = 0;                      											
						break;
					}
				}												
				int flag = 0;                         
				if(cmty_a==cmty_b) {
					flag = 1;                        			
				}
				//record the history of the q_list
				ArrayList<double[]> q_list_copyArrayList = new ArrayList<double[]>();
				for (int i = 0; i < q_list.size(); i++) {
					q_list_copyArrayList.add(q_list.get(i));
				}
				
				//Calculate the modularity after flipping the connection relationship
				computTempQ(flag, flag1, cmty_a, cmty_b, q_list);									
				
				for (int i = 0; i < q_list.size(); i++) {
					temp_Q += q_list.get(i)[0]/edge_data.size() - Math.pow(q_list.get(i)[1]/(edge_data.size()*edge_data.size()), 2);
				}
								
				if (Math.abs(temp_Q-aim_Q)<Math.abs(Q-aim_Q)) {					
					Q = temp_Q;  
					//Update edge_data
					if(flag1==0) {
						for(int i=0;i<edge_data.size();i++) {
							if((edge_data.get(i)[0].equals(node_a) && edge_data.get(i)[1].equals(node_b)) ||
									(edge_data.get(i)[0].equals(node_b) && edge_data.get(i)[1].equals(node_a))) {
								edge_data.remove(i);
							}
						}
					}
					else {
						Integer[] tmp = {node_a,node_b};
						edge_data.add(tmp);
					}
					//Update detail_data
					for(int i=0;i<cmty_data.size();i++) {						
						if(cmty_a == i || cmty_b == i) { 
							if(flag == 0) {	
								if(flag1 == 0) {
									for(int j=0;j<detail_edge.get(i).get(1).size();j++) {
										if((detail_edge.get(i).get(1).get(j)[0].equals(node_a) && detail_edge.get(i).get(1).get(j)[1].equals(node_b)) ||
												(detail_edge.get(i).get(1).get(j)[0].equals(node_b) && detail_edge.get(i).get(1).get(j)[1].equals(node_a))) {
											detail_edge.get(i).get(1).remove(j);
										}
									}
								}
								else {
									Integer[] tmp = {node_a,node_b};
									detail_edge.get(i).get(1).add(tmp);	
								}
							}
							else {
								if(flag1 == 0) {
									for(int j=0;j<detail_edge.get(i).get(0).size();j++) {
										if((detail_edge.get(i).get(0).get(j)[0].equals(node_a) && detail_edge.get(i).get(0).get(j)[1].equals(node_b)) ||
												(detail_edge.get(i).get(0).get(j)[0].equals(node_b) && detail_edge.get(i).get(0).get(j)[1].equals(node_a))) {
											detail_edge.get(i).get(0).remove(j);
										}
									}
								}
								else {
									Integer[] tmp = {node_a,node_b};
									detail_edge.get(i).get(0).add(tmp);
								}
							}
						}
					}			
				}	
				else { 
					double r1 = r.nextDouble();
					if(r1<Math.pow(Math.E, -t/T)) {
						Q = temp_Q;  
						//Update edge_data
						if(flag1==0) {
							for(int i=0;i<edge_data.size();i++) {
								if((edge_data.get(i)[0].equals(node_a) && edge_data.get(i)[1].equals(node_b)) ||
										(edge_data.get(i)[0].equals(node_b) && edge_data.get(i)[1].equals(node_a))) {
									edge_data.remove(i);
								}
							}
						}
						else {
							Integer[] tmp = {node_a,node_b};
							edge_data.add(tmp);
						}	
						//Update detail_data
						for(int i=0;i<cmty_data.size();i++) {						
							if(cmty_a == i || cmty_b == i) { 
								if(flag == 0) {	
									if(flag1 == 0) {
										for(int j=0;j<detail_edge.get(i).get(1).size();j++) {
											if((detail_edge.get(i).get(1).get(j)[0].equals(node_a) && detail_edge.get(i).get(1).get(j)[1].equals(node_b)) ||
													(detail_edge.get(i).get(1).get(j)[0].equals(node_b) && detail_edge.get(i).get(1).get(j)[1].equals(node_a))) {
												detail_edge.get(i).get(1).remove(j);
											}
										}
									}
									else {
										Integer[] tmp = {node_a,node_b};
										detail_edge.get(i).get(1).add(tmp);	
									}
								}
								else {
									if(flag1 == 0) {
										for(int j=0;j<detail_edge.get(i).get(0).size();j++) {
											if((detail_edge.get(i).get(0).get(j)[0].equals(node_a) && detail_edge.get(i).get(0).get(j)[1].equals(node_b)) ||
													(detail_edge.get(i).get(0).get(j)[0].equals(node_b) && detail_edge.get(i).get(0).get(j)[1].equals(node_a))) {
												detail_edge.get(i).get(0).remove(j);
											}
										}
									}
									else {
										Integer[] tmp = {node_a,node_b};
										detail_edge.get(i).get(0).add(tmp);
									}
								}
							}			
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
	
	
	//Calculate modularity after flipping
	 public static void computTempQ(int flag, int flag1,int cmty_a,int cmty_b,ArrayList<double[]> q_list) {	
		
		if(flag == 0) {	
			if(flag1 == 0) {
				q_list.get(cmty_a)[1]--;
				q_list.get(cmty_b)[1]--;
			}
			else {
				q_list.get(cmty_a)[1]++;
				q_list.get(cmty_b)[1]++;
			}
		}
		else {
			if(flag1 == 0) {
				q_list.get(cmty_a)[0]--;
				q_list.get(cmty_a)[1] -= 2;
			}
			else {
				q_list.get(cmty_a)[0]++;
				q_list.get(cmty_a)[1] += 2;
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
		
	//Nodes change
	public static void changNode(ArrayList<Integer> node_data,ArrayList<Integer[]> edge_data,
				ArrayList<ArrayList<Integer>> cmty_data,ArrayList<Integer> belongCmty,
				ArrayList<ArrayList<ArrayList<Integer[]>>> detail_edge, Random r) {
			// TODO Auto-generated method stub
			
			double random_p = r.nextDouble();
			double p = 0.6;
			int maxcount = (int) Math.ceil(node_data.size()*0.02); 		
			
			if(random_p<p) {
				//When insert a node we update the information of the networks			
				int insertcount = r.nextInt(maxcount);	
				int maxID = Collections.max(node_data);							
				
				if (insertcount>0) {
					for (int i=0;i<insertcount;i++) {
						int insertNode = maxID+1+i; 
						//Update node_data
						node_data.add(insertNode); 	
						int insertCmty = r.nextInt(cmty_data.size()); 						
						for(int j=0;j<cmty_data.get(insertCmty).size();j++) {
							Integer[] insertEdge = {insertNode,cmty_data.get(insertCmty).get(j)};
							//Update edge_data
							edge_data.add(insertEdge); 
							//Update detail_data
							detail_edge.get(insertCmty).get(0).add(insertEdge);
						}		
						//Update cmty_data
						cmty_data.get(insertCmty).add(insertNode);	
						//Update belongCmty
						belongCmty.add(insertCmty);										
					}	
				}
				
			}
			else {
				int deleteCount = r.nextInt(maxcount); 
				if (deleteCount>0) {
					for(int i=0;i<deleteCount;i++) {							
						int deleteIndex = r.nextInt(node_data.size()); 				
						int delete_node_name = node_data.get(deleteIndex); 											
						int delete_node_cmty = belongCmty.get(deleteIndex);
						//Update cmty_data
						for(int j=0;j<cmty_data.get(delete_node_cmty).size();j++) {
							if(cmty_data.get(delete_node_cmty).get(j).equals(delete_node_name) ) {
								cmty_data.get(delete_node_cmty).remove(j--);						
							}
						}
						//Update edge_data	
						for(int j=0;j<edge_data.size();j++) {
							if(edge_data.get(j)[0].equals(delete_node_name) || edge_data.get(j)[1].equals(delete_node_name)) {
								edge_data.remove(j--);
							}
						}
						//Update detail_data
						for(int j=0;j<detail_edge.size();j++) {
							for(int k=0;k<detail_edge.get(j).size();k++) {
								for(int h=0;h<detail_edge.get(j).get(k).size();h++) {
									if (detail_edge.get(j).get(k).get(h)[0].equals(delete_node_name) || detail_edge.get(j).get(k).get(h)[1].equals(delete_node_name)) {
										detail_edge.get(j).get(k).remove(h--);
										
									}
								}
							}
						}
						//Update node_data
						node_data.remove(deleteIndex);
						//Update belongCmty
						belongCmty.remove(deleteIndex); 						
					}
				}
				
			}		
		}
	
	//Calculate modularity
	public static ArrayList<double[]> conputQ(ArrayList<Integer[]> edge_data,
			ArrayList<ArrayList<Integer>> cmty_data,ArrayList<ArrayList<ArrayList<Integer[]>>> detail_edge) {
		ArrayList<double[]> q_list = new ArrayList<double[]>();
		for(int i=0;i<cmty_data.size();i++) {
			int inedge =  detail_edge.get(i).get(0).size();
			int betweenEdge = detail_edge.get(i).get(1).size();			
			double[] sub = {inedge/2,(2*inedge+betweenEdge)/4};
			q_list.add(sub);
		}
		return q_list;
		
	}
}

