import groovy.transform.Immutable

class Cube {

    Set<Bit> bits

    Slice getAt(Map map) {
        bits.findAll {
            map.any { k, v -> v == it.position."$k" }
        }.tap {
            if (it.size() != 9) throw new RuntimeException("Must be exactly 9 bits in slice $map")
        }.with {
            new Slice(bits: it)
        }
    }

    @Override
    String toString() {
        (0..2).collect { this[[x: it]].toString() }.join('|') + '$'
    }

    Boolean getIsComplete() {
        def test = {
            if (it.any { it == null }) throw new RuntimeException("Null color")
            return it.toSet().size() == 1
        }
        this[[x: 2]].bits.collect { it.side.xP }.with(test) &&
                this[[y: 2]].bits.collect { it.side.yP }.with(test) &&
                this[[z: 2]].bits.collect { it.side.zP }.with(test) &&
                this[[x: 0]].bits.collect { it.side.xN }.with(test) &&
                this[[y: 0]].bits.collect { it.side.yN }.with(test) &&
                this[[z: 0]].bits.collect { it.side.zN }.with(test)
    }

}

enum Color {
    R, Y, O, G, W, P
}

class Bit {
    Position position
    Side side

    void rotateXP() { side.rotate('yP', 'zP', 'yN', 'zN') }
    void rotateXN() { side.rotate('zN', 'yN', 'zP', 'yP') }
    void rotateYP() { side.rotate('zP', 'xP', 'zN', 'xN') }
    void rotateYN() { side.rotate('xN', 'zN', 'xP', 'zP') }
    void rotateZP() { side.rotate('xP', 'yP', 'xN', 'yN') }
    void rotateZN() { side.rotate('yN', 'xN', 'yP', 'xP') }

    @Override
    String toString() { "$side" }

}

@Immutable
class Position {
    Integer x
    Integer y
    Integer z

    @Override
    String toString() { "$x$y$z" }
}

class Side {
    Color xP
    Color xN
    Color yP
    Color yN
    Color zP
    Color zN

    void rotate(String... args) {
        args.toList().withIndex().collect {
            new Tuple2<>(it.first, this."${args[it.second - 1]}")
        }.each {
            this."${it.first}" = it.second
        }
    }

    @Override
    String toString() { "${xP?:' '}${yP?:' '}${zP?:' '}${xN?:' '}${yN?:' '}${zN?:' '}" }
}

class Slice {
    Set<Bit> bits

    Bit getAt(Map map) {
        bits.findAll {
            map.every { k, v -> v == it.position."$k" }
        }.tap {
            if (it.size() == 0) throw new RuntimeException("No bit at $map")
            if (it.size() > 1) throw new RuntimeException("Ambigous bits at $map")
        }.first()
    }

    void rotateXP() {
        rotate([ [z: 0, y: 0], [z: 0, y: 2], [z: 2, y: 2], [z: 2, y: 0] ])
        rotate([ [z: 0, y: 1], [z: 1, y: 2], [z: 2, y: 1], [z: 1, y: 0] ])
        bits.each { it.rotateXP() }
    }

    void rotateXN() {
        rotate([ [z: 0, y: 0], [z: 0, y: 2], [z: 2, y: 2], [z: 2, y: 0] ].<Map>reverse())
        rotate([ [z: 0, y: 1], [z: 1, y: 2], [z: 2, y: 1], [z: 1, y: 0] ].<Map>reverse())
        bits.each { it.rotateXN() }
    }

    void rotateYP() {
        rotate([ [x: 0, z: 0], [x: 0, z: 2], [x: 2, z: 2], [x: 2, z: 0] ])
        rotate([ [x: 0, z: 1], [x: 1, z: 2], [x: 2, z: 1], [x: 1, z: 0] ])
        bits.each { it.rotateYP() }
    }

    void rotateYN() {
        rotate([ [x: 0, z: 0], [x: 0, z: 2], [x: 2, z: 2], [x: 2, z: 0] ].<Map>reverse())
        rotate([ [x: 0, z: 1], [x: 1, z: 2], [x: 2, z: 1], [x: 1, z: 0] ].<Map>reverse())
        bits.each { it.rotateYN() }
    }

    void rotateZP() {
        rotate([ [y: 0, x: 0], [y: 0, x: 2], [y: 2, x: 2], [y: 2, x: 0] ])
        rotate([ [y: 0, x: 1], [y: 1, x: 2], [y: 2, x: 1], [y: 1, x: 0] ])
        bits.each { it.rotateZP() }
    }

    void rotateZN() {
        rotate([ [y: 0, x: 0], [y: 0, x: 2], [y: 2, x: 2], [y: 2, x: 0] ].<Map>reverse())
        rotate([ [y: 0, x: 1], [y: 1, x: 2], [y: 2, x: 1], [y: 1, x: 0] ].<Map>reverse())
        bits.each { it.rotateZN() }
    }

    void rotate(List<Map> coords) {
        coords.withIndex().collect {
            new Tuple2<>(this[it.first], this[coords[(it.second + 1) % coords.size()]].position)
        }.each {
            it.first.position = it.second
        }
    }

    @Override
    String toString() {
        bits.sort { it.position.toString() }
            .collect { it.toString() }
            .join(';')
    }
}

