'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = admin.firestore();

// This will allow background notifications.
exports.sendNotification = functions.firestore.document('notifications/{userId}' +
    '/{notificationType}/{notificationId}')
    .onWrite((change, context) => {
        // Store the values based off of their paramaters in the collection path.
        const userId = context.params.userId;
        const notificationType = context.params.notificationType;
        const notificationId = context.params.notificationId;

        console.log('Send notification to : ', userId);

        if (context.empty) {
            return console.log('Notification deleted : ', notificationId);
        }

        const fromUser = db.collection('notifications').doc(userId)
            .collection(notificationType).doc(notificationId).get();
        return fromUser.then(doc => {
            // Store the notification sender's user ID.
            const fromUserId = doc.data()['from'];
            console.log('from ', fromUserId);

            const userQuery = db.collection('users').doc(fromUserId).get();
            return userQuery.then(doc => {
                // Store the notification sender's username.
                const username = doc.data()['username'];

                const tokenRef = db.collection('users').doc(userId).collection('tokens').get();
                return tokenRef.then(snapshot => {

                    const tokenList = [];

                    if (snapshot.empty) {
                        console.log('No documents.');
                    }
                    snapshot.forEach(doc => {
                        // Store each of the device's token ID's in a list.
                        console.log("Token: " + doc.data()['token']);
                        tokenList.push(doc.data()['token']);
                        tokenList.forEach(token => console.log(token));
                        return null;
                    });

                    var notificationTitle = "";
                    var notificationBody = "";

                    // If the notification type is for friend requests, set the notification
                    // values to friend request.
                    if (notificationType == "friend_requests") {
                        notificationTitle = "You Have a New Friend Request";
                        notificationBody = username + " sent you a friend request.";
                        console.log('Friend request notification');
                    }

                    const payload = {
                        notification: {
                            // Notification body.
                            title: notificationTitle,
                            body: notificationBody,
                            icon: "default",

                            // Send click action to onMessageReceive().
                            clickAction: "com.example.fitnessapp_NOTIFICATION"
                        },
                        data: {
                            // Send the user's ID to onMessageReceive().
                            fromUserId: fromUserId
                        }
                    };

                    // Send a notification to each of the device's token ID's.
                    tokenList.forEach(tokenId => {
                        return admin.messaging().sendToDevice
                            (tokenId, payload).then(response => {
                                console.log('The notification has been sent.');
                                return null;
                            });
                    });

                    return null;

                })
                    .catch(err => {
                        console.log('Error getting documents', err);
                        return null;
                    });
            })
        });
    });