package org.lemsml.jlems.xml;

import java.util.ArrayList;
 
import org.lemsml.jlems.io.FormatException;
import org.lemsml.jlems.type.Attribute;
import org.lemsml.jlems.type.BodyValued;
import org.lemsml.jlems.type.Parameterized;
import org.lemsml.jlems.type.RawValued;
import org.lemsml.jlems.util.ContentError;
import org.lemsml.jlems.util.E;

public class XMLReader {

    Constructor instantiator;
    int sourceLength;
    int nerror;
    double progressFraction;

   

    public XMLReader(Constructor insta) {
        instantiator = insta;
    }

    public void addSearchPackage(Package pkg) {
        instantiator.addSearchPackage(pkg);
    }

   
    public Object readObject(String s) throws ParseException, BuildException, ContentError, FormatException, XMLException {
        return readFromString(s);
    }

    public Object read(String s) throws ParseException, BuildException, ContentError, FormatException, XMLException {
        return readFromString(s);
    }

    public Object readFromString(String sin) throws ParseException, BuildException, ContentError, FormatException, XMLException {
        String s = sin;
        s = XMLChecker.deGarbage(s);
        
        Object ret = null;
        if (s != null) {

        progressFraction = 0.;

        nerror = 0;
        sourceLength = s.split("\n").length;

        XMLTokenizer tkz = new XMLTokenizer(s);

        XMLToken xmlt = tkz.nextToken();
        while (xmlt.isIntro() || xmlt.isComment()) {

            if (xmlt.isComment()) {
                // E.info("reading comment " + xmlt);
            }
            xmlt = tkz.nextToken();
        }



        XMLHolder xmlHolder = new XMLHolder();

        readFieldIntoParent(tkz, xmlHolder, xmlt, null);

        ret = xmlHolder.getContent();
        }
        return ret;
    }

    class XMLHolder {

        Object content;

        public void setContent(Object obj) {
            content = obj;
        }

        public Object getContent() {
            return content;
        }
    }

