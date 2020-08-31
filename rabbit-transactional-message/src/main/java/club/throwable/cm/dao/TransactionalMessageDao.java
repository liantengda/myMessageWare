package club.throwable.cm.dao;

import club.throwable.cm.entity.TransactionalMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事务消息类
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/3 11:20
 */
public interface TransactionalMessageDao {

    void insertSelective(TransactionalMessage record);

    void updateStatusSelective(TransactionalMessage record);

    /**
     * 查询推送补偿记录
     * @param minScheduleTime
     * @param maxScheduleTime
     * @param limit
     * @return
     */
    List<TransactionalMessage> queryPendingCompensationRecords(LocalDateTime minScheduleTime,
                                                               LocalDateTime maxScheduleTime,
                                                               int limit);
}
