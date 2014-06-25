package org.sagemath.droid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import nl.qbusict.cupboard.QueryResultIterable;
import org.sagemath.droid.R;
import org.sagemath.droid.cells.CellCollectionXMLParser;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.utils.ExampleCreator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * @author Haven
 */
public class SageSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "SageDroid:SageSQLiteOpenHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sagedroid.db";

    private Context context;

    static {
        cupboard().register(Cell.class);
    }

    public SageSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating tables");
        cupboard().withDatabase(db).createTables();
        //TODO replace this, only for testing
        InputStream inputStream = context.getResources().openRawResource(R.raw.cell_collection);
        CellCollectionXMLParser parser = new CellCollectionXMLParser();
        List<Cell> initialCells = ExampleCreator.getExamples(parser.parse(inputStream));
        Log.i(TAG, "Initial Cells" + initialCells.toString());
        addInitialCells(initialCells, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO Figure out the code here.
    }

    public Long addCell(Cell cell) {
        Log.d(TAG, "Adding a new cell");
        try {
            return cupboard().withDatabase(getWritableDatabase()).put(cell);
        } catch (Exception e) {
            Log.e(TAG, "Unable to add cell");
        }
        return null;
    }

    public void addCells(List<Cell> cells) {
        Log.d(TAG, "Adding new cells: ");
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
        QueryResultIterable<Cell> itr = null;

        try {

            list = cupboard()
                    .withDatabase(getReadableDatabase())
                    .query(Cell.class)
                    .query()
                    .list();

        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            if (itr != null)
                itr.close();
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
                    .query()
                    .list();


        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
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

}
