from pathlib import Path
root = Path('/d/project/batch-video-editor/batch-video-editor/app/src/main/java/com/example/cj/videoeditor')

def write(path, content):
    p = root / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding='utf-8')
    print('Wrote', p)

write('utils/AppConfig.java', r'''package com.example.cj.videoeditor.utils;

public class AppConfig {

    public static final String BASE_URL = "http://10.0.2.2:8080";
    public static final boolean MOCK_MODE = true;

    public static final String SP_TOKEN = "token";
    public static final String SP_USER = "user";
    public static final String SP_LOGIN = "is_login";

    public static final String SP_USER_PHONE = "user_phone";
    public static final String SP_USER_NICKNAME = "user_nickname";
    public static final String SP_USER_AVATAR = "user_avatar";
    public static final String SP_USER_VIP = "user_vip";
    public static final String SP_USER_VIP_EXPIRE = "user_vip_expire";

    public static final String SP_COMPUTE_TOTAL = "compute_total";
    public static final String SP_COMPUTE_USED = "compute_used";
}
''')

write('bean/UserInfo.java', r'''package com.example.cj.videoeditor.bean;

public class UserInfo {
    public String phone;
    public String nickname;
    public String avatar;
    public boolean vip;
    public String vipExpire;

    public UserInfo() {}

    public UserInfo(String phone, String nickname, String avatar, boolean vip, String vipExpire) {
        this.phone = phone;
        this.nickname = nickname;
        this.avatar = avatar;
        this.vip = vip;
        this.vipExpire = vipExpire;
    }
}
''')

write('bean/Banner.java', r'''package com.example.cj.videoeditor.bean;

public class Banner {
    public String id;
    public String imageUrl;
    public String link;
    public String title;

    public Banner(String id, String imageUrl, String link, String title) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.link = link;
        this.title = title;
    }
}
''')

write('bean/Announcement.java', r'''package com.example.cj.videoeditor.bean;

public class Announcement {
    public String title;
    public String championName;
    public String amount;

    public Announcement(String title, String championName, String amount) {
        this.title = title;
        this.championName = championName;
        this.amount = amount;
    }
}
''')

write('bean/HomeMenu.java', r'''package com.example.cj.videoeditor.bean;

public class HomeMenu {
    public String title;
    public int iconRes;
    public String action;

    public HomeMenu(String title, int iconRes, String action) {
        this.title = title;
        this.iconRes = iconRes;
        this.action = action;
    }
}
''')

write('bean/Brand.java', r'''package com.example.cj.videoeditor.bean;

import java.util.List;

public class Brand {
    public String id;
    public String name;
    public String logo;
    public String desc;
    public String detail;
    public List<String> images;

    public Brand(String id, String name, String logo, String desc, String detail, List<String> images) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.desc = desc;
        this.detail = detail;
        this.images = images;
    }
}
''')

write('bean/Contact.java', r'''package com.example.cj.videoeditor.bean;

public class Contact {
    public String name;
    public String area;
    public String phone;

    public Contact(String name, String area, String phone) {
        this.name = name;
        this.area = area;
        this.phone = phone;
    }
}
''')

write('bean/ContactResult.java', r'''package com.example.cj.videoeditor.bean;

import java.util.List;

public class ContactResult {
    public String onlineName;
    public String onlineLink;
    public String phone;
    public String serviceTime;
    public List<Contact> contacts;

    public ContactResult(String onlineName, String onlineLink, String phone, String serviceTime, List<Contact> contacts) {
        this.onlineName = onlineName;
        this.onlineLink = onlineLink;
        this.phone = phone;
        this.serviceTime = serviceTime;
        this.contacts = contacts;
    }
}
''')

write('bean/Learning.java', r'''package com.example.cj.videoeditor.bean;

public class Learning {
    public String id;
    public String title;
    public String type;
    public String cover;
    public String url;
    public String time;
    public int views;

    public Learning(String id, String title, String type, String cover, String url, String time, int views) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.cover = cover;
        this.url = url;
        this.time = time;
        this.views = views;
    }
}
''')

write('bean/Document.java', r'''package com.example.cj.videoeditor.bean;

public class Document {
    public String id;
    public String title;
    public String category;
    public String updateTime;
    public String content;

    public Document(String id, String title, String category, String updateTime, String content) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.updateTime = updateTime;
        this.content = content;
    }
}
''')

