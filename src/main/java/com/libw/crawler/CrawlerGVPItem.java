package com.libw.crawler;

import java.io.IOException;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Generated;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author libw
 * @since 2021/9/26 22:27
 */

@Slf4j
public class CrawlerGVPItem {

  private static final String webSite = "https://gitee.com/gvp/all";

  public static void main(String[] args) throws IOException {
    Document doc = Jsoup.connect(webSite).get();

    String totalStr = doc.select("span .text-muted").text();

    String total = totalStr.replace("(", "").replace(")", "");

    Elements items = doc.getElementsByClass("ui fluid card project-card categorical-project-card");
    log.info("共有项目：{}",items.size());
    if (CollectionUtils.isNotEmpty(items) && items.size() == Integer.valueOf(total))
    {
      for (Element item : items) {
        String projectName = item.select("div .project-name").text();
        String projectDescription = item.select("div .project-description").text();
        String projectLabel = item.select("div .project-labels").text();
        String url = "https://gitee.com/" + item.select("a[target=_blank]").attr("href");
        Double starNum = convertKNum(item.select("span[title*=Star] > span").text());
        Double forkNum = convertKNum(item.select("span[title*=Fork] > span").text());

        GVPItem gvpItem = new GVPItem();
        gvpItem.setName(projectName);
        gvpItem.setDescription(projectDescription);
        gvpItem.setTag(projectLabel);
        gvpItem.setStarNum(starNum);
        gvpItem.setForkNum(forkNum);
        gvpItem.setUrl(url);


      }
    }
  }

  public static Double convertKNum(String num)
  {
    if (StringUtils.isEmpty(num))
    {
      return 0.0;
    }

    if (num.contains("K"))
    {
      Double number = Double.valueOf(num.replace("K", ""));
      return number * 1000;
    }

    return Double.valueOf(num);
  }

  @Data
  @NoArgsConstructor
  static class GVPItem{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目标签
     */
    private String tag;

    /**
     * star数
     */
    private Double starNum;

    /**
     * fork数
     */
    private Double forkNum;

    /**
     * url
     */
    private String url;
  }

}


