package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.MapstructUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.trade.constant.OrderStatus;
import com.sm.trade.constant.ProductStatus;
import com.sm.trade.domain.Order;
import com.sm.trade.domain.OrderReview;
import com.sm.trade.domain.OrderStatusLog;
import com.sm.trade.domain.PaymentRecord;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.ProductImage;
import com.sm.trade.domain.UserAddress;
import com.sm.trade.domain.UserProfile;
import com.sm.trade.domain.bo.OrderBo;
import com.sm.trade.domain.vo.OrderVo;
import com.sm.trade.domain.vo.SellerInfoVo;
import com.sm.trade.mapper.OrderMapper;
import com.sm.trade.mapper.OrderReviewMapper;
import com.sm.trade.mapper.OrderStatusLogMapper;
import com.sm.trade.mapper.PaymentRecordMapper;
import com.sm.trade.mapper.ProductImageMapper;
import com.sm.trade.mapper.ProductMapper;
import com.sm.trade.mapper.UserAddressMapper;
import com.sm.trade.mapper.UserProfileMapper;
import com.sm.message.service.INotificationService;
import com.sm.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单Service业务层处理
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    /** 订单延迟消息 Topic */
    public static final String TOPIC_ORDER_DELAY = "sm_order_delay";
    /** 30 分钟延迟等级(RocketMQ 默认等级表) */
    private static final int DELAY_LEVEL_30M = 18;
    /** 订单超时时长(毫秒) */
    private static final long ORDER_TIMEOUT_MS = 30 * 60 * 1000L;

    private final OrderMapper baseMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final PaymentRecordMapper paymentMapper;
    private final OrderReviewMapper reviewMapper;
    private final INotificationService notificationService;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final UserAddressMapper addressMapper;
    private final UserProfileMapper userProfileMapper;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderBo bo) {
        Long buyerId = LoginHelper.getUserId();
        Product product = productMapper.selectById(bo.getProductId());
        if (product == null || !ProductStatus.ON_SALE.equals(product.getStatus())) {
            throw new ServiceException("商品不存在或已下架");
        }
        if (buyerId.equals(product.getSellerId())) {
            throw new ServiceException("不能购买自己发布的商品");
        }
        UserAddress address = addressMapper.selectById(bo.getAddressId());
        if (address == null || !buyerId.equals(address.getUserId())) {
            throw new ServiceException("收货地址不存在");
        }
        // 商品状态 CAS 锁定,防止一物二卖
        LambdaUpdateWrapper<Product> uw = Wrappers.lambdaUpdate();
        uw.set(Product::getStatus, ProductStatus.LOCKED);
        uw.eq(Product::getId, product.getId());
        uw.eq(Product::getStatus, ProductStatus.ON_SALE);
        if (productMapper.update(null, uw) <= 0) {
            throw new ServiceException("商品已被他人下单,请刷新后重试");
        }

        Date now = new Date();
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setProductSnapshot(buildProductSnapshot(product));
        order.setPrice(product.getPrice());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setAddressSnapshot(buildAddressSnapshot(address));
        order.setTimeoutTime(new Date(now.getTime() + ORDER_TIMEOUT_MS));
        if (baseMapper.insert(order) <= 0) {
            throw new ServiceException("创建订单失败");
        }
        recordStatus(order.getId(), null, OrderStatus.PENDING_PAY, "BUYER", buyerId, "买家下单");
        sendDelayMessage(order);
        return order.getId();
    }

    private String buildProductSnapshot(Product product) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", product.getTitle());
        snapshot.put("description", product.getDescription());
        snapshot.put("price", product.getPrice());
        ProductImage cover = productImageMapper.selectOne(Wrappers.<ProductImage>lambdaQuery()
            .eq(ProductImage::getProductId, product.getId())
            .orderByAsc(ProductImage::getSort)
            .last("limit 1"));
        snapshot.put("image", cover == null ? null : cover.getUrl());
        return JsonUtils.toJsonString(snapshot);
    }

    private String buildAddressSnapshot(UserAddress address) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("receiver", address.getReceiver());
        snapshot.put("phone", address.getPhone());
        snapshot.put("fullAddress", address.getProvince() + address.getCity()
            + (address.getDistrict() == null ? "" : address.getDistrict()) + address.getDetail());
        return JsonUtils.toJsonString(snapshot);
    }

    private void sendDelayMessage(Order order) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("orderId", order.getId());
        message.put("orderNo", order.getOrderNo());
        message.put("productId", order.getProductId());
        message.put("triggerTime", order.getTimeoutTime());
        try {
            SendResult result = rocketMQTemplate.syncSend(TOPIC_ORDER_DELAY,
                MessageBuilder.withPayload(JsonUtils.toJsonString(message)).build(),
                3000, DELAY_LEVEL_30M);
            log.info("订单延迟消息发送成功 orderNo={} msgId={}", order.getOrderNo(), result.getMsgId());
        } catch (Exception e) {
            // 消息发送失败不阻断下单,由定时任务兜底取消
            log.error("订单延迟消息发送失败 orderNo={}", order.getOrderNo(), e);
        }
    }

    private String generateOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return time + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long orderId, String payType) {
        Long buyerId = LoginHelper.getUserId();
        Order order = getOrder(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ServiceException("无权操作该订单");
        }
        if (OrderStatus.PAID.equals(order.getStatus()) || OrderStatus.SHIPPED.equals(order.getStatus())) {
            return; // 幂等:已支付直接返回
        }
        if (!OrderStatus.PENDING_PAY.equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许支付");
        }
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setPayNo(order.getOrderNo() + "01");
        record.setAmount(order.getPrice());
        record.setPayType(payType == null ? "ALIPAY" : payType);
        record.setStatus("SUCCESS");
        record.setPayTime(new Date());
        try {
            paymentMapper.insert(record);
        } catch (DuplicateKeyException e) {
            return; // 幂等:重复支付
        }
        // 订单 CAS:待支付 → 已支付
        updateStatusCas(orderId, OrderStatus.PENDING_PAY, OrderStatus.PAID);
        recordStatus(orderId, OrderStatus.PENDING_PAY, OrderStatus.PAID, "BUYER", buyerId, "模拟支付成功");
        // 商品 CAS:锁定 → 已售出
        LambdaUpdateWrapper<Product> uw = Wrappers.lambdaUpdate();
        uw.set(Product::getStatus, ProductStatus.SOLD);
        uw.eq(Product::getId, order.getProductId());
        uw.eq(Product::getStatus, ProductStatus.LOCKED);
        productMapper.update(null, uw);
        notify(order.getSellerId(), "订单已支付", "买家已付款,请尽快发货", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        Long userId = LoginHelper.getUserId();
        Order order = getOrder(orderId);
        if (!userId.equals(order.getBuyerId())) {
            throw new ServiceException("无权操作该订单");
        }
        doCancel(order, "BUYER", userId, "买家取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeout(Long orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null || !OrderStatus.PENDING_PAY.equals(order.getStatus())) {
            return; // 已支付/已取消,消息到达后忽略
        }
        doCancel(order, "SYSTEM", null, "超时未支付自动取消");
    }

    private void doCancel(Order order, String operatorType, Long operatorId, String remark) {
        updateStatusCas(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.CANCELED);
        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate();
        uw.set(Order::getCancelTime, new Date());
        uw.eq(Order::getId, order.getId());
        baseMapper.update(null, uw);
        recordStatus(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.CANCELED, operatorType, operatorId, remark);
        // 商品解锁:锁定 → 在售
        LambdaUpdateWrapper<Product> puw = Wrappers.lambdaUpdate();
        puw.set(Product::getStatus, ProductStatus.ON_SALE);
        puw.eq(Product::getId, order.getProductId());
        puw.eq(Product::getStatus, ProductStatus.LOCKED);
        productMapper.update(null, puw);
        notify(order.getBuyerId(), "订单已取消", remark, order.getId());
        notify(order.getSellerId(), "订单已取消", remark, order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId, String expressCompany, String expressNo) {
        Long sellerId = LoginHelper.getUserId();
        Order order = getOrder(orderId);
        if (!sellerId.equals(order.getSellerId())) {
            throw new ServiceException("无权操作该订单");
        }
        if (expressNo == null || expressNo.isBlank()) {
            throw new ServiceException("快递单号不能为空");
        }
        updateStatusCas(orderId, OrderStatus.PAID, OrderStatus.SHIPPED);
        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate();
        uw.set(Order::getExpressCompany, expressCompany);
        uw.set(Order::getExpressNo, expressNo);
        uw.set(Order::getShipTime, new Date());
        uw.eq(Order::getId, orderId);
        baseMapper.update(null, uw);
        recordStatus(orderId, OrderStatus.PAID, OrderStatus.SHIPPED, "SELLER", sellerId, "卖家发货");
        notify(order.getBuyerId(), "商品已发货", "快递单号:" + expressNo, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long orderId) {
        Long buyerId = LoginHelper.getUserId();
        Order order = getOrder(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ServiceException("无权操作该订单");
        }
        updateStatusCas(orderId, OrderStatus.SHIPPED, OrderStatus.RECEIVED);
        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate();
        uw.set(Order::getReceiveTime, new Date());
        uw.eq(Order::getId, orderId);
        baseMapper.update(null, uw);
        recordStatus(orderId, OrderStatus.SHIPPED, OrderStatus.RECEIVED, "BUYER", buyerId, "买家确认收货");
        notify(order.getSellerId(), "买家已确认收货", "交易即将完成", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long orderId, Integer rating, String content) {
        Long buyerId = LoginHelper.getUserId();
        Order order = getOrder(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ServiceException("无权操作该订单");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new ServiceException("评分需在 1~5 星之间");
        }
        OrderReview review = new OrderReview();
        review.setOrderId(orderId);
        review.setReviewerId(buyerId);
        review.setRevieweeId(order.getSellerId());
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(new Date());
        try {
            reviewMapper.insert(review);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("该订单已评价");
        }
        updateStatusCas(orderId, OrderStatus.RECEIVED, OrderStatus.REVIEWED);
        recordStatus(orderId, OrderStatus.RECEIVED, OrderStatus.REVIEWED, "BUYER", buyerId, "买家评价");
        // 自动流转:已评价 → 已完成
        updateStatusCas(orderId, OrderStatus.REVIEWED, OrderStatus.FINISHED);
        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate();
        uw.set(Order::getFinishTime, new Date());
        uw.eq(Order::getId, orderId);
        baseMapper.update(null, uw);
        recordStatus(orderId, OrderStatus.REVIEWED, OrderStatus.FINISHED, "SYSTEM", null, "系统自动完成");
        updateSellerRating(order.getSellerId(), rating);
        notify(order.getSellerId(), "收到新评价", "买家给出了 " + rating + " 星评价", orderId);
    }

    /**
     * 更新卖家综合评分(加权平均)与成交笔数
     */
    private void updateSellerRating(Long sellerId, Integer rating) {
        UserProfile profile = userProfileMapper.selectById(sellerId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(sellerId);
            profile.setNickname("卖家" + sellerId);
            profile.setSellerRating(BigDecimal.valueOf(rating));
            profile.setRatingCount(1);
            profile.setDealCount(1);
            profile.setCreateTime(new Date());
            profile.setUpdateTime(new Date());
            userProfileMapper.insert(profile);
            return;
        }
        int count = profile.getRatingCount() == null ? 0 : profile.getRatingCount();
        BigDecimal oldRating = profile.getSellerRating() == null ? BigDecimal.valueOf(5) : profile.getSellerRating();
        BigDecimal newRating = oldRating.multiply(BigDecimal.valueOf(count))
            .add(BigDecimal.valueOf(rating))
            .divide(BigDecimal.valueOf(count + 1L), 1, RoundingMode.HALF_UP);
        LambdaUpdateWrapper<UserProfile> uw = Wrappers.lambdaUpdate();
        uw.set(UserProfile::getSellerRating, newRating);
        uw.set(UserProfile::getRatingCount, count + 1);
        uw.setSql("deal_count = deal_count + 1");
        uw.set(UserProfile::getUpdateTime, new Date());
        uw.eq(UserProfile::getUserId, sellerId);
        userProfileMapper.update(null, uw);
    }

    @Override
    public TableDataInfo<OrderVo> queryMyPage(OrderBo bo, PageQuery pageQuery, String role) {
        Long userId = LoginHelper.getUserId();
        LambdaQueryWrapper<Order> lqw = Wrappers.lambdaQuery();
        if ("seller".equalsIgnoreCase(role)) {
            lqw.eq(Order::getSellerId, userId);
        } else {
            lqw.eq(Order::getBuyerId, userId);
        }
        lqw.eq(bo.getStatus() != null && !bo.getStatus().isBlank(), Order::getStatus, bo.getStatus());
        lqw.orderByDesc(Order::getCreateTime);
        Page<OrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillDisplayFields(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public OrderVo queryDetail(Long orderId) {
        Order order = getOrder(orderId);
        Long userId = LoginHelper.getUserId();
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new ServiceException("无权查看该订单");
        }
        OrderVo vo = MapstructUtils.convert(order, OrderVo.class);
        vo.setStatusLogs(statusLogMapper.selectList(Wrappers.<OrderStatusLog>lambdaQuery()
            .eq(OrderStatusLog::getOrderId, orderId)
            .orderByAsc(OrderStatusLog::getCreateTime)));
        fillSellerName(vo);
        fillSnapshotFields(vo);
        return vo;
    }

    private void fillDisplayFields(List<OrderVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        records.forEach(vo -> {
            fillSellerName(vo);
            fillSnapshotFields(vo);
        });
    }

    private void fillSellerName(OrderVo vo) {
        SellerInfoVo buyer = userProfileMapper.selectSellerInfo(vo.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        SellerInfoVo seller = userProfileMapper.selectSellerInfo(vo.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
    }

    private void fillSnapshotFields(OrderVo vo) {
        if (vo.getProductSnapshot() == null) {
            return;
        }
        Map<String, Object> snapshot = JsonUtils.parseMap(vo.getProductSnapshot());
        vo.setProductTitle(snapshot.get("title") == null ? null : String.valueOf(snapshot.get("title")));
        vo.setProductImage(snapshot.get("image") == null ? null : String.valueOf(snapshot.get("image")));
    }

    private Order getOrder(Long orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }

    /**
     * 订单状态 CAS 更新:WHERE id=? AND status=期望原状态
     */
    private void updateStatusCas(Long orderId, String from, String to) {
        LambdaUpdateWrapper<Order> uw = Wrappers.lambdaUpdate();
        uw.set(Order::getStatus, to);
        uw.eq(Order::getId, orderId);
        uw.eq(Order::getStatus, from);
        if (baseMapper.update(null, uw) <= 0) {
            throw new ServiceException("订单状态已变化,请刷新后重试");
        }
    }

    private void recordStatus(Long orderId, String from, String to, String operatorType, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        statusLogMapper.insert(log);
    }

    private void notify(Long userId, String title, String content, Long orderId) {
        notificationService.send(userId, "ORDER", title, content, "/trade/orderDetail?id=" + orderId);
    }
}
