package com.abtnetworks.totems.whale.plugin.unix.iptables.impl;

import com.abtnetworks.totems.whale.plugin.unix.entity.FilterInfoEntity;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesEntity;
import com.abtnetworks.totems.whale.common.plugin.VisitorHelper;
import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesFilterEntity;
import com.abtnetworks.totems.whale.plugin.unix.iptables.IptablesBaseVisitor;
import com.abtnetworks.totems.whale.plugin.unix.iptables.IptablesParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zc
 * @date 2020/04/08
 */
@Slf4j
public class IptablesVisitorBase extends IptablesBaseVisitor<Void> {

    /**
     * windows 默认的策略集的名字
     */
    private static final String WINDOWS_DEFAULT_FILTER_NAME = "WINDOWS";

    ParseTreeProperty<Object> parseTreeProperty = new ParseTreeProperty<>();
    protected String configText;
    protected IptablesEntity iptablesEntity = new IptablesEntity();
    protected List<String> unknownCommandList = new ArrayList<>();
    protected Map<String, IptablesFilterEntity> iptablesFilterEntityMap = new HashMap<>();

    private IptablesFilterEntity getIptablesFilter(String filterName) {
        return iptablesFilterEntityMap.computeIfAbsent(filterName, k -> new IptablesFilterEntity());
    }

    private List<FilterInfoEntity> getFilterInfoList(IptablesFilterEntity iptablesFilterEntity) {
        List<FilterInfoEntity> filterInfoEntities = iptablesFilterEntity.getFilterInfoEntities();
        if (filterInfoEntities == null) {
            filterInfoEntities = new ArrayList<>();
            iptablesFilterEntity.setFilterInfoEntities(filterInfoEntities);
        }
        return filterInfoEntities;
    }

    @Override
    public Void visitLinuxIptables(IptablesParser.LinuxIptablesContext ctx) {
        String filterName = ctx.chainName.getText();

        IptablesFilterEntity iptablesFilterEntity = getIptablesFilter(filterName);
        List<FilterInfoEntity> filterInfoEntities = getFilterInfoList(iptablesFilterEntity);

        iptablesFilterEntity.setFilterName(filterName);
        String originalText = getCtxText(ctx);
        iptablesFilterEntity.setOriginalInfo(originalText);


        parseTreeProperty.put(ctx, iptablesFilterEntity);
        super.visitLinuxIptables(ctx);
        parseTreeProperty.removeFrom(ctx);

        String defaultAction = ctx.action.getText();
        if (defaultAction.endsWith(")")) {
            defaultAction = defaultAction.substring(0, defaultAction.length()-1);
        }
        FilterInfoEntity defaultFilterInfo = new FilterInfoEntity();
        defaultFilterInfo.setAction(defaultAction);
        defaultFilterInfo.setRemarks("默认策略");
        if (originalText.contains("\n")) {
            int eol = originalText.indexOf("\n");
            defaultFilterInfo.setOriginalInfo(originalText.substring(0, eol));
        }
        filterInfoEntities.add(defaultFilterInfo);
        return null;
    }

    @Override
    public Void visitLinuxIptablesTableInfo(IptablesParser.LinuxIptablesTableInfoContext ctx) {
        IptablesFilterEntity iptablesFilterEntity = (IptablesFilterEntity) parseTreeProperty.get(ctx.parent);
        FilterInfoEntity filterInfo = new FilterInfoEntity();
        filterInfo.setLineNumbers(VisitorHelper.getConfigLine(ctx));
        iptablesFilterEntity.getFilterInfoEntities().add(filterInfo);

        filterInfo.setAction(ctx.target.getText());
        filterInfo.setProtocol(ctx.prot.getText());
        filterInfo.setRemarks(ctx.opt.getText());
        if (ctx.in != null) {
            filterInfo.setInInterfaceName(ctx.in.getText());
        }
        if (ctx.out != null) {
            filterInfo.setOutInterfaceName(ctx.out.getText());
        }
        filterInfo.setSrcIp(ctx.source.getText());
        filterInfo.setDstIp(ctx.dstination.getText());

        if (ctx.misc != null) {
            for (IptablesParser.WordContext wordContext : ctx.misc) {
                String word = wordContext.getText();
                if (word.contains("dpt:") || word.contains("dpts:")) {
                    int index = word.indexOf(":");
                    filterInfo.setDstPort(word.substring(index + 1));
                }
                if (word.contains("spt:") || word.contains("spts:")) {
                    int index = word.indexOf(":");
                    filterInfo.setSrcPort(word.substring(index + 1));
                }
            }
        }

        filterInfo.setOriginalInfo(getCtxText(ctx));

        return null;
    }


    @Override
    public Void visitWindowsIptables(IptablesParser.WindowsIptablesContext ctx) {
        IptablesFilterEntity iptablesFilterEntity = getIptablesFilter(WINDOWS_DEFAULT_FILTER_NAME);
        if (iptablesFilterEntity.getFilterName() == null) {
            iptablesFilterEntity.setFilterName(WINDOWS_DEFAULT_FILTER_NAME);
        }
        List<FilterInfoEntity> filterInfoEntities = getFilterInfoList(iptablesFilterEntity);

        FilterInfoEntity filterInfoEntity = new FilterInfoEntity();

        boolean programNeed = true;
        boolean directionNeed = false;

        filterInfoEntity.setRuleName(ctx.name.getText());


        String direction = "";
        List<IptablesParser.WindowsIptablesSubContext> windowsIptablesSubContexts = ctx.windowsIptablesSub();
        for (IptablesParser.WindowsIptablesSubContext subContext : windowsIptablesSubContexts) {
            if (subContext.enable != null) {
                filterInfoEntity.setEnable(subContext.enable.getText());
            } else if (subContext.desc != null) {
                filterInfoEntity.setRemarks(subContext.desc.getText());
            } else if (subContext.action != null) {
                filterInfoEntity.setAction(subContext.action.getText());
            } else if (subContext.direction != null) {
                direction = subContext.direction.getText();
                if ("入".equals(direction)) {
                    directionNeed = true;
                }
            } else if (subContext.interfaceName != null) {
                if (directionNeed) {
                    filterInfoEntity.setInInterfaceName(subContext.interfaceName.getText());
                }
            } else if (subContext.srcIp != null) {
                filterInfoEntity.setSrcIp(subContext.srcIp.getText());
            } else if (subContext.dstIp != null) {
                filterInfoEntity.setDstIp(subContext.dstIp.getText());
            } else if (subContext.srcPort != null) {
                filterInfoEntity.setSrcPort(subContext.srcPort.getText());
            } else if (subContext.dstPort != null) {
                filterInfoEntity.setDstPort(subContext.dstPort.getText());
            } else if (subContext.protocol != null) {
                filterInfoEntity.setProtocol(subContext.protocol.getText());
            } else if (subContext.program != null) {
                if (!"任何".equals(subContext.program.getText())) {
                    log.debug("程序不是任何的丢弃");
                    programNeed = false;
                }
            }
        }

        if (programNeed && directionNeed) {
            filterInfoEntity.setOriginalInfo(getCtxText(ctx));
            filterInfoEntities.add(filterInfoEntity);
        }
        return null;
    }

    @Override
    public Void visitUnknownCommand(IptablesParser.UnknownCommandContext ctx) {
        String unknownCommand = getCtxText(ctx);
        if (StringUtils.isNotBlank(unknownCommand)) {
            this.unknownCommandList.add(unknownCommand);
        }
        return null;
    }

    protected String getCtxText(ParserRuleContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        return this.configText.substring(startIndex,stopIndex);
    }
}
