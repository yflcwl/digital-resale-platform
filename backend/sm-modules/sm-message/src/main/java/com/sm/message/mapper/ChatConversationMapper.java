package com.sm.message.mapper;

import com.sm.common.mybatis.core.mapper.BaseMapperPlus;
import com.sm.message.domain.ChatConversation;
import com.sm.message.domain.vo.ConversationVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper接口
 *
 * @author sm
 */
public interface ChatConversationMapper extends BaseMapperPlus<ChatConversation, ChatConversation> {

    /**
     * 查询当前用户的会话列表(含对方昵称/头像、商品信息、未读数)
     *
     * @param userId 当前用户ID
     */
    List<ConversationVo> selectConversationList(@Param("userId") Long userId);

    /**
     * 查询商品卖家ID
     */
    Long selectProductSellerId(@Param("productId") Long productId);
}
