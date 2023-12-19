package com.android.swaddle.networkManager;


import com.android.swaddle.JUpdateProfileModelKotlin;
import com.android.swaddle.models.AuthorizedAdultsResponse;
import com.android.swaddle.models.CenterInfoResponse;
import com.android.swaddle.models.ChatsResponse;
import com.android.swaddle.models.ChildDetailsResponse;
import com.android.swaddle.models.ChildesResponse;
import com.android.swaddle.models.ClassRoomsResponse;
import com.android.swaddle.models.DailyReportResponse;
import com.android.swaddle.models.ForgetPasswordResponse;
import com.android.swaddle.models.GetActivitiesResponse;
import com.android.swaddle.models.GetAttendanceResponse;
import com.android.swaddle.models.GetInvoicesResponse;
import com.android.swaddle.models.GetStaffResponse;
import com.android.swaddle.models.GetSummaryReportResponse;
import com.android.swaddle.models.LoginResponse;
import com.android.swaddle.models.MedicalReportsResponse;
import com.android.swaddle.models.NotificationSettingsResponse;
import com.android.swaddle.models.NotificationsResponse;
import com.android.swaddle.models.PaymentCardsResponse;
import com.android.swaddle.models.SingleChatsResponse;
import com.android.swaddle.models.UpdateProfileResponse;
import com.android.swaddle.models.UsersForChatsResponse;
import com.android.swaddle.models.WeeklyChildAttendanceModel;
import com.android.swaddle.models.delete_certifications.DeleteCertifications;
import com.android.swaddle.models.get_certifications.GetCertifications;
import com.android.swaddle.models.get_user_detail_models.GetuserDetailBaseModel;
import com.android.swaddle.models.update_account_status.UpdateAccountStatus;
import com.android.swaddle.models.uploadFileResponse.ChildRemoveResponse;
import com.android.swaddle.models.uploadFileResponse.UploadFileResponse;
import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface webRequests {

    @Multipart
    @POST("store-file")
    Call<UploadFileResponse> hitUploadFile(@Part MultipartBody.Part file);

//    @Multipart
//    @POST("register_or_update_child")
//    Call<UploadFileResponse> registerOrUpdateChild(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("auth/login")
    Call<LoginResponse> loginUser(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/forgot_password")
    Call<ForgetPasswordResponse> resetPassword(@Field("email") String email);

    @Multipart
    @POST("register_or_update_child")
    Call<UpdateProfileResponse> registerOrUpdateChild(@Header("Authorization") String headerToken,
                                                      @Part MultipartBody.Part file,
                                                      @Part MultipartBody.Part custodyDocument,
                                                      @Part("child_id") int child_id,
                                                      @Part("first_name") RequestBody first_name,
                                                      @Part("middle_name") RequestBody middle_name,
                                                      @Part("last_name") RequestBody last_name,
                                                      @Part("nick_name") RequestBody nick_name,
                                                      @Part("child_call_you") RequestBody child_call_you,
                                                      @Part("gender") RequestBody gender,
                                                      @Part("dob") RequestBody dob,
                                                      @Part("primary_address") RequestBody primary_address,
                                                      @Part("secondary_address") RequestBody secondary_address,
                                                      @Part("custody") RequestBody custody,
                                                      @Part("iep") RequestBody iep,
                                                      @Part("comment") RequestBody comment,
                                                      @Part("iep_diagnosed_by") RequestBody iep_diagnosed_by,
                                                      @Part("does_have_any_allergies") RequestBody does_have_any_allergies,
                                                      @Part("food_allergies[]") ArrayList<RequestBody> food_allergies,
                                                      @Part("environmental_allergies[]") RequestBody environmental_allergies,
                                                      @Part("does_child_require_epi_pin") RequestBody does_child_require_epi_pin,
                                                      @Part("will_you_provide_epi_pin") RequestBody will_you_provide_epi_pin,
                                                      @Part("custody_consideration") RequestBody custody_consideration,
                                                      @Part("custody_description") RequestBody custody_description,
                                                      @Part("monday[0]") RequestBody mondayOpening,
                                                      @Part("monday[1]") RequestBody mondayClosing,
                                                      @Part("tuesday[0]") RequestBody tuesdayOpening,
                                                      @Part("tuesday[1]") RequestBody tuesdayClosing,
                                                      @Part("wednesday[0]") RequestBody wednesdayOpening,
                                                      @Part("wednesday[1]") RequestBody wednesdayClosing,
                                                      @Part("thursday[0]") RequestBody thursdayOpening,
                                                      @Part("thursday[1]") RequestBody thursdayClosing,
                                                      @Part("friday[0]") RequestBody fridayOpening,
                                                      @Part("friday[1]") RequestBody fridayClosing,
                                                      @Part("lat") RequestBody lat,
                                                      @Part("lng") RequestBody lng);


    @POST("register_or_update_child")
    Call<UpdateProfileResponse> jUpdateChildWithoutAnyFile(
            @Header("Authorization") String headerToken,
            @Body JUpdateProfileModelKotlin jUpdateProfileModel);

    @Multipart
    @POST("register_or_update_child")
    Call<UpdateProfileResponse> registerOrUpdateChildWithOutImage(@Header("Authorization") String headerToken,
                                                                  @Part MultipartBody.Part custodyDocument,
                                                                  @Part("child_id") RequestBody child_id,
                                                                  @Part("first_name") RequestBody first_name,
                                                                  @Part("middle_name") RequestBody middle_name,
                                                                  @Part("last_name") RequestBody last_name,
                                                                  @Part("nick_name") RequestBody nick_name,
                                                                  @Part("child_call_you") RequestBody child_call_you,
                                                                  @Part("gender") RequestBody gender,
                                                                  @Part("dob") RequestBody dob,
                                                                  @Part("primary_address") RequestBody primary_address,
                                                                  @Part("secondary_address") RequestBody secondary_address,
                                                                  @Part("custody") RequestBody custody,
                                                                  @Part("iep") RequestBody iep,
                                                                  @Part("comment") RequestBody comment,
                                                                  @Part("iep_diagnosed_by") RequestBody iep_diagnosed_by,
                                                                  @Part("does_have_any_allergies") RequestBody does_have_any_allergies,
                                                                  @Part("food_allergies[]") ArrayList<RequestBody> food_allergies,
                                                                  @Part("environmental_allergies[]") RequestBody environmental_allergies,
                                                                  @Part("does_child_require_epi_pin") RequestBody does_child_require_epi_pin,
                                                                  @Part("will_you_provide_epi_pin") RequestBody will_you_provide_epi_pin,
                                                                  @Part("custody_consideration") RequestBody custody_consideration,
                                                                  @Part("custody_description") RequestBody custody_description,
                                                                  @Part("monday[0]") RequestBody mondayOpening,
                                                                  @Part("monday[1]") RequestBody mondayClosing,
                                                                  @Part("tuesday[0]") RequestBody tuesdayOpening,
                                                                  @Part("tuesday[1]") RequestBody tuesdayClosing,
                                                                  @Part("wednesday[0]") RequestBody wednesdayOpening,
                                                                  @Part("wednesday[1]") RequestBody wednesdayClosing,
                                                                  @Part("thursday[0]") RequestBody thursdayOpening,
                                                                  @Part("thursday[1]") RequestBody thursdayClosing,
                                                                  @Part("friday[0]") RequestBody fridayOpening,
                                                                  @Part("friday[1]") RequestBody fridayClosing,
                                                                  @Part("lat") RequestBody lat,
                                                                  @Part("lng") RequestBody lng);

    @Multipart
    @POST("add_or_update_center")
    Call<UpdateProfileResponse> registerOrUpdateCenter(@Header("Authorization") String headerToken,
                                                       @Part("center_id") RequestBody center_id,
                                                       @Part("center_name") RequestBody center_name,
                                                       @Part("email") RequestBody email,
                                                       @Part("location") RequestBody location,
                                                       @Part("phone_number") RequestBody phone_number,
                                                       @Part("monday[0]") RequestBody mondayOpening,
                                                       @Part("monday[1]") RequestBody mondayClosing,
                                                       @Part("tuesday[0]") RequestBody tuesdayOpening,
                                                       @Part("tuesday[1]") RequestBody tuesdayClosing,
                                                       @Part("wednesday[0]") RequestBody wednesdayOpening,
                                                       @Part("wednesday[1]") RequestBody wednesdayClosing,
                                                       @Part("thursday[0]") RequestBody thursdayOpening,
                                                       @Part("thursday[1]") RequestBody thursdayClosing,
                                                       @Part("friday[0]") RequestBody fridayOpening,
                                                       @Part("friday[1]") RequestBody fridayClosing,
                                                       @Part("saturday[0]") RequestBody saturdayOpening,
                                                       @Part("saturday[1]") RequestBody saturdayClosing,
                                                       @Part("sunday[0]") RequestBody sundayOpening,
                                                       @Part("sunday[1]") RequestBody sundayClosing,
                                                       @Part MultipartBody.Part file,
                                                       @Part("lat") RequestBody lat,
                                                       @Part("lng") RequestBody lng,
                                                       @Part("website") RequestBody website);

    @Multipart
    @POST("add_or_update_center")
    Call<UpdateProfileResponse> registerOrUpdateCenterWithoutImage(@Header("Authorization") String headerToken,
                                                                   @Part("center_id") RequestBody center_id,
                                                                   @Part("center_name") RequestBody center_name,
                                                                   @Part("email") RequestBody email,
                                                                   @Part("location") RequestBody location,
                                                                   @Part("phone_number") RequestBody phone_number,
                                                                   @Part("monday[0]") RequestBody mondayOpening,
                                                                   @Part("monday[1]") RequestBody mondayClosing,
                                                                   @Part("tuesday[0]") RequestBody tuesdayOpening,
                                                                   @Part("tuesday[1]") RequestBody tuesdayClosing,
                                                                   @Part("wednesday[0]") RequestBody wednesdayOpening,
                                                                   @Part("wednesday[1]") RequestBody wednesdayClosing,
                                                                   @Part("thursday[0]") RequestBody thursdayOpening,
                                                                   @Part("thursday[1]") RequestBody thursdayClosing,
                                                                   @Part("friday[0]") RequestBody fridayOpening,
                                                                   @Part("friday[1]") RequestBody fridayClosing,
                                                                   @Part("saturday[0]") RequestBody saturdayOpening,
                                                                   @Part("saturday[1]") RequestBody saturdayClosing,
                                                                   @Part("sunday[0]") RequestBody sundayOpening,
                                                                   @Part("sunday[1]") RequestBody sundayClosing,
                                                                   @Part("lat") RequestBody lat,
                                                                   @Part("lng") RequestBody lng,
                                                                   @Part("website") RequestBody website);

    @GET("get_provider_certifications")
    Call<GetCertifications> getProviderCertifications(@Header("Authorization") String headerToken, @Query("user_id") String user_id);

    @GET("update_account_status")
    Call<UpdateAccountStatus> updateAccountStatus(@Header("Authorization") String headerToken, @Query("mode") String mode);

    @GET("delete_provider_certification")
    Call<DeleteCertifications> deleteCertifications(@Header("Authorization") String headerToken, @Query("removal_index") String removal_index);

    @GET("get_childs_of_no_classroom")
    Call<ChildesResponse> getChildesOfNoClass(@Header("Authorization") String headerToken, @Query("with_trash") String with_trash);

    @GET("get_childs_of_a_provider")
    Call<ChildesResponse> getAllChilds(@Header("Authorization") String headerToken, @Query("with_trash") String with_trash);

    @FormUrlEncoded
    @POST("create_and_update_classroom")
    Call<ChildesResponse> createOrUpdateClassRoom(@Header("Authorization") String headerToken, @Field("classroom_id") String classroom_id, @Field("name") String name, @Field("description") String description, @Field("child_ids") String child_ids);

    @GET("get_childs_of_a_parent")
    Call<ChildesResponse> getChildesList(@Header("Authorization") String headerToken, @Query("with_trash") String with_trash);

    @FormUrlEncoded
    @POST("child_absent_notes")
    Call<ChildesResponse> saveAbsentNote(@Header("Authorization") String headerToken, @Field("child_id") int child_id, @Field("absent_reason") String absent_reason, @Field("is_absent") int is_absent);

    @GET("get_childs_of_a_classroom")
    Call<ChildesResponse> getChildesListOfRoom(@Header("Authorization") String headerToken, @Query("classroom_id") String classroom_id, @Query("with_trash") String with_trash);

    @GET("get_childs_of_a_classroom_with_attandance")
    Call<ChildesResponse> getChildesListOfRoomWithAttendance(@Header("Authorization") String headerToken, @Query("classroom_id") String classroom_id, @Query("with_trash") String with_trash, @Query("selected_date") String selected_date);

    @FormUrlEncoded
    @POST("create_invoice")
    Call<okhttp3.ResponseBody> generateInvoice(@Header("Authorization") String headerToken, @Field("child_id") String child_id, @Field("description") String description, @Field("amount") String amount, @Field("due_date") String due_date, @Field("bill_type") String bill_type, @Field("start_date") String start_date, @Field("end_date") String end_date, @Field("interval") String interval);

    @FormUrlEncoded
    @POST("update_invoice")
    Call<okhttp3.ResponseBody> updateInvoice(@Header("Authorization") String headerToken, @Field("id") String invoiceId, @Field("child_id") String child_id, @Field("description") String description, @Field("amount") String amount, @Field("due_date") String due_date, @Field("bill_type") String bill_type, @Field("start_date") String start_date, @Field("end_date") String end_date, @Field("interval") String interval);

//    @GET("get_child_attendance_report")
//    Call<ChildesResponse> getAttendanceReport(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("date_from") String date_from, @Query("date_to") String date_to);

    @GET("get_center_information")
    Call<CenterInfoResponse> getCenterInformation(@Header("Authorization") String headerToken);

    @GET("get_child_daily_report_details")
    Call<DailyReportResponse> getChildDailyReportDetails(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("report_date") String report_date);

    @GET("get_child_details")
    Call<ChildDetailsResponse> getChildDetails(@Header("Authorization") String headerToken, @Query("child_id") String child_id);

    @GET("get_classrooms")
    Call<ClassRoomsResponse> getClassRooms(@Header("Authorization") String headerToken);

    @GET("get_notifications")
    Call<NotificationsResponse> getNotifications(@Header("Authorization") String headerToken);

    @GET("get_child_attendance_report")
    Call<GetSummaryReportResponse> getAttendanceReport(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("time_period") String time_period, @Query("date_from") String date_from, @Query("date_to") String date_to);

//    @GET("get_child_attendance_report")
//    Call<GetSummaryReportResponse> getAttendanceReport(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("time_period") String time_period);

    @POST("contact_us")
    Call<LoginResponse> contactToSupport(@Header("Authorization") String headerToken, @Field("email") String email, @Field("name") String name, @Field("message") String msg);

    @FormUrlEncoded
    @POST("pay_bills")
    Call<ChildesResponse> payBills(@Header("Authorization") String headerToken, @Field("invoice_id") String invoice_id, @Field("amount") String amount, @Field("child_id") String child_id);


    @GET("get_staff_listing_with_todays_attendance")
    Call<GetStaffResponse> getStaffsDetails(@Header("Authorization") String headerToken);

    @GET("get_medical_information_req")
    Call<MedicalReportsResponse> getMedicalInformation(@Header("Authorization") String headerToken, @Query("child_id") String child_id);

    @GET("child_attendance_of_today")
    Call<GetAttendanceResponse> getChildAttendanceOfToday(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("attendance_date") String attendance_date);

    @GET("get_parents_of_a_child")
    Call<AuthorizedAdultsResponse> getAuthorizedAdults(@Header("Authorization") String headerToken, @Query("child_id") String child_id);

    @GET("provider_staffs_parent_adult")
    Call<AuthorizedAdultsResponse> getParentsOfProvider(@Header("Authorization") String headerToken);

    @GET("get_single_day_activities")
    Call<GetActivitiesResponse> getActivities(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("classroom_id") String classroom_id, @Query("activity_date") String activity_date);

    @GET("get_invoices")
    Call<GetInvoicesResponse> getInvoices(
            @Header("Authorization") String headerToken,
            @Query("child_id") String child_id,
            @Query("date_from") String date_from,
            @Query("date_to") String date_to,
            @Query("time_period") String time_period);

    @GET("get_invoices")
    Call<GetInvoicesResponse> getLatestInvoices(
            @Header("Authorization") String headerToken,
            @Query("child_id") String child_id,
            @Query("classroom_id") String classroom_id,
            @Query("date_from") String date_from,
            @Query("date_to") String date_to);


    @GET("get_incident_reports")
    Call<ChildesResponse> getIncidenceReport(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("classroom_id") String classroom_id, @Query("date") String date);

    @GET("get_all_incident_reports")
    Call<ChildesResponse> getAllIncidenceReport(@Header("Authorization") String headerToken, @Query("child_id") String child_id, @Query("classroom_id") String classroom_id, @Query("time_period") String time_period, @Query("date_from") String date_from, @Query("date_to") String date_to);

    @Multipart
    @POST("create_invoice")
    Call<LoginResponse> createInvoice(@Header("Authorization") String headerToken,
                                      @Part("child_id") RequestBody child_id,
                                      @Part("description") RequestBody description,
                                      @Part("amount") RequestBody amount,
                                      @Part("due_date") RequestBody due_date,
                                      @Part MultipartBody.Part file
    );

    @GET("get_all_chats")
    Call<ChatsResponse> getChats(@Header("Authorization") String headerToken);

    @GET("get_chat")
    Call<SingleChatsResponse> getChatMessages(@Header("Authorization") String headerToken, @Query("chat_id") String chat_id);

    @GET("users_list")
    Call<UsersForChatsResponse> getAllUsersForChat(@Header("Authorization") String headerToken);

    @FormUrlEncoded
    @POST("remove_child")
    Call<ClassRoomsResponse> removeChildOfClassRoom(@Header("Authorization") String headerToken, @Field("child_id") String child_id);

    @FormUrlEncoded
    @POST("restore_child")
    Call<ClassRoomsResponse> restoreChild(@Header("Authorization") String headerToken, @Field("child_id") String child_id);

    @FormUrlEncoded
    @POST("update_epi_pen_expiration_date")
    Call<LoginResponse> updateEpiPenExpirationDate(@Header("Authorization") String headerToken, @Field("child_id") String child_id, @Field("epi_pen_expiration_date") String epi_pen_expiration_date);

    @FormUrlEncoded
    @POST("delete_child")
    Call<ClassRoomsResponse> deleteChild(@Header("Authorization") String headerToken, @Field("child_id") String child_id);

    @GET("read_chat")
    Call<LoginResponse> readChat(@Header("Authorization") String token, @Query("chat_id") String chatId);

    @FormUrlEncoded
    @POST("delete_chat")
    Call<LoginResponse> deleteChat(@Header("Authorization") String token, @Field("chat_id") String chatId);

    @FormUrlEncoded
    @POST("create_chat")
    Call<LoginResponse> createChat(@Header("Authorization") String headerToken,
                                   @Field("members") String members,
                                   @Field("type") String type,
                                   @Field("message") String message,
                                   @Field("file") String file,
                                   @Field("group_photo") String group_photo,
                                   @Field("title") String title);

    @GET("delete_media")
    Call<LoginResponse> deleteMedia(@Header("Authorization") String headerToken, @Query("id") String reportId, @Query("media") String media);

    @GET("delete_medical_report")
    Call<LoginResponse> deleteMedicalReportMedia(@Header("Authorization") String headerToken, @Query("medical_info_id") String medical_info_id);

    @POST("delete_child_daily_report")
    Call<LoginResponse> deleteChildDailyReport(@Header("Authorization") String headerToken, @Query("id") String child_daily_report_id);

    @GET("delete_activity_media")
    Call<LoginResponse> deleteActivityMedia(@Header("Authorization") String headerToken, @Query("id") String id, @Query("media") String media);

    @GET("delete_custody_document")
    Call<LoginResponse> deleteCustodyDocument(@Header("Authorization") String headerToken, @Query("child_id") String id, @Query("custody_document") String media);

    @FormUrlEncoded
    @POST("stripe_connect")
    Call<LoginResponse> stripeConnect(@Header("Authorization") String headerToken, @Field("code") String code);

    @FormUrlEncoded
    @POST("add_payment_method")
    Call<LoginResponse> addPaymentMethod(@Header("Authorization") String headerToken, @Field("payment_method_id") String payment_method_id, @Field("is_default") int is_default);

    @GET("get_payment_methods")
    Call<PaymentCardsResponse> getPaymentMethods(@Header("Authorization") String headerToken);

    @FormUrlEncoded
    @POST("promote_child")
    Call<ClassRoomsResponse> promoteChild(@Header("Authorization") String headerToken, @Field("child_id") String child_id, @Field("classroom_id") String classroom_id);

    @FormUrlEncoded
    @POST("make_default_card")
    Call<PaymentCardsResponse> makeDefaultPaymentMethod(@Header("Authorization") String headerToken, @Field("card_id") String card_id);

    @FormUrlEncoded
    @POST("add_provider_certification")
    Call<LoginResponse> addCertification(@Header("Authorization") String headerToken, @Field("name") String name, @Field("date") String date);

    @FormUrlEncoded
    @POST("change_status_staff_before_chron")
    Call<LoginResponse> dismissStaff(@Header("Authorization") String headerToken, @Field("staff_id") String name, @Field("date") String date);

    @FormUrlEncoded
    @POST("staff_attendance")
    Call<LoginResponse> staffAttendance(@Header("Authorization") String headerToken, @Field("staff_id") String name, @Field("status") String status);

    @FormUrlEncoded
    @POST("delete_card")
    Call<PaymentCardsResponse> deletedCard(@Header("Authorization") String headerToken, @Field("card_id") String card_id);

    @FormUrlEncoded
    @POST("save_notification")
    Call<LoginResponse> saveNotification(@Header("Authorization") String headerToken, @Field("reciver_id") String reciver_id, @Field("notification") String notificationText);

    @FormUrlEncoded
    @POST("child_attendance")
    Call<ClassRoomsResponse> submitAttendance(@Header("Authorization") String headerToken, @Field("child_id") String child_id, @Field("attendance_date") String attendance_date, @Field("arrival_time") String arrival_time, @Field("departure_time") String departure_time, @Field("dropped_off_by") String dropped_off_by, @Field("picked_up_by") String picked_up_by);

    @FormUrlEncoded
    @POST("auth/register")
    Call<LoginResponse> registerUser(@Field("first_name") String first_name, @Field("last_name") String last_name, @Field("email") String email, @Field("password") String password, @Field("password_confirmation") String password_confirmation, @Field("invitation_code") String invitation_code, @Field("type") String type);

    @FormUrlEncoded
    @POST("update_user_profile")
    Call<LoginResponse> updateUserProfile(@Field("first_name") String first_name, @Field("last_name") String last_name, @Field("email") String email, @Field("password") String password, @Field("password_confirmation") String password_confirmation, @Field("invitation_code") String invitation_code, @Field("type") String type);

    @FormUrlEncoded
    @POST("send_invitation")
    Call<LoginResponse> sendInvitation(@Header("Authorization") String token, @Field("type") String type, @Field("email") String email);

    @FormUrlEncoded
    @POST("send_invitation")
    Call<LoginResponse> sendAuthrizedAdultsInvitation(@Header("Authorization") String token, @Field("type") String type, @Field("email") String email, @Field("linked_child_ids") String linked_child_ids);

    @FormUrlEncoded
    @POST("send_invitation")
    Call<LoginResponse> addRelationships(@Header("Authorization") String token, @Field("linked_child_ids") String linked_child_ids, @Field("email") String email);

    @FormUrlEncoded
    @POST("add_or_update_child_daily_report")
    Call<LoginResponse> subDailyReport(@Header("Authorization") String token
            , @Field("child_id") String child_id
            , @Field("breakfast_portion") String breakfast_portion
            , @Field("breakfast_description") String breakfast_description
            , @Field("lunch_portion") String lunch_portion
            , @Field("lunch_description") String lunch_description
            , @Field("snack_portion") String snack_portion
            , @Field("snack_description") String snack_description
            , @Field("bottles_drank_today[][]") ArrayList<ArrayList<String>> bottles_drank_today
            , @Field("bowel_movements_count") String bowel_movements_count
            , @Field("bowel_movements_condition") String bowel_movements_condition
            , @Field("nap_times[][]") ArrayList<ArrayList<String>> nap_times
            , @Field("things_to_bring_tomorrow[]") ArrayList<String> things_to_bring_tomorrow
            , @Field("comment") String comment
            , @Field("draft") String draft
    );


    @POST("add_or_update_child_daily_report")
    Call<LoginResponse> subDailyReportNew(@Header("Authorization") String token
            , @Body RequestBody requestBody
    );

    @FormUrlEncoded
    @PUT("update_user_profile")
    Call<LoginResponse> submitCallsYou(@Header("Authorization") String token
            , @Field("child_id[]") ArrayList<String> childIds
            , @Field("authorized_adult_child_call[]") ArrayList<String> authorizedAdultChildCall
    );

    @FormUrlEncoded
    @POST("change_password")
    Call<LoginResponse> updatePassword(@Header("Authorization") String token,
                                       @Field("old_password") String oldPass,
                                       @Field("email") String email,
                                       @Field("password") String password,
                                       @Field("password_confirmation") String password_confirmation);

    @FormUrlEncoded
    @POST("auth/reset_password")
    Call<LoginResponse> forgotPassword(@Field("email") String email,
                                       @Field("password") String password,
                                       @Field("password_confirmation") String password_confirmation);

    @FormUrlEncoded
    @POST("auth/send_invitation")
    Call<LoginResponse> sendInvitationToAuthorizedAdult(@Header("Authorization") String token, @Field("type") String type, @Field("email") String email, @Field("linked_child_ids") String linked_child_ids);

    @Multipart
    @POST("chat/create-chat")
    Call<LoginResponse> updateUserProfile(@Header("Authorization") RequestBody headerToken,
                                          @Part("first_name") RequestBody first_name,
                                          @Part("last_name") RequestBody last_name,
                                          @Part("email") RequestBody email,
                                          @Part("position") RequestBody position,
                                          @Part("experience") RequestBody experience,
                                          @Part("certification") RequestBody certification,
                                          @Part("inspiration") RequestBody inspiration,
                                          @Part MultipartBody.Part file,
                                          @Part MultipartBody.Part custodyDocument);

    @Multipart
    @POST("chat/create-chat")
    Call<LoginResponse> updateOrAddChildActivity(@Header("Authorization") RequestBody headerToken,
                                                 @Part("child_id") RequestBody first_name,
                                                 @Part("activity_type") RequestBody last_name,
                                                 @Part("activity_date") RequestBody email,
                                                 @Part("position") RequestBody position,
                                                 @Part("experience") RequestBody experience,
                                                 @Part("certification") RequestBody certification,
                                                 @Part("inspiration") RequestBody inspiration,
                                                 @Part MultipartBody.Part file,
                                                 @Part MultipartBody.Part custodyDocument);

    @POST("api/remove_child")
    Call<ChildRemoveResponse> remove_child(
            @Header("Authorization") String headerToken,
            @Field("child_id") int child_id
    );

    @GET("remove_classroom")
    Call<LoginResponse> removeClassRoom(
            @Header("Authorization") String headerToken,
            @Query("classroom_id") int child_id
    );

    @FormUrlEncoded
    @POST("add_update_message")
    Call<LoginResponse> addUpdateMessage(@Header("Authorization") String headerToken, @Field("message") String message);

    @FormUrlEncoded
    @POST("update_notification_settings")
    Call<NotificationSettingsResponse> getNotificationSetting(@Header("Authorization") String headerToken, @Field("allow_notification") String notificationSetting);

    @FormUrlEncoded
    @POST("update_notification_settings")
    Call<LoginResponse> updateNotificationSetting(@Header("Authorization") String headerToken, @Field("allow_notification") String notificationSetting);

    @GET("get_user_detail")
    Call<GetuserDetailBaseModel> getUserDetail(@Header("Authorization") String headerToken, @Query("user_id") String message);

    @FormUrlEncoded
    @POST("child_attendance_of_week")
    Call<WeeklyChildAttendanceModel> getWeeklyChildAttendance(
            @Header("Authorization") String headerToken,
            @Field("child_id") String child_id,
            @Field("attendance_date") String attendance_date);  //( Format : Y-m-d)

    @FormUrlEncoded
    @POST("all_attendance")
    Call<LoginResponse> signInSignOut(
            @Header("Authorization") String token,
            @Field("user_type") String user_type, //staff, child
            @Field("status") String status, //signIn, signOut
            @Field("attendance_date") String attendance_date,
            @Field("child_id") String child_id,
            @Field("punch_time") String attendance_time,
            @Field("staff_id") int staff_id
    );
}