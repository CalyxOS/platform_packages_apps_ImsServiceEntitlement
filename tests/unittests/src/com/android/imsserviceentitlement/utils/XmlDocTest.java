/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.imsserviceentitlement.utils;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class XmlDocTest {
    // XML sample from vendor A
    private static final String AUTH_RESPONSE_XML =
            "<wap-provisioningdoc version=\"1.1\">\n"
                    + "    <characteristic type=\"VERS\">\n"
                    + "        <parm name=\"version\" value=\"1\"/>\n"
                    + "        <parm name=\"validity\" value=\"1728000\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"TOKEN\">\n"
                    + "        <parm name=\"token\" value=\"kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX\"/>\n"
                    + "        <parm name=\"validity\" value=\"3600\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"APPLICATION\">\n"
                    + "        <parm name=\"AppID\" value=\"ap2004\"/>\n"
                    + "        <parm name=\"Name\" value=\"VoWiFi Entitlement settings\"/>\n"
                    + "        <parm name=\"EntitlementStatus\" value=\"0\"/>\n"
                    + "        <parm name=\"AddrStatus\" value=\"0\"/>\n"
                    + "        <parm name=\"TC_Status\" value=\"0\"/>\n"
                    + "        <parm name=\"ProvStatus\" value=\"2\"/>\n"
                    + "        <parm name=\"ServiceFlow_URL\""
                    + " value=\"http://vm-host:8180/self-prov-websheet/rcs\"/>\n"
                    + "        <parm name=\"ServiceFlow_UserData\""
                    + " value=\"token=Y5vcmc%3D&amp;entitlementStatus=0&amp;protocol=TS43&amp;"
                    + "provStatus=2&amp;deviceId=358316079424742&amp;subscriberId=0311580718847611"
                    + "%40nai.epc.mnc130.mcc310.3gppnetwork.org&amp;ShowAddress=true\"/>\n"
                    + "    </characteristic>\n"
                    + "</wap-provisioningdoc>\n";

    // XML sample from vendor B, note unescaped "&" in ServiceFlow_UserData
    private static final String AUTH_RESPONSE_XML_2 =
            "<?xml version=\"1.0\"?>"
                    + "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"VERS\">"
                    + "<parm name=\"version\" value=\"4\"/>"
                    + "<parm name=\"validity\" value=\"172800\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"TOKEN\">"
                    + "<parm name=\"token\" value=\"kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2004\"/>"
                    + "<parm name=\"Name\" value=\"VoWiFi Entitlement settings\"/>"
                    + "<parm name=\"MessageForIncompatible\" value=\"99\"/>"
                    + "<parm name=\"EntitlementStatus\" value=\"0\"/>"
                    + "<parm name=\"ServiceFlow_URL\" value=\""
                    + "https://deg.cspire.com/VoWiFi/CheckPostData\"/>"
                    + "<parm name=\"ServiceFlow_UserData\" value=\""
                    + "PostData=U6%2FbQ%2BEP&req_locale=en_US\"/>"
                    + "<parm name=\"AddrStatus\" value=\"0\"/>"
                    + "<parm name=\"TC_Status\" value=\"0\"/>"
                    + "<parm name=\"ProvStatus\" value=\"0\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A XML sample with "&amp;amp;" - unlikely to happen in practice but good to test
    private static final String AUTH_RESPONSE_XML_3 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2004\"/>"
                    + "<parm name=\"ServiceFlow_UserData\" value=\""
                    + "PostData=U6%2FbQ%2BEP&amp;amp;l=en_US\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A XML sample with server URL and user data unset.
    private static final String AUTH_RESPONSE_XML_4 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2004\"/>"
                    + "<parm name=\"ServiceFlow_URL\" value=\"\"/>"
                    + "<parm name=\"ServiceFlow_UserData\" value=\"\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A XML sample with server URL unset and user data set.
    private static final String AUTH_RESPONSE_XML_5 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2004\"/>"
                    + "<parm name=\"ServiceFlow_URL\" value=\"\"/>"
                    + "<parm name=\"ServiceFlow_UserData\" value=\"TEST_DATA\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A XML sample with multiple appIDs
    private static final String AUTH_RESPONSE_XML_6 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2004\"/>"
                    + "<parm name=\"EntitlementStatus\" value=\"0\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2005\"/>"
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A TS43 v8 XML sample with separate 5G config for home and roaming networks
    private static final String AUTH_RESPONSE_XML_7 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2003\"/>"
                    + "<parm name=\"Name\" value=\"Voice over Cellular Entitlement settings\"/>"
                    + "<characteristic type=\"VoiceOverCellularEntitleInfo\">"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"2\"/>" // 5G
                    + "<parm name=\"HomeRoamingNWType\" value=\"2\"/>" // Home network
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>" // Enabled
                    + "<parm name=\"NetworkVoiceIRATCapablity\" value=\"EPS-Fallback\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"2\"/>" // 5G
                    + "<parm name=\"HomeRoamingNWType\" value=\"3\"/>" // Roaming network
                    + "<parm name=\"EntitlementStatus\" value=\"2\"/>" // Incompatible
                    + "<parm name=\"MessageForIncompatible\" value=\"z\"/>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A TS43 v8 XML sample with single 4G and 5G config for both home and roaming networks
    private static final String AUTH_RESPONSE_XML_8 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2003\"/>"
                    + "<parm name=\"Name\" value=\"Voice over Cellular Entitlement settings\"/>"
                    + "<characteristic type=\"VoiceOverCellularEntitleInfo\">"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"1\"/>" // 4G
                    + "<parm name=\"HomeRoamingNWType\" value=\"1\"/>" // Home&Roaming
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>" // Enabled
                    + "</characteristic>"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"2\"/>" // 5G
                    + "<parm name=\"HomeRoamingNWType\" value=\"1\"/>" // Both Home and Roaming
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>" // Enabled
                    + "<parm name=\"NetworkVoiceIRATCapablity\" value=\"EPS-Fallback\"/>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A TS43 v8 XML sample with single 4G for home network only - unlikely to happen
    private static final String AUTH_RESPONSE_XML_9 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2003\"/>"
                    + "<parm name=\"Name\" value=\"Voice over Cellular Entitlement settings\"/>"
                    + "<characteristic type=\"VoiceOverCellularEntitleInfo\">"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"1\"/>" // 4G
                    + "<parm name=\"HomeRoamingNWType\" value=\"2\"/>" // Home
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>" // Enabled
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    // A TS43 v8 XML sample with single 5G for home network only - unlikely to happen
    private static final String AUTH_RESPONSE_XML_10 =
            "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2003\"/>"
                    + "<parm name=\"Name\" value=\"Voice over Cellular Entitlement settings\"/>"
                    + "<characteristic type=\"VoiceOverCellularEntitleInfo\">"
                    + "<characteristic type=\"RATVoiceEntitleInfoDetails\">"
                    + "<parm name=\"AccessType\" value=\"2\"/>" // 5G
                    + "<parm name=\"HomeRoamingNWType\" value=\"2\"/>" // Home
                    + "<parm name=\"EntitlementStatus\" value=\"1\"/>" // Enabled
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    private static final String TOKEN = "kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX";

    @Test
    public void parseAuthenticateResponse() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML);

        assertThat(xmlDoc.getFromToken("token").get()).isEqualTo(TOKEN);
        assertThat(xmlDoc.getFromVersion("version").get()).isEqualTo("1");
        // Note "&amp;" in input XML are un-escaped to "&".
        assertThat(xmlDoc.getFromVowifi("ServiceFlow_UserData").get())
                .isEqualTo("token=Y5vcmc%3D"
                        + "&entitlementStatus=0"
                        + "&protocol=TS43"
                        + "&provStatus=2"
                        + "&deviceId=358316079424742"
                        + "&subscriberId=0311580718847611%40nai.epc.mnc130.mcc310.3gppnetwork.org"
                        + "&ShowAddress=true");
    }

    @Test
    public void parseAuthenticateResponse2() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_2);

        assertThat(xmlDoc.getFromToken("token").get()).isEqualTo(TOKEN);
        // Note the "&" in input XML is kept as is
        assertThat(xmlDoc.getFromVowifi("ServiceFlow_UserData").get())
                .isEqualTo("PostData=U6%2FbQ%2BEP&req_locale=en_US");
    }

    @Test
    public void parseAuthenticateResponse3() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_3);

        // Note the "&amp;amp;" in input XML is un-escaped to "&amp;"
        assertThat(xmlDoc.getFromVowifi("ServiceFlow_UserData").get())
                .isEqualTo("PostData=U6%2FbQ%2BEP&amp;l=en_US");
    }

    @Test
    public void parseAuthenticateResponse4() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_4);

        assertThat(xmlDoc.getFromVowifi("ServiceFlow_URL").isPresent()).isFalse();
        assertThat(xmlDoc.getFromVowifi("ServiceFlow_UserData").isPresent()).isFalse();
    }

    @Test
    public void parseAuthenticateResponse5() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_5);

        assertThat(xmlDoc.getFromVowifi("ServiceFlow_URL").isPresent()).isFalse();
        assertThat(xmlDoc.getFromVowifi("ServiceFlow_UserData").isPresent()).isTrue();
    }

    @Test
    public void parseAuthenticateResponse6() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_6);

        assertThat(xmlDoc.getFromVowifi("EntitlementStatus").get()).isEqualTo("0");
        assertThat(xmlDoc.getFromSmsoverip("EntitlementStatus").get()).isEqualTo("1");
    }

    @Test
    public void parseAuthenticateResponse7() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_7);

        assertThat(xmlDoc.getFromVonrHome("EntitlementStatus").get()).isEqualTo("1");
        assertThat(xmlDoc.getFromVonrHome("NetworkVoiceIRATCapablity").get())
                .isEqualTo("EPS-Fallback");
        assertThat(xmlDoc.getFromVonrRoaming("EntitlementStatus").get()).isEqualTo("2");
        assertThat(xmlDoc.getFromVonrRoaming("NetworkVoiceIRATCapablity").isPresent()).isFalse();
    }

    @Test
    public void parseAuthenticateResponse8() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_8);

        assertThat(xmlDoc.getFromVolte("EntitlementStatus").get()).isEqualTo("1");
        assertThat(xmlDoc.getFromVonrHome("EntitlementStatus").get()).isEqualTo("1");
        assertThat(xmlDoc.getFromVonrHome("NetworkVoiceIRATCapablity").get())
                .isEqualTo("EPS-Fallback");
        assertThat(xmlDoc.getFromVonrRoaming("EntitlementStatus").get()).isEqualTo("1");
        assertThat(xmlDoc.getFromVonrRoaming("NetworkVoiceIRATCapablity").get())
                .isEqualTo("EPS-Fallback");
    }

    @Test
    public void parseAuthenticateResponse9() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_9);

        assertThat(xmlDoc.getFromVolte("EntitlementStatus").get()).isEqualTo("1");
    }

    @Test
    public void parseAuthenticateResponse10() {
        XmlDoc xmlDoc = new XmlDoc(AUTH_RESPONSE_XML_10);

        assertThat(xmlDoc.getFromVonrHome("EntitlementStatus").get()).isEqualTo("1");
        assertThat(xmlDoc.getFromVonrRoaming("EntitlementStatus").isPresent()).isFalse();
    }
}
