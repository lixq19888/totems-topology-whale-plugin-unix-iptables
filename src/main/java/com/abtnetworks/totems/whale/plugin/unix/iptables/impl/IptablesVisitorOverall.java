package com.abtnetworks.totems.whale.plugin.unix.iptables.impl;

import com.abtnetworks.totems.whale.plugin.unix.entity.FilterInfoEntity;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesEntity;
import com.abtnetworks.totems.whale.plugin.unix.entity.PortEnum;
import com.abtnetworks.totems.whale.plugin.unix.util.IptablesConstant;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesFilterEntity;
import com.abtnetworks.totems.whale.plugin.unix.iptables.IptablesParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author zc
 * @date 2020/04/08
 */
@Slf4j
public class IptablesVisitorOverall extends IptablesVisitorBase {

    private HashMap<String, String> portNameIntMap = new HashMap<>();

    public IptablesVisitorOverall() {
        for (PortEnum port : PortEnum.values()) {
            portNameIntMap.put(port.getPortName(), port.getPortInt());
        }
    }

    public IptablesEntity createIptablesData(IptablesParser parser, String configText) {
        this.configText = configText;
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        IptablesParser.ConfigContext configContext = parser.config();

        this.checkIsValidConfigFile(configContext);

        this.visitConfig(configContext);

        this.iptablesEntity.setOriginalInfo(configText);
        this.iptablesEntity.setUnknownCommand(unknownCommandList);
        if (!iptablesFilterEntityMap.isEmpty()) {
            this.iptablesEntity.setIptablesFilterEntities(new ArrayList<>(iptablesFilterEntityMap.values()));
            this.dataStandardization();
        }
        return this.iptablesEntity;
    }

    private void checkIsValidConfigFile(IptablesParser.ConfigContext configContext) {
        long count = configContext.command().stream()
                .filter(commandContext -> commandContext.knownCommand() != null)
                .count();
        if (count < 1) {
            throw new RuntimeException("Failed to parse config text. Is this a iptables config?");
        }
    }

    private void dataStandardization() {
        List<IptablesFilterEntity> iptablesFilterEntities = this.iptablesEntity.getIptablesFilterEntities();
        for (IptablesFilterEntity iptablesFilterEntity : iptablesFilterEntities) {
            List<FilterInfoEntity> filterInfoEntities = iptablesFilterEntity.getFilterInfoEntities();
            if (filterInfoEntities != null) {
                Iterator<FilterInfoEntity> filterInfoEntityIterator = filterInfoEntities.iterator();
                while (filterInfoEntityIterator.hasNext()) {
                    FilterInfoEntity filterInfoEntity = filterInfoEntityIterator.next();

                    String inInterfaceName = filterInfoEntity.getInInterfaceName();
                    if (inInterfaceName != null && inInterfaceName.equals("*")) {
                        filterInfoEntity.setInInterfaceName(null);
                    }
                    String outInterfaceName = filterInfoEntity.getOutInterfaceName();
                    if (outInterfaceName != null && outInterfaceName.equals("*")) {
                        filterInfoEntity.setOutInterfaceName(null);
                    }

                    String action = filterInfoEntity.getAction();
                    if (action == null || StringUtils.equalsAny(action,"ACCEPT", "LOG", "允许")) {
                        filterInfoEntity.setAction(IptablesConstant.PERMIT);
                    } else if (StringUtils.equalsAny(action,"DROP", "REJECT", "拒绝", "阻止")) {
                        filterInfoEntity.setAction(IptablesConstant.DENY);
                    } else {
                        log.error("未知的动作：{}，抛弃", action);
                        filterInfoEntityIterator.remove();
                        continue;
                    }

                    String srcIp = filterInfoEntity.getSrcIp();
                    if (srcIp == null || StringUtils.equalsAny(srcIp,"任何", "0.0.0.0/0")) {
                        filterInfoEntity.setSrcIp(IptablesConstant.ANY);
                    }

                    String dstIp = filterInfoEntity.getDstIp();
                    if (dstIp == null || StringUtils.equalsAny(dstIp,"任何", "0.0.0.0/0")) {
                        filterInfoEntity.setDstIp(IptablesConstant.ANY);
                    }

                    String srcPort = filterInfoEntity.getSrcPort();
                    if (srcPort == null || StringUtils.equalsAny(srcPort,"任何")) {
                        filterInfoEntity.setSrcPort(IptablesConstant.ANY);
                    } else if (srcPort.contains(":")) {
                        filterInfoEntity.setSrcPort(srcPort.replace(":", "-"));
                    } else if (portNameIntMap.containsKey(srcPort.toLowerCase())) {
                        filterInfoEntity.setSrcPort(portNameIntMap.get(srcPort.toLowerCase()));
                    }

                    String dstPort = filterInfoEntity.getDstPort();
                    if (dstPort == null || StringUtils.equalsAny(dstPort,"任何")) {
                        filterInfoEntity.setDstPort(IptablesConstant.ANY);
                    } else if (dstPort.contains(":")) {
                        filterInfoEntity.setDstPort(dstPort.replace(":", "-"));
                    } else if (portNameIntMap.containsKey(dstPort.toLowerCase())) {
                        filterInfoEntity.setDstPort(portNameIntMap.get(dstPort.toLowerCase()));
                    }

                    String protocol = filterInfoEntity.getProtocol();
                    if (protocol == null || StringUtils.equalsAny(protocol,"all","任何")) {
                        filterInfoEntity.setProtocol(IptablesConstant.ANY);
                    } else if ("tcp".equalsIgnoreCase(protocol)) {
                        filterInfoEntity.setProtocol("6");
                    } else if ("udp".equalsIgnoreCase(protocol)) {
                        filterInfoEntity.setProtocol("17");
                    } else if ("icmp".equalsIgnoreCase(protocol)) {
                        filterInfoEntity.setProtocol("1");
                    } else {
                        log.error("未知的协议：{}，抛弃", protocol);
                        filterInfoEntityIterator.remove();
                        continue;
                    }

                    String enable = filterInfoEntity.getEnable();
                    if (enable == null || StringUtils.equalsAny(enable,"是")) {
                        filterInfoEntity.setEnable(Boolean.toString(true));
                    } else {
                        filterInfoEntity.setEnable(Boolean.toString(false));
                    }
                }
            }

        }
    }
}
