package com.abtnetworks.totems.whale.plugin.unix;

import com.abtnetworks.totems.whale.plugin.unix.entity.IptablesEntity;
import com.abtnetworks.totems.whale.common.utils.IOUtil;
import com.abtnetworks.totems.whale.common.utils.JacksonObjectMapper;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author zc
 * @date 2020/04/08
 */
public class IptablesPluginTest {
    private static JacksonObjectMapper MAPPER = JacksonObjectMapper.GLOBAL_DEFAULT_MAPPER;

    @Test
    public void getIptablesFilters() throws Exception {
        IptablesPlugin iptablesPlugin = new IptablesPlugin();
//        String fileName = "linux-test3.conf";
        String fileName = "win-test3.conf";
        URL path = IptablesPluginTest.class.getClassLoader().getResource(fileName);
        String pathStr = path.getPath();
        int a = pathStr.indexOf(":");
        pathStr = pathStr.substring(a +1);
        String content = IOUtil.getFileContent(pathStr, 0);
        long before = System.currentTimeMillis();
        IptablesEntity iptablesEntity = iptablesPlugin.getIptablesFilters(content, null);
        long after = System.currentTimeMillis();
        System.out.println("Finished parsing, time taken: " + (after-before));
        Writer writer = new OutputStreamWriter(new FileOutputStream(pathStr + ".json"), StandardCharsets.UTF_8);
        MAPPER.writeValue( writer, iptablesEntity);
    }

    @Test
    public void getText() throws Exception {
        String fileName = "linux-test3.conf";
//        String fileName = "win-test3.conf";
        URL path = IptablesPluginTest.class.getClassLoader().getResource(fileName);
        String pathStr = path.getPath();
        int a = pathStr.indexOf(":");
        pathStr = pathStr.substring(a +1);
        String content = IOUtil.getFileContent(pathStr, 0);
        content = content.replace("\\r\\n", "\n");
        content = content.replace("\\n", "\n");
        System.out.println(content);
    }
}