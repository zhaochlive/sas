package com.js.sas;

import com.js.sas.service.FinanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SasApplicationTests {

    @Resource
    private FinanceService financeService;

    @Test
    public void contextLoads() {
    }

}
