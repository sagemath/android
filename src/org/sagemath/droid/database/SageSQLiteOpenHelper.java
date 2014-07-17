package org.sagemath.droid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import nl.qbusict.cupboard.QueryResultIterable;
import org.sagemath.droid.R;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.utils.FileXMLParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * @author Haven
 */
public class SageSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "SageDroid:SageSQLiteOpenHelper";

    private static SageSQLiteOpenHelper instance = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sagedroid.db";

    private Context context;

    static {
        cupboard().register(Cell.class);
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
        //TODO replace this, only for testing
        InputStream inputStream = context.getResources().openRawResource(R.raw.cell_collection);
        FileXMLParser parser = new FileXMLParser();
        List<Cell> initialCells = parser.parse(inputStream);
        addInitialCells(initialCells, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Not sure if this is alright...
        cupboard().withDatabase(getWritableDatabase()).dropAllTables();
        onCreate(getWritableDatabase());
    }

    public Long addCell(Cell cell) {
        try {
            return cupboard().withDatabase(getWritableDatabase()).put(cell);
        } catch (Exception e) {
            Log.e(TAG, "Unable to add cell: " + e);
        }
        return null;
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

    public void addInitialCells(List<Cell> cells, SQLiteDatabase db) {
        try {
            db.beginTransaction();
            for (Cell cell : cells) {
                cupboard().withDatabase(db).put(cell);
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

    public List<Cell> getCells() {

        List<Cell> list = null;
        try {

            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .query()
                    .list();

        } catch (Exception e) {
            Log.e(TAG, e + "");
        }

        return list;
    }

    public List<Cell> getCellsWithGroup(String group) {

        List<Cell> list = null;
        try {
            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .withSelection("cellGroup = ?", group)
                    .orderBy("title asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        list = getSortedByFavs(list);
        return list;
    }

    public List<Cell> getQueryCells(String group, String titleQuery) {
        String queryFormat = "%" + titleQuery + "%";
        List<Cell> list = null;
        try {
            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .withSelection("cellGroup = ? AND title LIKE ?", group, queryFormat)
                    .orderBy("title asc")
                    .query()
                    .list();
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        list = getSortedByFavs(list);
        return list;
    }

    public List<String> getGroups() {

        List<String> list = null;
        QueryResultIterable<Cell> itr = null;

        try {
            itr = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .withProjection("cellGroup")
                    .orderBy("cellGroup asc")
                    .distinct()
                    .query();

            list = new ArrayList<String>();

            for (Cell cell : itr) {
                list.add(cell.getGroup());
            }

            Log.d(TAG, "Returning Groups" + list.toString());

        } finally {
            if (itr != null)
                itr.close();
        }
        return list;
    }

    private List<Cell> getSortedByFavs(List<Cell> cells) {
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

    public void saveEditedCell(Cell cell) {
        cupboard().withDatabase(getWritableDatabase()).put(cell);
    }

    public void saveEditedCells(List<Cell> cells) {
        for (Cell cell : cells) {
            cupboard().withDatabase(getWritableDatabase()).put(cell);
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
