package org.lemsml.jlems.template;

import java.util.HashMap;

import org.lemsml.jlems.run.StateInstance;
 

public class FixedTemplateElement extends TemplateElement {

	
	public String val;
	
	
	public FixedTemplateElement(String sv) {
		val = sv;
	}
	
	public String toString() {
		return "Fixed: " + val;
	}
	
	 
	public String eval(HashMap<String, String> vmap) {
		return val;
	}

	@Override
	public String eval(StateInstance so, HashMap<String, StateInstance> context) {
		return val;
	}
 

}