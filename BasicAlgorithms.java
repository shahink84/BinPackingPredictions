import java.util.*;

// this class involves implementation of basic algorithms that serve as reference in our experiments
public class BasicAlgorithms {

	//k: bin capacity, sigma: input sequence
	public static int l2LowerBound (int[] sigma, int k) {
		
		int n = sigma.length;
		
		int[] a = Arrays.copyOf(sigma, n);

		//sorting the input, decreasingly based on their size
		Arrays.sort(a);
		int[] b = new int[n];
		for (int i = 0; i < n; i++) {
			b[i] = a[n-1-i];
		}
		
		//the wasted space
		int waste = 0;
		
		int begin = 0;
		int end = n - 1;
		
		int sum = 0;
		
		while (begin < end) {
			//getting the largest
			int s1 = b[begin];
			
			begin++;
			
			//calculating r
			int r = k - s1;
			
			//finding the items with size <= r
			while (b[end] <= r && begin < end) {
				sum += b[end];
				end --;
			}
			
			if (sum <= r) {
				waste += (r-sum);
			}
			else {
				sum = (sum-r); 
			}
		}

		//getting the sum of all the items
		int all_sum = waste;
		for (int i = 0; i < a.length; i++) {
			all_sum += a[i];
		}
		
		//diving the (sum + wasted) by bin capacity
		int l2 = (int)(Math.ceil(all_sum/k));
		return l2;
	}

	
	//first fit with packing (naive implementation suffices since it is used just for packing  profiles)
	static int[][] firstFitPack (int[] a, int n, int k) {
		
		int[][] bins = new int[n][k];
		
		//bin_cardin[i] indicates the number of items in the bin index i
		int[] bin_cardin = new int[n];
		for (int i = 0; i < n; i++) {
			bin_cardin[i] = 0;
		}

		int[] bin_levels = new int[n];
		int number_of_bins = 0;

		for (int i = 0; i < n; i++) {
			boolean packed = false;
			for (int b = 0; b < number_of_bins && !packed; b++) {
				//there is enough space
				if (bin_levels[b] + a[i] <= k) {
					//pack a[i] in bin b
					bin_levels[b] += a[i];
					bins[b][bin_cardin[b]] = a[i];
					bin_cardin[b]++;
					packed = true;
				}
			}
			//open a new bin
			if (packed == false) {
				//pack into a new bin
				bin_levels[number_of_bins] = a[i]; 
				bins[number_of_bins][bin_cardin[number_of_bins]] = a[i];
				bin_cardin[number_of_bins]++;
				number_of_bins ++;
			}
		}

		int[][] bins2 = new int[number_of_bins][k];
		for (int i = 0; i < number_of_bins; i++) {
			bins2[i] = bins[i];
		}
		return bins2;
	}

	
	//best fit with packing (naive implementation suffices since it is used just for packing small profiles)
	static int[][] bestFitPack (int[] a, int n, int k) {
		int[][] bins = new int[n][k];
		//bin_cardin[i] indicates the number of items in the bin index i
		int[] bin_cardin = new int[n];
		int[] binLevels = new int[n];
		
		for (int i = 0; i < n; i++) {
			bin_cardin[i] = binLevels[i] = 0;
		}
		
		int number_of_bins = 0;

		for (int i = 0; i < n; i++) {
			boolean packed = false;
			int res_bin = 0;
			int max_level = -1;
			for (int b = 0; b < number_of_bins; b++) {
				if (binLevels[b] + a[i] <= k && binLevels[b] > max_level) {// there is enough space
					res_bin = b;
					max_level = binLevels[b];
					packed = true;
				}
			}
			//open a new bin
			if (packed == false) {
				res_bin = number_of_bins;
				number_of_bins ++;
			}
			//pack into a new bin
			//so far if packed is still false then we have gotten a new res_bin index
			binLevels[res_bin] += a[i];
			bins[res_bin][bin_cardin[res_bin]] = a[i];
			bin_cardin[res_bin]++;
		}
		return bins;
	}

	
	//efficient implementation of first-fit
	static int firstFit2 (int[] a, int n, int k) {
		
		int[] binLevel = new int[n];
		for (int b = 0; b < n; b++) {
			//all bins are empty
			binLevel[b] = 0;
		}

		//initially 0 bins are opened
		int number_of_bins = 0;
		
		//first_indx[c] is the index of the first bin in the packing that has level exactly c
		int[] first_indx = new int[k+1];
		for (int c = 0; c <= k; c++) {
			first_indx[c] = n; //basically, infty (there are no more than n bins)
		}

		for (int i = 0; i < n; i++)	{
			//the candidate bin to pack the item; initially a new bin
			int cnd_bin = number_of_bins;
			//the necessary empty space
			for (int c = 1; c <= k - a[i]; c++) {
				if (first_indx[c] < cnd_bin)
					cnd_bin = first_indx[c];
			}
			if (cnd_bin == number_of_bins){
				number_of_bins++;
			}

			//the item should be packed into bin at index cnd_bin
			int prev_level = binLevel[cnd_bin];
			int cur_level = prev_level + a[i];

			//a[i] is now packed
			binLevel[cnd_bin] += a[i];

			//packing the item into bin causes an earlier bin having cur_empty_space
			if (cur_level < k && cnd_bin < first_indx[cur_level]){
				first_indx[cur_level] = cnd_bin; 
			}
			if (prev_level > 0){	
				first_indx[prev_level] = n;//infty
				
				//find a new bin that has empty space equal to pre_empty_space
				for (int j = cnd_bin + 1; j < number_of_bins; j++){
					if (binLevel[j] == prev_level) {
						first_indx[prev_level] = j;
						break;
					}
				}
			}
		}
		return number_of_bins;
	}