write('bean/VideoGroup.java', r'''package com.example.cj.videoeditor.bean;

public class VideoGroup {
    public String id;
    public String name;
    public String createDate;
    public int generatedCount;
    public int maxCount;

    public VideoGroup(String id, String name, String createDate, int generatedCount, int maxCount) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.generatedCount = generatedCount;
        this.maxCount = maxCount;
    }
}
''')

write('bean/Clip.java', r'''package com.example.cj.videoeditor.bean;

public class Clip {
    public String id;
    public int index;
    public String videoUrl;
    public boolean mirror;

    public Clip(String id, int index, String videoUrl, boolean mirror) {
        this.id = id;
        this.index = index;
        this.videoUrl = videoUrl;
        this.mirror = mirror;
    }
}
''')

write('bean/WatermarkResult.java', r'''package com.example.cj.videoeditor.bean;

import java.util.List;

public class WatermarkResult {
    public String videoUrl;
    public List<String> images;
    public String text;

    public WatermarkResult(String videoUrl, List<String> images, String text) {
        this.videoUrl = videoUrl;
        this.images = images;
        this.text = text;
    }
}
''')

write('bean/CustomerService.java', r'''package com.example.cj.videoeditor.bean;

public class CustomerService {
    public String phone;
    public String serviceTime;
    public String onlineLink;

    public CustomerService(String phone, String serviceTime, String onlineLink) {
        this.phone = phone;
        this.serviceTime = serviceTime;
        this.onlineLink = onlineLink;
    }
}
''')

