package com.nju.emall.search.service;

import com.nju.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @description
 * @date:2022/9/24 15:29
 * @author: qyl
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
