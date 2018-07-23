package com.arielnetworks.cubic.d3.sat;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SATSolver {
    private static VarFactory factory = new VarFactory();
    private static List<Board> brickBase = Arrays.asList(
            new Board(2, 2, 1, new int[]{
                    1, 1,
                    1, 0
            }),
            new Board(3, 2, 1, new int[]{
                    2, 2, 0,
                    0, 2, 2
            }),
            new Board(3, 2, 1, new int[]{
                    3, 3, 3,
                    3, 0, 0
            }),
            new Board(3, 2, 1, new int[]{
                    4, 4, 4,
                    0, 4, 0
            }),
            new Board(2, 2, 2, new int[]{
                    5, 5,
                    0, 0,

                    5, 0,
                    5, 0
            }),
            new Board(2, 2, 2, new int[]{
                    6, 6,
                    6, 0,

                    6, 0,
                    0, 0
            }),
            new Board(2, 2, 2, new int[]{
                    7, 7,
                    0, 0,

                    0, 7,
                    0, 7
            }));
    private static List<List<Board>> bricks;

    static {
        bricks = brickBase.stream()
                .map(board -> board.reduceRot((b, s) -> {
                    s.add(b);
                    return s;
                }, new ArrayList<Board>(24)))
                .collect(Collectors.toList());
    }

    private Board target;
    private Map<Var, Info> variables = new HashMap<>();
    private Map<Index, List<Var>> indexToVar = new HashMap<>();
    private Map<Integer, List<Var>> bordToVar = new HashMap<>();

    public SATSolver(Board target) {
        this.target = target;

        for (int i = 0; i < this.bricks.size(); i++) {
            for (int j = 0; j < this.bricks.get(i).size(); j++) {
                for (int z = 0; z < this.target.depth; z++) {
                    for (int y = 0; y < this.target.height; y++) {
                        for (int x = 0; x < this.target.width; x++) {
                            putBrick(x, y, z, i, j);
                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        Board Q = Board.genSQ(3, 3, 3);
        SATSolver solver = new SATSolver(Q);

        solver.run();

        System.out.println(solver.bordToVar);
        System.out.println(solver.indexToVar);
        System.out.println(solver.variables);

        System.exit(0);
    }

    private void putBrick(int x, int y, int z, int brickIndex, int rot) {
        Board brick = this.bricks.get(brickIndex).get(rot);

        if (this.target.place(x, y, z, brick) == null) {
            return;
        }

        Var var = factory.getVar();
        Info info = new Info(x, y, z, brickIndex, rot);
        variables.put(var, info);

        for (int iz = 0; iz < brick.depth; iz++) {
            for (int iy = 0; iy < brick.height; iy++) {
                for (int ix = 0; ix < brick.width; ix++) {
                    if (brick.get(ix, iy, iz) != 0) {
                        Index key = new Index(x + ix, y + iy, z + iz);
                        indexToVar.computeIfAbsent(key, $ -> new ArrayList<>())
                                .add(var);
                    }
                }
            }
        }

        bordToVar.computeIfAbsent(brickIndex, $ -> new ArrayList<>()).add(var);
    }

    private IVec<IVecInt> createBrickConstraints(List<Var> vars) {
        int[] ints = vars.stream().mapToInt(v -> -v.getIndex()).toArray();

        IVec<IVecInt> ret = new Vec<>(vars.size());
        for (int i = 0; i < ints.length; i++) {
            for (int j = i + 1; j < ints.length; j++) {
                ret.push(new VecInt(new int[]{ints[i], ints[j]}));
                //System.out.println("brick constrains: " + Arrays.asList(ints[i], ints[j]));
            }
        }

        return ret;
    }

    private IVec<IVecInt> createIndexConstraints() {
        IVec<IVecInt> ret = new Vec<>();
        for (List<Var> vars : indexToVar.values()) {
            int[] ints = vars.stream().mapToInt(Var::getIndex).toArray();
            ret.push(new VecInt(ints));

            for (int i = 0; i < ints.length; i++) {
                for (int j = i + 1; j < ints.length; j++) {
                    ret.push(new VecInt(new int[]{-ints[i], -ints[j]}));
                }
            }
        }

        return ret;
    }

    public void run() throws ContradictionException, TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(6000);

        solver.newVar(1000);
        solver.setExpectedNumberOfClauses(1000);

        for (List<Var> vars : this.bordToVar.values()) {
            solver.addAllClauses(createBrickConstraints(vars));
        }
        solver.addAllClauses(createIndexConstraints());

        int solved = 0;
        while (solver.isSatisfiable()) {

            int[] model = solver.model();
            System.out.println(String.format("SOLVED(%d): %s", ++solved, Arrays.toString(model)));

            Board result = this.target;
//            for (int i : model) {
//                if (i > 0) {
//                    Info info = variables.get(new Var(i));
//                    result = result.place(info.x, info.y, info.z, this.bricks.get(info.bordIndex).get(info.rot));
//                }
//            }
//            System.out.println(result.toString());

//            for (int i = 0; i < model.length; i++) {
//                model[i] = -model[i];
//            }
//            solver.addClause(new VecInt(model));
            solver.addAllClauses(createPrivAnserConstraints(model));
        }
    }

    private Info rotX(Board newTarget, Info oldInfo) {
        Board b = bricks.get(oldInfo.bordIndex).get(oldInfo.rot);
        Board nb = b.rotX();
        int nbi = bricks.get(oldInfo.bordIndex).indexOf(nb);

        return new Info(oldInfo.x, newTarget.height - oldInfo.z - nb.height, oldInfo.y, oldInfo.bordIndex, nbi);
    }

    private Info rotY(Board newTarget, Info oldInfo) {
        Board b = bricks.get(oldInfo.bordIndex).get(oldInfo.rot);
        Board nb = b.rotY();
        int nbi = bricks.get(oldInfo.bordIndex).indexOf(nb);

        return new Info(oldInfo.z, oldInfo.y, newTarget.depth - oldInfo.x - nb.depth, oldInfo.bordIndex, nbi);
    }

    private Info rotZ(Board newTarget, Info oldInfo) {
        Board b = bricks.get(oldInfo.bordIndex).get(oldInfo.rot);
        Board nb = b.rotZ();
        int nbi = bricks.get(oldInfo.bordIndex).indexOf(nb);

        return new Info(newTarget.width - oldInfo.y - nb.width, oldInfo.x, oldInfo.z, oldInfo.bordIndex, nbi);
    }

    private Var infoToVar(Info info) {
        return this.variables.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(info))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    private IVec<IVecInt> createPrivAnserConstraints(int[] model) {
        List<Info> infos = IntStream.of(model)
                .filter(i -> i > 0)
                .mapToObj(Var::new)
                .map(variables::get)
                .collect(Collectors.toList());

        Set<List<Integer>> found = new HashSet<>();
        IVec<IVecInt> ret = new Vec<>();

        Board b = this.target;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    List<Integer> vars = infos.stream().map(this::infoToVar).map(v -> v == null ? null : v.getIndex()).collect(Collectors.toList());
                    if (vars.stream().noneMatch(Objects::isNull)) {
                        if (found.add(vars)) {
                            int[] newModel = new int[model.length];
                            for (int l = 1; l <= model.length; l++) {
                                newModel[l - 1] = vars.contains(l) ? -l : l;
                            }
                            ret.push(new VecInt(newModel));
                        }
                    }

                    b = b.rotX();
                    Board bx = b;
                    infos.replaceAll(info -> this.rotX(bx, info));
                }

                b = b.rotY();
                Board by = b;
                infos.replaceAll(info -> this.rotY(by, info));
            }

            b = b.rotZ();
            Board bz = b;
            infos.replaceAll(info -> this.rotZ(bz, info));
        }

        return ret;
    }

    private static class Info {
        public final int x;
        public final int y;
        public final int z;
        public final int bordIndex;
        public final int rot;

        public Info(int x, int y, int z, int bordIndex, int rot) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.bordIndex = bordIndex;
            this.rot = rot;
        }

        @Override
        public String toString() {
            return String.format("p%d%db%d%d%d", bordIndex, rot, x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Info info = (Info) o;
            return x == info.x &&
                    y == info.y &&
                    z == info.z &&
                    bordIndex == info.bordIndex &&
                    rot == info.rot;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, bordIndex, rot);
        }
    }

    public static class VarFactory {
        private int index = 1;

        public Var getVar() {
            return new Var(index++);
        }
    }

    public static class Var {
        private final int index;

        public Var(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Var var = (Var) o;
            return index == var.index;
        }

        @Override
        public int hashCode() {

            return Objects.hash(index);
        }
    }

    public static class Index {
        public final int x, y, z;

        public Index(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index = (Index) o;
            return x == index.x &&
                    y == index.y &&
                    z == index.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

}
