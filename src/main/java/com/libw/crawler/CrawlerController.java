package com.libw.crawler;

import com.libw.crawler.entity.po.GVPItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 爬虫控制器
 *
 * @author libw
 * @since 2021/9/27 9:50
 */

@Slf4j
@RestController
public class CrawlerController
{

    @Autowired
    private IGVPItemService gvpItemService;

    @GetMapping("crawlerData")
    public void crawlerData()
    {
        gvpItemService.crawlerData();
    }

    @GetMapping("queryGVPItem")
    public Page<GVPItem> queryGVPItem(Pageable pageable, GVPItem gvpItem)
    {
        return gvpItemService.queryGVPItem(pageable, gvpItem);
    }

    @GetMapping("exportExcel")
    public void exportExcel()
    {
        gvpItemService.exportExcel();
    }

    @DeleteMapping("deleteAll")
    public void deleteAll()
    {
        gvpItemService.deleteAll();
    }
}
