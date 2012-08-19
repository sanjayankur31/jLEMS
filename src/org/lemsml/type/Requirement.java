package org.lemsml.type;

import org.lemsml.annotation.Mat;
import org.lemsml.annotation.Mel;
import org.lemsml.canonical.CanonicalElement;
import org.lemsml.expression.Dimensional;
import org.lemsml.util.ContentError;
import org.lemsml.util.E;

@Mel(info = "A Requirement gives the name and dimension of a quantity (parameter or variable)"
+ " that should be accessible within the scope of a "
+ "model component. This is only applicable for elements that can be included as children of other elements, where "
+ "the scope comprises its own parameters and those in the scope of its enclosing element. Once a requirement has "
+ "been declared, then the quantity can be used within the Behavior definition of the component. It is the "
+ "responsibility of an implementation to check that the component is only used in a context in which the requirement "
+ "is met. A typical example is in defining membrand bound components which require access to the membrane "
+ "potential  but where the variable that holds the potential itself is defined in the top level component.")
public class Requirement implements Named {

    @Mat(info = "name")
    public String name;
    @Mat(info = "reference to a dimension")
    public String dimension;
    public Dimension r_dimension;
    public String description;

    public Requirement() {
    }

    public Requirement(String name, Dimension r_dimension) {
        this.name = name;
        this.r_dimension = r_dimension;
        this.dimension = r_dimension.getName();
    }

    public void resolve(LemsCollection<Dimension> dimensions) throws ContentError {
        if (dimension == null) {
            E.warning("no dimension for " + name);
        } else if (dimension.equals("*")) {
            r_dimension = new DeferredDimension();
        } else {
            Dimension d = dimensions.getByName(dimension);
            if (d != null) {
                r_dimension = d;
                //	E.info("resolved param " + name);
            } else {
                throw new ContentError("No such dimension: " + dimension + ". Existing ones: " + dimensions.listAsText());
            }
        }
    }

    public String getName() {
        return name;
    }

    public Dimension getDimension() {
        return r_dimension;
    }

    public CanonicalElement makeCanonical() {
        CanonicalElement ret = new CanonicalElement("Requirement");
        ret.add(new CanonicalElement("name", name));
        ret.add(new CanonicalElement("dimension", dimension));
        return ret;
    }

    public Dimensional getDimensionality() {
        return r_dimension;
    }

    public Requirement makeCopy() {
        Requirement ret = new Requirement();
        ret.name = name;
        ret.dimension = dimension;
        ret.r_dimension = r_dimension;
        return ret;
    }
}