# Gitee-GVP-Crawler
爬取Gitee GVP相关数据

## 部署流程
- 新建数据库**crawler**  
- 数据库初始化脚本**db/gvp_item.sql**
- 启动项目

## 接口调用
执行爬虫，重新爬取数据并入库
http://localhost:8080/crawlerData

数据导出excel
http://localhost:8080/exportExcel