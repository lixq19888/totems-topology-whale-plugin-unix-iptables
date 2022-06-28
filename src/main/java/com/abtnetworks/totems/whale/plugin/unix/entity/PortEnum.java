package com.abtnetworks.totems.whale.plugin.unix.entity;

/**
 * @author zc
 * @date 2020/05/29
 */
public enum PortEnum {

    RPC("rpc", "135"),
    RPC_EPMAP("rpc-epmap", "593");

    String portName;
    String portInt;

    PortEnum(String portName, String portInt) {
        this.portName = portName;
        this.portInt = portInt;
    }

    public String getPortName() {
        return portName;
    }

    public String getPortInt() {
        return portInt;
    }
}
