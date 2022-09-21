package com.nju.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @description 拼音工具
 * @author: qyl
 * @create: 2022-06-07 14:22
 **/
@Slf4j
public class PinyinUtil {

    public static void main(String[] args) {
        System.out.println("'张三'转成拼音：" + toFirstUpChar("张三"));
    }
    /**
     * 获取字符串拼音的第一个字母,大写
     * @param chinese
     * @return
     */
    public static String toFirstUpChar(String chinese){
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();  //转为单个字符
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //只要第一个
        if(newChar.length > 0){
            for (int i = 0; i < 1; i++) {
                if (newChar[i] > 128) {
                    try {
                        pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0].charAt(0);
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        log.error(e.getLocalizedMessage());
                    }
                }else{
                    pinyinStr += newChar[i];
                }
            }
            return pinyinStr;
        }else{
            return null;
        }
    }
}
