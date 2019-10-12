package com.js.sas.service;

import com.js.sas.entity.SystemArea;
import com.js.sas.repository.SystemAreaResprository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchOptionService {

    @Autowired
    private SystemAreaResprository systemAreaResprository;

    @Autowired
    @Qualifier(value = "secodJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public List<SystemArea> getSystemAreaResprository(String name) {
        return systemAreaResprository.findAllByNameLike("%" + name + "%");
    }

    //客服名称
    public List<String> getClerkMan(String name) {
        String sql = "SELECT clerkname from ( SELECT clerkname from member GROUP BY clerkname union SELECT clerkname from orders GROUP BY clerkname )ss where clerkname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //分类 标准
    public List<String> getClassify(String name) {
        String sql = "SELECT classify from ( select classify from orderproduct GROUP BY classify )ss where classify ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //用户账号
    public List<String> getMemberUsername(String name) {
        String sql = "select username from member where username ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //牌号
    public List<String> getGradeNo(String name) {
        String sql = "SELECT gradeno from ( select gradeno from orderproduct GROUP BY gradeno )ss where gradeno ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //规格
    public List<String> getStandard(String name) {
        String sql = "SELECT standard from ( select standard from orderproduct GROUP BY standard )ss where standard ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //业务员
    public List<String> getWaysales(String name) {
        String sql = "SELECT waysalesman from ( SELECT waysalesman from member GROUP BY waysalesman union SELECT waysalesman from orders GROUP BY waysalesman )ss where waysalesman ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //紧商网用户名
    public List<String> getUsername(String name) {
        String sql = "select username from ( SELECT username from member GROUP BY username  )ss where username ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //客户名称
    public List<String> getCustomerMan(String name) {
        String sql = "select realname from ( SELECT realname from member GROUP BY realname  )ss where realname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //商家，店铺名称
    public List<String> getShopName(String name) {
        String sql = "SELECT shopname from ( SELECT shopname from sellercompanyinfo GROUP BY shopname union SELECT shopname from orders GROUP BY shopname )ss where shopname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    //商家，店铺公司名称
    public List<String> getSellerCompanyname(String name) {
        String sql = "SELECT distinct(companyname) from sellercompanyinfo where companyname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    //商家，店铺公司名称
    public List<String> getBuyerCompanyname(String name) {
        String sql = "SELECT distinct(companyname) from  buyercompanyinfo where companyname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    //发票抬头
    public List<String> getInvoiceHeadUp(String name) {
        String sql = "SELECT distinct(invoiceheadup) from  billingrecord  where invoiceheadup ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    //仓库名称
    public List<String> getStore(String name) {
        String sql = "SELECT storename from orderproduct where storename ilike '%" + name + "%' group by storename";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public  List<String> getProductName(String name) {
        String sql = "SELECT distinct(productname) from productinfo where productname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getBrand(String name) {
        String sql = "SELECT distinct(brand) from productinfo where brand ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getMark(String name) {
        String sql = "SELECT distinct(mark) from productinfo where mark ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getMaterial(String name) {
        String sql = "SELECT distinct(Material) from productinfo where Material ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getGrade(String name) {
        String sql = "SELECT distinct(cardnum) from productinfo where cardnum ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getSurface(String name) {
        String sql = "SELECT distinct(surfacetreatment) from productinfo where surfacetreatment ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getNominalDiameter(String name) {
        String sql = "SELECT distinct(value) from productattr where attribute ='公称直径' and value ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getPitch(String name) {
        String sql = "SELECT distinct(value) from productattr where attribute ='牙距' and value ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getExtent(String name) {
        String sql = "SELECT distinct(value) from productattr where attribute ='长度' and value ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getOuterDiameter(String name) {
        String sql = "SELECT distinct(value) from productattr where attribute ='外径' and value ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getThickness(String name) {
        String sql = "SELECT distinct(value) from productattr where attribute ='厚度' and value ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getLevelOne(String name) {
        String sql = "SELECT distinct(level1) from productinfo where level1 ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    public  List<String> getLevelTwo(String name) {
        String sql = "SELECT distinct(level2) from productinfo where level2 ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }


}
