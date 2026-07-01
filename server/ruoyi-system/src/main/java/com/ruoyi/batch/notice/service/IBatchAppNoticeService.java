package com.ruoyi.batch.notice.service;

import java.util.List;
import com.ruoyi.batch.notice.domain.BatchAppNotice;

/**
 * APP 公告 服务层
 *
 * @author ruoyi
 */
public interface IBatchAppNoticeService
{
    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public BatchAppNotice selectBatchAppNoticeById(Long noticeId);

    /**
     * 查询公告列表
     *
     * @param batchAppNotice 公告信息
     * @return 公告集合
     */
    public List<BatchAppNotice> selectBatchAppNoticeList(BatchAppNotice batchAppNotice);

    /**
     * 新增公告
     *
     * @param batchAppNotice 公告信息
     * @return 结果
     */
    public int insertBatchAppNotice(BatchAppNotice batchAppNotice);

    /**
     * 修改公告
     *
     * @param batchAppNotice 公告信息
     * @return 结果
     */
    public int updateBatchAppNotice(BatchAppNotice batchAppNotice);

    /**
     * 删除公告信息
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteBatchAppNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteBatchAppNoticeByIds(Long[] noticeIds);

    /**
     * 发布公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int publishNotice(Long noticeId);

    /**
     * 下架公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int unpublishNotice(Long noticeId);
}
