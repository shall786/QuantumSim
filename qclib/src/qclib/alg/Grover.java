package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;
import qclib.util.CartesianRepresentation;

/**
 * Class implementing Grover's algorithm.
 * Using order sqrt(N) evaluations of the function algorithm determines one of 
 * the solutions of the search problem with high probability.
 * The class also incorporates the visualisation of the mechanism of the algorithm.
 */
public class Grover {
	
	private QubitRegister qr;
	private CartesianRepresentation visualisation;
	private int arity;
	private boolean visualise;
	private int[] solutions;
	private int visualisationDelayTime;
	
	/**
	 * Implementing function provided as an Oracle.
	 */
	static class SpecialF extends Operator {

		private FunctionFilter funct;
		
		public SpecialF(FunctionFilter function, int arity) {

			super(arity);
			assert arity >= 2;
			this.funct = function;
		}

		@Override
		protected FieldVector<Complex> myApply(FieldVector<Complex> invec) {
			FieldVector<Complex> outvec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), invec.getDimension());
			outvec.set(Complex.ZERO);
			
			for (int x = 0; x < 1<<(this.getArity()-1); x++)
				for (int y=0; y <= 1; y++) {
					
					int idxin = (x << 1) | y;
					int idxout = (x << 1) | (y ^ (funct.apply(x) ? 1 : 0));
					
					outvec.setEntry( idxout,  outvec.getEntry(idxout).add(invec.getEntry(idxin)) );
				}
			
