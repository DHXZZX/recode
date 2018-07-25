package dhxz.session;

import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ChainTest {

    @Test
    public void test1() {

    }

    @Test
    public void session() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();

        Thread.sleep(1000L);
        watch.stop();

        System.out.println(watch.getTime());
    }

    @Test
    public void testStopwatch() throws InterruptedException {
        Stopwatch watch = Stopwatch.createStarted();
        Thread.sleep(1000L);
        long time1 = watch.elapsed(TimeUnit.MILLISECONDS);
        Thread.sleep(200L);
        long time2 = watch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println(time1);
        System.out.println(time2);

    }
}
