package com.arielnetworks.cubic.d2;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test
    public void test_toString_Square() {
        Board b = new Board(2, 2, new int[4]);

        assertEquals(b.toString().trim(), Stream.of(
                "00",
                "00").collect(Collectors.joining("\n")));
    }
    @Test
    public void test_toString_tetoraI() {
        Board b = new Board(1, 4, new int[] {
                1,
                1,
                1,
                1
        });

        assertEquals(b.toString().trim(), Stream.of(
                "1",
                "1",
                "1",
                "1").collect(Collectors.joining("\n")));
    }

    @Test
    public void test_rot90() {
        Board b = new Board(3, 2, new int[]{
                1, 1, 0,
                0, 1, 1,
        });

        assertEquals(b.rot90(), new Board(2, 3, new int[] {
                0, 1,
                1, 1,
                1, 0
        }));
    }

    @Test
    public void test_place() {
        Board b = Board.genSQ(4, 4);
        Board p = new Board(3, 2, new int[]{
                1, 1, 0,
                0, 1, 1,
        });

        Board ret = b.place(1, 0, p);
        assertEquals(ret, new Board(4, 4, new int[] {
                0, 1, 1, 0,
                0, 0, 1, 1,
                0, 0, 0, 0,
                0, 0, 0, 0
        }));
    }

    @Test
    public void test_place_tooSmall() {
        Board b = Board.genSQ(2, 2);
        Board p = new Board(3, 2, new int[]{
                1, 1, 0,
                0, 1, 1,
        });

        Board ret = b.place(1, 0, p);
        assertEquals(ret, null);
    }
}