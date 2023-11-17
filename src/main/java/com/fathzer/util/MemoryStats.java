package com.fathzer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryStats {
	public static boolean on = false;
	private static final MemoryStats ME = new MemoryStats();
	private Map<String, AtomicLong> map = new HashMap<>();
	
	public static void add(Object obj) {
		if (on) {
			ME.map.computeIfAbsent(obj.getClass().toString(), k->new AtomicLong()).incrementAndGet();
		}
	}
	public static void increment(String str) {
		if (on) {
			ME.map.computeIfAbsent(str, k->new AtomicLong()).incrementAndGet();
		}
	}
	
	public static void clear() {
		ME.map.clear();
	}
	
	public static void show() {
		System.out.println(ME.map);
	}
}
