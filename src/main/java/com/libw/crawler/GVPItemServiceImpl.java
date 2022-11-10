package com.libw.crawler;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.libw.crawler.entity.dto.ExportData;
import com.libw.crawler.entity.dto.Sheet;
import com.libw.crawler.entity.po.GVPItem;
import com.libw.crawler.entity.po.GVPItem_;
import com.libw.crawler.entity.vo.SheetVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.WorkbookUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫处理逻辑
 * @author libw
 * @since 2021/9/27 15:01
 */

@Slf4j
@Service
public class GVPItemServiceImpl implements IGVPItemService
{

    @Autowired
    GVPItemRepositry gvpItemRepositry;

    private static final String CRAWLER_WEB_SITE = "https://gitee.com/gvp/all";

    private static final String URL_PREFIX = "https://gitee.com/";

    @Value("${crawler.proxy.host}")
    private String proxyHost;

    @Value("${crawler.proxy.port}")
    private int proxyPort;

    @Override
    public void crawlerData()
    {
        log.info("[爬虫]-[清空旧数据]");
        this.deleteAll();

        log.info("[爬虫]-[开始爬取：{}]", CRAWLER_WEB_SITE);
        Document doc = null;
        try
        {
            Connection conn  = Jsoup.connect(CRAWLER_WEB_SITE);

            conn.timeout(60 * 1000);

            if (StringUtils.isNotBlank(proxyHost))
            {
                conn = conn.proxy(proxyHost, proxyPort);
            }

            doc = conn.get();
        }
        catch (IOException e)
        {
            log.error("[爬虫]-[抓取失败，网址无法访问]", e);
        }

        String totalStr = doc.select("span .text-muted").text();

        String total = totalStr.replace("(", "").replace(")", "");

        Elements items = doc.getElementsByClass("ui fluid card project-card categorical-project-card");
        log.info("[爬虫]-[共有项目：{}]",items.size());
        if (CollectionUtils.isNotEmpty(items) && items.size() == Integer.valueOf(total))
        {
            for (int i = 0; i < items.size(); i++)
            {
                Element item = items.get(i);
                String projectName = item.select("div .project-name").text();
                String projectDescription = item.select("div .project-description").text();
                String projectLabel = item.select("div .project-labels").text();
                String url = URL_PREFIX + item.select("a[target=_blank]").attr("href");
                int starNum = convertKNum(item.select("span[title*=Star] > span").text());
                int forkNum = convertKNum(item.select("span[title*=Fork] > span").text());

                GVPItem gvpItem = new GVPItem();
                gvpItem.setName(projectName);
                gvpItem.setDescription(projectDescription);
                gvpItem.setTag(StringUtils.defaultIfEmpty(projectLabel, "其他"));
                gvpItem.setStarNum(starNum);
                gvpItem.setForkNum(forkNum);
                gvpItem.setUrl(url);
                gvpItemRepositry.save(gvpItem);
                log.info("[爬虫进度：{}]", calculateProgress(i + 1, items.size()));
            }
        }
        else
        {
            log.info("[爬虫]-[爬取失败总数和爬取数不一致 total:{} items:{}]", total, items);
        }

        log.info("[爬虫]-[爬取结束]");
    }

    @Override
    public void deleteAll()
    {
        gvpItemRepositry.deleteAll();
    }

    @Override
    public Page queryGVPItem(Pageable pageable, GVPItem gvpItem)
    {
        Specification specification = (Specification) (root, query, builder) ->
        {
            List<Predicate> predicatesList = new ArrayList<>();
            String name = gvpItem.getName();
            if (StringUtils.isNotEmpty(name))
            {
                Predicate namePredicate = builder.like(root.get(GVPItem_.name), "%" + name + "%");
                predicatesList.add(namePredicate);
            }

            String tag = gvpItem.getTag();
            if (StringUtils.isNotEmpty(tag))
            {
                Predicate namePredicate = builder.equal(root.get(GVPItem_.tag), tag);
                predicatesList.add(namePredicate);
            }

            Predicate[] predicates = new Predicate[predicatesList.size()];
            return builder.and(predicatesList.toArray(predicates));
        };
        return gvpItemRepositry.findAll(specification, pageable);
    }

