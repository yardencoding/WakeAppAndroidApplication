package com.example.wakeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


public class MazeView extends View {

    private static final int ROWS = 8;
    private static final int COLUMNS = 8;

    private static final int WALL_THICKNESS = 3;

    private float horizontalMargin, verticalMargin;

    private final int cellSize = 100;
    private Cell[][] cells;
    private Paint paint, playerPaint;

    private Random random;

    private Cell player, exist;
    private Bitmap exist_box_maze;

    private RectF oval;

    private Rect existLocationRect, existSizeRect, playerRect_forInvalidate;

    private float playerMargin;


    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.GREEN);

        random = new Random();
        exist_box_maze = BitmapFactory.decodeResource(getResources(), R.drawable.exist_box_maze);

        oval = new RectF();
        existLocationRect = new Rect();
        existSizeRect = new Rect();
        playerRect_forInvalidate = new Rect();
        createMaze();
    }




    private void createMaze() {

        //Initialize the cells array.
        cells = new Cell[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++)
                cells[row][column] = new Cell(row, column);

        //player and exist cells.
        player = cells[0][0];
        exist = cells[ROWS - 1][COLUMNS - 1];

        /*
        The code below is used to create a path between the maze.
        It works like this:

        We start at the first cell[0][0] and make a connection to one of his neighbours,
        by connection I mean removing the walls(leftWall, rightWall, bottomWall, topWall), setting them to false.
        after that we add the cell to a Stack.

        we keep doing this to every cell (make a connection to one of his neighbours)
        if we have a reached a cell with no valid neighbours we backtrack to the previous cell
        using the stack and choosing a different neighbour.

        When the stack gets empty it means that we have reached every cell and created a connection between all of them.
        */
        Cell current, next;
        Stack<Cell> stack = new Stack<>();
        current = cells[0][0];
        current.setVisited(true);

        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.setVisited(true);
            } else
                current = stack.pop();

        } while (!stack.isEmpty());

    }

    private void removeWall(Cell current, Cell next) {

        //remove current cell top wall
        if (current.getColumn() == next.getColumn() && current.getRow() == next.getRow() + 1) {
            current.setTopWall(false);
            next.setBottomWall(false);
        }
        //remove current cell bottom wall
        if (current.getColumn() == next.getColumn() && current.getRow() == next.getRow() - 1) {
            current.setBottomWall(false);
            next.setTopWall(false);
        }
        //remove current cell left wall
        if (current.getColumn() == next.getColumn() + 1 && current.getRow() == next.getRow()) {
            current.setLeftWall(false);
            next.setRightWall(false);
        }

        //remove current cell right wall
        if (current.getColumn() == next.getColumn() - 1 && current.getRow() == next.getRow()) {
            current.setRightWall(false);
            next.setLeftWall(false);
        }
    }

    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left neighbor
        if (cell.getColumn() > 0)
            if (!cells[cell.getRow()][cell.getColumn() - 1].isVisited())
                neighbours.add(cells[cell.getRow()][(cell.getColumn() - 1)]);

        //right neighbor
        if (cell.getColumn() < COLUMNS - 1)
            if (!cells[cell.getRow()][cell.getColumn() + 1].isVisited())
                neighbours.add(cells[cell.getRow()][(cell.getColumn() + 1)]);

        //top neighbor
        if (cell.getRow() > 0)
            if (!cells[cell.getRow() - 1][cell.getColumn()].isVisited())
                neighbours.add(cells[cell.getRow() - 1][cell.getColumn()]);

        //bottom neighbor
        if (cell.getRow() < ROWS - 1)
            if (!cells[(cell.getRow() + 1)][cell.getColumn()].isVisited())
                neighbours.add(cells[cell.getRow() + 1][cell.getColumn()]);


        //to get a random neighbour
        if (!neighbours.isEmpty()) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //calculate view horizontal margin
        horizontalMargin = (float)(getWidth() - COLUMNS * cellSize) / 2;
        //calculate view vertical margin
        verticalMargin = (float)(getHeight() - ROWS * cellSize) / 2;

        //So that we do not have to add the horizontalMargin and verticalMargin every time.
        // It will instead adjust the canvas location based on them.
        canvas.translate(horizontalMargin, verticalMargin);

        //draw lines, after we have called createMaze() and created a path.
        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++) {
                if (cells[row][column].isTopWall())
                    canvas.drawLine(column * cellSize, row * cellSize, (column + 1) * cellSize, row * cellSize, paint);

                if (cells[row][column].isLeftWall())
                    canvas.drawLine(column * cellSize, row * cellSize, column * cellSize, (row + 1) * cellSize, paint);

                if (cells[row][column].isBottomWall())
                    canvas.drawLine(column * cellSize, (row + 1) * cellSize, (column + 1) * cellSize, (row + 1) * cellSize, paint);

                if (cells[row][column].isRightWall())
                    canvas.drawLine((column + 1) * cellSize, row * cellSize, (column + 1) * cellSize, (row + 1) * cellSize, paint);
            }

        //draw player
        playerMargin = cellSize / 10;
        oval.set(player.getColumn() * cellSize + playerMargin,
                player.getRow() * cellSize + playerMargin,
                (player.getColumn() + 1) * cellSize - playerMargin,
                (player.getRow() + 1) * cellSize - playerMargin);
        canvas.drawOval(oval, playerPaint);

        //draw exist image.
        existLocationRect.set(
                exist.getColumn() * cellSize,
                exist.getRow() * cellSize,
                (exist.getColumn() + 1) * cellSize,
                (exist.getRow() + 1) * cellSize
        );
        existSizeRect.set(0, 0, (int) cellSize, (int) cellSize);
        canvas.drawBitmap(exist_box_maze, existSizeRect, existLocationRect, null);
    }

        public void moveRight() {
        if (!player.isRightWall()) {
            player = cells[player.getRow()][player.getColumn() + 1];
            updatePlayerPosition();
        }
    }

    public void moveLeft() {
        if (!player.isLeftWall()) {
            player = cells[player.getRow()][player.getColumn() - 1];
            updatePlayerPosition();
        }
    }

    public void moveUp() {
        if (!player.isTopWall()) {
            player = cells[player.getRow() - 1][player.getColumn()];
            updatePlayerPosition();
        }
    }

    public void moveDown() {
        if (!player.isBottomWall()) {
            player = cells[player.getRow() + 1][player.getColumn()];
            updatePlayerPosition();
        }
    }

    public boolean hasReachedExist() {
        return (player.getColumn() == exist.getColumn() && player.getRow() == exist.getRow());
    }

    private void updatePlayerPosition() {
        //set that we only update the player position, and do not have to reDraw the entire view
        playerRect_forInvalidate.set(
                player.getColumn() * cellSize,
                player.getRow() * cellSize,
                (player.getColumn() + 1) * cellSize,
                (player.getRow() + 1) * cellSize
        );
        invalidate(playerRect_forInvalidate);
    }
}
