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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private Paint paint, playerPaint;

    private Random random;

    private Cell player, exist;

    private Bitmap  exist_icon;

    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int RIGHT = 3;
    private static final int LEFT = 4;

    private SurfaceHolder surfaceHolder;



    public MazeView(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        playerPaint = new Paint();
        playerPaint.setColor(Color.GREEN);

        random = new Random();
        exist_icon = BitmapFactory.decodeResource(getResources(), R.drawable.flag_maze);


        createMaze();


    }


    private void createMaze(){
        cells = new Cell[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++)
                cells[row][column] = new Cell(row,column);

        player = cells[0][0];
        exist = cells[ROWS - 1][COLUMNS -1 ];



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

        if (width / height < COLUMNS / ROWS)
            cellSize = width / (COLUMNS + 1);
        else
            cellSize = height / (ROWS + 1);

        horizontalMargin = (width - COLUMNS * cellSize) / 2;
        verticalMargin = (height - ROWS * cellSize) / 2;

        canvas.translate(horizontalMargin, verticalMargin);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (cells[row][column].topWall)
                    canvas.drawLine(column * cellSize, row * cellSize, (column + 1) * cellSize, row * cellSize, paint);

                if (cells[row][column].leftWall)
                    canvas.drawLine(column * cellSize, row * cellSize, column * cellSize, (row + 1) * cellSize, paint);

                if (cells[row][column].bottomWall)
                    canvas.drawLine(column * cellSize, (row + 1) * cellSize, (column + 1) * cellSize, (row + 1) * cellSize, paint);

                if (cells[row][column].rightWall)
                    canvas.drawLine((column + 1) * cellSize, row * cellSize, (column + 1) * cellSize, (row + 1) * cellSize, paint);
            }
        }


         float playerMargin = cellSize / 10;

        //draw player
        RectF ovalPlayer = new RectF(
                player.column*cellSize + playerMargin,
                player.row * cellSize + playerMargin,
                (player.column+1)*cellSize - playerMargin,
                (player.row+1)*cellSize - playerMargin
        );
        canvas.drawOval(ovalPlayer, playerPaint);


        //draw exist
        Rect existLocationRect  = new Rect(
                (int)(exist.column*cellSize),
                (int)(exist.row * cellSize ),
                (int)((exist.column+1)*cellSize),
                (int)((exist.row+1)*cellSize ));
        Rect existSizeRect = new Rect(0,0,(int)cellSize,(int)cellSize);
        canvas.drawBitmap(exist_icon, existSizeRect, existLocationRect, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) return true;
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            float playerCenterX = (horizontalMargin + (player.column + 0.5f) * cellSize);
            float playerCenterY = (verticalMargin + (player.row) * cellSize + 0.5f);

            float differenceX = x - playerCenterX;
            float differenceY = y - playerCenterY;

            float absX = Math.abs(differenceX);
            float absY = Math.abs(differenceY);

            if (absX > cellSize   || absY > cellSize  ) {

                if (absX > absY) {
                    //move in x direction
                    if (differenceX > 0) {
                        //move to the right
                        movePlayer(RIGHT);
                    } else {
                        //move to the left
                        movePlayer(LEFT);
                    }
                } else {
                    //move int y direction
                    if (differenceY > 0) {
                        //move down
                        movePlayer(DOWN);
                    } else {
                        //move up
                        movePlayer(UP);
                    }

                }

            }
        }
        return true;
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


    private void movePlayer(int direction){
        switch(direction){
            case UP:
                if(player.topWall == false)
                player = cells[player.row - 1][player.column];
                break;

            case DOWN:
                if(player.bottomWall == false)
                    player = cells[player.row + 1][player.column];
                break;

            case LEFT:
                if(player.leftWall == false)
                    player = cells[player.row ][player.column - 1];
                break;

            case RIGHT:
                if(player.rightWall == false)
                    player = cells[player.row ][player.column + 1];
                break;
        }




        if(hasReachExist()) {
            Toast.makeText(getContext(), "You won", Toast.LENGTH_LONG).show();

        }

    }

    private boolean hasReachExist(){
            return (player.row == exist.row && player.column == exist.column);
    }

}