static Cube makeCube() {
    List<Map> order = [
            [y: 0, z: 0], [y: 0, z: 1], [y: 0, z: 2],
            [y: 1, z: 0], [y: 1, z: 1], [y: 1, z: 2],
            [y: 2, z: 0], [y: 2, z: 1], [y: 2, z: 2]
    ]

    List<Integer> xOrder = [0, 1, 2]

    def YP = Color.R
    def YN = Color.P
    def XP = Color.Y
    def XN = Color.O
    def ZP = Color.G
    def ZN = Color.W

    Set<List<Side>> NORMAL = [
            [new Side(zN: ZN, xN: XN, yN: YN), new Side(xN: XN, yN: YN), new Side(zP: ZP, xN: XN, yN: YN),
             new Side(xN: XN, zN: ZN), new Side(xN: XN), new Side(zP: ZP, xN: XN),
             new Side(yP: YP, xN: XN, zN: ZN), new Side(yP: YP, xN: XN), new Side(zP: ZP, yP: YP, xN: XN)],

            [new Side(yN: YN, zN: ZN), new Side(yN: YN), new Side(zP: ZP, yN: YN),
             new Side(zN: ZN), new Side(), new Side(zP: ZP),
             new Side(yP: YP, zN: ZN), new Side(yP: YP), new Side(zP: ZP, yP: YP)],

            [new Side(xP: XP, zN: ZN, yN: YN), new Side(xP: XP, yN: YN), new Side(zP: ZP, xP: XP, yN: YN),
             new Side(xP: XP, zN: ZN), new Side(xP: XP), new Side(zP: ZP, xP: XP),
             new Side(yP: YP, xP: XP, zN: ZN), new Side(yP: YP, xP: XP), new Side(yP: YP, zP: ZP, xP: XP)],
    ]

    return xOrder.collectMany { xit ->
        new ArrayList<Bit>().tap { bits ->
            NORMAL[xit].eachWithIndex { Side entry, int i ->
                def pos = order[i]
                bits.add(new Bit(
                        position: new Position(x: xit, z: pos['z'], y: pos['y'] ),
//                        rotation: new Rotation(rX: 0, rY: 0, rZ: 0),
                        side: entry
                ))
            }
        }
    }.with {
        new Cube(bits: it)
    }
}

static void randomizeCube(Cube cube) {
    final def SLICES = [ [x: 0], [x: 1], [x: 2], [z: 0], [z: 1], [z: 2], [y: 0], [y: 1], [y: 2] ]

    final def ROTATIONS = [
            x: ['rotateXP', 'rotateXN'],
            y: ['rotateYP', 'rotateYN'],
            z: ['rotateZP', 'rotateZN']
    ]

    final def random = new Random()

    100.times {
        SLICES[random.nextInt(SLICES.size())].tap { slice ->
            ROTATIONS[slice.entrySet().first().key].with { it[random.nextInt(it.size())] }.tap { rotation ->
                cube[slice]."$rotation"()
            }
        }
    }
}

static void test() {
    def cube = makeCube()

    assert cube.isComplete : "test initial"

    cube[[x: 2]].rotateXP()
    assert !cube.isComplete
    cube[[x: 2]].rotateXP()
    cube[[x: 2]].rotateXP()
    cube[[x: 2]].rotateXP()
    assert cube.isComplete : "test x rotation 4"

    cube[[x: 2]].rotateXP()
    assert !cube.isComplete
    cube[[x: 2]].rotateXP()
    cube[[x: 2]].rotateXP()
    cube[[x: 2]].rotateXP()
    assert cube.isComplete : "test x rotation 4"

    cube[[x: 0]].rotateXP()
    assert !cube.isComplete
    cube[[x: 0]].rotateXN()
    assert cube.isComplete : "test x0 rotation return"

    cube[[x: 1]].rotateXP()
    assert !cube.isComplete
    cube[[x: 1]].rotateXN()
    assert cube.isComplete : "test x1 rotation return"

    cube[[x: 2]].rotateXP()
    assert !cube.isComplete
    cube[[x: 2]].rotateXN()
    assert cube.isComplete : "test x2 rotation return"

    cube[[y: 0]].rotateYP()
    assert !cube.isComplete
    cube[[y: 0]].rotateYN()
    assert cube.isComplete : "test y0  rotation return"

    cube[[y: 1]].rotateYP()
    assert !cube.isComplete
    cube[[y: 1]].rotateYN()
    assert cube.isComplete : "test y1 rotation return"

    cube[[y: 2]].rotateYP()
    assert !cube.isComplete
    cube[[y: 2]].rotateYN()
    assert cube.isComplete : "test y2 rotation return"

    cube[[z: 0]].rotateZP()
    assert !cube.isComplete
    cube[[z: 0]].rotateZN()
    assert cube.isComplete : "test z0 rotation return"

    cube[[z: 1]].rotateZP()
    assert !cube.isComplete
    cube[[z: 1]].rotateZN()
    assert cube.isComplete : "test z1 rotation return"

    cube[[z: 2]].rotateZP()
    assert !cube.isComplete
    cube[[z: 2]].rotateZN()
    assert cube.isComplete : "test z2 rotation return"

    cube[[x: 0]].rotateXP()
    assert !cube.isComplete
    cube[[x: 1]].rotateXP()
    cube[[x: 2]].rotateXP()
    assert cube.isComplete : "test full x rotation"

    cube[[x: 0]].rotateXN()
    assert !cube.isComplete
    cube[[x: 1]].rotateXN()
    cube[[x: 2]].rotateXN()
    assert cube.isComplete : "test full x return"

    cube[[y: 0]].rotateYP()
    assert !cube.isComplete
    cube[[y: 1]].rotateYP()
    cube[[y: 2]].rotateYP()
    assert cube.isComplete : "test full y rotation"

    cube[[y: 0]].rotateYN()
    assert !cube.isComplete
    cube[[y: 1]].rotateYN()
    cube[[y: 2]].rotateYN()
    assert cube.isComplete : "test y full return"

    cube[[z: 0]].rotateZP()
    assert !cube.isComplete
    cube[[z: 1]].rotateZP()
    cube[[z: 2]].rotateZP()
    assert cube.isComplete : "test z full rotation"

    cube[[z: 0]].rotateZN()
    assert !cube.isComplete
    cube[[z: 1]].rotateZN()
    cube[[z: 2]].rotateZN()
    assert cube.isComplete : "test z full rotation"

    cube[[y: 0]].rotateYP()
    assert !cube.isComplete
    cube[[y: 1]].rotateYP()
    cube[[y: 2]].rotateYP()
    assert cube.isComplete
    cube[[y: 2]].rotateYN()
    assert !cube.isComplete
    cube[[y: 1]].rotateYN()
    cube[[y: 0]].rotateYN()
    assert cube.isComplete : "test all y rotation then return"

    cube[[y: 0]].rotateYP()
    assert !cube.isComplete
    cube[[y: 0]].rotateYN()
    assert cube.isComplete
    cube[[y: 1]].rotateYP()
    cube[[y: 1]].rotateYN()
    cube[[y: 2]].rotateYP()
    cube[[y: 2]].rotateYN()
    assert cube.isComplete : "test all y rotation-return"
}

