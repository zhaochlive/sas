package com.js.sas.repository;

import com.js.sas.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName OrderProductRepository
 * @Description 订单商品
 * @Author zc
 * @Date 2019/7/16 10:57
 **/
public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Integer> {

}
