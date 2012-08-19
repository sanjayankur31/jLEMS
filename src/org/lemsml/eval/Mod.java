package org.lemsml.eval;

import java.util.ArrayList;

public class Mod extends DOp {

	
	public Mod(DVal dvl, DVal dvr) {
		super(dvl, dvr);
	}
	
	
	public Mod makeCopy() {
		return new Mod(left.makeCopy(), right.makeCopy());
	}
	
	public Mod makePrefixedCopy(String s) {
		return new Mod(left.makePrefixedCopy(s), right.makePrefixedCopy(s));
	}
	
	public double eval() {
		return Math.round(left.eval()) % Math.round(right.eval());
	}

        @Override
        public String toString() {
                return "(" + left + " % " + right + ")";
        }

        public String toString(String prefix, ArrayList<String> ignore) {
                return "("+left.toString(prefix, ignore) + " % " + right.toString(prefix, ignore) +")";
        }
	
}