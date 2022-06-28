package com.abtnetworks.totems.whale.plugin.unix.host;

import com.abtnetworks.totems.whale.plugin.unix.entity.FilterInfoEntity;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesEntity;
import com.abtnetworks.totems.whale.plugin.unix.util.IptablesConstant;
import com.abtnetworks.totems.whale.common.domain.deviceInterface.DeviceInterfacePO;
import com.abtnetworks.totems.whale.common.domain.deviceInterface.IPAddressInfoPO;
import com.abtnetworks.totems.whale.common.domain.filter.FilterRuleListPO;
import com.abtnetworks.totems.whale.common.domain.filter.FilterRulePO;
import com.abtnetworks.totems.whale.common.domain.filter.MatchClausePO;
import com.abtnetworks.totems.whale.common.domain.filter.PortMatchSpecPO;
import com.abtnetworks.totems.whale.common.domain.filter.ProtocolMatchSpecPO;
import com.abtnetworks.totems.whale.common.domain.ip.IP4AddressPO;
import com.abtnetworks.totems.whale.common.domain.metadata.DeviceMetadata;
import com.abtnetworks.totems.whale.common.domain.obj.DevicePO;
import com.abtnetworks.totems.whale.common.domain.obj.GenericDevicePO;
import com.abtnetworks.totems.whale.common.domain.obj.IPItemPO;
import com.abtnetworks.totems.whale.common.enums.FilterRuleActionEnum;
import com.abtnetworks.totems.whale.common.enums.IPTypeEnum;
import com.abtnetworks.totems.whale.common.enums.PortOperatorEnum;
import com.abtnetworks.totems.whale.common.enums.SystemRuleListTypeEnum;
import com.abtnetworks.totems.whale.common.exceptions.WhalePluginRuntimeException;
import com.abtnetworks.totems.whale.common.plugin.VisitorHelper;
import com.abtnetworks.totems.whale.plugin.unix.IptablesPlugin;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesFilterEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author zc
 * @date 2020/10/14
 */
@Slf4j
public class HostVisitorOverall {

    protected static String IPTABLES_RAW_CONFIG_NAME = "iptables";
    private static final String ACL_NAME = "iptables filter";
    protected DeviceMetadata deviceMeta = null;
    protected GenericDevicePO deviceInfo = new GenericDevicePO();
    protected DevicePO device = new DevicePO();
    protected List<DeviceInterfacePO> interfaces = new ArrayList<>();
    protected LinkedHashMap<String, FilterRuleListPO> aclMap = new LinkedHashMap<>();
    
    public GenericDevicePO createDeviceData(String hostDeviceConfig) {
        this.device.setOsPlatform("Host");
        this.device.setName("Host");
        this.parseJson(hostDeviceConfig);
        if (!this.interfaces.isEmpty()) {
            this.device.setDeviceInterfaces(this.interfaces);
        }
        this.deviceInfo.setDevice(this.device);
        this.deviceInfo.setFilterLists(this.aclMap);
        this.deviceInfo.setRawConfig(hostDeviceConfig);
        return this.deviceInfo;
    }

