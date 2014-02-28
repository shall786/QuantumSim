/**
 * 
 */
package qclib;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.op.CNOT;
import qclib.util.QuantumUtil;

/**
 * @author dhutchis
 *
 */
public class RandomTests {

	@Test
	public final void test_measure0() {
		QubitRegister qr = new QubitRegister(15);
		qr.setAmps(QuantumUtil.buildVector(3.0/5, 4.0/5), 10); // qubit 10 is (3/5)|0> + (4/5)|1>
		qr.setAmps(QuantumUtil.buildVector(3.0/5, -4.0/5), 11); // qubit 11 is (3/5)|0> - (4/5)|1>
		//System.out.println(qr.printBits(10,11));
		
		qr.couple(10,11);
		//System.out.println(qr.printBits(10,11));
		
		qr.doOp(new CNOT(),	10, 11); // target bit 10, control bit 11
		//System.out.println(qr.printBits(10,11));
		
		boolean meas0 = qr.measure(10);
		System.out.println("measured a "+(meas0 ? 1 : 0) +" on qubit 10");
		FieldVector<Complex> aout = qr.getAmps(10,11);
		
		if (!meas0) {
			// measurement is 0
			double norm = Math.sqrt((9.0/25)*(9.0/25) + (-16.0/25)*(-16.0/25));
			assertTrue( QuantumUtil.isApproxEqualVector(aout, 
					QuantumUtil.buildVector((9.0/25)/norm, 0, (-16.0/25)/norm, 0 )) );
			
			// now measure second bit
			boolean meas1 = qr.measure(10);
			assertEquals(meas0, meas1); // sanity
			meas1 = qr.measure(11);
			aout = qr.getAmps(10,11);
			
			System.out.println("measured a "+(meas1 ? 1 : 0) +" on qubit 11");
			System.out.println(qr.printBits(10,11));
			
			if (!meas1) {
				// measured 0 second bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(1, 0, 0, 0)) );
			} else {
				// measured 1 second bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 0, -1, 0 )) );
			}
			
			
		} else {
			// measurement is 1
			double norm = Math.sqrt((12.0/25)*(12.0/25) + (-12.0/25)*(-12.0/25));
			assertTrue( QuantumUtil.isApproxEqualVector(aout, 
					QuantumUtil.buildVector(0, (12.0/25)/norm, 0, (-12.0/25)/norm )) );
			
			// now measure second bit
			boolean meas1 = qr.measure(10);
			assertEquals(meas0, meas1); // sanity
			meas1 = qr.measure(11);
			aout = qr.getAmps(10,11);
			
			System.out.println("measured a "+(meas1 ? 1 : 0) +" on qubit 11");
			System.out.println(qr.printBits(10,11));
						
			if (!meas1) {
				// measured 0 second bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 1, 0, 0)) );
			} else {
				// measured 1 second bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 0, 0, -1 )) );
			}
		}
		
		//System.out.println(QuantumUtil.printVector(qr.getAmps(10)));
//		System.out.println(qr.printBits(10,11));
	}
	
	@Test
	public final void test_measure1() {
		QubitRegister qr = new QubitRegister(15);
		qr.setAmps(QuantumUtil.buildVector(3.0/5, 4.0/5), 10); // qubit 10 is (3/5)|0> + (4/5)|1>
		qr.setAmps(QuantumUtil.buildVector(3.0/5, -4.0/5), 11); // qubit 11 is (3/5)|0> - (4/5)|1>
		//System.out.println(qr.printBits(10,11));
		
		qr.couple(10,11);
		//System.out.println(qr.printBits(10,11));
		
		qr.doOp(new CNOT(),	10, 11); // target bit 10, control bit 11
		//System.out.println(qr.printBits(10,11));
		
		boolean meas1 = qr.measure(11);
		System.out.println("measured a "+(meas1 ? 1 : 0) +" on qubit 11");
		FieldVector<Complex> aout = qr.getAmps(10,11);
		System.out.println(qr.printBits(10,11));
		
		if (!meas1) {
			// measurement is 0
			double norm = Math.sqrt((9.0/25)*(9.0/25) + (12.0/25)*(12.0/25));
			assertTrue( QuantumUtil.isApproxEqualVector(aout, 
					QuantumUtil.buildVector((9.0/25)/norm, (12.0/25)/norm, 0, 0 )) );
			
			// now measure first bit
			boolean meas0 = qr.measure(11);
			assertEquals(meas1, meas0); // sanity
			meas0 = qr.measure(10);
			aout = qr.getAmps(10,11);
			
			System.out.println("measured a "+(meas0 ? 1 : 0) +" on qubit 11");
			System.out.println(qr.printBits(10,11));
			
			if (!meas0) {
				// measured 0 first bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(1, 0, 0, 0)) );
			} else {
				// measured 1 first bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 1, 0, 0 )) );
			}
			
			
		} else {
			// measurement is 1
			double norm = Math.sqrt((-16.0/25)*(-16.0/25) + (-12.0/25)*(-12.0/25));
			System.out.printf("%f %f\n", (-16.0/25)/norm, (-12.0/25)/norm);
			System.out.println(QuantumUtil.printVector(aout)); // DOES NOT MATCH printBits
			assertTrue( QuantumUtil.isApproxEqualVector(aout, 
					QuantumUtil.buildVector(0, 0, (-16.0/25)/norm, (-12.0/25)/norm )) );
			
			/* Possible error in getAmps() from aout
measured a 1 on qubit 11
{10,11}:
 { |00>=( 0    , 0    i), |01>=( 0    , 0    i), |10>=(-0.8  , 0    i), |11>=(-0.6  , 0    i) }

-0.800000 -0.600000
<(0.0, 0.0),(-0.8000000000000002, 0.0),(0.0, 0.0),(-0.6, 0.0)>
			 */
			
			// now measure first bit
			boolean meas0 = qr.measure(10);
			assertEquals(meas1, meas0); // sanity
			meas0 = qr.measure(11);
			aout = qr.getAmps(10,11);
			
			System.out.println("measured a "+(meas0 ? 1 : 0) +" on qubit 11");
			System.out.println(qr.printBits(10,11));
						
			if (!meas0) {
				// measured 0 first bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 0, -1, 0)) );
			} else {
				// measured 1 first bit
				assertTrue( QuantumUtil.isApproxEqualVector(aout, 
						QuantumUtil.buildVector(0, 0, 0, -1 )) );
			}
		}
		
		//System.out.println(QuantumUtil.printVector(qr.getAmps(10)));
//		System.out.println(qr.printBits(10,11));
	}

}
