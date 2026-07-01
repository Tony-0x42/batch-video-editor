package com.ruoyi.batch.document.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.batch.document.domain.BatchDocument;
import com.ruoyi.batch.document.mapper.BatchDocumentMapper;
import com.ruoyi.batch.document.service.IBatchDocumentService;

/**
 * 文档管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class BatchDocumentServiceImpl implements IBatchDocumentService
{
    @Autowired
    private BatchDocumentMapper batchDocumentMapper;

    /**
     * 查询文档管理
     */
    @Override
    public BatchDocument selectBatchDocumentById(Long documentId)
    {
        return batchDocumentMapper.selectBatchDocumentById(documentId);
    }

    /**
     * 查询文档管理列表
     */
    @Override
    public List<BatchDocument> selectBatchDocumentList(BatchDocument batchDocument)
    {
        return batchDocumentMapper.selectBatchDocumentList(batchDocument);
    }

    /**
     * 新增文档管理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatchDocument(BatchDocument batchDocument)
    {
        if (batchDocument.getStatus() == null)
        {
            batchDocument.setStatus(0);
        }
        if (batchDocument.getIsSystem() == null)
        {
            batchDocument.setIsSystem(0);
        }
        if (batchDocument.getSortWeight() == null)
        {
            batchDocument.setSortWeight(0);
        }
        checkUniqueEnabledDocument(batchDocument);
        return batchDocumentMapper.insertBatchDocument(batchDocument);
    }

    /**
     * 修改文档管理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatchDocument(BatchDocument batchDocument)
    {
        checkUniqueEnabledDocument(batchDocument);
        return batchDocumentMapper.updateBatchDocument(batchDocument);
    }

    /**
     * 启用/禁用文档
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(BatchDocument batchDocument)
    {
        BatchDocument current = batchDocumentMapper.selectBatchDocumentById(batchDocument.getDocumentId());
        if (current == null)
        {
            throw new ServiceException("文档不存在");
        }
        if (current.getIsSystem() != null && current.getIsSystem() == 1 && batchDocument.getStatus() != null && batchDocument.getStatus() == 1)
        {
            throw new ServiceException("系统默认文档不允许禁用");
        }
        current.setStatus(batchDocument.getStatus());
        checkUniqueEnabledDocument(current);
        return batchDocumentMapper.updateBatchDocument(batchDocument);
    }

    /**
     * 批量删除文档管理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchDocumentByIds(Long[] documentIds)
    {
        for (Long documentId : documentIds)
        {
            BatchDocument document = batchDocumentMapper.selectBatchDocumentById(documentId);
            if (document != null && document.getIsSystem() != null && document.getIsSystem() == 1)
            {
                throw new ServiceException("系统默认文档不可删除：" + document.getDocumentTitle());
            }
        }
        return batchDocumentMapper.deleteBatchDocumentByIds(documentIds);
    }

    /**
     * 删除文档管理信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatchDocumentById(Long documentId)
    {
        BatchDocument document = batchDocumentMapper.selectBatchDocumentById(documentId);
        if (document != null && document.getIsSystem() != null && document.getIsSystem() == 1)
        {
            throw new ServiceException("系统默认文档不可删除");
        }
        return batchDocumentMapper.deleteBatchDocumentById(documentId);
    }

    /**
     * 校验同一文档类型 + 适用页面组合下是否已存在启用状态的文档
     */
    private void checkUniqueEnabledDocument(BatchDocument batchDocument)
    {
        if (batchDocument.getStatus() == null || batchDocument.getStatus() != 0)
        {
            return;
        }
        if (batchDocument.getDocumentType() == null || !StringUtils.hasText(batchDocument.getApplyPages()))
        {
            return;
        }
        String[] pages = batchDocument.getApplyPages().split(",");
        for (String page : pages)
        {
            if (!StringUtils.hasText(page))
            {
                continue;
            }
            BatchDocument query = new BatchDocument();
            query.setDocumentId(batchDocument.getDocumentId());
            query.setDocumentType(batchDocument.getDocumentType());
            query.setApplyPages(page.trim());
            List<BatchDocument> list = batchDocumentMapper.selectEnabledByTypeAndPage(query);
            if (!CollectionUtils.isEmpty(list))
            {
                throw new ServiceException("该位置已存在启用的文档，请先禁用旧文档");
            }
        }
    }
}
