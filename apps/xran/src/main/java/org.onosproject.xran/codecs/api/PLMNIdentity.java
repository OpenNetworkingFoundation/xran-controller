/**
 * This class file was automatically generated by jASN1 v1.8.0 (http://www.openmuc.org)
 */

package org.onosproject.xran.codecs.api;

import org.onosproject.xran.codecs.ber.types.BerOctetString;

import java.util.Arrays;


public class PLMNIdentity extends BerOctetString {

    private static final long serialVersionUID = 1L;

    public PLMNIdentity() {
    }

    public PLMNIdentity(byte[] value) {
        super(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PLMNIdentity) {
            return Arrays.equals(value, ((PLMNIdentity) obj).value);
        }
        return super.equals(obj);
    }

}