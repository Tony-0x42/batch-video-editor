package com.ruoyi.batch.tutorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.batch.tutorial.domain.BatchTutorial;
import com.ruoyi.batch.tutorial.mapper.BatchTutorialMapper;
import com.ruoyi.batch.tutorial.service.IBatchTutorialService;

/**
 * 教程 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BatchTutorialServiceImpl implements IBatchTutorialService
{
    @Autowired
    private BatchTutorialMapper tutorialMapper;

    /**
     * 查询教程信息
     * 
     * @param tutorialId 教程ID
     * @return 教程信息
     */
    @Override
    public BatchTutorial selectTutorialById(Long tutorialId)
    {
        return tutorialMapper.selectTutorialById(tutorialId);
    }

    /**
     * 查询教程列表
     * 
     * @param tutorial 教程信息
     * @return 教程集合
     */
    @Override
    public List<BatchTutorial> selectTutorialList(BatchTutorial tutorial)
    {
        return tutorialMapper.selectTutorialList(tutorial);
    }

    /**
     * 新增教程
     * 
     * @param tutorial 教程信息
     * @return 结果
     */
    @Override
    public int insertTutorial(BatchTutorial tutorial)
    {
        return tutorialMapper.insertTutorial(tutorial);
    }

    /**
     * 修改教程
     * 
     * @param tutorial 教程信息
     * @return 结果
     */
    @Override
    public int updateTutorial(BatchTutorial tutorial)
    {
        return tutorialMapper.updateTutorial(tutorial);
    }

    /**
     * 删除教程对象
     * 
     * @param tutorialId 教程ID
     * @return 结果
     */
    @Override
    public int deleteTutorialById(Long tutorialId)
    {
        return tutorialMapper.deleteTutorialById(tutorialId);
    }

    /**
     * 批量删除教程信息
     * 
     * @param tutorialIds 需要删除的教程ID
     * @return 结果
     */
    @Override
    public int deleteTutorialByIds(Long[] tutorialIds)
    {
        return tutorialMapper.deleteTutorialByIds(tutorialIds);
    }
}
