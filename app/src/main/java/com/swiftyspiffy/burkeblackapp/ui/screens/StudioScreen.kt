package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

// region Data models

private data class Sponsor(
    val name: String,
    val description: String,
    val url: String,
    val color: Color
)

private data class GalleryPhoto(
    val label: String,
    @DrawableRes val imageRes: Int
)

private data class StudioItem(
    val name: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val amazonUrl: String
)

private data class StudioSection(
    val title: String,
    val items: List<StudioItem>
)

// endregion

// region Data

private val sponsors = listOf(
    Sponsor(
        "ViewSonic",
        "ViewSonic is a leading global provider of visual display products and solutions, dedicated to delivering innovative and reliable displays for gaming, entertainment, and professional use.",
        "https://www.viewsonic.com",
        Color(0xFF1A237E)
    ),
    Sponsor(
        "Elgato",
        "Elgato is a world-leading provider of audiovisual technology, synonymous with quality and performance, for content creators on all video sharing platforms.",
        "https://www.elgato.com",
        Color(0xFF1B2838)
    ),
    Sponsor(
        "Origin PC",
        "ORIGIN PC builds custom, high-performance gaming desktops, laptops, and workstations designed to deliver the ultimate computing experience.",
        "https://www.originpc.com",
        Color(0xFF8B1A1A)
    ),
    Sponsor(
        "Wyrmwood",
        "Wyrmwood crafts premium quality gaming furniture and accessories, designed by gamers for gamers, using the finest hardwoods and materials.",
        "https://www.wyrmwoodgaming.com",
        Color(0xFF5D4037)
    ),
)

private val galleryPhotos = listOf(
    GalleryPhoto("Captain's Quarters", R.drawable.studio_captains_quarters),
    GalleryPhoto("Streaming Space", R.drawable.studio_streaming_space),
    GalleryPhoto("Lounge", R.drawable.studio_lounge),
    GalleryPhoto("Games & Cabinets", R.drawable.studio_games_cabinets),
    GalleryPhoto("Games & Cabinets", R.drawable.studio_games_cabinets_alt),
    GalleryPhoto("Tabletop Area", R.drawable.studio_tabletop_area),
)

