package com.ruoyi.batch.document.controller;

import java.util.List;
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
import com.ruoyi.batch.document.domain.BatchDocument;
import com.ruoyi.batch.document.service.IBatchDocumentService;

/**
 * 文档管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/batch/document")
public class BatchDocumentController extends BaseController
{
    @Autowired
    private IBatchDocumentService batchDocumentService;

    /**
     * 查询文档管理列表
     */
    @PreAuthorize("@ss.hasPermi('batch:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(BatchDocument batchDocument)
    {
        startPage();
        List<BatchDocument> list = batchDocumentService.selectBatchDocumentList(batchDocument);
        return getDataTable(list);
    }

    /**
     * 根据文档编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('batch:document:query')")
    @GetMapping(value = "/{documentId}")
    public AjaxResult getInfo(@PathVariable Long documentId)
    {
        return success(batchDocumentService.selectBatchDocumentById(documentId));
    }

    /**
     * 新增文档管理
     */
    @PreAuthorize("@ss.hasPermi('batch:document:add')")
    @Log(title = "文档管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BatchDocument batchDocument)
    {
        batchDocument.setCreateBy(getUsername());
        return toAjax(batchDocumentService.insertBatchDocument(batchDocument));
    }

    /**
     * 修改文档管理
     */
    @PreAuthorize("@ss.hasPermi('batch:document:edit')")
    @Log(title = "文档管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BatchDocument batchDocument)
    {
        batchDocument.setUpdateBy(getUsername());
        return toAjax(batchDocumentService.updateBatchDocument(batchDocument));
    }

    /**
     * 修改文档状态（启用/禁用）
     */
    @PreAuthorize("@ss.hasPermi('batch:document:edit')")
    @Log(title = "文档管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody BatchDocument batchDocument)
    {
        batchDocument.setUpdateBy(getUsername());
        return toAjax(batchDocumentService.changeStatus(batchDocument));
    }

    /**
     * 删除文档管理
     */
    @PreAuthorize("@ss.hasPermi('batch:document:remove')")
    @Log(title = "文档管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{documentIds}")
    public AjaxResult remove(@PathVariable Long[] documentIds)
    {
        return toAjax(batchDocumentService.deleteBatchDocumentByIds(documentIds));
    }
}
