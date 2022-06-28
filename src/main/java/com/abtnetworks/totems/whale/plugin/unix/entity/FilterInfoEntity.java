package com.abtnetworks.totems.whale.plugin.unix.entity;

import com.abtnetworks.totems.whale.common.domain.ConfigLinePO;
import lombok.Data;

/**
 * @author zc
 * @date 2020/04/08
 */
@Data
public class FilterInfoEntity {

    /**
     * 是否启用
     */
    private String enable;

    /**
     * 规则名字
     */
    private String ruleName;

    /**
     * 进接口名
     */
    private String inInterfaceName;

    /**
     * 出接口名
     */
    private String outInterfaceName;

    /**
     * 源IP
     */
    private String srcIp;

    /**
     * 源端口
     */
    private String srcPort;

    /**
     * 目的IP
     */
    private String dstIp;

    /**
     * 目的端口
     */
    private String dstPort;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 动作
     */
    private String action;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 原始信息
     */
    private String originalInfo;

    /**
     * 行号
     */
    private ConfigLinePO lineNumbers;
}
