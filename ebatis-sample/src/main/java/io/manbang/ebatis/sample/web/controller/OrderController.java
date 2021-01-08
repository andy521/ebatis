package io.manbang.ebatis.sample.web.controller;

import io.manbang.ebatis.core.domain.Page;
import io.manbang.ebatis.core.domain.Pageable;
import io.manbang.ebatis.sample.entity.RecentOrder;
import io.manbang.ebatis.sample.entity.RecentOrderModel;
import io.manbang.ebatis.sample.mapper.OrderMapper;
import io.manbang.ebatis.sample.mapper.RecentOrderGetMapper;
import io.manbang.ebatis.sample.mapper.RecentOrderIndexMapper;
import io.manbang.ebatis.sample.mapper.RecentOrderMultiGetMapper;
import io.manbang.ebatis.sample.model.Order;
import io.manbang.ebatis.sample.model.OrderCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 章多亮
 * @since 2020/6/1 18:18
 */
@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    private final OrderMapper orderMapper;
    private final RecentOrderGetMapper recentOrderGetMapper;
    private final RecentOrderIndexMapper recentOrderIndexMapper;
    private final RecentOrderMultiGetMapper recentOrderMultiGetMapper;

//    public OrderController(OrderMapper orderMapper) {
//        this.orderMapper = orderMapper;
//    }
    public OrderController(OrderMapper orderMapper, RecentOrderGetMapper recentOrderGetMapper, RecentOrderIndexMapper recentOrderIndexMapper, RecentOrderMultiGetMapper recentOrderMultiGetMapper) {
        this.orderMapper = orderMapper;
        this.recentOrderGetMapper = recentOrderGetMapper;
        this.recentOrderIndexMapper = recentOrderIndexMapper;
        this.recentOrderMultiGetMapper = recentOrderMultiGetMapper;
    }

    @PostMapping
    public Page<Order> search(@RequestBody OrderCondition condition, Pageable pageable) {
        return orderMapper.search(condition, pageable);
    }

    @GetMapping("/{id}")
    public DeferredResult<Order> findById(@PathVariable String id) {
        DeferredResult<Order> deferredResult = new DeferredResult<>(TimeUnit.SECONDS.toMillis(30));

        orderMapper.findById(id)
                .whenComplete((order, throwable) -> {
                    if (throwable == null) {
                        deferredResult.setResult(order);
                    } else {
                        deferredResult.setErrorResult(throwable);
                    }
                });

        return deferredResult;
    }

    /**
     * 创建一笔订单
     */
    @PostMapping("/createOne")
    public  DeferredResult<String> createOne(RecentOrderModel recentOrderModel) {
        DeferredResult<String> result=new DeferredResult<String>();
        String id = recentOrderIndexMapper.indexRecentOrderString(recentOrderModel);
        log.info("index id:{}", id);
        result.setResult(id);
        return result;
    }

    @PostMapping("/getAll")
    public  DeferredResult<List<RecentOrder>> getAll() {
        DeferredResult<List<RecentOrder>>  result =new DeferredResult<List<RecentOrder>> ();
        List<RecentOrder> recentOrders = recentOrderMultiGetMapper.getRecentOrders();
        result.setResult(recentOrders);
        return  result;

    }

    @PostMapping("/getOne/{id}")
    public  DeferredResult<RecentOrder> getOne(@PathVariable String id) {
        DeferredResult<RecentOrder> result =new DeferredResult<RecentOrder>();
        RecentOrder recentOrder = recentOrderGetMapper.getRecentOrder(id);
        result.setResult(recentOrder);
        return  result;

    }


}
