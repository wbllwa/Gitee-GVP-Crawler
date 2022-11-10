package com.libw.crawler;

import com.libw.crawler.entity.po.GVPItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 爬虫接口
 *
 * @author libw
 * @since 2021/9/27 15:03
 */
public interface IGVPItemService
{
    /**
     * 抓取数据
     */
    void crawlerData();

    /**
     * 清空数据
     */
    void deleteAll();

    /**
     * 复杂分页查询
     * @param pageable
     * @param gvpItem
     * @return
     */
    Page<GVPItem> queryGVPItem(Pageable pageable, GVPItem gvpItem);

    /**
     * 按照tag导出至不同sheet，按照starNum倒序
     */
    void exportExcel(OutputStream outputStream);

    /**
     * 导出CSV
     */
    void exportCSV(OutputStream outputStream) throws IOException;

    /**
     * 导出html
     */
    void exportHtml(OutputStream outputStream);
}
