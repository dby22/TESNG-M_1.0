package Overlap;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class computTempQCall implements Callable<Double>{

	
	private  ArrayList<Integer> node_data;
	private  ArrayList<Integer[]> cmty_data;
	private  ArrayList<ArrayList<Integer>> belongCmty;
	private  ArrayList<Integer> degree;
	private  int flag;
	private int nodeindex;
	private int nodeflag;
	private int connect;
	private int cmtyindex;
	
	
	//Constructed function
	public computTempQCall(ArrayList<Integer> n,  ArrayList<Integer[]> c, ArrayList<Integer> d, ArrayList<ArrayList<Integer>> bc,
			int nd, int nf, int f, int ct, int ci) {
		// TODO Auto-generated constructor stub
		node_data = n;
		cmty_data = c;
		belongCmty = bc;
		degree = d;
		nodeindex = nd;
		nodeflag = nf;
		flag = f;
		connect = ct;
		cmtyindex = ci;
	}

	//Implementation interface
	@Override
	public Double call() throws Exception {
		// TODO Auto-generated method stub
		
		double sub = 0;
		double k = 0;
		if (flag == 1) {
			if (connect == 1) {
				k = degree.get(nodeindex) - 1;
			}
			else {
				k = degree.get(nodeindex) + 1;
			}			
		}
		else {
			k = degree.get(nodeindex);
		}		
		double o = belongCmty.get(nodeindex).size();
		
		for (int i = 0; i < cmty_data.get(cmtyindex).length; i++) {			
			double k_i = 0;
			int nodei = cmty_data.get(cmtyindex)[i];
			if (nodei == nodeflag) {
				if (connect == 1) {
					k_i = degree.get(node_data.indexOf(nodei)) -1;
				}
				else {
					k_i = degree.get(node_data.indexOf(nodei)) + 1;
				}
			}
			else {
				k_i = degree.get(node_data.indexOf(nodei));				
			}			
			double o_i = belongCmty.get(node_data.indexOf(cmty_data.get(cmtyindex)[i])).size();
			
			sub += (k*k_i)/(2*o*o_i);
		}
		return sub;
	}

}
