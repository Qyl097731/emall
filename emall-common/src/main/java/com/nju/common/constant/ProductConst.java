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


    public enum ProductStatusEnum {
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(2,"商品下架");

        private int code;

        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        ProductStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }
}
