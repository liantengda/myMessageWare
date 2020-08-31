package club.throwable.cm.support.binding;

import lombok.Builder;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 10:43
 */
@Builder
public class DefaultDestination implements Destination {

    /**
     * 交换类型
     */
    private ExchangeType exchangeType;

    /**
     * 队列名
     */
    private String queueName;

    /**
     * 交换器名
     */
    private String exchangeName;

    /**
     * 路由键
     */
    private String routingKey;

    @Override
    public ExchangeType exchangeType() {
        return exchangeType;
    }

    @Override
    public String queueName() {
        return queueName;
    }

    @Override
    public String exchangeName() {
        return exchangeName;
    }

    @Override
    public String routingKey() {
        return routingKey;
    }
}
