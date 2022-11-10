package com.libw.crawler;

import cn.hutool.core.util.StrUtil;
import com.libw.crawler.entity.dto.ExportData;

import java.util.List;

/**
 * 导出工具类
 *
 * @author libw
 * @since 2022/3/23 17:43
 */
public class ExportUtil<T>
{
    public static final String XLSX = "xlsx";
    public static final String CSV = "csv";
    public static final String HTML = "html";

    /**
     * 导入导出数据列表
     */
    private List<T> list;


    /**
     * 支持导出格式
     */
    public final String[] supportFile = new String[]{XLSX, CSV, HTML};

    /**
     * 获取导出文件名
     * @param suffix
     * @return
     */
    public static String getFileName(String suffix)
    {
        return StrUtil.format("GVP-Crawler-{}.{}", System.currentTimeMillis(), suffix);
    }

    /**
     * 获取导出Xlsx名
     * @return
     */
    public static String getXlsxName()
    {
        return getFileName(XLSX);
    }

    /**
     * 获取导出Csv名
     * @return
     */
    public static String getCsvName()
    {
        return getFileName(CSV);
    }

    /**
     * 获取导出HTML名
     * @return
     */
    public static String getHtmlName()
    {
        return getFileName(HTML);
    }

    /**
     * 创建数据集
     * @param list
     * @param sheetName
     * @param title
     */
    public ExportData init(List<T> list, String sheetName, String title)
    {
        return null;
    }
}
