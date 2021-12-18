import com.sun.source.util.DocTreePathScanner;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static class Config {
        public static Boolean multiCase = false;
        public static Boolean debug = false; // 这里设为true不一定有效，还需要把进程的参数设为 local
    }

    public static IOUtils.FastScanner scanner = new IOUtils.FastScanner(new BufferedInputStream(System.in, 1 << 20)); // 1MB buffer
    public static PrintStream printStream = new PrintStream(new BufferedOutputStream(System.out, 1 << 20)); // 1MB buffer
    public static Random random = new Random();

    public static void simpleCase(int caseNumber) {
        List<List<Integer>> queryList = new ArrayList<>();
        int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            queryList.add(IOUtils.readIntList());
        }

        List<Integer> ans = new ArrayList<>();
        Map<Integer, Integer> replace = new HashMap<>();
        for (int i = n - 1; i >= 0; i--) {
            List<Integer> op = queryList.get(i);
            if (op.size() == 1) {
                ans.add(replace.getOrDefault(op.get(0), op.get(0)));
            } else {
                if (replace.containsKey(op.get(1))) {
                    replace.put(op.get(0), replace.get(op.get(1)));
                } else {
                    replace.put(op.get(0), op.get(1));
                }
            }
        }
        IOUtils.printRevertIntList(ans.size(), ans);
    }

    public static void main(String[] args) throws IOException {
        Config.debug = Config.debug && args.length == 1 && args[0].equals("local");
        if (Config.debug) {
            printStream = System.out;
            Algorithm.Test.test();
        }
        if (Config.multiCase) {
            IntStream.range(1, 1 + scanner.nextInt()).forEach(Main::simpleCase);
        } else {
            simpleCase(1);
        }
        printStream.flush();
        System.in.close();
        System.out.close();
    }

    public static class Algorithm {
        /**
         * 寻找第一个满足check的数据<br/>
         * 要求： 如果i满足，则i+1一定满足<br/>
         * 可视化情况：????????????????????????????????<br/>
         * &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
         * &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;↑
         *
         * @param ql    左端点 include
         * @param qr    右端点 exclude
         * @param check 检验函数
         * @return 二分的值
         */
        public static long searchTheFirstOne(long ql, long qr, Function<Long, Boolean> check) {
            long l = ql, r = qr - 1;
            while (l < r) {
                long mid = (l + r) >> 1;
                if (check.apply(mid)) {
                    r = mid;
                } else {
                    l = mid + 1;
                }
            }
            if (Config.debug) {
                Assert.isTrue(ql < qr, "二分区间不存在");
                Assert.isTrue(l <= ql || random.longs(100, ql, l).noneMatch(check::apply), "二分的check函数不满足ans的前缀均为false");
                Assert.isTrue(qr <= l || random.longs(100, l, qr).allMatch(check::apply), "二分的check函数不满足ans的后缀均为true");
            }
            return l;
        }

        /**
         * 寻找最后一个满足check的数据<br/>
         * 要求： 如果i满足，则i-1一定满足<br/>
         * 可视化情况：????????????????????????????????<br/>
         * &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
         * &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;↑
         *
         * @param ql    左端点 include
         * @param qr    右端点 exclude
         * @param check 检验函数
         * @return 二分的值
         */
        public static long searchTheLastOne(long ql, long qr, Function<Long, Boolean> check) {
            long l = ql, r = qr - 1;
            while (l < r) {
                long mid = (l + r + 1) >> 1;
                if (check.apply(mid)) {
                    l = mid;
                } else {
                    r = mid - 1;
                }
            }
            if (Config.debug) {
                Assert.isTrue(ql < qr, "二分区间不存在");
                Assert.isTrue(random.longs(100, ql, l + 1).allMatch(check::apply), "二分的check函数不满足ans的前缀均为true");
                Assert.isTrue(random.longs(100, l + 1, qr).noneMatch(check::apply), "二分的check函数不满足ans的后缀均为false");
            }
            return l;
        }

        public static class Test {
            public static void searchTheFirstOneTest() {
                int[] list = {2, 3, 5, 7, 11, 13, 17, 19, 21, 29, 31, 37};
                // 寻找第一个大于x的数
                int[][] listq = {
                        {1, 2}, {2, 3}, {3, 5}, {4, 5},
                        {5, 7}, {6, 7}, {7, 11}, {12, 13},
                        {20, 21}, {21, 29}, {31, 37}, {36, 37},
                };
                Arrays.stream(listq).forEach(q -> {
                    int index = (int) searchTheFirstOne(0, list.length, i -> list[i.intValue()] > q[0]);
                    Assert.isTrue(list[index] == q[1], "测试失败");
                });
            }

            public static void searchTheLastOneTest() {
                int[] list = {2, 3, 5, 7, 11, 13, 17, 19, 21, 29, 31, 37};
                // 寻找第一个小于x的数
                int[][] qs = {
                        {3, 2}, {4, 3}, {5, 3}, {6, 5},
                        {9, 7}, {10, 7}, {11, 7}, {12, 11},
                        {20, 19}, {21, 19}, {31, 29}, {36, 31},
                };
                Arrays.stream(qs).forEach(q -> {
                    int index = (int) searchTheLastOne(0, list.length, i -> list[i.intValue()] < q[0]);
                    Assert.isTrue(list[index] == q[1], "测试失败");
                });
            }

            public static void test() {
                searchTheFirstOneTest();
                searchTheLastOneTest();
                printStream.println("Algorithm类： 测试成功");
            }
        }

    }


    public static class IOUtils {
        public static List<Integer> readIntList() {
            int size = scanner.nextInt();
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(scanner.nextInt());
            }
            return list;
        }

        public static List<Integer> readIntList(int size) {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(scanner.nextInt());
            }
            return list;
        }

        public static void printIntList(int size, List<Integer> list) {
            if (Config.debug) {
                Assert.isTrue(list.size() <= size, "list 输出错误");
            }
            for (int i = 0; i < size; i++) {
                printStream.print(list.get(i));
                printStream.print(i == size - 1 ? '\n' : ' ');
            }
        }

        public static void printRevertIntList(int size, List<Integer> list) {
            if (Config.debug) {
                Assert.isTrue(list.size() <= size, "list 输出错误");
            }
            for (int i = size - 1; i >= 0; i--) {
                printStream.print(list.get(i));
                printStream.print(i == 0 ? '\n' : ' ');
            }
        }

        public static void printRevertIntList(int size, DataStructs.IntArrayList list) {
            if (Config.debug) {
                Assert.isTrue(list.size() <= size, "list 输出错误");
            }
            for (int i = size - 1; i >= 0; i--) {
                printStream.print(list.get(i));
                printStream.print(i == 0 ? '\n' : ' ');
            }
        }

        public static void printList(List<Integer> list, String sizeSplit) {
            printStream.print(list.size());
            printStream.print(sizeSplit);
            String collect = list.stream().map(Object::toString).collect(Collectors.joining(" "));
            printStream.println(collect);
        }

        public static class FastScanner {
            BufferedReader br;
            StringTokenizer st;

            public FastScanner(InputStream source) {
                br = new BufferedReader(new InputStreamReader(source));
            }

            String next() {
                while (st == null || !st.hasMoreElements()) {
                    try {
                        st = new StringTokenizer(br.readLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return st.nextToken();
            }

            public int nextInt() {
                return Integer.parseInt(next());
            }

            public long nextLong() {
                return Long.parseLong(next());
            }

            public double nextDouble() {
                return Double.parseDouble(next());
            }

            public String nextLine() {
                String str = "";
                try {
                    str = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return str;
            }
        }
    }

    public static class Assert {
        public static void isTrue(Boolean check, String msg) {
            if (!check) {
                throw new RuntimeException("error: " + msg);
            }
        }
    }

    public static class DataStructs {
        public static final class ArrayMap<V> implements Map<Integer, V> {
            int minValue;
            int maxValue;
            int size;
            Object[] array;

            ArrayMap(int minValue, int maxValue) {
                if (Config.debug) {
                    Assert.isTrue(minValue == 0, "");
                }
                this.minValue = minValue;
                this.maxValue = maxValue;
                this.size = 0;
                this.array = new Object[this.maxValue - this.minValue + 1];
            }

            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return size == 0;
            }

            @Override
            public boolean containsKey(Object key) {
                if (Config.debug) {
                    Assert.isTrue(key instanceof Integer, "key 类型不对");
                }
                return array[(int) key] != null;
            }

            @Override
            public boolean containsValue(Object value) {
                if (Config.debug) {
                    throw new RuntimeException("未实现的方法");
                }
                return false;
            }

            @Override
            public V get(Object key) {
                if (Config.debug) {
                    Assert.isTrue(key instanceof Integer, "key 类型不对");
                }
                return (V) array[(int) key];
            }

            @Override
            public V put(Integer key, V value) {
                Object old = array[(int) key];
                array[(int) key] = value;
                return (V) old;
            }

            @Override
            public V remove(Object key) {
                if (Config.debug) {
                    Assert.isTrue(key instanceof Integer, "key 类型不对");
                }
                Object old = array[(int) key];
                array[(int) key] = null;
                return (V) old;
            }

            @Override
            public void putAll(Map<? extends Integer, ? extends V> m) {
                if (Config.debug) {
                    throw new RuntimeException("未实现的方法");
                }
            }

            @Override
            public void clear() {
                size = 0;
                this.array = new Object[this.maxValue - this.minValue + 1];
            }

            @Override
            public Set<Integer> keySet() {
                if (Config.debug) {
                    throw new RuntimeException("未实现的方法");
                }
                return null;
            }

            @Override
            public Collection<V> values() {
                if (Config.debug) {
                    throw new RuntimeException("未实现的方法");
                }
                return null;
            }

            @Override
            public Set<Entry<Integer, V>> entrySet() {
                if (Config.debug) {
                    throw new RuntimeException("未实现的方法");
                }
                return null;
            }
        }

        // 能快100ms
        public static final class IntArrayMap {
            int minValue;
            int maxValue;
            int notUseTag;
            int size;
            int[] array;

            public IntArrayMap(int minValue, int maxValue, int notUseTag) {
                this.minValue = minValue;
                this.maxValue = maxValue;
                this.notUseTag = notUseTag;
                this.size = 0;
                this.array = new int[this.maxValue - this.minValue + 1];
                Arrays.fill(array, notUseTag);
            }


            public int getOrDefault(int key, int defaultValue) {
                return array[key] == notUseTag ? defaultValue : array[key];
            }

            public boolean containsKey(int key) {
                return array[key] != notUseTag;
            }

            public int get(int key) {
                return array[key];
            }

            public void put(int key, int value) {
                array[key] = value;
            }
        }

        // 能快100ms
        public static final class IntArrayList {

            int size;
            int[] values;

            IntArrayList() {
                size = 0;
                values = new int[2];
            }

            public void add(Integer value) {
                if (size == values.length) {
                    int[] old = values;
                    values = new int[2 * size];
                    System.arraycopy(old, 0, values, 0, size);
                }
                values[size++] = value;
            }

            public int size() {
                return size;
            }

            public int get(int i) {
                return values[i];
            }
        }
    }
}