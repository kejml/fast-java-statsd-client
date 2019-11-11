package tech.energyit.statsd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tech.energyit.statsd.utils.LoggingSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class FastStatsDClientTest {

    private LoggingSender sender;
    private FastStatsDClient statsDClient;

    @Before
    public void setUp() {
        sender = new LoggingSender();
        statsDClient = new FastStatsDClient("my.prefix", sender);
    }

    @After
    public void teardown() {
        statsDClient.clear();
    }

    @Test
    public void countShouldBeSendCorrectly() {
        statsDClient.count("my.metric".getBytes(), 10);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c");
    }

    @Test
    public void countWithRateShouldBeSendCorrectly() {
        statsDClient.count("my.metric".getBytes(), 10, 0.1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|@0.1");
    }

    @Test
    public void negativeRateShouldMakeTheMessageIgnored() {
        statsDClient.count("my.metric".getBytes(), 10, -5);
        assertThat(sender.getMessages()).isEmpty();
    }

    @Test
    public void zeroRateShouldMakeTheMessageIgnored() {
        statsDClient.count("my.metric".getBytes(), 10, 0);
        assertThat(sender.getMessages()).isEmpty();
    }

    @Test
    public void rateGreaterThanOneShouldMakeTheMessageIgnored() {
        statsDClient.count("my.metric".getBytes(), 10, 5);
        assertThat(sender.getMessages()).isEmpty();
    }

    @Test
    public void countWithOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.count("my.metric".getBytes(), 10, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|#" + tag1);
    }

    @Test
    public void countWithRateAndOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.count("my.metric".getBytes(), 10, 0.1, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|@0.1|#" + tag1);
    }

    @Test
    public void gaugeShouldBeSendCorrectly() {
        statsDClient.gauge("my.metric".getBytes(), -1234567890123456789L);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:-1234567890123456789|g");
    }

    @Test
    public void gaugeWithOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.gauge("my.metric".getBytes(), 1234567890123456L, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:1234567890123456|g|#" + tag1);
    }

    @Test
    public void gaugeWithTwoTagsShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        Tag tag2 = new TagImpl("tag2".getBytes(), "val2".getBytes());
        statsDClient.gauge("my.metric".getBytes(), 123456789, tag1, tag2);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:123456789|g|#" + tag1 + ',' + tag2);
    }

    @Test
    public void emptyTagsMustWorkOk() {
        statsDClient.gauge("my.metric".getBytes(), 123456789, new Tag[0]);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:123456789|g");
    }

    @Test
    public void timeShouldBeSendCorrectly() {
        statsDClient.time("my.metric".getBytes(), -1234567890);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:-1234567890|ms");
    }

    @Test
    public void timerWithOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.time("my.metric".getBytes(), 1234567890, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:1234567890|ms|#" + tag1);
    }

    @Test
    public void nullPrefixShouldBeSendCorrectly() {
        statsDClient = new FastStatsDClient(sender);
        statsDClient.count("my.metric".getBytes(), 10);
        assertThat(sender.getMessages()).containsExactly("my.metric:10|c");
    }

    @Test
    public void emptyPrefixShouldBeSendCorrectly() {
        statsDClient = new FastStatsDClient("", sender);
        statsDClient.count("my.metric".getBytes(), 10);
        assertThat(sender.getMessages()).containsExactly("my.metric:10|c");
    }

    @Test
    public void bigMessageMustBeSent() {
        byte[] bigMessage = new byte[FastStatsDClient.MAX_BUFFER_LENGTH - 15];
        statsDClient.count(bigMessage, 10);
        assertThat(sender.getMessages()).hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void messageBiggerThenMaxBufferLengthMustFail() {
        byte[] hugeMessage = new byte[FastStatsDClient.MAX_BUFFER_LENGTH + 1];
        statsDClient.count(hugeMessage, 10);
    }

    @Test
    public void clientMustBeUsableFromMultipleThreads() {
        final AtomicInteger seq = new AtomicInteger();
        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                int value = seq.incrementAndGet();
                statsDClient.time("my.metric".getBytes(), value);
                assertThat(sender.getMessages()).contains("my.prefix.my.metric:" + value + "|ms");
            });

        }


    }
}
