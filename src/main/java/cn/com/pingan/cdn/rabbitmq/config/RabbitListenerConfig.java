package cn.com.pingan.cdn.rabbitmq.config;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @Classname RabbitListener
 * @Description TODO
 * @Date 2020/10/27 16:34
 * @Created by Luj
 */
@Component
public class RabbitListenerConfig {
    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    public void stopAll() {
        this.rabbitListenerEndpointRegistry.stop();
    }

    public void startAll() {
        this.rabbitListenerEndpointRegistry.start();
    }

    private boolean isQueueListener(String queueName, MessageListenerContainer listenerContainer) {
        if (listenerContainer instanceof AbstractMessageListenerContainer) {
            AbstractMessageListenerContainer abstractMessageListenerContainer = (AbstractMessageListenerContainer) listenerContainer;
            String[] queueNames = abstractMessageListenerContainer.getQueueNames();
            return ArrayUtils.contains(queueNames, queueName);
        }
        return false;
    }

    public boolean stop(String queueName) {
        Collection<MessageListenerContainer> listenerContainers = this.rabbitListenerEndpointRegistry.getListenerContainers();
        for (MessageListenerContainer listenerContainer : listenerContainers) {
            if (this.isQueueListener(queueName, listenerContainer)) {
                listenerContainer.stop();
                return true;
            }
        }
        return false;
    }

    public boolean start(String queueName) {
        Collection<MessageListenerContainer> listenerContainers = this.rabbitListenerEndpointRegistry.getListenerContainers();
        for (MessageListenerContainer listenerContainer : listenerContainers) {
            if (this.isQueueListener(queueName, listenerContainer)) {
                listenerContainer.start();
                return true;
            }
        }
        return false;
    }
}
