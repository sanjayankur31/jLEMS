package org.lemsml.jlemsio.examples;

import java.io.File;

import org.lemsml.jlems.logging.E;
import org.lemsml.jlems.sim.Sim;
import org.lemsml.jlemsio.reader.FileInclusionReader;


public class RunFileExample {
 
	
	File fdir = null;
	String filename;
	
	
	
	
	public RunFileExample(String fnm) {
		this (new File("."), fnm);
	}
	
	public RunFileExample(File f, String fnm) {
		fdir = f;
		filename = fnm;
	 
	}
  	
	private File getSrcFile() {
		File fs = new File(fdir, filename);
		return fs;
	}
	
	 
		
	public void run() {
		 
			FileInclusionReader fir = new FileInclusionReader(getSrcFile());
 			
			
			try {
			Sim sim = new Sim(fir.read());
	 
			sim.readModel();	
 			
		
		    sim.build();
	        sim.run();
			} catch (Exception ex) {
				E.report("Failed to run " + filename, ex);
			}
		}
	
	
	public void runEulerTree() {
         try {
	 
		FileInclusionReader fir = new FileInclusionReader(getSrcFile());
			Sim sim = new Sim(fir.read());
		 
		sim.setNoConsolidation();
		
		sim.readModel();	
	 		
		sim.build();
		
		sim.runTree();
         } catch (Exception ex) {
        	 E.report("Failed to run " + filename, ex);
         }
		 
	}
	
	public void runWithMeta() {
        try {
	 
		FileInclusionReader fir = new FileInclusionReader(getSrcFile());
		Sim sim = new Sim(fir.read());
		 
		// sim.setNoConsolidation();

		sim.readModel();	
	 		
		sim.build();
		
		sim.runWithMeta();
        } catch (Exception ex) {
       	 E.report("Failed to run " + filename, ex);
        }
	}
	
	
	
	public void printConsolidated() {
		
 
		try {
			FileInclusionReader fir = new FileInclusionReader(getSrcFile());
			Sim sim = new Sim(fir.read());
			sim.readModel();	
	 		
			sim.build();
			sim.printFirstConsolidated();
		} catch (Exception ex) {
			E.report("Failed to consolidate" + filename, ex);
			 
		}
	}
	
}