package com.swiftyspiffy.burkeblackapp.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// API response wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class ApiErrorOrSuccess(
    val success: Boolean,
    val error: String? = null
)

// Home
@Serializable
data class HomeData(
    @SerialName("is_live") val isLive: Boolean,
    val stream: StreamInfo? = null,
    val announcements: List<Announcement> = emptyList(),
    val stats: ChannelStats? = null
)

@Serializable
data class StreamInfo(
    val id: String,
    val title: String,
    val game: String,
    @SerialName("viewer_count") val viewerCount: Int,
    @SerialName("started_at") val startedAt: String? = null
)

@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val date: String
)

@Serializable
data class ChannelStats(
    val followers: Int,
    val subscribers: Int
)

// Stream status
@Serializable
data class StreamStatusResponse(
    @SerialName("is_live") val isLive: Boolean,
    val title: String? = null,
    @SerialName("game_name") val gameName: String? = null,
    @SerialName("viewer_count") val viewerCount: Int? = null
)

// Schedule
@Serializable
data class ScheduleItem(
    val id: String,
    val title: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String? = null,
    val game: String? = null
)

// Profile
@Serializable
data class ProfileData(
    @SerialName("display_name") val displayName: String,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarURL: String? = null
)

// Dashboard
@Serializable
data class DashboardData(
    @SerialName("user_id") val userId: String,
    val username: String,
    val doubloons: Int = 0,
    @SerialName("soundbyte_credits") val soundbyteCredits: Int = 0,
    val donations: Double = 0.0,
    @SerialName("events_donations") val eventsDonations: Double? = null,
    @SerialName("total_bits") val totalBits: Int? = null,
    @SerialName("follow_date") val followDate: String? = null,
    @SerialName("latest_sub") val latestSub: LatestSubData? = null,
    @SerialName("giveaways_entered") val giveawaysEntered: Int = 0,
    @SerialName("giveaways_won") val giveawaysWon: Int = 0,
    @SerialName("giveaways_donated") val giveawaysDonated: Int = 0,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("soundbyte_sends") val soundbyteSends: Int? = null,
    @SerialName("fav_soundbyte") val favSoundbyte: FavSoundbyteData? = null,
    @SerialName("last_soundbyte") val lastSoundbyte: LastSoundbyteData? = null,
    @SerialName("soundbytes_enabled") val soundbytesEnabled: Boolean? = null
)

@Serializable
data class LatestSubData(
    val tier: String,
    @SerialName("cumulative_months") val cumulativeMonths: Int,
    @SerialName("streak_months") val streakMonths: Int,
    @SerialName("is_gift") val isGift: Boolean,
    val gifter: String? = null,
    val message: String? = null,
    val date: String
)

@Serializable
data class FavSoundbyteData(
    val name: String,
    val count: Int
)

@Serializable
data class LastSoundbyteData(
    val name: String,
    val date: Long
)

// User status
@Serializable
data class UserStatus(
    val follows: Boolean,
    val subscribed: Boolean,
    @SerialName("sub_tier") val subTier: String? = null,
    @SerialName("user_role") val userRole: String,
    @SerialName("is_moderator") val isModerator: Boolean? = null,
    @SerialName("is_sub_gifter") val isSubGifter: Boolean? = null,
    @SerialName("is_bits_sender") val isBitsSender: Boolean? = null,
    @SerialName("followed_at") val followedAt: String? = null
)

// Soundbytes
@Serializable
data class Soundbyte(
    val id: Int,
    val name: String,
    val location: String,
    @SerialName("uploaded_by") val uploadedBy: String,
    val genre: String,
    val horror: Int,
    @SerialName("credit_cost") val creditCost: Int,
    val plays: Int,
    @SerialName("last_played_by") val lastPlayedBy: String? = null
)

@Serializable
data class SoundbytesResponse(
    val soundbytes: List<Soundbyte>,
    val total: Int,
    val offset: Int,
    val amount: Int
)

@Serializable
data class SoundbyteSendResponse(
    val message: String,
    @SerialName("soundbyte_name") val soundbyteName: String,
    @SerialName("credits_remaining") val creditsRemaining: Int
)

@Serializable
data class SoundbyteHistoryItem(
    @SerialName("soundbyte_id") val soundbyteId: Int,
    val name: String,
    val announced: Boolean,
    val date: Long
)

@Serializable
data class SoundbyteHistoryResponse(
    val history: List<SoundbyteHistoryItem>
)

@Serializable
data class SoundbyteGenre(
    val id: Int,
    val genre: String
)

@Serializable
data class SoundbyteGenresResponse(
    val genres: List<SoundbyteGenre>
)

