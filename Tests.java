import java.io.*;

public class Tests {
	
	HybridAlgorithm hybrid_alg;
	PredictionOracle prediction_oracle;

	//alg_cost_sum[] is used for adaptive test (taking average costs)
	int ff_cost_sum, bf_cost_sum, l2_sum, alg_cost_sum[];

	int[][] fixed_cost_sum; // for taking avg for fixed input
	int[][] fixed_cost_point_no; // number of points for fixed input that we take average on
	
	Tests() {
		hybrid_alg = new HybridAlgorithm();
		prediction_oracle = new PredictionOracle();
		ff_cost_sum = bf_cost_sum = l2_sum = 0;
		alg_cost_sum = new int[101000];

		fixed_cost_sum = new int[10][10000]; 
		fixed_cost_point_no = new int[10][10000];
		/* there are at most 10 (typically four) algorithms (values of lambda) and the number of points (window lengths) 
		is no more than 10000 (typicall 100)//*/
	}

	/*
	 * return L2, ff_cost, and bf_cost while reporting additional algorithms costs
	 * (the values are returned only to facilitate drawing charts)  
	 */
	public int[] reportBasicAlgCosts(int[] sigma, int k, BufferedWriter writer) {

		int ff_cost = BasicAlgorithms.firstFit2(sigma, sigma.length, k);		
		int bf_cost = BasicAlgorithms.bestFit2(sigma, sigma.length, k);
		int l2 = BasicAlgorithms.l2LowerBound(sigma, k);
		int ffd_cost = BasicAlgorithms.firstFitDecreasing(sigma,sigma.length,k);

		System.out.println("ff cost:" + ff_cost);
		System.out.println("bf cost:" + bf_cost);
		System.out.println("ffd cost:" + ffd_cost);
		System.out.println("L2 lower bound:" + l2);//
        int[] res = new int[3];
        res[0] = l2; res[1] = ff_cost; res[2] = bf_cost;
        return res;
	}
	
	/* Run adaptive with different values for w (window length), and add its cost to alg_cost_sum[w] (to take the average). 
	called from testEvolving */
	public void testWindowErrorAdaptive (int[] sigma, int k, String out_file_name, int m) {
	
		try {
			int[] classic_alg_costs = reportBasicAlgCosts(sigma, k, null);
			
			int a_plus_b = 12;
			int a = a_plus_b;

			System.out.println("_____________\n lambda is: " + (double) a / a_plus_b + "\n (w, cost) are as follows:" );
			l2_sum += classic_alg_costs[0];
			ff_cost_sum += classic_alg_costs[1];
			bf_cost_sum += classic_alg_costs[2] ;

			for (int w = 100; w <= 101000; w += 1000) {//w +=10,000
				
				int alg_cost_adaptive = hybrid_alg.getAlgCostAdaptive(sigma, w, m, k, a, a_plus_b-a);

				//ask for garbage collection
				System.gc();
				
				System.out.println(w + ",\t" + alg_cost_adaptive);
				alg_cost_sum[w] += alg_cost_adaptive;
				if (alg_cost_sum[w] < 0) {
					System.out.println("Alg cost overflow");
				}
			}
		}
	    catch(Exception e) {
	        e.printStackTrace();
	    }
	}


