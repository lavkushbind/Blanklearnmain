 const {onValueCreated} = require("firebase-functions/v2/database");
const {initializeApp} = require("firebase-admin/app");
const {getDatabase} = require("firebase-admin/database");
const {getMessaging} = require("firebase-admin/messaging");
const logger = require("firebase-functions/logger");

// Initialize the Firebase Admin SDK. This is required for all functions.
initializeApp();


// ================================================================= //
//                Function 1: Send Demo Booking Notifications        //
// ================================================================= //
/**
 * Triggers when a new demo is booked under /allocated_classes/{allocationId}.
 * It sends a push notification to the assigned teacher.
 */
exports.sendDemoBookingNotification = onValueCreated("/allocated_classes/{allocationId}", async (event) => {
  const bookingData = event.data.val();
  if (!bookingData) {
    logger.log("Demo Booking: New node is empty. Exiting.");
    return null;
  }

  const teacherId = bookingData.teacherID;
  const studentName = bookingData.studentName || "A new student";
  const className = bookingData.className;
  const timeSlot = bookingData.timeSlot;

  // Exit if there's no teacher ID in the booking data.
  if (!teacherId) {
    logger.log("Demo Booking: No teacher ID found. Exiting.");
    return null;
  }

  logger.log(`Demo Booking: New demo for teacher ${teacherId} by student ${studentName}`);

  try {
    const db = getDatabase();
    // CORRECTED: Look for the teacher's profile in the /Users node.
    const teacherProfileSnap = await db.ref(`/Users/${teacherId}`).once("value");

    if (!teacherProfileSnap.exists()) {
      logger.error(`Demo Booking: Teacher profile not found in /Users for ID: ${teacherId}`);
      return null;
    }

    const fcmToken = teacherProfileSnap.child("fcmToken").val();
    if (!fcmToken) {
      logger.warn(`Demo Booking: FCM token not found for teacher: ${teacherId}`);
      return null;
    }

    logger.log(`Demo Booking: Found FCM token for teacher: ${fcmToken}`);

    // Construct the notification payload
    const payload = {
      notification: {
        title: "New Demo Class Booked! 🎉",
        body: `${studentName} has booked a demo for ${className} at ${timeSlot}.`,
      },
      data: {
        bookingId: event.params.allocationId,
        type: "demo_booking" // A custom type to identify this notification
      },
    };

    // Send the message
    const messaging = getMessaging();
    await messaging.send({ token: fcmToken, ...payload });
    logger.log(`Demo Booking: Notification sent successfully to teacher ${teacherId}`);

  } catch (error) {
    logger.error("Demo Booking: Error sending message:", error);
  }

  return null;
});


// ================================================================= //
//                Function 2: Send Chat Notifications                //
// ================================================================= //
/**
 * Triggers when a new message is created in a personal chat.
 * Sends a push notification to the recipient with an intelligent message body.
 */
exports.sendChatNotification = onValueCreated("/Personal_chat/{chatId}/mess/{messageId}", async (event) => {
  // Get the data of the new message that was just created.
  const messageData = event.data.val();
  if (!messageData) {
    logger.log("Chat: Message data is null. Exiting function.");
    return null;
  }

  // Get the sender's ID and the message text from your database structure.
  const senderId = messageData.muid;
  const messageText = messageData.masseg;
  const chatId = event.params.chatId;

  // Safety check: Exit if we don't have a sender or chat ID.
  if (!senderId || !chatId) {
    logger.log("Chat: Missing senderId or chatId. Exiting.");
    return null;
  }

  // --- Logic to determine who the recipient is ---
  // The chatId is a combination of two user IDs (e.g., "userA_userB").
  const ids = chatId.split("_");
  // The recipient is the ID in the chatID that is NOT the sender.
  const recipientId = ids[0] === senderId ? ids[1] : ids[0];

  if (!recipientId) {
    logger.error("Chat: Could not determine recipient ID from chatId:", chatId);
    return null;
  }

  logger.log(`Chat: New message from ${senderId} to ${recipientId}`);

  try {
    const db = getDatabase();

    // --- Get the profiles of both the sender and the recipient ---
    const [senderProfileSnap, recipientProfileSnap] = await Promise.all([
      db.ref(`/Users/${senderId}`).once("value"),
      db.ref(`/Users/${recipientId}`).once("value"),
    ]);

    // Check if the recipient exists in the database and has an FCM token.
    if (!recipientProfileSnap.exists()) {
      logger.log("Chat: Recipient profile not found for ID:", recipientId);
      return null;
    }
    const recipientToken = recipientProfileSnap.child("fcmToken").val();
    if (!recipientToken) {
      logger.warn("Chat: FCM token not found for recipient:", recipientId);
      return null;
    }

    const senderName = senderProfileSnap.child("name").val() || "Someone";

    // --- Intelligent Notification Body Logic ---
    let notificationBody;
    if (messageText && messageText.startsWith("https://firebasestorage.googleapis.com")) {
      // It's a file from your Firebase Storage.
      notificationBody = `Sent a file.`; // Generic message for any file
    } else {
      // It's regular text or another URL, so show the original message.
      notificationBody = messageText || "Sent a new message."; // Fallback if message is empty
    }
    // --- End of Logic ---

    const payload = {
      notification: {
        title: senderName,
        body: notificationBody,
      },
      data: {
        type: "chat_message",
        senderId: senderId,
      },
    };

    const messaging = getMessaging();
    await messaging.send({ token: recipientToken, ...payload });
    logger.log("Chat: Notification sent successfully to", recipientId);

  } catch (error) {
    logger.error("Chat: Error sending notification:", error);
  }
  return null;
});