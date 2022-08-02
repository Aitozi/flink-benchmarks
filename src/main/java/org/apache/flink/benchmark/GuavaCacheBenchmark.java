package org.apache.flink.benchmark;


import org.apache.flink.state.benchmark.ListStateBenchmark;
import org.apache.flink.table.shaded.com.jayway.jsonpath.JsonPath;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.Cache;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.CacheProvider;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.LRUCache;

import org.apache.flink.shaded.guava30.com.google.common.cache.CacheBuilder;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
public class GuavaCacheBenchmark extends BenchmarkBase {

	final ThreadLocalRandom random = ThreadLocalRandom.current();
	Cache cache;
	Cache readCache = new JsonPathCache();
	JsonPath jsonPath;
	List<String> randomKey = new ArrayList<>();
	int cacheSize = 400;

	public static void main(String[] args) throws Exception {
		Options opt =
				new OptionsBuilder()
						.verbosity(VerboseMode.NORMAL)
						.include(".*" + GuavaCacheBenchmark.class.getCanonicalName() + ".*")
						.build();

		new Runner(opt).run();
	}

	@Setup
	public void setUp() {
		cache = new JsonPathCache();
		this.jsonPath = JsonPath.compile("$.store.book[1]");
		for (int i = 0; i < cacheSize; i++) {
			String key = random.nextLong() + "";
			randomKey.add(key);
			readCache.put(key, jsonPath);
		}
	}

	@Benchmark
	@Threads(4)
	public void get() {
		readCache.get(randomKey.get(random.nextInt(cacheSize)));
	}


	@Benchmark
	@Threads(4)
	public void put() {
		cache.put(randomKey.get(random.nextInt(cacheSize)), jsonPath);
	}

	public class JsonPathCache implements Cache {

		private static final long DEFAULT_CACHE_MAXIMUM_SIZE = 400;

		private final org.apache.flink.shaded.guava30.com.google.common.cache.Cache<String, JsonPath>
				jsonPathCache =
				CacheBuilder.newBuilder().maximumSize(DEFAULT_CACHE_MAXIMUM_SIZE).build();

		@Override
		public JsonPath get(String s) {
			return jsonPathCache.getIfPresent(s);
		}

		@Override
		public void put(String s, JsonPath jsonPath) {
			jsonPathCache.put(s, jsonPath);
		}
	}
}
