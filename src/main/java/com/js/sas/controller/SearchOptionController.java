package com.js.sas.controller;

import com.js.sas.entity.SystemArea;
import com.js.sas.repository.SystemAreaResprository;
import com.js.sas.service.SearchOptionService;
import com.js.sas.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author daniel
 *
 * @Date 2019-9-17 14:19:00
 *
 */
@RestController
@RequestMapping("/search")
public class SearchOptionController {

    @Autowired
    private SearchOptionService searchOptionService;

    /**
     * 地区填充 省市县区
     * @param name
     * @return
     */
    @PostMapping("/area")
    public Result getArea(@Param("name")String name ){
        List<SystemArea> all = searchOptionService.getSystemAreaResprository(name);
        Result success = new Result("200", "success", all);
        return  success;
    }

    /**
     * 业务员名称
     * @param name
     * @return
     */
    @PostMapping("/waysales")
    public Result getWaySalesMan(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getWaysales(name));
    }
    /**
     * 客服名称
     * @param name
     * @return
     */
    @PostMapping("/SaleMan")
    public Result getClerkMan(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getClerkMan(name));
    }
    /**
     * 客户名称
     * @param name
     * @return
     */
    @PostMapping("/shopName")
    public Result getShopName(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getShopName(name));
    }
    /**
     * 客户名称
     * @param name
     * @return
     */
    @PostMapping("/CustomerMan")
    public Result getCustomerMan(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getCustomerMan(name));
    }
    /**
     * 分类、标准
     * @param name
     * @return
     */
    @PostMapping("/classify")
    public Result getClassify(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getClassify(name));
    }
    /**
     * 牌号
     * @param name
     * @return
     */
    @PostMapping("/getGradeNo")
    public Result getGradeNo(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getGradeNo(name));
    }
    /**
     * 规格
     * @param name
     * @return
     */
    @PostMapping("/getStandard")
    public Result getStandard(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getStandard(name));
    }

    /**
     * 商家公司名称
     * @param name
     * @return
     */
    @PostMapping("/getSellerCompanyName")
    public Result getSellerCompanyName(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getSellerCompanyname(name));
    }
    /**
     * 买家公司名称
     * @param name
     * @return
     */
    @PostMapping("/getBuyerCompanyname")
    public Result getBuyerCompanyname(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getBuyerCompanyname(name));
    }
    /**
     * 买家公司名称
     * @param name
     * @return
     */
    @PostMapping("/getInvoiceHeadUp")
    public Result getInvoiceHeadUp(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getInvoiceHeadUp(name));
    }


}
