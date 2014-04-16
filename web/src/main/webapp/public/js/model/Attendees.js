/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function compareByEmail(user) {
    return user.user.address;
}

var Attendees = Backbone.Model.extend({

    defaults: {
        events: [],
        attendees: [],
        waiting: false
    },

    initialize: function () {
        _.bindAll(this, 'onDateChange', 'onData', 'addAttendee', 'onNewAttendee', 'removeAttendee', 'updateByEmailsString', 'withRecurrence');
        this.get('filterValues').bind('change:day', this.onDateChange);
        this.get('filterValues').bind('change:timeframe', this.onDateChange);
        this.get('filterValues').bind('change:location', this.onDateChange);
        this.get('filterValues').bind('change:recurrence', this.onDateChange);
    },

    onData: function (data) {
        if (this.get('location').getDayForLocation() != data.day) {
            return;
        }

        var events = _.uniq(_.flatten(_.map(data.result, function (u) {
            return _.map(u.schedule, function (e) {
                return e.eventId;
            });
        })));

        var uniqueAttendees = _.uniq(data.result, false, compareByEmail);
        _.each(uniqueAttendees, itemSchedulePatch);

        this.set({
            events: events,
            attendees: uniqueAttendees,
            waiting: false
        });
    },

    onNewAttendee: function (data) {
        itemSchedulePatch(data);
        var arr = this.get('attendees');
        arr.push(data);
        arr = _.uniq(arr, false, compareByEmail);

        var events = _.uniq(_.union(this.get('events'), _.map(data.schedule, function (a) {
            return a.eventId;
        })));

        this.set('events', events, SILENT);
        this.set('attendees', [], SILENT);
        this.set('attendees', arr, SILENT);
        this.change();
    },

    addAttendee: function (email) {
        $.ajax($.withErrorHandling({
            url: '/schedule/user',
            data: this.withRecurrence({
                email: email,
                day: this.get('location').getDayForLocation(),
                timeframe: this.get('filterValues').get('timeframe'),
                location: this.get('location').getCurrentSiteId()
            }),
            success: this.onNewAttendee,
            errorMsg: 'Unable to get user schedule'
        }));
    },

    removeAttendee: function (email) {
        var arr = _.uniq(_.reject(this.get('attendees'), function (a) {
            return a.user.address === email;
        }), false, compareByEmail);
        this.set('attendees', arr);
    },

    onDateChange: function () {
        var attendees = this.get('attendees');
        if (attendees.length === 0) {
            return;
        }
        var emails = _.map(attendees,function (att) {
            return att.user.address;
        }).join();
        this.updateByEmailsString(emails);
    },

    updateByEmailsString: function (emailsString) {
        this.set({
            attemdees: [],
            waiting: true
        });
        $.ajax($.withErrorHandling({
            url: '/schedule/users',
            data: this.withRecurrence({
                emails: emailsString,
                day: this.get('location').getDayForLocation(),
                timeframe: this.get('filterValues').get('timeframe'),
                location: this.get('location').getCurrentSiteId()
            }),
            success: this.onData,
            errorMsg: 'Unable to get users schedule'
        }));
    },

    withRecurrence: function(data) {
        var recurrence = this.get('filterValues').get('recurrence');
        if (recurrence.active) {
            return _.extend({}, data, recurrence.params);
        }
        return data;
    }
});
