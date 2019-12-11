package com.js.sas.repository;

import com.js.sas.entity.OrderProductBackInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderProductBackInfoRepository extends JpaRepository<OrderProductBackInfo, Long> {

    List<OrderProductBackInfo> findOrderProductBackInfoByOrderno(String orderno);


}