@Serializable
data class SoundbyteCreditsResponse(
    @SerialName("soundbyte_credits") val soundbyteCredits: Int
)

@Serializable
data class SoundbyteSendBody(
    @SerialName("soundbyte_id") val soundbyteId: Int,
    val announce: Int
)

// Bits
@Serializable
data class BitCheer(
    val bits: Int,
    val message: String,
    val date: String
)

@Serializable
data class BitsResponse(
    val bits: List<BitCheer>
)

// Donations
@Serializable
data class Donation(
    val amount: Double,
    val currency: String,
    val message: String,
    val date: String
)

@Serializable
data class DonationsResponse(
    val donations: List<Donation>
)

// Giveaways
@Serializable
data class GiveawayEntry(
    val name: String,
    val donator: String,
    @SerialName("giveaway_state") val giveawayState: String,
    @SerialName("entry_state") val entryState: String,
    val date: Long
)

@Serializable
data class GiveawayWin(
    val name: String,
    val donator: String,
    val prize: String
)

@Serializable
data class GiveawayDonated(
    val name: String,
    val state: String,
    @SerialName("winner_userid") val winnerUserid: String,
    val date: Long
)

@Serializable
data class GiveawayEntriesResponse(
    @SerialName("giveaway_entries") val giveawayEntries: List<GiveawayEntry>
)

@Serializable
data class GiveawayWinsResponse(
    @SerialName("giveaway_wins") val giveawayWins: List<GiveawayWin>
)

@Serializable
data class GiveawayDonatedResponse(
    @SerialName("giveaway_donated") val giveawayDonated: List<GiveawayDonated>
)

// Mod panel - Permissions (PHP returns ints as flexible int/string)
@Serializable
data class ModPermissions(
    val admin: Int = 0,
    val commands: Int = 0,
    val chat: Int = 0,
    val timeout: Int = 0,
    val spoiler: Int = 0,
    val timed: Int = 0,
    val links: Int = 0,
    val giveaways: Int = 0,
    val audio: Int = 0,
    val settings: Int = 0,
    val news: String = "0"
) {
    val hasAdmin get() = admin == 1
    val hasCommands get() = commands == 1
    val hasChat get() = chat == 1
    val hasTimeout get() = timeout == 1
    val hasSpoiler get() = spoiler == 1
    val hasTimed get() = timed == 1
    val hasLinks get() = links == 1
    val hasGiveaways get() = giveaways == 1
    val hasAudio get() = audio == 1
    val hasSettings get() = settings == 1
    val hasNews get() = news == "1"
}

@Serializable
data class ModSettings(
    @SerialName("soundbytes_enabled") val soundbytesEnabled: Boolean,
    @SerialName("enforce_punishments") val enforcePunishments: Boolean
)

@Serializable
data class ToggleSBResponse(
    @SerialName("soundbytes_enabled") val soundbytesEnabled: Boolean
)

@Serializable
data class ToggleCEResponse(
    @SerialName("enforce_punishments") val enforcePunishments: Boolean
)

@Serializable
data class MessageResponse(
    val message: String
)

// Mod - Commands
@Serializable
data class ModCommand(
    val id: Int? = null,
    val command: String = "",
    val data: String = "",
    val tier: String = "viewer",
    val tags: String = "",
    val cooldown: Int = 0
)

@Serializable
data class ModCommandsResponse(
    val commands: List<ModCommand>
)

// Mod - Timed Messages
@Serializable
data class ModTimedMessage(
    val id: Int? = null,
    val name: String = "",
    val message: String = "",
    val interval: Int = 15,
    val dedicated: Int = 0,
    val disabled: Int = 0
)

@Serializable
data class ModTimedResponse(
    val messages: List<ModTimedMessage>
)

// Mod - Links
@Serializable
data class ModLink(
    val id: Int? = null,
    @SerialName("custom_name") val customName: String = "",
    @SerialName("long_url") val longUrl: String = ""
)

@Serializable
data class ModLinksResponse(
    val links: List<ModLink>
)

// Mod - Timeout Words
@Serializable
data class ModTimeoutWord(
    val id: Int? = null,
    val word: String = "",
    val category: String = "",
    val silent: Int = 0,
    @SerialName("part_of") val partOf: Int = 0,
    val enabled: Int = 1
)

@Serializable
data class ModTOCategory(
    val id: Int,
    val category: String
)

@Serializable
data class ModTOWordsResponse(
    val words: List<ModTimeoutWord>,
    val categories: List<ModTOCategory> = emptyList()
)

