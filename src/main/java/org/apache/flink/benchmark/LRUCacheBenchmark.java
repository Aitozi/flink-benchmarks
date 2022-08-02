package org.apache.flink.benchmark;

import org.apache.flink.state.benchmark.ListStateBenchmark;
import org.apache.flink.table.shaded.com.jayway.jsonpath.JsonPath;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.Cache;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.CacheProvider;
import org.apache.flink.table.shaded.com.jayway.jsonpath.spi.cache.LRUCache;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
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
public class LRUCacheBenchmark extends BenchmarkBase {

	final ThreadLocalRandom random = ThreadLocalRandom.current();
	Cache cache;
	JsonPath jsonPath;
	List<String> randomKey = new ArrayList<>();
	int cacheSize = 400;
	Cache readCache = new LRUCache(cacheSize);

	public static void main(String[] args) throws Exception {
		Options opt =
				new OptionsBuilder()
						.verbosity(VerboseMode.NORMAL)
						.include(".*" + LRUCacheBenchmark.class.getCanonicalName() + ".*")
						.build();

		new Runner(opt).run();
	}

	@Setup
	public void setUp() {
		cache = new LRUCache(cacheSize);
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

}