private val studioSections = listOf(
    StudioSection(
        title = "Lighting",
        items = listOf(
            StudioItem(
                "Aputure LS 60d Focusing LED",
                "The LS 60d is a battery-powerable daylight-balanced focusing LED that uses custom aspherical optics to achieve a 15-45\u00B0 spot-flood beam angle with intense output and flexibility.",
                R.drawable.equip_aputure_ls_60d,
                "https://amzn.to/3oPc5ef"
            ),
            StudioItem(
                "Aputure LS 60x Bi-Color Adjustable LED",
                "The LS 60x is a battery-powerable bi-color focusing LED that uses custom aspherical optics to achieve a 15-45\u00B0 spot-flood beam angle with intense output and flexibility.",
                R.drawable.equip_aputure_ls_60x,
                "https://amzn.to/3s0GYyj"
            ),
            StudioItem(
                "Aputure Lantern Softbox",
                "The Lantern Softbox utilizes a 26\" spherical design, spreading the light from your LED or Studio Strobe in all directions with a 270\u00B0 beam angle.",
                R.drawable.equip_aputure_lantern,
                "https://amzn.to/3rZocHr"
            ),
            StudioItem(
                "Elgato Key Light",
                "The Elgato Key Light uses 160 premium OSRAM LEDs, that output a massive 2800-lumens, to ensure extra-bright illumination you can dim down to a subtle glow.",
                R.drawable.equip_elgato_key_light,
                "https://amzn.to/30kpXDE"
            ),
        )
    ),
    StudioSection(
        title = "Cameras",
        items = listOf(
            StudioItem("Sony Alpha A6000", "Freeze fast moving moments with 0.06 Auto-focus, BIONZ X processor and 24.3MP.", R.drawable.equip_sony_a6000, "https://amzn.to/31SfKiz"),
            StudioItem("Logitech Brio 4K Pro Webcam", "Captures every detail in crisp, high-resolution color with frame rates up to 90 fps.", R.drawable.equip_logitech_brio, "https://amzn.to/3sTe42t"),
        )
    ),
    StudioSection(
        title = "Audio",
        items = listOf(
            StudioItem("RODECaster Pro II", "The ultimate audio production solution for streamers, podcasters, and musicians.", R.drawable.equip_rodecaster_pro2, "https://www.amazon.com/dp/B0BV6NK6HW"),
            StudioItem("Corsair Virtuoso Wireless Headset", "High-fidelity audio experience with memory foam earpads and Slipstream Wireless technology.", R.drawable.equip_corsair_virtuoso, "https://amzn.to/32HNJuw"),
            StudioItem("Astro A10 Gaming Headset", "Tuned for gaming with immersive and accurate audio and precise voice communication.", R.drawable.equip_astro_a10, "https://amzn.to/3LGiRec"),
            StudioItem("Audio-Technica BP894cT4 Condenser Microphone", "Head worn condenser microphone with a cardioid polar pattern.", R.drawable.equip_audio_technica_bp894ct4, "https://amzn.to/3pM9IJo"),
            StudioItem("Shure GLXD4 Single Channel Wireless Receiver", "Revolutionary LINKFREQ Automatic Frequency Management on the 2.4 GHz frequency band.", R.drawable.equip_shure_glxd4, "https://amzn.to/3zh8f0Y"),
            StudioItem("Shure GLXD1 Bodypack Transmitter", "Ergonomic design and reversible belt clip with 16 hours continuous use.", R.drawable.equip_shure_glxd1, "https://amzn.to/3Px5YXf"),
            StudioItem("Klipsch ProMedia 2.1 Bluetooth Speaker System", "The next generation of the legendary Klipsch ProMedia 2.1 offering exceptional sound.", R.drawable.equip_klipsch_speaker, "https://amzn.to/3z00ZbP"),
        )
    ),
    StudioSection(
        title = "Gaming PC Specs",
        items = listOf(
            StudioItem("Corsair iCUE 5000x RGB Mid-Tower ATX Case", "A stunning, showpiece-worthy PC with beautiful tempered glass.", R.drawable.equip_corsair_5000x, "https://bit.ly/3Nyvmgn"),
            StudioItem("Microsoft Windows 10 Home", "The latest features and security with faster startups and an expanded Start menu.", R.drawable.equip_windows_10, "https://www.amazon.com/dp/B01019BM7O"),
            StudioItem("Intel Core i9-13900KS 6.0 GHz 24-Core Processor", "Blazing-fast 6.0GHz max clock speed, 24 cores, and 32 threads.", R.drawable.equip_intel_i9_13900ks, "https://www.amazon.com/dp/B0BPXCRWB2"),
            StudioItem("Corsair iCUE H150i Elite CAPELLIX XT", "360mm radiator, 3x 120mm fans, copper cold plate with stunning CAPELLIX LEDs.", R.drawable.equip_corsair_capellix, "https://www.amazon.com/dp/B0BQJ6QL7L"),
            StudioItem("ASUS ROG MAXIMUS Z790 Hero DDR5 Motherboard", "Immense 20-stage power solution, hyperspeed DDR5 memory with PCIe 5.0 connectivity.", R.drawable.equip_asus_maximus_z790, "https://www.amazon.com/dp/B0BG6M53DG"),
            StudioItem("Corsair 4TB MP600 PRO XT GEN 4", "PCIe Gen4 x4 extreme data performance with phenomenal read/write speeds.", R.drawable.equip_corsair_mp600, "https://www.amazon.com/dp/B09F5XC93H"),
            StudioItem("Samsung 2TB 870 QVO SATA", "Samsung's latest 2nd generation QLC SSD with up to 8TB storage capacity.", R.drawable.equip_samsung_870_qvo, "https://www.amazon.com/dp/B089C6LZ42"),
            StudioItem("NVIDIA GeForce RTX 4090 Graphics Card", "The ultimate GeForce GPU.", R.drawable.equip_nvidia_rtx_4090, "https://www.amazon.com/dp/B09YD4FJ5R"),
            StudioItem("Corsair RM1200x SHIFT 80 PLUS Gold Power Supply", "Fully modular power supplies with revolutionary patent-pending side cable interface.", R.drawable.equip_rm1200x_shift, "https://www.amazon.com/dp/B0BP88MYM4"),
            StudioItem("Corsair iCUE QL120 RGB 120mm Fan", "Incredibly compact with USB 2.0 and SATA connections.", R.drawable.equip_corsair_ql120_fan, "https://www.amazon.com/dp/B07Z9SW756"),
        )
    ),
    StudioSection(
        title = "Streaming PC Specs",
        items = listOf(
            StudioItem("Corsair iCUE 5000x Case", "Mid-tower ATX case with four stunning tempered glass panels.", R.drawable.equip_origin_icue, "https://amzn.to/39QXVUR"),
            StudioItem("Microsoft Windows 10 Home", "The latest features and security with faster startups.", R.drawable.equip_windows_10, "https://www.amazon.com/dp/B01019BM7O"),
            StudioItem("AMD Ryzen 9 5950X", "Unprecedented speed of the world's best desktop processors.", R.drawable.equip_amd_ryzen_9, "https://amzn.to/3lESsCY"),
            StudioItem("Corsair iCUE H150i RGB PRO XT", "All-in-one liquid CPU cooler with 360mm radiator and three CORSAIR ML120 PWM fans.", R.drawable.equip_corsair_icue_h150i, "https://amzn.to/3PyCSa1"),
            StudioItem("MSI MEG X570 GODLIKE Motherboard", "Flagship 14+4+1 phases IR digital VRM symbolizing unlimited performance.", R.drawable.equip_msi_meg_godlike, "https://amzn.to/3wOhmWd"),
            StudioItem("Corsair Dominator Platinum 64GB DDR4", "Superior aluminum craftsmanship with tightly screened high-frequency memory chips.", R.drawable.equip_corsair_dominator, "https://amzn.to/3PFthOs"),
            StudioItem("Samsung 980 PRO SSD 1TB PCIe NVMe M.2", "2x the data transfer rate of PCIe 3.0, while maintaining compatibility.", R.drawable.equip_samsung_980, "https://amzn.to/3lFCTLv"),
            StudioItem("GIGABYTE GeForce RTX 4070 Ti Super Eagle OC 16G", "Stunning visuals with amazingly fast frame rates and 16 GB of GDDR6X memory.", R.drawable.equip_gigabyte_4070ti_super, "https://www.amazon.com/dp/B0CSK87B4R/"),
            StudioItem("Corsair AX1600i Digital Power Supply", "The ultimate digital ATX power supply delivering more than 94% efficiency.", R.drawable.equip_corsair_axi, "https://amzn.to/3wFyD4S"),
            StudioItem("Corsair QL120 RGB 120mm Fan", "Spectacular lighting from all angles with 102 individually adjustable RGB LEDs.", R.drawable.equip_corsair_ql120_fan, "https://amzn.to/3MLtb68"),
            StudioItem("Elgato 4K60 Pro MK.2 Capture Card", "Capture 4K60 HDR10 content with ultra low latency Instant Gameview.", R.drawable.equip_elgato_4k60, "https://amzn.to/3LKQ5cu"),
            StudioItem("Elgato Cam Link Pro", "Powerful video mixer capturing four HDMI signals from cameras and computers.", R.drawable.equip_elgato_camlink, "https://amzn.to/3Nrgz3S"),
        )
    ),
    StudioSection(
        title = "Peripherals",
        items = listOf(
            StudioItem("ViewSonic ELITE XG270 27\" Monitor", "Blazing fast 240Hz refresh rate, 1ms response time with vibrant IPS color.", R.drawable.equip_viewsonic_xg270, "https://www.amazon.com/dp/B0BCXJ7XXM"),
            StudioItem("Alienware AW3225QF 32\" 4K QD-OLED Monitor", "Unrivaled viewing experience with 4K resolution, curved panel, Dolby Vision and 240Hz.", R.drawable.equip_alienware_aw3225qf, "https://www.dell.com/en-us/shop/alienware-32-4k-qd-oled-gaming-monitor-aw3225qf/apd/210-blmq/monitors-monitor-accessories"),
            StudioItem("LG OLED C1 Series 48\" Monitor", "No detail goes unseen with advanced gaming technology like NVIDIA G-SYNC.", R.drawable.equip_lg_oled_c1, "https://amzn.to/3MG40SD"),
            StudioItem("Corsair K100 RGB Mechanical Gaming Keyboard", "Aluminum design, per-key RGB lighting with Corsair AXON Hyper-Processing.", R.drawable.equip_corsair_k100_rgb, "https://amzn.to/3qFH2Rz"),
            StudioItem("Corsair Scimitar RGB Gaming Mouse", "Key Slider control system with 12 mechanical side buttons and 12000 DPI optical sensor.", R.drawable.equip_corsair_scimitar, "https://amzn.to/3FJKUr7"),
            StudioItem("Logitech G600 MMO Gaming Mouse", "20 buttons making it the most customizable mouse for mastering your favorite MMOs.", R.drawable.equip_logitech_g600, "https://amzn.to/3lDuKao"),
            StudioItem("Elgato Stream Deck", "15 LCD keys at your fingertips for unlimited studio control.", R.drawable.equip_elgato_stream_deck, "https://amzn.to/3qHBZQP"),
            StudioItem("Logitech G13 Programmable Gameboard", "Game-changing comfort and control with onboard memory for 5 ready-to-play profiles.", R.drawable.equip_logitech_g13, "https://amzn.to/3lEzSeu"),
            StudioItem("Thrustmaster T.16000M FCS Space Sim Duo", "Combines two joysticks, enabling gamers to play with both hands.", R.drawable.equip_thrustmaster_t1600, "https://amzn.to/3Gb656m"),
            StudioItem("Logitech G Farm Simulator Side Panel", "A bumper crop of immersive gear designed to enhance the excitement.", R.drawable.equip_logitech_farm_panel, "https://amzn.to/3LNHlCE"),
            StudioItem("Logitech G920 USB Steering Wheel w/ Pedals", "The definitive sim racing wheel with realistic dual-motor force feedback.", R.drawable.equip_logitech_g920, "https://amzn.to/3wD1yFm"),
            StudioItem("CyberPower PFC Sinewave UPS System", "Mini-tower UPS providing battery backup and surge protection.", R.drawable.equip_cyberpower_ups, "https://www.amazon.com/dp/B00429N19W"),
            StudioItem("HTC Vive PRO 2", "High visual fidelity, balanced ergonomics with sub-millimeter tracking accuracy.", R.drawable.equip_htc_vive_pro2, "https://amzn.to/3wEyn62"),
            StudioItem("Meta Quest 2", "The most advanced VR system offering new dimensions of gaming, social and entertainment.", R.drawable.equip_meta_quest_2, "https://amzn.to/3wDfIHQ"),
            StudioItem("Playstation VR", "A new world of unexpected gaming experiences with moments so intense.", R.drawable.equip_playstation_vr, "https://amzn.to/3MFXKdt"),
        )
    ),
    StudioSection(
        title = "Furniture",
        items = listOf(
            StudioItem("Wyrmwood Modular Gaming Table", "A new benchmark for gaming tables designed to accommodate every member of your party.", R.drawable.equip_wyrmwood_table, "https://wyrmwoodgaming.com/modular-gaming-table/"),
            StudioItem("UPLIFT v2 80\" Inch Standing Desk", "Maximize the comfort and productivity with thoughtfully designed furniture.", R.drawable.equip_uplift_v2, "https://www.upliftdesk.com/uplift-v2-standing-desk-v2-or-v2-commercial/"),
            StudioItem("Ikea Idasen Standing Desk", "A sturdy desk built to outlast years of coffee and hard work.", R.drawable.equip_ikea_idasen, "https://www.ikea.com/us/en/p/idasen-desk-sit-stand-black-dark-gray-s79280998/"),
            StudioItem("Herman Miller Aeron Chair: Gaming Edition (Size C)", "The iconic Aeron ideally suited for gamers' specific needs with customized support.", R.drawable.equip_herman_aeron, "https://amzn.to/3yUZfQX"),
            StudioItem("Under Desk Drawer Unit", "Clean look that's easy to like with all sides that are just as beautiful.", R.drawable.equip_alex_drawer, "https://www.ikea.com/us/en/p/alex-drawer-unit-drop-file-storage-black-brown-10508178/"),
            StudioItem("Retro Game Vault", "Media Cabinet With Drawers providing waist high storage without stooping.", R.drawable.equip_retro_game_vault, "https://amzn.to/3ES8wse"),
            StudioItem("War Gaming Figures Cabinet", "Classic yet modern glass-door cabinet with simple, well-thought-out details.", R.drawable.equip_regissoer_cabinet, "https://www.ikea.com/us/en/p/regissoer-glass-door-cabinet-brown-50342077/"),
            StudioItem("Cube Shelf Storage", "Shelving unit or room divider that adapts to taste and budget.", R.drawable.equip_kallax_shelf, "https://www.ikea.com/us/en/p/kallax-shelf-unit-black-brown-70301542/"),
            StudioItem("Board Game Storage", "Cluttered items can be placed out of sight while showcasing favorite objects.", R.drawable.equip_besta_cabinet, "https://www.ikea.com/us/en/p/besta-storage-combination-with-doors-black-brown-lappviken-stubbarp-black-brown-s59301765/"),
        )
    ),
    StudioSection(
        title = "Pirate Wall",
        items = listOf(
            StudioItem("Godzilla (Godzilla vs. Kong)", "", R.drawable.equip_godzilla_sideshow, "https://www.sideshow.com/collectibles/godzilla-vs-kong-godzilla-prime-1-studio-908118"),
            StudioItem("Assassin's Creed Edward Kenway Statuette", "", R.drawable.equip_ac_edward_kenway, "https://www.amazon.com/dp/B09GRTV9JY"),
            StudioItem("Decorative Gothic Skull with a Dragon", "", R.drawable.equip_dragon_skull, "https://amzn.to/3yVqaw6"),
            StudioItem("Melting Gold Skull", "", R.drawable.equip_melting_skull, "https://jackofthedust.com/products/melting-gold-skull"),
            StudioItem("Pete the Undead Pirate Parrot", "", R.drawable.equip_pete_parrot, "https://amzn.to/3wEZZqC"),
            StudioItem("Robert Louis Stevenson: Seven Novels", "", R.drawable.equip_rls_novels, "https://www.amazon.com/dp/160710315X"),
            StudioItem("Wooden Beer Mug", "", R.drawable.equip_wooden_mug, "https://www.etsy.com/listing/513473306"),
            StudioItem("STAR WARS Collector Fleet Star Destroyer", "", R.drawable.equip_star_destroyer, "https://amzn.to/3GdTbVt"),
            StudioItem("Godzilla (Godzilla: King of the Monsters)", "", R.drawable.equip_godzilla_kotm, "https://www.tamashiinations.com/product/details.php?detail=79"),
            StudioItem("King Ghidorah (Godzilla: King of the Monsters)", "", R.drawable.equip_ghidorah_kotm, "https://www.tamashiinations.com/product/details.php?detail=87"),
        )
    ),
    StudioSection(
        title = "Miscellaneous",
        items = listOf(
            StudioItem("PSY Acoustics Wall Panels", "Handmade studio grade acoustic panels with customizable high-resolution artwork.", R.drawable.equip_acoustic_panels, "https://psyacoustics.com/"),
            StudioItem("Elgato Master Mount (L)", "Modular rigging system allowing you to prop and lock your camera, light or phone.", R.drawable.equip_elgato_master_mount, "https://amzn.to/38vKlpr"),
            StudioItem("EZM Deluxe Pyramid Quad Monitor Mount", "Sturdy, high-quality ergonomic stand allowing users to manage multiple programs.", R.drawable.equip_ezm_quad_mount, "https://amzn.to/3PTnTr5"),
            StudioItem("HUANUO Triple Monitor Stand", "Solid construction and heavy duty base securely holds 3 monitors up to 32\".", R.drawable.equip_huanuo_triple_mount, "https://amzn.to/3M14elQ"),
            StudioItem("Nintendo Switch", "Single and multiplayer thrills at home with full home console experience anytime.", R.drawable.equip_nintendo_switch, "https://www.amazon.com/dp/B07VGRJDFY"),
            StudioItem("Playstation 5", "Fast loading with an ultra-high speed SSD with deeper immersion and 3D Audio.", R.drawable.equip_playstation_5, "https://www.amazon.com/dp/B09DFCB66S"),
            StudioItem("Xbox Series X", "The fastest, most powerful Xbox ever with thousands of titles from four generations.", R.drawable.equip_xbox_series_x, "https://www.amazon.com/dp/B08H75RTZ8"),
            StudioItem("Lenovo Legion Y720 15.6\" Gaming Laptop", "Built for gaming with 7th Generation Intel Core i7 processors and NVIDIA GeForce GTX 1060.", R.drawable.equip_lenovo_laptop, "https://www.amazon.com/dp/B06WVQ7SQL"),
        )
    ),
)

