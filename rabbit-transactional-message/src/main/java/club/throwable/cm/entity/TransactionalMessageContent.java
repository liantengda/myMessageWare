package club.throwable.cm.entity;

import lombok.Data;

/**
 * 事务消息内容表
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 11:23
 */
@Data
public class TransactionalMessageContent {

    private Long id;
    /**
     * 事务消息记录id
     */
    private Long messageId;
    /**
     * 消息内容
     */
    private String content;
}
