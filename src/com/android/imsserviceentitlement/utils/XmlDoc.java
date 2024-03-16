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

import static com.android.imsserviceentitlement.ts43.Ts43Constants.AccessTypeValue.LTE;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.AccessTypeValue.NGRAN;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.HomeRoamingNwTypeValue.ALL;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.HomeRoamingNwTypeValue.HOME;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.HomeRoamingNwTypeValue.ROAMING;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.ACCESS_TYPE;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.APP_ID;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.HOME_ROAMING_NW_TYPE;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlAttributes.RAT_VOICE_ENTITLE_INFO_DETAILS;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlNode.APPLICATION;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlNode.TOKEN;
import static com.android.imsserviceentitlement.ts43.Ts43Constants.ResponseXmlNode.VERS;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.android.imsserviceentitlement.debug.DebugUtils;
import com.android.libraries.entitlement.ServiceEntitlement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** Wrap the raw content and parse it into nodes. */
public class XmlDoc {
    private static final String TAG = "IMSSE-XmlDoc";

    private static final String NODE_CHARACTERISTIC = "characteristic";
    private static final String NODE_PARM = "parm";
    private static final String PARM_NAME = "name";
    private static final String PARM_VALUE = "value";

    private final Map<String, Map<String, String>> mNodesMap = new ArrayMap<>();

    public XmlDoc(String responseBody) {
        parseXmlResponse(responseBody);
    }

    public Optional<String> getFromToken(String key) {
        Map<String, String> paramsMap = mNodesMap.get(TOKEN);
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromVersion(String key) {
        Map<String, String> paramsMap = mNodesMap.get(VERS);
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromVowifi(String key) {
        Map<String, String> paramsMap = mNodesMap.get(ServiceEntitlement.APP_VOWIFI);
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromVolte(String key) {
        Map<String, String> paramsMap = mNodesMap.get(ServiceEntitlement.APP_VOLTE);
        paramsMap = paramsMap == null ? mNodesMap.get(LTE + ALL) : paramsMap;
        paramsMap = paramsMap == null ? mNodesMap.get(LTE + HOME) : paramsMap;
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromSmsoverip(String key) {
        Map<String, String> paramsMap = mNodesMap.get(ServiceEntitlement.APP_SMSOIP);
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromVonrHome(String key) {
        Map<String, String> paramsMap = mNodesMap.get(NGRAN + ALL);
        paramsMap = paramsMap == null ? mNodesMap.get(NGRAN + HOME) : paramsMap;
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    public Optional<String> getFromVonrRoaming(String key) {
        Map<String, String> paramsMap = mNodesMap.get(NGRAN + ALL);
        paramsMap = paramsMap == null ? mNodesMap.get(NGRAN + ROAMING) : paramsMap;
        return Optional.ofNullable(paramsMap == null ? null : paramsMap.get(key));
    }

    /**
     * Parses the response body as per format defined in TS.43 2.7.2 New Characteristics for
     * XML-Based Document.
     */
    private void parseXmlResponse(String responseBody) {
        if (responseBody == null) {
            return;
        }

        if (DebugUtils.isPiiLoggable()) {
            Log.d(TAG, "Raw Response Body: " + responseBody);
        }
        // Workaround: some server doesn't escape "&" in XML response and that will cause XML parser
        // failure later.
        // This is a quick impl of escaping w/o intorducing a ton of new dependencies.
        responseBody = responseBody.replace("&", "&amp;").replace("&amp;amp;", "&amp;");

        try {
            InputSource inputSource = new InputSource(new StringReader(responseBody));
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputSource);
            doc.getDocumentElement().normalize();

            if (DebugUtils.isPiiLoggable()) {
                Log.d(
                        TAG,
                        "parseXmlResponseForNode() Root element: "
                                + doc.getDocumentElement().getNodeName());
            }

            NodeList nodeList = doc.getElementsByTagName(NODE_CHARACTERISTIC);
            for (int i = 0; i < nodeList.getLength(); i++) {
                NamedNodeMap map = nodeList.item(i).getAttributes();
                if (DebugUtils.isPiiLoggable()) {
                    Log.d(
                            TAG,
                            "parseAuthenticateResponse() node name="
                                    + nodeList.item(i).getNodeName()
                                    + " node value="
                                    + map.item(0).getNodeValue());
                }
                Element element = (Element) nodeList.item(i);
                if (element.getElementsByTagName(NODE_CHARACTERISTIC).getLength() != 0) {
                    continue;
                }

                Map<String, String> paramsMap = new ArrayMap<>();
                String characteristicType = map.item(0).getNodeValue();
                String key;
                paramsMap.putAll(parseParams(element.getElementsByTagName(NODE_PARM)));
                if (APPLICATION.equals(characteristicType)) {
                    key = paramsMap.get(APP_ID);
                } else if (RAT_VOICE_ENTITLE_INFO_DETAILS.equals(characteristicType)) {
                    key = paramsMap.get(ACCESS_TYPE) + paramsMap.get(HOME_ROAMING_NW_TYPE);
                } else { // VERS or TOKEN
                    key = characteristicType;
                }
                mNodesMap.put(key, paramsMap);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Log.e(TAG, "Failed to parse XML node. " + e);
        }
    }

    private static Map<String, String> parseParams(NodeList nodeList) {
        Map<String, String> nameValue = new ArrayMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            NamedNodeMap map = node.getAttributes();
            String name = "";
            String value = "";
            for (int j = 0; j < map.getLength(); j++) {
                if (PARM_NAME.equals(map.item(j).getNodeName())) {
                    name = map.item(j).getNodeValue();
                } else if (PARM_VALUE.equals(map.item(j).getNodeName())) {
                    value = map.item(j).getNodeValue();
                }
            }
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
                continue;
            }
            nameValue.put(name, value);

            if (DebugUtils.isPiiLoggable()) {
                Log.d(TAG, "parseParams() put name '" + name + "' with value " + value);
            }
        }
        return nameValue;
    }
}
