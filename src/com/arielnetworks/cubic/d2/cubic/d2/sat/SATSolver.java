package com.arielnetworks.cubic.d2.cubic.d2.sat;

import com.arielnetworks.cubic.d2.Board;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

public class SATSolver {
    private static class Info {
        public final int x;
        public final int y;
        public final int bordIndex;
        public final int rot;

        public Info(int x, int y, int bordIndex, int rot) {
            this.x = x;
            this.y = y;
            this.bordIndex = bordIndex;
            this.rot = rot;
        }

        @Override
        public String toString() {
            return String.format("p%d%db%d%d", bordIndex, rot, x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Info info = (Info) o;
            return x == info.x &&
                    y == info.y &&
                    bordIndex == info.bordIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, bordIndex);
        }
    }

    private Board target;
    private VarFactory factory = new VarFactory();
    private List<List<Board>> bricks = Arrays.asList(
            Arrays.asList(
                    new Board(3, 1, new int[]{
                            1, 1, 1
                    }),
                    new Board(1, 3, new int[]{
                            1,
                            1,
                            1
                    })
            ),
            Arrays.asList(
                    new Board(2, 2, new int[]{
                            2, 2,
                            2, 0
                    }),
                    new Board(2, 2, new int[]{
                            2, 2,
                            0, 2
                    }),
                    new Board(2, 2, new int[]{
                            0, 2,
                            2, 2
                    }),
                    new Board(2, 2, new int[]{
                            2, 0,
                            2, 2
                    })));
    private Map<Var, Info> variables = new HashMap<>();

    private Map<Integer, Map<Integer, List<Var>>> indexToVar = new HashMap<>();
    private Map<Integer, List<Var>> bordToVar = new HashMap<>();

    private void putBrick(int x, int y, int brickIndex, int rot) {
        Board brick = this.bricks.get(brickIndex).get(rot);

        if (this.target.place(x, y, brick) == null) {
            return;
        }

        Var var = factory.getVar();
        Info info = new Info(x, y, brickIndex, rot);
        variables.put(var, info);

        for (int iy = 0; iy < brick.height; iy++) {
            for (int ix = 0; ix < brick.width; ix++) {
                if (brick.get(ix, iy) != 0) {
                    indexToVar.computeIfAbsent(x + ix, $ -> new HashMap<>())
                            .computeIfAbsent(y + iy, $ -> new ArrayList<>())
                            .add(var);
                }
            }
        }

        bordToVar.computeIfAbsent(brickIndex, $ -> new ArrayList<>()).add(var);
    }

    public SATSolver(Board target) {
        this.target = target;

        for (int i = 0; i < this.bricks.size(); i++) {
            for (int j = 0; j < this.bricks.get(i).size(); j++) {
                for (int y = 0; y < this.target.height; y++) {
                    for (int x = 0; x < this.target.width; x++) {
                        putBrick(x, y, i, j);
                    }
                }
            }
        }
    }

    private IVec<IVecInt> createBrickConstraints(List<Var> vars) {
        int[] ints = vars.stream().mapToInt(v -> -v.getIndex()).toArray();

        IVec<IVecInt> ret = new Vec<>(vars.size());
        for (int i = 0; i < ints.length; i++) {
            for (int j = i + 1; j < ints.length; j++) {
                ret.push(new VecInt(new int[]{ ints[i], ints[j] }));
                //System.out.println("brick constrains: " + Arrays.asList(ints[i], ints[j]));
            }
        }

        return ret;
    }

    private IVec<IVecInt> createIndexConstraints() {
        IVec<IVecInt> ret = new Vec<>();
        for (Map<Integer, List<Var>> xs : indexToVar.values()) {
            for (List<Var> vars : xs.values()) {
                ret.push(new VecInt(vars.stream().mapToInt(Var::getIndex).toArray()));
            }
        }

        return ret;
    }

    public void run() throws ContradictionException, TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(60);

        solver.newVar(1000);
        solver.setExpectedNumberOfClauses(1000);

        for (List<Var> vars : this.bordToVar.values()) {
            solver.addAllClauses(createBrickConstraints(vars));
        }
        solver.addAllClauses(createIndexConstraints());

        while (solver.isSatisfiable()) {

            int[] model = solver.model();
            System.out.println("SOLVED: " + Arrays.toString(model));

            Board result = this.target;
            for (int i : model) {
                if (i > 0) {
                    Info info = variables.get(new Var(i));
                    result = result.place(info.x, info.y, this.bricks.get(info.bordIndex).get(info.rot));
                }
            }
            System.out.println(result.toString());

            for (int i = 0; i < model.length; i++) {
                model[i] = -model[i];
            }
            solver.addClause(new VecInt(model));
        }
    }

    public static void main(String args[]) throws Exception {
        SATSolver solver = new SATSolver(new Board(3, 3, new int[]{
                0, 0, 0,
                0, 0, 9,
                0, 9, 9
        }));

        solver.run();

        System.out.println(solver.bordToVar);
        System.out.println(solver.indexToVar);
        System.out.println(solver.variables);

        System.exit(0);
    }

}
