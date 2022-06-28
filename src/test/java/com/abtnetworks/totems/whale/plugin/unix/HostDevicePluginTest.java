package com.abtnetworks.totems.whale.plugin.unix;

import com.abtnetworks.totems.whale.common.domain.obj.GenericDevicePO;
import com.abtnetworks.totems.whale.common.utils.IOUtil;
import com.abtnetworks.totems.whale.common.utils.JacksonObjectMapper;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zc
 * @date 2020/10/14
 */
public class HostDevicePluginTest {
    private static JacksonObjectMapper MAPPER = JacksonObjectMapper.GLOBAL_DEFAULT_MAPPER;

    @Test
    public void getDevices() throws Exception {
        HostDevicePlugin hostDevicePlugin = new HostDevicePlugin();
        String fileName = "linux-test2.json";
//        String fileName = "win-test3.conf";
        URL path = IptablesPluginTest.class.getClassLoader().getResource(fileName);
        String pathStr = path.getPath();
        int a = pathStr.indexOf(":");
        pathStr = pathStr.substring(a +1);
        String content = IOUtil.getFileContent(pathStr, 0);
        long before = System.currentTimeMillis();
        List<GenericDevicePO> genericDevicePOS = hostDevicePlugin.getDevices(content, null);
        long after = System.currentTimeMillis();
        System.out.println("Finished parsing, time taken: " + (after-before));
        Writer writer = new OutputStreamWriter(new FileOutputStream(pathStr + ".json"), StandardCharsets.UTF_8);
        MAPPER.writeValue( writer, genericDevicePOS);
    }
}