package org.sagemath.droid.utils;

import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.models.database.Cell;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Haven
 */
public class ExampleCreator {

    public static List<Cell> getExamples(LinkedList<CellData> cellDatas) {

        List<Cell> examples = new ArrayList<Cell>();

        for (CellData cellData : cellDatas) {
            Cell cell = new Cell();
            cell.setUUID(cellData.getUUID());
            cell.setDescription(cellData.getDescription());
            cell.setFavorite(cellData.isFavorite());
            cell.setInput(cellData.getInput());
            cell.setRank(cellData.getRank());
            cell.setGroup(cellData.getGroup());
            cell.setTitle(cellData.getTitle());
            examples.add(cell);
        }

        return examples;

    }

}
