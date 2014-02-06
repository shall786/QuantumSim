package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class PhaseGate extends Operator {
	private double phaseShift;
	
	public PhaseGate(double phaseShift) {
		super(1);
		this.phaseShift = phaseShift;
	}

	/**
	 * a|0> + b|1> ==> a|0> - b*e^(i*phaseShift)|1>
	 * Creates new vector; does not change original.
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(1, outvec.getEntry(1).multiply(new Complex(0,this.phaseShift).exp()));
		return outvec;
	}

}
