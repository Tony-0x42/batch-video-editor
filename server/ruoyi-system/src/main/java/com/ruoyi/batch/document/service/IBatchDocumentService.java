package com.ruoyi.batch.document.service;

import java.util.List;
import com.ruoyi.batch.document.domain.BatchDocument;

/**
 * 文档管理Service接口
 *
 * @author ruoyi
 */
public interface IBatchDocumentService
{
    /**
     * 查询文档管理
     *
     * @param documentId 文档管理主键
     * @return 文档管理
     */
    public BatchDocument selectBatchDocumentById(Long documentId);

    /**
     * 查询文档管理列表
     *
     * @param batchDocument 文档管理
     * @return 文档管理集合
     */
    public List<BatchDocument> selectBatchDocumentList(BatchDocument batchDocument);

    /**
     * 新增文档管理
     *
     * @param batchDocument 文档管理
     * @return 结果
     */
    public int insertBatchDocument(BatchDocument batchDocument);

    /**
     * 修改文档管理
     *
     * @param batchDocument 文档管理
     * @return 结果
     */
    public int updateBatchDocument(BatchDocument batchDocument);

    /**
     * 启用/禁用文档
     *
     * @param batchDocument 文档管理
     * @return 结果
     */
    public int changeStatus(BatchDocument batchDocument);

    /**
     * 批量删除文档管理
     *
     * @param documentIds 需要删除的文档管理主键集合
     * @return 结果
     */
    public int deleteBatchDocumentByIds(Long[] documentIds);

    /**
     * 删除文档管理信息
     *
     * @param documentId 文档管理主键
     * @return 结果
     */
    public int deleteBatchDocumentById(Long documentId);
}
