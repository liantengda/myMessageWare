package club.throwable.cm.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 11:23
 */
@Data
public class TransactionalMessage {

    private Long id;
    private Long createTime;
    private Long editTime;
    private String creator;
    private String editor;
    private Integer deleted;
    private Integer currentRetryTimes;
    private Integer maxRetryTimes;
    private String queueName;
    private String exchangeName;
    private String exchangeType;
    private String routingKey;
    private String businessModule;
    private String businessKey;
    private Long nextScheduleTime;
    private Integer messageStatus;
    private Long initBackoff;
    private Integer backoffFactor;
}
