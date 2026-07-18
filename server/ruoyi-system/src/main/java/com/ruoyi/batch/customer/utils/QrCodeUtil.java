package com.ruoyi.batch.customer.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;

/**
 * 二维码生成工具
 *
 * @author ruoyi
 */
@Component
public class QrCodeUtil
{
    /** 扫码统计回调路径，扫码后计入统计并 302 跳转到 APP 下载地址 */
    private static final String SCAN_PATH = "/batch/qrcode/scan?phone=";

    /** 服务对外访问地址（域名+端口），用于拼接二维码内容URL与图片访问URL */
    @Value("${batch.app.server-url:http://localhost:8080}")
    private String serverUrl;

    /**
     * 生成带邀请手机号的注册二维码并上传到文件服务器
     *
     * @param phone 邀请人手机号
     * @return 二维码图片访问 URL
     */
    public String generateQrCode(String phone)
    {
        try
        {
            String content = buildQrContent(phone);
            BufferedImage image = createQrImage(content, 300, 300);
            byte[] imageBytes = imageToBytes(image, "png");
            String fileName = uploadQrCode(imageBytes, phone);
            return getServerUrl() + fileName;
        }
        catch (Exception e)
        {
            throw new RuntimeException("二维码生成失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取服务对外访问地址（配置项 batch.app.server-url）
     */
    public String getServerUrl()
    {
        return serverUrl;
    }

    /**
     * 构建二维码内容：指向扫码统计接口，扫码后由服务端统计并跳转下载地址
     */
    public String buildQrContent(String phone)
    {
        return getServerUrl() + SCAN_PATH + phone;
    }

    /**
     * 生成二维码图片
     */
    private BufferedImage createQrImage(String content, int width, int height) throws Exception
    {
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 图片转字节数组
     */
    private byte[] imageToBytes(BufferedImage image, String format) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 上传二维码图片
     */
    private String uploadQrCode(byte[] imageBytes, String phone) throws Exception
    {
        String filePath = RuoYiConfig.getUploadPath();
        MultipartFile multipartFile = new ByteArrayMultipartFile(imageBytes, "qrcode_" + phone + ".png", "image/png");
        return FileUploadUtils.upload(filePath, multipartFile, new String[] { "png", "jpg", "jpeg" });
    }

    /**
     * 基于字节数组的简单 MultipartFile 实现
     */
    public static class ByteArrayMultipartFile implements MultipartFile
    {
        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String originalFilename, String contentType)
        {
            this.content = content;
            this.name = originalFilename;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getOriginalFilename()
        {
            return originalFilename;
        }

        @Override
        public String getContentType()
        {
            return contentType;
        }

        @Override
        public boolean isEmpty()
        {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize()
        {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException
        {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException
        {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}
