package com.libw.crawler.entity.dto;

import com.libw.crawler.entity.po.GVPItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libw
 * @since 2022/3/23 18:01
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sheet
{
    /**
     * 标签名称
     */
    String tagName;

    /**
     * 总数
     */
    Long total;

    /**
     * 表头
     */
    private List<String> header = new ArrayList<>();

    /**
     * 表格数据
     */
    private List<GVPItem> data = new ArrayList<>();
}
