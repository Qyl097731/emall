package com.nju.emall.product.feign;

import com.nju.common.to.es.SkuEsModel;
import com.nju.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
 * @description
 * @date:2022/9/24 15:52
 * @author: qyl
 */
@FeignClient("emall-search")
public interface SearchFeignService {
    @PostMapping("search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
