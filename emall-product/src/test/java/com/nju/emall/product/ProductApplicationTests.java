package com.nju.emall.product;

import com.nju.emall.product.entity.BrandEntity;
import com.nju.emall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("1111");
        brandService.save(brandEntity);

        brandEntity = brandService.getOne(null);
        brandEntity.setName("2222");
        brandService.update(brandEntity,null);
    }

}
