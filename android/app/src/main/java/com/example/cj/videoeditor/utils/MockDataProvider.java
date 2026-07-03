package com.example.cj.videoeditor.utils;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Announcement;
import com.example.cj.videoeditor.bean.Banner;
import com.example.cj.videoeditor.bean.Brand;
import com.example.cj.videoeditor.bean.Clip;
import com.example.cj.videoeditor.bean.ContactPerson;
import com.example.cj.videoeditor.bean.CustomerService;
import com.example.cj.videoeditor.bean.Document;
import com.example.cj.videoeditor.bean.HomeEntry;
import com.example.cj.videoeditor.bean.LearningMaterial;
import com.example.cj.videoeditor.bean.UserInfo;
import com.example.cj.videoeditor.bean.VideoGroup;
import com.example.cj.videoeditor.bean.WatermarkResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockDataProvider {

    private MockDataProvider() {}

    public static List<Banner> getBanners() {
        List<Banner> list = new ArrayList<>();
        list.add(new Banner("1", "https://placehold.co/600x300/2196F3/FFFFFF?text=Banner1", "", 1));
        list.add(new Banner("2", "https://placehold.co/600x300/1976D2/FFFFFF?text=Banner2", "", 2));
        list.add(new Banner("3", "https://placehold.co/600x300/64B5F6/FFFFFF?text=Banner3", "", 3));
        return list;
    }

    public static Announcement getAnnouncement() {
        return new Announcement("恭喜华东区超额完成 Q2 目标！", "王小明", "586000");
    }

    public static List<HomeEntry> getHomeEntries() {
        List<HomeEntry> list = new ArrayList<>();
        list.add(new HomeEntry("学习专区", R.drawable.ic_ai_creation));
        list.add(new HomeEntry("信息咨询", R.drawable.ic_watermark));
        list.add(new HomeEntry("品牌专区", R.drawable.ic_profile));
        list.add(new HomeEntry("其他服务", R.drawable.ic_home));
        return list;
    }

    public static Banner getTutorialEntry() {
        return new Banner("tutorial", "https://placehold.co/600x200/2196F3/FFFFFF?text=新手指南", "", 1);
    }

    public static List<LearningMaterial> getLearningMaterials(String type) {
        List<LearningMaterial> list = new ArrayList<>();
        if ("全部".equals(type) || "视频".equals(type)) {
            list.add(new LearningMaterial("v1", "快速入门：AI 去水印", "视频",
                    "https://placehold.co/200x120/2196F3/FFFFFF?text=Video1", "",
                    "2024-06-01", 1200));
        }
        if ("全部".equals(type) || "文档".equals(type)) {
            list.add(new LearningMaterial("d1", "账号层级说明", "文档",
                    "https://placehold.co/200x120/757575/FFFFFF?text=Doc1", "",
                    "2024-06-02", 800));
        }
        if ("全部".equals(type) || "PDF".equals(type)) {
            list.add(new LearningMaterial("p1", "批量剪辑 使用手册", "PDF",
                    "https://placehold.co/200x120/FB8C00/FFFFFF?text=PDF1", "",
                    "2024-06-03", 500));
        }
        return list;
    }

    public static List<String> getLearningTabs() {
        return Arrays.asList("全部", "视频", "文档", "PDF");
    }

    public static CustomerService getCustomerService() {
        return new CustomerService("400-888-6666", "早 9:00-18:00", "");
    }

    public static List<ContactPerson> getContactPersons() {
        List<ContactPerson> list = new ArrayList<>();
        list.add(new ContactPerson("张伟", "华东区", "13800138001"));
        list.add(new ContactPerson("李娜", "华北区", "13800138002"));
        list.add(new ContactPerson("王强", "华南区", "13800138003"));
        return list;
    }

    public static List<Brand> getBrands() {
        List<Brand> list = new ArrayList<>();
        list.add(new Brand("1", "批量剪辑", "",
                "批量剪辑 AI 短视频创作品牌",
                "批量剪辑致力于通过 AI 技术降低短视频创作门槛，提供去水印、AI 分割、批量生成等能力。",
                Arrays.asList("https://placehold.co/400x300/2196F3/FFFFFF?text=Brand1")));
        list.add(new Brand("2", "星辰传媒", "",
                "品牌宣传与短视频运营服务商",
                "星辰传媒提供全链路短视频运营服务，与批量剪辑 AI 深度合作。",
                Arrays.asList("https://placehold.co/400x300/1976D2/FFFFFF?text=Brand2")));
        return list;
    }

    public static List<String> getDocumentTabs() {
        return Arrays.asList("快速上手", "操作指南", "常见问题");
    }

    public static List<Document> getDocuments(String category) {
        List<Document> list = new ArrayList<>();
        if ("快速上手".equals(category)) {
            list.add(new Document("q1", "如何快速去除视频水印", "快速上手", "2024-06-01",
                    "1. 复制短视频平台分享链接；\n2. 打开 AI 去水印页面粘贴链接；\n3. 点击解析并等待处理完成；\n4. 预览去水印结果后点击保存。"));
        } else if ("操作指南".equals(category)) {
            list.add(new Document("o1", "AI 云创分镜头管理", "操作指南", "2024-06-02",
                    "在 AI 云创页面，您可以创建视频组并上传素材，系统自动分割成分镜头。支持拖拽排序、镜像翻转和替换素材。"));
        } else {
            list.add(new Document("f1", "算力不足怎么办？", "常见问题", "2024-06-03",
                    "当算力耗尽时，视频生成与下载功能将被禁用，请联系管理员增加算力额度。"));
        }
        return list;
    }

    public static List<VideoGroup> getVideoGroups() {
        List<VideoGroup> list = new ArrayList<>();
        list.add(new VideoGroup("g1", "夏季新品推广", "2024-06-10", 3, 10));
        list.add(new VideoGroup("g2", "品牌故事混剪", "2024-06-08", 0, 8));
        list.add(new VideoGroup("g3", "达人带货素材", "2024-06-05", 5, 5));
        return list;
    }

    public static List<Clip> getClips() {
        List<Clip> list = new ArrayList<>();
        list.add(new Clip("c1", 1, "", false));
        list.add(new Clip("c2", 2, "", false));
        list.add(new Clip("c3", 3, "", false));
        return list;
    }

    public static WatermarkResult getWatermarkResult() {
        return new WatermarkResult(
                "https://placehold.co/400x700/000000/FFFFFF?text=Video",
                Arrays.asList(
                        "https://placehold.co/200x200/2196F3/FFFFFF?text=Frame1",
                        "https://placehold.co/200x200/1976D2/FFFFFF?text=Frame2",
                        "https://placehold.co/200x200/64B5F6/FFFFFF?text=Frame3"),
                "批量剪辑，让短视频创作更简单。 #批量剪辑 #AI创作"
        );
    }

    public static UserInfo getUserInfo() {
        return new UserInfo("15912341415", "批量剪辑用户", "",
                true, "2029-05-24", 1000, 356);
    }

    public static String getUserAgreement() {
        return "<h2>用户协议</h2>"
                + "<p>欢迎使用批量剪辑 AI。本协议为您与批量剪辑平台之间就使用本 APP 服务所订立的协议。</p>"
                + "<p>1. 您在使用本服务时应遵守国家法律法规，不得利用本服务从事违法违规行为。</p>"
                + "<p>2. 批量剪辑平台有权根据运营需要调整服务内容，并将在合理范围内提前通知。</p>"
                + "<p>3. 您应妥善保管账号信息，因保管不善导致的损失由您自行承担。</p>"
                + "<p>4. 本协议最终解释权归批量剪辑平台所有。</p>";
    }

    public static String getPrivacyPolicy() {
        return "<h2>隐私政策</h2>"
                + "<p>批量剪辑 AI 重视您的个人信息保护。本政策将说明我们如何收集、使用和保护您的个人信息。</p>"
                + "<p>1. 我们收集的信息包括您注册时提供的手机号、昵称以及使用服务过程中产生的日志信息。</p>"
                + "<p>2. 我们仅将您的信息用于提供服务、安全保障及改进产品体验，不会未经授权向第三方披露。</p>"
                + "<p>3. 您可以通过 APP 内的账号管理功能查阅、更正或删除您的个人信息。</p>"
                + "<p>4. 如您对本政策有任何疑问，请联系客服。</p>";
    }
}