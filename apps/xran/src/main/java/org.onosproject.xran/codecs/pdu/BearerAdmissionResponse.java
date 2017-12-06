/**
 * This class file was automatically generated by jASN1 v1.8.0 (http://www.openmuc.org)
 */

package org.onosproject.xran.codecs.pdu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.onosproject.xran.codecs.api.*;
import org.onosproject.xran.codecs.ber.BerByteArrayOutputStream;
import org.onosproject.xran.codecs.ber.BerLength;
import org.onosproject.xran.codecs.ber.BerTag;
import org.onosproject.xran.codecs.ber.types.BerInteger;
import org.onosproject.xran.codecs.ber.types.string.BerUTF8String;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class BearerAdmissionResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);

	@JsonIgnore
	public byte[] code = null;
	private CRNTI crnti = null;
	private ECGI ecgi = null;
	private BerInteger numErabList = null;
	private ERABResponse erabResponse = null;
	
	public BearerAdmissionResponse() {
	}

	public BearerAdmissionResponse(byte[] code) {
		this.code = code;
	}

	public void setCrnti(CRNTI crnti) {
		this.crnti = crnti;
	}

	public CRNTI getCrnti() {
		return crnti;
	}

	public void setEcgi(ECGI ecgi) {
		this.ecgi = ecgi;
	}

	public ECGI getEcgi() {
		return ecgi;
	}

	public void setNumErabList(BerInteger numErabList) {
		this.numErabList = numErabList;
	}

	public BerInteger getNumErabList() {
		return numErabList;
	}

	public void setErabResponse(ERABResponse erabResponse) {
		this.erabResponse = erabResponse;
	}

	public ERABResponse getErabResponse() {
		return erabResponse;
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
		codeLength += erabResponse.encode(os, false);
		// write tag: CONTEXT_CLASS, CONSTRUCTED, 3
		os.write(0xA3);
		codeLength += 1;
		
		codeLength += numErabList.encode(os, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 2
		os.write(0x82);
		codeLength += 1;
		
		codeLength += ecgi.encode(os, false);
		// write tag: CONTEXT_CLASS, CONSTRUCTED, 1
		os.write(0xA1);
		codeLength += 1;
		
		codeLength += crnti.encode(os, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 0
		os.write(0x80);
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
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
			crnti = new CRNTI();
			subCodeLength += crnti.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 1)) {
			ecgi = new ECGI();
			subCodeLength += ecgi.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
			numErabList = new BerInteger();
			subCodeLength += numErabList.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 3)) {
			erabResponse = new ERABResponse();
			subCodeLength += erabResponse.decode(is, false);
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
		if (crnti != null) {
			sb.append("crnti: ").append(crnti);
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (ecgi != null) {
			sb.append("ecgi: ");
			ecgi.appendAsString(sb, indentLevel + 1);
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (numErabList != null) {
			sb.append("numErabList: ").append(numErabList);
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (erabResponse != null) {
			sb.append("erabResponse: ");
			erabResponse.appendAsString(sb, indentLevel + 1);
		}
		
		sb.append("\n");
		for (int i = 0; i < indentLevel; i++) {
			sb.append("\t");
		}
		sb.append("}");
	}

	public static XrancPdu constructPacket(ECGI ecgi, CRNTI crnti, ERABParams erabParams, BerInteger numParams, boolean b) {
		ERABResponse erabResponse = new ERABResponse();

		for (int i = 0; i < numParams.intValue(); i++) {
			ERABParamsItem erabParamsItem = erabParams.getERABParamsItem().get(i);

			ERABResponseItem responseItem = new ERABResponseItem();
			responseItem.setId(erabParamsItem.getId());

			// FIXME: add logic
			responseItem.setDecision(new ERABDecision(b ? 0 : 1));

			erabResponse.setERABResponse(responseItem);
		}


		BearerAdmissionResponse bearerAdmissionResponse = new BearerAdmissionResponse();
		bearerAdmissionResponse.setCrnti(crnti);
		bearerAdmissionResponse.setEcgi(ecgi);
		bearerAdmissionResponse.setErabResponse(erabResponse);
		bearerAdmissionResponse.setNumErabList(numParams);

		XrancPduBody body = new XrancPduBody();
		body.setBearerAdmissionResponse(bearerAdmissionResponse);

		BerUTF8String ver = null;
		try {
			ver = new BerUTF8String("3");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		XrancApiID apiID = new XrancApiID(9);
		XrancPduHdr hdr = new XrancPduHdr();
		hdr.setVer(ver);
		hdr.setApiId(apiID);

		XrancPdu pdu = new XrancPdu();
		pdu.setHdr(hdr);
		pdu.setBody(body);

		return pdu;
	}

}
