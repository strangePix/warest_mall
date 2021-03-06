package com.warest.mall.util;

import java.math.BigDecimal;

/**
 * 工具类
 */
public class BigDecimalUtil {

    private BigDecimalUtil(){

    }


    // 将double类型进行相加操作
    public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal add(BigDecimal v1,BigDecimal v2){
        return v1.add(v2);
    }

    // 减法操作
    public static BigDecimal subtract(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }


    public static BigDecimal multiply(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal multiply(BigDecimal v1,double v2){
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return v1.multiply(b2);
    }

    /**
     * 除法，四舍五入保留2位小数
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divide(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数
        //除不尽的情况
    }





}
