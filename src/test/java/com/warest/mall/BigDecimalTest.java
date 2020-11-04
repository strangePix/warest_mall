package com.warest.mall;

// import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @FileName: CartServiceImplTest
 * @project: mmall
 * @author: ALIENWARE
 * @created: 2020-08-31 9:34
 */
public class BigDecimalTest {

    @Test
    public void test1(){
        System.out.println(0.05+0.1);
        System.out.println(0.05-0.1);
        System.out.println(0.05*0.1);
        System.out.println(0.05/0.1);
    }

    @Test
    public void test2() {

        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.1);
        BigDecimal b3 = new BigDecimal("0.05");
        BigDecimal b4 = new BigDecimal("0.1");

        System.out.println(b1.add(b2));
        System.out.println(b3.add(b4));


    }
}