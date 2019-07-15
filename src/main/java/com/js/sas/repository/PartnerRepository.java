package com.js.sas.repository;

import com.js.sas.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName PartnerRepository
 * @Description 往来单位
 * @Author zc
 * @Date 2019/6/10 08:00
 **/
public interface PartnerRepository extends JpaRepository<PartnerEntity, Integer> {

}
