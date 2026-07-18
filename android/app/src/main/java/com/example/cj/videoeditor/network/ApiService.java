package com.example.cj.videoeditor.network;

import com.example.cj.videoeditor.network.dto.*;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * batch-video-editor 后端 API 接口定义。
 *
 * 所有需要认证的接口会自动携带 [Authorization: Bearer &lt;token&gt;]（由 TokenInterceptor 处理）。
 */
public interface ApiService {

    // ==================== 认证 ====================

    @POST("login")
    Call<BaseResponse<LoginData>> login(@Body LoginBody body);

    @GET("getInfo")
    Call<BaseResponse<Map<String, Object>>> getUserInfo();

    @GET("getRouters")
    Call<BaseResponse<List<Map<String, Object>>>> getRouters();

    @POST("register")
    Call<BaseResponse<Object>> register(@Body RegisterBody body);

    // ==================== APP 端专属接口（/batch/app/*）====================

    @POST("batch/app/login")
    Call<BaseResponse<BatchCustomerDto>> appLogin(@Body AppLoginBody body);

    @POST("batch/app/register")
    Call<BaseResponse<BatchCustomerDto>> appRegister(@Body AppRegisterBody body);

    @GET("batch/app/customer/phone/{phone}")
    Call<BaseResponse<BatchCustomerDto>> getAppCustomer(@Path("phone") String phone);

    @PUT("batch/app/customer")
    Call<BaseResponse<Object>> updateAppCustomer(@Body BatchCustomerDto customer);

    @POST("batch/app/logout")
    Call<BaseResponse<Object>> appLogout();

    @Multipart
    @POST("batch/app/upload")
    Call<BaseResponse<String>> uploadAvatar(@Part MultipartBody.Part file);

    @DELETE("batch/app/customer")
    Call<BaseResponse<Object>> deleteCurrentCustomer();

    // ==================== 客户/APP 账号 ====================

    @GET("batch/customer/list")
    Call<PageResponse<BatchCustomerDto>> getCustomerList(@QueryMap Map<String, String> params);

    @GET("batch/customer/{customerId}")
    Call<BaseResponse<BatchCustomerDto>> getCustomerById(@Path("customerId") Long customerId);

    @GET("batch/customer/phone/{phone}")
    Call<BaseResponse<BatchCustomerDto>> getCustomerByPhone(@Path("phone") String phone);

    @POST("batch/customer")
    Call<BaseResponse<Object>> addCustomer(@Body BatchCustomerDto customer);

    @PUT("batch/customer")
    Call<BaseResponse<Object>> updateCustomer(@Body BatchCustomerDto customer);

    @PUT("batch/customer/changeStatus")
    Call<BaseResponse<Object>> changeCustomerStatus(@Body BatchCustomerDto customer);

    @DELETE("batch/customer/{customerIds}")
    Call<BaseResponse<Object>> deleteCustomer(@Path("customerIds") String customerIds);

    @PUT("batch/customer/qrCode/{customerId}")
    Call<BaseResponse<Map<String, Object>>> resetCustomerQrCode(@Path("customerId") Long customerId);

    @PUT("batch/customer/upgrade/{customerId}")
    Call<BaseResponse<Object>> upgradeCustomer(@Path("customerId") Long customerId, @Body BatchCustomerDto data);

    @PUT("batch/customer/migrate/{customerId}")
    Call<BaseResponse<Object>> migrateCustomer(@Path("customerId") Long customerId, @Body BatchCustomerDto data);

    // ==================== 首页 ====================

    @GET("batch/home/banner/list")
    Call<PageResponse<BatchHomeBannerDto>> getHomeBannerList();

    @GET("batch/home/banner/list")
    Call<PageResponse<BatchHomeBannerDto>> getHomeBannerList(@QueryMap Map<String, String> params);

    @GET("batch/home/banner/{bannerId}")
    Call<BaseResponse<BatchHomeBannerDto>> getHomeBannerById(@Path("bannerId") Long bannerId);

    @GET("batch/home/entry/list")
    Call<PageResponse<BatchHomeEntryDto>> getHomeEntryList();

    @GET("batch/home/entry/list")
    Call<PageResponse<BatchHomeEntryDto>> getHomeEntryList(@QueryMap Map<String, String> params);

