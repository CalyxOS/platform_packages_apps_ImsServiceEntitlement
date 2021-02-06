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

    private static final String TOKEN = "kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX";

    @Test
    public void parseAuthenticateResponse() {
        assertThat(new XmlDoc(AUTH_RESPONSE_XML).get("TOKEN", "token")).isEqualTo(TOKEN);
        // Note "&amp;" in input XML are un-escaped to "&".
        assertThat(new XmlDoc(AUTH_RESPONSE_XML).get("APPLICATION", "ServiceFlow_UserData"))
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
        assertThat(new XmlDoc(AUTH_RESPONSE_XML_2).get("TOKEN", "token")).isEqualTo(TOKEN);
        // Note the "&" in input XML is kept as is
        assertThat(new XmlDoc(AUTH_RESPONSE_XML_2).get("APPLICATION", "ServiceFlow_UserData"))
                .isEqualTo("PostData=U6%2FbQ%2BEP&req_locale=en_US");
    }

    @Test
    public void parseAuthenticateResponse3() {
        // Note the "&amp;amp;" in input XML is un-escaped to "&amp;"
        assertThat(new XmlDoc(AUTH_RESPONSE_XML_3).get("APPLICATION", "ServiceFlow_UserData"))
                .isEqualTo("PostData=U6%2FbQ%2BEP&amp;l=en_US");
    }
}
