import React, { useState, useEffect, useRef } from "react";
import "../../../styles/ChatWindow.css";
import { FaTimes } from "react-icons/fa";
import { Stomp } from "@stomp/stompjs"; // STOMP protocol for WebSocket communication
import SockJS from "sockjs-client"; // SockJS client for WebSocket fallback
import { getUserDetailsFromToken } from "../../../Utils/TokenUtil";

const ChatWindow = ({ chatGroup, onClose }) => {
  // State to hold the input message
  const [message, setMessage] = useState("");
  // State to hold the list of messages received in the chat
  const [messages, setMessages] = useState([]);

  // useRef to hold the WebSocket client across renders without causing re-renders
  const stompClient = useRef(null);

  // Extract user details from the token, default to "User" if not available
  const userDetails = getUserDetailsFromToken();

  //"User" placeholder if somewhy the util wouldn't be able to extract username
  const username = userDetails ? userDetails.username : "User";

  // useEffect to handle WebSocket connection when the chatGroup changes
  useEffect(() => {
    // Initialize the STOMP client over SockJS
    stompClient.current = Stomp.over(
      () => new SockJS("http://localhost:8080/ws")
    );
    stompClient.current.connect(
      {},
      () => {
        console.log("Connected to WebSocket");
        onConnected();
      },
      onError
    );

    // Clean up function to disconnect WebSocket when the component unmounts
    return () => {
      if (stompClient.current !== null) {
        stompClient.current.disconnect();
        console.log("Disconnected from WebSocket");
      }
    };
  });

  // Function to handle WebSocket connection and subscribing to the group
  const onConnected = () => {
    console.log(`Connected to chat group: ${chatGroup}`);
    stompClient.current.subscribe(`/topic/${chatGroup}`, onMessageReceived);
  };

  // Function to handle errors during WebSocket connection
  const onError = (error) => {
    console.error("WebSocket connection error:", error);
  };

  // Function to handle incoming messages
  const onMessageReceived = (payload) => {
    const receivedMessage = JSON.parse(payload.body); //Parse message JSON
    console.log("Received message:", receivedMessage);

    // Add the new message to the messages array if it is not a duplicate
    setMessages((prevMessages) => {
      if (
        prevMessages.length === 0 ||
        !(
          prevMessages[prevMessages.length - 1].content ===
            receivedMessage.content &&
          prevMessages[prevMessages.length - 1].sender ===
            receivedMessage.sender
        )
      ) {
        return [...prevMessages, receivedMessage]; // Add new message
      }
      return prevMessages; // Ignore if it's a duplicate message
    });
  };

  // Function to send a message to the server
  const sendMessage = () => {
    const trimmedMessage = message.trim(); // Remove any leading or trailing spaces
    console.log("Attempting to send message:", trimmedMessage);

    // Ensure the STOMP client is connected and the message is not empty
    if (stompClient.current) {
      if (trimmedMessage) {
        const chatMessage = {
          group: chatGroup,
          sender: username,
          content: trimmedMessage,
        };

        // Send the message to the server
        console.log("Sending message:", chatMessage);
        stompClient.current.send(
          "/app/sendMessage", // Destination endpoint on the server
          {}, // Headers
          JSON.stringify(chatMessage) // Message payload
        );
        console.log("Message sent, waiting for response...");

        setMessage(""); // Clear the input field after sending
      } else {
        console.warn("Cannot send empty message:", trimmedMessage);
      }
    }
  };

  return (
    <div className="chat-window">
      <div className="chat-header">
        <span>{chatGroup}</span>
        <FaTimes className="close-icon" onClick={() => onClose(chatGroup)} />
      </div>
      <div className="chat-body">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`chat-message ${
              msg.sender === username ? "message-right" : "message-left"
            }`}
          >
            <p className="message-sender">{msg.sender}</p>
            <p className="message-content">{msg.content}</p>
          </div>
        ))}
      </div>
      <div className="chat-footer">
        <input
          type="text"
          placeholder="Type a message..."
          value={message}
          onChange={(e) => {
            setMessage(e.target.value);
            console.log("Input value changed:", e.target.value);
          }}
          onKeyPress={(e) => {
            if (e.key === "Enter") {
              console.log("Enter key pressed");
              sendMessage();
            }
          }}
        />
        <button
          onClick={() => {
            console.log("Send button clicked");
            sendMessage();
          }}
        >
          Send
        </button>
      </div>
    </div>
  );
};

export default ChatWindow;
