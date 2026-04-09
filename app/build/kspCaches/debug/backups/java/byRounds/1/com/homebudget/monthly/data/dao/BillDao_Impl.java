package com.homebudget.monthly.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.homebudget.monthly.data.entities.Bill;
import java.lang.Class;
import java.lang.Double;
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
public final class BillDao_Impl implements BillDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Bill> __insertionAdapterOfBill;

  private final EntityDeletionOrUpdateAdapter<Bill> __deletionAdapterOfBill;

  private final EntityDeletionOrUpdateAdapter<Bill> __updateAdapterOfBill;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePaidStatus;

  public BillDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBill = new EntityInsertionAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bills` (`id`,`name`,`amount`,`dueDay`,`categoryName`,`categoryIcon`,`categoryColor`,`isPaid`,`paidDate`,`reminderEnabled`,`reminderEmail`,`note`,`isRecurring`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getAmount());
        statement.bindLong(4, entity.getDueDay());
        statement.bindString(5, entity.getCategoryName());
        statement.bindString(6, entity.getCategoryIcon());
        statement.bindString(7, entity.getCategoryColor());
        final int _tmp = entity.isPaid() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getPaidDate() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPaidDate());
        }
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindString(11, entity.getReminderEmail());
        statement.bindString(12, entity.getNote());
        final int _tmp_2 = entity.isRecurring() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        statement.bindLong(14, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfBill = new EntityDeletionOrUpdateAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `bills` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBill = new EntityDeletionOrUpdateAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `bills` SET `id` = ?,`name` = ?,`amount` = ?,`dueDay` = ?,`categoryName` = ?,`categoryIcon` = ?,`categoryColor` = ?,`isPaid` = ?,`paidDate` = ?,`reminderEnabled` = ?,`reminderEmail` = ?,`note` = ?,`isRecurring` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getAmount());
        statement.bindLong(4, entity.getDueDay());
        statement.bindString(5, entity.getCategoryName());
        statement.bindString(6, entity.getCategoryIcon());
        statement.bindString(7, entity.getCategoryColor());
        final int _tmp = entity.isPaid() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getPaidDate() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getPaidDate());
        }
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindString(11, entity.getReminderEmail());
        statement.bindString(12, entity.getNote());
        final int _tmp_2 = entity.isRecurring() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        statement.bindLong(14, entity.getCreatedAt());
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfUpdatePaidStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE bills SET isPaid = ?, paidDate = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Bill bill, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBill.insertAndReturnId(bill);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Bill bill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBill.handle(bill);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Bill bill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBill.handle(bill);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePaidStatus(final long id, final boolean isPaid, final Long paidDate,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePaidStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isPaid ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        if (paidDate == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, paidDate);
        }
        _argIndex = 3;
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
          __preparedStmtOfUpdatePaidStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Bill>> getAllBills() {
    final String _sql = "SELECT * FROM bills ORDER BY dueDay ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryName");
          final int _cursorIndexOfCategoryIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryIcon");
          final int _cursorIndexOfCategoryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryColor");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidDate = CursorUtil.getColumnIndexOrThrow(_cursor, "paidDate");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEmail");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryIcon;
            _tmpCategoryIcon = _cursor.getString(_cursorIndexOfCategoryIcon);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidDate;
            if (_cursor.isNull(_cursorIndexOfPaidDate)) {
              _tmpPaidDate = null;
            } else {
              _tmpPaidDate = _cursor.getLong(_cursorIndexOfPaidDate);
            }
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final String _tmpReminderEmail;
            _tmpReminderEmail = _cursor.getString(_cursorIndexOfReminderEmail);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpIsRecurring;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpName,_tmpAmount,_tmpDueDay,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpIsPaid,_tmpPaidDate,_tmpReminderEnabled,_tmpReminderEmail,_tmpNote,_tmpIsRecurring,_tmpCreatedAt);
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
  public Flow<List<Bill>> getUnpaidBills() {
    final String _sql = "SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueDay ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryName");
          final int _cursorIndexOfCategoryIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryIcon");
          final int _cursorIndexOfCategoryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryColor");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidDate = CursorUtil.getColumnIndexOrThrow(_cursor, "paidDate");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEmail");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryIcon;
            _tmpCategoryIcon = _cursor.getString(_cursorIndexOfCategoryIcon);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidDate;
            if (_cursor.isNull(_cursorIndexOfPaidDate)) {
              _tmpPaidDate = null;
            } else {
              _tmpPaidDate = _cursor.getLong(_cursorIndexOfPaidDate);
            }
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final String _tmpReminderEmail;
            _tmpReminderEmail = _cursor.getString(_cursorIndexOfReminderEmail);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpIsRecurring;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpName,_tmpAmount,_tmpDueDay,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpIsPaid,_tmpPaidDate,_tmpReminderEnabled,_tmpReminderEmail,_tmpNote,_tmpIsRecurring,_tmpCreatedAt);
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
  public Flow<Double> getTotalBills() {
    final String _sql = "SELECT SUM(amount) FROM bills";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Flow<Double> getTotalUnpaidBills() {
    final String _sql = "SELECT SUM(amount) FROM bills WHERE isPaid = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Object getById(final long id, final Continuation<? super Bill> $completion) {
    final String _sql = "SELECT * FROM bills WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Bill>() {
      @Override
      @Nullable
      public Bill call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryName");
          final int _cursorIndexOfCategoryIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryIcon");
          final int _cursorIndexOfCategoryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryColor");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final int _cursorIndexOfPaidDate = CursorUtil.getColumnIndexOrThrow(_cursor, "paidDate");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfReminderEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEmail");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Bill _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryIcon;
            _tmpCategoryIcon = _cursor.getString(_cursorIndexOfCategoryIcon);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            final Long _tmpPaidDate;
            if (_cursor.isNull(_cursorIndexOfPaidDate)) {
              _tmpPaidDate = null;
            } else {
              _tmpPaidDate = _cursor.getLong(_cursorIndexOfPaidDate);
            }
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final String _tmpReminderEmail;
            _tmpReminderEmail = _cursor.getString(_cursorIndexOfReminderEmail);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final boolean _tmpIsRecurring;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Bill(_tmpId,_tmpName,_tmpAmount,_tmpDueDay,_tmpCategoryName,_tmpCategoryIcon,_tmpCategoryColor,_tmpIsPaid,_tmpPaidDate,_tmpReminderEnabled,_tmpReminderEmail,_tmpNote,_tmpIsRecurring,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
