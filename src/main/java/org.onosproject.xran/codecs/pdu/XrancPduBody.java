/**
 * This class file was automatically generated by jASN1 v1.8.0 (http://www.openmuc.org)
 */

package org.onosproject.xran.codecs.pdu;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class XrancPduBody implements Serializable {

	private static final long serialVersionUID = 1L;

	public byte[] code = null;
	private CellConfigRequest cellConfigRequest = null;
	private CellConfigReport cellConfigReport = null;
	private UEAdmissionRequest uEAdmissionRequest = null;
	private UEAdmissionResponse uEAdmissionResponse = null;
	private UEAttachComplete uEAttachComplete = null;
	private UEAdmissionStatus uEAdmissionStatus = null;
	private UEReconfigInd uEReconfigInd = null;
	private UEReleaseInd uEReleaseInd = null;
	private BearerAdmissionRequest bearerAdmissionRequest = null;
	private BearerAdmissionResponse bearerAdmissionResponse = null;
	private BearerAdmissionStatus bearerAdmissionStatus = null;
	private BearerReleaseInd bearerReleaseInd = null;
	private UECapabilityEnquiry uECapabilityEnquiry = null;
	private UECapabilityInfo uECapabilityInfo = null;
	private HORequest hORequest = null;
	private HOFailure hOFailure = null;
	private HOComplete hOComplete = null;
	private RXSigMeasConfig rXSigMeasConfig = null;
	private RXSigMeasReport rXSigMeasReport = null;
	private L2MeasConfig l2MeasConfig = null;
	private RadioMeasReportPerUE radioMeasReportPerUE = null;
	private RadioMeasReportPerCell radioMeasReportPerCell = null;
	private SchedMeasReportPerUE schedMeasReportPerUE = null;
	private SchedMeasReportPerCell schedMeasReportPerCell = null;
	private PDCPMeasReportPerUe pDCPMeasReportPerUe = null;
	private XICICConfig xICICConfig = null;
	private RRMConfig rRMConfig = null;
	private RRMConfigStatus rRMConfigStatus = null;
	private ScellAdd scellAdd = null;
	private ScellAddStatus scellAddStatus = null;
	private ScellDelete scellDelete = null;
	private SeNBAdd seNBAdd = null;
	private SeNBAddStatus seNBAddStatus = null;
	private SeNBDelete seNBDelete = null;
	private TrafficSplitConfig trafficSplitConfig = null;
	
	public XrancPduBody() {
	}

	public XrancPduBody(byte[] code) {
		this.code = code;
	}

	public void setCellConfigRequest(CellConfigRequest cellConfigRequest) {
		this.cellConfigRequest = cellConfigRequest;
	}

	public CellConfigRequest getCellConfigRequest() {
		return cellConfigRequest;
	}

	public void setCellConfigReport(CellConfigReport cellConfigReport) {
		this.cellConfigReport = cellConfigReport;
	}

	public CellConfigReport getCellConfigReport() {
		return cellConfigReport;
	}

	public void setUEAdmissionRequest(UEAdmissionRequest uEAdmissionRequest) {
		this.uEAdmissionRequest = uEAdmissionRequest;
	}

	public UEAdmissionRequest getUEAdmissionRequest() {
		return uEAdmissionRequest;
	}

	public void setUEAdmissionResponse(UEAdmissionResponse uEAdmissionResponse) {
		this.uEAdmissionResponse = uEAdmissionResponse;
	}

	public UEAdmissionResponse getUEAdmissionResponse() {
		return uEAdmissionResponse;
	}

	public void setUEAttachComplete(UEAttachComplete uEAttachComplete) {
		this.uEAttachComplete = uEAttachComplete;
	}

	public UEAttachComplete getUEAttachComplete() {
		return uEAttachComplete;
	}

	public void setUEAdmissionStatus(UEAdmissionStatus uEAdmissionStatus) {
		this.uEAdmissionStatus = uEAdmissionStatus;
	}

	public UEAdmissionStatus getUEAdmissionStatus() {
		return uEAdmissionStatus;
	}

	public void setUEReconfigInd(UEReconfigInd uEReconfigInd) {
		this.uEReconfigInd = uEReconfigInd;
	}

	public UEReconfigInd getUEReconfigInd() {
		return uEReconfigInd;
	}

	public void setUEReleaseInd(UEReleaseInd uEReleaseInd) {
		this.uEReleaseInd = uEReleaseInd;
	}

	public UEReleaseInd getUEReleaseInd() {
		return uEReleaseInd;
	}

	public void setBearerAdmissionRequest(BearerAdmissionRequest bearerAdmissionRequest) {
		this.bearerAdmissionRequest = bearerAdmissionRequest;
	}

	public BearerAdmissionRequest getBearerAdmissionRequest() {
		return bearerAdmissionRequest;
	}

	public void setBearerAdmissionResponse(BearerAdmissionResponse bearerAdmissionResponse) {
		this.bearerAdmissionResponse = bearerAdmissionResponse;
	}

	public BearerAdmissionResponse getBearerAdmissionResponse() {
		return bearerAdmissionResponse;
	}

	public void setBearerAdmissionStatus(BearerAdmissionStatus bearerAdmissionStatus) {
		this.bearerAdmissionStatus = bearerAdmissionStatus;
	}

	public BearerAdmissionStatus getBearerAdmissionStatus() {
		return bearerAdmissionStatus;
	}

	public void setBearerReleaseInd(BearerReleaseInd bearerReleaseInd) {
		this.bearerReleaseInd = bearerReleaseInd;
	}

	public BearerReleaseInd getBearerReleaseInd() {
		return bearerReleaseInd;
	}

	public void setUECapabilityEnquiry(UECapabilityEnquiry uECapabilityEnquiry) {
		this.uECapabilityEnquiry = uECapabilityEnquiry;
	}

	public UECapabilityEnquiry getUECapabilityEnquiry() {
		return uECapabilityEnquiry;
	}

	public void setUECapabilityInfo(UECapabilityInfo uECapabilityInfo) {
		this.uECapabilityInfo = uECapabilityInfo;
	}

	public UECapabilityInfo getUECapabilityInfo() {
		return uECapabilityInfo;
	}

	public void setHORequest(HORequest hORequest) {
		this.hORequest = hORequest;
	}

	public HORequest getHORequest() {
		return hORequest;
	}

	public void setHOFailure(HOFailure hOFailure) {
		this.hOFailure = hOFailure;
	}

	public HOFailure getHOFailure() {
		return hOFailure;
	}

	public void setHOComplete(HOComplete hOComplete) {
		this.hOComplete = hOComplete;
	}

	public HOComplete getHOComplete() {
		return hOComplete;
	}

	public void setRXSigMeasConfig(RXSigMeasConfig rXSigMeasConfig) {
		this.rXSigMeasConfig = rXSigMeasConfig;
	}

	public RXSigMeasConfig getRXSigMeasConfig() {
		return rXSigMeasConfig;
	}

	public void setRXSigMeasReport(RXSigMeasReport rXSigMeasReport) {
		this.rXSigMeasReport = rXSigMeasReport;
	}

	public RXSigMeasReport getRXSigMeasReport() {
		return rXSigMeasReport;
	}

	public void setL2MeasConfig(L2MeasConfig l2MeasConfig) {
		this.l2MeasConfig = l2MeasConfig;
	}

	public L2MeasConfig getL2MeasConfig() {
		return l2MeasConfig;
	}

	public void setRadioMeasReportPerUE(RadioMeasReportPerUE radioMeasReportPerUE) {
		this.radioMeasReportPerUE = radioMeasReportPerUE;
	}

	public RadioMeasReportPerUE getRadioMeasReportPerUE() {
		return radioMeasReportPerUE;
	}

	public void setRadioMeasReportPerCell(RadioMeasReportPerCell radioMeasReportPerCell) {
		this.radioMeasReportPerCell = radioMeasReportPerCell;
	}

	public RadioMeasReportPerCell getRadioMeasReportPerCell() {
		return radioMeasReportPerCell;
	}

	public void setSchedMeasReportPerUE(SchedMeasReportPerUE schedMeasReportPerUE) {
		this.schedMeasReportPerUE = schedMeasReportPerUE;
	}

	public SchedMeasReportPerUE getSchedMeasReportPerUE() {
		return schedMeasReportPerUE;
	}

	public void setSchedMeasReportPerCell(SchedMeasReportPerCell schedMeasReportPerCell) {
		this.schedMeasReportPerCell = schedMeasReportPerCell;
	}

	public SchedMeasReportPerCell getSchedMeasReportPerCell() {
		return schedMeasReportPerCell;
	}

	public void setPDCPMeasReportPerUe(PDCPMeasReportPerUe pDCPMeasReportPerUe) {
		this.pDCPMeasReportPerUe = pDCPMeasReportPerUe;
	}

	public PDCPMeasReportPerUe getPDCPMeasReportPerUe() {
		return pDCPMeasReportPerUe;
	}

	public void setXICICConfig(XICICConfig xICICConfig) {
		this.xICICConfig = xICICConfig;
	}

	public XICICConfig getXICICConfig() {
		return xICICConfig;
	}

	public void setRRMConfig(RRMConfig rRMConfig) {
		this.rRMConfig = rRMConfig;
	}

	public RRMConfig getRRMConfig() {
		return rRMConfig;
	}

	public void setRRMConfigStatus(RRMConfigStatus rRMConfigStatus) {
		this.rRMConfigStatus = rRMConfigStatus;
	}

	public RRMConfigStatus getRRMConfigStatus() {
		return rRMConfigStatus;
	}

	public void setScellAdd(ScellAdd scellAdd) {
		this.scellAdd = scellAdd;
	}

	public ScellAdd getScellAdd() {
		return scellAdd;
	}

	public void setScellAddStatus(ScellAddStatus scellAddStatus) {
		this.scellAddStatus = scellAddStatus;
	}

	public ScellAddStatus getScellAddStatus() {
		return scellAddStatus;
	}

	public void setScellDelete(ScellDelete scellDelete) {
		this.scellDelete = scellDelete;
	}

	public ScellDelete getScellDelete() {
		return scellDelete;
	}

	public void setSeNBAdd(SeNBAdd seNBAdd) {
		this.seNBAdd = seNBAdd;
	}

	public SeNBAdd getSeNBAdd() {
		return seNBAdd;
	}

	public void setSeNBAddStatus(SeNBAddStatus seNBAddStatus) {
		this.seNBAddStatus = seNBAddStatus;
	}

	public SeNBAddStatus getSeNBAddStatus() {
		return seNBAddStatus;
	}

	public void setSeNBDelete(SeNBDelete seNBDelete) {
		this.seNBDelete = seNBDelete;
	}

	public SeNBDelete getSeNBDelete() {
		return seNBDelete;
	}

	public void setTrafficSplitConfig(TrafficSplitConfig trafficSplitConfig) {
		this.trafficSplitConfig = trafficSplitConfig;
	}

	public TrafficSplitConfig getTrafficSplitConfig() {
		return trafficSplitConfig;
	}

	public int encode(BerByteArrayOutputStream os) throws IOException {

		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				os.write(code[i]);
			}
			return code.length;
		}

		int codeLength = 0;
		if (trafficSplitConfig != null) {
			codeLength += trafficSplitConfig.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 34
			os.write(0x22);
			os.write(0xBF);
			codeLength += 2;
			return codeLength;
		}
		
		if (seNBDelete != null) {
			codeLength += seNBDelete.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 33
			os.write(0x21);
			os.write(0xBF);
			codeLength += 2;
			return codeLength;
		}
		
		if (seNBAddStatus != null) {
			codeLength += seNBAddStatus.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 32
			os.write(0x20);
			os.write(0xBF);
			codeLength += 2;
			return codeLength;
		}
		
		if (seNBAdd != null) {
			codeLength += seNBAdd.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 31
			os.write(0x1F);
			os.write(0xBF);
			codeLength += 2;
			return codeLength;
		}
		
		if (scellDelete != null) {
			codeLength += scellDelete.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 30
			os.write(0xBE);
			codeLength += 1;
			return codeLength;
		}
		
		if (scellAddStatus != null) {
			codeLength += scellAddStatus.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 29
			os.write(0xBD);
			codeLength += 1;
			return codeLength;
		}
		
		if (scellAdd != null) {
			codeLength += scellAdd.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 28
			os.write(0xBC);
			codeLength += 1;
			return codeLength;
		}
		
		if (rRMConfigStatus != null) {
			codeLength += rRMConfigStatus.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 27
			os.write(0xBB);
			codeLength += 1;
			return codeLength;
		}
		
		if (rRMConfig != null) {
			codeLength += rRMConfig.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 26
			os.write(0xBA);
			codeLength += 1;
			return codeLength;
		}
		
		if (xICICConfig != null) {
			codeLength += xICICConfig.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 25
			os.write(0xB9);
			codeLength += 1;
			return codeLength;
		}
		
		if (pDCPMeasReportPerUe != null) {
			codeLength += pDCPMeasReportPerUe.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 24
			os.write(0xB8);
			codeLength += 1;
			return codeLength;
		}
		
		if (schedMeasReportPerCell != null) {
			codeLength += schedMeasReportPerCell.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 23
			os.write(0xB7);
			codeLength += 1;
			return codeLength;
		}
		
		if (schedMeasReportPerUE != null) {
			codeLength += schedMeasReportPerUE.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 22
			os.write(0xB6);
			codeLength += 1;
			return codeLength;
		}
		
		if (radioMeasReportPerCell != null) {
			codeLength += radioMeasReportPerCell.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 21
			os.write(0xB5);
			codeLength += 1;
			return codeLength;
		}
		
		if (radioMeasReportPerUE != null) {
			codeLength += radioMeasReportPerUE.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 20
			os.write(0xB4);
			codeLength += 1;
			return codeLength;
		}
		
		if (l2MeasConfig != null) {
			codeLength += l2MeasConfig.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 19
			os.write(0xB3);
			codeLength += 1;
			return codeLength;
		}
		
		if (rXSigMeasReport != null) {
			codeLength += rXSigMeasReport.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 18
			os.write(0xB2);
			codeLength += 1;
			return codeLength;
		}
		
		if (rXSigMeasConfig != null) {
			codeLength += rXSigMeasConfig.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 17
			os.write(0xB1);
			codeLength += 1;
			return codeLength;
		}
		
		if (hOComplete != null) {
			codeLength += hOComplete.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 16
			os.write(0xB0);
			codeLength += 1;
			return codeLength;
		}
		
		if (hOFailure != null) {
			codeLength += hOFailure.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 15
			os.write(0xAF);
			codeLength += 1;
			return codeLength;
		}
		
		if (hORequest != null) {
			codeLength += hORequest.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 14
			os.write(0xAE);
			codeLength += 1;
			return codeLength;
		}
		
		if (uECapabilityInfo != null) {
			codeLength += uECapabilityInfo.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 13
			os.write(0xAD);
			codeLength += 1;
			return codeLength;
		}
		
		if (uECapabilityEnquiry != null) {
			codeLength += uECapabilityEnquiry.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 12
			os.write(0xAC);
			codeLength += 1;
			return codeLength;
		}
		
		if (bearerReleaseInd != null) {
			codeLength += bearerReleaseInd.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 11
			os.write(0xAB);
			codeLength += 1;
			return codeLength;
		}
		
		if (bearerAdmissionStatus != null) {
			codeLength += bearerAdmissionStatus.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 10
			os.write(0xAA);
			codeLength += 1;
			return codeLength;
		}
		
		if (bearerAdmissionResponse != null) {
			codeLength += bearerAdmissionResponse.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 9
			os.write(0xA9);
			codeLength += 1;
			return codeLength;
		}
		
		if (bearerAdmissionRequest != null) {
			codeLength += bearerAdmissionRequest.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 8
			os.write(0xA8);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEReleaseInd != null) {
			codeLength += uEReleaseInd.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 7
			os.write(0xA7);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEReconfigInd != null) {
			codeLength += uEReconfigInd.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 6
			os.write(0xA6);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEAdmissionStatus != null) {
			codeLength += uEAdmissionStatus.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 5
			os.write(0xA5);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEAttachComplete != null) {
			codeLength += uEAttachComplete.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 4
			os.write(0xA4);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEAdmissionResponse != null) {
			codeLength += uEAdmissionResponse.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 3
			os.write(0xA3);
			codeLength += 1;
			return codeLength;
		}
		
		if (uEAdmissionRequest != null) {
			codeLength += uEAdmissionRequest.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 2
			os.write(0xA2);
			codeLength += 1;
			return codeLength;
		}
		
		if (cellConfigReport != null) {
			codeLength += cellConfigReport.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 1
			os.write(0xA1);
			codeLength += 1;
			return codeLength;
		}
		
		if (cellConfigRequest != null) {
			codeLength += cellConfigRequest.encode(os, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 0
			os.write(0xA0);
			codeLength += 1;
			return codeLength;
		}
		
		throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
	}

	public int decode(InputStream is) throws IOException {
		return decode(is, null);
	}

	public int decode(InputStream is, BerTag berTag) throws IOException {

		int codeLength = 0;
		BerTag passedTag = berTag;

		if (berTag == null) {
			berTag = new BerTag();
			codeLength += berTag.decode(is);
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
			cellConfigRequest = new CellConfigRequest();
			codeLength += cellConfigRequest.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 1)) {
			cellConfigReport = new CellConfigReport();
			codeLength += cellConfigReport.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 2)) {
			uEAdmissionRequest = new UEAdmissionRequest();
			codeLength += uEAdmissionRequest.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 3)) {
			uEAdmissionResponse = new UEAdmissionResponse();
			codeLength += uEAdmissionResponse.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 4)) {
			uEAttachComplete = new UEAttachComplete();
			codeLength += uEAttachComplete.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 5)) {
			uEAdmissionStatus = new UEAdmissionStatus();
			codeLength += uEAdmissionStatus.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 6)) {
			uEReconfigInd = new UEReconfigInd();
			codeLength += uEReconfigInd.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 7)) {
			uEReleaseInd = new UEReleaseInd();
			codeLength += uEReleaseInd.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 8)) {
			bearerAdmissionRequest = new BearerAdmissionRequest();
			codeLength += bearerAdmissionRequest.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 9)) {
			bearerAdmissionResponse = new BearerAdmissionResponse();
			codeLength += bearerAdmissionResponse.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 10)) {
			bearerAdmissionStatus = new BearerAdmissionStatus();
			codeLength += bearerAdmissionStatus.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 11)) {
			bearerReleaseInd = new BearerReleaseInd();
			codeLength += bearerReleaseInd.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 12)) {
			uECapabilityEnquiry = new UECapabilityEnquiry();
			codeLength += uECapabilityEnquiry.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 13)) {
			uECapabilityInfo = new UECapabilityInfo();
			codeLength += uECapabilityInfo.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 14)) {
			hORequest = new HORequest();
			codeLength += hORequest.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 15)) {
			hOFailure = new HOFailure();
			codeLength += hOFailure.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 16)) {
			hOComplete = new HOComplete();
			codeLength += hOComplete.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 17)) {
			rXSigMeasConfig = new RXSigMeasConfig();
			codeLength += rXSigMeasConfig.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 18)) {
			rXSigMeasReport = new RXSigMeasReport();
			codeLength += rXSigMeasReport.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 19)) {
			l2MeasConfig = new L2MeasConfig();
			codeLength += l2MeasConfig.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 20)) {
			radioMeasReportPerUE = new RadioMeasReportPerUE();
			codeLength += radioMeasReportPerUE.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 21)) {
			radioMeasReportPerCell = new RadioMeasReportPerCell();
			codeLength += radioMeasReportPerCell.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 22)) {
			schedMeasReportPerUE = new SchedMeasReportPerUE();
			codeLength += schedMeasReportPerUE.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 23)) {
			schedMeasReportPerCell = new SchedMeasReportPerCell();
			codeLength += schedMeasReportPerCell.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 24)) {
			pDCPMeasReportPerUe = new PDCPMeasReportPerUe();
			codeLength += pDCPMeasReportPerUe.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 25)) {
			xICICConfig = new XICICConfig();
			codeLength += xICICConfig.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 26)) {
			rRMConfig = new RRMConfig();
			codeLength += rRMConfig.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 27)) {
			rRMConfigStatus = new RRMConfigStatus();
			codeLength += rRMConfigStatus.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 28)) {
			scellAdd = new ScellAdd();
			codeLength += scellAdd.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 29)) {
			scellAddStatus = new ScellAddStatus();
			codeLength += scellAddStatus.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 30)) {
			scellDelete = new ScellDelete();
			codeLength += scellDelete.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 31)) {
			seNBAdd = new SeNBAdd();
			codeLength += seNBAdd.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 32)) {
			seNBAddStatus = new SeNBAddStatus();
			codeLength += seNBAddStatus.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 33)) {
			seNBDelete = new SeNBDelete();
			codeLength += seNBDelete.decode(is, false);
			return codeLength;
		}

		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 34)) {
			trafficSplitConfig = new TrafficSplitConfig();
			codeLength += trafficSplitConfig.decode(is, false);
			return codeLength;
		}

		if (passedTag != null) {
			return 0;
		}

		throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream os = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(os);
		code = os.getArray();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendAsString(sb, 0);
		return sb.toString();
	}

	public void appendAsString(StringBuilder sb, int indentLevel) {

		if (cellConfigRequest != null) {
			sb.append("\"cellConfigRequest\": ");
			cellConfigRequest.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (cellConfigReport != null) {
			sb.append("\"cellConfigReport\": ");
			cellConfigReport.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEAdmissionRequest != null) {
			sb.append("\"uEAdmissionRequest\": ");
			uEAdmissionRequest.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEAdmissionResponse != null) {
			sb.append("\"uEAdmissionResponse\": ");
			uEAdmissionResponse.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEAttachComplete != null) {
			sb.append("\"uEAttachComplete\": ");
			uEAttachComplete.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEAdmissionStatus != null) {
			sb.append("\"uEAdmissionStatus\": ");
			uEAdmissionStatus.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEReconfigInd != null) {
			sb.append("\"uEReconfigInd\": ");
			uEReconfigInd.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uEReleaseInd != null) {
			sb.append("\"uEReleaseInd\": ");
			uEReleaseInd.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (bearerAdmissionRequest != null) {
			sb.append("\"bearerAdmissionRequest\": ");
			bearerAdmissionRequest.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (bearerAdmissionResponse != null) {
			sb.append("\"bearerAdmissionResponse\": ");
			bearerAdmissionResponse.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (bearerAdmissionStatus != null) {
			sb.append("\"bearerAdmissionStatus\": ");
			bearerAdmissionStatus.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (bearerReleaseInd != null) {
			sb.append("\"bearerReleaseInd\": ");
			bearerReleaseInd.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uECapabilityEnquiry != null) {
			sb.append("\"uECapabilityEnquiry\": ");
			uECapabilityEnquiry.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (uECapabilityInfo != null) {
			sb.append("\"uECapabilityInfo\": ");
			uECapabilityInfo.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (hORequest != null) {
			sb.append("\"hORequest\": ");
			hORequest.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (hOFailure != null) {
			sb.append("\"hOFailure\": ");
			hOFailure.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (hOComplete != null) {
			sb.append("\"hOComplete\": ");
			hOComplete.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (rXSigMeasConfig != null) {
			sb.append("\"rXSigMeasConfig\": ");
			rXSigMeasConfig.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (rXSigMeasReport != null) {
			sb.append("\"rXSigMeasReport\": ");
			rXSigMeasReport.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (l2MeasConfig != null) {
			sb.append("\"l2MeasConfig\": ");
			l2MeasConfig.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (radioMeasReportPerUE != null) {
			sb.append("\"radioMeasReportPerUE\": ");
			radioMeasReportPerUE.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (radioMeasReportPerCell != null) {
			sb.append("\"radioMeasReportPerCell\": ");
			radioMeasReportPerCell.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (schedMeasReportPerUE != null) {
			sb.append("\"schedMeasReportPerUE\": ");
			schedMeasReportPerUE.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (schedMeasReportPerCell != null) {
			sb.append("\"schedMeasReportPerCell\": ");
			schedMeasReportPerCell.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (pDCPMeasReportPerUe != null) {
			sb.append("\"pDCPMeasReportPerUe\": ");
			pDCPMeasReportPerUe.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (xICICConfig != null) {
			sb.append("\"xICICConfig\": ");
			xICICConfig.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (rRMConfig != null) {
			sb.append("\"rRMConfig\": ");
			rRMConfig.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (rRMConfigStatus != null) {
			sb.append("\"rRMConfigStatus\": ");
			rRMConfigStatus.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (scellAdd != null) {
			sb.append("\"scellAdd\": ");
			scellAdd.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (scellAddStatus != null) {
			sb.append("\"scellAddStatus\": ");
			scellAddStatus.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (scellDelete != null) {
			sb.append("\"scellDelete\": ");
			scellDelete.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (seNBAdd != null) {
			sb.append("\"seNBAdd\": ");
			seNBAdd.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (seNBAddStatus != null) {
			sb.append("\"seNBAddStatus\": ");
			seNBAddStatus.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (seNBDelete != null) {
			sb.append("\"seNBDelete\": ");
			seNBDelete.appendAsString(sb, indentLevel + 1);
			return;
		}

		if (trafficSplitConfig != null) {
			sb.append("\"trafficSplitConfig\": ");
			trafficSplitConfig.appendAsString(sb, indentLevel + 1);
			return;
		}

		sb.append("<none>");
	}

}

