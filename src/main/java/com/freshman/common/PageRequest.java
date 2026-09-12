package com.freshman.common;

/**
 * 分页请求参数类
 * 功能：封装前端传来的分页查询参数，包括页码、每页条数、搜索关键词等，
 *       并提供计算偏移量的方法，方便与MyBatis-Plus分页插件配合使用
 * 负责成员：D (Team Lead)
 * 所属模块：通用工具模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
public class PageRequest {

    /** 当前页码，默认为第1页 */
    private Integer page = 1;

    /** 每页显示条数，默认为10条 */
    private Integer size = 10;

    /** 搜索关键词，用于模糊查询 */
    private String keyword;

    /** 获取当前页码 */
    public Integer getPage() { return page; }

    /** 设置当前页码 */
    public void setPage(Integer page) { this.page = page; }

    /** 获取每页条数 */
    public Integer getSize() { return size; }

    /** 设置每页条数 */
    public void setSize(Integer size) { this.size = size; }

    /** 获取搜索关键词 */
    public String getKeyword() { return keyword; }

    /** 设置搜索关键词 */
    public void setKeyword(String keyword) { this.keyword = keyword; }

    /**
     * 计算数据库查询的偏移量
     * 公式：(当前页码 - 1) * 每页条数
     *
     * @return 数据库查询的offset值，用于SQL的LIMIT子句
     */
    public Integer getOffset() { return (page - 1) * size; }
}
