package br.net.brjdevs.steven.bran.core.snowflakes;

public class SnowflakeGenerator {
	
	private static final SnowflakeGenerator DEFAULT_GENERATOR = new SnowflakeGenerator(1, 1);
	private long workerId;
	private long datacenterId;
	private long sequence = 0L;
	private long twepoch = 1288834974657L;
	private long workerIdBits = 5L;
	private long datacenterIdBits = 5L;
	private long maxWorkerId = ~(-1L << workerIdBits);
	private long maxDatacenterId = ~(-1L << datacenterIdBits);
	private long sequenceBits = 12L;
	private long workerIdShift = sequenceBits;
	private long datacenterIdShift = sequenceBits + workerIdBits;
	private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
	private long sequenceMask = ~(-1L << sequenceBits);
	private long lastTimestamp = -1L;
	
	public SnowflakeGenerator(long workerId, long datacenterId) {
		// sanity check for workerId
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}
	
	public static SnowflakeGenerator getDefaultGenerator() {
		return DEFAULT_GENERATOR;
	}
	
	public long nextId() {
		long timestamp = timeGen();
		
		if (timestamp < lastTimestamp) {
			throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}
		
		synchronized (this) {
			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & sequenceMask;
				if (sequence == 0) {
					timestamp = tilNextMillis(lastTimestamp);
				}
			} else {
				sequence = 0L;
			}
			
			lastTimestamp = timestamp;
			
			return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
		}
	}
	
	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}
	
	public long getCreationTime(long snowflake) {
		return (snowflake >> 22) + 1288834974657L;
	}
	
	private long timeGen() {
		return System.currentTimeMillis();
	}
}