			return outvec;
		}
		
	}
	
	public Grover(){
		this.setVisualisation(false);
		this.setVisualisationDelayTime(500);
	}
	
	public Grover(boolean visualise){
		this.setVisualisation(visualise);
		this.setVisualisationDelayTime(500);
	}
	
	public Grover(int visualisationDelayTime){
		this.setVisualisation(true);
		this.setVisualisationDelayTime(visualisationDelayTime);
	}
	
	public Grover(boolean visualise, int visualisationDelayTime){
		this.setVisualisation(visualise);
		this.setVisualisationDelayTime(visualisationDelayTime);
	}
	
	public void setVisualisation(boolean visualise){
		if(visualise){
			this.visualisation = new CartesianRepresentation();
		} else {
			this.visualisation = null;
		}
		this.visualise = visualise;
	}
	
	public boolean getVisualisation(){
		return this.visualise;
	}
	
	public void setVisualisationDelayTime(int visualisationDelayTime){
		this.visualisationDelayTime = visualisationDelayTime;
	}
	
	public int getVisualisationDelayTime(){
		return this.visualisationDelayTime;
	}
	
	public void setSolutions(int[] solutions){
		this.solutions = solutions.clone();
	}
	
	public int[] getSolutions(){
		return this.solutions;
	}
	
	/**
	 * A simple function determining if the array of ints contains the integer of interest
	 */
	private static boolean intArrayContains(int currentState, int[] myArray) {
	    boolean found = false;

	    for (int i = 0; !found && (i < myArray.length); i++) {
	        found = (myArray[i] == currentState);
	    }

	    return found;
	}

	//Method used to visualize Grover's algorithm if visualization is switched on
	public void visualiseGrover(boolean firstTime) throws InterruptedException {
		if(!this.visualise){
			return;
		}
		
		double xc = 0;
		double yc = 0;
		
		FieldVector<Complex> temp1 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, this.arity+1));
		
		for(int i=0;i<(1<<this.arity+1);i=i+2){
			if(intArrayContains((int)Math.floor(i/2), this.solutions)){
				yc += Math.sqrt(2)*temp1.getEntry(i).getReal();
			} else {
				xc += Math.sqrt(2)*temp1.getEntry(i).getReal();
			}
		}
		
		xc /= Math.sqrt((1<<this.arity)-this.solutions.length);
		yc /= Math.sqrt(this.solutions.length);
		
		this.visualisation.vector.setComponents(xc, yc);
		if(firstTime){
			this.visualisation.initialStateLine.setComponents(xc, yc);
		}
		this.visualisation.repaint();
		
		Thread.sleep(this.visualisationDelayTime);
	}
	
	/**
	 * Performs Grover's algorithm.
	 * Works with high probability.
	 * @param arity - the number of the qubits used for as the arguments for the function
	 * @param funct - Oracle function returning true if the argument is the solution of the search problem, false otherwise
	 * @return solution of the solutions of the search problems (with high probability)
	 * 
	 * @throws InterruptedException 
	 */
	public long doGrover(final int arity, FunctionFilter funct) throws InterruptedException {
		assert arity > 1;
		
		this.arity = arity;
		
		if(this.solutions == null){
			throw new IllegalArgumentException("Solutions list not provided");
		}
		
		//In case more than a half of the elements of the search space are the solutions it is necessary to add additional
		//qubit to double the search space and hence perform a search where less than half of the elements are the soltuions
		//This is because due to the nature of the algorithm and particularly the way of calculating the number of iterations
		if(this.solutions.length >= (1<<this.arity)/2){
			return doGrover(arity+1, funct);
		}
		
		this.qr = new QubitRegister(arity+1);
		
		//Build the quantum register with first bit |1> and then 'arity' bits |0>
		this.qr.setAmps( QuantumUtil.buildVector(0,1), 0);
		for(int i=1;i<this.qr.getNumqubits();i++){
			this.qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		
		//Apply H gate to every qubit
		for(int i=0;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
		
		//Updates the state of the visualisation of the algorithm
		//The argument true indicates that this is the equally weighted superposition to draw the symmetry axis for part of the
		//Grover's iteration (2|v><v|-I)
		this.visualiseGrover(true);
		
		//Defines the Oracle function
		SpecialF search = new SpecialF(funct, arity+1);
		
		//Perform Grover iterations
		for(int j=1;j<Math.ceil(Math.PI/4*Math.sqrt((1 << arity)/(double) this.solutions.length));j++){
			
			//Oracle
			this.qr.doOp(search, QuantumUtil.makeConsecutiveIntArray(0, arity+1));
			
			this.visualiseGrover(false);
			
			//H gates
			for(int i=1;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
			
			//Conditional phase shift
			FieldVector<Complex> temp2 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, arity+1));			
			for(int i=2;i<(1<<arity+1);i++){
				temp2.setEntry(i, temp2.getEntry(i).negate());
			}
			this.qr.setAmps(temp2, QuantumUtil.makeConsecutiveIntArray(0, arity+1));
			
			//H gates
			for(int i=1;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
			
			this.visualiseGrover(false);
		}
		
		System.out.print(this.qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		//Measurement
		long result = 0;
		for(int i=1;i<arity;i++){
			if(this.qr.measure(i)){
				result += (1 << (i-1));
			}
		}
		return result;
	}
	
	//Helper function to be implemented in the Oracle
	private static class Find implements FunctionFilter {
		private int[] toFind;
		
		public Find(int[] intsToFind){
			this.toFind = intsToFind;
		}
		
		@Override
		public boolean apply(int argument){
			if(intArrayContains(argument,this.toFind)){
				return true;
			}
			return false;
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		Grover d = new Grover(true);
		
		//Firstly you have to create an array of solutions of the problem
		int[] solutions = new int[2];
		solutions[0] = 0;
		solutions[1] = 1;
		
		//then provide the array of solutions to the object for the purpose of visualization
		d.setSolutions(solutions);
		//set the delay in microseconds between the steps of visualization
		d.setVisualisationDelayTime(300);
		
		long result;
		
		//Perform Grover algorithm for search space of size _n_ qubits
		//Implement function looking for the array of _solutions_ inside the Oracle
		int n = 5;
		result = d.doGrover(n, new Find(solutions));
		System.out.println("Result: " + result);
	}
}
