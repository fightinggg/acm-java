import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static class Config {
        public static Boolean multiCase = true;
        public static Boolean debug = true; // 这里设为true不一定有效，还需要把进程的参数设为 local
    }


    public static Scanner scanner = new Scanner(new BufferedInputStream(System.in));
    public static PrintStream printStream = new PrintStream(new BufferedOutputStream(System.out));
    public static Random random = new Random();

    public static void simpleCase(int caseNumber) {
        int a = scanner.nextInt();
        String s = scanner.next();
        for (int len = 2; len <= 7; len++) {
            boolean ok = false;
            int[] ct = new int[3];
            for (int i = 0; i < len - 1 && i < s.length(); i++) {
                ct[s.charAt(i) - 'a']++;
            }
            for (int i = len - 1; i < s.length(); i++) {
                ct[s.charAt(i) - 'a']++;
                if (ct[0] > ct[1] && ct[0] > ct[2]) {
                    ok = true;
                    break;
                }
                ct[s.charAt(i - len + 1) - 'a']--;
            }
            if (ok) {
                printStream.println(len);
                return;
            }
        }
        printStream.println(-1);
    }

    public static void main(String[] args) {
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

    public static class IOFormat {
        public static void printList(List<Integer> list, String sizeSplit) {
            printStream.print(list.size());
            printStream.print(sizeSplit);
            String collect = list.stream().map(Object::toString).collect(Collectors.joining(" "));
            printStream.println(collect);
        }

    }

    public static class Assert {
        public static void isTrue(Boolean check, String msg) {
            if (!check) {
                throw new RuntimeException("error: " + msg);
            }
        }
    }
}