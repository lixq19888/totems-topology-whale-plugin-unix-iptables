package com.abtnetworks.totems.whale.plugin.unix.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zc
 * @date 2020/04/08
 */
@Data
public class IptablesFilterEntity {

    /**
     * filter集名字
     */
    private String filterName;

    /**
     * filter集具体信息
     */
    private List<FilterInfoEntity> filterInfoEntities;

    /**
     * 原始信息
     */
    private String originalInfo;

}
