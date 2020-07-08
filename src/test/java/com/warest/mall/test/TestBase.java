package com.warest.mall.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations={"classpath:springMVC.xml"})
public abstract class TestBase {
//public abstract class TestBase extends AbstractTransactionalJUnit4SpringContextTests {

}
