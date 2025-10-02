# UniVERSE

## Pitch
Student life is full of 'what's happening tonight?' moments, but finding the answer is lost in messy group chats and generic city-wide apps. UniVERSE solves this by putting the entire social pulse of your campus onto a live, interactive map. It’s an exclusive community, verified by school emails, where you can instantly discover everything from study groups to pickup games, see where the crowds are with real-time heatmaps, and join event-specific chats to connect before you even go. It's not just an event app; it's your key to a safer, more connected campus.

## Features
1. Interactive real-time map to discover live events at a glance.
2. Built-in group chats to coordinate and connect with attendees before the event.
3. Simple event creation to organize your own meetups, parties, and study groups.
4. Advanced event filtering to find exactly what you're looking for by date or activity type.
5. Personalized user profiles to showcase interests and find like-minded people.
6. Student verification via school email to ensure a safe and exclusive community.

## Additional Features
1. Calendar integration to sync your social schedule directly with your personal calendar.
2. Generative AI suggestions for personalized event recommendations based on your interests.

## Requirements
- **Split App Model:** Part of the map can be stored locally, most of the data is stored on Firebase.
- **User Support:** Profiles, preferences, active chat for events.
- **Sensor Use:** GPS is used for map interactivity.
- **Offline Mode:** Profiles, recently viewed events, saved events are cached locally.

## Team Conventions

### Code Review Process
#### Pull Request:
* Create a feature/fix branch for the task you are working on
* Ensure you have pulled the latest commit in the main branch
* Keep the request focused (only include related changes)
* Include tests for new/changed functionality
* Avoid including a lot of commits that could be added into one, or have been replaced by a later commit
* Keep commit messages clear (feat: add login API, test: add unit tests for login)
* Include a description on the PR explaining what, why, how and include (if it is the case) the issue it solves
* Example of good PR:
```
Title: feat: add login form and authentication API integration

Description:
Implements user login form and integrates with backend authentication API.

- Added LoginForm component with validation
- Integrated API call to /api/auth/login
- Added unit tests for form validation
- Added integration test for login flow
- Updated README with login instructions

Closes #12

Commits inside PR:
- feat: add login form UI with email + password fields
- feat: connect login form to backend API
- test: add unit tests for login validation
- test: add integration test for login flow
- docs: update README with login usage
```
* To consider it approved:
   * At least one team mate approval required
   * All CI checks must pass
* To merge: 
   * Close the related issue (if it is the case)
   * Squash and Merge to main

#### Code Review: 
* Review as fast as possible
* Read/ask to understand clearly the goal of the PR
* Check for:
   * Correctness (does it solve the task?)
   * Design (clean, maintainable, not over-engineered)
   * Integration (doesn't break other code)
   * Tests (enough coverage, edge cases)
   * Clarity (commit history + code make sense later)
* Add prefixes to comments so the PR author can prioritize:
   * **Important:** must fix before merge
   * **Question:** asking for clarification
   * **Minor:** non-blocking, suggestion
 
### Communication Channel 
Team's Telegram group

### Branch Naming 
- **Features:** `feature/<feature-name>`  
- **Bugfixes:** `fix/<issue-number>` (issue number of the GitHub issue previously created)
- 
### Figma
[View the Figma project](https://www.figma.com/files/team/1555155436667817387/project/462527297?fuid=1555684783915773231)