	//efficient implementation of best-fit
	static int bestFit2 (int[] a, int n, int k) {
		
		//bin levels
		int[] binLevel = new int[n];
		for (int b = 0; b < n; b++) {
			//all bins are empty
			binLevel[b] = 0;
		}

		//initially 0 bins are opened
		int number_of_bins = 0;
		
		//first_indx[c] is the index of the first bin in the packing that has level exactly c.
		int[] first_indx = new int[k+1];
		for (int c = 0; c <= k; c++) {
			//basically, infty (there are no more than n bins)
			first_indx[c] = n;
		}

		for (int i = 0; i < n; i++) {
			//the candidate bin to pack the item; initially a new bin
			int cnd_bin = number_of_bins;
			//the necessary empty space
			for (int c = k - a[i]; c >= 1; c--) {
				if (first_indx[c] < cnd_bin) {
					cnd_bin = first_indx[c];
					break;
				}
			}
			
			if (cnd_bin == number_of_bins) {
				number_of_bins++;
			}

			//the item should be packed into bin at index cnd_bin
			int prev_level = binLevel[cnd_bin];
			int cur_level = prev_level + a[i];

			binLevel[cnd_bin] += a[i]; // a[i] is now packed

			//packing the item into bin causes an earlier bin having cur_empty_space
			if (cur_level < k && cnd_bin < first_indx[cur_level]) {
				first_indx[cur_level] = cnd_bin; 
			}

			if (prev_level > 0) {
				first_indx[prev_level] = n;	//infty	
				//find a new bin that has empty space equal to pre_empty_space
				for (int j = cnd_bin + 1; j < number_of_bins; j++) {
					if (binLevel[j] == prev_level) {
						first_indx[prev_level] = j;
						break;
					}
				}
			}
		}
		return number_of_bins;
	}


	//FFD algorithm
	static int firstFitDecreasing (int[] sigma, int n, int k) {
		
		//a copy of input
		int []a = Arrays.copyOf(sigma, n);
		//sorting it decreasingly based on item sizes
		Arrays.sort(a);
		int[] b = new int[n];
		for (int i = 0; i < n; i++){
			b[i] = a[n-1-i];
		}
		
		int cost = firstFit2(b,n,k);
		return cost;
	}

	//BFD algorithm
	static int bestFitDecreasing (int[] sigma, int n, int k) {
		
		//a copy of input
		int []a = Arrays.copyOf(sigma, n);
		//sorting it decreasingly based on item sizes
		Arrays.sort(a);
		int[] b = new int[n];
		for (int i=0; i<n; i++) {
			b[i] = a[n-1-i];
		}
		
		int cost = bestFit2(b, n, k);
		return cost;
	}

	//L1 algorithm
	public static int l1LowerBound (int[] a, int k) {

		int sum = 0;
		for (int i=0; i < a.length; i++) {
			sum += a[i];
		}
		int L1 = (int)(Math.ceil(sum/k));
		return L1;
	}
}