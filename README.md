![RookBoom](http://blog.linkedin.com/wp-content/uploads/2012/12/gobook.png)

What is RookBoom?
===

RookBoom is a web application for creating meetings. It displays calendars of participants together with conference
rooms and other resources. Suggested time slots are highlighted for convenience, making it easier to find a time and place for a meeting in a typical busy office space.

Unlike other meeting scheduling applications (e.g., Microsoft Outlook), RookBoom maintains a list of all available
conference rooms. Users don't need to remember every possible conference room they could use while looking for an
available time slot.

RookBoom works on top of an existing calendar database (e.g., Microsoft Exchange). Data is synchronized both to and from RookBoom, allowing interoperability with all existing clients.

License
===

    (c) Copyright 2014 LinkedIn Corp. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Installation
===

Requirements
---

- Microsoft Exchange 2010 or higher
- MySQL 5
- Java 6
- Jetty 8

Building from source code
---

To build the application from source code, run the following commands:

    git clone git@github.com:linkedin/RookBoom.git
    cd RookBoom
    mvn install

This gives you a WAR file ready to be deployed into a Jetty container (`web/target/RookBoom.war`).

Configuration
---

Copy the `config` directory to a place accessible from Jetty. Configure Jetty environment variables to have `rookboom.config.dir` pointing to the `config` directory location (e.g., using `/etc/default/jetty` to pass the location).

Update `config/config.properties` according to instructions contained in it.

Database
---

If you do not already have a working MySQL server, download the 'MySQL Community Server' installation package from the
[download page](http://dev.mysql.com/downloads/) and follow the [instructions](http://dev.mysql.com/doc/index-topic.html).

Create the database and the user by running the `services/src/main/db/create.sql` script. Change the password for the
user created (don't forget to update `config/config.properties`). Apply the new user's privileges by running
`FLUSH PRIVILEGES`.

Security
---

We recommend that you run RookBoom behind an HTTPS proxy, because otherwise users' credentials would be sent unencrypted.

JavaMelody monitoring page may need to be secured. Refer to
[JavaMelody user guide](http://code.google.com/p/javamelody/wiki/UserGuide#16._Security).

The database is created with the default user and password. Make sure to change that.

Jetty
---

If you want to deploy RookBoom without an extension, copy `RookBoom.war` to `$JETTY_HOME/webapps`. The application
should now be accessible at the `/rookboom` context path. If you prefer to have it at the root context path, rename the
WAR to `ROOT.war`.

To deploy the application with a custom extension, you need to create a deployment descriptor file similar to the following example, and put it into `$JETTY_HOME/contexts`.

    <!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_0.dtd">
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      <Set name="contextPath">/</Set>
      <Set name="war">/opt/RookBoom/bin/RookBoom.war</Set>
      <Set name="extraClasspath">/opt/RookBoom/bin/extension.jar</Set>
    </Configure>

For more information about deployment descriptors, check the official Jetty
[documentation](http://www.eclipse.org/jetty/documentation/current/configuring-specific-webapp-deployment.html).


Troubleshooting
===

RookBoom comes with JavaMelody bundled. You can get information about your application's health by looking into its
monitoring page at `https://yourhost/monitoring`.

Check RookBoom logs for errors. You might want to enable logging of all communications with the Exchange server. To do so, uncomment the corresponding lines in `config/log4j.xml`.


Development
===

This section explains how to run RookBoom in development mode and to write extensions for it.

Dev mode
---

To run RookBoom in development mode, change `config.properties` inside the repo according to your environment, then run the `mvn jetty:run` command in the `web` folder. This brings the application up in a Jetty instance,
launched by the Maven Jetty plugin. The application is now available at `http://localhost:12000`.

You may need to change your VM configuration parameters to give the application more memory. For example, `-Xmx1024m -XX:MaxPermSize=256m`.

You can also run the previous Maven command from an IDE to debug and hot reload your changes.

You can change `log4j.xml` to enabled console logging.

Extension
---

RookBoom supports extending it for custom fitting into an existing infrastructure. You can override behavior of all core services with your own implementation.

The extension interface is defined by the `com.linkedin.RookBoom.extension.ServiceFactory` trait. To extend
RookBoom, you need to implement the trait and specify the fully-qualified class name of the implementation in the
`service.factory` property. The implementation and all its dependencies must be available in the classpath.

Instead of implementing a new service factory from scratch, you can extends the default one. Create a new class extending `com.linkedin.RookBoom.extension.DefaultServiceFactory` and override one or more methods in it. The following example shows such a class:

    class SampleServiceFactory(context: ServiceContext) extends DefaultServiceFactory(context) with Logging {

      override lazy val layoutManager: LayoutManager = {
        log.info("Creating FSLayoutManager")
        val layoutDir = getConfigFile("layouts")
        val layoutManager = new FSLayoutManager(layoutDir)
        val reloadTrigger = context.getProperty("layout.reload.cron")
        context.schedule(reloadTrigger, layoutManager.reload(), true)
        layoutManager
      }

    }

Now you can run the application with your extension using the following command: 
`mvn jetty:run -Dextension.classpath=<path to the JAR> -Dextension.config=<path to the config dir>`

----------


User Manual
===


The RookBoom screen
===

1. **Global Menu** -- contains global actions:
    * log in/log out
    * about page
    * location chooser
2. **Settings/Filter toggle** -- opens/closes the settings/filters dialog
3. **Recurrent settings toggle** -- opens/closes recurrent settings dialog
4. **Date label** -- displays the selected date (in the one-time meeting mode) or recurrent pattern description (in the recurrent meeting mode)
5. **Time window labels** -- time frame for the selected date
6. **Current time mark** -- if the selected date is today and the current time is within the time window, the vertical line marks the current time
7. **Schedule area** -- the main data area, contains schedule for all attendees and conference rooms


One-time meeting scheduling
===

The one-time meeting scheduling mode is the default mode, so you don't have to do anything to enable it.

Finding a room
---
The schedule area displays data for a day indicated by the date label and for the time frame indicated in the header. By default, it's the current day from 8:30am to 7:30pm.
There are two options to change the date:

* Using the left/right arrow around the date label takes you to the previous/next day.
* Clicking the date label displays the date chooser calendar control. To change the time frame, use the 'Time window' setting (see 'Using settings and filters').

You can also change the time frame. In the settings/filters dialog, you have three options for the 'Time window' property:

* **Morning** -- 12:30am to 11:30am
* **Day** -- 8:30am to 7:30pm
* **Night** -- 1:30pm to 12:30am (next day)

In the schedule area, you can see every room and if it's free or busy at the given time. Busy time is indicated by the dashed rectangle spanning through the whole time frame of the event. If you click on this busy label, the information box displays with information about the meeting owner.

There are a number of options to narrow down the list of the rooms in the schedule area. Those options are available in the settings/filters dialog:

* **building** -- building name
* **floor** -- floor number
* **size** -- conference room size
* **features** -- room features (e.g., video conference)

Those options may or may not be there: if the some option has the soma value for all rooms it is not displayed. For example, if in some location all conference rooms are on the first floor the 'floor' option is not available in the filters. Disclaimer: options availability requires server-side support and is only available with a proper configuration.

After you locate the room and time that work for you, you can continue to the booking step.

Booking a room
---
Booking a room is an action performed on an Exchange server on behalf of the RookBoom user so at this time you'll need to log into the system to proceed. Use your LDAP credentials (that is, the same credentials you're using to access your mailbox) to log into the RookBoom.

Once you logged in and found the date, time, and room you want to book in the schedule area, select the cell that corresponds to the start time of the event. That is, if you want to book the Batman conference room from 12pm to 1pm, you should select the 12pm-12:30pm cell in the 'Batman' row.
In the displayed dialog, you can set up the duration, location (a prefilled text field with a description, including the room name and location), subject, and body of the invitation message. You can also add meeting attendees in the dialog.
After you have set up the meeting properties, proceed with booking ('Book!' button) or you can choose to cancel ('Cancel').

After the booking request is sent, the page freezes showing a progress indicator. Wait until the RookBoom receives the response to the booking request and the result is displayed. At this point, there is no guarantee that the room is booked.

After booking request is processed the notification of the result is displayed. If no error was identified, you'll see a success message; otherwise, the failure message is shown with (possibly) some explanation message.

> **IMPORTANT!**
The final confirmation of the room being booked is an email from the room's email address with an 'Accepted' status.

Adding attendees
---
As was previously described, you can add attendees to the meeting invitation in the booking dialog. However, most of the time you will want to take into account their schedule on the step of choosing time for the meeting. RookBoom gives an option not only to see attendees' schedule but it also highlights the time that works better for everyone.

To add a person to the attendees list, type a name or email in the attendees input box. You can also use the same box to add mailing lists, the only exception is that mailing lists don't have any schedule information available, so all time is considered to be free. Also, if you are logged into the system your own schedule displays by default.

After there are attendees in the list, the highlighting feature turns on: now for every conference room the available time slots are being highlighted differently depending on how a specific time works for attendees. The following color codes are used:

* **solid green** -- the time slot works for **all** attendees in the list, that is, everyone
* **dashed green** -- the time slot works for more than half of the attendees
* **no highlighting** -- room is available, but more than half of the attendees have this time busy

Currently, RookBoom doesn't support optional attendees, all attendees are considered to be required.

Roomless meetings
---
You can also schedule a meeting that is happening outside of conference rooms. The interface includes a row with a special location for that: 'Anywhere'. No time is ever busy for this location and invitations are sent only to the meeting owner and attendees.

This can also be used as a method to book your own time while you are out of office.


Recurrent meeting scheduling
===
Finding a room for a recurrent meeting is something that is almost impossible to do in the Outlook scheduling assistant. The only way to do that is to try booking and see what happens -- if at least one occurrence of your meeting is conflicting with any other booking, the entire request is rejected. RookBoom introduces a solution for the problem that allows you to see whether or not the requested time is available before you send the actual invitation.

Finding a room
---
To switch to the recurrent meeting scheduling mode use the Recurrent Settings Toggle control. The form displays to allow you to set up the recurrence pattern:

* **Every** field -- controls the pattern period
* **On** field -- lets you select days within a week when the meeting will occur
* **Start Date** -- the day when the schedule takes effect
* **End After** -- how long the schedule will be effective

This doesn't cover all possible patterns, but is enough for the most frequently used cases. Here are some examples:

* **Every week day for 2 months, effective 1/1/2014**

    > **Every** _week_
    > **On** _Mon, Tue, Wed, Thu, Fri_
    > **Start Date** _1/1/2014_
    > **End After** _2 month_

* **Every second Monday for 4 months, effective 1/1/2014**

    > **Every** _other week_
    > **On** _Mon_
    > **Start Date** _1/1/2014_
    > **End After** _4 month_

Note: The Exchange server may have policy settings that could prevent you from scheduling a recurrent meeting too far ahead.

Once you configure the pattern, you can apply it with the Show Schedule button. The result shown in the Schedule Area is no longer a single day schedule. Instead, it's a schedule that is calculated by intersecting the schedules for all days described by the recurrence pattern. For example, if you are scheduling an everyday event and a room has a 1pm to 2pm meeting on one day and a 2pm to 3pm meeting on the other day, the calculated schedule displays the entire 1pm to 3pm time as booked. So if the time slot is free in the aggregated schedule, that means that this slot is free for all days described by the pattern.

Booking a room, adding attendees
---
These steps are the same as in the single meeting mode.


Canceling a meeting
===
Once you logged into the RookBoom system in the Schedule Area, you can see the meetings created by you marked with a flag. If you click on the meeting label, the dialog displays, allowing you to cancel the meeting. If the meeting is recurrent, all occurrences are canceled.


Things you can't do (yet)
===
Some features are not yet supported by RookBoom. Some of them are:

* Editing a meeting (adding/removing attendees, updating time/location)
* Canceling a single occurrence of the recurrent meeting
* Booking several conference rooms at a time
* Less commonly used recurrence patterns (like 'first day of the month')

----------

FAQ
===

### 1. RookBoom says 'booking successful', but the room is not booked
#### Problem
RookBoom says 'booking successful', but the room is not booked.
#### Background
We do our best to verify that the room accepts the invitation. But in practice rooms' behaviour may vary depending on the configuration. So the source of truth here is always the email from the room with a confirmation.

### 2. Double Booking
#### Problem
Several people claim to book the same room for the same or overlapping time.
#### Background
Double booking is frequent complaint which is in fact a result of misunderstanding of the Exchange booking mechanics. The misunderstanding is based on the assumption that meeting request fails when the room rejects the booking, which is wrong. The truth is that room acts like any other attendee -- it can accept, reject or even ignore the invitation. The *meeting* however will still be created and the time in the creators calendar will be booked. But the main and the final confirmation of the room booking is an email from the room with the 'Accept' status. 
The actual double booking would mean that two or more people received an 'Accepted' email from the same room for the same (or overlapping) time which we've never seen. 
#### Resolution
Whenever someone complains that double booking happened please provide an explanation of the booking mechanics and ask to provide two emails from the room in question with the 'Accept' status for overlapping time.

### 3. RookBoom failed to book a room
#### Problem
While attempting to schedule a meeting RookBoom interface responds with an error message or doesn't respond at all. 
#### Resolution
Several reasons are possible here:

- **user can't book anything at all** -- it's most likely an issue described in 6
- **booking fails for any time slot for this room only** -- it's most likely an issue described in 5
- **booking fails for some specific time slot for a specific room** -- it's most likely an issue described in 4
- **interface doesn't respond** -- this happens sometimes when Exchange request times out, the booking request however may or may not go through

The best point to start is an email that user gets back from the room after the booking request. This email contains a reason why the booking was rejected. Different explanations are possible, but it always boils down to the room config or user config (password expired), we've never had an issue that required code changes.

### 4. RookBoom schedule is different from the Outlook schedule
#### Problem
Some room that is displayed as booked in Outlook is displayed as available in RookBoom (or other way around).
Sometime people may complain that booking failed because of the conflicting meeting while the slot was available in the RookBoom.
#### Background
RookBoom syncs up with Exchange once every X minutes (where X is defined in the configuration). There may be a short period of time in between 2 synchronizations where RookBoom displays stale data.  If the room was booked via Outlook (or the booking was canceled or modified) it may take up to X min before the booking will appear in the RookBoom. 

### 5. A restricted access room is displayed in RookBoom
#### Problem
A restricted access room is displayed in RookBoom. When a user without required permissions tries to book it the request fails.
#### Background
Exchange allows to configure access permissions for individual resources. RookBoom shows all rooms despite their restrictions.

### 6. All booking requests fail for me
#### Problem
The most probable cause (and in fact the only we had so far) is that user's password was recently updated. RookBoom doesn't perform automatic log out, so after the password change user should manually re-log in to the RookBoom.
#### Resolution
Ask user to re-login to the RookBoom.

### 7. Tentative time displayed as free
#### Problem
If someone replies with a tentative status to the meeting this time will be marked as free in the user's schedule in RookBoom.
#### Background
Exchange supports 3 time slot states: Free, Busy and Tentative and the Outlook UI displays all three states differently. In RookBoom we made a decision to support just 2 states (Free and Busy) so we had to convert tentative state to one of them. We made a choice to treat tentative time as free.

### 8. A user can't log in using his/her AD/LDAP credentials
#### Problem
A user is able to login into Outlook, but not in RookBoom
#### Background
It's possible that user's LDAP entry is misconfigured (specifically the account name).
#### Resolution
Verify that the 'samaccountname' LDAP attribute is equal to the user's login.