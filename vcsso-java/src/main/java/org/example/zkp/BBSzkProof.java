package org.example.zkp;

import java.util.Objects;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

public class BBSzkProof implements Signature {
    @Represented(restorer = "G1")
    private GroupElement elementAPrime;
    @Represented(restorer = "G1")
    private GroupElement elementABar;
    @Represented(restorer = "G1")
    private GroupElement elementD;
    @Represented(restorer = "G1")
    private GroupElement elementPi;

    public BBSzkProof(Representation repr, Group groupG1) {
        (new ReprUtil(this)).register(groupG1, "G1").register(new Zn(groupG1.size()), "Zp").deserialize(repr);
    }

    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public BBSzkProof(GroupElement elementAPrime, GroupElement elementABar, GroupElement elementD, GroupElement elementPi) {
        this.elementAPrime = elementAPrime;
        this.elementABar = elementABar;
        this.elementD = elementD;
        this.elementPi = elementPi;
    }

    public GroupElement getElementAPrime() {
        return elementAPrime;
    }

    public GroupElement getElementABar() {
        return elementABar;
    }

    public GroupElement getElementD() {
        return elementD;
    }

    public GroupElement getElementPi() {
        return elementPi;
    }

    public String toString() {
        return "BBSzkProof"
                + " [elementAPrime=" + this.elementAPrime
                + ", elementABar=" + this.elementABar
                + ", elementD=" + this.elementD
                + ", elementPi=" + this.elementPi
                + "]";
    }

    public int hashCode() {
        //int prime = true;
        int result = 1;
        result = 31 * result + (this.elementAPrime == null ? 0 : this.elementAPrime.hashCode());
        result = 31 * result + (this.elementABar == null ? 0 : this.elementABar.hashCode());
        result = 31 * result + (this.elementD == null ? 0 : this.elementD.hashCode());
        result = 31 * result + (this.elementPi == null ? 0 : this.elementPi.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            BBSzkProof other = (BBSzkProof) obj;
            return Objects.equals(
                    this.elementAPrime, other.elementAPrime)
                    && Objects.equals(this.elementABar, other.elementABar)
                    && Objects.equals(this.elementD, other.elementD)
                    && Objects.equals(this.elementPi, other.elementPi);
        }
    }
}
