package com.arielnetworks.cubic.d3.sat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class Board implements Cloneable {
    public final int width, height, depth;
    private int board[];

    public Board(int W, int H, int D, int[] board) {
        assert W * H * D == board.length;
        this.width = W;
        this.height = H;
        this.depth = D;
        this.board = Objects.requireNonNull(board);
    }

    public static Board genSQ(int W, int H, int D) {
        return new Board(W, H, D, new int[W * H * D]);
    }

    public Board rotX() {
        Board ret = new Board(this.width, this.depth, this.height, new int[this.board.length]);

        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int from = this.pos(x, y, z);
                    ret.put(x, ret.height - z - 1, y, this.board[from]);
                }
            }
        }

        return ret;
    }

    public Board rotY() {
        Board ret = new Board(this.depth, this.height, this.width, new int[this.board.length]);

        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int from = this.pos(x, y, z);
                    ret.put(z, y, ret.depth - x - 1, this.board[from]);
                }
            }
        }

        return ret;
    }

    public Board rotZ() {
        Board ret = new Board(this.height, this.width, this.depth, new int[this.board.length]);

        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int from = this.pos(x, y, z);
                    ret.put(ret.width - y - 1, x, z, this.board[from]);
                }
            }
        }

        return ret;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.board.length + this.height);

        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int i = this.pos(x, y, z);
                    sb.append(this.board[i]);
                }
                sb.append('\n');
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
                depth == board1.depth &&
                Arrays.equals(board, board1.board);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(width, height, depth);
        result = 31 * result + Arrays.hashCode(board);
        return result;
    }

    public Board place(int x, int y, int z, Board p) {
        if (y < 0 || this.height < y + p.height ||
                x < 0 || this.width < x + p.width) {
            return null;
        }

        Board n = this.clone();
        for (int iz = 0; iz < p.depth; iz++) {
            for (int iy = 0; iy < p.height; iy++) {
                for (int ix = 0; ix < p.width; ix++) {
                    int pc = p.get(ix, iy, iz);
                    if (pc != 0 && !n.put(x + ix, y + iy, z + iz, pc)) {
                        return null;
                    }
                }
            }
        }

        return n;
    }

    private int pos(int x, int y, int z) {
        return (z * this.height + y) * this.width + x;
    }

    public boolean put(int x, int y, int z, int color) {
        int i = this.pos(x, y, z);
        if (0 <= i && i < this.board.length && this.board[i] == 0) {
            this.board[i] = color;
            return true;
        } else {
            return false;
        }
    }

    public int get(int x, int y, int z) {
        return this.board[this.pos(x, y, z)];
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

    public <T> T reduceRot(BiFunction<Board, T, T> f, T init) {
        Set<Board> bs = new HashSet<>(24);
        Board b = this;
        for (int i = 0; i < 4; i++, b = b.rotX()) {
            for (int j = 0; j < 4; j++, b = b.rotY()) {
                for (int k = 0; k < 4; k++, b = b.rotZ()) {
                    if (bs.add(b)) {
                        init = f.apply(b, init);
                    }
                }
            }
        }
        return init;
    }

    public boolean rotSame(Board target) {
        return this.reduceRot((b, s) ->
                        s ? s : b.equals(target)
                , false);
    }
}