test()

static void randomTest() {
    def cube = makeCube()

    randomizeCube(cube)

    def sides = ['xP', 'yP', 'zP', 'xN', 'yN', 'zN']

    cube.bits.collectMany {
        sides.collect { s -> it.side."$s" }
    }.groupBy { it }.tap {
        assert it[null] != null
        assert it[null].size() == 108
        it.entrySet().findAll { it.key != null }.tap {
            assert it.size() == 6
        }.each {
            assert it.value.size() == 9
        }
    }

    assert cube.bits.collect { it.position.toString() }.toSet().size() == 27
}

randomTest()

static void randomReturnTest() {
    def cube = makeCube()
    def cubeString = cube.toString()

    def rotations = [
            rotateXP: 'rotateXN',
            rotateXN: 'rotateXP',
            rotateYP: 'rotateYN',
            rotateYN: 'rotateYP',
            rotateZP: 'rotateZN',
            rotateZN: 'rotateZP'
    ]

    def axes = [
            x: ['rotateXP', 'rotateXN'],
            y: ['rotateYP', 'rotateYN'],
            z: ['rotateZP', 'rotateZN']
    ]

    def random = new Random()
    def history = []

    100.times {
        def axe = axes.entrySet()[random.nextInt(2)]
        def rotation = axe.value[random.nextInt(axe.value.size())]
        def slice = [:].tap { put("${axe.key}".toString(), random.nextInt(3)) }

        history.add(new Tuple2<>(slice, rotation))
        cube[slice]."$rotation"()
    }

    history.reverse().forEach { Tuple2 hist ->
        cube[hist.first]."${rotations[hist.second]}"()
    }

    assert cube.isComplete
    assert cubeString == cube.toString()
}

randomReturnTest()

/**
 * Визуальное представление куба в виде 6 граней. Частицы каждой грани отсортированы по xyz
 * @param cube
 * @return
 */
static String formatCube0(Cube cube) {
    def sides = [
            [x: 2]: 'xP',
            [y: 2]: 'yP',
            [z: 2]: 'zP',
            [x: 0]: 'xN',
            [y: 0]: 'yN',
            [z: 0]: 'zN'
    ]

    def slides = sides.collectEntries {
        def bits = cube[it.key].bits.sort(false) { it.position.toString() }
        [(bits): it.value]
    }
    def stringBuilder = new StringBuilder()

    (0..2).each { row ->
        slides.each { entry ->
            (0..2).each { column ->
                def slice = entry.key
                def side = entry.value
                Color color = slice[row * 3 + column].side."$side"
                stringBuilder.append(color)
            }
            stringBuilder.append('   ')
        }
        stringBuilder.append('\n')
    }

    return stringBuilder.toString()
}

/**
 * Более наглядное представление куба.
 * Визуально - шесть граней. Верх и них выводятся первой и последней гранью.
 * У остальных граней y = строка
 * @param cube
 * @return
 */
static String formatCube(Cube cube) {
    def sides = [
            [y: 2]: 'yP',
            [x: 2]: 'xP',
            [z: 0]: 'zN',
            [x: 0]: 'xN',
            [z: 2]: 'zP',
            [y: 0]: 'yN'
    ]

    def slides = sides.collectEntries {
        def bits = cube[it.key].bits.sort(false) { it.position.with { "${it.y}${it.x}${it.z}" } }
        [(bits): it.value]
    }
    def stringBuilder = new StringBuilder()

    (0..2).each { row ->
        slides.each { entry ->
            (0..2).each { column ->
                def slice = entry.key
                def side = entry.value
                Color color = slice[row * 3 + column].side."$side"
                stringBuilder.append(color)
            }
            stringBuilder.append('   ')
        }
        stringBuilder.append('\n')
    }

    return stringBuilder.toString()
}

