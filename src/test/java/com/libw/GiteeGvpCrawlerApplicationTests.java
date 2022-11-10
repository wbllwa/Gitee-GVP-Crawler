package com.libw;

import cn.hutool.core.io.FileUtil;
import com.libw.crawler.ExportUtil;
import com.libw.crawler.IGVPItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@SpringBootTest
class GiteeGvpCrawlerApplicationTests {

	@Autowired
	IGVPItemService gvpItemService;

	/**
	 * 导出最新数据
	 */
	@Test
	void exportXlsxLastData() {
		gvpItemService.deleteAll();
		gvpItemService.crawlerData();

		File file = new File("./" + ExportUtil.getXlsxName());
		try (OutputStream outputStream = FileUtil.getOutputStream(file))
		{
			gvpItemService.exportExcel(outputStream);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}

	}

}