    @GET("batch/home/entry/{entryId}")
    Call<BaseResponse<BatchHomeEntryDto>> getHomeEntryById(@Path("entryId") Long entryId);

    @GET("batch/home/news/list")
    Call<PageResponse<BatchHomeNewsDto>> getHomeNewsList();

    @GET("batch/home/news/list")
    Call<PageResponse<BatchHomeNewsDto>> getHomeNewsList(@QueryMap Map<String, String> params);

    @GET("batch/home/news/{newsId}")
    Call<BaseResponse<BatchHomeNewsDto>> getHomeNewsById(@Path("newsId") Long newsId);

    @GET("batch/home/tutorialEntry/list")
    Call<PageResponse<BatchHomeTutorialEntryDto>> getHomeTutorialEntryList();

    @GET("batch/home/tutorialEntry/list")
    Call<PageResponse<BatchHomeTutorialEntryDto>> getHomeTutorialEntryList(@QueryMap Map<String, String> params);

    @GET("batch/home/tutorialEntry/{entryId}")
    Call<BaseResponse<BatchHomeTutorialEntryDto>> getHomeTutorialEntryById(@Path("entryId") Long entryId);

    @GET("batch/home/tutorialEntry/documentList")
    Call<BaseResponse<List<BatchHomeDocumentOptionDto>>> getHomeTutorialDocumentOptions();

    // ==================== 教程 ====================

    @GET("batch/tutorial/list")
    Call<PageResponse<BatchTutorialDto>> getTutorialList();

    @GET("batch/tutorial/list")
    Call<PageResponse<BatchTutorialDto>> getTutorialList(@QueryMap Map<String, String> params);

    @GET("batch/tutorial/{tutorialId}")
    Call<BaseResponse<BatchTutorialDto>> getTutorialById(@Path("tutorialId") Long tutorialId);

    @POST("batch/tutorial")
    Call<BaseResponse<Object>> addTutorial(@Body BatchTutorialDto tutorial);

    @PUT("batch/tutorial")
    Call<BaseResponse<Object>> updateTutorial(@Body BatchTutorialDto tutorial);

    @PUT("batch/tutorial/changeStatus")
    Call<BaseResponse<Object>> changeTutorialStatus(@Body BatchTutorialDto tutorial);

    @DELETE("batch/tutorial/{tutorialIds}")
    Call<BaseResponse<Object>> deleteTutorial(@Path("tutorialIds") String tutorialIds);

    @GET("batch/tutorial/category/list")
    Call<PageResponse<BatchTutorialCategoryDto>> getTutorialCategoryList();

    @GET("batch/tutorial/category/list")
    Call<PageResponse<BatchTutorialCategoryDto>> getTutorialCategoryList(@QueryMap Map<String, String> params);

    @GET("batch/tutorial/category/all")
    Call<BaseResponse<List<BatchTutorialCategoryDto>>> getTutorialCategoryAll();

    @GET("batch/tutorial/category/{categoryId}")
    Call<BaseResponse<BatchTutorialCategoryDto>> getTutorialCategoryById(@Path("categoryId") Long categoryId);

    @POST("batch/tutorial/category")
    Call<BaseResponse<Object>> addTutorialCategory(@Body BatchTutorialCategoryDto category);

    @PUT("batch/tutorial/category")
    Call<BaseResponse<Object>> updateTutorialCategory(@Body BatchTutorialCategoryDto category);

    @DELETE("batch/tutorial/category/{categoryIds}")
    Call<BaseResponse<Object>> deleteTutorialCategory(@Path("categoryIds") String categoryIds);

    // ==================== 文档 ====================

    @GET("batch/document/list")
    Call<PageResponse<BatchDocumentDto>> getDocumentList();

    @GET("batch/document/list")
    Call<PageResponse<BatchDocumentDto>> getDocumentList(@QueryMap Map<String, String> params);

    @GET("batch/document/{documentId}")
    Call<BaseResponse<BatchDocumentDto>> getDocumentById(@Path("documentId") Long documentId);

    @GET("batch/document/category/all")
    Call<BaseResponse<List<String>>> getDocumentCategoryAll();

    @POST("batch/document")
    Call<BaseResponse<Object>> addDocument(@Body BatchDocumentDto document);

    @PUT("batch/document")
    Call<BaseResponse<Object>> updateDocument(@Body BatchDocumentDto document);

