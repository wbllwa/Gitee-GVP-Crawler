package com.libw.crawler;

import com.libw.crawler.entity.po.GVPItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        HttpServletResponse response = ServletUtils.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=" + ExportUtil.getXlsxName());
        try (ServletOutputStream outputStream = response.getOutputStream())
        {
            gvpItemService.exportExcel(outputStream);
        }
        catch (IOException e)
        {
            log.error("导出数据失败", e);
        }
    }

    @GetMapping("exportCsv")
    public void exportCsv()
    {
        HttpServletResponse response = ServletUtils.getResponse();
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=" + ExportUtil.getCsvName());
        try (ServletOutputStream outputStream = response.getOutputStream())
        {
            gvpItemService.exportCSV(outputStream);
        }
        catch (IOException e)
        {
            log.error("导出数据失败", e);
        }
    }

    @GetMapping("exportHtml")
    public void exportHtml()
    {
        HttpServletResponse response = ServletUtils.getResponse();
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=" + ExportUtil.getCsvName());
        try (ServletOutputStream outputStream = response.getOutputStream())
        {
            gvpItemService.exportHtml(outputStream);
        }
        catch (IOException e)
        {
            log.error("导出数据失败", e);
        }
    }


    @DeleteMapping("deleteAll")
    public void deleteAll()
    {
        gvpItemService.deleteAll();
    }
}