// Mod - Spoiler Words
@Serializable
data class ModSpoilerWord(
    val id: Int? = null,
    val word: String = "",
    val silent: Int = 0,
    @SerialName("part_of") val partOf: Int = 0,
    val enabled: Int = 1
)

@Serializable
data class ModSpoilerResponse(
    val words: List<ModSpoilerWord>
)

// Mod - Viewer Lookup
@Serializable
data class ViewerLookupResult(
    val viewer: String = "",
    val twitch: ViewerTwitchInfo? = null,
    val doubloons: Int = 0,
    @SerialName("soundbyte_credits") val soundbyteCredits: Int = 0,
    @SerialName("giveaway_wins") val giveawayWins: List<GiveawayWin> = emptyList()
)

@Serializable
data class ViewerTwitchInfo(
    val id: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    val type: String? = null,
    @SerialName("profile_image_url") val profileImageUrl: String? = null
)

// Mod - Giveaway Submissions
@Serializable
data class GiveawaySubmission(
    val id: Int,
    @SerialName("real_user") val realUser: String = "",
    val date: String = "",
    @SerialName("suggested_user") val suggestedUser: String = "",
    @SerialName("giveaway_name") val giveawayName: String = "",
    @SerialName("giveaway_type") val giveawayType: String = "",
    @SerialName("giveaway_data") val giveawayData: String = "",
    @SerialName("giveaway_extra") val giveawayExtra: String = "",
    val hidden: Int = 0
)

@Serializable
data class SubmissionsResponse(
    val submissions: List<GiveawaySubmission>
)

@Serializable
data class GiveawayHistoryItem(
    val id: Int,
    val timestamp: String = "",
    val author: String = "",
    val name: String = "",
    val donator: String = "",
    val prize: String = "",
    val state: String = "",
    @SerialName("winner_userid") val winnerUserid: String = "",
    val filter: String = "",
    @SerialName("filter_value") val filterValue: String = ""
)

@Serializable
data class GiveawayHistoryResponse(
    val giveaways: List<GiveawayHistoryItem>
)

@Serializable
data class ViewWinsResponse(
    val wins: List<GiveawayWin>
)

// Mod - Soundbyte Library
@Serializable
data class ModSoundbyte(
    val id: Int,
    @SerialName("audio_name") val audioName: String = "",
    @SerialName("uploaded_by") val uploadedBy: String = "",
    val genre: String = "",
    val approved: Int = 0,
    @SerialName("horror_night") val horrorNight: Int = 0,
    @SerialName("credit_cost") val creditCost: Int = 1,
    @SerialName("mod_only") val modOnly: Int = 0,
    @SerialName("audio_location") val audioLocation: String = ""
)

@Serializable
data class ModSBLibraryResponse(
    val soundbytes: List<ModSoundbyte>,
    val genres: List<SoundbyteGenre> = emptyList()
)

@Serializable
data class ModSBCredit(
    val id: Int,
    @SerialName("user_id") val userId: String = "",
    @SerialName("credit_count") val creditCount: Int = 0,
    val username: String? = null
)

@Serializable
data class ModSBCreditsResponse(
    val credits: List<ModSBCredit>,
    val total: Int? = null
)

@Serializable
data class ModSBHistoryItem(
    @SerialName("sb_id") val sbId: Int = 0,
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    val announce: Int = 0,
    val platform: String = "",
    val timestamp: String = "",
    @SerialName("audio_name") val audioName: String? = null
)

@Serializable
data class ModSBHistoryResponse(
    val history: List<ModSBHistoryItem>
)

// Reviewer login
@Serializable
data class ReviewerResponse(
    val token: String,
    val username: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_moderator") val isModerator: Boolean? = null
)

// Clip Voting
@Serializable
data class ClipVotingConfig(
    @SerialName("voting_mode") val votingMode: String,
    @SerialName("multi_type") val multiType: String? = null,
    @SerialName("vote_count") val voteCount: Int = 1,
    @SerialName("clip_count") val clipCount: Int = 10,
    @SerialName("show_view_counts") val showViewCounts: Boolean = false,
    @SerialName("show_points") val showPoints: Boolean = false,
    @SerialName("show_vote_counts") val showVoteCounts: Boolean = false
)

@Serializable
data class ClipVotingResponse(
    val month: String,
    @SerialName("period_start") val periodStart: String,
    @SerialName("period_end") val periodEnd: String,
    val clips: List<VotingClip>,
    @SerialName("total_voters") val totalVoters: Int = 0,
    val authenticated: Boolean = false,
    @SerialName("user_has_voted") val userHasVoted: Boolean = false,
    val config: ClipVotingConfig
)

