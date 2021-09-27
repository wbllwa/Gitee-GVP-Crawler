package com.libw.crawler;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.libw.crawler.entity.po.GVPItem;
import com.libw.crawler.entity.po.GVPItem_;
import com.libw.crawler.entity.vo.SheetNameVO;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
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

/**
 * @author libw
 * @since 2021/9/27 15:01
 */

@Slf4j
@Service
public class GVPItemServiceImpl implements IGVPItemService
{

    @Autowired
    GVPItemRepositry gvpItemRepositry;

    @Value("${libw.openProxy:true}")
    private Boolean openProxy;

    private static final String CRAWLER_WEB_SITE = "https://gitee.com/gvp/all";

    private static final String URL_PREFIX = "https://gitee.com/";

    private static final String PROXY_HOST = "192.190.10.101";
    private static final int PROXY_PORT = 3128;

    @Override
    public void crawlerData()
    {
        log.info("[爬虫]-[开始爬取：{}]", CRAWLER_WEB_SITE);
        Document doc = null;
        try
        {
            Connection conn  = Jsoup.connect(CRAWLER_WEB_SITE);

            conn.timeout(60 * 1000);

            if (openProxy)
            {
                conn = conn.proxy(PROXY_HOST, PROXY_PORT);
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

    @Override
    public void exportExcel()
    {
        HttpServletResponse response = ServletUtils.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename=GVP-Crawler.xlsx");

        try(ExcelWriter writer = ExcelUtil.getWriter(true);
                ServletOutputStream outputStream = response.getOutputStream())
        {
            List<SheetNameVO> sheetNames = gvpItemRepositry.findSheetName();

            for (int i = 0; i < sheetNames.size(); i++)
            {
                SheetNameVO sheetNameVO = sheetNames.get(i);
                String sheetName = StrUtil.format("{} ({})", sheetNameVO.getTagName(), sheetNameVO.getTotal());
                writer.setSheet(i);
                writer.renameSheet(i, WorkbookUtil.createSafeSheetName(sheetName));

                List<String> headRow = new ArrayList<>();
                headRow.add("项目名称");
                headRow.add("url");
                headRow.add("star数");
                headRow.add("fork数");
                headRow.add("项目描述");
                writer.writeHeadRow(headRow);
                // 标题行冻结
                writer.setFreezePane(1);

                // 写入数据
                List<GVPItem> gvpItems = gvpItemRepositry.findAllByTagEqualsOrderByStarNumDesc(sheetNameVO.getTagName());
                for (int j = 0; j < gvpItems.size(); j++) {
                    GVPItem gvpItem = gvpItems.get(j);
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
        catch (IOException e)
        {
            log.error("导出数据失败", e);
        }
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
