package com.abtnetworks.totems.whale.plugin.unix;

import com.abtnetworks.totems.whale.plugin.unix.host.HostVisitorOverall;
import com.abtnetworks.totems.whale.common.domain.metadata.DeviceMetadata;
import com.abtnetworks.totems.whale.common.domain.obj.DevicePO;
import com.abtnetworks.totems.whale.common.domain.obj.GenericDevicePO;
import com.abtnetworks.totems.whale.common.enums.DeviceTypeEnum;
import com.abtnetworks.totems.whale.common.enums.SystemRuleListTypeEnum;
import com.abtnetworks.totems.whale.common.exceptions.WhalePluginRuntimeException;
import com.abtnetworks.totems.whale.common.exceptions.WhaleValidationException;
import com.abtnetworks.totems.whale.common.plugin.MultiDevicePlugin;
import com.abtnetworks.totems.whale.common.plugin.PluginMetadata;
import com.abtnetworks.totems.whale.common.plugin.PluginType;
import com.abtnetworks.totems.whale.common.utils.Assertion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zc
 * @date 2020/10/13
 */
public class HostDevicePlugin implements MultiDevicePlugin {

    private static final DeviceMetadata deviceMetadata = null;
    public static String DEVICE_METADATA_NAME = "linux.host";

    public PluginMetadata getPluginMetadata() {
        return new PluginMetadata(PluginType.DEVICE, "linux-host", "Linux Host", "Linux Host plugin");
    }

    private List<String> getRuleListOrder() {
        List<String> processingOrder = new ArrayList<>();
        processingOrder.add(SystemRuleListTypeEnum.SYSTEM__POLICY_1.name());
        return processingOrder;
    }

    public List<DeviceMetadata> getDeviceMetadata() {
        return Collections.singletonList(deviceMetadata);
    }

    public Set<String> getSupportedDeviceFamily() {
        return Collections.singleton("LINUX_HOST");
    }

    public GenericDevicePO getDevice(String configText, Map<String, Object> options) throws Exception {
        List<GenericDevicePO> mylist = this.getDevices(configText, null);
        if (mylist == null) {
            return null;
        }
        if (mylist.size() != 1) {
            throw new WhalePluginRuntimeException("getDevice() API doesn't support more than one host. Please use getDevices() ");
        }
        return mylist.get(0);
    }

    public List<GenericDevicePO> getDevices(String configText, Map<String, Object> options) throws Exception {
        List<JsonNode> hostNodes = this.splitHosts(configText);
        List<GenericDevicePO> result = new ArrayList<>();
        for (JsonNode hostNode : hostNodes) {
            HostVisitorOverall overall = new HostVisitorOverall();
            GenericDevicePO myResult = overall.createDeviceData(hostNode.toString());
            DevicePO device = myResult.getDevice();
            device.setDeviceMetadataName(DEVICE_METADATA_NAME);
            device.setManufacturer("Linux");
            device.setDeviceType(DeviceTypeEnum.HOST);
            device.setDeviceFamily("LINUX_HOST");
            result.add(myResult);
        }
        return result;
    }

    private List<JsonNode> splitHosts(String configText) {
        List<JsonNode> result = new ArrayList<>();
        JsonNode root;
        try {
            ObjectMapper mapper = new ObjectMapper();
            root = mapper.readTree(configText);
            this.validate(root);
        } catch (Throwable ex) {
            throw new WhalePluginRuntimeException("Failed to parse json file.", ex);
        }
        ArrayNode deviceNodes = (ArrayNode) root.get("hosts");
        for (JsonNode deviceNode : deviceNodes) {
            result.add(deviceNode);
        }
        return result;
    }

    public boolean canProcess(String configText) {
        Pattern regex = Pattern.compile("^\\s*\\{\\s*\"hosts\"\\s*:");
        Matcher m = regex.matcher(configText);
        return m.find();
    }

    private void validate(JsonNode root) {
        ArrayNode hostNodes = (ArrayNode) root.get("hosts");
        if (hostNodes == null) {
            throw new WhaleValidationException("Invalid json. 'hosts' is required in root level.");
        }
        int index = 0;
        for (JsonNode deviceNode : hostNodes) {
            ++index;
            Assertion.notNull(deviceNode.get("hostName"), "Invalid json. 'hostName' is required at host [" + index + "]");
            String hostName = deviceNode.get("hostName").asText();
            Assertion.notNull(deviceNode.get("primaryId"), "Invalid json. 'primaryId' is required at host: " + hostName);
            String primaryId = deviceNode.get("primaryId").asText();
            Assertion.notEmpty(primaryId, "Invalid json. 'primaryId' is required at host: " + hostName);
            Assertion.notNull(deviceNode.get("interfaceList"), "Invalid json. 'interfaceList' is required at host: " + hostName);
        }
    }

}
