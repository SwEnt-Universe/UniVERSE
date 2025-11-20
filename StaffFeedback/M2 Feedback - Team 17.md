# SwEnt M2 Team Grading Report

The M2 feedback provides an opportunity to give you, as a team, formal feedback on how you are performing in the project. By now, you should be building upon the foundations set in M1, achieving greater autonomy and collaboration within the team. This is meant to complement the informal, ungraded feedback from your coaches given during the weekly meetings or asynchronously on Discord, email, etc.

The feedback focuses on how well your team has applied good software engineering practices, delivered user value, and collaborated effectively. We assessed the quality and maintainability of your implementation, the clarity of your design, and the consistency of your delivery and teamwork across Sprints. An important component is how much you have progressed since the previous milestone. You can find the evaluation criteria in the [M2 Deliverables](https://github.com/swent-epfl/public/blob/main/project/M2.md) document. As mentioned in the past, the standards for M2 are elevated relative to M1, and this progression will continue into M3.

## Black Belt

You qualified for a Black Belt 🥋⚫ and got a final grade of 5.55/6 for M2. Fantastic! You've demonstrated true mastery. 

We looked at several aspects, grouped into six categories. Here is the breakdown of the points you earned:

| Metric                                   | **Points Earned**              | **Weight** | **Feedback**                              |
|------------------------------------------|--------------------------------|------------|-------------------------------------------|
| **Implementation (APK, code quality)**   | 5.38 out of 6 | 40%        | [See Details](#implementation-apk-code-quality) |
| **Features**                             | 5.5 out of 6   | 20%        | [See Details](#features)                  |
| **Figma and Architecture Diagram**       | 6 out of 6 | 10%     | [See Details](#figma-and-architecture-diagram) |
| **Sprint Backlog & Product Backlog**     | 6 out of 6    | 10%        | [See Details](#sprint-backlog--product-backlog) |
| **Scrum Process (documents, autonomy)**  | 6 out of 6 | 10%       | [See Details](#scrum-process-documents-autonomy) |
| **Consistent Delivery of Value**         | 5 out of 6 | 10%      | [See Details](#consistent-delivery-of-value) |
| **Final Grade**                          | **5.55 out of 6**   |            |                                           |

In addition to the feedback you received from the coaches during the Sprints, you will find some extra feedback below.

---

## Implementation (APK, code quality)

We evaluated your APK’s functionality, stability, and user experience, along with the quality and consistency of your code. We also reviewed your CI setup, Sonar integration, and tests, including the presence of at least two meaningful end-to-end tests and the line coverage achieved.

We have identified some bugs or issues which lightly affect the user experience: 
Regarding User Experience: The screen currently shows “Sign In” instead of “Sign Up,” which may confuse new users about how to create an account.
Profile picture flow:
There’s no “Add” button when the user doesn’t yet have a profile picture, but a “Delete” button is shown.
Users must navigate back to see their profile picture, and it isn’t visible within Settings. Improving this flow would help with usability.
Interests list: The “Transport” entry appears under “Interests,” but it doesn’t fit well as a user interest. Revisiting the category list could improve relevance.
Chat UI: There are a few visual/UI issues in the chat view that may need attention.
Events page:
Event cards are currently not clickable.
Long event descriptions block the title/description area.
Users can create an event in the past.
Map interactions: Selecting events directly from the map is difficult.

For this part, you received 5.38 points out of a maximum of 6.

## Features

We evaluated the features implemented in this milestone. We looked for the completion of at least one epic, as well as the use of at least one public cloud service, one phone sensor, and a concept of authentication.
We assessed how well the implemented features align with the app’s objectives, integrate with existing functionality, and contribute to delivering clear user value.

Your app has evolved from M1, but there are still bugs in some of the features you implemented. Having said that, you have the highest line coverage amongst all teams, well done!

For this part, you received 5.5 points out of a maximum of 6.

## Figma and Architecture Diagram

We evaluated whether your Figma and architecture diagram accurately reflect the current implementation of the app and how well they align with the app's functionality and structure. 

Everything is perfect! Good job.

For this part, you received 6 points out of a maximum of 6.

## Sprint Backlog & Product Backlog

We assessed the structure and clarity of your Sprint and Product Backlogs and how you used them. We looked at whether your tasks are well defined, appropriately sized, and aligned with user stories; whether the Product Backlog is well organized and value-driven; and whether the Sprint Backlog is continuously updated and demonstrates good planning and prioritization.

Everything is perfect! Good job.

For this part, you received 6 points out of a maximum of 6.

## Scrum Process (documents, autonomy)

We evaluated your ability to autonomously run and document the Scrum process. We looked at how well you documented your team Stand-Ups and Retrospectives for each Sprint. We also assessed your level of autonomy in organizing and conducting these ceremonies, defining and prioritizing user stories in your Product Backlog, and planning well-scoped Sprint tasks that lead to concrete, valuable increments.

Everything is perfect! Good job.

For this part, you received 6 points out of a maximum of 6.

## Consistent Delivery of Value

We reviewed your team’s ability to deliver meaningful increments of value at the end of each Sprint.  
We assessed whether your progress was steady, visible, and tied to concrete user value and app functionality, in line with your Product Backlog objectives.

The team delivered meaningful value consistently across the last sprints, with visible improvements and new usable functionality in each increment. Delivery appears well-organized and aligned with priorities. There is still some room for refinement and stronger polish to reach full consistency and robustness, but overall progress between sprints was clear and impactful.

For this part, you received 5 points out of a maximum of 6.

## Summary

Your team grade for milestone M2 is 5.55/6. If you are interested in how this fits into the bigger grading scheme, please see [project README](https://github.com/swent-epfl/public/blob/main/project/README.md) and the [course README](https://github.com/swent-epfl/public/blob/main/README.md).

Your coaches will be happy to discuss the above feedback in more detail.

Keep up the good work and good luck for the next milestone!