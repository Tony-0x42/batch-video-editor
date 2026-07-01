package com.ruoyi.batch.tutorial.service;

import java.util.List;
import com.ruoyi.batch.tutorial.domain.BatchTutorial;

/**
 * 教程 服务层
 * 
 * @author ruoyi
 */
public interface IBatchTutorialService
{
    /**
     * 查询教程信息
     * 
     * @param tutorialId 教程ID
     * @return 教程信息
     */
    public BatchTutorial selectTutorialById(Long tutorialId);

    /**
     * 查询教程列表
     * 
     * @param tutorial 教程信息
     * @return 教程集合
     */
    public List<BatchTutorial> selectTutorialList(BatchTutorial tutorial);

    /**
     * 新增教程
     * 
     * @param tutorial 教程信息
     * @return 结果
     */
    public int insertTutorial(BatchTutorial tutorial);

    /**
     * 修改教程
     * 
     * @param tutorial 教程信息
     * @return 结果
     */
    public int updateTutorial(BatchTutorial tutorial);

    /**
     * 删除教程信息
     * 
     * @param tutorialId 教程ID
     * @return 结果
     */
    public int deleteTutorialById(Long tutorialId);

    /**
     * 批量删除教程信息
     * 
     * @param tutorialIds 需要删除的教程ID
     * @return 结果
     */
    public int deleteTutorialByIds(Long[] tutorialIds);
}
