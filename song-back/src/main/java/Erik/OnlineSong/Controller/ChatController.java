package Erik.OnlineSong.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import Erik.OnlineSong.DTO.ChatMessage;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Endpoint for sending a message
    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage chatMessage) {
        // Sending the message to specific topic
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getGroup(), chatMessage);
    }
}
