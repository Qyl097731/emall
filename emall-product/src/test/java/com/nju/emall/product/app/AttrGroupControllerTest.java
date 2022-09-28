package com.nju.emall.product.app;

import com.nju.emall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @description
 * @date:2022/9/20 8:02
 * @author: qyl
 */
@Slf4j
@SpringBootTest
public class AttrGroupControllerTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    public void info() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("路径为:{}", Arrays.asList(catelogPath));
    }
}
