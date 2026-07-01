package com.ruoyi.batch.tutorial.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.batch.tutorial.domain.BatchTutorial;
import com.ruoyi.batch.tutorial.domain.BatchTutorialCategory;
import com.ruoyi.batch.tutorial.service.IBatchTutorialCategoryService;
import com.ruoyi.batch.tutorial.service.IBatchTutorialService;

/**
 * 教程管理 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/tutorial")
public class BatchTutorialController extends BaseController
{
    @Autowired
    private IBatchTutorialService tutorialService;

    @Autowired
    private IBatchTutorialCategoryService categoryService;

    /**
     * 获取教程列表
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchTutorial tutorial)
    {
        startPage();
        List<BatchTutorial> list = tutorialService.selectTutorialList(tutorial);
        return getDataTable(list);
    }

    /**
     * 导出教程列表
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:export')")
    @Log(title = "教程管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BatchTutorial tutorial)
    {
        List<BatchTutorial> list = tutorialService.selectTutorialList(tutorial);
        ExcelUtil<BatchTutorial> util = new ExcelUtil<BatchTutorial>(BatchTutorial.class);
        util.exportExcel(response, list, "教程数据");
    }

    /**
     * 根据教程编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:query')")
    @GetMapping(value = "/{tutorialId}")
    public AjaxResult getInfo(@PathVariable Long tutorialId)
    {
        return success(tutorialService.selectTutorialById(tutorialId));
    }

    /**
     * 新增教程
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:add')")
    @Log(title = "教程管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BatchTutorial tutorial)
    {
        tutorial.setCreateBy(getUsername());
        tutorial.setViewCount(0);
        return toAjax(tutorialService.insertTutorial(tutorial));
    }

    /**
     * 修改教程
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:edit')")
    @Log(title = "教程管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BatchTutorial tutorial)
    {
        tutorial.setUpdateBy(getUsername());
        return toAjax(tutorialService.updateTutorial(tutorial));
    }

    /**
     * 修改教程状态
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:edit')")
    @Log(title = "教程管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchTutorial tutorial)
    {
        tutorial.setUpdateBy(getUsername());
        return toAjax(tutorialService.updateTutorial(tutorial));
    }

    /**
     * 删除教程
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:remove')")
    @Log(title = "教程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tutorialIds}")
    public AjaxResult remove(@PathVariable Long[] tutorialIds)
    {
        return toAjax(tutorialService.deleteTutorialByIds(tutorialIds));
    }

    /**
     * 获取教程分类列表
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:list')")
    @GetMapping("/category/list")
    public TableDataInfo categoryList(BatchTutorialCategory category)
    {
        startPage();
        List<BatchTutorialCategory> list = categoryService.selectCategoryList(category);
        return getDataTable(list);
    }

    /**
     * 获取所有有效分类
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:list')")
    @GetMapping("/category/all")
    public AjaxResult categoryAll()
    {
        return success(categoryService.selectCategoryAll());
    }

    /**
     * 根据分类编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:query')")
    @GetMapping("/category/{categoryId}")
    public AjaxResult getCategoryInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增分类
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:add')")
    @Log(title = "教程分类", businessType = BusinessType.INSERT)
    @PostMapping("/category")
    public AjaxResult addCategory(@Validated @RequestBody BatchTutorialCategory category)
    {
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改分类
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:edit')")
    @Log(title = "教程分类", businessType = BusinessType.UPDATE)
    @PutMapping("/category")
    public AjaxResult editCategory(@Validated @RequestBody BatchTutorialCategory category)
    {
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除分类
     */
    @PreAuthorize("@ss.hasPermi('batch:tutorial:remove')")
    @Log(title = "教程分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/category/{categoryIds}")
    public AjaxResult removeCategory(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }
}
