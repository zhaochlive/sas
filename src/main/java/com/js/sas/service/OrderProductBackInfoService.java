package com.js.sas.service;

import com.js.sas.entity.OrderProductBackInfo;
import com.js.sas.repository.OrderProductBackInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: daniel
 * @date: 2019/12/7 0007 16:34
 * @Description:
 */
@Component
public class OrderProductBackInfoService {

    @Autowired
    private OrderProductBackInfoRepository orderProductBackInfoRepository;


    public List<OrderProductBackInfo> saveAll(List<OrderProductBackInfo> orderProductBackInfos){

        List<OrderProductBackInfo> all = orderProductBackInfoRepository.saveAll(orderProductBackInfos);

        return all;
    }

    public OrderProductBackInfo findById(Long id){
        return orderProductBackInfoRepository.findById(id).orElse(null);
    }
}
