package com.nju.emall.search.controller;

import com.nju.emall.search.service.MallSearchService;
import com.nju.emall.search.vo.SearchParam;
import com.nju.emall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @description
 * @date:2022/9/26 20:46
 * @author: qyl
 */
@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;
    @GetMapping({"list.html","/"})
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {
        param.set_queryString(request.getQueryString());
        SearchResult result  = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
