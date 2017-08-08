package org.onosproject.xran.samplemessages;

import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PRBUsage;
import org.onosproject.xran.codecs.api.QCI;
import org.onosproject.xran.codecs.pdu.*;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.string.BerUTF8String;

import java.io.UnsupportedEncodingException;

public class SchedReportPerCell {

    public XrancPdu setPacketProperties(XrancPdu recv_pdu) {
        ECGI ecgi = recv_pdu.getBody().getL2MeasConfig().getEcgi();

        SchedMeasReportPerCell.QciVals qciVals = new SchedMeasReportPerCell.QciVals();
        qciVals.setQCI(new QCI(1));
        qciVals.setQCI(new QCI(2));

        PRBUsage pcell = new PRBUsage();
        PRBUsage.PrbUsageDl prbUsageDl = new PRBUsage.PrbUsageDl();
        prbUsageDl.setBerInteger(new BerInteger(50));
        prbUsageDl.setBerInteger(new BerInteger(100));
        pcell.setPrbUsageDl(prbUsageDl);

        PRBUsage.PrbUsageUl prbUsageUl = new PRBUsage.PrbUsageUl();
        prbUsageUl.setBerInteger(new BerInteger(50));
        prbUsageUl.setBerInteger(new BerInteger(100));
        pcell.setPrbUsageUl(prbUsageUl);

        PRBUsage scell = new PRBUsage();
        prbUsageDl.setBerInteger(new BerInteger(50));
        prbUsageDl.setBerInteger(new BerInteger(100));
        scell.setPrbUsageDl(prbUsageDl);

        prbUsageUl.setBerInteger(new BerInteger(50));
        prbUsageUl.setBerInteger(new BerInteger(100));
        scell.setPrbUsageUl(prbUsageUl);

        SchedMeasReportPerCell schedMeasReportPerCell = new SchedMeasReportPerCell();
        schedMeasReportPerCell.setEcgi(ecgi);
        schedMeasReportPerCell.setQciVals(qciVals);
        schedMeasReportPerCell.setPrbUsagePcell(pcell);
        schedMeasReportPerCell.setPrbUsageScell(scell);

        XrancPduBody body = new XrancPduBody();
        body.setSchedMeasReportPerCell(schedMeasReportPerCell);

        BerUTF8String ver = null;
        try {
            ver = new BerUTF8String("4");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        XrancApiID apiID = new XrancApiID(23);
        XrancPduHdr hdr = new XrancPduHdr();
        hdr.setVer(ver);
        hdr.setApiId(apiID);

        XrancPdu pdu = new XrancPdu();
        pdu.setHdr(hdr);
        pdu.setBody(body);

        return pdu;

    }
}
