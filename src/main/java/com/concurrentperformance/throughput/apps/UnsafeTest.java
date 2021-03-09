package com.concurrentperformance.throughput.apps;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * TODO comments???
 *
 * @author Steve Lake
 */
public class UnsafeTest {

//    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
//                Unsafe unsafe = getTheUnsafe();
//
//
//        long address = unsafe.allocateMemory(1000000);
//
//        unsafe.putDouble(address, 100);
//        double aDouble = unsafe.getDouble(address);
//
//        System.out.println(address);
//        System.out.println(aDouble);
//
//    }

    public static void main(String... args) {
        ByteBuffer bb1 = ByteBuffer.allocateDirect(256 * 1024 * 1024).order(ByteOrder.nativeOrder());
        ByteBuffer bb2 = ByteBuffer.allocateDirect(256 * 1024 * 1024).order(ByteOrder.nativeOrder());

        Unsafe unsafe = getTheUnsafe();

        long addr1 = getAddress(bb1);
        long addr2 = getAddress(bb2);

        for (int i = 0, len = Math.min(bb1.capacity(), bb2.capacity()); i < len; i += 4) {
            unsafe.putInt(addr1 + i, i);
        }

        for (int i = 0; i < 100; i++)
            runTest2(bb1, bb2);
    }

    private static void runTest1(ByteBuffer bb1, ByteBuffer bb2) {
        bb1.clear();
        bb2.clear();
        long start = System.nanoTime();
        int count = 0;
        while (bb2.remaining() > 0)
            bb2.putInt(bb1.getInt());
        long time = System.nanoTime() - start;
        int operations = bb1.capacity() / 4 * 2;
        System.out.printf("Each putInt/getInt took an average of %.1f ns%n", (double) time / operations);
    }

    private static void runTest2(ByteBuffer bb1, ByteBuffer bb2) {
        Unsafe unsafe = getTheUnsafe();
        long start = System.nanoTime();

        long addr1 = getAddress(bb1);
        long addr2 = getAddress(bb2);
        for (int i = 0, len = Math.min(bb1.capacity(), bb2.capacity()); i < len; i += 4) {
            unsafe.putInt(addr1 + i, unsafe.getInt(addr2 + i));
        }
        long time = System.nanoTime() - start;
        int operations = bb1.capacity() / 4 * 2;
        System.out.printf("Each putInt/getInt took an average of %.1f ns%n", (double) time / operations);
    }

    public static Unsafe getTheUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    public static long getAddress(ByteBuffer buffer) {
        return getTheUnsafe().getLong(buffer, fieldOffset(field(Buffer.class, "address")));
    }

    private static long fieldOffset(Field field) {
        return field == null ? -1 : getTheUnsafe().objectFieldOffset(field);
    }

    private static Field field(Class<?> clazz, String fieldName) {
        Field field;

        try {
            field = clazz.getDeclaredField(fieldName);

            field.setAccessible(true);
        } catch (Throwable t) {
            // Failed to access the fields.
            field = null;
        }

        return field;
    }

}
