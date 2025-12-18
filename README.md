# UniVERSE

## Pitch
Social life happens in places, not in lists. UniVERSE eliminates the friction of social planning by replacing disorganized group chats with a live, geolocation-centric interface. We address the two primary issues in ad-hoc gathering: logistical inefficiency and the lack of trust in user profiles. By leveraging Generative AI for automated content creation and a "Data vs. Aspiration" profile system, UniVERSE provides a transparent, efficient, and dynamic mechanism to coordinate campus life. It serves as a tool for verifying user reliability and accessing the private layer of a social network.

## Features
1. **Map-First Interface:** A real-time, interactive map prioritizing spatial relevance over chronological feeds, allowing users to assess event value and location immediately.
2. **Contextual Ephemeral Chats:** Event-specific communication channels that instantiate only upon joining and dissolve when the event concludes, preventing the accumulation of dormant group chats.
3. **Data-Driven Profiles:** A dual-layer identity system that contrasts self-selected interest tags against an immutable log of past event participation to verify behavioral consistency.
4. **The Private Layer:** A functional "Follow" mechanism where connections serve as an access key to restricted, follower-only events rather than just a passive content feed.
5. **List View:** An alternative, high-density interface for users who prefer scanning data points over spatial exploration.
6. **Trust Verification:** A system that prioritizes historical activity data over self-reported descriptions to establish transparency between attendees.
7. **AI Event Refinement:** A generative tool that processes raw user intent and restructures the phrasing to match specific social or professional tones.
8. **Zero-Shot Event Generation:** An automated system that analyzes environmental variables—such as time, location, weather, and season, to propose fully formed events without manual input.

## Requirements
- **Split App Model:** Map tiles and rendering are handled via the TomTom SDK, while application logic and state synchronization rely on Firebase.
- **User Support:** Advanced profile structures that strictly separate user-defined aspirations from system-generated activity logs.
- **Sensor Use:** Real-time GPS and geolocation services are mandatory for map interactivity and local discovery.
- **Backend:** Google Firestore handles real-time data persistence, with the Gemini API driving the generative intelligence features.

## Visual Showcase
<img width="270" height="600" alt="Screenshot_20251218_020751" src="https://github.com/user-attachments/assets/75f88ac3-8efe-47f7-a123-32138f07932e" />
<img width="270" height="600" alt="Screenshot_20251218_020825" src="https://github.com/user-attachments/assets/9322d8a4-7e83-4cbd-b5e4-cb1622cc9611" />


### [Figma](https://www.figma.com/design/l8d5EDp3MVQ6yAhZAaCLB8/M2?version-id=2298680008820030344&node-id=371-14030&p=f&t=JPeXE17QynChGNUk-0)

### [Diagram](https://excalidraw.com/#json=aihSBmPrciFoy536SArZc,ikeKVfC_ySXwXL_Xfklw8Q)

