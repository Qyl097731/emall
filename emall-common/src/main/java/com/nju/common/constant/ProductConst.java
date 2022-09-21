package com.nju.common.constant;

/**
 * @description
 * @date:2022/9/20 19:33
 * @author: qyl
 */
public class ProductConst {
    public enum AttrType {
        SALE("sale", 0), BASE("base", 1);
        private String msg;
        private Integer code;


        AttrType(String msg,Integer code) {
            this.msg = msg;
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
