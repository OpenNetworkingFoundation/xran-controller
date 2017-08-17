/**
 * This class file was automatically generated by jASN1 v1.8.0 (http://www.openmuc.org)
 */

package org.onosproject.xran.codecs.api;

import org.onosproject.xran.codecs.ber.types.BerInteger;

import java.math.BigInteger;


public class ARFCNValue extends BerInteger {

    private static final long serialVersionUID = 1L;

    public ARFCNValue() {
    }

    public ARFCNValue(byte[] code) {
        super(code);
    }

    public ARFCNValue(BigInteger value) {
        super(value);
    }

    public ARFCNValue(long value) {
        super(value);
    }

    @Override
    public int hashCode() {
        return value.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ARFCNValue) {
            return value.intValue() == ((ARFCNValue) obj).value.intValue();
        }
        return super.equals(obj);
    }
}
