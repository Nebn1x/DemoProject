package org.example.service;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.RequiredArgsConstructor;
import org.example.config.RateLimitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Rate limiting через Bucket4j + Redis.
 * Підключення до Redis - ЛІНИВЕ (при першому виклику), а не в @PostConstruct.
 * Підтримує TLS (для AWS ElastiCache) через прапорець spring.data.redis.ssl.enabled.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitProperties props;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    // TLS: на AWS ElastiCache = true (rediss://), локально = false (redis://)
    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSsl;

    private volatile ProxyManager<String> proxyManager;

    public record RateLimitResult(boolean allowed, long retryAfterSeconds, long remaining) {}

    public RateLimitResult checkLimits(String userHash, String endpointKey) {
        if (!props.isEnabled()) {
            return new RateLimitResult(true, 0, Long.MAX_VALUE);
        }

        ProxyManager<String> pm = getProxyManager();

        var userBucket = pm.builder()
                .build("rl:user:" + userHash, bucketConfig(props.getPerUserPerMinute()));
        ConsumptionProbe userProbe = userBucket.tryConsumeAndReturnRemaining(1);
        if (!userProbe.isConsumed()) {
            return new RateLimitResult(false,
                    nanosToSeconds(userProbe.getNanosToWaitForRefill()), 0);
        }

        var endpointBucket = pm.builder()
                .build("rl:ep:" + endpointKey, bucketConfig(props.getPerEndpointPerMinute()));
        ConsumptionProbe epProbe = endpointBucket.tryConsumeAndReturnRemaining(1);
        if (!epProbe.isConsumed()) {
            return new RateLimitResult(false,
                    nanosToSeconds(epProbe.getNanosToWaitForRefill()), 0);
        }

        return new RateLimitResult(true, 0, epProbe.getRemainingTokens());
    }

    private ProxyManager<String> getProxyManager() {
        if (proxyManager == null) {
            synchronized (this) {
                if (proxyManager == null) {
                    proxyManager = createProxyManager();
                }
            }
        }
        return proxyManager;
    }

    @SuppressWarnings("deprecation")
    private ProxyManager<String> createProxyManager() {
        // rediss:// для TLS (AWS ElastiCache), redis:// для звичайного (локально)
        String scheme = redisSsl ? "rediss://" : "redis://";
        RedisClient redisClient = RedisClient.create(
                scheme + redisHost + ":" + redisPort);
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(codec);

        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy
                                .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(2)))
                .build();
    }

    private Supplier<BucketConfiguration> bucketConfig(int perMinute) {
        return () -> BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(perMinute)
                        .refillGreedy(perMinute, Duration.ofMinutes(1)))
                .build();
    }

    private long nanosToSeconds(long nanos) {
        long sec = nanos / 1_000_000_000L;
        return Math.max(sec, 1);
    }
}