package qclib.op;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

/**
 * 
 * @author dhutchis
 *
 */
public class CZ extends Operator {

	public CZ() {
		super(2);
	}

	/**
	 * <pre>
	 *     a|00> + b|01> + c|10> + d|11>
	 * ==> a|00> + b|01> + c|10> - d|11>
	 * </pre>
	 * Creates new vector; does not change original.
	 * First bit is target bit; second bit is control bit.
	 * Todo: FIX ME
	 */
	@Override
	public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(3, outvec.getEntry(3).negate());
		return outvec;
	}

}