// endregion

@Composable
fun StudioScreen(onBack: () -> Unit) {
    LaunchedEffect(Unit) { AppLogger.log("Studio: appeared") }
    val uriHandler = LocalUriHandler.current
    val bgColor = MaterialTheme.colorScheme.background
    var fullscreenPhotoIndex by remember { mutableIntStateOf(-1) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
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
                painter = androidx.compose.ui.res.painterResource(com.swiftyspiffy.burkeblackapp.R.drawable.ic_burke_captain),
                contentDescription = "Captain's Studio",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The Captain's Studio",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 24.sp,
                color = PirateTheme.accentColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // === Sponsors ===
            SponsorSection(uriHandler = uriHandler)

            Spacer(modifier = Modifier.height(32.dp))

            // === Streaming in Style / YouTube ===
            StreamingInStyleSection(uriHandler = uriHandler)

            Spacer(modifier = Modifier.height(32.dp))

            // === Gallery ===
            GallerySection(onPhotoClick = { index -> fullscreenPhotoIndex = index })

            Spacer(modifier = Modifier.height(32.dp))

            // === Equipment Sections ===
            EquipmentSections(uriHandler = uriHandler)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Fullscreen photo viewer
    if (fullscreenPhotoIndex >= 0) {
        FullscreenPhotoViewer(
            initialIndex = fullscreenPhotoIndex,
            photos = galleryPhotos,
            onDismiss = { fullscreenPhotoIndex = -1 }
        )
    }
    } // end Box
}

