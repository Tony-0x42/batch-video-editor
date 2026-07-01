package com.ruoyi.batch.tutorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.batch.tutorial.domain.BatchTutorialCategory;
import com.ruoyi.batch.tutorial.mapper.BatchTutorialCategoryMapper;
import com.ruoyi.batch.tutorial.mapper.BatchTutorialMapper;
import com.ruoyi.batch.tutorial.service.IBatchTutorialCategoryService;

/**
 * 教程分类 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BatchTutorialCategoryServiceImpl implements IBatchTutorialCategoryService
{
    @Autowired
    private BatchTutorialCategoryMapper categoryMapper;

    @Autowired
    private BatchTutorialMapper tutorialMapper;

    /**
     * 查询分类信息
     * 
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @Override
    public BatchTutorialCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    /**
     * 查询分类列表
     * 
     * @param category 分类信息
     * @return 分类集合
     */
    @Override
    public List<BatchTutorialCategory> selectCategoryList(BatchTutorialCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 查询所有有效分类
     * 
     * @return 分类集合
     */
    @Override
    public List<BatchTutorialCategory> selectCategoryAll()
    {
        return categoryMapper.selectCategoryAll();
    }

    /**
     * 新增分类
     * 
     * @param category 分类信息
     * @return 结果
     */
    @Override
    public int insertCategory(BatchTutorialCategory category)
    {
        return categoryMapper.insertCategory(category);
    }

    /**
     * 修改分类
     * 
     * @param category 分类信息
     * @return 结果
     */
    @Override
    public int updateCategory(BatchTutorialCategory category)
    {
        return categoryMapper.updateCategory(category);
    }

    /**
     * 删除分类对象
     * 
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long categoryId)
    {
        if (tutorialMapper.countByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("该分类下存在教程，无法删除");
        }
        return categoryMapper.deleteCategoryById(categoryId);
    }

    /**
     * 批量删除分类信息
     * 
     * @param categoryIds 需要删除的分类ID
     * @return 结果
     */
    @Override
    public int deleteCategoryByIds(Long[] categoryIds)
    {
        for (Long categoryId : categoryIds)
        {
            if (tutorialMapper.countByCategoryId(categoryId) > 0)
            {
                throw new ServiceException("存在分类下包含教程，无法删除");
            }
        }
        return categoryMapper.deleteCategoryByIds(categoryIds);
    }
}
