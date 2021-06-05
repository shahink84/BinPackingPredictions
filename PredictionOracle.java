
import java.util.*;

class PredictionOracle {

	//return (perfect) predictions
	double[] getCorrectPredictions (int[] a, int k) {
		
		double[] freq = new double[k+1];
		
		for (int i = 0; i < a.length; i++) {
			freq[a[i]]++;
		}
		
		for (int c = 0; c < k + 1; c++) {
			freq[c]/=a.length;
		}
		
		return freq;
	}


	//read the first w numbers and set frequencies accordingly
	double[] getWindowPredictions (int[] a, int k, int w) {
		
		double[] freq = new double[k+1];
		
		for (int i = 0; i < w; i ++) {
			freq[a[i]]++;
		}
		
		for (int c = 0; c < k + 1; c++) {
			freq[c] /= w;
		}
		return freq;
	}

	//returns the L1 distance between the actual frequencies and the predicted ones
	double getL1Distance (double[] act_frq, double[] prdc_frq) {
		
		double l1_dist = 0;
		
		for (int i = 1; i < act_frq.length; i++) {	
			double d = act_frq[i] - prdc_frq[i];
			
			if (d > 0.5) {
				System.out.println("d: " + d + " i:" + i + " act[i]" + act_frq[i] + " pred[i]" + prdc_frq[i]);
			}
			if (d > 0) {
				l1_dist += d;
			}
			else {
				l1_dist -= d;
			}
		}
		return l1_dist;
	}	
}