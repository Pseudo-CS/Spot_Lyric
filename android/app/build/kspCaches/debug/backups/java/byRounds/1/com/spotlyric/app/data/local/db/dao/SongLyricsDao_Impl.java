package com.spotlyric.app.data.local.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SongLyricsDao_Impl implements SongLyricsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SongLyricsEntity> __insertionAdapterOfSongLyricsEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAiFields;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByBookmarkId;

  public SongLyricsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSongLyricsEntity = new EntityInsertionAdapter<SongLyricsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `song_lyrics` (`id`,`bookmarkId`,`originalLyrics`,`translatedLyrics`,`aiRomanized`,`aiTranslation`,`sourceUrl`,`extractionStage`,`confidenceScore`,`originalLanguage`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SongLyricsEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBookmarkId());
        statement.bindString(3, entity.getOriginalLyrics());
        statement.bindString(4, entity.getTranslatedLyrics());
        if (entity.getAiRomanized() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAiRomanized());
        }
        if (entity.getAiTranslation() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAiTranslation());
        }
        if (entity.getSourceUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSourceUrl());
        }
        if (entity.getExtractionStage() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getExtractionStage());
        }
        if (entity.getConfidenceScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getConfidenceScore());
        }
        if (entity.getOriginalLanguage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getOriginalLanguage());
        }
      }
    };
    this.__preparedStmtOfUpdateAiFields = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE song_lyrics\n"
                + "           SET aiRomanized = ?, aiTranslation = ?\n"
                + "           WHERE bookmarkId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByBookmarkId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM song_lyrics WHERE bookmarkId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SongLyricsEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSongLyricsEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAiFields(final long bookmarkId, final String aiRomanized,
      final String aiTranslation, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAiFields.acquire();
        int _argIndex = 1;
        if (aiRomanized == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, aiRomanized);
        }
        _argIndex = 2;
        if (aiTranslation == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, aiTranslation);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, bookmarkId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateAiFields.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByBookmarkId(final long bookmarkId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByBookmarkId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, bookmarkId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByBookmarkId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object findByBookmarkId(final long bookmarkId,
      final Continuation<? super SongLyricsEntity> $completion) {
    final String _sql = "SELECT * FROM song_lyrics WHERE bookmarkId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookmarkId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SongLyricsEntity>() {
      @Override
      @Nullable
      public SongLyricsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBookmarkId = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkId");
          final int _cursorIndexOfOriginalLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLyrics");
          final int _cursorIndexOfTranslatedLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "translatedLyrics");
          final int _cursorIndexOfAiRomanized = CursorUtil.getColumnIndexOrThrow(_cursor, "aiRomanized");
          final int _cursorIndexOfAiTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "aiTranslation");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfExtractionStage = CursorUtil.getColumnIndexOrThrow(_cursor, "extractionStage");
          final int _cursorIndexOfConfidenceScore = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceScore");
          final int _cursorIndexOfOriginalLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLanguage");
          final SongLyricsEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBookmarkId;
            _tmpBookmarkId = _cursor.getLong(_cursorIndexOfBookmarkId);
            final String _tmpOriginalLyrics;
            _tmpOriginalLyrics = _cursor.getString(_cursorIndexOfOriginalLyrics);
            final String _tmpTranslatedLyrics;
            _tmpTranslatedLyrics = _cursor.getString(_cursorIndexOfTranslatedLyrics);
            final String _tmpAiRomanized;
            if (_cursor.isNull(_cursorIndexOfAiRomanized)) {
              _tmpAiRomanized = null;
            } else {
              _tmpAiRomanized = _cursor.getString(_cursorIndexOfAiRomanized);
            }
            final String _tmpAiTranslation;
            if (_cursor.isNull(_cursorIndexOfAiTranslation)) {
              _tmpAiTranslation = null;
            } else {
              _tmpAiTranslation = _cursor.getString(_cursorIndexOfAiTranslation);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpExtractionStage;
            if (_cursor.isNull(_cursorIndexOfExtractionStage)) {
              _tmpExtractionStage = null;
            } else {
              _tmpExtractionStage = _cursor.getString(_cursorIndexOfExtractionStage);
            }
            final Float _tmpConfidenceScore;
            if (_cursor.isNull(_cursorIndexOfConfidenceScore)) {
              _tmpConfidenceScore = null;
            } else {
              _tmpConfidenceScore = _cursor.getFloat(_cursorIndexOfConfidenceScore);
            }
            final String _tmpOriginalLanguage;
            if (_cursor.isNull(_cursorIndexOfOriginalLanguage)) {
              _tmpOriginalLanguage = null;
            } else {
              _tmpOriginalLanguage = _cursor.getString(_cursorIndexOfOriginalLanguage);
            }
            _result = new SongLyricsEntity(_tmpId,_tmpBookmarkId,_tmpOriginalLyrics,_tmpTranslatedLyrics,_tmpAiRomanized,_tmpAiTranslation,_tmpSourceUrl,_tmpExtractionStage,_tmpConfidenceScore,_tmpOriginalLanguage);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findBySongAndArtist(final String songName, final String artistName,
      final Continuation<? super SongLyricsEntity> $completion) {
    final String _sql = "SELECT sl.* FROM song_lyrics sl \n"
            + "           INNER JOIN bookmarked_song bs ON sl.bookmarkId = bs.id \n"
            + "           WHERE LOWER(bs.songName) = LOWER(?) \n"
            + "             AND LOWER(bs.artistName) = LOWER(?) \n"
            + "           LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, songName);
    _argIndex = 2;
    _statement.bindString(_argIndex, artistName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SongLyricsEntity>() {
      @Override
      @Nullable
      public SongLyricsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBookmarkId = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkId");
          final int _cursorIndexOfOriginalLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLyrics");
          final int _cursorIndexOfTranslatedLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "translatedLyrics");
          final int _cursorIndexOfAiRomanized = CursorUtil.getColumnIndexOrThrow(_cursor, "aiRomanized");
          final int _cursorIndexOfAiTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "aiTranslation");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfExtractionStage = CursorUtil.getColumnIndexOrThrow(_cursor, "extractionStage");
          final int _cursorIndexOfConfidenceScore = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceScore");
          final int _cursorIndexOfOriginalLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLanguage");
          final SongLyricsEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBookmarkId;
            _tmpBookmarkId = _cursor.getLong(_cursorIndexOfBookmarkId);
            final String _tmpOriginalLyrics;
            _tmpOriginalLyrics = _cursor.getString(_cursorIndexOfOriginalLyrics);
            final String _tmpTranslatedLyrics;
            _tmpTranslatedLyrics = _cursor.getString(_cursorIndexOfTranslatedLyrics);
            final String _tmpAiRomanized;
            if (_cursor.isNull(_cursorIndexOfAiRomanized)) {
              _tmpAiRomanized = null;
            } else {
              _tmpAiRomanized = _cursor.getString(_cursorIndexOfAiRomanized);
            }
            final String _tmpAiTranslation;
            if (_cursor.isNull(_cursorIndexOfAiTranslation)) {
              _tmpAiTranslation = null;
            } else {
              _tmpAiTranslation = _cursor.getString(_cursorIndexOfAiTranslation);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpExtractionStage;
            if (_cursor.isNull(_cursorIndexOfExtractionStage)) {
              _tmpExtractionStage = null;
            } else {
              _tmpExtractionStage = _cursor.getString(_cursorIndexOfExtractionStage);
            }
            final Float _tmpConfidenceScore;
            if (_cursor.isNull(_cursorIndexOfConfidenceScore)) {
              _tmpConfidenceScore = null;
            } else {
              _tmpConfidenceScore = _cursor.getFloat(_cursorIndexOfConfidenceScore);
            }
            final String _tmpOriginalLanguage;
            if (_cursor.isNull(_cursorIndexOfOriginalLanguage)) {
              _tmpOriginalLanguage = null;
            } else {
              _tmpOriginalLanguage = _cursor.getString(_cursorIndexOfOriginalLanguage);
            }
            _result = new SongLyricsEntity(_tmpId,_tmpBookmarkId,_tmpOriginalLyrics,_tmpTranslatedLyrics,_tmpAiRomanized,_tmpAiTranslation,_tmpSourceUrl,_tmpExtractionStage,_tmpConfidenceScore,_tmpOriginalLanguage);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<SongLyricsEntity>> $completion) {
    final String _sql = "SELECT * FROM song_lyrics";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SongLyricsEntity>>() {
      @Override
      @NonNull
      public List<SongLyricsEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBookmarkId = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkId");
          final int _cursorIndexOfOriginalLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLyrics");
          final int _cursorIndexOfTranslatedLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "translatedLyrics");
          final int _cursorIndexOfAiRomanized = CursorUtil.getColumnIndexOrThrow(_cursor, "aiRomanized");
          final int _cursorIndexOfAiTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "aiTranslation");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfExtractionStage = CursorUtil.getColumnIndexOrThrow(_cursor, "extractionStage");
          final int _cursorIndexOfConfidenceScore = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceScore");
          final int _cursorIndexOfOriginalLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "originalLanguage");
          final List<SongLyricsEntity> _result = new ArrayList<SongLyricsEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SongLyricsEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBookmarkId;
            _tmpBookmarkId = _cursor.getLong(_cursorIndexOfBookmarkId);
            final String _tmpOriginalLyrics;
            _tmpOriginalLyrics = _cursor.getString(_cursorIndexOfOriginalLyrics);
            final String _tmpTranslatedLyrics;
            _tmpTranslatedLyrics = _cursor.getString(_cursorIndexOfTranslatedLyrics);
            final String _tmpAiRomanized;
            if (_cursor.isNull(_cursorIndexOfAiRomanized)) {
              _tmpAiRomanized = null;
            } else {
              _tmpAiRomanized = _cursor.getString(_cursorIndexOfAiRomanized);
            }
            final String _tmpAiTranslation;
            if (_cursor.isNull(_cursorIndexOfAiTranslation)) {
              _tmpAiTranslation = null;
            } else {
              _tmpAiTranslation = _cursor.getString(_cursorIndexOfAiTranslation);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpExtractionStage;
            if (_cursor.isNull(_cursorIndexOfExtractionStage)) {
              _tmpExtractionStage = null;
            } else {
              _tmpExtractionStage = _cursor.getString(_cursorIndexOfExtractionStage);
            }
            final Float _tmpConfidenceScore;
            if (_cursor.isNull(_cursorIndexOfConfidenceScore)) {
              _tmpConfidenceScore = null;
            } else {
              _tmpConfidenceScore = _cursor.getFloat(_cursorIndexOfConfidenceScore);
            }
            final String _tmpOriginalLanguage;
            if (_cursor.isNull(_cursorIndexOfOriginalLanguage)) {
              _tmpOriginalLanguage = null;
            } else {
              _tmpOriginalLanguage = _cursor.getString(_cursorIndexOfOriginalLanguage);
            }
            _item = new SongLyricsEntity(_tmpId,_tmpBookmarkId,_tmpOriginalLyrics,_tmpTranslatedLyrics,_tmpAiRomanized,_tmpAiTranslation,_tmpSourceUrl,_tmpExtractionStage,_tmpConfidenceScore,_tmpOriginalLanguage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
