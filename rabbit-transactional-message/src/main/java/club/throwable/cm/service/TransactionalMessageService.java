package club.throwable.cm.service;

import club.throwable.cm.support.binding.Destination;
import club.throwable.cm.support.message.TxMessage;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 9:59
 */
public interface TransactionalMessageService {
    /**
     * 发送事务消息
     *
     * 该消息在事务成功后发送到rabbit服务器中
     * @param destination
     * @param message
     */
    void sendTransactionalMessage(Destination destination, TxMessage message);
}
