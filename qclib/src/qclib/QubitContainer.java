package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.linear.SparseFieldVector;

import qclib.util.QuantumUtil;

@SuppressWarnings("deprecation")
public class QubitContainer {
	private int numbits;
	/** Either a dense or sparse vector of data.
	 *  Ex. 2 qubits => {|00>, |01>, |10>, |11>} */
	private FieldVector<Complex> data;
	
	/** Qubit container initially contains 1 in the |00...0> amplitude and 0 everywhere else. */
	public QubitContainer(int numbits, boolean isSparse) {
		this.numbits = numbits;
		if (isSparse) {
			data = new SparseFieldVector<Complex>(ComplexField.getInstance(),numbits);
		} else {
			data = new ArrayFieldVector<Complex>(ComplexField.getInstance(),numbits);
		}
	}
	
	public int getNumbits() { return numbits; }
	
	/** Returns a copy of the data vector. */
	public FieldVector<Complex> getAmps() {
		return data.copy(); 
	}
	
	/** 
	 * Sets amplitudes and returns itself.
	 * @param amps Complex Vector of length 2^numbits
	 * @return this
	 */
	public QubitContainer setAmps(FieldVector<Complex> amps) {
		if (amps == null || amps.getDimension() > numbits)
			throw new IllegalArgumentException("bad number of amps given: "+amps);
		data = amps.copy();
		checkUnit(); // for safety
		return this;
	}
	
	/**
	 * Ensures unit magnitude for sums of squares of amplitudes.
	 */
	private void checkUnit() {
		//TODO: turn off for performance, along with all the above argument error checking
		double sumSquares = 0.0;
		for (Complex c : data.toArray())
			sumSquares += c.abs()*c.abs();
		if (!QuantumUtil.isApproxZero(sumSquares - 1.0))
			throw new IllegalStateException("Qubit data does not have proper amplitudes (squares of amps sum to "+sumSquares+")");
	}
	
	/**
	 * Perform Operation on the targetbits in the order specified.
	 * Call with either this.doOp(op, new int[] {3, 1});
	 * 				or	this.doOp(op, 3, 1);
	 * @param op
	 * @param targetbits
	 * @return
	 */
	public QubitContainer doOp(Operator op, int... targetbits) {
		if (op == null || targetbits == null || targetbits.length > numbits 
				|| op.getArity() != targetbits.length)
			throw new IllegalArgumentException("bad argument operator targetbits");
		int targetbitmask = 0;
		for (int tb : targetbits)
			targetbitmask |= 1<<tb;
		doOpFree(op, targetbits, 0, targetbitmask, 0);
		return this;
	}
	
	/*
	 * The following code implements a recursive algorithm to fill the argument vector according to the 
	 * bits specified in order via targetbits.  Todo: write lots of unit tests. 
	 */
	
	/** Build freebitset */
	private void doOpFree(Operator op, int[] targetbits, 
			int bit, int targetbitmask, int freebitset) {
		while (bit < numbits && ((1<<bit)&targetbitmask) != 0)
			bit++;
		if (bit >= numbits)
			doOpTarget(op, targetbits, freebitset);
		doOpFree(op, targetbits, bit+1, targetbitmask, freebitset);
		doOpFree(op, targetbits, bit+1, targetbitmask, freebitset | (1<<bit));
	}
	
	/** Given fixed configuration of free bits, transfer data from this register to vec, do op on vec to get new vec, and transfer data from vec back to this register */
	private void doOpTarget(Operator op, int[] targetbits, int freebitset) {
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),targetbits.length);
		doOpTargetGet(targetbits, vec, freebitset, 0, 0);
		vec = op.apply(vec);
		doOpTargetSet(targetbits, vec, freebitset, 0, 0);
	}
	
	/** Fills vec with the appropriate amps in order */
	private void doOpTargetGet(int[] targetbits, FieldVector<Complex> vec, 
			int origbitset, int vecidx, int vecbitset) {
		if (vecidx == targetbits.length)
			vec.setEntry(vecbitset, this.data.getEntry(origbitset));
		else {
			doOpTargetGet(targetbits, vec, origbitset, vecidx+1, vecbitset); // targetbits[targetidx]->0
			origbitset |= 1<<targetbits[vecidx];
			vecbitset |= 1<<vecidx;
			doOpTargetGet(targetbits, vec, origbitset, vecidx+1, vecbitset); // targetbits[targetidx]->1
		}
	}
	
	/** Changes the qubit values to those in vec, using order of targetbits */
	private void doOpTargetSet(int[] targetbits, FieldVector<Complex> vec, 
			int origbitset, int vecidx, int vecbitset) {
		if (vecidx == targetbits.length)
			this.data.setEntry(vecbitset, vec.getEntry(origbitset));
		else {
			doOpTargetGet(targetbits, vec, origbitset, vecidx+1, vecbitset); // targetbits[targetidx]->0
			origbitset |= 1<<targetbits[vecidx];
			vecbitset |= 1<<vecidx;
			doOpTargetGet(targetbits, vec, origbitset, vecidx+1, vecbitset); // targetbits[targetidx]->1
		}
	}

	/**
	 * Measure a single bit. Uses probability according to the amplitudes.
	 * Collapses the container to a state with only the measured bit.
	 * @param targetbit which bit to measure
	 * @return boolean true for |1> and false for |0>
	 */
	public boolean measure(int targetbit) {
		if (targetbit < 0 || targetbit >= numbits)
			throw new IllegalArgumentException("bad target measurement bit: "+targetbit);
		double sumSquaresZero = measureHelp(targetbit, 0, 0);
		double sumSquaresOne = measureHelp(targetbit, 0, 1<<targetbit);
		assert QuantumUtil.isApproxZero(sumSquaresZero + sumSquaresOne - 1); // sanity check
		
		// return 0 with probability sumSquaresZero
		// return 1 with probability 1-sumSquaresZero == sumSquaresOne
		return (Math.random() < sumSquaresZero);
	}
	
	// targetbit==1, bitnum==2, bitaddr==1 01
	// sum the squares of each coefficient, holding targetbit constant and varying all other free bits
	private double measureHelp(int targetbit, int bitnum, int bitaddr) {
		if (bitnum == targetbit)
			return measureHelp(targetbit, bitnum+1, bitaddr);
		if (bitnum >= numbits)
			return QuantumUtil.square( data.getEntry(bitaddr).abs() );
		double tmp = measureHelp(targetbit, bitnum+1, bitaddr);
		tmp += measureHelp(targetbit, bitnum+1, bitaddr | (1<<bitnum));
		return tmp;
	}
	
}