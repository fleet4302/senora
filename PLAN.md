# Sonora - Self-Hosted Music Streaming Architecture & Implementation Plan

"Sonora" is a self-hosted music app that operates on the Stremio model for music:
- **Catalog ("What exists")**: MusicBrainz + Cover Art Archive + LRCLIB (synced lyrics) + Deezer/ListenBrainz charts.
- **Source ("Where to get it")**: Soulseek P2P network. Tracks are resolved dynamically from Soulseek peers, queued, and progressively streamed.
- **Presentation**: Apple Music-inspired visual design, translucent materials, dynamic color extraction, mini-player capsule, full-screen lyric-synced player, Sources sheet (Stremio-style stream picker), Library management, and Transfers panel.

---

## 1. Stack Choices & Justification

### UI & Architecture
- **Framework**: Modern Android Jetpack Compose with Material 3 theming styled precisely after Apple Music's design language (signature vibrant red/pink accent `#FC3C44`, translucent floating bars, blur effects, smooth card shelves, typography hierarchy).
- **Architecture**: Clean Architecture / MVVM.
  - `data/catalog`: Providers for MusicBrainz API, Cover Art Archive, LRCLIB synced lyrics, Deezer charts.
  - `data/source`: Abstract `SourceProvider` interface with `SoulseekSourceProvider` (implements Soulseek client protocol, slsk search & peer transfer protocol) and `SimulatedSoulseekProvider` (offline-resilient mock peer swarm with realistic bitrates, queues, speeds, and partial-content audio synthesis/streams).
  - `data/resolver`: Ranking engine scoring matches: title similarity, album track count, duration tolerance (<= 3s), format priority (FLAC > 320kbps > V0 > 192), upload slots, queue length, and peer speed. Pre-resolves next track in queue.
  - `data/player`: Progressive audio player utilizing Android's Media framework with local partial file caching, HTTP Range support, buffering states, and lock-screen MediaSession notifications.
  - `data/library`: Room Database for persisted library (Artists, Albums, Songs, Playlists, Transfers, LRU Cache metadata).

### Why Direct Android Core with Socket/HTTP Layer?
- Android has full TCP socket capabilities (`java.net.Socket`, `ServerSocket`) allowing direct Soulseek protocol communication (server handshake, peer connection, message parsing) without requiring a separate external NodeJS/Python daemon, while also supporting connecting to external `slskd` instances via REST/WebSocket if configured in Settings.
- Built-in local HTTP server/caching proxy serves chunks via standard `http://127.0.0.1:<port>` with HTTP Range headers for progressive playback in `MediaPlayer` or direct progressive playback from the downloaded cache files.

---

## 2. Key Risks & Mitigations

1. **Soulseek Peer Availability & Search Latency**:
   - *Risk*: Soulseek queries take 3–8 seconds to gather peer responses, and peers may have locked files, slow upload speeds, or long queues.
   - *Mitigation*: 6-second resolution budget with progressive ranking as peer search responses stream in. Transparent UI states: "Resolving on Soulseek...", "Queued at peer (pos #2)", "Buffering 1.2MB...", "Playing FLAC 24-bit 96kHz". Instant manual fallback via "Sources" sheet. Pre-resolve the next queued track in background.

2. **Network Offline / Firewall Restrictions in Sandbox**:
   - *Risk*: Cloud emulator sandbox or strict firewalls may block outbound TCP connections to Soulseek server (port 2242) or incoming peer connections.
   - *Mitigation*: Implement dual-engine `SoulseekProvider`:
     1. Live Soulseek protocol engine (configurable server `server.slsknet.org:2242` or custom slskd gateway).
     2. Built-in `SimulatedSoulseekProvider` with authentic Soulseek peer swarms (peers like `vocaloid_king`, `flac_hoarder_99`, `indie_vault`, `lossprevention`), real audio streams, and simulated download rates/queues to ensure the app is 100% functional immediately in any test or demo environment.

3. **Rate Limits on MusicBrainz**:
   - *Risk*: MusicBrainz limits non-commercial requests to 1 request/second and requires a descriptive User-Agent.
   - *Mitigation*: Enforce a strict token bucket / 1 req/sec throttler in the OkHttp interceptor with `User-Agent: Sonora/1.0.0 (https://github.com/sonora-music)` and in-memory cache for artists/release-groups.

4. **Audio File Integrity & Security**:
   - *Risk*: Peer filenames and metadata are untrusted.
   - *Mitigation*: Strict path traversal prevention (`basename` validation, no `..`, path sanitization), file extension whitelist (`.mp3`, `.flac`, `.m4a`, `.ogg`, `.wav`), zero execution privileges.

---

## 3. Milestones & Implementation Sequence

- **M0: Foundation & Core Setup**:
  - Update `metadata.json`, `strings.xml`, `applicationId` in `app/build.gradle.kts`.
  - Generate custom Sonora launcher icon with distinctive soundwave/music note branding.
  - Enable `coil-compose` in `build.gradle.kts` for artwork loading.
- **M1: Catalog & Soulseek Resolver Architecture**:
  - MusicBrainz API client & models (Artist, Album, Track, Release Group, Cover Art Archive URLs).
  - Deezer charts / Listen Now discovery recommendations.
  - LRCLIB client for synchronized time-stamped lyrics.
  - Soulseek protocol models & resolver: scoring algorithm (format, bitrate, slots, queue, speed), candidate ranking, and source selection.
- **M2: Audio Player & Progressive Streaming**:
  - Audio playback engine with buffering notifications, seekable position, progressive cache, pre-fetching next track, and MediaSession integration.
- **M3: Apple Music-Inspired UI (Mobile + Tablet/Desktop Layouts)**:
  - Material 3 theme with signature Apple Music styling (translucent tab bar, red-pink accent `#FC3C44`, dark/light glass panels).
  - Bottom navigation (Listen Now, Browse, Library, Search, Transfers).
  - Card shelves (Recently Played, Top Picks, New Releases).
  - Album view & Artist view with hero banners, action buttons (Play, Shuffle, Add to Library).
  - Floating Mini-Player capsule transitioning to Full-Screen Player with blurred dynamic artwork background, interactive time-synced lyrics, Up Next queue, and Stremio-style "Sources" sheet.
- **M4: Library, Transfers, Settings & Soulseek Search**:
  - Library tabs: Playlists, Artists, Albums, Songs, Downloaded.
  - Raw Soulseek search scope (files, folders, users, share browsing).
  - Active transfers manager (download speed, progress, queue position, ETA).
  - Settings (Soulseek credentials, server, format preference FLAC/320, cache size, sharing folders).
- **M5: Compilation & Polish**:
  - Verify complete end-to-end functionality and flawless build with `compile_applet`.
