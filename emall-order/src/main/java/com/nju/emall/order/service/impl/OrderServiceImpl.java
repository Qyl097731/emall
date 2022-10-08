package com.nju.emall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nju.common.constant.AuthServerConstant;
import com.nju.common.exception.NoStockException;
import com.nju.common.to.OrderTo;
import com.nju.common.to.SkuHasStockTo;
import com.nju.common.utils.R;
import com.nju.common.vo.MemberResponseVo;
import com.nju.emall.order.constant.OrderConstant;
import com.nju.emall.order.constant.PayConstant;
import com.nju.emall.order.entity.OrderItemEntity;
import com.nju.emall.order.entity.PaymentInfoEntity;
import com.nju.emall.order.enume.OrderStatusEnum;
import com.nju.emall.order.feign.CartFeignSerivce;
import com.nju.emall.order.feign.MemberFeignService;
import com.nju.emall.order.feign.ProductFeignService;
import com.nju.emall.order.feign.WmsFeignService;
import com.nju.emall.order.interceptor.OrderInterceptor;
import com.nju.emall.order.service.OrderItemService;
import com.nju.emall.order.service.PaymentInfoService;
import com.nju.emall.order.to.OrderCreateTo;
import com.nju.emall.order.to.SpuInfoTo;
import com.nju.emall.order.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nju.common.utils.PageUtils;
import com.nju.common.utils.Query;

import com.nju.emall.order.dao.OrderDao;
import com.nju.emall.order.entity.OrderEntity;
import com.nju.emall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.nju.emall.order.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignSerivce cartFeignSerivce;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        baseMapper.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder(HttpServletRequest request) {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 获取所有可选地址
        MemberResponseVo memberResponseVo = (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressFeature = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> addresses = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addresses);
        }, executor);


        CompletableFuture<Void> cartItemFeature = CompletableFuture.runAsync(() -> {
            // 获取购物车中的所有商品
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> cartItems = cartFeignSerivce.getCurrentCartItems();
            confirmVo.setItems(cartItems);
        }, executor).thenRunAsync(() -> {
            Map<Long, Boolean> hasstock = new HashMap<>();
            List<OrderItemVo> items = confirmVo.getItems();
            if (!CollectionUtils.isEmpty(items)) {
                List<Long> ids = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = wmsFeignService.hasstock(ids);
                List<SkuHasStockTo> data = r.getData("data", new TypeReference<List<SkuHasStockTo>>() {
                });
                if (!CollectionUtils.isEmpty(data)) {
                    hasstock = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                }
            }
            confirmVo.setStocks(hasstock);
        });

        // 用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressFeature, cartItemFeature).join();

        return confirmVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo orderResponseVo = new SubmitOrderResponseVo();
        orderResponseVo.setCode(0);
        HttpSession session = OrderInterceptor.threadLocal.get();
        MemberResponseVo responseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        String orderToken = vo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(USER_ORDER_TOKEN_PREFIX + responseVo.getId()),
                orderToken);
        // 验证失败 没有token
        if (result == 0L) {
            orderResponseVo.setCode(1);
            return orderResponseVo;
        } else {
            // 令牌验证成功
            // 下单 去创建订单，验证令牌、验证价格、锁库存
            // 创建订单、订单项等信息
            OrderCreateTo order = createOrder();
            // 验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 金额对比成功
                // 保存订单
                saveOrder(order);
                // 库存锁定，只要有异常回滚订单数据
                // 订单号，所有订单项 SkuId，skuName，num
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                // 远程所库存
                // 为了保证高并发，库存服务自己回滚，可以发消息给库存服务
                // 库存服务本身也可以使用自动解锁，消息队列
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
                    orderResponseVo.setOrder(order.getOrder());
                    // 订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return orderResponseVo;
                } else {
                    // 锁定失败
                    orderResponseVo.setCode(3);
                    throw new NoStockException();
                }

            } else {
                orderResponseVo.setCode(2);
                return orderResponseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 查询当前的订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCELED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // TODO 保证消息一定发送出去 每个消息都做好日志记录 把消息详细信息存储在数据库
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                // TODO 将没发送成功的消息进行重新发送
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderEntity = getOrderByOrderSn(orderSn);
        PayVo payVo = new PayVo();
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(orderSn);


        List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        payVo.setSubject(itemEntities.get(0).getSkuName());
        payVo.setBody(itemEntities.get(0).getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        HttpSession session = OrderInterceptor.threadLocal.get();
        MemberResponseVo responseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", responseVo.getId())
                        .orderByDesc("id")
        );
        List<OrderEntity> records = page.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            records = records.stream().map(record -> {
                List<OrderItemEntity> items = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", record.getOrderSn()));
                record.setOrderItems(items);
                return record;
            }).collect(Collectors.toList());
        }
        page.setRecords(records);
        return new PageUtils(page);


    }

    /**
     * 处理支付宝支付结果
     *
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // 保存加以流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        paymentInfoEntity.setSubject(vo.getBody());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCreateTime(new Date());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        // 修改订单的状态
        String status = vo.getTrade_status();
        if (status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")) {
            //支付成功状态
            String orderSn = vo.getOut_trade_no(); //获取订单号
            baseMapper.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }


    private OrderCreateTo createOrder() {
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        // 创建订单
        OrderCreateTo createTo = new OrderCreateTo();
        OrderEntity orderEntity = createOrder(submitVo);
        createTo.setOrderItems(buildOrderItems(orderEntity.getOrderSn()));
        createTo.setOrder(orderEntity);
        return createTo;
    }

    private OrderEntity createOrder(OrderSubmitVo submitVo) {
        // 生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        // 获取收货地址信息
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });

        //设置运费
        orderEntity.setFreightAmount(fareVo.getFare());
        // 设置收货人信息
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());


        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        // 获取所有订单项
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        // 校验价格
        computePrice(orderEntity, orderItems);
        return orderEntity;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        int gift = 0;
        int growth = 0;

        for (OrderItemEntity orderItem : orderItems) {
            total = total.add(orderItem.getRealAmount());
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            integration = integration.add(orderItem.getIntegrationAmount());
            gift += orderItem.getGiftIntegration();
            growth += orderItem.getGiftGrowth();
        }
        // 订单总额
        orderEntity.setTotalAmount(total);
        // 订单加运费的总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setGrowth(growth);
        orderEntity.setIntegration(gift);
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemEntity> itemEntities = new ArrayList<>();
        // 获取所有的订单项
        List<OrderItemVo> cartItems = cartFeignSerivce.getCurrentCartItems();
        if (!CollectionUtils.isEmpty(cartItems)) {
            itemEntities = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return itemEntities;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 订单信息：订单号
        // 商品的SPU
        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoTo data = r.getData(new TypeReference<SpuInfoTo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());

        // 商品的SKU
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(String.join(";", item.getSkuAttrValues()));
        orderItemEntity.setSkuQuantity(item.getCount());
        // 优惠信息[不做]
        // 积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        // 订单项的价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
        // 当前订单项实际金额
        BigDecimal realPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()))
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount())
                .subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }

}
