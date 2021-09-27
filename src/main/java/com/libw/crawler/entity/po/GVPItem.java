package com.libw.crawler.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * GVP项目实体
 *
 * @author libw
 * @since 2021/9/27 9:49
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gvp_item")
public class GVPItem
{
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
    private Integer starNum;

    /**
     * fork数
     */
    private Integer forkNum;

    /**
     * url
     */
    private String url;
}