write('mock/MockDataProvider.java', r'''package com.example.cj.videoeditor.mock;

import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.bean.Announcement;
import com.example.cj.videoeditor.bean.Banner;
import com.example.cj.videoeditor.bean.Brand;
import com.example.cj.videoeditor.bean.Contact;
import com.example.cj.videoeditor.bean.ContactResult;
import com.example.cj.videoeditor.bean.CustomerService;
import com.example.cj.videoeditor.bean.Document;
import com.example.cj.videoeditor.bean.HomeMenu;
import com.example.cj.videoeditor.bean.Learning;
import com.example.cj.videoeditor.bean.VideoGroup;
import com.example.cj.videoeditor.bean.WatermarkResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockDataProvider {

    public static List<Banner> getBanners() {
        List<Banner> list = new ArrayList<>();
        list.add(new Banner("1", "https://example.com/banner1.jpg", "", "品牌宣传"));
        list.add(new Banner("2", "https://example.com/banner2.jpg", "", "活动海报"));
        list.add(new Banner("3", "https://example.com/banner3.jpg", "", "新手教程"));
        return list;
    }

    public static Announcement getAnnouncement() {
        return new Announcement("恭喜华东区超额完成 Q2 目标！", "王小明", "586000");
    }

    public static List<HomeMenu> getHomeMenus() {
        List<HomeMenu> list = new ArrayList<>();
        list.add(new HomeMenu("学习专区", R.drawable.ic_home, "learning"));
        list.add(new HomeMenu("信息咨询", R.drawable.ic_profile, "contact"));
        list.add(new HomeMenu("品牌专区", R.drawable.ic_ai_creation, "brand"));
        list.add(new HomeMenu("其他服务", R.drawable.ic_watermark, "other"));
        return list;
    }

    public static List<Learning> getLearningMaterials(String type) {
        List<Learning> all = new ArrayList<>();
        all.add(new Learning("1", "批量剪辑快速上手指南", "doc", "", "", "2025-06-20", 1200));
        all.add(new Learning("2", "AI 云创操作全流程", "video", "", "", "2025-06-18", 980));
        all.add(new Learning("3", "去水印功能演示", "video", "", "", "2025-06-15", 760));
        all.add(new Learning("4", "VIP 会员权益说明", "pdf", "", "", "2025-06-10", 540));
        if ("全部".equals(type)) return all;
        List<Learning> result = new ArrayList<>();
        for (Learning item : all) {
            if (("视频".equals(type) && "video".equals(item.type)) ||
                    ("文档".equals(type) && ("doc".equals(item.type) || "pdf".equals(item.type)))) {
                result.add(item);
            }
        }
        return result;
    }

    public static ContactResult getContacts() {
        List<Contact> list = new ArrayList<>();
        list.add(new Contact("张经理", "华东区", "13800138001"));
        list.add(new Contact("李经理", "华南区", "13800138002"));
        list.add(new Contact("王经理", "华北区", "13800138003"));
        return new ContactResult("在线客服", "https://example.com/kefu", "400-888-8888", "早 9:00-18:00", list);
    }

    public static List<Brand> getBrands() {
        List<Brand> list = new ArrayList<>();
        list.add(new Brand("1", "批量剪辑", "", "批量剪辑 AI 短视频创作平台，助力品牌数字化营销。", "批量剪辑 AI 提供短视频创作、去水印、AI 分割、配音等一站式能力。", Arrays.asList("https://example.com/b1.jpg")));
        list.add(new Brand("2", "合作品牌 A", "", "专注于智能视频处理技术。", "合作品牌 A 提供底层视频编解码能力。", new ArrayList<>()));
        list.add(new Brand("3", "合作品牌 B", "", "领先的 AI 语音合成服务商。", "合作品牌 B 提供高质量文字转语音服务。", new ArrayList<>()));
        return list;
    }

    public static List<Document> getDocuments(String category) {
        List<Document> all = new ArrayList<>();
        all.add(new Document("1", "如何注册与登录", "快速上手", "2025-06-20", "打开 APP 后，输入手机号和密码完成登录，新用户可通过扫码注册。"));
        all.add(new Document("2", "AI 云创使用说明", "操作指南", "2025-06-18", "在 AI 云创页面上传视频素材，选择分镜头时长后点击 AI 生成。"));
        all.add(new Document("3", "去水印常见问题", "常见问题", "2025-06-15", "请粘贴完整分享链接，目前支持抖音、小红书等平台。"));
        all.add(new Document("4", "账号与安全", "操作指南", "2025-06-10", "请妥善保管密码，定期修改以保障账号安全。"));
        if ("全部".equals(category)) return all;
        List<Document> result = new ArrayList<>();
        for (Document item : all) {
            if (item.category.equals(category)) result.add(item);
        }
        return result;
    }

    public static List<VideoGroup> getVideoGroups() {
        List<VideoGroup> list = new ArrayList<>();
        list.add(new VideoGroup("1", "夏季新品宣传", "2025-06-20", 3, 10));
        list.add(new VideoGroup("2", "品牌故事系列", "2025-06-18", 0, 8));
        list.add(new VideoGroup("3", "活动促销合集", "2025-06-15", 5, 12));
        return list;
    }

    public static WatermarkResult getWatermarkResult() {
        return new WatermarkResult("https://example.com/video_no_watermark.mp4",
                Arrays.asList("https://example.com/frame1.jpg", "https://example.com/frame2.jpg"),
                "这是一段精彩的短视频文案，适合用于品牌宣传。");
    }

    public static CustomerService getCustomerService() {
        return new CustomerService("400-888-8888", "早 9:00-18:00", "https://example.com/kefu");
    }
}
''')

write('utils/PowerUtil.java', r'''package com.example.cj.videoeditor.utils;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class PowerUtil {

    public static boolean isPowerExhausted(Context context) {
        int total = SharedPrefUtil.getInt(context, AppConfig.SP_COMPUTE_TOTAL, 1000);
        int used = SharedPrefUtil.getInt(context, AppConfig.SP_COMPUTE_USED, 0);
        return used >= total;
    }

    public static int getTotalPower(Context context) {
        return SharedPrefUtil.getInt(context, AppConfig.SP_COMPUTE_TOTAL, 1000);
    }

    public static int getUsedPower(Context context) {
        return SharedPrefUtil.getInt(context, AppConfig.SP_COMPUTE_USED, 0);
    }

    public static void setComputePower(Context context, int total, int used) {
        SharedPrefUtil.putInt(context, AppConfig.SP_COMPUTE_TOTAL, total);
        SharedPrefUtil.putInt(context, AppConfig.SP_COMPUTE_USED, used);
    }

    public static void showExhaustedDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(R.string.power_exhausted)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }
}
''')

print('Part 1 done')
