package club.throwable.redisson;

import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 悲观锁强依赖数据库的可用性，数据库是单点，一旦挂掉，导致业务系统不可用
 * 一旦悲观锁解锁操作失败，就会导致锁记录一直在数据库中，其他线程无法再获得锁
 * 乐观锁适合读多写少的场景。
 * @author throwable
 * @version v1.0
 * @description
 * @since 2019/12/27 10:31
 */
public class RedissonMain {

    public static void main(String[] args) throws Exception {
        RedissonClient client = Redisson.create();
        RSemaphore semaphore = client.getSemaphore("ds:semaphore");
        semaphore.delete();
        semaphore.trySetPermits(5);
        List<Thread> threads = new ArrayList<>(16);
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println(String.format("线程-%s获取到信号量......", Thread.currentThread().getName()));
                    semaphore.release();
                    System.out.println(String.format("线程-%s释放信号量......", Thread.currentThread().getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.setName("线程" + i);
            thread.setDaemon(true);
            threads.add(thread);
        }
        threads.forEach(t -> t.start());
        Thread.sleep(Integer.MAX_VALUE);
    }
}