static void traverse() {
    final def SLICES = [ [x: 0], [x: 1], [x: 2], [z: 0], [z: 1], [z: 2], [y: 0], [y: 1], [y: 2] ]

    final def ROTATIONS = [
            x: ['rotateXP', 'rotateXN'],
            y: ['rotateYP', 'rotateYN'],
            z: ['rotateZP', 'rotateZN']
    ]

    final def RETURNS = [
            rotateXP: 'rotateXN',
            rotateXN: 'rotateXP',
            rotateYP: 'rotateYN',
            rotateYN: 'rotateYP',
            rotateZP: 'rotateZN',
            rotateZN: 'rotateZP'
    ]

    final def WAYS = SLICES
            .collectMany { slice -> ROTATIONS[slice.entrySet().first().key].collect { new Tuple2<>(slice, it) } }

    final def states = new HashSet<String>()

    final def cube = makeCube()

    def process
    process = { String state ->
        if (states.contains(state)) {
//            println "return"
            return
        }

//        println "${formatCube(cube)} | ${states.size()} | "

        states << state

        WAYS.each { way ->
            cube[way.first]."${way.second}"()
            process(cube.toString())
            cube[way.first]."${RETURNS[way.second]}"()
        }
    }

    process(cube.toString())
}

static void traverse2() {
    final def SLICES = [ [x: 0], [x: 1], [x: 2], [z: 0], [z: 1], [z: 2], [y: 0], [y: 1], [y: 2] ]

    final def ROTATIONS = [
            x: ['rotateXP', 'rotateXN'],
            y: ['rotateYP', 'rotateYN'],
            z: ['rotateZP', 'rotateZN']
    ]

    final def RETURNS = [
            rotateXP: 'rotateXN',
            rotateXN: 'rotateXP',
            rotateYP: 'rotateYN',
            rotateYN: 'rotateYP',
            rotateZP: 'rotateZN',
            rotateZN: 'rotateZP'
    ]

    final def WAYS = SLICES
            .collectMany { slice -> ROTATIONS[slice.entrySet().first().key].collect { new Tuple2<>(slice, it) } }

    final def states = new HashSet<String>()

    final def way = new LinkedList<Tuple2<Map, String>>()

    final def cube = makeCube()
    randomizeCube(cube)

    while (!cube.isComplete) {
        def state = cube.toString()

        //println "${formatCube(cube)} | ${states.size()} | ${way.size()}"

        if (states.contains(state)) {
            def ret = way.poll()
            cube[ret.first]."${RETURNS[ret.second]}"()
            //println "return"

            if (ret != WAYS.last()) {
                def next = WAYS[WAYS.indexOf(ret) + 1]
                cube[next.first]."${next.second}"()
                way.push(next)
            } else {
//                println "last"
            }

            continue
        }

        states << state

        def step = WAYS.first()
        cube[step.first]."${step.second}"()
        way.push(step)
    }

}

static void completeStep1(Cube cube) {
    def color = cube[[y: 2]][[x: 1, z: 1]].side.yP
    def testPattern = ".$color.$color{3}.$color.".toString()

    /**
     * крест на верхней грани, кубики креста совпадают с серединками
     */
    def test = {
        cube[[y: 2]].bits.sort(false) { it.position.toString() } .collect { "${it.side.yP}" }.join("") ==~ testPattern &&
                [
                        new Tuple3('xP', [x: 2], [[y: 1, z: 1], [y: 2, z: 1]]),
                        new Tuple3('zN', [z: 0], [[x: 1, y: 1], [x: 1, y: 2]]),
                        new Tuple3('xN', [x: 0], [[y: 2, z: 1], [y: 2, z: 1]]),
                        new Tuple3('zP', [z: 2], [[x: 1, y: 1], [x: 1, y: 2]])
                ].every { tuple ->
                    cube[tuple.second].with { slice -> tuple.third.collect { slice[it].side."${tuple.first}" } .toSet().size() } == 1
                }
    }

    /**
     * все кубики креста на нажней грани
     */
    def prerequisitesTest0 = {
        cube[[y: 0]].with { slice ->
            [[x: 0, z: 1], [x: 1, z: 2], [x: 2, z: 1], [x: 1, z: 0]].collect { slice[it] }.every { bit ->
                ['yN', 'xP', 'xN', 'zP', 'zN'].any {
                    bit.side."$it" == color
                }
            }
        }
    }

//    println "prerequisitesTest0"
    while (!prerequisitesTest0()) {
        /**
         * на нижней грани есть кубик креста
         */
        def prerequisitesTest00 = {
            cube[[x: 2]][[y: 0, z: 1]].side.with { it.xP == color || it.yN == color }
        }

        /**
         * кубик креста на лицевой плоскости, но не внизу
         */
        def prerequisitesTest01 = {
            cube[[x: 2]].with { slice ->
                [[y:2, z:1], [y: 1, z: 0], [y: 1, z: 2]].collect { slice[it] }.any { bit ->
                    ['xP', 'zP', 'zN', 'yP'].any {
                        bit.side."$it" == color
                    }
                }
            }
        }

//        println "prerequisitesTest01"
        while (prerequisitesTest01()) {
//            println "prerequisitesTest00"
            while (prerequisitesTest00()) {
                cube[[y: 0]].rotateYP()

//                println "change00\n${formatCube(cube)}"
            }

            cube[[x: 2]].rotateXP()

//            println "change01\n${formatCube(cube)}"
        }

        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()

//        println "change0\n${formatCube(cube)}"
    }

    while (!test()) {
        def color2 = cube[[x: 2]][[y: 1, z: 1]].side.xP
//        println "step 1, $color, $color2\n${formatCube(cube)}"

        /**
         * внизу лицевой плостости нужный кубик в любом положении
         */
        def prerequisitesTest2 = {
            cube[[x: 2]][[y: 0, z: 1]].side.with { (it.xP == color && it.yN == color2) || (it.xP == color2 && it.yN == color) }
        }

//        println "prerequisitesTest2"
        while (!prerequisitesTest2()) {
            cube[[y: 0]].rotateYP()

//            println "change2\n${formatCube(cube)}"
        }

        if (cube[[x: 2]][[y: 0, z: 1]].side.yN == color) {
//            println "way 1"

            cube[[x: 2]].rotateXN()
            cube[[x: 2]].rotateXN()
        } else {
//            println "way 2"

            cube[[y: 0]].rotateYP()
            cube[[z: 0]].rotateZP()
            cube[[x: 2]].rotateXP()
            cube[[z: 0]].rotateZN()
        }

//        println "result\n${formatCube(cube)}"
        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()
    }
}

