/**
 * This class file was automatically generated by jASN1 v1.8.0 (http://www.openmuc.org)
 */

package org.onosproject.xran.codecs.api;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.types.BerBoolean;
import org.openmuc.jasn1.ber.types.BerEnum;
import org.openmuc.jasn1.ber.types.BerInteger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class PropScell implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);

	public byte[] code = null;
	private PCIARFCN pciArfcn = null;
	private BerBoolean crossCarrierSchedEnable = null;
	private BerEnum caDirection = null;
	private BerInteger deactTimer = null;
	
	public PropScell() {
	}

	public PropScell(byte[] code) {
		this.code = code;
	}

	public void setPciArfcn(PCIARFCN pciArfcn) {
		this.pciArfcn = pciArfcn;
	}

	public PCIARFCN getPciArfcn() {
		return pciArfcn;
	}

	public void setCrossCarrierSchedEnable(BerBoolean crossCarrierSchedEnable) {
		this.crossCarrierSchedEnable = crossCarrierSchedEnable;
	}

	public BerBoolean getCrossCarrierSchedEnable() {
		return crossCarrierSchedEnable;
	}

	public void setCaDirection(BerEnum caDirection) {
		this.caDirection = caDirection;
	}

	public BerEnum getCaDirection() {
		return caDirection;
	}

	public void setDeactTimer(BerInteger deactTimer) {
		this.deactTimer = deactTimer;
	}

	public BerInteger getDeactTimer() {
		return deactTimer;
	}

	public int encode(BerByteArrayOutputStream os) throws IOException {
		return encode(os, true);
	}

	public int encode(BerByteArrayOutputStream os, boolean withTag) throws IOException {

		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				os.write(code[i]);
			}
			if (withTag) {
				return tag.encode(os) + code.length;
			}
			return code.length;
		}

		int codeLength = 0;
		codeLength += deactTimer.encode(os, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 3
		os.write(0x83);
		codeLength += 1;
		
		codeLength += caDirection.encode(os, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 2
		os.write(0x82);
		codeLength += 1;
		
		codeLength += crossCarrierSchedEnable.encode(os, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 1
		os.write(0x81);
		codeLength += 1;
		
		codeLength += pciArfcn.encode(os, false);
		// write tag: CONTEXT_CLASS, CONSTRUCTED, 0
		os.write(0xA0);
		codeLength += 1;
		
		codeLength += BerLength.encodeLength(os, codeLength);

		if (withTag) {
			codeLength += tag.encode(os);
		}

		return codeLength;

	}

	public int decode(InputStream is) throws IOException {
		return decode(is, true);
	}

	public int decode(InputStream is, boolean withTag) throws IOException {
		int codeLength = 0;
		int subCodeLength = 0;
		BerTag berTag = new BerTag();

		if (withTag) {
			codeLength += tag.decodeAndCheck(is);
		}

		BerLength length = new BerLength();
		codeLength += length.decode(is);

		int totalLength = length.val;
		codeLength += totalLength;

		subCodeLength += berTag.decode(is);
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
			pciArfcn = new PCIARFCN();
			subCodeLength += pciArfcn.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
			crossCarrierSchedEnable = new BerBoolean();
			subCodeLength += crossCarrierSchedEnable.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
			caDirection = new BerEnum();
			subCodeLength += caDirection.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 3)) {
			deactTimer = new BerInteger();
			subCodeLength += deactTimer.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
		}
		throw new IOException("Unexpected end of sequence, length tag: " + totalLength + ", actual sequence length: " + subCodeLength);

		
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream os = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(os, false);
		code = os.getArray();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendAsString(sb, 0);
		return sb.toString();
	}

	public void appendAsString(StringBuilder sb, int indentLevel) {

		sb.append("{");
		sb.append("\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (pciArfcn != null) {
			sb.append("pciArfcn: ");
			pciArfcn.appendAsString(sb, indentLevel + 1);
		}
		else {
			sb.append("pciArfcn: <empty-required-field>");
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (crossCarrierSchedEnable != null) {
			sb.append("crossCarrierSchedEnable: ").append(crossCarrierSchedEnable);
		}
		else {
			sb.append("crossCarrierSchedEnable: <empty-required-field>");
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (caDirection != null) {
			sb.append("caDirection: ").append(caDirection);
		}
		else {
			sb.append("caDirection: <empty-required-field>");
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (deactTimer != null) {
			sb.append("deactTimer: ").append(deactTimer);
		}
		else {
			sb.append("deactTimer: <empty-required-field>");
		}
		
		sb.append("\n");
		for (int i = 0; i < indentLevel; i++) {
			sb.append("\t");
		}
		sb.append("}");
	}

}

