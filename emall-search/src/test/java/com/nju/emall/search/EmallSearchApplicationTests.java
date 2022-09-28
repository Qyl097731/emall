package com.nju.emall.search;

import com.alibaba.fastjson.JSON;
import com.nju.emall.search.config.EmallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EmallSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Data
    static class UserVo {
        private int age;
        private String gender;
        private String name;
    }

    @Test
    void contextLoads() {
    }

    /**
     * @Description 检索
     * @Date  2022/9/23
     * @Author Mr.Qiu
    **/
    @Test
    public void searchData() throws IOException {
        SearchRequest searchResult = new SearchRequest().indices("users");
        SearchSourceBuilder query = new SearchSourceBuilder()
                // 模糊匹配name 是张的数据
                .query(QueryBuilders.matchQuery("name","张"))
                // 求出年龄分布
                .aggregation(AggregationBuilders.terms("genAgg").field("gender.keyword"))
                .aggregation(AggregationBuilders.avg("ageAvg").field("age"));


        searchResult.source(query);

        System.out.println(query.toString());

        SearchResponse response = client.search(searchResult, EmallElasticSearchConfig.COMMON_OPTIONS);
        response.getHits().forEach(hit-> System.out.println(JSON.parseObject(hit.getSourceAsString(),UserVo.class)));

        Aggregations aggregations = response.getAggregations();
        Terms genAgg = aggregations.get("genAgg");
        for (Terms.Bucket bucket:genAgg.getBuckets()){
            System.out.println(bucket.getKeyAsString());
        }

        Avg ageAvg = aggregations.get("ageAvg");
        System.out.println(ageAvg.getValue());


    }

    /**
     * @Description 存储 / 更新
     * @Date  2022/9/23
     * @Author Mr.Qiu
     **/
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User("张三","女",11);
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse response = client.index(indexRequest, EmallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response.getResult());
    }


    @Data
    @AllArgsConstructor
    class User{
        private String name;
        private String gender;
        private Integer age;
    }

}
