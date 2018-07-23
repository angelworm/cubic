package com.arielnetworks.cubic.d3.sat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void rotX() {
        Board init = new Board(2, 1, 2, new int[]{
                1, 1,

                1, 0,
        }), b = init;


        b = b.rotX();
        assertEquals(b, new Board(2, 2, 1, new int[]{
                1, 0,
                1, 1
        }));

        b = b.rotX();
        assertEquals(b, new Board(2, 1, 2, new int[]{
                1, 0,

                1, 1
        }));

        b = b.rotX();
        assertEquals(b, new Board(2, 2, 1, new int[]{
                1, 1,
                1, 0
        }));

        b = b.rotX();
        assertEquals(b, init);
    }

    @Test
    void rotY() {
        Board init = new Board(2, 1, 2, new int[]{
                1, 1,

                1, 0,
        }), b = init;

        b = b.rotY();
        assertEquals(b, new Board(2, 1, 2, new int[]{
                1, 0,

                1, 1
        }));

        b = b.rotY();
        assertEquals(b, new Board(2, 1, 2, new int[]{
                0, 1,

                1, 1
        }));

        b = b.rotY();
        assertEquals(b, new Board(2, 1, 2, new int[]{
                1, 1,

                0, 1
        }));

        b = b.rotY();
        assertEquals(b, init);
    }

    @Test
    void rotZ() {
        Board init = new Board(2, 1, 2, new int[]{
                1, 1,

                1, 0,
        }), b = init;

        b = b.rotZ();
        assertEquals(b, new Board(1, 2, 2, new int[]{
                1,
                1,

                1,
                0
        }));

        b = b.rotZ();
        assertEquals(b, new Board(2, 1, 2, new int[]{
                1, 1,

                0, 1
        }));

        b = b.rotZ();
        assertEquals(b, new Board(1, 2, 2, new int[]{
                1,
                1,

                0,
                1
        }));

        b = b.rotZ();
        assertEquals(b, init);
    }

    @Test
    public void hoge() {
        assertTrue(new Board(3, 2, 1, new int[]{
                1, 1, 0,
                0, 1, 1
        }).rotSame(new Board(3, 2, 1, new int[]{
                0, 1, 1,
                1, 1, 0
        })));
    }
}