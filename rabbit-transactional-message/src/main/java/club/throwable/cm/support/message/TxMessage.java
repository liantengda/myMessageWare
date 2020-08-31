package club.throwable.cm.support.message;

/**
 * 事务消息
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 10:50
 */
public interface TxMessage {
    /**
     * 业务模块
     * @return
     */
    String businessModule();

    /**
     * 业务键
     * @return
     */
    String businessKey();

    /**
     * 业务内容
     * @return
     */
    String content();
}
