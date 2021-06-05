import java.util.*;
import java.lang.Object;
import java.util.ArrayList;
import java.io.*;


public class MainClass {


	//reading the input sequence from the file
	public static int[] readInput(String file_name) {
	
		ArrayList<Integer> integers = new ArrayList<Integer>();
		
		//the number of integers in the file
        int n = 0;

        try {
        	FileReader file = new FileReader(file_name);
	      	Scanner input = new Scanner(file);

        	while(input.hasNext()){
    			integers.add(input.nextInt());
    			n++;
        	}
            input.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

		int[] a = new int[n];
		int indx = 0;

		for (int i = 0; i < n; i++) {
			int next_num = integers.get(i);
			if (next_num > 0) {
				a[indx] = next_num; 
				indx ++;
			}
		}

		//b: the item sizes without any empty spaces in the array
		int[] b = new int[indx];
		for (int i = 0; i < indx; i++)
			b[i] = a[i];

		return b;
	}


	//generate window_len integers uniformly, at random from the input file and store them in an array list
	public static ArrayList<Integer> getInputBPP (String file_name, int k, int window_len) {
		
		int num = 0;
		 
		ArrayList<Integer> integers = new ArrayList<Integer>();
		
        try {
        	FileReader file = new FileReader(file_name);
	      	Scanner input = new Scanner(file);
	      	
	      	//getting capacity and n on the file
	      	//in all files, the first one is the number of files, the second one is the capacity
			int n_on_file = input.nextInt();
	      	int c = input.nextInt();
	      	
        	while(input.hasNext()) {
        		int x = input.nextInt();
        		x = (int) (Math.ceil((double) x * k / c ));
    			integers.add(x);
    			num++;
        	}
	        input.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        //getting the actual evolving inputs
        ArrayList<Integer> res = new ArrayList<Integer>();
        Random rd = new Random();
        
        //generating window_len items uniformly at random
        for (int i = 0; i < window_len; i++) {
        	int rnd_indx = Math.abs(rd.nextInt()) % num;
        	//getting a random number from integers array using the random index
        	res.add(integers.get(rnd_indx));
        }
		return res;
	}

	//given a benchmark name it returns an array of all the file names in that directory
	static ArrayList<String> getFileNames (String benchmark_name) {
		
		ArrayList<String> path_names_arrays = new ArrayList<>();
		String[] path_names; 
        //creates a new File instance by converting the given path_name string into an abstract path_name
        File f = new File(benchmark_name);

        //populates the array with names of files and directories
        path_names = f.list();

        //for each path_name in the path_names array
        for (String path_name : path_names) {
            path_names_arrays.add(benchmark_name + "/" + path_name);
        }
        return path_names_arrays;
	}

	/*create fileNo input sequences of length n with items in [1..k] input from files (distributions) inside a folder named benchmark_name */
	public static void generateInputEvolving(String benchmark_name, int n, int k, int window_len, int fileNo) {
		
		ArrayList<String> file_names = getFileNames("Data/Benchmarks/" + benchmark_name); //"Difficult_Instances/AI");//Irnich_BPP");
		
		String out_file_name = "Data/GeneratedSequences/Evolving/" + benchmark_name + "/" + benchmark_name +"InputSeqEvolving"; 
    	String log_file_name = "Data/GeneratedSequences/Evolving/" + benchmark_name + "/" + benchmark_name +"InputSeqEvolvingLog.txt"; 

    	try {
    		BufferedWriter writer_log = new BufferedWriter(new FileWriter(log_file_name));
    		String msg1= "generating evolving input from a randomly selected file from this list: ";
			writer_log.write(msg1);
			
	    	for (int i = 0; i < file_names.size(); i++) {
	    		writer_log.write(file_names.get(i) + "\n");
	    	}

			Random rd = new Random();
			int number_of_files = file_names.size();
			
			
			for (int q = 1; q <= fileNo; q++) {
				ArrayList<Integer> input = new ArrayList<Integer>();	        
				System.out.println("generating file indexed " + q + " out of " + fileNo);
				BufferedWriter writer = new BufferedWriter(new FileWriter(out_file_name + q + ".txt"));
				
				while (input.size() < n) {
					
					//get a random index, the %number_of_files is used to make sure the index stays in bounds	
					int rnd_indx_file = Math.abs(rd.nextInt()) % number_of_files;
					ArrayList<Integer> temp = getInputBPP (file_names.get(rnd_indx_file), k, window_len);
					input.addAll(temp);
				}

				//writing the input on file
				int[] sigma = new int[input.size()];
				
				for (int i = 0; i < input.size(); i++) {
					sigma[i] = input.get(i);	
					writer.write(sigma[i] + "\n");
				}
				writer.close();
			}

			writer_log.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
	}

	/* create fileNo input sequences of length n with items in [1..k] from a random files (distributions) inside the folder 
	Benchmarks/"BenchmarkName" and write the resulting instance on a file */
	public static void generateInputFixed(String benchmark_name, int n, int k, int fileNo) {

		ArrayList<String> file_names = getFileNames("Data/Benchmarks/" + benchmark_name);

		String out_file_folder = "Data/GeneratedSequences/Fixed/" + benchmark_name + "/";  // + "InputSeqFixed.txt"; 
    	String log_file_folder = "Data/GeneratedSequences/Fixed/" + benchmark_name + "/InputSeqFixedLog.txt"; 

		try {
			String msg1= "generating fixed input from a randomly selected file from this list: ";
			
	    	BufferedWriter writerLog = new BufferedWriter(new FileWriter(log_file_folder));
			for (int i = 0; i < file_names.size(); i++){
	    		writerLog.write(file_names.get(i));
	    	}

			int number_of_files = file_names.size();
			
			
			//getting a random index once bounded by the number of files since it is fixed
			Random rd = new Random();
			int rnd_indx_file = Math.abs(rd.nextInt()) % number_of_files;
			String rnd_file_name = file_names.get(rnd_indx_file);
			System.out.println("randomly selected file is: " + rnd_file_name);

			for (int fileIndx = 1; fileIndx <= fileNo; fileIndx++)
			{
				ArrayList<Integer> input = new ArrayList<Integer>();
				System.out.println("generating file indexed " + fileIndx + " out of " + fileNo);
				
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(out_file_folder + fileIndx + ".txt"));
				writerLog.write(rnd_file_name);
		    	
				ArrayList<Integer> temp = getInputBPP (file_names.get(rnd_indx_file), k, n); 
				input.addAll(temp);

				//writing the input in a file
				int[] sigma = new int[input.size()];
				
	        	for (int i = 0; i < input.size(); i++) {
					sigma[i] = input.get(i);	
					writer.write(sigma[i] + "\n");
				}
				writer.close();
			}
			writerLog.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
	}


	/*test for the evolving input. Given evolving sequences of length n and bin capacity k, apply adaptive (with a given 
	value of windw length) on 20 suche sequences (and take the average).*/
	public static void testEvolving(int n, int k, int window_len, String benchmark_name, int m) {

		Tests test = new Tests();
    	int n_test = 20;
    	for (int q = 1; q <= n_test; q++) {
    		System.out.println("\n\ntry " + q + " out of " + n_test );
    		int[] sigma = readInput("Data/GeneratedSequences/Evolving/"+benchmark_name+ "/" + benchmark_name +"InputSeqEvolving" + q+ ".txt");
	    	test.testWindowErrorAdaptive(sigma, k, " ", m);
		}

   		String out_file_name = "Data/Results/"+ benchmark_name+"OutputEvolving.txt";
   		
   		//getting the average cost for the tests in the Evolving input sequence for the algorithms L2, FF, BF
   		int l2_avg = test.l2_sum/n_test;
   		int ff_avg = test.ff_cost_sum/n_test;
   		int bf_avg = test.bf_cost_sum/n_test;
   				
   		try {
   			
   			//writing the output files
   			BufferedWriter writer = new BufferedWriter(new FileWriter(out_file_name));
			writer.write("\n\nw\tL2 Lower Bound(Opt)\tFirst Fit\tBest Fit\tEvolving Heuristic\n");

			for (int w = 100; w <= 101000; w += 1000) {//w += 10,000
				
   				int alg_avg = test.alg_cost_sum[w]/n_test;
				writer.write(w + "\t" + l2_avg + "\t" + ff_avg  + "\t" +  bf_avg + "\t" + alg_avg  +" \n");
			} 
			writer.close();			
   		}
        catch(Exception e) {
            e.printStackTrace();
        }
	}

	//test for the fixed input
	public static void testFixed(int n, int k, int m, String benchmark_name) {

		Tests test = new Tests();
    	int[] sigma = readInput("Data/GeneratedSequences/Fixed/" + benchmark_name + "/" +"1.txt"); // use the first randomly generated sequence
   		String out_file_name = "Data/Results/"+ benchmark_name+"OutputFixed.txt";
   		try{
	   		FileWriter fw = new FileWriter(out_file_name);
			BufferedWriter writer = new BufferedWriter(fw);
			test.testWindowErrorFixed (sigma, k, m, writer, false); //out_file_name);
			writer.close();

		}
		catch(Exception e)
        {}
	}


	public static void testProfileSize(int n, int k, String benchmark_name) {
		Tests test = new Tests();
    	int[] sigma = readInput("Data/GeneratedSequences/Fixed/" + benchmark_name + "/" +"1.txt"); // use the first randomly generated sequence
   		String out_file_name = "Data/Results/"+ benchmark_name+"OutputProfileSizeExperiment.txt";
   		try{
	   		FileWriter fw = new FileWriter(out_file_name);
			BufferedWriter writer = new BufferedWriter(fw);

			test.testProfileSizeFix (sigma, k, writer); 
		}
		catch(Exception e)
        {}
	}



	public static void test_static_avg(int n, int K, int M, String benchmarkName)
	{

		Tests test = new Tests();
   		
   		try{
	   		String out_file_name = "Data/Results/"+ benchmarkName+"OutputFixedAverage.txt";
   			FileWriter fw = new FileWriter(out_file_name);
			BufferedWriter writer = new BufferedWriter(fw);

	    	int n_test = 20;
	    	for (int q = 1; q <= n_test; q++)
	    	{
	    		System.out.println("___________________________________\ntest number is: " + q + " out of " + n_test);
	    		String fname = "Data/GeneratedSequences/Fixed/" + benchmarkName + "/" + q + ".txt";
	    		int[] sigma = readInput(fname);
	   			test.testWindowErrorFixed (sigma, K, M, writer, true); 
			}
			

	   		int l2_avg = test.l2_sum/n_test;
	   		int ff_avg = test.ff_cost_sum/n_test;
	   		int bf_avg = test.bf_cost_sum/n_test;
	   		int no_algs = 4;  		
			writer.write("eta\tL2 Lower Bound(Opt)\tFirst Fit\tBest Fit\tHybrid (Lambda = 0.25)\tHybrid (Lambda = 0.5)\tHybrid (Lambda = 0.75)\tProfile Packing (Lambda = 1)\n");

			int alg_avg = 0;
			for (int eta = 0; eta < 100; eta++)
   			{
				if (test.fixed_cost_point_no[0][eta] == 0)
   					continue;
				writer.write("\n" + eta + "\t" + l2_avg + "\t" + ff_avg  + "\t" +  bf_avg + "\t");
   				for (int ii=0; ii<no_algs; ii++)
	   			{	
	   				alg_avg = test.fixed_cost_sum[ii][eta] / test.fixed_cost_point_no[ii][eta];
   					writer.write(alg_avg + "\t");
   				}
			} 
			writer.close();			
   		}
        catch(Exception e)
        {
            e.printStackTrace();
        }

	}

	// output the running time (in seconds) of different algortihms on an input of length n generated from an input benchmark
	public static void testRunTime(int n, int k, int m, String benchmark_name) {
		int[] sigma = readInput("Data/GeneratedSequences/Fixed/" + benchmark_name + "/" +"1.txt"); // use the first randomly generated sequence

		PredictionOracle prediction_oracle = new PredictionOracle();
		double[] act_freq = prediction_oracle.getCorrectPredictions(sigma,k);
		double[] predicted_freq = prediction_oracle.getWindowPredictions(sigma, k, 1000);
		double eta = prediction_oracle.getL1Distance(act_freq, predicted_freq);

		HybridAlgorithm hybrid_alg = new HybridAlgorithm();

		int nTest = 20; //no. tests to take avg time

		for (int alg =0; alg < 3; alg++) //alg =0 -> firstfit   alg=1 -> bestfit   alg=2 -> adaptive
		{
			long sumTime = 0;
			for (int j=0; j < nTest; j++)
			{	
				long start, finish; 
				int algCost = -1;
				if (alg == 0)
				{
					start = System.currentTimeMillis();
					algCost = BasicAlgorithms.firstFit2(sigma, sigma.length, k);		
					finish = System.currentTimeMillis();
				}
				else if (alg == 1)
				{
					start = System.currentTimeMillis();
					algCost = BasicAlgorithms.bestFit2(sigma, sigma.length, k);		
					finish = System.currentTimeMillis();
				}
				else
				{
					int a_plus_b = 12;
					int a = a_plus_b; //use pp (lambda = 1)
					start = System.currentTimeMillis();
					hybrid_alg.getAlgCostAdaptive(sigma, 1000, m, k, a, a_plus_b);					
					finish = System.currentTimeMillis();
				}
				long time = finish-start;
				sumTime += time;
			}
			System.out.println("alg is (0 is FF, 1 is BF, and 2 is adaptive): " + alg + " avg. runtime is:" + sumTime/nTest);
		}			
		

		int a_plus_b = 12;
		for (int a=3; a <= a_plus_b; a+=3)
		{	
			double lambda = (double) a / a_plus_b;
			long sumTime = 0;
			for (int j=0; j < nTest; j++)
			{	
				long start = System.currentTimeMillis();		
				int algCost = hybrid_alg.getAlgCostFixed(sigma, k, predicted_freq, m, a, a_plus_b - a);
				long finish = System.currentTimeMillis();
				long time = finish-start;
				sumTime += time;
			}
			System.out.println("lambda is: " + lambda + " avg. runtime is:" + sumTime/nTest + " eta is: " + eta) ;
		}

	}


	static boolean isPresent (String name, boolean b) {
		
		ArrayList<String> path_names_arrays = new ArrayList<>();
		String[] path_names; 
        //creates a new File instance by converting the given path_name string into an abstract path_name
        File f = new File(name);

        //populates the array with names of files and directories
        path_names = f.list();
        
        if(b) {
        	if(path_names == null || path_names.length == 0) {
            	return false;
            }
            return true;
        }
        else {
        	if(path_names == null) {
            	return false;
            }
            return true;
        }
        
	
	}

@SuppressWarnings("unused")
	public static void main(String[] args) {
    	
    	//number of items
		int n = 1000000;
		//bin capacity
		int k = 100;
		//for generating adaptive input
		int window_len = 50000; 
		//the profile size
		int m = 5000;
		
		

		Scanner scanner = new Scanner(System.in);
		
		//generating the sequence or running the alg
		boolean test = false;
		System.out.println("Welcome to the experiments for online bin packing with predictions."); //We have n=10^6, k=100, and m=5000.\n");
		

		//benchmark names
		System.out.print("\nPlease enter the name of your preferable benchmark: (e.g., GI, Weibull, Hard28, Randomly_Generated, Schwerin, Wascher) \n you can add your own benchmark under Data\\Benchmarks. \n ");
		String benchmark_name = "";
		int sw = 0;
		while(sw == 0) {
			benchmark_name = scanner.next();
			if(isPresent("Data/Benchmarks/" + benchmark_name, true)) {
				sw = 1;
			}
			else {
				System.out.print("Sorry, your input is invalid (no such directory). Try again!");
			}
		}


		System.out.println("\nPlease select the experiment. Enter one of the followings:"
		+ "\n0: Generating input sequences; use this option at the beginning for experiments that need more than one input (evolving sequences and section B4 ))"  
		+ "\n1: Test Hybrid on fixed input on a singe file (file 1.txt under GeneratedSequences/Fixed/" + benchmark_name + ")"
		+ "\n2: Test Adaptive on an evolving seqence (first make sure there are 20 files generated in GeneratedSequences/Evolving/" + benchmark_name + ")"
		+ "\n3: Profile size experiment (on file 1.txt under GeneratedSequences/Fixed/" + benchmark_name + ")" 
		+ "\n4: Test Hybrid on 20 input sequence with avg_error (first make sure there are 20 files generated in GeneratedSequences/Fixed/" + benchmark_name + ")");



		int experType = scanner.nextInt();
		int experMode = -1;
		if (experType == 0) // generating input
		{
			System.out.println("Generating input. Indicate the type of input (0 for fixed and 1 for evolving)");
			experMode = scanner.nextInt(); 
			System.out.println(" ________________________________________\nGenerating sequences: \n");

			if (experMode == 0) //fixed
			{

				if(!isPresent("Data/GeneratedSequences/Fixed/" + benchmark_name, false)) {
					String p = "Data/GeneratedSequences/Fixed/" + benchmark_name;
					File file = new File(p);
					boolean b = file.mkdir();
					if(!b) {
						System.out.println("Error: the folder to create your adaptive sequence could not be created. Please try again.");
						System.exit(0);
					}
				}
				generateInputFixed(benchmark_name, n, k, 20);
				System.out.println("Please find 20  generated input sequences at Data/GeneratedSequnce/Fixed/" + benchmark_name + "/");
			}
			else
			{
				if(!isPresent("Data/GeneratedSequences/Evolving/" + benchmark_name, false)) {
					String p = "Data/GeneratedSequences/Evolving/" + benchmark_name;
					File file = new File(p);
					boolean b = file.mkdir();
					if(!b) {
						System.out.println("Error: the folder to create your adaptive sequence could not be created. Please try again.");
						System.exit(0);
					}
				}
				generateInputEvolving(benchmark_name, n, k, window_len, 20);
				System.out.println("Please find 20 generated input sequences at Data/GeneratedSequnces/Evolving/" + benchmark_name);
			}
		}
		else if (experType == 1)
		{
			if(!new File("Data/GeneratedSequences/Fixed/" + benchmark_name + "/1.txt").isFile()) {
				generateInputFixed(benchmark_name, n, k,1);
				System.out.println("We generated sequence from your chosen benchmark to run the test.\n You can find it at Data/GeneratedSequnces/Fixed" 
						+ benchmark_name + "/1.txt");
			}
			System.out.println(" ________________________________________\nTesting Hybrid on the following fixed sequence: \n");
			System.out.println("Data/GeneratedSequences/Fixed/" + benchmark_name + "/1.txt\n");

			testFixed(n, k, m, benchmark_name);
			System.out.println("Please find your results at Data/Results/" + benchmark_name + "OutputFixed.txt");
		}
		else if (experType == 2)
		{
			if(!new File("Data/GeneratedSequences/Evolving/" + benchmark_name +  "/" + benchmark_name + "InputSeqEvolving2.txt").isFile()) {
				System.out.println("\n The input sequence files are missing. First use option 0 to generate (evolving) sequences.");
			}
			else{
				System.out.println(" ________________________________________\nTesting Adaptive on evolving sequences in the following folder: \n");
				System.out.println("Data/GeneratedSequences/Evolving/" + benchmark_name + "\n");

				testEvolving(n, k, window_len, benchmark_name, m);
				System.out.println("Please find your results at Data/Results/"	+ benchmark_name + "OutputEvolving.txt");	
			}
		}
		else if (experType == 3)
		{
			System.out.println(" ________________________________________\nTesting Hybrid with different profile sizes on the following fixed sequence: \n");
			System.out.println("Data/GeneratedSequences/Fixed/" + benchmark_name + "/1.txt\n");
			testProfileSize( n, k, benchmark_name);
			System.out.println("Please find your results at Data/Results/"	+ benchmark_name + "OutputProfileSizeExperiment.txt");
		}
		else if (experType == 4)
		{
			if(!new File("Data/GeneratedSequences/Fixed/" + benchmark_name +  "/2.txt").isFile()) {
				System.out.println("\n The input sequence files are missing. First use option 0 to generate (fixed) sequences.");
			}
			else{
				System.out.println(" ________________________________________\nTesting Hybrid on sequences: in the following folder: \n");
				System.out.println("Data/GeneratedSequences/Fixed/" + benchmark_name + "\n");
				test_static_avg(n, k, m, benchmark_name);
				System.out.println("Please find your results at Data/Results/"	+ benchmark_name + "OutputFixedAverage.txt");
			}
		}
		else {
			System.out.println("Sorry, your input was invalid. Please select your preferred distribution. Try again.\n ");
		}
	}
}

