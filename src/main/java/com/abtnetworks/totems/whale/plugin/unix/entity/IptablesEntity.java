package com.abtnetworks.totems.whale.plugin.unix.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zc
 * @date 2020/04/08
 */
@Data
public class IptablesEntity {

    /**
     * iptables filter list
     */
    private List<IptablesFilterEntity> iptablesFilterEntities;

    /**
     * 原始信息
     */
    private String originalInfo;

    /**
     * 未知的命令
     */
    private List<String> unknownCommand;
}
