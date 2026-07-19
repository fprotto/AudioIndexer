# Walkthrough - Metadata Cover Support

I have implemented support for extracting and displaying album and song covers from audio metadata.

## Changes Made

### 1. Dependencies
- Integrated **Coil 2.7.0** for efficient image loading from file URIs.
- Updated `libs.versions.toml` and `app/build.gradle.kts`.

### 2. Database and Domain Models
- **Database Schema (v3):** Added `coverType` and `coverValue` fields to `SongEntity`.
- **Domain Model:** Added a `cover: IconSource` field to the `Song` class.
- **Repository:**
    - Updated mappers to handle the new cover fields.
    - Added `saveArtwork(ByteArray): String?` to persist extracted metadata pictures as JPG files in the app's internal storage (`filesDir/covers/`).
    - Uses MD5 hashing of the image data for file naming to prevent redundant storage of the same artwork.

### 3. Sync Logic (`RemoteSyncWorker`)
- Enhanced the sync process to use `MediaMetadataRetriever.getEmbeddedPicture()`.
- Automatically extracts artwork during the initial song sync.
- **Album Covers:** When an album is created, it takes the artwork of the first song synced. If subsequent songs have artwork and the album is still using a default icon, the album cover is updated.

### 4. UI Components
- **Songs Screen:** `SongCard` now displays the metadata cover if available, falling back to the `MusicNote` icon.
- **Albums Screen:** `AlbumItem` and `AlbumHeader` (details) now display the extracted album art, falling back to the `Album` icon.

## How to Verify

1. **Deploy the app:** The database will undergo a destructive migration, clearing existing synced data.
2. **Sync a Remote Source:** Use a source containing files with embedded ID3 artwork.
3. **Check Covers:** Navigate to the "Songs" or "Albums" sections. You should see the actual covers instead of the generic icons.

> [!TIP]
> You can check `Logcat` for `RemoteSyncWorker` or `MusicRepository` logs if you want to verify the extraction process.
