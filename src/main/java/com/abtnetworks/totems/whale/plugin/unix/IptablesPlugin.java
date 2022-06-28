package com.abtnetworks.totems.whale.plugin.unix;

import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesEntity;
import com.abtnetworks.totems.whale.plugin.unix.iptables.IptablesLexer;
import com.abtnetworks.totems.whale.plugin.unix.iptables.IptablesParser;
import com.abtnetworks.totems.whale.plugin.unix.iptables.impl.IptablesVisitorOverall;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Map;

/**
 * @author zc
 * @date 2020/04/08
 */
@Slf4j
public class IptablesPlugin {

    public IptablesEntity getIptablesFilters(String configText, Map<String, Object> options) {
        IptablesEntity iptablesEntity;
        log.info("iptables开始解析");
        log.debug("配置文件:{}", configText);
        try {
            configText += "\n";
            CodePointCharStream input = CharStreams.fromString(configText);
            IptablesLexer lexer = new IptablesLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            IptablesParser parser = new IptablesParser(tokens);
            IptablesVisitorOverall visitor = new IptablesVisitorOverall();
            iptablesEntity = visitor.createIptablesData(parser, configText);
            log.debug("解析结果为：{}", JSON.toJSONString(iptablesEntity, true));
        } catch (Exception e) {
            log.error("getIptablesFilters异常：", e);
            iptablesEntity = null;
        }
        log.info("iptables结束解析");
        return iptablesEntity;
    }

}