    @PUT("batch/document/changeStatus")
    Call<BaseResponse<Object>> changeDocumentStatus(@Body BatchDocumentDto document);

    @DELETE("batch/document/{documentIds}")
    Call<BaseResponse<Object>> deleteDocument(@Path("documentIds") String documentIds);

    // ==================== 公告 ====================

    @GET("batch/notice/list")
    Call<PageResponse<BatchAppNoticeDto>> getNoticeList();

    @GET("batch/notice/list")
    Call<PageResponse<BatchAppNoticeDto>> getNoticeList(@QueryMap Map<String, String> params);

    @GET("batch/notice/{noticeId}")
    Call<BaseResponse<BatchAppNoticeDto>> getNoticeById(@Path("noticeId") Long noticeId);

    @GET("batch/notice/preview/{noticeId}")
    Call<BaseResponse<BatchAppNoticeDto>> previewNotice(@Path("noticeId") Long noticeId);

    @POST("batch/notice")
    Call<BaseResponse<Object>> addNotice(@Body BatchAppNoticeDto notice);

    @PUT("batch/notice")
    Call<BaseResponse<Object>> updateNotice(@Body BatchAppNoticeDto notice);

    @DELETE("batch/notice/{noticeIds}")
    Call<BaseResponse<Object>> deleteNotice(@Path("noticeIds") String noticeIds);

    @PUT("batch/notice/publish/{noticeId}")
    Call<BaseResponse<Object>> publishNotice(@Path("noticeId") Long noticeId);

    @PUT("batch/notice/unpublish/{noticeId}")
    Call<BaseResponse<Object>> unpublishNotice(@Path("noticeId") Long noticeId);

    // ==================== 算力日志 ====================

    @GET("batch/computing/log/list")
    Call<PageResponse<BatchComputingPowerLogDto>> getComputingLogList();

    @GET("batch/computing/log/list")
    Call<PageResponse<BatchComputingPowerLogDto>> getComputingLogList(@QueryMap Map<String, String> params);

    // ==================== VIP 管理 ====================

    @GET("batch/vip/list")
    Call<PageResponse<BatchCustomerDto>> getVipList();

    @GET("batch/vip/list")
    Call<PageResponse<BatchCustomerDto>> getVipList(@QueryMap Map<String, String> params);

    @PUT("batch/vip/{customerId}")
    Call<BaseResponse<Object>> updateVipExpireDate(@Path("customerId") Long customerId, @Body BatchVipUpdateDto data);

    @PUT("batch/vip/batch")
    Call<BaseResponse<Object>> updateVipExpireDateBatch(@Body BatchVipUpdateDto data);

    // ==================== 品牌专区 ====================

    @GET("batch/brand/list")
    Call<BaseResponse<List<BatchBrandDto>>> getBrandList();

    // ==================== 联系方式 ====================

    @GET("batch/contact/list")
    Call<BaseResponse<List<BatchContactDto>>> getContactList();

    // ==================== 系统配置 ====================

    @GET("batch/config/brand")
    Call<BaseResponse<Map<String, Object>>> getBrandConfig();

    @GET("batch/config/global")
    Call<BaseResponse<Map<String, Object>>> getGlobalConfig();

    @GET("batch/config/version/list")
    Call<PageResponse<BatchAppVersionDto>> getAppVersionList();

    @GET("batch/config/version/list")
    Call<PageResponse<BatchAppVersionDto>> getAppVersionList(@QueryMap Map<String, String> params);

    @GET("batch/config/version/{versionId}")
    Call<BaseResponse<BatchAppVersionDto>> getAppVersionById(@Path("versionId") Long versionId);

    // ==================== AI 去水印 ====================

    @POST("batch/watermark/parse")
    Call<BaseResponse<WatermarkParseDto>> parseWatermark(@Body WatermarkParseBody body);

    @GET("batch/watermark/parse/list")
    Call<PageResponse<WatermarkParseDto>> getWatermarkParseList();

    @GET("batch/watermark/parse/{parseId}")
    Call<BaseResponse<WatermarkParseDto>> getWatermarkParseById(@Path("parseId") Long parseId);

    // ==================== AI 云创 ====================

    @GET("batch/ai/video/group/list")
    Call<PageResponse<BatchAiVideoGroupDto>> getAiVideoGroupList();

