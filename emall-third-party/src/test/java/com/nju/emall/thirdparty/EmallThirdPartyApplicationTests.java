package com.nju.emall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class EmallThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;
    @Test
    void contextLoads() {
    }
    @Test
    public void testUpload() throws FileNotFoundException {
        InputStream in = new FileInputStream("C:\\Users\\asus\\Desktop\\QQ图片20220909151535.png");
        ossClient.putObject("njxzc-edu","20220909151535.png",in);
        ossClient.shutdown();
        System.out.println("上传成功");
    }

}
