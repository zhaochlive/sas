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
     * 紧商网用户名username
     * @param name
     * @return
     */
    @PostMapping("/username")
    public Result getUsername(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getUsername(name));
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
     * 一级分类、标准
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

    /**
     * 仓库名称
     * @param name
     * @return
     */
    @PostMapping("/getStore")
    public Result getStore(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getStore(name));
    }
    /**
     * 商品名称
     * @param name
     * @return
     */
    @PostMapping("/getProductName")
    public Result getProuctName(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getProductName(name));
    }
    /**
     * 品牌
     * @param name
     * @return
     */
    @PostMapping("/getBrand")
    public Result getBrand(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getBrand(name));
    }
    /**
     * 印记
     * @param name
     * @return
     */
    @PostMapping("/getMark")
    public Result getMark(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getMark(name));
    }
    /**
     * 材质
     * @param name
     * @return
     */
    @PostMapping("/getMaterial")
    public Result getMaterial(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getMaterial(name));
    }
    /**
     * 牌号
     * @param name
     * @return
     */
    @PostMapping("/getGrade")
    public Result getGrade(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getGrade(name));
    }
    /**
     * 表面处理
     * @param name
     * @return
     */
    @PostMapping("/getSurface")
    public Result getSurface(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getSurface(name));
    }
    /**
     * 公称直径
     * @param name
     * @return
     */
    @PostMapping("/getNominalDiameter")
    public Result getNominalDiameter(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getNominalDiameter(name));
    }
    /**
     * 牙距
     * @param name
     * @return
     */
    @PostMapping("/getPitch")
    public Result getPitch(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getPitch(name));
    }
    /**
     * 长度
     * @param name
     * @return
     */
    @PostMapping("/getExtent")
    public Result getExtent(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getExtent(name));
    }
    /**
     * 外径
     * @param name
     * @return
     */
    @PostMapping("/getOuterDiameter")
    public Result getOuterDiameter(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getOuterDiameter(name));
    }
    /**
     * 厚度
     * @param name
     * @return
     */
    @PostMapping("/getThickness")
    public Result getThickness(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getThickness(name));
    }
    /**
     * 一级分类
     * @param name
     * @return
     */
    @PostMapping("/getLevelOne")
    public Result getLevelOne(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getLevelOne(name));
    }
    /**
     * 二级分类
     * @param name
     * @return
     */
    @PostMapping("/getLevelTwo")
    public Result getLevelTwo(@Param("name")String name ){
        return new Result("200", "success", searchOptionService.getLevelTwo(name));
    }


}
