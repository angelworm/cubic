package com.arielnetworks.cubic.d2;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    @Test
    void initialize() {
        List<Board> bs = Queen.initialize(Board.genSQ(4,4));

        assertEquals(bs.size(), 3);
        assertEquals(bs, Collections.emptyList());
    }
}