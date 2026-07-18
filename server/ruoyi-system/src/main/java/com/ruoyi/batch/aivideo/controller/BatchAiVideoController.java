package com.ruoyi.batch.aivideo.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoClip;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateBody;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGenerateLog;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoGroup;
import com.ruoyi.batch.aivideo.domain.BatchAiVideoSplitBody;
import com.ruoyi.batch.aivideo.service.IBatchAiVideoService;

/**
 * APP 端 AI 云创视频接口
 */
@Validated
@RestController
@RequestMapping("/batch/ai/video")
public class BatchAiVideoController extends BaseController
{
    /** 允许上传的视频格式 */
    private static final String[] VIDEO_EXTENSIONS = { "mp4", "mov", "avi", "flv", "mkv", "webm" };

    @Autowired
    private IBatchAiVideoService aiVideoService;

    /**
     * 查询当前账号视频组列表
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @GetMapping("/group/list")
    public TableDataInfo list()
    {
        String phone = getLoginUser().getUsername();
        startPage();
        List<BatchAiVideoGroup> list = aiVideoService.selectBatchAiVideoGroupList(phone);
        return getDataTable(list);
    }

    /**
     * 获取视频组详情（含分镜头）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @GetMapping("/group/{groupId}")
    public AjaxResult getInfo(@PathVariable("groupId") Long groupId)
    {
        BatchAiVideoGroup group = aiVideoService.selectBatchAiVideoGroupById(groupId);
        checkGroupOwner(group);
        return success(group);
    }

    /**
     * 新增视频组
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @PostMapping("/group")
    public AjaxResult add(@Valid @RequestBody BatchAiVideoGroup group)
    {
        String phone = getLoginUser().getUsername();
        group.setPhone(phone);
        int rows = aiVideoService.insertBatchAiVideoGroup(group);
        AjaxResult ajax = toAjax(rows);
        ajax.put("groupId", group.getGroupId());
        return ajax;
    }

    /**
     * 修改视频组（含分镜头覆盖保存）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @PutMapping("/group")
    public AjaxResult edit(@RequestBody BatchAiVideoGroup group)
    {
        BatchAiVideoGroup dbGroup = aiVideoService.selectBatchAiVideoGroupById(group.getGroupId());
        checkGroupOwner(dbGroup);
        return toAjax(aiVideoService.updateBatchAiVideoGroup(group));
    }

    /**
     * 删除视频组
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @DeleteMapping("/group/{groupId}")
    public AjaxResult remove(@PathVariable("groupId") Long groupId)
    {
        BatchAiVideoGroup group = aiVideoService.selectBatchAiVideoGroupById(groupId);
        checkGroupOwner(group);
        return toAjax(aiVideoService.deleteBatchAiVideoGroupById(groupId));
    }

    /**
     * 视频素材上传（存 profile/video/upload，返回可访问完整 URL）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return AjaxResult.error("上传文件不能为空");
        }
        String fileName = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/video/upload", file, VIDEO_EXTENSIONS);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("url", buildFileUrl(fileName));
        return ajax;
    }

    /**
     * AI 分割：将已上传视频按指定时长切成多段并保存为该组分镜头
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @PostMapping("/split")
    public AjaxResult split(@Valid @RequestBody BatchAiVideoSplitBody body)
    {
        String phone = getLoginUser().getUsername();
        List<BatchAiVideoClip> clips = aiVideoService.splitVideo(phone, body);
        return AjaxResult.success(clips);
    }

    /**
     * 提交 AI 视频批量生成（异步合成，通过任务查询接口轮询进度）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @PostMapping("/generate")
    public AjaxResult generate(@Valid @RequestBody BatchAiVideoGenerateBody body)
    {
        String phone = getLoginUser().getUsername();
        List<Long> logIds = aiVideoService.submitGenerate(phone, body);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("logId", logIds.isEmpty() ? null : logIds.get(0));
        ajax.put("logIds", logIds);
        return ajax;
    }

    /**
     * 生成任务列表（APP 轮询，按时间倒序）
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @GetMapping("/task/list")
    public AjaxResult taskList(@RequestParam("groupId") Long groupId)
    {
        BatchAiVideoGroup group = aiVideoService.selectBatchAiVideoGroupById(groupId);
        checkGroupOwner(group);
        BatchAiVideoGenerateLog query = new BatchAiVideoGenerateLog();
        query.setGroupId(groupId);
        query.setPhone(getLoginUser().getUsername());
        return success(aiVideoService.selectBatchAiVideoGenerateLogList(query));
    }

    /**
     * 单条生成任务详情
     */
    @PreAuthorize("@ss.hasPermi('app:user')")
    @GetMapping("/task/{logId}")
    public AjaxResult taskInfo(@PathVariable("logId") Long logId)
    {
        BatchAiVideoGenerateLog task = aiVideoService.selectBatchAiVideoGenerateLogById(logId);
        if (task == null)
        {
            return AjaxResult.error("生成记录不存在");
        }
        if (!SecurityUtils.isAdmin() && !getLoginUser().getUsername().equals(task.getPhone()))
        {
            throw new ServiceException("无权查看该生成记录");
        }
        return success(task);
    }

    /**
     * 生成记录分页列表（管理后台）
     */
    @PreAuthorize("@ss.hasPermi('batch:aivideo:list')")
    @GetMapping("/log/list")
    public TableDataInfo logList(BatchAiVideoGenerateLog query)
    {
        startPage();
        List<BatchAiVideoGenerateLog> list = aiVideoService.selectBatchAiVideoGenerateLogList(query);
        return getDataTable(list);
    }

    /**
     * 校验视频组归属（APP 用户只能操作自己的组，admin 不受限）
     */
    private void checkGroupOwner(BatchAiVideoGroup group)
    {
        if (group == null)
        {
            throw new ServiceException("视频组不存在");
        }
        if (!SecurityUtils.isAdmin() && !getLoginUser().getUsername().equals(group.getPhone()))
        {
            throw new ServiceException("无权操作该视频组");
        }
    }

    /**
     * 根据文件名构造完整访问 URL（参照 /batch/app/upload）
     */
    private String buildFileUrl(String fileName)
    {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null)
        {
            return fileName;
        }
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length())
                .append(contextPath).append(fileName).toString();
    }
}
