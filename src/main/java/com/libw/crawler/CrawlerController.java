package com.libw.crawler;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    GVPItemRepositry gvpItemRepositry;

    private static final String CRAWLER_WEB_SITE = "https://gitee.com/gvp/all";

    private static final String URL_PREFIX = "https://gitee.com/";

    /**
     * 抓取数据
     */
    @GetMapping("crawlerData")
    public void crawlerData()
    {
        log.info("[爬虫]-[开始爬取：{}]", CRAWLER_WEB_SITE);
        Document doc = null;
        try
        {
            doc = Jsoup.connect(CRAWLER_WEB_SITE).proxy("192.190.10.101", 3128).get();
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
                BigDecimal starNum = convertKNum(item.select("span[title*=Star] > span").text());
                BigDecimal forkNum = convertKNum(item.select("span[title*=Fork] > span").text());

                GVPItem gvpItem = new GVPItem();
                gvpItem.setName(projectName);
                gvpItem.setDescription(projectDescription);
                gvpItem.setTag(projectLabel);
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

    /**
     * 复杂分页查询
     * @param pageable
     * @param gvpItem
     * @return
     */
    @GetMapping("queryGVPItem")
    public Page<GVPItem> queryGVPItem(Pageable pageable, GVPItem gvpItem)
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

    @DeleteMapping("deleteAll")
    public void deleteAll()
    {
        gvpItemRepositry.deleteAll();
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
    public static BigDecimal convertKNum(String numStr)
    {
        if (StringUtils.isEmpty(numStr))
        {
            return BigDecimal.ZERO;
        }

        if (numStr.contains("K"))
        {
            String num = numStr.replace("K", "");
            return NumberUtil.mul(num, "1000");
        }

        return NumberUtil.toBigDecimal(numStr);
    }
}
