package com.arielnetworks.cubic.d2;

import java.util.Arrays;
import java.util.Objects;

public class Board implements Cloneable {
    public final int width, height;
    private int board[];

    public Board(int W, int H, int[] board) {
        assert W * H == board.length;
        this.width = W;
        this.height = H;
        this.board = Objects.requireNonNull(board);
    }

    public static Board genSQ(int W, int H) {
        return new Board(W, H, new int[W * H]);
    }

    public Board rot90() {
        int[] nb = new int[this.board.length];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                nb[x * this.height + (this.height - y - 1)] = this.board[y * this.width + x];
            }
        }

        return new Board(this.height, this.width, nb);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.board.length + this.height);

        for (int y = 0; y < this.height; y++) {
            for (int i = y * this.width; i < (y + 1) * this.width; i++) {
                sb.append(this.board[i]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board1 = (Board) o;
        return width == board1.width &&
                height == board1.height &&
                Arrays.equals(board, board1.board);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(board);
        return result;
    }

    public Board place(int x, int y, Board p) {
        if (y < 0 || this.height < y + p.height ||
                x < 0 || this.width < x + p.width) {
            return null;
        }

        Board n = this.clone();
        for (int iy = 0; iy < p.height; iy++) {
            for (int ix = 0; ix < p.width; ix++) {
                int pc = p.get(ix, iy);
                if (pc != 0 && !n.put(x + ix, y + iy, pc)) {
                    return null;
                }
            }
        }

        return n;
    }

    public boolean put(int x, int y, int color) {
        int i = y * this.width + x;
        if (0 <= i && i < this.board.length && this.board[i] == 0) {
            this.board[i] = color;
            return true;
        } else {
            return false;
        }
    }

    public int get(int x, int y) {
        return this.board[y * this.width + x];
    }

    @Override
    public Board clone() {
        try {
            Board ret = (Board) super.clone();
            ret.board = Arrays.copyOf(ret.board, ret.board.length);
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean rotSame(Board target) {
        if (this.width != target.width && this.height != target.height) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (this.equals(target)) {
                return true;
            }
            target = target.rot90();
        }
        return false;
    }
}