    public XMLToken readToken(XMLTokenizer tkz) throws ParseException, XMLException {
        XMLToken xmlt = tkz.nextToken();

        int lno = tkz.lineno();
        if (nerror > 4) {
            throw new ParseException("too many errors - aborting at line " + lno);
        }

        return xmlt;
    }

    
    public String readInnerBlock(XMLTokenizer tkz, XMLToken opener) throws ContentError, XMLException {
       	
    	StringBuffer sb = new StringBuffer();
        if (opener.isClose()) {
            // OK, already got it all;
        } else {
        	String cts = opener.getName();
          	
        	sb.append(tkz.readUntil(cts));
        }
        return sb.toString();
    }
    
    
    public String readBlock(XMLTokenizer tkz, XMLToken opener) throws ParseException, ContentError, XMLException {
        StringBuffer sb = new StringBuffer();
        sb.append(opener.getXMLText());
        if (opener.isClose()) {
            // OK, already got it all;
        } else {
            while (true) {
                XMLToken wkt = readToken(tkz);

                if (wkt == null) {
                    throw new ContentError("no closing token for " + opener + "?");

                } else {
                    sb.append(wkt.getXMLText());

                    if (wkt.closes(opener)) {
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

   
    public void readFieldIntoParent(XMLTokenizer tkz, Object parent, XMLToken start,
        Parameterized rootPtzd) throws ParseException, BuildException, ContentError, FormatException, XMLException {
        // read the child object that is known to the parent as item.name
        // if the parent is a vector, the object is added as a new element;
        // if the parent is a string, the xml is just apended;
        // otherwise the field is set.

        ///E.info("readFieldIntoParent: "+parent+",  start: "+ start);

        Parameterized pt = rootPtzd;
        if (pt == null && parent instanceof Parameterized) {
            pt = (Parameterized) parent;
        }

        if (!start.isOpen()) {
            nerror++;
            throw new XMLException("ERROR - read object start item was not an open tag " + start);
        }


        Object child = null;

        if (parent instanceof String || parent instanceof StringBuffer) {
            child = new StringBuffer();
            ((StringBuffer) child).append(start.getOpenTagString());

        } else {
            // attributes may contain the class - the instantiator processes
            // all the attributes here
            Attribute[] atts = start.getAttributes();
            child = instantiator.getChildObject(parent, start.getName(), atts);
 
            if (child != null) {
                instantiator.applyAttributes(child, atts, pt);
            }

            //E.info("Child: "+ child);


            if (child == null) {
                child = new ArrayList<Object>();

            } else if (child instanceof String) {
                // in this case, set its length to 0. Subseqnet parts of the
                // string will get appended to the current value, so want to
                // keep track of the fact that it is a string, without keeping
                // the default that may have come from above;
                child = new StringBuffer();


            } else if (child.getClass().isArray()) {
                // make it an array list for the time being, then
                // give the lsit to teh instantiator to make into theright sort of
                // array;
                child = new ArrayList<Object>();
            }


            if (start.isClose()) {
                // the tag was both an open and a close tag, so now that we've
                // processed the attributes, we're done;
            } else {
            	if (child instanceof XMLContainer) {
            		 String stxt = readInnerBlock(tkz, start);
                     ((XMLContainer) child).setXMLContent(stxt);
            		
            	}  else {
            	
            	
                // read on and fill in fields until we get a closing tag which
                // matches the start tag
                // the fields will be inserted in xTarget;
                XMLToken next = readToken(tkz);

                while (true) {
                    if (next.isNone()) {
                        // should mean EOF, but could also be an error
                        // return whatever;
                        break;


                    } else if (next.isOpen()) {
                        // open tags could mean anything - elementary field, array,
                        // or object, but in any case, pass them back to this method;
                        if (child instanceof XMLContainer) {
                        	E.info("got and XMLContainer child - reading as raw");
                        	
                            // dont parse the XML - just give it to the contaienr object
                            String stxt = readInnerBlock(tkz, next);
                            ((XMLContainer) child).setXMLContent(stxt);

                            
                            
                            
                 	   } else if (child instanceof RawValued) {
                		   String stxt = readInnerBlock(tkz, next);
                		   String wtxt = "<Wrapper>" + stxt + "</Wrapper>";
                		   Object obj = ElementXMLReader.deserialize(wtxt);
                		   if (obj instanceof XMLElement) {
                			   XMLElement xe = (XMLElement)obj;
                			   for (XMLElement xel : xe.getElements()) {
                				   ((RawValued)child).addXMLElement(xel);
                			   }
                			   // TODO if there is body text in the wrapper...
                		   } else {
                			   throw new ContentError("cant read content as XMLElement: " + stxt);
                		   }
          
                             
                            
                                   
                            
                        } else {
                            /* need to queue tokens for this - is it a good thing to do? MAYDO probably not - doesn't nest
                            if (child instanceof SourceUser) {
                            // it wants the XML, but it wants it parsed too
                            String stxt = readBlock(tkz, next);
                            ((XMLContainer)child).setXMLContent(stxt);
                            }
                             */

                              readFieldIntoParent(tkz, child, next, pt);
                        }


                    } else if (next.isClose()) {
                        if (next.closes(start)) {
                            // fine - close item

                            if (parent instanceof String || parent instanceof StringBuffer) {
                                ((StringBuffer) child).append(next.getCloseTagString());
                            }


                        } else {
                            nerror++;
                            E.shortError(" non-matching close item \n" + "start Item was: \n"
                                + start.toString() + "\n" + "but close was: \n" + next.toString() + "\n");
                        }

                        // stop anyway - either its the right close item, or
                        // the wrong one but lets cary on and see what happens;
                        break;


                    } else if (next.isString()) {
                        // this occurs if we're just reading a simple string
                        // field into the parent, or if we're in an array of strings;
                        // first case obj is defined, so reset it;
                        // second case put it in the vector;

                        
                        
                        if (child instanceof ArrayList) {
                            E.error("attempted to read string into array list?  - ignored" + next.svalue);

                            // ((ArrayList)child).add(next.svalue);


                        } else if (child instanceof StringBuffer) {
                            //   E.deprecate("xml reader - string added to string buffer " + next.svalue);

                            StringBuffer sbo = (StringBuffer) child;
                            String ssf = sbo.toString();
                            if (ssf.endsWith(">") || next.svalue.startsWith("<") || ssf.length() == 0) {
                                sbo.append(next.svalue);

                            } else {
                                sbo.append(" ");
                                sbo.append(next.svalue);
                            }


                        } else {
                            if (child instanceof String && ((String) child).length() > 0) {
                                child = child + " " + next.svalue;

                                //    E.deprecate("appended string to string " + next.svalue);

                            } else if (child == null) {
                                child = next.svalue;

                            } else if (child instanceof Double && ((Double) child).doubleValue() == 0.0) {
                                child = new Double(next.svalue);

                                //          E.info("resetting dbl field " + parent + " " + start.getName() + " " + next.svalue);

                            } else if (child instanceof Integer && ((Integer) child).intValue() == 0) {
                                child = Integer.valueOf(next.svalue);

                            } else if (child instanceof BodyValued) {
                                ((BodyValued) child).setBodyValue(next.svalue);

                            } else if (child instanceof XMLContainer) {
                            	 throw new ContentError("started reading content of XMLContainer - shouldn't be parsing this: " + child + " but read: " + next.svalue); 
                                
                                
                            } else {
                                instantiator.appendContent(child, next.svalue);

                            }
                        }


                    } else if (next.isNumber()) {
                        E.shortError("XMLReader should never return numbers....!!!! but " + "just got " + next);
                    }
                    next = readToken(tkz);
                }
            	}
            }

            // presumably got a close object, and have done one of:
            // a) filled the parameters of obj;
            // b) replaced obj with a new object of the same type
            // c) filled the vector with strings, doubles or objects;



            if (child instanceof StringBuffer) {
                child = ((StringBuffer) child).toString();
            }

            /*
            if (child instanceof ReReferencable) {
            ((ReReferencable)child).reReference();
            }
             */

            if (parent instanceof StringBuffer) {
                StringBuffer psb = (StringBuffer) parent;
                psb.append(child);
                psb.append("\n");


            } else if (parent instanceof XMLHolder) {
                ((XMLHolder) parent).setContent(child);

            } else if (parent instanceof ArrayList) {
                setListChild(parent, child);

            } else {
                ///E.info("           - Setting field: " + parent + ", " + start);
                instantiator.setField(parent, start.getName(), child, pt);
            }
        }
    }

     @SuppressWarnings("unchecked")
	private void setListChild(Object parent, Object child) {
        ((ArrayList<Object>) parent).add(child);
    }
}