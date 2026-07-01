package com.ruoyi.batch.notice.mapper;

import java.util.List;
import com.ruoyi.batch.notice.domain.BatchAppNotice;

/**
 * APP 公告 Mapper接口
 *
 * @author ruoyi
 */
public interface BatchAppNoticeMapper
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
     * 删除公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteBatchAppNoticeById(Long noticeId);

    /**
     * 批量删除公告
     *
     * @param noticeIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBatchAppNoticeByIds(Long[] noticeIds);
}