static void completeStep2(Cube cube) {

    def color = cube[[y: 2]][[x: 1, z: 1]].side.yP

    /**
     * собрана вся верхняя грань, боковые цвета совпадают с серединками
     */
    def test = {
        cube[[y: 2]].bits.with { bits ->
            bits.every { it.side.yP == color }
        } &&
                cube[[x: 2]].with { slice -> slice.bits.findAll { it.position.y == 2 } .every { it.side.xP ==  slice[[y: 1, z: 1]].side.xP}} &&
                cube[[z: 0]].with { slice -> slice.bits.findAll { it.position.y == 2 } .every { it.side.zN ==  slice[[x: 1, y: 1]].side.zN}} &&
                cube[[x: 0]].with { slice -> slice.bits.findAll { it.position.y == 2 } .every { it.side.xN ==  slice[[y: 1, z: 1]].side.xN}} &&
                cube[[z: 2]].with { slice -> slice.bits.findAll { it.position.y == 2 } .every { it.side.zP ==  slice[[x: 1, y: 1]].side.zP}}
    }

    /**
     * на верхней грани есть угловые кубики с цветом верхней грани
     */
    def prerequisitesTest1 = {
        [[x: 0, z: 0], [x:0, z: 2], [x: 2, z: 0], [x: 2, z: 2]].any {
            ['xP', 'xN', 'yP', 'yN', 'zP', 'zN'].any { side ->
                cube[[y: 2]][it].side."$side" == color
            }
        }
    }

    /**
     * кубик 222 содержит цвет верхней грани
     */
    def prerequisitesTest11 = {
        ['xP', 'yP', 'zP'].any { side ->
            cube[[y: 2]][[x: 2, z: 2]].side."$side" == color
        }
    }

    /**
     * убираем угловые кубики верхней грани на нижнюю грань
     */
//    println "prerequisitesTest1"
    while (prerequisitesTest1()) {
//        println "prerequisitesTest11"
        while (prerequisitesTest11()) {
            cube[[z: 2]].rotateZN()
            cube[[y: 0]].rotateYP()
            cube[[z: 2]].rotateZP()
            cube[[y: 0]].rotateYP()

//            println "change11\n${formatCube(cube)}"
        }

        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()

//        println "change1\n${formatCube(cube)}"
    }

    while (!test()) {
        def color2 = cube[[y: 2]][[x: 2, z: 1]].side.xP
//        println "step 2, $color, $color2\n${formatCube(cube)}"

        /**
         * кубик 202 содержит цвет верхней грани и цвет лицевой грани
         */
        def prerequisitesTest2 = {
            [['xP', 'yN'], ['yN', 'zP'], ['zP', 'xP']].collectEntries().any { entry ->
                cube[[x: 2]][[y: 0, z: 2]].side.with { it."${entry.key}" == color && it."${entry.value}" == color2 }
            }
        }

        /**
         * ищем угловой кубин на нижней грани
         */
//        println "prerequisitesTest2"
        while (!prerequisitesTest2()) {
            cube[[y: 0]].rotateYP()

//            println "change2\n${formatCube(cube)}"
        }

        switch (cube[[x: 2]][[y: 0, z: 2]].side) {
            case { it.zP == color }:
//                println "way 1"
                cube[[z: 2]].rotateZN()
                cube[[y: 0]].rotateYP()
                cube[[z: 2]].rotateZP()
                break
            case { it.xP == color }:
//                println "way 2"
                cube[[x: 2]].rotateXP()
                cube[[y: 0]].rotateYN()
                cube[[x: 2]].rotateXN()
                break
            case { it.yN == color }:
//                println "way 3"
                cube[[x: 2]].rotateXP()
                cube[[z: 0]].rotateZN()
                cube[[y: 0]].rotateYP()
                cube[[y: 0]].rotateYP()
                cube[[z: 0]].rotateZP()
                cube[[x: 2]].rotateXN()
                break
            default: throw new RuntimeException("Invalid state")
        }

//        println "result\n${formatCube(cube)}"
        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()
    }
}

