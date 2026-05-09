package com.swiftyspiffy.burkeblackapp.data.api

import com.swiftyspiffy.burkeblackapp.data.models.*
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface BurkeBlackApi {

    // Public endpoints
    @GET("home")
    suspend fun fetchHome(): ApiResponse<HomeData>

    @GET("schedule")
    suspend fun fetchSchedule(): ApiResponse<List<ScheduleItem>>

    @GET("profile")
    suspend fun fetchProfile(): ApiResponse<ProfileData>

    @GET("stream-status")
    suspend fun fetchStreamStatus(): ApiResponse<StreamStatusResponse>

    @GET("soundbytes-status")
    suspend fun fetchSoundbytesStatus(): ApiResponse<JsonObject>

    // Auth
    @POST("auth/reviewer")
    suspend fun reviewerLogin(@Body body: JsonObject): ApiResponse<ReviewerResponse>

    // Clip Voting
    @GET("clips")
    suspend fun fetchClips(@Header("Authorization") auth: String = ""): ApiResponse<ClipVotingResponse>

    @POST("clips/vote")
    suspend fun submitClipVote(
        @Header("Authorization") auth: String,
        @Body body: ClipVoteBody
    ): ApiResponse<ClipVoteResponse>

    // Community Game Servers (works with or without auth)
    @GET("community-servers")
    suspend fun fetchCommunityServers(@Header("Authorization") auth: String = ""): ApiResponse<CommunityServersResponse>

    // Authenticated endpoints
    @GET("dashboard")
    suspend fun fetchDashboard(@Header("Authorization") auth: String): ApiResponse<DashboardData>

    @GET("user-status")
    suspend fun fetchUserStatus(@Header("Authorization") auth: String): ApiResponse<UserStatus>

    // Giveaways
    @GET("giveaway-entries")
    suspend fun fetchGiveawayEntries(@Header("Authorization") auth: String): ApiResponse<GiveawayEntriesResponse>

    @GET("giveaway-wins")
    suspend fun fetchGiveawayWins(@Header("Authorization") auth: String): ApiResponse<GiveawayWinsResponse>

    @GET("giveaway-donated")
    suspend fun fetchGiveawayDonated(@Header("Authorization") auth: String): ApiResponse<GiveawayDonatedResponse>

    @GET("giveaway-active")
    suspend fun fetchActiveGiveaway(@Header("Authorization") auth: String): ApiResponse<JsonObject>

    @POST("giveaway-enter")
    suspend fun enterGiveaway(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<JsonObject>

    @POST("giveaway-leave")
    suspend fun leaveGiveaway(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<JsonObject>

    @POST("giveaway-pass")
    suspend fun passGiveaway(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<JsonObject>

    @POST("giveaway-claim")
    suspend fun claimGiveaway(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<JsonObject>

    // Soundbytes
    @GET("soundbytes")
    suspend fun fetchSoundbytes(
        @Header("Authorization") auth: String,
        @Query("offset") offset: Int,
        @Query("amount") amount: Int,
        @Query("search_term") searchTerm: String? = null,
        @Query("genre") genre: String? = null
    ): ApiResponse<SoundbytesResponse>

    @GET("soundbyte-genres")
    suspend fun fetchSoundbyteGenres(@Header("Authorization") auth: String): ApiResponse<SoundbyteGenresResponse>

    @GET("soundbyte-credits")
    suspend fun fetchSoundbyteCredits(@Header("Authorization") auth: String): ApiResponse<SoundbyteCreditsResponse>

    @POST("soundbyte-send")
    suspend fun sendSoundbyte(
        @Header("Authorization") auth: String,
        @Body body: SoundbyteSendBody
    ): ApiResponse<SoundbyteSendResponse>

    @GET("soundbyte-history")
    suspend fun fetchSoundbyteHistory(@Header("Authorization") auth: String): ApiResponse<SoundbyteHistoryResponse>

    // Bits & Donations
    @GET("bits")
    suspend fun fetchBits(@Header("Authorization") auth: String): ApiResponse<BitsResponse>

    @GET("donations")
    suspend fun fetchDonations(@Header("Authorization") auth: String): ApiResponse<DonationsResponse>

    // Feedback
    @Multipart
    @POST("feedback")
    suspend fun submitFeedback(
        @Header("Authorization") auth: String,
        @Part("data") data: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): ApiResponse<JsonObject>

    @POST("feedback")
    suspend fun submitFeedbackNoImages(
        @Header("Authorization") auth: String,
        @Body body: FeedbackBody
    ): ApiResponse<JsonObject>

    @POST("feedback")
    suspend fun submitFeedbackPublic(
        @Body body: FeedbackBody
    ): ApiResponse<JsonObject>

    // Mod panel - Permissions & Settings
    @GET("mod/permissions")
    suspend fun fetchModPermissions(@Header("Authorization") auth: String): ApiResponse<ModPermissions>

    @GET("mod/settings")
    suspend fun fetchModSettings(@Header("Authorization") auth: String): ApiResponse<ModSettings>

    @POST("mod/toggle-soundbytes")
    suspend fun toggleSoundbytes(@Header("Authorization") auth: String): ApiResponse<ToggleSBResponse>

    @POST("mod/toggle-enforcements")
    suspend fun toggleEnforcements(@Header("Authorization") auth: String): ApiResponse<ToggleCEResponse>

    @POST("mod/restart-bot")
    suspend fun restartBot(@Header("Authorization") auth: String): ApiResponse<MessageResponse>

    // Mod - Viewer Lookup
    @GET("mod/viewer-lookup")
    suspend fun viewerLookup(
        @Header("Authorization") auth: String,
        @Query("viewer") viewer: String
    ): ApiResponse<ViewerLookupResult>

    // Mod - Commands
    @GET("mod/commands")
    suspend fun fetchCommands(@Header("Authorization") auth: String): ApiResponse<ModCommandsResponse>

    @POST("mod/commands")
    suspend fun createCommand(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/commands/update")
    suspend fun updateCommand(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/commands/delete")
    suspend fun deleteCommand(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Timed Messages
    @GET("mod/timed-messages")
    suspend fun fetchTimedMessages(@Header("Authorization") auth: String): ApiResponse<ModTimedResponse>

    @POST("mod/timed-messages")
    suspend fun createTimedMessage(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/timed-messages/update")
    suspend fun updateTimedMessage(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/timed-messages/delete")
    suspend fun deleteTimedMessage(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Timeout Words
    @GET("mod/timeout-words")
    suspend fun fetchTimeoutWords(@Header("Authorization") auth: String): ApiResponse<ModTOWordsResponse>

    @POST("mod/timeout-words")
    suspend fun createTimeoutWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/timeout-words/toggle")
    suspend fun toggleTimeoutWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/timeout-words/delete")
    suspend fun deleteTimeoutWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Spoiler Words
    @GET("mod/spoiler-words")
    suspend fun fetchSpoilerWords(@Header("Authorization") auth: String): ApiResponse<ModSpoilerResponse>

    @POST("mod/spoiler-words")
    suspend fun createSpoilerWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/spoiler-words/toggle")
    suspend fun toggleSpoilerWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/spoiler-words/delete")
    suspend fun deleteSpoilerWord(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Links
    @GET("mod/links")
    suspend fun fetchLinks(@Header("Authorization") auth: String): ApiResponse<ModLinksResponse>

    @POST("mod/links")
    suspend fun createLink(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/links/update")
    suspend fun updateLink(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/links/delete")
    suspend fun deleteLink(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Giveaway Submissions
    @GET("mod/giveaway-submissions")
    suspend fun fetchGiveawaySubmissions(@Header("Authorization") auth: String): ApiResponse<SubmissionsResponse>

    @POST("mod/giveaway-submissions/hide")
    suspend fun hideGiveawaySubmission(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @POST("mod/giveaway-submissions/unhide")
    suspend fun unhideGiveawaySubmission(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @GET("mod/giveaway-hidden")
    suspend fun fetchHiddenSubmissions(@Header("Authorization") auth: String): ApiResponse<SubmissionsResponse>

    @POST("mod/giveaway-submissions/add")
    suspend fun addGiveaway(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    @POST("mod/giveaway-send")
    suspend fun sendGiveawayToKraken(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @GET("mod/giveaway-history")
    suspend fun fetchGiveawayHistory(@Header("Authorization") auth: String): ApiResponse<GiveawayHistoryResponse>

    @GET("mod/giveaway-wins")
    suspend fun fetchViewerWins(
        @Header("Authorization") auth: String,
        @Query("viewer") viewer: String
    ): ApiResponse<ViewWinsResponse>

    // Mod - Soundbyte Library
    @GET("mod/soundbyte-library")
    suspend fun fetchSoundbyteLibrary(@Header("Authorization") auth: String): ApiResponse<ModSBLibraryResponse>

    @POST("mod/soundbyte-library/update")
    suspend fun updateSoundbyte(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @GET("mod/soundbyte-credits-list")
    suspend fun fetchSoundbyteCreditsList(@Header("Authorization") auth: String): ApiResponse<ModSBCreditsResponse>

    @POST("mod/soundbyte-credits/modify")
    suspend fun modifySoundbyteCredits(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    // Mod - Alert Burke
    @POST("mod/alert-burke")
    suspend fun alertBurke(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<MessageResponse>

    @GET("mod/soundbyte-full-history")
    suspend fun fetchSoundbyteFullHistory(@Header("Authorization") auth: String): ApiResponse<ModSBHistoryResponse>

    // Push Notifications
    @POST("device-token")
    suspend fun registerDeviceToken(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    @HTTP(method = "DELETE", path = "device-token", hasBody = true)
    suspend fun removeDeviceToken(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    @GET("notification-preferences")
    suspend fun getNotificationPreferences(@Header("Authorization") auth: String): ApiResponse<JsonObject>

    @POST("notification-preferences")
    suspend fun updateNotificationPreferences(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    // Mod - Notifications
    @POST("mod/send-notification")
    suspend fun sendModNotification(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    @GET("mod/notification-history")
    suspend fun getNotificationHistory(@Header("Authorization") auth: String): ApiResponse<JsonObject>

    // YouTube
    @GET("socials/youtube-videos")
    suspend fun fetchYouTubeVideos(
        @Header("Authorization") auth: String = "",
        @Query("limit") limit: Int = 10
    ): ApiResponse<YouTubeResponse>

    @GET("socials/youtube-shorts")
    suspend fun fetchYouTubeShorts(
        @Header("Authorization") auth: String = "",
        @Query("limit") limit: Int = 10
    ): ApiResponse<YouTubeResponse>

    // Twitter / X
    @GET("socials/twitter-posts")
    suspend fun fetchTwitterPosts(
        @Query("limit") limit: Int = 10
    ): ApiResponse<TwitterResponse>

    // TikTok
    @GET("socials/tiktok-videos")
    suspend fun fetchTikTokVideos(
        @Query("limit") limit: Int = 10
    ): ApiResponse<TikTokResponse>

    // News / Tidings
    @GET("news")
    suspend fun fetchNews(@Query("platform") platform: String = "android"): ApiResponse<NewsResponse>

    // Mod - News / Tidings (all articles, no platform filter)
    @GET("mod/news")
    suspend fun fetchModNews(
        @Header("Authorization") auth: String
    ): ApiResponse<NewsResponse>

    // Mod - News / Tidings creation
    @POST("mod/news")
    suspend fun createNewsArticle(
        @Header("Authorization") auth: String,
        @Body body: CreateNewsBody
    ): ApiResponse<CreateNewsResponse>

    @Multipart
    @POST("mod/news/upload-image")
    suspend fun uploadNewsImage(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    ): ApiResponse<NewsImageUploadResponse>

    @POST("mod/news/update")
    suspend fun updateNewsArticle(
        @Header("Authorization") auth: String,
        @Body body: UpdateNewsBody
    ): ApiResponse<MessageResponse>

    @POST("mod/news/delete")
    suspend fun deleteNewsArticle(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<MessageResponse>

    @GET("mod/news/deleted")
    suspend fun fetchDeletedNewsArticles(
        @Header("Authorization") auth: String
    ): ApiResponse<NewsResponse>

    @POST("mod/news/restore")
    suspend fun restoreNewsArticle(
        @Header("Authorization") auth: String,
        @Body body: JsonObject
    ): ApiResponse<MessageResponse>

    // Twitch Token (for emote gallery)
    @GET("twitch-token")
    suspend fun fetchTwitchToken(@Header("Authorization") auth: String): ApiResponse<TwitchTokenResponse>

    // Captain's Dispatch
    @POST("captain/send-notification")
    suspend fun sendCaptainNotification(@Header("Authorization") auth: String, @Body body: JsonObject): ApiResponse<JsonObject>

    @GET("captain/notification-history")
    suspend fun getCaptainNotificationHistory(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 20
    ): ApiResponse<JsonObject>

    // Notification Permissions
    @GET("notification-permissions")
    suspend fun getNotificationPermissions(@Header("Authorization") auth: String): ApiResponse<JsonObject>
}
