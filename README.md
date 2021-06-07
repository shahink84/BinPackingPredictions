# Online Bin Packing with Predictions
Experiments on online bin packing algorithms with predictions. See https://arxiv.org/abs/2102.03311


### Prerequisites

- Java: Download and install Java [Oracle's website](https://www.oracle.com/java/technologies/javase-downloads.html).

### Compiling the Project

Type javac *.java.

## Deployment

Follow the steps below to generate input sequences from the benchmark you choose and run tests on the benchmarks you choose:

Type java MainClass to run the project.

The code is designed to run the experiments described in the paper and in the appendix. 

First, you are asked to choose from one of the default benchmarks (you can also create your own and place it under "Data/Benchmarks").

Then, you are asked to choose the type of experiments:


	0. generating input sequences. 
			Given the size of input sequences, we have included one sequence for each benchmark. 
			For experiments that concern one sequence (Fixed distributions in Sections 6.4. and B.2.1), this step can be skipped as one default sequence is included for each benchmark (the default sequence is the one used for experiments reported in the paper).  
			For experiments that require averaging over multiple sequences, start with generating the input sequences.
			You are prompted to choose between generating fixed (0) or evolving (1) sequences. 
			For experiments in Section B.4. (averaging the cost of Hybrid over multiple sequences), enter "0". 
			For experiments that involve Adaptive (Evolving distributions in Sections 6.4. and B.2.1), enter "1". 

	1. running Hybrid on a single file (see "Fixed distributions" in Sections 6.4. and B.2.1)
	2. running Adaptive and taking average costs over 20 sequences (see "Evolving distributions" in Sections 6.4. and B.2.1)
	3. running Hybrid with different values of profile size (see Section B3)
	4. running Hybrid and taking the average (see Section B4)

For each experiment, the results will be written on a file under Data/Results/
