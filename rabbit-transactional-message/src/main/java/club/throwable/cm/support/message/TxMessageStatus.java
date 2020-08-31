package club.throwable.cm.support.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息状态
 * @author throwable
 * @version v1.0
 * @description
 * @since 2020/2/5 9:47
 */
@Getter
@RequiredArgsConstructor
public enum TxMessageStatus {

    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 待处理
     */
    PENDING(0),

    /**
     * 处理失败
     */
    FAIL(-1),

    ;

    private final Integer status;
}
