package club.throwable.cm.service;

import club.throwable.cm.dao.TransactionalMessageContentDao;
import club.throwable.cm.dao.TransactionalMessageDao;
import club.throwable.cm.entity.TransactionalMessage;
import club.throwable.cm.entity.TransactionalMessageContent;
import club.throwable.cm.support.message.TxMessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/5 9:52
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionalMessageManagementService {

    private final TransactionalMessageDao messageDao;
    private final TransactionalMessageContentDao contentDao;
    private final RabbitTemplate rabbitTemplate;

    private static final Long END = LocalDateTime.of(2999, 1, 1, 0, 0, 0).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    private static final long DEFAULT_INIT_BACKOFF = 10L;
    private static final int DEFAULT_BACKOFF_FACTOR = 2;
    private static final int DEFAULT_MAX_RETRY_TIMES = 5;
    private static final int LIMIT = 100;

    public void saveTransactionalMessageRecord(TransactionalMessage record, String content) {
        record.setMessageStatus(TxMessageStatus.PENDING.getStatus());
        record.setNextScheduleTime(calculateNextScheduleTimeByTimeStamp(System.currentTimeMillis(), DEFAULT_INIT_BACKOFF,
                DEFAULT_BACKOFF_FACTOR, 0));
        record.setCurrentRetryTimes(0);
        record.setInitBackoff(DEFAULT_INIT_BACKOFF);
        record.setBackoffFactor(DEFAULT_BACKOFF_FACTOR);
        record.setMaxRetryTimes(DEFAULT_MAX_RETRY_TIMES);
        messageDao.insertSelective(record);
        TransactionalMessageContent messageContent = new TransactionalMessageContent();
        messageContent.setContent(content);
        messageContent.setMessageId(record.getId());
        contentDao.insert(messageContent);
    }

    public void sendMessageSync(TransactionalMessage record, String content) {
        try {
            rabbitTemplate.convertAndSend(record.getExchangeName(), record.getRoutingKey(), content);
            if (log.isDebugEnabled()) {
                log.debug("发送消息成功,目标队列:{},消息内容:{}", record.getQueueName(), content);
            }
            // 标记成功---------如果发送成功，则新增成功
            markSuccess(record);
        } catch (Exception e) {
            // 标记失败
            markFail(record, e);
        }
    }

    /**
     * 如果消息正常发送，则标记该条记录为成功
     * @param record
     */
    private void markSuccess(TransactionalMessage record) {
        // 标记下一次执行时间为最大值
        record.setNextScheduleTime(END);
        record.setCurrentRetryTimes(record.getCurrentRetryTimes().compareTo(record.getMaxRetryTimes()) >= 0 ?
                record.getMaxRetryTimes() : record.getCurrentRetryTimes() + 1);
        record.setMessageStatus(TxMessageStatus.SUCCESS.getStatus());
        record.setEditTime(System.currentTimeMillis());
        messageDao.updateStatusSelective(record);
    }

    /**
     * 如果消息发送失败，则标记该条记录为失败
     * @param record
     * @param e
     */
    private void markFail(TransactionalMessage record, Exception e) {
        log.error("发送消息失败,目标队列:{}", record.getQueueName(), e);
        record.setCurrentRetryTimes(record.getCurrentRetryTimes().compareTo(record.getMaxRetryTimes()) >= 0 ?
                record.getMaxRetryTimes() : record.getCurrentRetryTimes() + 1);
        // 计算下一次的执行时间
        Long nextScheduleTime = calculateNextScheduleTimeByTimeStamp(
                record.getNextScheduleTime(),
                record.getInitBackoff(),
                record.getBackoffFactor(),
                record.getCurrentRetryTimes()
        );
        record.setNextScheduleTime(nextScheduleTime);
        record.setMessageStatus(TxMessageStatus.FAIL.getStatus());
        record.setEditTime(System.currentTimeMillis());
        messageDao.updateStatusSelective(record);
    }



    /**
     * 计算下一次执行时间
     *
     * @param base          基础时间
     * @param initBackoff   退避基准值
     * @param backoffFactor 退避指数
     * @param round         轮数
     * @return LocalDateTime
     */
    private Long calculateNextScheduleTimeByTimeStamp(Long base,
                                                    long initBackoff,
                                                    long backoffFactor,
                                                    long round) {
        double delta = initBackoff * Math.pow(backoffFactor, round);
        return  base+Math.round(delta*1000);
    }

    /**
     * 计算下一次执行时间
     *
     * @param base          基础时间
     * @param initBackoff   退避基准值
     * @param backoffFactor 退避指数
     * @param round         轮数
     * @return LocalDateTime
     */
    private LocalDateTime calculateNextScheduleTime(LocalDateTime base,
                                                    long initBackoff,
                                                    long backoffFactor,
                                                    long round) {
        double delta = initBackoff * Math.pow(backoffFactor, round);
        return  base.plusSeconds((long) delta);
    }




    /**
     * 推送补偿 - 里面的参数应该根据实际场景定制
     */
    public void processPendingCompensationRecords() {
        // 这里预防把刚保存的消息也推送了
        LocalDateTime max = LocalDateTime.now().plusSeconds(-DEFAULT_INIT_BACKOFF);
        LocalDateTime min = max.plusHours(-1);
        Map<Long, TransactionalMessage> collect = messageDao.queryPendingCompensationRecords(min, max, LIMIT)
                .stream()
                .collect(Collectors.toMap(TransactionalMessage::getId, x -> x));
        if (!collect.isEmpty()) {
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            collect.keySet().forEach(x -> joiner.add(x.toString()));
            contentDao.queryByMessageIds(joiner.toString())
                    .forEach(item -> {
                        TransactionalMessage message = collect.get(item.getMessageId());
                        //同步发送补偿消息
                        sendMessageSync(message, item.getContent());
                    });
        }
    }
}