@Serializable
data class VotingClip(
    @SerialName("clip_id") val clipId: String,
    val title: String,
    @SerialName("clip_url") val clipUrl: String,
    @SerialName("embed_url") val embedUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("creator_name") val creatorName: String,
    @SerialName("view_count") val viewCount: Int = 0,
    val duration: Float = 0f,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("user_rank") val userRank: Int? = null
)

@Serializable
data class ClipVoteBody(
    val rankings: List<String>
)

@Serializable
data class ClipVoteResponse(
    val month: String,
    val message: String
)

// Community Game Servers
@Serializable
data class CommunityServersResponse(
    val servers: List<CommunityServer>,
    val authenticated: Boolean = false
)

@Serializable
data class CommunityServer(
    val id: Int,
    @SerialName("server_name") val serverName: String,
    @SerialName("game_name") val gameName: String,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("require_sub") val requireSub: Boolean = false,
    @SerialName("require_follow") val requireFollow: Boolean = false,
    @SerialName("require_allowlist") val requireAllowlist: Boolean = false,
    val info: String? = null,
    @SerialName("has_access") val hasAccess: Boolean = false,
    @SerialName("ip_address") val ipAddress: String? = null,
    val password: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// News / Tidings
@Serializable
data class NewsResponse(
    val articles: List<NewsArticle>
)

@Serializable
data class NewsArticle(
    val id: Int,
    val subject: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_by_username") val createdByUsername: String,
    @SerialName("is_visible_on_ios") val isVisibleOnIos: Int = 1,
    @SerialName("is_visible_on_android") val isVisibleOnAndroid: Int = 1,
    @SerialName("is_visible_on_website") val isVisibleOnWebsite: Int = 1
)

@Serializable
data class CreateNewsBody(
    val subject: String,
    val body: String,
    @SerialName("is_visible_on_ios") val isVisibleOnIos: Int = 1,
    @SerialName("is_visible_on_android") val isVisibleOnAndroid: Int = 1,
    @SerialName("is_visible_on_website") val isVisibleOnWebsite: Int = 1,
    @SerialName("send_push") val sendPush: Int = 1
)

@Serializable
data class UpdateNewsBody(
    val id: Int,
    val subject: String,
    val body: String,
    @SerialName("is_visible_on_ios") val isVisibleOnIos: Int = 1,
    @SerialName("is_visible_on_android") val isVisibleOnAndroid: Int = 1,
    @SerialName("is_visible_on_website") val isVisibleOnWebsite: Int = 1
)

@Serializable
data class CreateNewsResponse(
    val message: String,
    val id: Int
)

@Serializable
data class NewsImageUploadResponse(
    val url: String,
    val filename: String
)

// Twitch Token
@Serializable
data class TwitchTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("client_id") val clientId: String
)

// YouTube
@Serializable
data class YouTubeItem(
    @SerialName("video_id") val videoId: String,
    val title: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val views: Int = 0,
    val type: String,
    val url: String,
    @SerialName("uploaded_at") val uploadedAt: String
)

@Serializable
data class YouTubeResponse(
    val items: List<YouTubeItem>,
    val total: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0
)

// TikTok
@Serializable
data class TikTokItem(
    @SerialName("video_id") val videoId: String,
    val title: String,
    val description: String = "",
    val url: String,
    @SerialName("cover_image_url") val coverImageUrl: String,
    val duration: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("share_count") val shareCount: Int = 0,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("published_at") val publishedAt: String = ""
)

@Serializable
data class TikTokResponse(
    val items: List<TikTokItem>,
    val total: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0
)

// Twitter
@Serializable
data class TwitterPost(
    @SerialName("tweet_id") val tweetId: String,
    @SerialName("author_username") val authorUsername: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_profile_image_url") val authorProfileImageUrl: String,
    val text: String,
    val url: String,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("retweet_count") val retweetCount: Int = 0,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("quote_count") val quoteCount: Int = 0,
    @SerialName("media_urls") val mediaUrls: List<String> = emptyList(),
    @SerialName("is_retweet") val isRetweet: Boolean = false,
    @SerialName("is_reply") val isReply: Boolean = false,
    @SerialName("is_quote") val isQuote: Boolean = false,
    @SerialName("published_at") val publishedAt: String = ""
)

@Serializable
data class TwitterResponse(
    val items: List<TwitterPost>,
    val total: Int = 0,
    val limit: Int = 20,
    val offset: Int = 0
)

// Feedback
@Serializable
data class FeedbackBody(
    val username: String,
    @SerialName("user_id") val userId: String? = null,
    val target: String,
    val message: String,
    val images: List<String>? = null,
    val diagnostics: String? = null
)
