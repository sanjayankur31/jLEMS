package org.lemsml.jlems.sim;

import java.util.ArrayList;

import org.lemsml.jlems.run.ComponentBehavior;
import org.lemsml.jlems.run.RuntimeOutput;
import org.lemsml.jlems.run.RuntimeRecorder;
import org.lemsml.jlems.util.E;

public class RecorderCollector implements ComponentBehaviorVisitor {

	ArrayList<RuntimeRecorder> recorders;
	
	public RecorderCollector(ArrayList<RuntimeRecorder> al) {
		recorders = al;
	}

	@Override
	public void visit(ComponentBehavior cb) {
		ArrayList<RuntimeRecorder> a = cb.getRuntimeRecorders();
		if (a != null && a.size() > 0) {
			recorders.addAll(a);
			// E.info("Added recorder " + a + " from " + cb);
		}
		
	}

	
	
}