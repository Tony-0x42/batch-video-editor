package com.ruoyi.batch.notice.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.batch.notice.domain.BatchAppNotice;
import com.ruoyi.batch.notice.mapper.BatchAppNoticeMapper;
import com.ruoyi.batch.notice.service.IBatchAppNoticeService;

/**
 * APP 公告 服务层实现
 *
 * @author ruoyi
 */
@Service
public class BatchAppNoticeServiceImpl implements IBatchAppNoticeService
{
    @Autowired
    private BatchAppNoticeMapper batchAppNoticeMapper;

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public BatchAppNotice selectBatchAppNoticeById(Long noticeId)
    {
        return batchAppNoticeMapper.selectBatchAppNoticeById(noticeId);
    }

    /**
     * 查询公告列表
     *
     * @param batchAppNotice 公告信息
     * @return 公告集合
     */
    @Override
    public List<BatchAppNotice> selectBatchAppNoticeList(BatchAppNotice batchAppNotice)
    {
        return batchAppNoticeMapper.selectBatchAppNoticeList(batchAppNotice);
    }

    /**
     * 新增公告
     *
     * @param batchAppNotice 公告信息
     * @return 结果
     */
    @Override
    public int insertBatchAppNotice(BatchAppNotice batchAppNotice)
    {
        // 默认暂存
        if (batchAppNotice.getPublishStatus() == null)
        {
            batchAppNotice.setPublishStatus(2);
        }
        // 立即发布时设置发布时间
        if (Integer.valueOf(0).equals(batchAppNotice.getPublishStatus()))
        {
            batchAppNotice.setPublishTime(new Date());
        }
        if (batchAppNotice.getReadCount() == null)
        {
            batchAppNotice.setReadCount(0);
        }
        return batchAppNoticeMapper.insertBatchAppNotice(batchAppNotice);
    }

    /**
     * 修改公告
     *
     * @param batchAppNotice 公告信息
     * @return 结果
     */
    @Override
    public int updateBatchAppNotice(BatchAppNotice batchAppNotice)
    {
        // 从暂存变为已发布时设置发布时间
        if (Integer.valueOf(0).equals(batchAppNotice.getPublishStatus()))
        {
            BatchAppNotice exist = batchAppNoticeMapper.selectBatchAppNoticeById(batchAppNotice.getNoticeId());
            if (exist == null || exist.getPublishTime() == null)
            {
                batchAppNotice.setPublishTime(new Date());
            }
        }
        return batchAppNoticeMapper.updateBatchAppNotice(batchAppNotice);
    }

    /**
     * 删除公告对象
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int deleteBatchAppNoticeById(Long noticeId)
    {
        return batchAppNoticeMapper.deleteBatchAppNoticeById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    public int deleteBatchAppNoticeByIds(Long[] noticeIds)
    {
        return batchAppNoticeMapper.deleteBatchAppNoticeByIds(noticeIds);
    }

    /**
     * 发布公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int publishNotice(Long noticeId)
    {
        BatchAppNotice notice = new BatchAppNotice();
        notice.setNoticeId(noticeId);
        notice.setPublishStatus(0);
        notice.setPublishTime(new Date());
        return batchAppNoticeMapper.updateBatchAppNotice(notice);
    }

    /**
     * 下架公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int unpublishNotice(Long noticeId)
    {
        BatchAppNotice notice = new BatchAppNotice();
        notice.setNoticeId(noticeId);
        notice.setPublishStatus(1);
        return batchAppNoticeMapper.updateBatchAppNotice(notice);
    }
}
