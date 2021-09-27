package com.libw.crawler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 存储层
 *
 * @author libw
 * @since 2021/9/27 9:53
 */

@Repository
public interface GVPItemRepositry extends JpaRepository<GVPItem, Long>, JpaSpecificationExecutor<GVPItem>
{

}