// region Fullscreen Viewer

@Composable
private fun FullscreenPhotoViewer(
    initialIndex: Int,
    photos: List<GalleryPhoto>,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Swipeable pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val photo = photos[page]
            var scale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2.5f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onTap = { onDismiss() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(photo.imageRes),
                    contentDescription = photo.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Photo label at bottom
        Text(
            text = photos[pagerState.currentPage].label,
            fontFamily = PirateTheme.fontFamily,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

// endregion

// region Sponsors

@Composable
private fun SponsorSection(uriHandler: androidx.compose.ui.platform.UriHandler) {
    Text(
        text = "Sponsors",
        fontFamily = PirateTheme.fontFamily,
        fontSize = 22.sp,
        color = PirateTheme.accentColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    sponsors.forEach { sponsor ->
        SponsorCard(sponsor = sponsor, onOpenUrl = { uriHandler.openUri(sponsor.url) })
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SponsorCard(sponsor: Sponsor, onOpenUrl: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = sponsor.color),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sponsor.name,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 24.sp,
                    color = Color.White
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = sponsor.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Learn more at: ${sponsor.url}",
                        fontSize = 13.sp,
                        color = PirateTheme.accentColor,
                        modifier = Modifier.clickable(onClick = onOpenUrl)
                    )
                }
            }
        }
    }
}