    public void parseJson(String hostDeviceConfig) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(hostDeviceConfig);
            if (jsonNode.has("hostName")) {
                this.device.setName(jsonNode.get("hostName").asText());
            }
            if (jsonNode.has("uuid")) {
                this.device.setUuid(jsonNode.get("uuid").asText());
            }
            if (jsonNode.has("os")) {
                JsonNode os = jsonNode.get("os");
                if (os.has("version")) {
                    this.device.setOsVersion(os.get("version").asText());
                }
                if (os.has("name")) {
                    this.device.setOsPlatform(os.get("name").asText());
                }
            }
            if (jsonNode.has("interfaceList")) {
                for (JsonNode interfaceItem : jsonNode.get("interfaceList")) {
                    DeviceInterfacePO deviceInterface = new DeviceInterfacePO();
                    if (interfaceItem.has("ipType")) {
                        String ipType = interfaceItem.get("ipType").asText();
                        if (!ipType.equalsIgnoreCase("ipv4")) {
                            return;
                        }
                    }
                    if (interfaceItem.has("name")) {
                        deviceInterface.setName(interfaceItem.get("name").asText());
                    }
                    if (interfaceItem.has("description")) {
                        deviceInterface.setDescription(interfaceItem.get("description").asText());
                    }
                    if (interfaceItem.has("address") && interfaceItem.has("netmask")) {
                        String address = interfaceItem.get("address").asText();
                        String netmask = interfaceItem.get("netmask").asText();
                        IPAddressInfoPO ipi = new IPAddressInfoPO(IPTypeEnum.IP4);
                        IP4AddressPO ip4 = IP4AddressPO.create(address);
                        ipi.setIp4Address(ip4);
                        IP4AddressPO mask = IP4AddressPO.create(netmask);
                        ipi.setIp4Mask(mask);
                        deviceInterface.addIPAddress(ipi);
                    }
                    this.interfaces.add(deviceInterface);
                }
            }
            if (jsonNode.has("iptables")) {
                String iptables = jsonNode.get("iptables").asText();
                this.deviceInfo.addOtherRawConfig(IPTABLES_RAW_CONFIG_NAME, iptables);
                IptablesPlugin iptablesPlugin = new IptablesPlugin();
                iptables = iptables.replace("\\r\\n", "\n").replace("\\n", "\n");
                IptablesEntity iptablesEntity = iptablesPlugin.getIptablesFilters(iptables, null);
                iptablesHandle(iptablesEntity);
            }

        } catch (JsonProcessingException e) {
            throw new WhalePluginRuntimeException("Failed to parse json file.", e);
        }
    }

    private void iptablesHandle(IptablesEntity iptablesEntity) {
        List<IptablesFilterEntity> iptablesFilterEntities = iptablesEntity.getIptablesFilterEntities();
        if (iptablesFilterEntities != null) {
            for (IptablesFilterEntity iptablesFilterEntity : iptablesFilterEntities) {
                String filterName = iptablesFilterEntity.getFilterName();
                FilterRuleListPO filterRuleList = this.getFilterRuleList(filterName, FilterRuleListPO.FilterRuleListOption.IP4_ONLY);
                List<FilterInfoEntity> filterInfoEntities = iptablesFilterEntity.getFilterInfoEntities();
                if (filterInfoEntities != null) {
                    for (FilterInfoEntity filterInfoEntity : filterInfoEntities) {
                        FilterRulePO filterRule = filterInfoHandle(filterInfoEntity);
                        filterRuleList.addFilterRule(filterRule);
                    }
                }
            }
        }
    }

    private FilterRulePO filterInfoHandle(FilterInfoEntity filterInfoEntity) {
        FilterRulePO filterRule = new FilterRulePO();
        filterRule.setIpType(IPTypeEnum.IP4);
        if ("false".equals(filterInfoEntity.getEnable())) {
            filterRule.setInactive(true);
        }
        if (IptablesConstant.PERMIT.equals(filterInfoEntity.getAction())) {
            filterRule.setAction(FilterRuleActionEnum.PERMIT);
        } else {
            filterRule.setAction(FilterRuleActionEnum.DENY);
        }
        filterRule.setName(filterInfoEntity.getRuleName());
        filterRule.addInInterfaceGroupRef(filterInfoEntity.getInInterfaceName());
        filterRule.addOutInterfaceGroupRef(filterInfoEntity.getOutInterfaceName());

        MatchClausePO matchClause = new MatchClausePO();
        String srcIp = filterInfoEntity.getSrcIp();
        if (srcIp != null && !srcIp.equals(IptablesConstant.ANY)) {
            if (srcIp.contains("/")) {
                String[] srcIpArray = srcIp.split("/");
                matchClause.addSrcIpItem(IPItemPO.createSubnetType(IP4AddressPO.create(srcIpArray[0]), Integer.parseInt(srcIpArray[1])));
            } else {
                matchClause.addSrcIpItem(IPItemPO.createHostIpType(IP4AddressPO.create(srcIp)));
            }
        }
        String srcPort = filterInfoEntity.getSrcPort();
        if (srcPort != null && !srcPort.equals(IptablesConstant.ANY)) {
            if (srcPort.contains("-")) {
                String[] srcPortArray = srcPort.split("-");
                matchClause.addSrcPort(PortMatchSpecPO.createPortValueType(PortOperatorEnum.RANGE, srcPortArray[0], srcPortArray[1]));
            } else {
                matchClause.addSrcPort(PortMatchSpecPO.createPortValueType(PortOperatorEnum.EQ, srcPort));
            }
        }
        String dstIp = filterInfoEntity.getDstIp();
        if (dstIp != null && !dstIp.equals(IptablesConstant.ANY)) {
            if (dstIp.contains("/")) {
                String[] dstIpArray = dstIp.split("/");
                matchClause.addDstIpItem(IPItemPO.createSubnetType(IP4AddressPO.create(dstIpArray[0]), Integer.parseInt(dstIpArray[1])));
            } else {
                matchClause.addDstIpItem(IPItemPO.createHostIpType(IP4AddressPO.create(dstIp)));
            }
        }
        String dstPort = filterInfoEntity.getDstPort();
        if (dstPort != null && !dstPort.equals(IptablesConstant.ANY)) {
            if (dstPort.contains("-")) {
                String[] dstPortArray = dstPort.split("-");
                matchClause.addDstPort(PortMatchSpecPO.createPortValueType(PortOperatorEnum.RANGE, dstPortArray[0], dstPortArray[1]));
            } else {
                matchClause.addDstPort(PortMatchSpecPO.createPortValueType(PortOperatorEnum.EQ, dstPort));
            }
        }
        String protocol = filterInfoEntity.getProtocol();
        if (protocol != null && !protocol.equals(IptablesConstant.ANY)) {
            matchClause.addProtocol(ProtocolMatchSpecPO.createProtocolNumType(Integer.parseInt(protocol)));
        }
        filterRule.setMatchClause(matchClause);

        filterRule.setLineNumbers(filterInfoEntity.getLineNumbers());
        filterRule.setDescription(filterInfoEntity.getRemarks());
        return filterRule;
    }

    private FilterRuleListPO getFilterRuleList(String aclName, FilterRuleListPO.FilterRuleListOption ruleListOption) {
        if (aclName == null) {
            aclName = ACL_NAME;
        }
        FilterRuleListPO myobj = this.aclMap.get(aclName);
        if (myobj == null) {
            myobj = this.createFilterRuleList(aclName, SystemRuleListTypeEnum.SYSTEM__POLICY_1.name(), null, ruleListOption);
            this.aclMap.put(aclName, myobj);
        }
        return myobj;
    }

    protected FilterRuleListPO createFilterRuleList(String aclName, String ruleListType, ParserRuleContext ctx, FilterRuleListPO.FilterRuleListOption ruleListOption) {
        FilterRuleListPO result = new FilterRuleListPO();
        result.setName(aclName);
        if (ctx != null) {
            result.setLineNumbers(VisitorHelper.getConfigLine(ctx));
        }

        result.setOption(ruleListOption);
        result.setRuleListType(ruleListType);
        return result;
    }

}
