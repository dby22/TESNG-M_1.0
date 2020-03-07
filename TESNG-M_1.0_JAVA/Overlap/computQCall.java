package Overlap;


import java.util.ArrayList;
import java.util.concurrent.Callable;


public class computQCall implements Callable<ArrayList<Double[]>>{
	
	private  ArrayList<Integer> node_data;
	private  ArrayList<Integer[]> edge_data;
	private  ArrayList<Integer[]> cmty_data;
	private  ArrayList<ArrayList<Integer>> belongCmty;
	private  ArrayList<Integer> degree;
	private  ArrayList<Double[]> threadQ = new ArrayList<Double[]>();
	
	
	public computQCall(ArrayList<Integer>n,ArrayList<Integer[]>e,
			ArrayList<Integer[]>c,ArrayList<ArrayList<Integer>> bc,ArrayList<Integer> dg) {
		
		node_data = n;
		edge_data = e;
		cmty_data = c;
		belongCmty = bc;
		degree = dg;
	}


	@Override
	public ArrayList<Double[]> call() throws Exception {
		// TODO Auto-generated method stub
			for(int i = 0;i<cmty_data.size();i++) {
				double subQ1 = 0;
				double subQ2 = 0;
				Double[] eachcmty = new Double[2];				
				for (int v=0;v<cmty_data.get(i).length;v++) {
					for(int w=0;w<cmty_data.get(i).length;w++) {
						double A_vw = 0;
												
						double k_v = degree.get(node_data.indexOf(cmty_data.get(i)[v])); // Degree of v
						double k_w = degree.get(node_data.indexOf(cmty_data.get(i)[w])); // Degree of w
						
						double o_v = belongCmty.get(node_data.indexOf(cmty_data.get(i)[v])).size();  //The size of the community which the nodes v belong to 
						double o_w = belongCmty.get(node_data.indexOf(cmty_data.get(i)[w])).size();  //The size of the community which the nodes v belong to		
						
						for(int edge=0;edge<edge_data.size();edge++) {
							if((edge_data.get(edge)[0].equals(cmty_data.get(i)[w]) && edge_data.get(edge)[1].equals(cmty_data.get(i)[v]))
									|| (edge_data.get(edge)[1].equals(cmty_data.get(i)[w]) && edge_data.get(edge)[0].equals(cmty_data.get(i)[v]))) {
								A_vw = 1;
								break;
							}
						}
												
						subQ1 += A_vw/(o_v*o_w);
						subQ2 += (k_v*k_w)/(2*o_v*o_w);												
					 }			
				}	
				eachcmty[0] = subQ1;
				eachcmty[1] = subQ2;				
				threadQ.add(eachcmty);
			}			
	return threadQ;
	}
}
