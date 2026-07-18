package com.ruoyi.batch.aivideo.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;

/**
 * FFmpeg 视频处理服务（AI 分割、批量合成、随机化去重）
 */
@Service
public class BatchFfmpegService
{
    private static final Logger log = LoggerFactory.getLogger(BatchFfmpegService.class);

    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d+):(\\d+):(\\d+\\.\\d+)");

    /** 单次 ffmpeg 调用超时（分钟） */
    private static final int CMD_TIMEOUT_MINUTES = 30;

    /** FFmpeg 可执行文件路径，默认 ffmpeg（从 PATH 查找） */
    @Value("${batch.video.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    /** 可用性检测结果（缓存） */
    private volatile Boolean available;

    @PostConstruct
    public void init()
    {
        if (isAvailable())
        {
            log.info("FFmpeg 检测通过: {}", ffmpegPath);
        }
        else
        {
            log.error("FFmpeg 不可用（路径: {}），AI 视频分割/生成功能将失败，请配置 batch.video.ffmpeg-path 或安装 ffmpeg", ffmpegPath);
        }
    }

    /**
     * 检测 ffmpeg 是否可用
     */
    public boolean isAvailable()
    {
        if (available == null)
        {
            synchronized (this)
            {
                if (available == null)
                {
                    try
                    {
                        run(buildCmd(ffmpegPath, "-version"), 1);
                        available = true;
                    }
                    catch (Exception e)
                    {
                        log.error("FFmpeg 可用性检测失败: {}", e.getMessage());
                        available = false;
                    }
                }
            }
        }
        return available;
    }

    /**
     * 将 /profile/** 形式的 URL（或完整 URL）解析为本地文件
     */
    public File resolveLocalFile(String url)
    {
        if (url == null)
        {
            throw new ServiceException("视频地址不能为空");
        }
        int idx = url.indexOf(Constants.RESOURCE_PREFIX + "/");
        if (idx < 0)
        {
            throw new ServiceException("仅支持本站已上传的视频素材: " + url);
        }
        String rel = url.substring(idx + Constants.RESOURCE_PREFIX.length());
        File file = new File(RuoYiConfig.getProfile() + rel);
        if (!file.exists())
        {
            throw new ServiceException("视频文件不存在: " + url);
        }
        return file;
    }

    /**
     * 将 profile 目录下的文件转换为可访问 URL（/profile/** 开头）
     */
    public String toUrl(File file)
    {
        String profile = new File(RuoYiConfig.getProfile()).getAbsolutePath();
        String path = file.getAbsolutePath();
        String rel = path.substring(profile.length()).replace('\\', '/');
        return Constants.RESOURCE_PREFIX + rel;
    }

    /**
     * 获取视频时长（秒）
     */
    public double probeDuration(File file) throws IOException
    {
        String output = runExpectFail(buildCmd(ffmpegPath, "-i", file.getAbsolutePath()), 5);
        Matcher m = DURATION_PATTERN.matcher(output);
        if (!m.find())
        {
            throw new IOException("无法解析视频时长: " + file.getName());
        }
        double duration = Long.parseLong(m.group(1)) * 3600L
                + Long.parseLong(m.group(2)) * 60L
                + Double.parseDouble(m.group(3));
        if (duration <= 0)
        {
            throw new IOException("视频时长异常: " + file.getName());
        }
        return duration;
    }

    /**
     * 视频是否包含音频流
     */
    public boolean hasAudio(File file)
    {
        try
        {
            String output = runExpectFail(buildCmd(ffmpegPath, "-i", file.getAbsolutePath()), 5);
            return output.contains("Audio:");
        }
        catch (Exception e)
        {
            log.warn("音频流检测失败，按无音频处理: {}", file.getName());
            return false;
        }
    }

    /**
     * 按固定时长将视频切为多段，返回每段文件与实际时长
     */
    public List<Segment> split(File input, double sliceDuration, File outDir) throws IOException
    {
        double total = probeDuration(input);
        int count = (int) Math.ceil(total / sliceDuration);
        if (count <= 0)
        {
            throw new ServiceException("视频时长过短，无法分割");
        }
        if (!outDir.exists() && !outDir.mkdirs())
        {
            throw new IOException("无法创建切片目录: " + outDir.getAbsolutePath());
        }
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            double start = i * sliceDuration;
            double expectDuration = Math.min(sliceDuration, total - start);
            File out = new File(outDir, String.format(Locale.US, "seg_%03d.mp4", i + 1));
            List<String> cmd = buildCmd(ffmpegPath, "-y", "-ss", fmt(start), "-t", fmt(sliceDuration),
                    "-i", input.getAbsolutePath(),
                    "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p",
                    "-c:a", "aac", "-b:a", "128k",
                    out.getAbsolutePath());
            run(cmd, CMD_TIMEOUT_MINUTES);
            double actual;
            try
            {
                actual = probeDuration(out);
            }
            catch (Exception e)
            {
                actual = expectDuration;
            }
            segments.add(new Segment(out, actual));
        }
        return segments;
    }

    /**
     * 合成一个产出视频：随机选段乱序 + 每段随机变速/镜像/色彩抖动/CRF，拼接为 h264+aac mp4
     *
     * @param sources    素材段文件
     * @param outFile    输出文件
     * @param seed       随机种子
     * @param progressCb 进度回调（0-95，预留 5% 给收尾）
     * @return 输出文件 MD5
     */
    public String compose(List<File> sources, File outFile, long seed, IntConsumer progressCb) throws IOException
    {
        Random random = new Random(seed);
        List<File> selected = pickSegments(sources, sources.size(), random);

        File workDir = new File(outFile.getParentFile(), "tmp_" + outFile.getName().replace(".mp4", ""));
        if (!workDir.exists() && !workDir.mkdirs())
        {
            throw new IOException("无法创建临时目录: " + workDir.getAbsolutePath());
        }
        try
        {
            List<File> normalized = new ArrayList<>();
            int total = selected.size();
            for (int i = 0; i < total; i++)
            {
                File seg = selected.get(i);
                // 每段独立随机化：0.92~1.08 变速、50% 水平镜像、亮度/对比度 ±5% 抖动、CRF 20~26
                double speed = 0.92 + random.nextDouble() * 0.16;
                boolean flip = random.nextBoolean();
                double brightness = (random.nextDouble() - 0.5) * 0.1;
                double contrast = 0.95 + random.nextDouble() * 0.1;
                int crf = 20 + random.nextInt(7);

                StringBuilder vf = new StringBuilder(
                        "scale=720:1280:force_original_aspect_ratio=decrease,"
                                + "pad=720:1280:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30,setpts=PTS/" + fmt(speed));
                if (flip)
                {
                    vf.append(",hflip");
                }
                vf.append(",eq=brightness=").append(fmt(brightness)).append(":contrast=").append(fmt(contrast));

                File tmp = new File(workDir, String.format(Locale.US, "part_%03d.mp4", i));
                List<String> cmd = new ArrayList<>();
                cmd.add(ffmpegPath);
                cmd.add("-y");
                cmd.add("-i");
                cmd.add(seg.getAbsolutePath());
                boolean audio = hasAudio(seg);
                if (!audio)
                {
                    cmd.add("-f");
                    cmd.add("lavfi");
                    cmd.add("-i");
                    cmd.add("anullsrc=channel_layout=stereo:sample_rate=44100");
                }
                cmd.add("-vf");
                cmd.add(vf.toString());
                if (audio)
                {
                    cmd.add("-af");
                    cmd.add("atempo=" + fmt(speed) + ",aformat=sample_rates=44100:channel_layouts=stereo");
                    cmd.add("-map");
                    cmd.add("0:v");
                    cmd.add("-map");
                    cmd.add("0:a");
                }
                else
                {
                    cmd.add("-map");
                    cmd.add("0:v");
                    cmd.add("-map");
                    cmd.add("1:a");
                    cmd.add("-shortest");
                }
                cmd.add("-c:v");
                cmd.add("libx264");
                cmd.add("-preset");
                cmd.add("veryfast");
                cmd.add("-crf");
                cmd.add(String.valueOf(crf));
                cmd.add("-pix_fmt");
                cmd.add("yuv420p");
                cmd.add("-c:a");
                cmd.add("aac");
                cmd.add("-b:a");
                cmd.add("128k");
                cmd.add(tmp.getAbsolutePath());
                run(cmd, CMD_TIMEOUT_MINUTES);
                normalized.add(tmp);
                if (progressCb != null)
                {
                    progressCb.accept((int) ((i + 1) * 95L / total));
                }
            }

            // concat 拼接（各段编码参数一致，可直接 copy）
            File listFile = new File(workDir, "concat.txt");
            StringBuilder sb = new StringBuilder();
            for (File f : normalized)
            {
                sb.append("file '").append(f.getAbsolutePath().replace('\\', '/')).append("'\n");
            }
            Files.write(listFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            run(buildCmd(ffmpegPath, "-y", "-f", "concat", "-safe", "0", "-i", listFile.getAbsolutePath(),
                    "-c", "copy", "-movflags", "+faststart", outFile.getAbsolutePath()), CMD_TIMEOUT_MINUTES);

            return md5(outFile);
        }
        finally
        {
            deleteQuietly(workDir);
        }
    }

    /**
     * 从素材中随机抽取 need 段并乱序：多于需求时随机子集，少于需求时重复利用但顺序打乱
     */
    private List<File> pickSegments(List<File> sources, int need, Random random)
    {
        List<File> pool = new ArrayList<>(sources);
        List<File> result = new ArrayList<>();
        while (result.size() < need)
        {
            Collections.shuffle(pool, random);
            for (File f : pool)
            {
                if (result.size() >= need)
                {
                    break;
                }
                result.add(f);
            }
        }
        Collections.shuffle(result, random);
        return result;
    }

    /**
     * 文件 MD5
     */
    public String md5(File file) throws IOException
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (FileInputStream in = new FileInputStream(file))
            {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    digest.update(buf, 0, len);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest())
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new IOException("MD5 计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行命令，非 0 退出码抛异常（输出尾部作为错误信息）
     */
    private String run(List<String> cmd, int timeoutMinutes) throws IOException
    {
        return exec(cmd, timeoutMinutes, false);
    }

    /**
     * 执行命令，允许非 0 退出码（如 ffmpeg -i 探测场景，退出码为 1 但输出有效）
     */
    private String runExpectFail(List<String> cmd, int timeoutMinutes) throws IOException
    {
        return exec(cmd, timeoutMinutes, true);
    }

    private String exec(List<String> cmd, int timeoutMinutes, boolean allowFail) throws IOException
    {
        Process process = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append('\n');
                }
            }
            if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES))
            {
                process.destroyForcibly();
                throw new IOException("FFmpeg 执行超时: " + cmd.get(0));
            }
            int exit = process.exitValue();
            if (exit != 0 && !allowFail)
            {
                String tail = output.length() > 500 ? output.substring(output.length() - 500) : output.toString();
                throw new IOException("FFmpeg 执行失败(exit=" + exit + "): " + tail.trim());
            }
            return output.toString();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg 执行被中断", e);
        }
        finally
        {
            if (process != null && process.isAlive())
            {
                process.destroyForcibly();
            }
        }
    }

    private List<String> buildCmd(String... args)
    {
        List<String> cmd = new ArrayList<>();
        Collections.addAll(cmd, args);
        return cmd;
    }

    /** 数字格式化（统一小数点，避免本地化逗号） */
    private String fmt(double value)
    {
        return String.format(Locale.US, "%.2f", value);
    }

    private void deleteQuietly(File dir)
    {
        try
        {
            File[] files = dir.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    Files.deleteIfExists(f.toPath());
                }
            }
            Files.deleteIfExists(dir.toPath());
        }
        catch (Exception e)
        {
            log.warn("临时目录清理失败: {}", dir.getAbsolutePath());
        }
    }

    /**
     * 切片结果
     */
    public static class Segment
    {
        private final File file;
        private final double duration;

        public Segment(File file, double duration)
        {
            this.file = file;
            this.duration = duration;
        }

        public File getFile()
        {
            return file;
        }

        public double getDuration()
        {
            return duration;
        }
    }
}
