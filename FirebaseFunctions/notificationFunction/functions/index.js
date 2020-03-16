'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = admin.firestore();

exports.sendNotification = functions.firestore.document('notifications/{userId}' +
    '/{notificationType}/{notificationId}')
    .onWrite((change, context) => {
        // Store the values based off of their parameters in the collection path.
        const userId = context.params.userId;
        const notificationType = context.params.notificationType;
        const notificationId = context.params.notificationId;

        console.log('Send notification to : ', userId);

        if (context.empty) {
            return console.log('Notification deleted : ', notificationId);
        }

        const tokenRef = db.collection('users').doc(userId).collection('tokens');
        const allTokens = tokenRef.get()
            .then(snapshot => {

                const tokenList = [];

                if (snapshot.empty) {
                    console.log('No documents.');
                    return;
                }
                snapshot.forEach(doc => {
                    // Store each of the device's token ID's in a list.
                    tokenList.push(doc.val());
                    console.log('Token IDs: ', tokenList);
                });

                var notificationTitle = "";
                var notificationBody = "";

                // If the notification type is for friend requests, set the notification
                // values to friend request.
                if (notificationType == "friend_requests") {
                    notificationTitle = "Friend Request";
                    notificationBody = "You've received a new friend request";
                    console.log('Friend request notification');
                }

                // Notification body.
                const payload = {
                    notification: {
                        title: "Friend Request",
                        body: "You've received a new friend request",
                        icon: "default"
                    }
                };

                // Send a notification to each of the device's token ID's.
                tokenList.forEach(tokenId => {return admin.messaging().sendToDevice
                    (tokenId, payload).then(response => {
                        console.log('The notification has been sent.');
                    })});

            })
            .catch(err => {
                console.log('Error getting documents', err);
            });
    });