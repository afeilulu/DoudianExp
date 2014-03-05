package cn.com.xinli.android.doudian.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author ChenGefei
 * 
 */
public class SAXforUpgradePackageHandler extends DefaultHandler {
	private UpgradePackage upgrade;

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		upgrade = new UpgradePackage();
	}

	@Override
	public void startElement(String uri, String localname, String qname,
			Attributes attributes) throws SAXException {
		if ("version".equals(localname)) {
			upgrade.setMessage(attributes.getValue(0));
			if(attributes.getLength()>1) {
				upgrade.setAnnouncement(attributes.getValue(1));
			}
		} else if("current".equals(localname)) {
			upgrade.setCurVersion(Integer.parseInt(attributes.getValue(0)));
		} else if("package".equals(localname)) {
			String apkUrl = attributes.getValue(0);
			if (apkUrl.endsWith(".apk")){
				upgrade.setPackages(apkUrl);
				upgrade.setSize(attributes.getValue(1));
				if (attributes.getLength() > 2)
					if (attributes.getValue(2) != null)
						upgrade.setMd5(attributes.getValue(2));
			}
		}
		super.startElement(uri,localname,qname,attributes);
	}

	public UpgradePackage getUpgradePackage() {
		return upgrade;
	}
}
