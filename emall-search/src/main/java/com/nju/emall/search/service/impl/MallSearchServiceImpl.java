package com.nju.emall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nju.common.to.es.SkuEsModel;
import com.nju.common.utils.R;
import com.nju.emall.search.config.EmallElasticSearchConfig;
import com.nju.emall.search.constant.EsConstant;
import com.nju.emall.search.feign.ProductFeignService;
import com.nju.emall.search.service.MallSearchService;
import com.nju.emall.search.vo.AttrResponseVo;
import com.nju.emall.search.vo.SearchParam;
import com.nju.emall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.bouncycastle.util.Arrays;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.job.results.Bucket;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    //
//    @Resource
//    private ProductFeignService productFeignService;
//
    @Override
    public SearchResult search(SearchParam param) {

        //1、动态构建出查询需要的DSL语句
        SearchResult result = null;

        //1、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2、执行检索请求
            SearchResponse response = client.search(searchRequest, EmallElasticSearchConfig.COMMON_OPTIONS);

            //3、分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();

        List<SkuEsModel> esModels = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        if (!Arrays.isNullOrContainsNull(searchHits)) {
            for (SearchHit hit : searchHits) {
                String source = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(source, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String s = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(s);
                }
                esModels.add(skuEsModel);
            }
        }
        result.setProduct(esModels);

        Aggregations aggregations = response.getAggregations();

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        Terms catalogAgg = aggregations.get("catalog_agg");

        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            Terms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }

        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        //获取到品牌的聚合
        Terms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            //2、得到品牌的名字
            Terms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            List<? extends Terms.Bucket> buckets = brandNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(buckets)) {
                brandVo.setBrandName(buckets.get(0).getKeyAsString());
            }

            //3、得到品牌的图片
            Terms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            if (!CollectionUtils.isEmpty(brandImgAgg.getBuckets())) {
                brandVo.setBrandName(brandImgAgg.getBuckets().get(0).getKeyAsString());
            }
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }

        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = aggregations.get("attrs_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //2、得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = null;
            List<? extends Terms.Bucket> buckets = attrNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(buckets)) {
                attrName = buckets.get(0).getKeyAsString();
            }
            attrVo.setAttrName(attrName);

            //3、得到属性的所有值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        long total = hits.getTotalHits().value;
        result.setAttrs(attrVos);
        result.setCatalogs(catalogVos);
        result.setBrands(brandVos);
        result.setPageNum(param.getPageNum());
        result.setTotal(total);
        result.setTotalPages((int) Math.ceil(total / 1.0 / EsConstant.PRODUCT_PAGESIZE));

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= result.getTotalPages(); i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                //2、取消了这个面包屑以后，我们要跳转到哪个地方，将请求的地址url里面的当前置空
                //拿到所有的查询条件，去掉当前
                try {
                    attr = URLEncoder.encode(attr, "UTF-8");
                    attr = attr.replace("+", "%20");  //浏览器对空格的编码和Java不一样，差异化处理
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String replace = param.get_queryString().replace("&attrs=" + attr, "");
                navVo.setLink("http://search.emall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);

        }
        return result;
    }


    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",
                    param.getKeyword()));
        }

        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //skuPrice形式为：1_500或_500或500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] price = param.getSkuPrice().split("_");
            if (price.length == 2) {
                rangeQueryBuilder.gte(price[0]).lte(price[1]);
            } else if (price.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(price[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(price[0]);
                }
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        if (!CollectionUtils.isEmpty(param.getAttrs())) {

            param.getAttrs().forEach(item -> {
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                String[] s = item.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQueryBuilder);
            });
        }

        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            sourceBuilder.sort(s[0], s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }

        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        sourceBuilder.query(boolQuery);

        sourceBuilder
                .aggregation(AggregationBuilders.terms("brand_agg").field("brandId").size(50)
                        .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1))
                        .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1)))
                .aggregation(AggregationBuilders.terms("catalog_agg").field("catalogId").size(20)
                        .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName.keyword").size(1)))
                .aggregation(AggregationBuilders.nested("attrs_agg", "attrs")
                        .subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1))
                                .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50)))
                );

        if (!StringUtils.isEmpty(param.getKeyword())) {
            sourceBuilder.highlighter(new HighlightBuilder().field("skuTitle").preTags("<b style='color:red" +
                    "'>").postTags("</b>"));
        }
        System.out.println(sourceBuilder.toString());

        return new SearchRequest(EsConstant.PRODUCT_INDEX).source(sourceBuilder);
    }
}
