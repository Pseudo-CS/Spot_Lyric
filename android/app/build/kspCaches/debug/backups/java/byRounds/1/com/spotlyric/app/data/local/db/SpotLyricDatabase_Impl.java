package com.spotlyric.app.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao;
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao_Impl;
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao;
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao_Impl;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SpotLyricDatabase_Impl extends SpotLyricDatabase {
  private volatile BookmarkedSongDao _bookmarkedSongDao;

  private volatile SongLyricsDao _songLyricsDao;

  private volatile PreferredSourceDao _preferredSourceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarked_song` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `songName` TEXT NOT NULL, `artistName` TEXT NOT NULL, `bookmarkedUrl` TEXT NOT NULL, `title` TEXT NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bookmarked_song_songName_artistName` ON `bookmarked_song` (`songName`, `artistName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `song_lyrics` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookmarkId` INTEGER NOT NULL, `originalLyrics` TEXT NOT NULL, `translatedLyrics` TEXT NOT NULL, `aiRomanized` TEXT, `aiTranslation` TEXT, `sourceUrl` TEXT, `extractionStage` TEXT, `confidenceScore` REAL, `originalLanguage` TEXT, FOREIGN KEY(`bookmarkId`) REFERENCES `bookmarked_song`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_song_lyrics_bookmarkId` ON `song_lyrics` (`bookmarkId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `preferred_source` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `domain` TEXT NOT NULL, `displayName` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_preferred_source_domain` ON `preferred_source` (`domain`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '37bf19357c820353b8414390a79ae538')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `bookmarked_song`");
        db.execSQL("DROP TABLE IF EXISTS `song_lyrics`");
        db.execSQL("DROP TABLE IF EXISTS `preferred_source`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBookmarkedSong = new HashMap<String, TableInfo.Column>(5);
        _columnsBookmarkedSong.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarkedSong.put("songName", new TableInfo.Column("songName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarkedSong.put("artistName", new TableInfo.Column("artistName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarkedSong.put("bookmarkedUrl", new TableInfo.Column("bookmarkedUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmarkedSong.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBookmarkedSong = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBookmarkedSong = new HashSet<TableInfo.Index>(1);
        _indicesBookmarkedSong.add(new TableInfo.Index("index_bookmarked_song_songName_artistName", true, Arrays.asList("songName", "artistName"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoBookmarkedSong = new TableInfo("bookmarked_song", _columnsBookmarkedSong, _foreignKeysBookmarkedSong, _indicesBookmarkedSong);
        final TableInfo _existingBookmarkedSong = TableInfo.read(db, "bookmarked_song");
        if (!_infoBookmarkedSong.equals(_existingBookmarkedSong)) {
          return new RoomOpenHelper.ValidationResult(false, "bookmarked_song(com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity).\n"
                  + " Expected:\n" + _infoBookmarkedSong + "\n"
                  + " Found:\n" + _existingBookmarkedSong);
        }
        final HashMap<String, TableInfo.Column> _columnsSongLyrics = new HashMap<String, TableInfo.Column>(10);
        _columnsSongLyrics.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("bookmarkId", new TableInfo.Column("bookmarkId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("originalLyrics", new TableInfo.Column("originalLyrics", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("translatedLyrics", new TableInfo.Column("translatedLyrics", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("aiRomanized", new TableInfo.Column("aiRomanized", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("aiTranslation", new TableInfo.Column("aiTranslation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("sourceUrl", new TableInfo.Column("sourceUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("extractionStage", new TableInfo.Column("extractionStage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("confidenceScore", new TableInfo.Column("confidenceScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongLyrics.put("originalLanguage", new TableInfo.Column("originalLanguage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSongLyrics = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSongLyrics.add(new TableInfo.ForeignKey("bookmarked_song", "CASCADE", "NO ACTION", Arrays.asList("bookmarkId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSongLyrics = new HashSet<TableInfo.Index>(1);
        _indicesSongLyrics.add(new TableInfo.Index("index_song_lyrics_bookmarkId", false, Arrays.asList("bookmarkId"), Arrays.asList("ASC")));
        final TableInfo _infoSongLyrics = new TableInfo("song_lyrics", _columnsSongLyrics, _foreignKeysSongLyrics, _indicesSongLyrics);
        final TableInfo _existingSongLyrics = TableInfo.read(db, "song_lyrics");
        if (!_infoSongLyrics.equals(_existingSongLyrics)) {
          return new RoomOpenHelper.ValidationResult(false, "song_lyrics(com.spotlyric.app.data.local.db.entity.SongLyricsEntity).\n"
                  + " Expected:\n" + _infoSongLyrics + "\n"
                  + " Found:\n" + _existingSongLyrics);
        }
        final HashMap<String, TableInfo.Column> _columnsPreferredSource = new HashMap<String, TableInfo.Column>(5);
        _columnsPreferredSource.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreferredSource.put("domain", new TableInfo.Column("domain", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreferredSource.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreferredSource.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreferredSource.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPreferredSource = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPreferredSource = new HashSet<TableInfo.Index>(1);
        _indicesPreferredSource.add(new TableInfo.Index("index_preferred_source_domain", true, Arrays.asList("domain"), Arrays.asList("ASC")));
        final TableInfo _infoPreferredSource = new TableInfo("preferred_source", _columnsPreferredSource, _foreignKeysPreferredSource, _indicesPreferredSource);
        final TableInfo _existingPreferredSource = TableInfo.read(db, "preferred_source");
        if (!_infoPreferredSource.equals(_existingPreferredSource)) {
          return new RoomOpenHelper.ValidationResult(false, "preferred_source(com.spotlyric.app.data.local.db.entity.PreferredSourceEntity).\n"
                  + " Expected:\n" + _infoPreferredSource + "\n"
                  + " Found:\n" + _existingPreferredSource);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "37bf19357c820353b8414390a79ae538", "2d456aa2ed61b0338658bdc1d392fa2d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "bookmarked_song","song_lyrics","preferred_source");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `bookmarked_song`");
      _db.execSQL("DELETE FROM `song_lyrics`");
      _db.execSQL("DELETE FROM `preferred_source`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BookmarkedSongDao.class, BookmarkedSongDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SongLyricsDao.class, SongLyricsDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PreferredSourceDao.class, PreferredSourceDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BookmarkedSongDao bookmarkedSongDao() {
    if (_bookmarkedSongDao != null) {
      return _bookmarkedSongDao;
    } else {
      synchronized(this) {
        if(_bookmarkedSongDao == null) {
          _bookmarkedSongDao = new BookmarkedSongDao_Impl(this);
        }
        return _bookmarkedSongDao;
      }
    }
  }

  @Override
  public SongLyricsDao songLyricsDao() {
    if (_songLyricsDao != null) {
      return _songLyricsDao;
    } else {
      synchronized(this) {
        if(_songLyricsDao == null) {
          _songLyricsDao = new SongLyricsDao_Impl(this);
        }
        return _songLyricsDao;
      }
    }
  }

  @Override
  public PreferredSourceDao preferredSourceDao() {
    if (_preferredSourceDao != null) {
      return _preferredSourceDao;
    } else {
      synchronized(this) {
        if(_preferredSourceDao == null) {
          _preferredSourceDao = new PreferredSourceDao_Impl(this);
        }
        return _preferredSourceDao;
      }
    }
  }
}
