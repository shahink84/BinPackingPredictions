
import java.util.*;
import java.util.ArrayList;

//a single bin with spots to be packed
class Bin {
	
	short[] n_spots;
	
	//boolean isEmpty;
	Bin (int[] n_spots0, int k) {
		
		int n = n_spots0.length;
		n_spots = new short[k+1];

		for (short x = 0; x < n; x ++) {
			short temp = (short) n_spots0[x];
			n_spots[x] = (short) (temp);
		}
	}


	Bin(Bin b, int k) {
		
		int n = b.n_spots.length;
		n_spots = new short[k+1];

		for (short x = 0; x < n; x ++) {
			short temp = b.n_spots[x];
			n_spots[x] = temp;
		}	
	}

	boolean pack (int x) {
		int count = n_spots[x];
		if (count > 0) {
			n_spots[x] --;
			return true;
		}
		return false;
	}

	boolean hasAvailableSpot (int x) {
		int res = n_spots[x];
		return (res > 0);
	}

	void print() {
		for (int x = 1; x < n_spots.length; x++) {
			if (n_spots[x] >= 1) {
				System.out.print(x + "\t");
			}
		}
		System.out.println();
	}
}


//the state of the packing when profiles are used
public class Profile_Packing { 

	//here the integer key is in the index of the bin among active bins
    HashMap<Integer, Bin> active_bins;
    //the integer key is the type (hash) of the empty bin
	HashMap<Integer, Integer> count_empty_bins;

	//used to retrieve a bin from its type
	HashMap<Integer, Bin> type_bin_kvp;
	//my_bins[x] indicates the types of the bins in the profile that include a spot for x
	ArrayList<ArrayList<Integer>> my_bins;
	
	//contains_spot[x] indicates whether there is any spot for x in the profile
	boolean[] contains_spot;

	int number_of_active_bins;
	int k;
	//first_active_bin[x] is the index of the first active bin that has an available spot of size x
	int[] first_active_bin;

	Profile_Packing(int k0) {
		k = k0;
		active_bins = new HashMap<>(); 
		count_empty_bins = new HashMap<>();
		type_bin_kvp = new HashMap<>();
	
		my_bins = new ArrayList<>(k+1);
		for (int i = 0; i <= k; i++) {
			my_bins.add (new ArrayList<>());
		}
	
		number_of_active_bins = 0;
		first_active_bin = new int[k+1];
	}

	int getHashCode (int [] b) {
		int code = 1;
		for (int i=0; i < b.length; i++) {
			code = 1381 * code + b[i];
		}
		return code * b[0];
	}

	//pack x into the next bin that already has an item in it
	boolean packFilled(int x) {
		
		//there is an active bin with enough space for x
		if (first_active_bin[x] != number_of_active_bins) {
		
			Bin b = active_bins.get(first_active_bin[x]);
			if (b == null) {
				return false;
			}
			boolean packed = b.pack(x);
			if (!packed) {
				System.out.println("sth wrong12 x:" + x);
			}
			int j = first_active_bin[x];
			while (j < number_of_active_bins && active_bins.get(j).n_spots[x] <= 0) {
				j++;
			}
			first_active_bin[x] = j;
			return true;
		}
		else {
			return false;
		}
	}


	boolean packEmpty(int x) {

		//go through all bins in the profile that have placeholder for x; it works better if you start from the last FFD bin
		for (int i = my_bins.get(x).size() - 1; i >= 0; i--){
			//the type of the next empty bin which has space for x
		 	int type = my_bins.get(x).get(i);
            if (count_empty_bins.get(type) == null) {
            	//x is present in a bin of this type; but there is no such free bin currently available.
			 	continue;
            }

            int count = count_empty_bins.get(type);

            if (count > 0) {
            	
		 		//we have found an empty bin (with count > 0) which has a placeholder for x.
		 		Bin b = type_bin_kvp.get(type);
		 		
			 	if (b.hasAvailableSpot(x) == false) {
			 		System.out.println("hash collision " + x + " " + count + "   " + b.n_spots[x] + "  " + type ) ;
			 		//collision, try another empty bin type
			 		continue;
			 	}
			 	
			 	Bin new_active_bin = new Bin(b, k);
			 	new_active_bin.pack(x);
				active_bins.put(number_of_active_bins,new_active_bin);//=b;
				
				for (int j = 1; j <= k; j++) {
					if (first_active_bin[j] == number_of_active_bins && new_active_bin.n_spots[j]<=0 ) {				
						first_active_bin[j] = number_of_active_bins +1;
					}
				}
				if (count > 1) {
					//number of bins of this type is now decremented.
					count_empty_bins.put(type, count-1);
					if (count_empty_bins.get(type) == 0) {
						System.out.println("error not POSSIBLE");
					}
				}
				else {
					count_empty_bins.remove(type);
				}

				number_of_active_bins ++;
			 	return true;
		 	}
		 	else {
		 		System.out.println("sth wrong EE2 " + x +  "  " + count);
		 	}
		}
		return false;
	}	


	//q is the number of bins in the packed profile
	void addProfile(int[][] b) {
		//adding a profile to the packing; this does NOT create new bins, only increasing the count of empty bins
		int q = b.length;

		for (int i = 0; i < q; i++) {
			//*b[i][0] the last product is to reduce the chance of collision
			int type = getHashCode(b[i]); //Arrays.hashCode(b[i]);

			if (count_empty_bins.get(type)==null) {
				//the first bin of the type
				count_empty_bins.put(type,1);
			}
			else {
				int oldCount = count_empty_bins.get(type);
				count_empty_bins.put(type, oldCount+1);				
			}
		}

		int[] card = new int [k+1];
		
		for (int i = 0; i < b.length; i++) {
			for (int x = 1; x <= k; x++) {
				card[x] = 0;
			}
			for (int j = 0; j < b[i].length; j++) {
				int x = b[i][j];
				card[x] ++;
			}

			//the last product is to reduce the chance of collision
			int type = getHashCode(b[i]);
			Bin binak = new Bin(card, k);
			type_bin_kvp.put(type, binak);

			for (int j = 0; j < b[i].length; j++) {	
				int x = b[i][j];
				if (x == 0) {
					continue;
				}
				boolean bin_present_in_list = false;

				int mm = my_bins.get(x).size();
				for (int p = 0; p < mm; p++) {
					if (my_bins.get(x).get(p) == type){
						bin_present_in_list = true;
						break;
					}				
				}
				if (bin_present_in_list == false) {
					my_bins.get(x).add(mm,type);
				}
			}
		}	
	}

	//returning the cost of Profile Packing
	int getCost() {
		int cost = number_of_active_bins;
		return cost;
	}

	//determining whether there is a place holder for item x in the bins
	boolean containsSpot(int x) {
		return (my_bins.get(x).size() > 0); 
	}
}