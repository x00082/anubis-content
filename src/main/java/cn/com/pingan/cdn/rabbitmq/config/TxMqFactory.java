package cn.com.pingan.cdn.rabbitmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * @Classname TxMqFactory
 * @Description TODO
 * @Date 2020/10/19 16:42
 * @Created by Luj
 */
public class TxMqFactory implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(TxMqFactory.class);
    private RabbitAdmin rabbitAdmin;
    private DirectExchange exchange;
    private List<String> queues;

    public TxMqFactory(RabbitAdmin rabbitAdmin, DirectExchange exchange, List<String> queues) {
        this.rabbitAdmin = rabbitAdmin;
        this.exchange = exchange;
        this.queues = queues;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (queues == null || queues.size() == 0) {
            logger.warn("no valid queue");
            return;
        }
        queues.forEach(q -> {
            Queue queue = new Queue(q, true, false, false);
            this.rabbitAdmin.declareQueue(queue);
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(q);
            this.rabbitAdmin.declareBinding(binding);
        });
    }
}
