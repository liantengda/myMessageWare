package club.throwable.cm.support.binding;

/**
 * 终端
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 10:02
 */
public interface Destination {

    /**
     * 交换机类型
     * @return
     */
    ExchangeType exchangeType();

    /**
     * 队列名称
     * @return
     */
    String queueName();

    /**
     * 交换机名称
     * @return
     */
    String exchangeName();

    /**
     * 路由键
     * @return
     */
    String routingKey();
}
