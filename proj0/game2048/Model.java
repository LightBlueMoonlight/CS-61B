package game2048;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Observable;


/**
 * The state of a game of 2048.
 *
 * @author kaixiang Wang
 */
public class Model extends Observable {
    /**
     * Current contents of the board.  面板类
     */
    private Board board;
    /**
     * Current score. 得分
     */
    private int score;
    /**
     * Maximum score so far.  Updated when game ends. 到目前为止，最高分。 游戏结束时更新
     */
    private int maxScore;
    /**
     * True iff game is ended. 游戏结束标识符
     */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (wheaddTilere row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /**
     * Largest piece value.
     */
    public static final int MAX_PIECE = 2048;

    /**
     * A new 2048 game on a board of size SIZE with no pieces
     * and score 0.
     */
    //一个新的2048游戏，在尺寸大小的棋盘上，没有棋子 得分 0
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /**
     * A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes.
     */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /**
     * Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     * 0 <= COL < size(). Returns null if there is no tile there.
     * Used for testing. Should be deprecated and removed.
     */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /**
     * Return the number of squares on one side of the board.
     * Used for testing. Should be deprecated and removed.
     */
    public int size() {
        return board.size();
    }

    /**
     * Return true iff the game is over (there are no moves, or
     * there is a tile with value 2048 on the board).
     */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /**
     * Return the current score.
     */
    public int score() {
        return score;
    }

    /**
     * Return the current maximum game score (updated at end of game).
     */
    public int maxScore() {
        return maxScore;
    }

    /**
     * Clear the board to empty and reset the score.
     */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /**
     * Add TILE to the board. There must be no Tile currently at the
     * same position.
     */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    //将板向一侧倾斜。如果此更改板，则返回true。
    //1. 如果两个平铺对象在运动方向上相邻且具有相同的值，它们合并为一个两倍于原始值的平铺值，并将该新值添加到score实例变量中
    //2. 作为合并结果的磁贴将不会在该磁贴上再次合并倾斜。因此，每一个移动，每一个瓷砖将永远只是其中的一部分，最多一个合并（可能为零）。
    //3. 当运动方向上的三个相邻瓷砖具有相同值，然后沿运动方向的前两个平铺合并，而后面的磁贴则不会。
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        //将此.board（可能还有此.score）修改为account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        ////用于向一侧倾斜。如果电路板发生变化，请设置将局部变量更改为true。
        if (atLeastOneMoveExists(board)) {
            String before = board.toString();//变化之前
            board.setViewingPerspective(side);
            for (int i = 0; i < board.size(); i++) {
                moveCol(i);
            }
            board.setViewingPerspective(Side.NORTH);
            String after = board.toString();//变化之后
            if (!before.equals(after)) {
                changed = true;
            }
        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    private void moveCol(int i) {
        int top = board.size()-1;
        Tile topTile = board.tile(i,top);
        int j = top -1;
        while(j>=0){
            Tile tile1 = board.tile(i,j);
            if(tile1 == null){
                j--;
                continue;
            }else{
                if (topTile==null){
                    board.move(i,top,tile1);
                    topTile = tile1;
                }else if(topTile.value()==tile1.value()){
                    board.move(i,top,tile1);
                    this.score += tile1.value()*2;
                    top--;
                    topTile = board.tile(i,top);
                }else{
                    top--;
                    board.move(i,top,tile1);
                    topTile = board.tile(i,top);
                }
                j--;
            }
        }
    }

    /**
     * Checks if the game is over and sets the gameOver variable
     * appropriately.
     */
    //检查游戏是否结束并设置gameOver变量适当地。
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /**
     * Determine whether game is over.
     */
    //决定比赛是否结束
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /**
     * Returns true if at least one space on the Board is empty.
     * Empty spaces are stored as null.
     */
    public static boolean emptySpaceExists(Board b) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (b.tile(i, j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (b.tile(i, j) == null) {
                    continue;
                }

                if (b.tile(i, j).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    //如果板上有任何有效移动，则返回true。有效移动有两种方式：1. 板上至少有一个空白。2. 有两个相邻的瓷砖具有相同的值。
    public static boolean atLeastOneMoveExists(Board b) {
        boolean flag = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {

                if (b.tile(i, j) == null) {
                    return flag = true;
                }

                if (i != 3) {
                    if (b.tile(i + 1, j) != null && b.tile(i + 1, j).value() == (b.tile(i, j).value())) {
                        return flag = true;
                    }
                }
                if (j != 3) {
                    if (b.tile(i, j + 1) != null && b.tile(i, j + 1).value() == (b.tile(i, j).value())) {
                        return flag = true;
                    }
                }
            }
        }
        return flag;
    }


    @Override
    /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    //返回两个模型是否相等
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    //返回模型字符串的哈希代码。
    public int hashCode() {
        return toString().hashCode();
    }
}