	//testing with window error, where predictions are generated based on frequencies in a prefix of a given length
	public  void testWindowErrorFixed (int[] sigma, int k, int m, BufferedWriter writer, boolean avgExp){ 
				
        try {

			//the cost of classic algorithms like ff, bf, l2, and so on
			int[] classic_alg_costs = reportBasicAlgCosts(sigma, k, writer);
			l2_sum += classic_alg_costs[0];
			ff_cost_sum += classic_alg_costs[1];
			bf_cost_sum += classic_alg_costs[2] ;
			
			//actual (correct) predictions
			double[] act_freq = prediction_oracle.getCorrectPredictions(sigma,k);

			//for lambda for the hybrid algorithm
			int a_plus_b = 12;
			if (avgExp == false)
			{
				writer.write("___________________________\n\n\n");
				writer.write("PrefixWindow\tEta\tL2 Lower Bound(Opt)\tFirst Fit\tBest Fit\tHybrid (Lambda = 0.25)\tHybrid (Lambda = 0.5)\tHybrid (Lambda = 0.75)\tProfile Packing (Lambda = 1)\n");
			}
			//the size of the window for learning in adaptive mode
			int n_w = 44530; //357434; //137806; //  10 * 1.1^100
			
			//window lengths for learning in the adaptive mode
			int[] window = new int[n_w * 2];
		
			double expo = 1.05;

			//going through different window lengths
			for (double ww = 338; ww < n_w; ww *= expo){ 	
				
				//getting the predictions based on the window
				double[] predicted_freq = prediction_oracle.getWindowPredictions(sigma, k, (int) ww); //+t*10);
				//the error defined as the L1 Distance between the correct and learned predictions
				double eta = prediction_oracle.getL1Distance(act_freq, predicted_freq);
				
				//we want errors < 2 to be in our learning window
				if (eta < 2) {
					window[(int)(eta*n_w)] = (int) ww;
				}
			}

			//the results of the algorithms, we have 5 algorithms, ff, bf, l2, hybrid, pp
			int[][] alg_res = new int[5][n_w];

			//the error results
			double[] eta_res = new double[n_w];
			//algorithm number
			int no_algs = 0;
			//experiment number
			int no_exps = 0;
			
			for (int a = 3; a <= a_plus_b; a += 3) {
				//for recording results and printing them later
				no_exps = 0;
				System.out.println("\nworking on Hybrid with lambda:  " + (double)a/a_plus_b + "\ntriplet (prefix-length, error, alg-cost) are as follows:"  );
				for (int q = 0; q < window.length; q++) {
					if (window[q] == 0) {
						continue;
					}

					/*
					 * getting the prediction from window learning
					 * getting the error
					 * getting the cost
					 */
					double[] predicted_freq = prediction_oracle.getWindowPredictions(sigma, k, window[q]); //+t*10);
					double eta = prediction_oracle.getL1Distance(act_freq, predicted_freq);
					int alg_cost = hybrid_alg.getAlgCostFixed (sigma, k, predicted_freq, m, a, a_plus_b-a);

					//ask for garbage collection
					System.gc();
					
					//sum_eta/n_test;
					eta_res[no_exps] = eta;
					//sum_alg/n_test;
					alg_res[no_algs][no_exps] = alg_cost;


					fixed_cost_sum[no_algs][(int)(eta*100)] += alg_cost;
					fixed_cost_point_no[no_algs][(int)(eta*100)] ++;
					
					System.out.println(window[q] + ",\t" + eta + ",\t" + alg_cost);
					no_exps++;
				}
				no_algs++;
			}
			if (avgExp == false)
			{
				int j = 0;
				for (int q = 0; q < window.length; q++) {
					if (window[q] == 0) {
						continue;
					}

					writer.write(window[q] + "\t" + eta_res[j] + "\t"+classic_alg_costs[0] + "\t" + classic_alg_costs[1] + "\t" + classic_alg_costs[2] + "\t");
					
					for (int ii = 0; ii < no_algs; ii++) {
						writer.write(alg_res[ii][j] + "\t");
					}
					
					writer.write("\n");
					j++;
				}
			}
		}
	    catch(Exception e) {
            e.printStackTrace();
        }
	}


	//fix all parameters and change the profile size (m) and report the cost of Hybrid. 
	public  void testProfileSizeFix (int[] sigma, int k, BufferedWriter writer){ 
	try {
			//the cost of classic algorithms like ff, bf, l2, and so on
			int[] classic_alg_costs = reportBasicAlgCosts(sigma, k, writer);
			
			//actual (correct) predictions
			double[] act_freq = prediction_oracle.getCorrectPredictions(sigma,k);
			
			// find predicted frequencies based on a prefix of length 1000
			int window_len = 1000;
			double[] predicted_freq = prediction_oracle.getWindowPredictions(sigma, k, window_len); 
			double eta = prediction_oracle.getL1Distance(act_freq, predicted_freq);

			//for lambda for the hybrid algorithm
			writer.write("___________________________\n\n\n");
			writer.write("error is: " + eta + "\n\n");
			System.out.println("error is: " + eta);
			
			writer.write("m\tL2 Lower Bound(Opt)\tFirst Fit\tBest Fit\tHybrid (Lambda = 0.25)\tHybrid (Lambda = 0.5)\tHybrid (Lambda = 0.75)\tProfile Packing (Lambda = 1)\n");
			
			
			int[][] alg_res = new int[5][20000];

			//algorithm number
			int no_algs = 0;
			//experiment number
			int no_exps = 0;
			int min_m = 100;
			int max_m = 100200;
			int a_plus_b = 12;
			for (int a = 3; a <= a_plus_b; a += 3) {
				//for recording results and printing them later
				no_exps = 0;
				System.out.println("_____________\n working on hybrid with lambda: " + (double)a/a_plus_b + "\n (m, cost) are as follows:" );
				for (int m = min_m; m < max_m; m+= 1000) {

					int alg_cost = hybrid_alg.getAlgCostFixed (sigma, k, predicted_freq, m, a, a_plus_b-a);

					//ask for garbage collection
					System.gc();
					
					//sum_alg/n_test;
					alg_res[no_algs][no_exps] = alg_cost;
					System.out.println(m + ",\t" + alg_cost);
					no_exps++;
				}
				no_algs++;
			}
			int j = 0;
			
			no_exps = 0;
			for (int m = min_m; m < max_m; m+= 1000) {

				writer.write(m + "\t" + classic_alg_costs[0] + "\t" + classic_alg_costs[1] + "\t" + classic_alg_costs[2] + "\t");
				
				for (int ii = 0; ii < no_algs; ii++) {
					writer.write(alg_res[ii][no_exps] + "\t");
				}
				no_exps ++;
				writer.write("\n");
			}
			writer.close();
		}
	    catch(Exception e) {
            e.printStackTrace();
        }
	}

}