static void completeStep3(Cube cube) {
    /**
     * собраны 2 верхних слоя
     */
    def test = {
        cube[[y: 2]].with { slice ->
            slice.bits.every { it.side.yP == slice[[x: 1, z: 1]].side.yP }
        } &&
                cube[[x: 2]].with { slice -> slice.bits.findAll { it.position.y == 2 || it.position.y == 1 } .every { it.side.xP ==  slice[[y: 1, z: 1]].side.xP}} &&
                cube[[z: 0]].with { slice -> slice.bits.findAll { it.position.y == 2 || it.position.y == 1 } .every { it.side.zN ==  slice[[x: 1, y: 1]].side.zN}} &&
                cube[[x: 0]].with { slice -> slice.bits.findAll { it.position.y == 2 || it.position.y == 1 } .every { it.side.xN ==  slice[[y: 1, z: 1]].side.xN}} &&
                cube[[z: 2]].with { slice -> slice.bits.findAll { it.position.y == 2 || it.position.y == 1 } .every { it.side.zP ==  slice[[x: 1, y: 1]].side.zP}}
    }

    while (!test()) {
        def color = cube[[x: 2]][[y: 1, z: 1]].side.xP
        def colorL = cube[[z: 2]][[y: 1, x: 1]].side.zP
        def colorR = cube[[z: 0]][[y: 1, x: 1]].side.zN
//        println "step 3, $color, $colorL, $colorR\n${formatCube(cube)}"

        /**
         * угловые кубы y1 содержат этот цвет
         */
        def prerequisitesTest1 = {
            [[x: 0, z: 0], [x:0, z: 2], [x: 2, z: 0], [x: 2, z: 2]].any {
                ['xP', 'xN', 'zP', 'zN'].any { side ->
                    cube[[y: 1]][it].side."$side" == color
                }
            }
        }

        /**
         * некоторые крайние кубики y1 на лицевой стороне содержат этот цвет
         */
        def prerequisitesTest2 = {
            cube[[x: 2]].with { slice ->
                [[y: 1, z: 0], [y: 1, z: 2]].any {
                    slice[it].side.xP == color
                }
            }
        }

        /**
         * нижний кубик содержи этот цвет
         */
        def prerequisitesTest3 = {
            cube[[x: 2]][[y: 0, z: 1]].side.with { it.xP == color || it.yN == color}
        }

        /**
         * убираем угловые кубики верхней грани на нижнюю грань
         */
//        println "prerequisitesTest1"
        while (prerequisitesTest1()) {
            /**
             * ищем угловые кубики на разных сторонах
             */
//            println "prerequisitesTest2"
            while (!prerequisitesTest2()) {
                cube[[y: 0]].rotateYP()
                cube[[y: 1]].rotateYP()
                cube[[y: 2]].rotateYP()

//                println "change2\n${formatCube(cube)}"
            }

            /**
             * двигаем нижнюю грань что бы не было углоого кубика этого цвета
             */
//            println "prerequisitesTest3"
            while (prerequisitesTest3()) {
                cube[[y: 0]].rotateYP()

//                println "change3\n${formatCube(cube)}"
            }

            /**
             * убираем угловой кубик вниз
             */
            cube[[x: 2]].tap { slice ->
                switch (slice) {
                    case { it[[y: 1, z: 2]].side.xP == color }:
                        cube[[x: 2]].rotateXP()
                        cube[[y: 0]].rotateYN()
                        cube[[x: 2]].rotateXN()
                        cube[[y: 0]].rotateYP()
                        cube[[z: 2]].rotateZN()
                        cube[[y: 0]].rotateYP()
                        cube[[z: 2]].rotateZP()
                        cube[[y: 0]].rotateYN()

//                        println "change1a\n${formatCube(cube)}"
                        break
                    case { it[[y: 1, z: 0]].side.xP == color }:
                        cube[[x: 2]].rotateXN()
                        cube[[y: 0]].rotateYP()
                        cube[[x: 2]].rotateXP()
                        cube[[y: 0]].rotateYN()
                        cube[[z: 0]].rotateZN()
                        cube[[y: 0]].rotateYN()
                        cube[[z: 0]].rotateZP()
                        cube[[y: 0]].rotateYP()

//                        println "change1b\n${formatCube(cube)}"
                        break
                    default: throw new RuntimeException('Invalid state')
                }
            }
        }

        /**
         * лицевая грань этого цвета
         */
        def prerequisitesTest4 = {
            cube[[x: 2]][[y: 1, z: 1]].side.xP == color
        }

        /**
         * вернемся к нужной стороне
         */
//        println "prerequisitesTest4"
        while (!prerequisitesTest4()) {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "change4\n${formatCube(cube)}"
        }

        /**
         * на нижней грани есть угловые кубики с этим, правым и левым цветами
         */
        def subtest = {
            [
                    new Tuple3([x: 2], 'xP', [y: 0, z: 1]),
                    new Tuple3([z: 0], 'zN', [y: 0, x: 1]),
                    new Tuple3([x: 0], 'xN', [y: 0, z: 1]),
                    new Tuple3([z: 2], 'zP', [y: 0, x: 1]),
            ].any { tuple ->
                cube[tuple.first].with { slice -> slice[tuple.third].side }
                    .with {
                        (it."${tuple.second}" == color && (it.yN == colorL || it.yN == colorR))
                    }
            }
        }

        /**
         * на нижней грани гловой кубик с нужными цветами
         */
        def prerequisitesTest5 = {
            cube[[x: 2]][[y: 0, z: 1]].side.with {
                (it.xP == color && (it.yN == colorR || it.yN == colorL))
            }
        }

        /**
         * поставим угловые кубики в нужные места
         */
        while (subtest()) {
//            println "prerequisitesTest5"
            while (!prerequisitesTest5()) {
                cube[[y: 0]].rotateYP()

//                println "change5\n${formatCube(cube)}"
            }

            switch (cube[[x: 2]][[y: 0, z: 1]].side) {
                case { it.yN == colorL }:
//                    println "way 1"
                    cube[[y: 0]].rotateYP()
                    cube[[z: 2]].rotateZN()
                    cube[[y: 0]].rotateYN()
                    cube[[z: 2]].rotateZP()
                    cube[[y: 0]].rotateYN()
                    cube[[x: 2]].rotateXP()
                    cube[[y: 0]].rotateYP()
                    cube[[x: 2]].rotateXN()
                    break
                case { it.yN == colorR }:
//                    println "way 2"
                    cube[[y: 0]].rotateYN()
                    cube[[z: 0]].rotateZN()
                    cube[[y: 0]].rotateYP()
                    cube[[z: 0]].rotateZP()
                    cube[[y: 0]].rotateYP()
                    cube[[x: 2]].rotateXN()
                    cube[[y: 0]].rotateYN()
                    cube[[x: 2]].rotateXP()
                    break
                default: throw new RuntimeException("Invalid state")
            }
        }


//        println "result\n${formatCube(cube)}"
        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()
    }
}

