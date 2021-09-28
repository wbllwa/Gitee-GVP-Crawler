package com.libw.crawler;

import com.libw.crawler.entity.po.GVPItem;
import com.libw.crawler.entity.vo.SheetNameVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 存储层
 *
 * @See https://stackoverflow.com/questions/46083329/no-converter-found-capable-of-converting-from-type-to-type
 * @author libw
 * @since 2021/9/27 9:53
 */

@Repository
public interface GVPItemRepositry extends JpaRepository<GVPItem, Long>, JpaSpecificationExecutor<GVPItem>
{
    @Query(value = "SELECT IF (g.tag != '', g.tag, '其他') as tagName, count(*) as total FROM gvp_item g GROUP BY g.tag ORDER BY total DESC", nativeQuery = true)
    List<SheetNameVO> findSheetName();

    List<GVPItem> findAllByTagEqualsOrderByStarNumDesc(String tag);
}
