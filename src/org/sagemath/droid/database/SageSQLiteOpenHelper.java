package org.sagemath.droid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.sagemath.droid.R;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.database.Group;
import org.sagemath.droid.models.database.Inserts;
import org.sagemath.droid.utils.FileXMLParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * @author Nikhil Peter Raj
 */
public class SageSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "SageDroid:SageSQLiteOpenHelper";

    private static SageSQLiteOpenHelper instance = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sagedroid.db";

    private Context context;

    private List<Group> currentGroups;

    static {
        cupboard().register(Cell.class);
        cupboard().register(Group.class);
        cupboard().register(Inserts.class);
    }

    public static SageSQLiteOpenHelper getInstance(Context context) {

        if (instance == null) {
            instance = new SageSQLiteOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SageSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating tables");
        cupboard().withDatabase(db).createTables();
        InputStream inputStream = context.getResources().openRawResource(R.raw.cell_collection);
        FileXMLParser parser = new FileXMLParser();
        parser.parse(inputStream);
        List<Cell> initialCells = parser.getIntitalCells();
        List<Group> initialGroups = parser.getInitialGroups();
        addInitialGroups(initialGroups, db);
        addInitialCells(initialCells, db);
        addInitialInserts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Not sure if this is alright...
        //cupboard().withDatabase(getWritableDatabase()).dropAllTables();
        Log.i(TAG, "In onUpgrade");
        onCreate(getWritableDatabase());
    }

    public Long addCell(Cell cell) {
        Log.i(TAG, "Adding cell: " + cell);
        try {
            currentGroups = getGroups();
            if (currentGroups.contains(cell.getGroup())) {
                cell.setGroup(currentGroups.get(currentGroups.indexOf(cell.getGroup())));
            }
            return cupboard().withDatabase(getWritableDatabase()).put(cell);
        } catch (Exception e) {
            Log.e(TAG, "Unable to add cell: " + e);
        }
        return null;
    }

    private void reRegisterEntity(Class clazz) {
        cupboard().register(clazz);
    }

    public void addCells(List<Cell> cells) {
        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            db.beginTransaction();
            for (Cell cell : cells) {
                cupboard().withDatabase(db).put(cell);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }

    }

    private void addInitialInserts(SQLiteDatabase db) {

        Inserts insert1 = new Inserts();
        insert1.setInsertDescription("List Comprehension");
        insert1.setInsertText("[ i for i in range(0,10) ]");
        insert1.setFavorite(false);

        Inserts insert2 = new Inserts();
        insert2.setInsertDescription("For Loop");
        insert2.setInsertText("for i in range(0,10):");
        insert2.setFavorite(false);
        cupboard().withDatabase(db).put(insert1, insert2);
    }

    public void addInitialGroups(List<Group> groups, SQLiteDatabase db) {
        Log.i(TAG, "Adding Initial Groups" + groups.toString());
        try {
            //Add playground
            Group playgroundGroup = new Group(context.getString(R.string.group_playground));
            cupboard().withDatabase(db).put(playgroundGroup);

            db.beginTransaction();
            for (Group group : groups) {
                cupboard().withDatabase(db).put(group);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public void addInitialCells(List<Cell> cells, SQLiteDatabase db) {
        try {
            currentGroups = getGroups(db);
            db.beginTransaction();
            for (Cell cell : cells) {
                if (!currentGroups.contains(cell.getGroup())) {
                    cupboard().withDatabase(db).put(cell);
                } else {
                    Group group = currentGroups.get(currentGroups.indexOf(cell.getGroup()));
                    cell.setGroup(group);
                    cupboard().withDatabase(db).put(cell);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public List<Cell> getCellsWithGroup(Group group) {

        List<Cell> list = null;
        try {
            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .withSelection("cellGroup = ?", String.valueOf(group.getId()))
                    .orderBy("title asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        list = sortCellsByFavorite(list);
        return list;
    }

    public List<Cell> getQueryCells(Group group, String titleQuery) {
        String queryFormat = "%" + titleQuery + "%";
        List<Cell> list = null;
        try {
            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .withSelection("cellGroup = ? AND title LIKE ?", String.valueOf(group.getId()), queryFormat)
                    .orderBy("title asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        list = sortCellsByFavorite(list);
        return list;
    }

    public List<Group> getGroups(SQLiteDatabase db) {
        List<Group> list = null;
        try {
            list = cupboard()
                    .withDatabase(db)
                    .query(Group.class)
                    .orderBy("cellGroup asc")
                    .distinct()
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return list;
    }

    public List<Group> getGroups() {

        List<Group> list = null;
        try {
            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Group.class)
                    .orderBy("cellGroup asc")
                    .distinct()
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return list;
    }

    public void addGroup(Group group) {
        try {
            //If the group is duplicate, discard
            List<Group> currentGroups = getGroups();
            if (!currentGroups.contains(group))
                cupboard().withDatabase(getWritableDatabase()).put(group);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    public void deleteGroup(Group group) {
        try {
            //Delete all cells with this group

            List<Cell> cells = getCellsWithGroup(group);
            Log.i(TAG, "Deleting " + cells.size() + "cells and group: " + group.getCellGroup());
            deleteCells(cells);

            //Delete the group
            cupboard().withDatabase(getWritableDatabase()).delete(group);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    public void saveGroup(Group group) {
        try {
            cupboard().withDatabase(getWritableDatabase()).put(group);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    public List<Inserts> getQueryInserts(String query) {
        String queryInsert = "%" + query + "%";
        List<Inserts> inserts = null;

        try {
            inserts = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Inserts.class)
                    .withSelection("insertDescription LIKE ?", queryInsert)
                    .orderBy("insertDescription asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return inserts;
    }

    public List<Inserts> getInserts() {

        List<Inserts> inserts = null;

        try {
            inserts = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Inserts.class)
                    .orderBy("insertDescription asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }

        if (inserts != null) {
            inserts = sortInsertsByFavorite(inserts);
        }

        return inserts;
    }

    public void addInsert(Inserts insert) {
        try {
            cupboard().withDatabase(getWritableDatabase()).put(insert);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    public boolean addInsert(List<Inserts> inserts) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (Inserts insert : inserts) {
                cupboard().withDatabase(db).put(inserts);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e + "");
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    public void deleteInsert(Inserts insert) {
        try {
            cupboard().withDatabase(getWritableDatabase()).delete(insert);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    public boolean deleteInsert(List<Inserts> inserts) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            for (Inserts insert : inserts) {
                Log.i(TAG, "Deleting:" + insert);
                cupboard().withDatabase(db).delete(insert);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e + "");
            return false;
        } finally {
            db.endTransaction();
        }

        return true;
    }

    private List<Cell> sortCellsByFavorite(List<Cell> cells) {
        ArrayList<Cell> favs = new ArrayList<>();
        ArrayList<Cell> others = new ArrayList<>();

        for (Cell cell : cells) {
            if (cell.isFavorite())
                favs.add(cell);
            else
                others.add(cell);
        }

        favs.addAll(others);
        return favs;
    }

    private List<Inserts> sortInsertsByFavorite(List<Inserts> inserts) {
        ArrayList<Inserts> favs = new ArrayList<>();
        ArrayList<Inserts> others = new ArrayList<>();

        for (Inserts insert : inserts) {
            if (insert.isFavorite()) {
                favs.add(insert);
            } else {
                others.add(insert);
            }
        }

        favs.addAll(others);
        return favs;
    }

    public void saveEditedCell(Cell cell) {

        currentGroups = getGroups();
        if (currentGroups.contains(cell.getGroup())) {
            cell.setGroup(currentGroups.get(currentGroups.indexOf(cell.getGroup())));
        }
        cupboard().withDatabase(getWritableDatabase()).put(cell);

    }

    public void saveEditedCells(List<Cell> cells) {
        for (Cell cell : cells) {
            saveEditedCell(cell);
        }
    }

    public Cell getCellbyID(Long id) {
        return cupboard().withDatabase(getReadableDatabase()).get(Cell.class, id);
    }

    public void deleteCell(Cell cell) {
        cupboard().withDatabase(getWritableDatabase()).delete(cell);
    }

    public boolean deleteCells(List<Cell> cells) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            for (Cell cell : cells) {
                cupboard().withDatabase(getWritableDatabase()).delete(cell);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e + "");
            return false;
        } finally {
            db.endTransaction();
        }

        return true;
    }

}
