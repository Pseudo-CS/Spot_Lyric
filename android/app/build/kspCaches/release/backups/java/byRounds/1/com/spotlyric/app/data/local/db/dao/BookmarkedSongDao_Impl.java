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
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity;
import java.lang.Class;
import java.lang.Exception;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BookmarkedSongDao_Impl implements BookmarkedSongDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookmarkedSongEntity> __insertionAdapterOfBookmarkedSongEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public BookmarkedSongDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookmarkedSongEntity = new EntityInsertionAdapter<BookmarkedSongEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bookmarked_song` (`id`,`songName`,`artistName`,`bookmarkedUrl`,`title`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookmarkedSongEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSongName());
        statement.bindString(3, entity.getArtistName());
        statement.bindString(4, entity.getBookmarkedUrl());
        statement.bindString(5, entity.getTitle());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM bookmarked_song WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final BookmarkedSongEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBookmarkedSongEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BookmarkedSongEntity>> getAllFlow() {
    final String _sql = "SELECT * FROM bookmarked_song ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bookmarked_song"}, new Callable<List<BookmarkedSongEntity>>() {
      @Override
      @NonNull
      public List<BookmarkedSongEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSongName = CursorUtil.getColumnIndexOrThrow(_cursor, "songName");
          final int _cursorIndexOfArtistName = CursorUtil.getColumnIndexOrThrow(_cursor, "artistName");
          final int _cursorIndexOfBookmarkedUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkedUrl");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final List<BookmarkedSongEntity> _result = new ArrayList<BookmarkedSongEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookmarkedSongEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSongName;
            _tmpSongName = _cursor.getString(_cursorIndexOfSongName);
            final String _tmpArtistName;
            _tmpArtistName = _cursor.getString(_cursorIndexOfArtistName);
            final String _tmpBookmarkedUrl;
            _tmpBookmarkedUrl = _cursor.getString(_cursorIndexOfBookmarkedUrl);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            _item = new BookmarkedSongEntity(_tmpId,_tmpSongName,_tmpArtistName,_tmpBookmarkedUrl,_tmpTitle);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookmarkedSongEntity>> searchFlow(final String query) {
    final String _sql = "SELECT * FROM bookmarked_song \n"
            + "           WHERE songName LIKE '%' || ? || '%' \n"
            + "              OR artistName LIKE '%' || ? || '%' \n"
            + "           ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bookmarked_song"}, new Callable<List<BookmarkedSongEntity>>() {
      @Override
      @NonNull
      public List<BookmarkedSongEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSongName = CursorUtil.getColumnIndexOrThrow(_cursor, "songName");
          final int _cursorIndexOfArtistName = CursorUtil.getColumnIndexOrThrow(_cursor, "artistName");
          final int _cursorIndexOfBookmarkedUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkedUrl");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final List<BookmarkedSongEntity> _result = new ArrayList<BookmarkedSongEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookmarkedSongEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSongName;
            _tmpSongName = _cursor.getString(_cursorIndexOfSongName);
            final String _tmpArtistName;
            _tmpArtistName = _cursor.getString(_cursorIndexOfArtistName);
            final String _tmpBookmarkedUrl;
            _tmpBookmarkedUrl = _cursor.getString(_cursorIndexOfBookmarkedUrl);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            _item = new BookmarkedSongEntity(_tmpId,_tmpSongName,_tmpArtistName,_tmpBookmarkedUrl,_tmpTitle);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object findBySongAndArtist(final String songName, final String artistName,
      final Continuation<? super BookmarkedSongEntity> $completion) {
    final String _sql = "SELECT * FROM bookmarked_song \n"
            + "           WHERE LOWER(songName) = LOWER(?) \n"
            + "             AND LOWER(artistName) = LOWER(?) \n"
            + "           LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, songName);
    _argIndex = 2;
    _statement.bindString(_argIndex, artistName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BookmarkedSongEntity>() {
      @Override
      @Nullable
      public BookmarkedSongEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSongName = CursorUtil.getColumnIndexOrThrow(_cursor, "songName");
          final int _cursorIndexOfArtistName = CursorUtil.getColumnIndexOrThrow(_cursor, "artistName");
          final int _cursorIndexOfBookmarkedUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkedUrl");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final BookmarkedSongEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSongName;
            _tmpSongName = _cursor.getString(_cursorIndexOfSongName);
            final String _tmpArtistName;
            _tmpArtistName = _cursor.getString(_cursorIndexOfArtistName);
            final String _tmpBookmarkedUrl;
            _tmpBookmarkedUrl = _cursor.getString(_cursorIndexOfBookmarkedUrl);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            _result = new BookmarkedSongEntity(_tmpId,_tmpSongName,_tmpArtistName,_tmpBookmarkedUrl,_tmpTitle);
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
  public Object findById(final long id,
      final Continuation<? super BookmarkedSongEntity> $completion) {
    final String _sql = "SELECT * FROM bookmarked_song WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BookmarkedSongEntity>() {
      @Override
      @Nullable
      public BookmarkedSongEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSongName = CursorUtil.getColumnIndexOrThrow(_cursor, "songName");
          final int _cursorIndexOfArtistName = CursorUtil.getColumnIndexOrThrow(_cursor, "artistName");
          final int _cursorIndexOfBookmarkedUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarkedUrl");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final BookmarkedSongEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSongName;
            _tmpSongName = _cursor.getString(_cursorIndexOfSongName);
            final String _tmpArtistName;
            _tmpArtistName = _cursor.getString(_cursorIndexOfArtistName);
            final String _tmpBookmarkedUrl;
            _tmpBookmarkedUrl = _cursor.getString(_cursorIndexOfBookmarkedUrl);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            _result = new BookmarkedSongEntity(_tmpId,_tmpSongName,_tmpArtistName,_tmpBookmarkedUrl,_tmpTitle);
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
  public Object getBookmarkedUrls(final String songName, final String artistName,
      final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT bookmarkedUrl FROM bookmarked_song \n"
            + "           WHERE LOWER(songName) = LOWER(?) \n"
            + "             AND LOWER(artistName) = LOWER(?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, songName);
    _argIndex = 2;
    _statement.bindString(_argIndex, artistName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
