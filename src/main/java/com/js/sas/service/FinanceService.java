package com.js.sas.service;

import lombok.extern.slf4j.Slf4j;
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
}
