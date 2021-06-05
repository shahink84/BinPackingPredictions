
public class HybridAlgorithm {
	
	BasicAlgorithms basic_alg;
	HybridAlgorithm() {
		basic_alg = new BasicAlgorithms();
	}

	
	//building a Profile for Profile Packing
	int[][] buildProfile (int[] counts, int window_length, int k, int m) {

		double[] freq = new double [k+1];

		for (int i = 0; i <= k; i++) {
			freq[i] = (double) counts[i] / window_length;
		}
		return buildProfile(freq, k ,m);
	}

	
	//building a Profile for Profile Packing
	int[][] buildProfile (double[] predicted_frequency, int k, int m) {
		
		//profile_freq[x] is the number of items of size x in the profile
		int[] profile_freq = new int[k+1];
		
		for (int x = 1; x <= k; x ++) {
			profile_freq[x] = (int)(Math.ceil(predicted_frequency[x]*m));
		}
		
		//a sorted sequence formed by profile
		int[] a = new int[2 * (m + k)];
		int n = 0;
		
		for (int i = k; i >= 1; i --) {
			for (int j = 0; j < profile_freq[i]; j++) {
				a[n] = i;
				n++;
			}
		}
		
		int[][] bins = BasicAlgorithms.bestFitPack(a,n,k);
		
		return bins;
	}

	//getting the cost for the fixed distributions, the method called by the Test class 
	int getAlgCostFixed(int[] sigma, int k, double[] predicted_frequency, int m, int a, int b) {
		
		int[][] bb = buildProfile(predicted_frequency,k ,m);
		double lambda = (double)(a)/(a+b);
		return getAlgCostFixed(sigma, k, bb, lambda);
	}

	//getting the cost for the fixed distributions
	int getAlgCostFixed(int[] sigma, int k, int[][] profile_bins, double lambda) {

		int n = sigma.length;
		
		int [] index = new int[k+1];
		for (int x = 1; x <= k; x++) {
			index[x] = 0;
		}

		Profile_Packing packing = new Profile_Packing(k);
		packing.addProfile(profile_bins);
	
		//the items to be packed by FF
		int [] sigma_ff = new int[n];

		//PP items received so far
		int[] count_pp = new int[k+1];
		//FF items received so far
		int[] count_ff = new int[k+1];

		/*
		 * number of items that need to be packed using FF
		 * both the FF items and the special items 
		 */
		int n_ff = 0;
		
		for (int i = 0; i < n; i++) {
			
			boolean packed = false;
			int x = sigma[i];
			
			//if x is special (not present in the profile)
			if (packing.containsSpot(x) == false) {
				
				/*
				 * we'd pack the item using FF
				 * it is decided that x should be added to FF sequence and packed later using FF. 
				 */
				sigma_ff[n_ff] = x;
				n_ff++;
				count_ff[x] ++;
				packed = true;
				continue;
			}
			
			//the item is not special; first try to place it in a placeholder
			packed = packing.packFilled(x);

			if (packed) {
				count_pp[x] ++;
				continue;	
			}

			/*
			 * if there was no placeholder
			 * determine whether it is a PP or FF item using lambda 
			 */
			if (count_pp [x] < lambda * (count_pp[x] + count_ff[x])) {
				
				//if it is a PP item, try to put it in an empty place holder
				packed = packing.packEmpty(x); 
				
				//if not, open a new profile
				if (!packed) {
					
					packing.addProfile(profile_bins);
					packed = packing.packEmpty(x);
				}
				count_pp[x] ++;
			}
			//x is an FF item
			else {
				sigma_ff[n_ff] = x;
				n_ff++;
				count_ff[x]++;
			}
		}

		int cost_PP = packing.getCost();
		int cost_FF = BasicAlgorithms.firstFit2(sigma_ff, n_ff, k);
		return cost_PP + cost_FF;
	}

	//getting the cost for adaptive distribution
	int getAlgCostAdaptive(int[] sigma, int window_length, int m, int k, int a, int b) {

		int n = sigma.length;
		
		int [] index = new int[k+1];
		for (int x = 1; x <= k; x++) {
			index[x] = 0;
		}

		//the lambda for deciding whether an item is PP or FF
		double lambda = (double)(a)/(a+b);

		Profile_Packing packing = new Profile_Packing(k);

		//the items to be packed using FF, both the FF items and the special ones
		int n_ff = 0;
		int [] sigma_ff = new int[n];

		//the count of each item size
		int[] count = new int[k+1];
		//the window it is going to learn from
		int[] learning_str = new int[window_length];
		//to iterate through the learning_str
		int learning_indx = 0;
		
		//PP items received so far
		int[] count_pp = new int[k+1];
		//FF items received so far
		int[] count_ff = new int[k+1];

		//going over the items
		for (int i=0; i < n; i++) {

			//getting the item
			int x = sigma[i];
			boolean packed = false;

			//updating count for the item we have received
			count[x] ++;
			
			//if we have passed the window learning length
			if (i >= window_length) {
				//if it is passed our learning window we need to update count so that it reflects the last w items
				count[learning_str[learning_indx]] --;
			}
			
			//for updating count with it
			learning_str[learning_indx] = x;
			learning_indx = (learning_indx+1) % window_length;
			
			//we are just learning at the beginning
			if (i < window_length) {
				sigma_ff[n_ff] = x;
				n_ff++;
				//it is decided that x should be added to FF seq and packed later using FF.
				packed = true;
				continue;
			}
			//adding the first profile
			if (i == window_length) {
				int[][] first_profile_bins = buildProfile(count,window_length, k ,m);
				packing.addProfile(first_profile_bins);
			}

			//x is a special item (no available spot for x in the packing)
			if (packing.containsSpot(x) == false) {
				sigma_ff[n_ff] = x;
				n_ff++;
				//it is decided that x should be added to FF seq and packed later using FF.
				packed = true;
				continue;
			}
			packed = packing.packFilled(x);
			if (packed) {
				count_pp[x] ++;
				continue;	
			}
			else
			//there was no placeholder
			if (count_pp [x] <= lambda * (count_pp[x] + count_ff[x])) {
				packed = packing.packEmpty(x); 
				if (!packed) {
					int[][] new_profile_bins = buildProfile(count, window_length, k, m);
					packing.addProfile(new_profile_bins);
					packed = packing.packEmpty(x);
				}
				count_pp[x] ++;
			}
			else {
				sigma_ff[n_ff] = x;
				n_ff++;
				count_ff[x] ++;
			}
		}

		int cost_PP = packing.getCost();
		int cost_FF = BasicAlgorithms.firstFit2(sigma_ff, n_ff, k);
		return cost_PP + cost_FF;
	}
}