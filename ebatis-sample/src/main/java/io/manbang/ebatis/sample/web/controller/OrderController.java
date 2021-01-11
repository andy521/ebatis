package io.manbang.ebatis.sample.web.controller;

import io.manbang.ebatis.core.domain.Page;
import io.manbang.ebatis.core.domain.Pageable;
import io.manbang.ebatis.sample.condition.RecentOrderCondition;
import io.manbang.ebatis.sample.entity.RecentOrder;
import io.manbang.ebatis.sample.entity.RecentOrderModel;
import io.manbang.ebatis.sample.mapper.*;
import io.manbang.ebatis.sample.model.Order;
import io.manbang.ebatis.sample.model.OrderCondition;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestStatus;
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
    private final RecentOrderSearchMapper recentOrderSearchMapper;
    private final RecentOrderGetMapper recentOrderGetMapper;
    private final RecentOrderIndexMapper recentOrderIndexMapper;
    private final RecentOrderMultiGetMapper recentOrderMultiGetMapper;
    private final RecentOrderDeleteMapper recentOrderDeleteMapper;
    private final RecentOrderUpdateMapper recentOrderUpdateMapper;

//    public OrderController(OrderMapper orderMapper) {
//        this.orderMapper = orderMapper;
//    }
    public OrderController( RecentOrderSearchMapper recentOrderSearchMapper, RecentOrderGetMapper recentOrderGetMapper, RecentOrderIndexMapper recentOrderIndexMapper, RecentOrderMultiGetMapper recentOrderMultiGetMapper, RecentOrderDeleteMapper recentOrderDeleteMapper, RecentOrderUpdateMapper recentOrderUpdateMapper) {
        this.recentOrderSearchMapper = recentOrderSearchMapper;
        this.recentOrderGetMapper = recentOrderGetMapper;
        this.recentOrderIndexMapper = recentOrderIndexMapper;
        this.recentOrderMultiGetMapper = recentOrderMultiGetMapper;
        this.recentOrderDeleteMapper = recentOrderDeleteMapper;
        this.recentOrderUpdateMapper = recentOrderUpdateMapper;
    }

    @PostMapping("/search")
    public Page<RecentOrder> search( RecentOrderCondition condition, Pageable pageable) {
        return recentOrderSearchMapper.queryRecentOrderPage(pageable,condition);
    }
    @PostMapping("/searchList")
    public List<RecentOrder> searchList( RecentOrderCondition condition) {
        return recentOrderSearchMapper.queryRecentOrderList(condition);
    }
    /*@PostMapping("/search")
    public Page<RecentOrder> search(@RequestBody RecentOrderCondition condition, Pageable pageable) {
        return recentOrderSearchMapper.queryRecentOrderPage(pageable,condition);
    }*/

    @GetMapping("/{id}")
    public DeferredResult<RecentOrder> findById(@PathVariable String id) {
        ////建立DeferredResult对象，设置超时时间，以及超时返回超时结果
        DeferredResult<RecentOrder> deferredResult = new DeferredResult<>(TimeUnit.SECONDS.toMillis(30));

        recentOrderGetMapper.getRecentOrderCompletableFuture(id)
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

    /**
     * 查所用  < 有问题fixme
     * @return
     */
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
    @PostMapping("/updateOne")
    public  DeferredResult<String> update(RecentOrderModel recentOrderModel) {
        DeferredResult<String> result =new DeferredResult<String>();
        RestStatus restStatus = recentOrderUpdateMapper.updateRecentOrderRestStatus(recentOrderModel);
        result.setResult(restStatus.name());
        return  result;

    }


}
