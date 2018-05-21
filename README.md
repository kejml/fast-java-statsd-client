# Fast Java Statsd Client

[![Build Status](https://travis-ci.org/energyIt/fast-java-statsd-client.svg?branch=master)](https://travis-ci.org/energyIt/fast-java-statsd-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tech.energyit%3Afast-java-statsd-client&metric=coverage)](https://sonarcloud.io/dashboard?id=tech.energyit%3Afast-java-statsd-client)

A statsd client library implemented in Java.  Allows Java applications to easily communicate via statsd and reduce the the allocaiton or performance overhead to ZERO.

This version was inspired by [java-dogstatsd-client](https://github.com/indeedeng/java-dogstatsd-client) but our major requirement was to reduce allocations and the performance impact on the running jvm.

## Allocation Rate Comparison
```
Benchmark                                                                                Mode  Cnt     Score     Error   Units
StatsdClientBenchmark.countWithTagViaDatadogClient                                       avgt    5     1.641 ±   0.452   us/op
StatsdClientBenchmark.countWithTagViaDatadogClient:·gc.alloc.rate                        avgt    5  1257.084 ± 730.464  MB/sec
StatsdClientBenchmark.countWithTagViaDatadogClient:·gc.alloc.rate.norm                   avgt    5  2288.080 ± 551.177    B/op
StatsdClientBenchmark.countWithTagViaDatadogClient:·gc.count                             avgt    5   173.000            counts
StatsdClientBenchmark.countWithTagViaDatadogClient:·gc.time                              avgt    5   354.000                ms
StatsdClientBenchmark.countWithTagViaFastClient                                          avgt    5     3.050 ±   0.435   us/op
StatsdClientBenchmark.countWithTagViaFastClient:·gc.alloc.rate                           avgt    5     7.029 ±   1.082  MB/sec
StatsdClientBenchmark.countWithTagViaFastClient:·gc.alloc.rate.norm                      avgt    5    24.000 ±   0.002    B/op
StatsdClientBenchmark.countWithTagViaFastClient:·gc.count                                avgt    5     7.000            counts
StatsdClientBenchmark.countWithTagViaFastClient:·gc.time                                 avgt    5    13.000                ms
StatsdClientBenchmark.countWithTwoTagsViaDatadogClient                                   avgt    5     1.641 ±   0.519   us/op
StatsdClientBenchmark.countWithTwoTagsViaDatadogClient:·gc.alloc.rate                    avgt    5  1368.544 ± 688.381  MB/sec
StatsdClientBenchmark.countWithTwoTagsViaDatadogClient:·gc.count                         avgt    5   202.000            counts
StatsdClientBenchmark.countWithTwoTagsViaDatadogClient:·gc.time                          avgt    5   387.000                ms
StatsdClientBenchmark.countWithTwoTagsViaFastClient                                      avgt    5     3.184 ±   0.689   us/op
StatsdClientBenchmark.countWithTwoTagsViaFastClient:·gc.alloc.rate                       avgt    5     6.747 ±   1.876  MB/sec
StatsdClientBenchmark.countWithTwoTagsViaFastClient:·gc.alloc.rate.norm                  avgt    5    24.001 ±   0.002    B/op
StatsdClientBenchmark.countWithTwoTagsViaFastClient:·gc.count                            avgt    5     6.000            counts
StatsdClientBenchmark.countWithTwoTagsViaFastClient:·gc.time                             avgt    5    12.000                m
```
See `StatsdClientBenchmark`

NOTE: if no statsd server is listening on the ports, DatagramChannels generate IOExceptions which is expensive. If the clients uses a ErrorHandler that also generates stacktrace, the impact is big.