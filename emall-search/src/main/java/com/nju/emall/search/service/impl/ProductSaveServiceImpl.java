package com.nju.emall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.nju.common.to.es.SkuEsModel;
import com.nju.emall.search.config.EmallElasticSearchConfig;
import com.nju.emall.search.constant.EsConstant;
import com.nju.emall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description
 * @date:2022/9/24 15:31
 * @author: qyl
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest(EsConstant.PRODUCT_INDEX);
        for (SkuEsModel skuEsModel : skuEsModels) {
            bulkRequest.add(new IndexRequest()
                    .id(skuEsModel.getSkuId().toString())
                    .source(JSON.toJSONString(skuEsModel), XContentType.JSON));

        }
        BulkResponse responses = client.bulk(bulkRequest, EmallElasticSearchConfig.COMMON_OPTIONS);
        boolean b = responses.hasFailures();
        List<String> collect = Arrays.stream(responses.getItems())
                .map(BulkItemResponse::getId)
                .collect(Collectors.toList());
        log.info("商品上架完成，{}", collect);
        return !b;
    }
}
