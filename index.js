'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
exports.sendMessNotification = functions.database.ref('/MessageNoti/{user_id}/{notification_id}').onWrite((change, context)=>{
	
	const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;
	
	console.log('We have a notification from : ', user_id);
	
	
	
	 const fromUser = admin.database().ref(`/MessageNoti/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;

    console.log('You have new notification from  : ', from_user_id);

    /*
     * The we run two queries at a time using Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : "New Message",
          body: `${userName} has sent you message`,
          icon: "default",
          click_action : "com.example.encchat_TARGET_MNOTIFICATION"
        },
        data : {
          from_user_id : from_user_id
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was the notification Feature');

      });

    });

  });
	
	
	
	
	
	
	
	
	
	
	
	
});

