static void completeStep4(Cube cube) {
    cube[[x: 0]].rotateXP()
    cube[[x: 1]].rotateXP()
    cube[[x: 2]].rotateXP()
    cube[[x: 0]].rotateXP()
    cube[[x: 1]].rotateXP()
    cube[[x: 2]].rotateXP()

    /**
     * кубики вернего креста находятся на нужных позициях, но могут быть в неправильном положении
     */
    def test = {
        cube[[y: 2]][[x: 1, z: 1]].side.yP.with { top ->
            [
                new Tuple4(cube[[x: 2]], [z: 1, y: 1], 'xP', [z: 1, y: 2]),
                new Tuple4(cube[[z: 0]], [x: 1, y: 1], 'zN', [x: 1, y: 2]),
                new Tuple4(cube[[x: 0]], [z: 1, y: 1], 'xN', [z: 1, y: 2]),
                new Tuple4(cube[[z: 2]], [x: 1, y: 1], 'zP', [x: 1, y: 2])
            ].every {
                def side = it.first[it.second].side."${it.third}"
                it.first[it.fourth].side.with { target ->
                    (target.yP == top && target."${it.third}" == side) ||
                            (target.yP == side && target."${it.third}" == top)
                }
            }
        }
    }

    while (!test()) {
        /**
         * верхний средний кубик на лицевой стороне находится на своей позиции
         */
        def prerequisitesTest = {
            cube[[y: 2]][[x: 1, z: 1]].side.yP.with { top ->
                cube[[x: 2]].with { slice ->
                    def side = slice[[z: 1, y: 1]].side.xP
                    slice[[z: 1, y: 0]].side.with { target ->
                        (target.yP == top && target.xP == side) ||
                                (target.yP == side && target.xP == top)
                    }
                }
            }
        }

//        println "prerequisitesTest"
        while (prerequisitesTest()) {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "change\n${formatCube(cube)}"
        }

        def colorT = cube[[y: 2]][[x: 1, z: 1]].side.yP
        def colorS = cube[[x: 2]][[y: 1, z: 1]].side.xP
//        println "step 4, $colorT, $colorS\n${formatCube(cube)}"

        /**
         * Верхний средний кубик на лицевой стороне - искомый
         */
        def findTest = {
            cube[[x: 2]][[y: 2, z: 1]].side.with {
                (it.xP == colorT && it.yP == colorS) || (it.xP == colorS && it.yP == colorT)
            }
        }

//        println "findTest"
        while (!findTest()) {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "find\n${formatCube(cube)}"
        }

        /**
         * В исходной позиции
         */
        def returnTest = {
            cube[[x: 2]][[y: 1, z: 1]].side.xP == colorS
        }

//        println "returnTest"
        while (!returnTest()) {
            cube[[y: 0]].rotateYN()
            cube[[y: 1]].rotateYN()
            cube[[y: 2]].rotateYN()

            cube[[y: 2]].rotateYN()
            cube[[x: 2]].rotateXN()
            cube[[z: 0]].rotateZP()
            cube[[y: 2]].rotateYN()
            cube[[z: 0]].rotateZN()
            cube[[y: 2]].rotateYP()
            cube[[x: 2]].rotateXP()

//            println "change\n${formatCube(cube)}"
        }

//        println "result\n${formatCube(cube)}"
        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()
    }
}

static void completeStep5(Cube cube) {
    /**
     * все кубики верхнего креста одного цвета
     */
    def test = {
        cube[[y: 2]].with { slice ->
            [[x: 1, z: 0], [x: 0, z: 1], [x: 1, z: 2], [x: 2, z: 1], [x: 1, z: 1]]
                    .collect { slice[it].side.yP }
                    .toSet().size() == 1
        }
    }

    while (!test()) {
        def color = cube[[y: 2]][[x: 1, z: 1]].side.yP
//        println "step 4, $color\n${formatCube(cube)}"

        def findTest = {
            cube[[y: 2]].with { slice ->
                slice[[x: 2, z: 1]].side.yP != color &&
                        (slice[[x: 1, z: 0]].side.yP != color || slice[[x: 0, z: 1]].side.yP != color)
            }
        }

//        println "findTest"
        while (!findTest()) {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "change\n${formatCube(cube)}"
        }

        def change = {
            cube[[z: 0]].rotateZP()
            cube[[y: 1]].rotateYP()
            cube[[z: 0]].rotateZP()
            cube[[y: 1]].rotateYP()
            cube[[z: 0]].rotateZP()
            cube[[y: 1]].rotateYP()
            cube[[z: 0]].rotateZP()
            cube[[y: 1]].rotateYP()
        }

        switch(cube[[y: 2]]) {
            case { it[[x: 1, z: 0]].side.yP != color }:
//                println "way 1"

                change()

                cube[[y: 2]].rotateYP()

                change()

                cube[[y: 2]].rotateYN()

                break
            case { it[[x: 0, z: 1]].side.yP != color }:
//                println "way 2"

                change()

                cube[[y: 2]].rotateYP()
                cube[[y: 2]].rotateYP()

                change()

                cube[[y: 2]].rotateYN()
                cube[[y: 2]].rotateYN()

                break
            default: throw new RuntimeException("Invalid state")
        }

//        println "result\n${formatCube(cube)}"
        cube[[y: 0]].rotateYP()
        cube[[y: 1]].rotateYP()
        cube[[y: 2]].rotateYP()
    }
}