    private ExportData initExportData()
    {
        ExportData exportData = new ExportData();
        List<Sheet> sheets = new ArrayList<>();
        List<SheetVO> sheetVos = gvpItemRepositry.findSheetName();


        List<String> headRow = new ArrayList<>();
        headRow.add("项目名称");
        headRow.add("url");
        headRow.add("star数");
        headRow.add("fork数");
        headRow.add("项目描述");

        for (SheetVO sheetVo : sheetVos)
        {
            List<GVPItem> gvpItems = gvpItemRepositry.findAllByTagEqualsOrderByStarNumDesc(sheetVo.getTagName());

            Sheet sheet = new Sheet();
            sheet.setTagName(sheetVo.getTagName());
            sheet.setTotal(sheetVo.getTotal());
            sheet.setData(gvpItems);
            sheet.setHeader(headRow);
            sheets.add(sheet);
        }
        exportData.setSheets(sheets);

        return exportData;
    }

    @Override
    public void exportExcel(OutputStream outputStream)
    {
        try(ExcelWriter writer = ExcelUtil.getWriter(true))
        {
            ExportData exportData = this.initExportData();
            List<Sheet> sheets = exportData.getSheets();

            for (int i = 0; i < sheets.size(); i++)
            {
                Sheet sheet = sheets.get(i);
                String sheetName = StrUtil.format("{} ({})", sheet.getTagName(), sheet.getTotal());
                writer.setSheet(i);
                writer.renameSheet(i, WorkbookUtil.createSafeSheetName(sheetName));
                writer.writeHeadRow(sheet.getHeader());
                // 标题行冻结
                writer.setFreezePane(1);

                // 写入数据
                List<GVPItem> data = sheet.getData();
                for (int j = 0; j < data.size(); j++) {
                    GVPItem gvpItem = data.get(j);
                    writer.writeCellValue("A"+ (j + 2), gvpItem.getName());
                    Hyperlink hyperlink = writer.createHyperlink(HyperlinkType.URL, gvpItem.getUrl());
                    writer.writeCellValue("B"+ (j + 2), hyperlink);
                    writer.writeCellValue("C"+ (j + 2), gvpItem.getStarNum());
                    writer.writeCellValue("D"+ (j + 2), gvpItem.getForkNum());
                    writer.writeCellValue("E"+ (j + 2), gvpItem.getDescription());
                }

                // 列设置自动宽度
                writer.autoSizeColumnAll();
            }

            writer.flush(outputStream, true);
        }
    }

    @Override
    public void exportCSV(OutputStream outputStream) throws
            IOException
    {
        outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
        ExportData exportData = this.initExportData();
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CsvWriter csvWriter = CsvUtil.getWriter(writer))
        {
            List<Sheet> sheets = exportData.getSheets();
            for (Sheet sheet : sheets)
            {
                csvWriter.writeHeaderLine(sheet.getHeader().toArray(new String[0]));
                csvWriter.writeBeans(sheet.getData());
            }
            csvWriter.flush();
        }
    }

    @Override
    public void exportHtml(OutputStream outputStream)
    {

    }

    /**
     * 计算进度
     * @param index
     * @param size
     * @return
     */
    private String calculateProgress(int index, int size)
    {
        BigDecimal div = NumberUtil.div(String.valueOf(index), String.valueOf(size));
        BigDecimal mul = NumberUtil.mul(div, new BigDecimal("100"));
        return mul.intValue() + "%";
    }

    /**
     * 转换带K的数字值
     * @param numStr
     * @return
     */
    private static int convertKNum(String numStr)
    {
        if (StringUtils.isEmpty(numStr))
        {
            return 0;
        }

        String K = "K";
        if (numStr.contains(K))
        {
            String num = numStr.replace(K, "");
            return NumberUtil.mul(num, "1000").intValue();
        }

        return Integer.valueOf(numStr);
    }
}
