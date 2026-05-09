package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

private data class FaqItem(
    val question: String,
    val answer: String
)

private data class FaqSection(
    val title: String,
    val items: List<FaqItem>
)

private val faqSections = listOf(
    FaqSection(
        title = "About Burke",
        items = listOf(
            FaqItem("Who is BurkeBlack?", "Burke is an avid gamer and nerd who loves comic books, sci-fi and fantasy movies, everything gaming, and making people laugh."),
            FaqItem("Business Contact Email", "burkeblack@fenixdown.co"),
            FaqItem("When is Burke's birthday?", "July 7th, 1979"),
            FaqItem("Where does Burke live?", "Indiana, USA"),
            FaqItem("Who does Burke live with?", "Burke currently lives with his father, better known as Dadmiral (Radlit1), and his mother."),
            FaqItem("What does Burke do for work?", "Burke is a full-time streamer (and pirate)."),
            FaqItem("Is Burke single, taken or married?", "Burke is married to the sea."),
            FaqItem("Is Burke standing while gaming?", "Yes, Burke does stand while gaming. He stands for 4 hours, sits for 2 hours, and then stands for another 4 hours. He uses a fatigue pad to avoid any leg and back pains. He uses the UPLIFT V2 Standing Desk (72x30 inch)"),
            FaqItem("Where did Burke get that hat?", "To get your own infamous pirate hat, visit the Swedish clothing company Longnose Leather"),
            FaqItem("Where did Burke get that mug?", "To get your own stainless steel handcrafted mug, search Amazon for Design Toscano JQ8967 Skullduggery Mug Beer"),
        )
    ),
    FaqSection(
        title = "About The Crew",
        items = listOf(
            FaqItem("Who are the Pirates?", "The Pirates are YOU, the crew of our mighty ship, The Dirty Skull. The community evolved from \"The Black Crew\" created during GTA V's launch, adopting pirate-themed rank titles like Corsairs and Buccaneers."),
            FaqItem("What is The Dirty Skull?", "The Dirty Skull is the name of our mighty ship, on the seas of Twitch. Members can join via the Discord community."),
            FaqItem("Who is The Late Shift?", "The Late Shift is a team of streamers, including BurkeBlack. Team members include CletusBueford, CrReaM, and GassyMexican. It represents both the streaming team and their combined communities."),
            FaqItem("Who are the administrators?", "Head Moderator: jstubbles manages moderators. Community Manager: BleuBelladonna handles organization. Tech Manager: swiftyspiffy oversees the Kraken Bot, website, and extension."),
            FaqItem("Who are the moderators?", "The moderation team includes jstubbles, BleuBelladonna, swiftyspiffy, A_p_p_l_e_s, Acebravo69, Xorshasia, and many more dedicated crew members."),
            FaqItem("What is the crew's Steam group?", "\"The Black Crew Twitch TV\" Steam community group serves the crew."),
        )
    ),
    FaqSection(
        title = "About The Stream",
        items = listOf(
            FaqItem("What is Burke's streaming schedule?", "Burke streams Monday through to Saturday, starting at 10PM EST and ending at 8AM EST. Sunday is Burke's shore leave. Times and dates are subject to change."),
            FaqItem("When did Burke start streaming?", "June 29th, 2013"),
            FaqItem("When did Burke get his sub button?", "July 7th, 2014"),
            FaqItem("What type of games does Burke usually stream?", "Burke is a variety streamer, so he will play just about any game out there."),
            FaqItem("Who made Burke's art?", "Multiple artists contributed, including Casy Nuf (emotes), JouJouet (extension backgrounds), Jstubbles (overlays, animations, alerts), Lorgarn (scene elements), SenzuArts/SaucyArts (animated emotes), Shticky (emotes), Twosenseless (badges, headers, alerts), and Venalis (film work)."),
            FaqItem("Who made Burke's website?", "The website was put together by Bennyfits, with additional items added by swiftyspiffy and BleuBelladonna."),
            FaqItem("Who made Burke's custom bot, The_Kraken_Bot?", "swiftyspiffy is the mastermind behind the channel bot."),
        )
    ),
    FaqSection(
        title = "Burke's Booty Extension",
        items = listOf(
            FaqItem("What are Extensions?", "Extensions are programmable, interactive overlays and panels, which help broadcasters interact with viewers through features like heat maps, real-time overlays, mini-games, and leaderboards."),
            FaqItem("What types of Extensions exist?", "Three types: panel extensions (below video player, always active), video-overlay extensions (transparent overlay on player, live only), and video-component extensions (partial screen, live only)."),
            FaqItem("What is Burke's Booty Extension?", "An interactive panel extension developed by swiftyspiffy for twitch.tv/burkeblack. Features include claiming/entering giveaways, purchasing soundbyte credits with bits, redeeming prizes, sending soundbytes, submitting feedback, and viewing doubloons/soundbyte credits."),
            FaqItem("How do I access Burke's Booty Extension?", "Located in the panel below the video player. Grant permissions by clicking the \"Grant Permissions\" icon (desktop) or clicking the \"BB\" logo and authenticating (mobile)."),
            FaqItem("How do I provide feedback on the extension?", "Mobile only: Click three-line icon, then \"Feedback,\" select OS, enter feedback, and click \"Send Feedback.\""),
            FaqItem("How do I enter a giveaway?", "When a giveaway is live, locate the extension and click the \"Enter Giveaway\" button to participate."),
            FaqItem("How do I leave a giveaway?", "While a giveaway is active, find the extension and click the \"Leave Giveaway\" button to withdraw your entry."),
            FaqItem("How do I claim a giveaway?", "After a giveaway ends, locate the extension and click the \"Claim\" button. Only the winner can access this button."),
            FaqItem("How do I send a soundbyte?", "Click the music note icon, select or search a soundbyte, then click \"Send.\" A bot message confirms submission in chat unless disabled in extension settings."),
            FaqItem("How do I earn more soundbyte credits?", "Earn through various stream activities. Purchase additional credits with bits: click plus icon, then \"Get More Soundbytes,\" then \"Get Credits\" (desktop only)."),
        )
    ),
    FaqSection(
        title = "Subscription",
        items = listOf(
            FaqItem("Pirates (Tier 1) - \$4.99/month", "60 Channel Emotes + 5 Animated Emotes, ad-free viewing, and access to game servers. Resub benefits include 50 Doubloons and 5 Soundbyte Credits."),
            FaqItem("Swashbucklers (Tier 2) - \$9.99/month", "Includes all Tier 1 benefits plus 5 Exclusive Tier 2 Emotes and higher multipliers."),
            FaqItem("Corsairs (Tier 3) - \$24.99/month", "Premium tier with 5 Exclusive Tier 3 Emotes and 2x Channel Points multiplier."),
            FaqItem("Prime Gaming - Free with Amazon Prime", "Same base benefits as Tier 1 with additional gaming benefits."),
            FaqItem("Gift Subscriptions", "Gift subscriptions to other viewers at any tier. The recipient receives all corresponding rewards."),
        )
    ),
    FaqSection(
        title = "Giveaways",
        items = listOf(
            FaqItem("How do I submit a giveaway?", "Access Burke's Booty Extension, click the present icon for \"Giveaway Submission Form,\" and complete the form with details including giveaway name, contributor name, type, key/code/link, doubloon requirement, and subscriber restrictions. A moderator must manually run it afterward. Desktop only."),
            FaqItem("How do I enter a giveaway?", "When a giveaway is live, locate Burke's Booty Extension and click the \"Enter Giveaway\" button to participate."),
            FaqItem("How do I leave a giveaway?", "While a giveaway is active, find Burke's Booty Extension and click the \"Leave Giveaway\" button to withdraw your entry."),
            FaqItem("How do I claim a giveaway?", "After a giveaway ends, locate Burke's Booty Extension and click the \"Claim\" button. Only the winner can access this button."),
            FaqItem("How do I redeem a giveaway?", "Click the trophy icon (\"Giveaway Wins\"), find your most recent win, and copy the key/code/link to redeem it elsewhere. On mobile, use the three-line menu, select \"Raffles,\" then \"Raffle Wins.\""),
        )
    ),
    FaqSection(
        title = "Doubloons",
        items = listOf(
            FaqItem("What are Doubloons?", "Doubloons are our custom currency system, used to reward viewers for watching the stream and subscribing."),
            FaqItem("What are Doubloons for?", "Doubloon amounts are used as entry requirements to gain access to community game servers and large giveaways."),
            FaqItem("How do I view my doubloons count?", "Your count displays at the bottom left of the extension with a refresh icon nearby. On mobile, tap the three-line menu icon on Burke's Booty Extension and select \"User Stats.\""),
            FaqItem("How do I earn more doubloons?", "Earn doubloons through various stream activities including watching the stream and subscribing. Rates are subject to change."),
        )
    ),
    FaqSection(
        title = "Soundbytes",
        items = listOf(
            FaqItem("What are Soundbytes?", "A soundbyte is a short clip of speech or music extracted from a longer piece of audio, often used to encourage funny moments, dancing and jump scares."),
            FaqItem("How do I submit my own soundbyte?", "Visit the Soundbyte Website and click \"Upload to Public Catalog.\" Submissions must be under 15 seconds, MP3 format, and exclude political, religious, copyrighted, or obscene content. Volume must match existing soundbytes."),
            FaqItem("How do I send a soundbyte?", "Click the music note icon on Burke's Booty Extension (or access via three-line menu on mobile). Search or select a soundbyte, optionally listen, then click \"Send.\" A bot message confirms submission in chat unless disabled in extension settings."),
            FaqItem("What are Soundbyte Credits?", "Soundbyte Credits are the currency used to send a soundbyte."),
            FaqItem("How do I view my soundbyte credits count?", "Credits display at the bottom right of the extension with a refresh icon. On mobile, click the three-line icon, then \"User Stats.\""),
            FaqItem("How do I earn more soundbyte credits?", "Earn through various stream activities at rates shown in the extension (rates subject to change). Purchase additional credits with bits via the \"Get More Soundbytes\" plus icon in the extension."),
        )
    ),
    FaqSection(
        title = "Channel Points",
        items = listOf(
            FaqItem("What are Channel Points?", "Channel Points is a customizable points program that lets streamers reward members of their community with perks, including a taste of benefits typically reserved for subscribers."),
            FaqItem("What are Gold Coins?", "Gold Coins is the custom name BurkeBlack uses for the Channel Points system."),
            FaqItem("How do I earn more gold coins?", "Earn Channel Points through various stream activities including watching, chatting, and follow streaks. Rates are subject to change."),
            FaqItem("What can I spend my gold coins on?", "Burke has set up custom Channel Point rewards. Both the available rewards and their costs are subject to change. Check the rewards menu on Twitch for the full list."),
        )
    ),
)

@Composable
fun FaqScreen(onBack: () -> Unit) {
    LaunchedEffect(Unit) { AppLogger.log("FAQ: appeared") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PirateTheme.accentColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.HelpOutline,
                contentDescription = null,
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Information & FAQ",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 22.sp,
                color = PirateTheme.accentColor
            )
        }

        // FAQ sections
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            faqSections.forEach { section ->
                FaqSectionCard(section)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FaqSectionCard(section: FaqSection) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 20.sp,
                    color = PirateTheme.accentColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = PirateTheme.accentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expandable Q&A items
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f))
                ) {
                    HorizontalDivider(color = PirateTheme.accentColor.copy(alpha = 0.15f))
                    section.items.forEachIndexed { index, item ->
                        FaqItemRow(item)
                        if (index < section.items.lastIndex) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItemRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2693",
                fontSize = 14.sp,
                color = PirateTheme.accentColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.question,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                text = item.answer,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 8.dp)
            )
        }
    }
}
