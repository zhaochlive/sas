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
        String sql = "SELECT companyname from ( SELECT companyname from sellercompanyinfo GROUP BY companyname  )ss where companyname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    //商家，店铺公司名称
    public List<String> getBuyerCompanyname(String name) {
        String sql = "SELECT companyname from ( select companyname from buyercompanyinfo GROUP BY companyname )ss where companyname ilike '%" + name + "%'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
