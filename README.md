# Utcazene

An unofficial app for Veszprém's street music festival.

## TODO list before party

- drinking games
- ideas if there's time for it
  - "Where am I?" feature which shows you the current musician based on your location

## Drinking game ideas

Creative Drinking Games for UZ App

Since your app is designed for a street music festival (Utcazene) and includes features like Google Nearby Connections, real-time schedules, and home screen widgets, here are some creative drinking games that take advantage of these smartphone capabilities:

1. "Nearby Roulette" (The Proximity Game)
   •
   The Tech: Uses the NearbyManager and FriendsFeature to detect other app users nearby.
   •
   How it works: Whenever your phone successfully establishes a connection with a new "Friend" in the background (via Bluetooth/Nearby API), the app sends a push notification: "New Musical Soulmate Found! 🍻 Cheers!".
   •
   The Rule: You and the person you just "connected" with (if you can find them in the crowd!) both take a drink. If you're with a group, everyone drinks when any group member's phone pings a new connection.
2. "Stage Sync-or-Drink"
   •
   The Tech: Uses the localScreen and favoriteSlugs data currently broadcasted in FriendsFeature.
   •
   How it works: Open the "Nearby Friends" screen. The app shows what stage or artist your friends are currently looking at or have favorited.
   •
   The Rule: If you and your friends are looking at different artists' schedules, the person looking at the artist furthest away (in time or distance) drinks. If you all happen to be looking at the same stage, you're "in sync" and everyone else in the bar drinks.
3. "Widget Flash Mob"
   •
   The Tech: Uses the NowPlayingWidget and NotificationHelper.
   •
   How it works: The app sends a "Hidden Performance" notification at random intervals.
   •
   The Rule: The last person in your group to tap the NowPlayingWidget on their home screen to see the "performance details" has to finish their drink. It turns the home screen into a reaction-time game.
4. "The Thread Node Relay" (Message Chain)
   •
   The Tech: Uses the ThreadNode messaging system in FriendsFeature.
   •
   How it works: Start a message thread in the app. Each person has to add a word to a sentence using the "Send Message" feature.
   •
   The Rule: If the Nearby sync fails to deliver your message to the group (because you wandered too far away from the "mesh"), you're out of the loop and must drink to rejoin.
5. "Battery Chicken: Festival Edition"
   •
   The Tech: Basic system battery API.
   •
   How it works: The app displays a "Power Leaderboard" for all connected Nearby friends.
   •
   The Rule: At the end of every music set (checked via ImuRepository schedules), the person with the lowest battery percentage drinks. They're clearly using the app the most—or forgot their power bank!
6. "Artist Match-Maker"
   •
   The Tech: Uses favoriteSlugs from the FriendsPayload.
   •
   How it works: Compare your "Favorite Artists" list with a nearby stranger using the app.
   •
   The Rule: For every artist you both have in common in your favorites, take a sip. If you have zero matches, you both drink to find better common ground.
   Implementation Suggestion:
   You could add a simple DrinkingGameManager that listens to FriendsFeature.connectedFriends and triggers these events. For example, a "Cheers" notification when _connectedFriends.value.size increases.
   Would you like me to help you implement a simple version of one of these, perhaps a "Nearby Connection" notification or a "Battery Leaderboard"?

## Contribution

### Localization

In the [resources folder](./app/src/main/res) you can find the localization files in [values](./app/src/main/res/values) and [values-hu](./app/src/main/res/values-hu) folders. Every text is in the [strings.xml](./app/src/main/res/values/strings.xml) file and "message of the day" items are in the [motds.xml](./app/src/main/res/values/motds.xml) file.
