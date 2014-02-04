package qclib.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BitSetUtil {

	/**
	 * Typically takes a list of BitSets and returns a BitSet with the OR of all of them.
	 * @param bss Any iterable collection of BitSets 
	 * @return
	 */
	public static BitSet orAll(Iterable<BitSet> bss) {
		if (bss == null)
			throw new IllegalArgumentException("please no null's");
		Iterator<BitSet> iter = bss.iterator();
		if (!iter.hasNext()) 							// case where no BitSets in bss
			return new BitSet(); 						// (all bits set to 0)
		BitSet ret = (BitSet)iter.next().clone(); 		// copy of first BitSet
		for ( ; iter.hasNext(); ret.or(iter.next()))	// OR each additional BitSet with ret
			;
		return ret;
	}

	/** bs.flip(0, numbits); */
	public static void complementBitSet(BitSet bs, int numbits) {
		bs.flip(0, numbits);
	}
	

//////// Realized it may be easier to work with int[] specifying bit positions instead of List<BitSet>.  We'll see.
	
//	/** Return BitSet with one bit set -- the bit at position i */
//	public static BitSet toBitSet(int i) {
//		BitSet ret = new BitSet();
//		ret.set(i);
//		return ret;
//	}
//	
//	/** Return <tt>ArrayList</tt> with a BitSet for every argument given, with one bit set each at the given position */
//	public static List<BitSet> toBitSetList(int... bits) {
//		if (bits == null)
//			throw new IllegalArgumentException("please no null's");
//		List<BitSet> l = new ArrayList<BitSet>(bits.length);
//		for (int i=0; i<bits.length; i++)
//			l.add(toBitSet(bits[i]));
//		return l;
//	}
//	
//	/** Throws an exception if there is a BitSet with more than one bit set. 
//	 * Typically used to guarantee that each BitSet represents a single qubit index. */
//	public static void checkIsSingleBitList(List<BitSet> l) {
//		if (l == null)
//			throw new IllegalArgumentException("please no null's");
//		for (BitSet bs : l)
//			if (bs.cardinality() != 1)
//				throw new RuntimeException("list of BitSets contains a BitSet with more than one bit set: "+bs);
//	}
//	
//	/** A kind of toString method for single bit lists. */
//	public static String printSingleBitList(List<BitSet> l) {
//		checkIsSingleBitList(l);
//		StringBuilder sb = new StringBuilder("{");
//		for (BitSet bs : l)
//			sb.append(bs.nextSetBit(0)).append(", ");
//		sb.insert(sb.length()-1, '}');
//		return sb.toString();
//	}
//	
//	/** Iterates over the singe int's inside the list of single bits. */
//	public static ListIterator<Integer> iterateOverSingleBitList(List<BitSet> l) {
//		checkIsSingleBitList(l);
//		final ListIterator<BitSet> bsiter = l.listIterator();
//		return new ListIterator<Integer>() {
//
//			@Override
//			public boolean hasNext() {
//				return bsiter.hasNext();
//			}
//
//			@Override
//			public Integer next() {
//				return bsiter.next().nextSetBit(0);
//			}
//
//			@Override
//			public void remove() {
//				bsiter.remove();
//			}
//
//			@Override
//			public boolean hasPrevious() {
//				return bsiter.hasPrevious();
//			}
//
//			@Override
//			public Integer previous() {
//				return bsiter.previous().nextSetBit(0);
//			}
//
//			@Override
//			public int nextIndex() {
//				return bsiter.nextIndex();
//			}
//
//			@Override
//			public int previousIndex() {
//				return bsiter.previousIndex();
//			}
//
//			@Override
//			public void set(Integer i) {
//				bsiter.set(toBitSet(i));
//			}
//
//			@Override
//			public void add(Integer i) {
//				bsiter.add(toBitSet(i));
//			}
//			
//		};
//	}

}
