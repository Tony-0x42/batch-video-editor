package com.ruoyi.batch.tutorial.service;

import java.util.List;
import com.ruoyi.batch.tutorial.domain.BatchTutorialCategory;

/**
 * 教程分类 服务层
 * 
 * @author ruoyi
 */
public interface IBatchTutorialCategoryService
{
    /**
     * 查询分类信息
     * 
     * @param categoryId 分类ID
     * @return 分类信息
     */
    public BatchTutorialCategory selectCategoryById(Long categoryId);

    /**
     * 查询分类列表
     * 
     * @param category 分类信息
     * @return 分类集合
     */
    public List<BatchTutorialCategory> selectCategoryList(BatchTutorialCategory category);

    /**
     * 查询所有有效分类
     * 
     * @return 分类集合
     */
    public List<BatchTutorialCategory> selectCategoryAll();

    /**
     * 新增分类
     * 
     * @param category 分类信息
     * @return 结果
     */
    public int insertCategory(BatchTutorialCategory category);

    /**
     * 修改分类
     * 
     * @param category 分类信息
     * @return 结果
     */
    public int updateCategory(BatchTutorialCategory category);

    /**
     * 删除分类信息
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    public int deleteCategoryById(Long categoryId);

    /**
     * 批量删除分类信息
     * 
     * @param categoryIds 需要删除的分类ID
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] categoryIds);
}
