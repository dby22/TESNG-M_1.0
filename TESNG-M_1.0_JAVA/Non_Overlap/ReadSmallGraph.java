package Non_Overlap;


// 读取gml格式的文件
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ReadSmallGraph {

	public static ArrayList<Object> readSmallGraph(String pathname) throws FileNotFoundException, IOException {
		
		//Read and compute the information of the initial network
		
		ArrayList<Object> resultArrayList = new ArrayList<Object>();
		ArrayList<Integer>  node_data = new ArrayList<Integer>();		
		ArrayList<Integer[]> edge_data = new ArrayList<Integer[]>();
		ArrayList<ArrayList<Integer>> cmty_data = new ArrayList<ArrayList<Integer>>();
		ArrayList<String> belongcmty = new ArrayList<String>();
		ArrayList<Integer> newbelong = new ArrayList<Integer>();
		ArrayList<ArrayList<ArrayList<Integer[]>>> detaiList = new ArrayList<ArrayList<ArrayList<Integer[]>>>();

		try (FileReader reader = new FileReader(pathname);
				 BufferedReader br = new BufferedReader(reader)){
						
			// Remove header
			for(int i=0;i<4;i++) {
				br.readLine();
			}	
			// Different initial files are read in different ways
			if(pathname.equals("./data/netscience.txt")) {
				// Read nodes
				while(br.readLine().contains("node")) {				
					br.readLine();
					node_data.add(Integer.valueOf(br.readLine().split(" ")[5]).intValue());
					String string = br.readLine().split(" ")[6];						
					belongcmty.add(string.substring(0, string.length()-1));
					br.readLine();					
				}		
				//Read edge			
				br.readLine();
				Integer[] aa = new Integer[2];			
				aa[0] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
				aa[1] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
				br.readLine();
				br.readLine();
				edge_data.add(aa);
				while(br.readLine()!=null) {
					if(br.readLine()==null) {
						break;
					}
					Integer[] aa1 = new Integer[2];
					aa1[0] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
					aa1[1] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
					edge_data.add(aa1);
					br.readLine();	
					br.readLine();
				}
				
			}
			else {
				//Read nodes	
				while(br.readLine().contains("node")) {				
						br.readLine();
						node_data.add(Integer.valueOf(br.readLine().split(" ")[5]).intValue());
						br.readLine();
						belongcmty.add(br.readLine().split(" ")[5]);
						br.readLine();
				}	
				//Read edge			
				br.readLine();
				Integer[] aa = new Integer[2];			
				aa[0] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
				aa[1] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
				br.readLine();
				edge_data.add(aa);
				while(br.readLine()!=null) {
					if(br.readLine()==null) {
						break;
					}
					Integer[] aa1 = new Integer[2];
					aa1[0] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
					aa1[1] = Integer.valueOf(br.readLine().split(" ")[5]).intValue();
					edge_data.add(aa1);
					br.readLine();				
				}
			}			
			//Read cmty
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set cmtySet = new HashSet(belongcmty);		
			for (int i = 0; i < cmtySet.size(); i++) {
				ArrayList<Integer> aaArrayList = new ArrayList<Integer>();
				cmty_data.add(aaArrayList);
			}
			for (int i = 0; i < belongcmty.size(); i++) {
				int k=0;
				@SuppressWarnings("unchecked")
				Iterator<String> iterator = cmtySet.iterator();
				while (iterator.hasNext()) {
					String string = (String) iterator.next();
					if (belongcmty.get(i).equals(string)) {
						newbelong.add(k);
						cmty_data.get(k).add(node_data.get(i));
						break;
					}
					k++;
				}				
			}		
			//read detail_list
			for (int i = 0; i < cmty_data.size(); i++) {
				ArrayList<Integer[]> inedge = new ArrayList<Integer[]>();
				ArrayList<Integer[]> betweenedge = new ArrayList<Integer[]>();
				ArrayList<ArrayList<Integer[]>> eachcmty = new ArrayList<ArrayList<Integer[]>>();
				eachcmty.add(inedge);
				eachcmty.add(betweenedge);
				detaiList.add(eachcmty);			
			}			
			
			for(int i=0;i<edge_data.size();i++) {
				Integer cmtyA = newbelong.get(node_data.indexOf(edge_data.get(i)[0]));
				Integer cmtyB = newbelong.get(node_data.indexOf(edge_data.get(i)[1]));		
				if(cmtyA == cmtyB) {
					for(int j=0;j<detaiList.size();j++) {
						if(j==cmtyA) {
							detaiList.get(j).get(0).add(edge_data.get(i));
							break;
						}
					}
				}
				else {
					for(int j=0;j<detaiList.size();j++) {
						if(cmtyA==j) {
							detaiList.get(j).get(1).add(edge_data.get(i));
						}
						if(cmtyB==j) {
							detaiList.get(j).get(1).add(edge_data.get(i));
						}
					}
				}	
			}			
			resultArrayList.add(node_data);
			resultArrayList.add(edge_data);
			resultArrayList.add(cmty_data);
			resultArrayList.add(newbelong);
			resultArrayList.add(detaiList);
			return resultArrayList;
		}
	}
}
