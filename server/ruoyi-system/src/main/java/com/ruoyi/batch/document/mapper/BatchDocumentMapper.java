package com.ruoyi.batch.document.mapper;

import java.util.List;
import com.ruoyi.batch.document.domain.BatchDocument;

/**
 * 文档管理Mapper接口
 *
 * @author ruoyi
 */
public interface BatchDocumentMapper
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
     * 根据文档类型和适用页面查询启用的文档
     *
     * @param batchDocument 查询条件
     * @return 结果列表
     */
    public List<BatchDocument> selectEnabledByTypeAndPage(BatchDocument batchDocument);

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
     * 删除文档管理
     *
     * @param documentId 文档管理主键
     * @return 结果
     */
    public int deleteBatchDocumentById(Long documentId);

    /**
     * 批量删除文档管理
     *
     * @param documentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBatchDocumentByIds(Long[] documentIds);
}
