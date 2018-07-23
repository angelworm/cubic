package com.arielnetworks.cubic.d2;

import java.util.*;

public class Queen {
    private static Board[] BRICKS = new Board[]{
            new Board(2, 2, new int[]{
                    1, 1,
                    1, 1
            }),
            new Board(3, 2, new int[]{
                    0, 2, 2,
                    2, 2, 0
            }),
            new Board(3, 2, new int[]{
                    3, 3, 0,
                    0, 3, 3
            }),
            new Board(3, 2, new int[]{
                    4, 4, 4,
                    0, 4, 0
            }),
            new Board(3, 2, new int[]{
                    5, 5, 5,
                    5, 0, 0
            }),
            new Board(3, 2, new int[]{
                    6, 6, 6,
                    0, 0, 6
            }),
            new Board(4, 1, new int[]{
                    7, 7, 7, 7
            }),
    };

    private static List<List<Board>> ROTS = new ArrayList<>(BRICKS.length);
    static  {
        for (Board brick : BRICKS) {
            ArrayList<Board> rs = new ArrayList<>(4);
            while (!rs.contains(brick)) {
                rs.add(brick);
                brick.rot90();
            }
            ROTS.add(rs);
        }
    }

    public static List<Board> initialize(Board target) {
        ArrayList<Board> ret = new ArrayList<>();
        Board p = BRICKS[0];

        for (int x = 0; x < Math.ceil(target.width / 2.0); x++) {
            for (int y = 0; y < Math.ceil(target.height / 2.0); y++) {
                Board place = target.place(x, y, p);
                if (place != null && ret.stream().noneMatch(nb -> nb.rotSame(place))) {
                    ret.add(place);
                }
            }
        }

        return ret;
    }

    public static void runInternal(List<Board> result, Board target, int x, int y) {

    }

    public static List<Board> run(Board target) {
        Queue<Board> Q = null;
        return null;
    }
}
