package com.libw.crawler.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author libw
 * @since 2022/3/23 18:01
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportData
{
    List<Sheet> sheets;

}
