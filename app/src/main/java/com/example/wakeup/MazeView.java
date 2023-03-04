package com.example.wakeup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


public class MazeView extends View {

    private static final int ROWS = 14;
    private static final int COLUMNS = 10;

    private static final int WALL_THICKNESS = 3;

    private float cellSize, horizontalMargin, verticalMargin;
    private Cell[][] cells;
    private Paint paint;

    private Random random;


    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(WALL_THICKNESS);
        random = new Random();
        createMaze();
    }


    private void createMaze(){
        cells = new Cell[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++)
                cells[row][column] = new Cell(row,column);


        Cell current, next;
        Stack<Cell> stack = new Stack<>();
        current = cells[0][0];
        current.visited = true;

        do{
            next = getNeighbour(current);
            if(next != null){
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else
                current = stack.pop();

        }while(!stack.isEmpty());

    }

    private void removeWall(Cell current, Cell next) {
        if(current.column == next.column && current.row == next.row + 1){
            current.topWall = false;
            next.bottomWall = false;
        }
        if(current.column == next.column && current.row == next.row - 1){
            current.bottomWall = false;
            next.topWall = false;
        }
        if(current.column == next.column + 1 && current.row == next.row){
            current.leftWall = false;
            next.rightWall = false;
        }
        if(current.column == next.column - 1 && current.row == next.row ){
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left neighbor
        if(cell.column > 0)
        if(!cells[cell.row][cell.column - 1].visited)
            neighbours.add(cells[cell.row][cell.column - 1]);

        //right neighbor
        if(cell.column < COLUMNS - 1)
        if(!cells[cell.row][cell.column + 1].visited)
            neighbours.add(cells[cell.row][cell.column + 1]);

        //top neighbor
        if(cell.row > 0)
        if(!cells[cell.row - 1][cell.column].visited)
            neighbours.add(cells[cell.row - 1][cell.column]);

        //bottom neighbor
        if(cell.row < ROWS - 1)
        if(!cells[cell.row + 1][cell.column].visited)
            neighbours.add(cells[cell.row + 1][cell.column]);


        if(!neighbours.isEmpty()) {
            int index = random.nextInt(neighbours.size());
           return  neighbours.get(index);
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if(width / height < COLUMNS / ROWS)
            cellSize = width/(COLUMNS+1);
        else
            cellSize = height/(ROWS+1);

        horizontalMargin = (width - COLUMNS*cellSize) / 2;
        verticalMargin = (height - ROWS*cellSize) / 2;

        canvas.translate(horizontalMargin, verticalMargin);

        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++){
                if(cells[row][column].topWall)
                    canvas.drawLine(column*cellSize, row*cellSize, (column+1)*cellSize, row*cellSize, paint);

                if(cells[row][column].leftWall)
                    canvas.drawLine(column*cellSize, row*cellSize, column*cellSize, (row+1)*cellSize, paint);

                if(cells[row][column].bottomWall)
                    canvas.drawLine(column*cellSize, (row+1)*cellSize, (column+1)*cellSize, (row+1)*cellSize, paint);

                if(cells[row][column].rightWall)
                    canvas.drawLine((column+1)*cellSize, row*cellSize, (column+1)*cellSize, (row+1)*cellSize, paint);
            }
        }



    private class Cell{
        boolean leftWall = true;
        boolean rightWall = true;
        boolean bottomWall = true;
        boolean topWall = true;

        boolean visited;

        int row, column;
        public Cell(int row, int column){
            this.row = row;
            this.column = column;
        }

    }

}