    @GET("batch/ai/video/group/{groupId}")
    Call<BaseResponse<BatchAiVideoGroupDto>> getAiVideoGroup(@Path("groupId") Long groupId);

    @POST("batch/ai/video/group")
    Call<BaseResponse<BatchAiVideoGroupCreateResultDto>> addAiVideoGroup(@Body BatchAiVideoGroupDto group);

    @PUT("batch/ai/video/group")
    Call<BaseResponse<Object>> updateAiVideoGroup(@Body BatchAiVideoGroupDto group);

    @DELETE("batch/ai/video/group/{groupId}")
    Call<BaseResponse<Object>> deleteAiVideoGroup(@Path("groupId") Long groupId);

    @POST("batch/ai/video/generate")
    Call<BaseResponse<BatchAiVideoGenerateResultDto>> generateAiVideo(@Body BatchAiVideoGenerateBody body);

    /**
     * 上传素材视频，返回 data.url。
     */
    @Multipart
    @POST("batch/ai/video/upload")
    Call<BaseResponse<Map<String, Object>>> uploadAiVideo(@Part MultipartBody.Part file);

    /**
     * 按切片时长把素材切成多个分镜头，body: {groupId, videoUrl, sliceDuration}。
     */
    @POST("batch/ai/video/split")
    Call<BaseResponse<List<BatchAiVideoClipDto>>> splitAiVideo(@Body Map<String, Object> body);

    /**
     * 异步批量生成视频，body: {groupId, count}。
     */
    @POST("batch/ai/video/generate")
    Call<BaseResponse<Object>> submitAiVideoGenerate(@Body Map<String, Object> body);

    /**
     * 查询某视频组的生成任务列表：[{logId,status,progress,resultUrl,errorMsg,createTime}]，
     * status: 0=处理中 1=成功 2=失败。
     */
    @GET("batch/ai/video/task/list")
    Call<BaseResponse<List<Map<String, Object>>>> getAiVideoTaskList(@Query("groupId") Long groupId);

    // ==================== 算力 ====================

    @POST("batch/computing/log/consume")
    Call<BaseResponse<ComputingConsumeDto>> consumeComputingPower(@Body ComputingConsumeBody body);

    // ==================== 数据统计 ====================

    @GET("batch/statistics/overview")
    Call<BaseResponse<BatchStatisticsOverviewDto>> getStatisticsOverview();

    @GET("batch/statistics/overview")
    Call<BaseResponse<BatchStatisticsOverviewDto>> getStatisticsOverview(@QueryMap Map<String, String> params);

    @GET("batch/statistics/account")
    Call<PageResponse<Map<String, Object>>> getStatisticsAccountList();

    @GET("batch/statistics/account")
    Call<PageResponse<Map<String, Object>>> getStatisticsAccountList(@QueryMap Map<String, String> params);

    @GET("batch/statistics/computing")
    Call<PageResponse<Map<String, Object>>> getStatisticsComputingList();

    @GET("batch/statistics/computing")
    Call<PageResponse<Map<String, Object>>> getStatisticsComputingList(@QueryMap Map<String, String> params);

    @GET("batch/statistics/video")
    Call<PageResponse<Map<String, Object>>> getStatisticsVideoList();

    @GET("batch/statistics/video")
    Call<PageResponse<Map<String, Object>>> getStatisticsVideoList(@QueryMap Map<String, String> params);

    @GET("batch/statistics/qrcode")
    Call<PageResponse<Map<String, Object>>> getStatisticsQrCodeList();

    @GET("batch/statistics/qrcode")
    Call<PageResponse<Map<String, Object>>> getStatisticsQrCodeList(@QueryMap Map<String, String> params);

    @GET("batch/statistics/news")
    Call<PageResponse<Map<String, Object>>> getStatisticsNewsList();

    @GET("batch/statistics/news")
    Call<PageResponse<Map<String, Object>>> getStatisticsNewsList(@QueryMap Map<String, String> params);

    @GET("batch/statistics/trend")
    Call<BaseResponse<Map<String, Object>>> getStatisticsTrend();

    @GET("batch/statistics/trend")
    Call<BaseResponse<Map<String, Object>>> getStatisticsTrend(@QueryMap Map<String, String> params);
}