static void completeStep6(Cube cube) {
    def random = new Random()

    def change1 = {
        cube[[z: 0]].rotateZN()
        cube[[x: 2]].rotateXP()
        cube[[z: 2]].rotateZP()
        cube[[x: 2]].rotateXN()
        cube[[z: 0]].rotateZP()
        cube[[x: 2]].rotateXP()
        cube[[z: 2]].rotateZN()
        cube[[x: 2]].rotateXN()
    }

    /**
     * угловые кубики верхней грани находятся на своих местах, возможно в неверной позиции
     */
    def test = {
        def method = it == true ? 'any' : 'every'

        def colors = [
                'xP': cube[[x: 2]][[y: 1, z: 1]],
                'zP': cube[[z: 2]][[y: 1, x: 1]],
                'yP': cube[[y: 2]][[x: 1, z: 1]],
                'zN': cube[[z: 0]][[y: 1, x: 1]],
                'xN': cube[[x: 0]][[y: 1, z: 1]]
        ].collectEntries {
            [it.key, it.value.side."${it.key}"]
        }

        cube[[y: 2]].with { slice ->
            [
                    new Tuple2([x: 2, z: 0], ['xP', 'zN', 'yP']),
                    new Tuple2([x: 0, z: 0], ['xN', 'zN', 'yP']),
                    new Tuple2([x: 0, z: 2], ['xN', 'zP', 'yP']),
                    new Tuple2([x: 2, z: 2], ['xP', 'zP', 'yP'])
            ]."$method" {
                def bit = slice[it.first]
                def bitColors = it.second.collect { side -> bit.side."$side" }.toSet()
                def cubeColors = it.second.collect { side -> colors."$side" }.toSet()
                bitColors == cubeColors
            }
        }
    }

    while(!test()) {
//        println "step 6\n${formatCube(cube)}"

//        println "random"
        random.nextInt(3).times {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "change\n${formatCube(cube)}"
        }

        change1()

//        println "result\n${formatCube(cube)}"
    }
}

static void completeStep7(Cube cube) {
    def color = cube[[y: 2]][[x: 1, z: 1]].side.yP

    /**
     * все верхние стороны верхней грани одного цвета
     */
    def test = {
        cube[[y: 2]].with { slice -> slice.bits.every { it.side.yP == color } }
    }

    while (!test()) {
//        println "step 7, $color\n${formatCube(cube)}"

        /**
         * Найдем 2 сесдних кубика, повернутых неправильно или 2 противоположных кубика, повернутых неправильно
         */
        def findTest = {
            cube[[y: 2]].with { slice ->
                slice[[x: 2, z: 0]].side.yP != color &&
                        (slice[[x: 2, z: 2]].side.yP != color || slice[[x: 0, z: 2]].side.yP != color)
            }
        }

//        println "findTest"
        while (!findTest()) {
            cube[[y: 0]].rotateYP()
            cube[[y: 1]].rotateYP()
            cube[[y: 2]].rotateYP()

//            println "change\n${formatCube(cube)}"
        }

        def change = {
            cube[[z: 0]].rotateZP()
            cube[[x: 2]].rotateXP()
            cube[[z: 0]].rotateZN()
            cube[[x: 2]].rotateXN()

            cube[[z: 0]].rotateZP()
            cube[[x: 2]].rotateXP()
            cube[[z: 0]].rotateZN()
            cube[[x: 2]].rotateXN()
        }

        /**
         * Правй кубик верхней грани встал правильно
         */
        def subtest = {
            cube[[y: 2]][[x: 2, z: 0]].side.yP == color
        }

        switch (cube[[y: 2]]) {
            case { it[[x: 2, z: 2]].side.yP != color }:
//                println "way 1"

//                println "subtest"
                while (!subtest()) {
                    change()

                    cube[[y: 2]].rotateYP()

                    change()
                    change()

                    cube[[y: 2]].rotateYN()

//                    println "change\n${formatCube(cube)}"
                }

                break
            case { it[[x: 0, z: 2]].side.yP != color }:
//                println "way 2"

//                println "subtest"
                while (!subtest()) {
                    change()

                    cube[[y: 2]].rotateYP()
                    cube[[y: 2]].rotateYP()

                    change()
                    change()

                    cube[[y: 2]].rotateYN()
                    cube[[y: 2]].rotateYN()

//                    println "change\n${formatCube(cube)}"
                }

                break
            default: throw new RuntimeException("Invalid state")
        }

//        println "result\n${formatCube(cube)}"
    }

}

static void complete(Cube cube) {
    completeStep1(cube)
    completeStep2(cube)
    completeStep3(cube)
    completeStep4(cube)
    completeStep5(cube)
    completeStep6(cube)
    completeStep7(cube)
}

static void testComplete() {
    100.times {
        def cube = makeCube()
        randomizeCube(cube)

        complete(cube)

        assert cube.isComplete

        println formatCube(cube)
    }
}

testComplete()
