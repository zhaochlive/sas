package com.js.sas.service;

import com.js.sas.dto.OverdueDTO;
import com.js.sas.repository.PartnerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName FinanceService
 * @Description 财务Service
 * @Author zc
 * @Date 2019/6/19 18:58
 **/
@Service
@Slf4j
public class FinanceService {

    @PersistenceContext
    private EntityManager entityManager;

    final
    PartnerRepository partnerRepository;

    @Autowired
    PartnerService partnerService;

    public FinanceService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    /**
     * 结算客户对账单（线上、线下）
     *
     * @param name      结算客户名称
     * @param channel   来源（线上、线下）
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param offset    偏移量
     * @param limit     数量
     * @param sort      排序字段
     * @param sortOrder 排序规则
     * @return Map<String, Object>
     */
    public Map<String, Object> getSettlementSummary(String name, String channel, String startDate, String endDate, int offset, int limit, String sort, String sortOrder) {
        HashMap<String, Object> result = new HashMap<>();

        StoredProcedureQuery store = this.entityManager.createNamedStoredProcedureQuery("getSettlementSummary");

        store.setParameter("settlementName", name);
        store.setParameter("channel", channel);
        store.setParameter("startDate", startDate);
        store.setParameter("endDate", endDate);
        store.setParameter("offsetNum", offset);
        store.setParameter("limitNum", limit);
        store.setParameter("sort", sort);
        store.setParameter("sortOrder", sortOrder);

        List settlementSummaryList = store.getResultList();

        result.put("rows", settlementSummaryList);
        result.put("total", store.getOutputParameterValue("totalNum"));

        return result;
    }

    public Page<OverdueDTO> findOverdue(OverdueDTO partner) {
        // 排序规则
        Sort.Direction sortDirection;
        if (partner.getSortOrder() == null || partner.getSortOrder().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else {
            sortDirection = Sort.Direction.ASC;
        }
        // 判断排序字段
        if (!StringUtils.isNotBlank(partner.getSort())) {
            partner.setSort("name");
        }

        if (partner.getLimit() <= 0) {
            partner.setLimit(1);
        }

        Sort sort = new Sort(sortDirection, partner.getSort());
        PageRequest pageRequest = PageRequest.of(partner.getOffset() / partner.getLimit(), partner.getLimit(), sort);

        // 查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("offset")
                .withIgnorePaths("limit")
                .withIgnorePaths("sort")
                .withIgnorePaths("sortOrder");

        Example example = Example.of(partner, matcher);

        return partnerRepository.findAll(example, pageRequest);
    }

}