// endregion

// region Streaming in Style

@Composable
private fun StreamingInStyleSection(uriHandler: androidx.compose.ui.platform.UriHandler) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Streaming in Style",
            fontFamily = PirateTheme.fontFamily,
            fontSize = 28.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Follow Captain BurkeBlack as he gives a tour of his NEW pirate studio!",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // YouTube thumbnail card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { uriHandler.openUri("https://www.youtube.com/watch?v=nZlZl131rdA") }
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/nZlZl131rdA/hqdefault.jpg",
                    contentDescription = "Pirate Studio Tour 2022",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                // Play button overlay
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF0000).copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// endregion

// region Gallery

@Composable
private fun GallerySection(onPhotoClick: (Int) -> Unit = {}) {
    Text(
        text = "The Captain's Quarters",
        fontFamily = PirateTheme.fontFamily,
        fontSize = 22.sp,
        color = PirateTheme.accentColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        galleryPhotos.forEachIndexed { index, photo ->
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(280.dp)
                    .clickable { onPhotoClick(index) }
            ) {
                Box {
                    Image(
                        painter = painterResource(photo.imageRes),
                        contentDescription = photo.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f),
                        contentScale = ContentScale.Crop
                    )
                    // Label at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = photo.label,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// endregion

// region Equipment

@Composable
private fun EquipmentSections(uriHandler: androidx.compose.ui.platform.UriHandler) {
    Text(
        text = "The Captain's Gear",
        fontFamily = PirateTheme.fontFamily,
        fontSize = 22.sp,
        color = PirateTheme.accentColor,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    studioSections.forEach { section ->
        EquipmentSectionCard(section = section, uriHandler = uriHandler)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EquipmentSectionCard(
    section: StudioSection,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (section.items.isNotEmpty()) expanded = !expanded }
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
                if (section.items.isNotEmpty()) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = PirateTheme.accentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Coming Soon",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            // Expandable items
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
                        StudioItemRow(item = item, uriHandler = uriHandler)
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
private fun StudioItemRow(
    item: StudioItem,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    var showFullImage by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image (bundled, loaded via Coil for memory efficiency)
        AsyncImage(
            model = item.imageRes,
            contentDescription = item.name,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable { showFullImage = true },
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name + description + Amazon link
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 16.sp,
                color = PirateTheme.accentColor
            )
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PirateTheme.accentColor.copy(alpha = 0.15f))
                    .clickable { uriHandler.openUri(item.amazonUrl) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Find It Here!",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 13.sp,
                    color = PirateTheme.accentColor
                )
            }
        }
    }

    // Fullscreen equipment image viewer
    if (showFullImage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { showFullImage = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = item.imageRes,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.name,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 20.sp,
                    color = PirateTheme.accentColor,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = { showFullImage = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// endregion
