package com.example.dropbox.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dropbox.config.RabbitMQConfig;
import com.example.dropbox.dto.file.FileProcessMsgDto;

@Service
public class FileMsgProducerService {
     @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendFileForProcessing(FileProcessMsgDto message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.ROUTING_KEY,
            message
        );
    }
}
